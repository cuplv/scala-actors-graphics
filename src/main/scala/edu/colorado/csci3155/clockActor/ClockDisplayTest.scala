package edu.colorado.csci3155.clockActor

import java.awt.Color


import scala.swing.BorderPanel.Position.{Center, South}
import scala.swing.{BorderPanel, Button, Dimension, MainFrame, SimpleSwingApplication}
import scala.swing.event.{ButtonClicked, MouseClicked}
import scala.util.Random


object Graphics2DTest extends SimpleSwingApplication {
    def top = new MainFrame {
        title = "Sriram's Basic Test Application"
        val canvas = new ClockDisplay {
            preferredSize = new Dimension(300, 300)
        }
        canvas.setHour(8)
        canvas.setMinute(15)
        canvas.setSecond(32)
        canvas.repaint()
        val button = new Button {
            text = "STOP"
            foreground = Color.BLUE
            background = Color.RED
            borderPainted = true
            enabled = true
            tooltip = "Click and See"
        }
        contents = new BorderPanel {
            layout(canvas) = Center
            layout(button) = South
        }
        listenTo(button)
        size = new Dimension(320, 320)
        val (cancellable, clockActor) = CreateClockActorAndGo.doIt(canvas)
        reactions += {
            case ButtonClicked(c) if c == button =>
                cancellable.cancel()
                clockActor!Stop
        }
    }
}
