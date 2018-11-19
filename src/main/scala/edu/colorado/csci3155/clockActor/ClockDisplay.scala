package edu.colorado.csci3155.clockActor
import scala.swing._
import event._
import scala.swing.BorderPanel.Position._
import java.awt.{BasicStroke, Color, Graphics2D}
import java.awt.geom._


class ClockDisplay extends Panel {
    var sec:Int = 0
    var min: Int = 0
    var hour: Int = 0
    val secondHandLength: Double = 80.0
    val minuteHandLength: Double = 65.0
    val hourHandLength: Double = 50.0

    def drawSecondHand(g: Graphics2D): Unit  = {
        val xC: Double = size.width/2.0
        val yC: Double = size.height/2.0
        g.setColor(Color.BLACK)
        g.setStroke(new BasicStroke())
        g.draw(new Line2D.Double(xC, yC,
            xC + secondHandLength * math.sin(math.Pi/30*sec),
            yC - secondHandLength * math.cos(math.Pi/30*sec)))
    }
    def drawMinuteHand(g: Graphics2D): Unit  = {
        val xC: Double = size.width/2.0
        val yC: Double = size.height/2.0
        g.setColor(Color.BLACK)
        g.setStroke(new BasicStroke())
        g.draw(new Line2D.Double(xC, yC,
            xC  + minuteHandLength * math.sin(math.Pi/30*min),
            yC  -  minuteHandLength * math.cos(math.Pi/30*min)))
    }

    def drawHourHand(g: Graphics2D) = {
        val xC: Double = size.width/2.0
        val yC: Double = size.height/2.0
        g.setColor(Color.BLACK)
        g.setStroke(new BasicStroke())
        g.draw(new Line2D.Double(xC, yC,
            xC + hourHandLength * math.sin(math.Pi/6*hour),
            yC - hourHandLength * math.cos(math.Pi/6*hour)))
    }
    override def paintComponent(g: Graphics2D): Unit = {
        val xC: Double = size.width/2.0
        val yC: Double = size.height/2.0
        // Draw the second hand

        drawMinuteHand(g)
        drawHourHand(g)
        drawSecondHand(g)
        // Draw a yellow circle in the midle
        g.setColor(Color.YELLOW)
        g.fill(new Ellipse2D.Double(xC-5, yC-5, 10, 10))
    }

    def setHour(hr: Int)= {
        hour = hr
        repaint()
    }
    def setMinute(m: Int) = {
        min = m
        repaint()
    }
    def setSecond(s: Int) = {
        sec = s
        repaint()
    }

}
