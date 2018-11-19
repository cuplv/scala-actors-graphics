package edu.colorado.csci3155.clockActor

import java.util.concurrent.TimeUnit

import akka.actor._

import scala.concurrent.duration._
import scala.concurrent._
import ExecutionContext.Implicits.global

case object Tick
case class SetMinute(min: Int)
case class SetHour(hour: Int)
case class SetSecond(second: Int)
case object Stop


class HourActor(val canvas: ClockDisplay) extends Actor {
    var hour = 0
    def updateHour : Unit = {
        if (hour == 12)
            hour = 0
        canvas.setHour(hour)
    }

    override def receive: Receive = {
        case Tick => {
            hour = hour + 1
            updateHour
        }
        case Stop => context.stop(self)
        case SetHour(hr) => { hour = hr; updateHour }

    }
}

class MinuteActor(val hourActor: ActorRef, val canvas: ClockDisplay) extends Actor {
    var min = 0

    def updateMinute: Unit = {
        if (min == 60) {
            min = 0
            hourActor ! Tick
        }
        canvas.setMinute(min)
    }

    override def receive: Receive =  {
        case Tick => {
            min = (min+1)
            this.updateMinute
        }

        case SetMinute(m) => {
            min = m
            this.updateMinute
        }

        case Stop => context.stop(self)
    }
}

class SecondActor(val minuteActor: ActorRef, val hourActor: ActorRef, val canvas: ClockDisplay)  extends Actor{
    var sec = 0
    def updateSecond: Unit = {
        if (sec == 60) {
            sec = 0
            minuteActor ! Tick
        }
        canvas.setSecond(sec)
    }

    override def receive: Receive =  {
        case Tick => {
            sec = (sec+1)
            this.updateSecond
        }

        case SetSecond(s) => {
            sec = s
            this.updateSecond
        }

        case Stop => context.stop(self)
    }
}

object ActorFactory {
    def createPropsForSecondActor (minuteActor: ActorRef, hourActor: ActorRef, canvas: ClockDisplay) =
        Props(new SecondActor(minuteActor, hourActor, canvas))
    def createPropsForMinuteActor(hourActor: ActorRef, canvas: ClockDisplay) =
        Props(new MinuteActor(hourActor, canvas))
    def createPropsForHourActor(canvas: ClockDisplay) =
        Props(new HourActor(canvas))
}


class ClockActor(val canvas: ClockDisplay) extends Actor {

    //1. Create actors for managing seconds, hours and minutes
    val hrActor = context.actorOf(ActorFactory.createPropsForHourActor(canvas), "houractor")
    val minActor = context.actorOf(ActorFactory.createPropsForMinuteActor(hrActor, canvas), "minuteactor")
    val secActor = context.actorOf(ActorFactory.createPropsForSecondActor(minActor,hrActor,canvas), name = "secondactor")

    override def preStart(): Unit = {
        hrActor! SetHour(8)
        minActor! SetMinute(15)
        secActor !SetSecond(32)
    }

    override def receive: Receive = {
        case Tick => secActor ! Tick
        case Stop => {
            hrActor ! Stop
            secActor ! Stop
            minActor ! Stop
            context.stop(self)
        }
    }

}

object ClockActor {
    def props(canvas: ClockDisplay) = Props(new ClockActor(canvas))
}

object CreateClockActorAndGo {
    def doIt(canvas: ClockDisplay) = {
        val system = ActorSystem("ClockActor")
        val clockActor = system.actorOf(ClockActor.props(canvas))
        val cancellable =
            system.scheduler.schedule(
                0 milliseconds,
                10 milliseconds,
                clockActor,
                Tick)

        (cancellable, clockActor)
    }
}
