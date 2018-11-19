/*
    Author: Sriram Sankaranarayanan
    srirams@<what is the centennial state?>.edu
 */
package edu.colorado.csci3155.ConwaysGameOfLifeActor

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

/* Messages that our actor will receive */
case object Terminate /* Stop the actor */
case class StateUpdate(id: (Int, Int), isAlive: Boolean) /* Actor id is sending a message with its current state*/

/*NeighborDescription class describes the state of a neighboring cell. It is updated each time
a StateUpdate message with a matching id is received.
Note that each cell has id (i,j) which is its position in the board.
*/

case class NeighborDescription(id: (Int, Int), acRef: ActorRef, var actorIsAlive: Boolean)

/* Class GameOfLifeCell
    Models a single cell of the game of life
 */
class GameOfLifeCell (var amAlive: Boolean = false, val myId: (Int, Int), actSys: ActorSystem) {

    /* Who are my neighbors? What do I know about them? */
    var neighborList: List[NeighborDescription] = Nil /* No neighbors to start with */

    def id: (Int, Int) = myId /* getter method for id */

    def isAlive: Boolean = amAlive /* getter method for isAlive */

    /* Add a new neighbor give its GameOfLife Cell */
    def addNeighbor(g: GameOfLifeCell): Unit = {
        this.addNeighbor(g.id, g.getActorRef, g.isAlive)
    }
    /* Add a neighbor given its ID, reference to its actor and isAlive */
    def addNeighbor(id: (Int, Int), acRef: ActorRef, isAlive: Boolean): Unit = {
        neighborList = NeighborDescription(id, acRef, isAlive) :: neighborList
    }
    /* Update my own state given what I know about my neighbors */
    def updateMyState(): Unit = {
    /* From wikipedia
       Any live cell with fewer than two live neighbors dies, as if by underpopulation.
       Any live cell with two or three live neighbors lives on to the next generation.
       Any live cell with more than three live neighbors dies, as if by overpopulation.
       Any dead cell with exactly three live neighbors becomes a live cell, as if by reproduction.*/

        val numLiveNeighbors:Int = neighborList.count { case NeighborDescription(_, _, actorIsAlive) => actorIsAlive }
        if (amAlive && numLiveNeighbors < 2) amAlive = false
        else if (amAlive && numLiveNeighbors <= 3) amAlive = true
        else if (numLiveNeighbors > 3 && amAlive ) amAlive = false
        else if (numLiveNeighbors == 3 && !amAlive) amAlive = true
    }

    /* Send messages about myself to my neighbors */
    def sendMessagesToNeighbor(): Unit = {
        /* iterate through the list of neighbors */
        neighborList foreach {
            /* for each neighbor, get the actor and send a message StateUpdate(...)*/
            case NeighborDescription(_, acRef, _) => acRef ! StateUpdate(myId, amAlive)
        }
    }

    /* This message is called when an update *isAlive* is received from neighbor with *id* */
    def updateNeighborState(id: (Int,Int), isAlive: Boolean): Unit = {
        /*- Go through list of neighbors one by one -*/
        neighborList = neighborList.map {
            /* if neighbor's id maps the id received, update, or else keep the same */
            case NeighborDescription(nid, acRef, nAlive) =>
                if (id == nid) NeighborDescription(nid, acRef, isAlive) /* this is the neighbor to update*/
                else NeighborDescription(nid, acRef, nAlive) /* this is not the neighbor to update */
        }
    }

    /* The actor is an inner class of the GameOfLifeCell. This makes it easy to create and access the key contents of
    a cell from outside. Note that the actor has access to all the fields of the outer class */
    class GameOfLifeActor extends Actor {
        override def receive: Receive = {
            case StateUpdate(id, isAlive) => /* I received a state update from a neighbor */
                updateNeighborState(id, isAlive)

            case Terminate => context.stop(self) /* I received a message to stop */
        }
    }

    /* This props method is needed to create an instance of  the actor */
    def props(): Props = Props(new GameOfLifeActor())

    /* This is called during the construction of the class. The actor is created then using the props() method */
    val gActor : ActorRef = {
        actSys.actorOf(props(), name=s"Cell${myId._1}_${myId._2}")
    }

    /* A getter  method for the actor reference */
    def getActorRef: ActorRef = gActor
}

/* Game of Life Board creates all cells and populates their neighbors. It forces periodic
* updates to the cells and message passing to neighbours */
class GameOfLifeBoard (numRows: Int, numCols: Int) {
    /* Create an actor system */
    val actSys = ActorSystem("GameOfLife")
    /* Create a list of list (matrix) of cells. Notice how I create a cellList as an immutable
     * using the map method. (0 until numRows).toList creates a list from 0 to numRows - 1 */
    val cellList: List[List[GameOfLifeCell]] = (0 until numRows).toList.map(i => {
        (0 until numCols).toList.map(j => {
            new GameOfLifeCell(math.random < 0.5, (i, j), actSys)
        })
    })
    /* This code is run as part of the constructor: in scala any code placed like this is
     * run when the object instance is first created. */
    for (i <- 0 until numRows) {
        for (j <- 0 until numCols) {
            val g = cellList(i)(j)
            /* add neighbors for each cell. Allow wraparound */
            val iPrev = if (i >= 1) (i - 1) else (numRows - 1)
            val iNext = if (i <= numRows - 2) i + 1 else 0
            val jPrev = if (j >= 1) (j - 1) else (numCols - 1)
            val jNext = if (j <= numCols - 2) j + 1 else 0
            g.addNeighbor(cellList(iPrev)(j))
            g.addNeighbor(cellList(iNext)(j))
            g.addNeighbor(cellList(i)(jPrev))
            g.addNeighbor(cellList(i)(jNext))
            g.addNeighbor(cellList(iPrev)(jPrev))
            g.addNeighbor(cellList(iPrev)(jNext))
            g.addNeighbor(cellList(iNext)(jPrev))
            g.addNeighbor(cellList(iNext)(jNext))

        }
    }

    /* Get the state of a cell */
    def getCellState(i: Int, j: Int) = cellList(i)(j).isAlive
    /* Set the state of a cell */
    def setCellState(i: Int, j: Int, alive: Boolean): Unit = {
        cellList(i)(j).amAlive = alive
        cellList(i)(j).sendMessagesToNeighbor()
    }

    /* Ask a cell to update its state */
    def updateCell(i: Int, j: Int) = cellList(i)(j).updateMyState()
    /* Ask a cell to send messages to its neighbors */
    def messageNeighbors(i: Int, j: Int) = cellList(i)(j).sendMessagesToNeighbor()
    /* Stop everything */
    def stopAll(): Unit = {
        for (i <- 0 until numRows) {
            for (j <- 0 until numCols) {
                cellList(i)(j).getActorRef ! Terminate
            }
        }
    }
}
