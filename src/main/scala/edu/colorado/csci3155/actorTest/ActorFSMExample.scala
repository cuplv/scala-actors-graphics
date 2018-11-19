package edu.colorado.csci3155.actorTest

import akka.actor.{Actor, ActorRef, ActorLogging, ActorSystem, Props}
import edu.colorado.csci3155.actorTest.ActorTest.system

case class CharMessage(c: Char)
case class SendActorRef(a: ActorRef)

class FSM2 extends Actor {
    var curState = 1
    var fsm1Actors: List[ActorRef] = Nil
    def setFSM1(fsm1: ActorRef) = {
        fsm1Actors = fsm1::fsm1Actors
    }

    def transition (a: Char): Unit = (curState, a) match {
        case (0, 'a') =>  { curState = 0 }
        case (0, 'b') => {curState = 1}
        case (1, 'a') => {
            for (fsm1 <- fsm1Actors){
                fsm1 ! CharMessage('a')
            }
        }
        case (1, 'b') => {
            curState = 0
        }
    }

    override def receive: Receive = {
        case CharMessage(c) => { println(s"FSM2: state = $curState"); transition (c) }
        case SendActorRef(a) => setFSM1(a)
        case _ => println("Ignoring what I just received.")
    }
}

class FSM1 (fsm2actor: ActorRef) extends Actor with ActorLogging {
    var curState = 0
    def transition(a: Char): Unit = (curState, a) match {
        case (0,'a') => curState = 1
        case (0, 'b') => { curState = 0; fsm2actor ! CharMessage('b') }
        case (1, 'a') => { curState = 1; fsm2actor ! CharMessage('a') }
        case (1, 'b') => { curState = 0; fsm2actor ! CharMessage('b') }
        case _ => { fsm2actor ! CharMessage('c') ; println("FSM1 Done."); context.stop(self) }
    }

    override def receive: Receive = {
        case CharMessage(c) => { println(s"FSM1: state = $curState"); transition (c) }
        case _ => println("Ignoring what I just received.")
    }
}

object FSM1 {
    def props(fsm2actor: ActorRef) = {
        Props(new FSM1(fsm2actor))
    }
}

object ActorFSMExample extends App {
    val system = ActorSystem("FSMActors")
    val fsm2Actor = system.actorOf(Props[FSM2], "FSM2")
    val fsm1Actor = system.actorOf(FSM1.props(fsm2Actor), "FSM1")
    fsm2Actor ! SendActorRef(fsm1Actor)

    for (i <- Range(0, 100)) {
        fsm1Actor ! CharMessage('a')
        fsm1Actor ! CharMessage('b')
    }

}
