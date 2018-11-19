package edu.colorado.csci3155.actorTest

import akka.actor._

case class StrMessage(s: String)
case object ExitMessage

class ChildActor extends Actor {
    override def receive = {
        case StrMessage(s) => {
            println(s"Child received $s")
        }
        case ExitMessage => {
            println("Child Exit Right!")
            context.stop(self)
        }
    }
}

class MyActor extends Actor {
    // Pattern used to create a child actor
    val child = context.actorOf(Props[ChildActor], name = "child")
    override def preStart(): Unit = {
        println("Pre-start of MyActor")
    }

    override def receive= {
        // I handle all the messages
            case StrMessage(s) => { println(s"Actor received $s"); child ! StrMessage("Child:"+s) }
            case ExitMessage => {
                println("Exit Left!");
                child ! ExitMessage
                context.stop(self)
            }
        }
}

object ActorTest extends App {
    val system = ActorSystem("PingPongSystem")
    // Create an Actor
    val a =system.actorOf(Props[MyActor], name = "a")
    // Send it a bunch of messages
    a!StrMessage("Hello")
    a!StrMessage("World")
    a!StrMessage("Shakespeare Rocks")
    a!ExitMessage
    // Terminate
    system.terminate()
}
