package edu.colorado.csci3155.ConwaysGameOfLifeActor

import java.awt.Graphics2D
import akka.actor.{Actor, ActorRef, ActorSystem, Props}

case object Tick
case object Terminate
case class StateUpdate(id: (Int, Int), isAlive: Boolean)
case class PaintYourself(g: Graphics2D, x: Double, y: Double, w: Double, h: Double)

case class NeighborDescription(id: (Int, Int), acRef: ActorRef, var actorIsAlive: Boolean)

class GameOfLifeCell (var amAlive: Boolean = false, val myId: (Int, Int), actSys: ActorSystem) {


    var neighborList: List[NeighborDescription] = Nil
    def id: (Int, Int) = myId
    def isAlive: Boolean = amAlive

    def addNeighbor(g: GameOfLifeCell): Unit = {
        this.addNeighbor(g.id, g.getActorRef, g.isAlive)
    }

    def addNeighbor(id: (Int, Int), acRef: ActorRef, isAlive: Boolean): Unit = {
        neighborList = NeighborDescription(id, acRef, isAlive) :: neighborList
    }

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

    def sendMessagesToNeighbor(): Unit = {
        neighborList foreach {
            case NeighborDescription(_, acRef, _) => acRef ! StateUpdate(myId, amAlive)
        }
    }

    def updateNeighborState(id: (Int,Int), isAlive: Boolean): Unit = {
        neighborList = neighborList.map {
            case NeighborDescription(nid, acRef, nAlive) =>
                if (id == nid) NeighborDescription(nid, acRef, isAlive)
                else NeighborDescription(nid, acRef, nAlive)
        }
    }

    class GameOfLifeActor extends Actor {
        override def receive: Receive = {
            case StateUpdate(id, isAlive) =>
                updateNeighborState(id, isAlive)


            case Tick =>
                updateMyState()
                sendMessagesToNeighbor()

            case Terminate => context.stop(self)
        }
    }
    def props(): Props = Props(new GameOfLifeActor())

    val gActor : ActorRef = {
        println(s"Creating cell $myId")
        actSys.actorOf(props(), name=s"Cell${myId._1}_${myId._2}")
    }

    def getActorRef: ActorRef = gActor
}


class GameOfLifeBoard (numRows: Int, numCols: Int) {
    val actSys = ActorSystem("GameOfLife")
    var cellList: List[List[GameOfLifeCell]] = (0 until numRows).toList.map(i => {
        (0 until numCols).toList.map(j => {
            new GameOfLifeCell(math.random < 0.5, (i, j), actSys)
        })
    })
    for (i <- 0 until numRows) {
        for (j <- 0 until numCols) {
            val g = cellList(i)(j)
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

    def getCellState(i: Int, j: Int) = cellList(i)(j).isAlive

    def setCellState(i: Int, j: Int, alive: Boolean): Unit = {
        cellList(i)(j).amAlive = alive
        cellList(i)(j).sendMessagesToNeighbor()
    }

    def updateCell(i: Int, j: Int) = cellList(i)(j).updateMyState()
    def messageNeighbors(i: Int, j: Int) = cellList(i)(j).sendMessagesToNeighbor()
}
