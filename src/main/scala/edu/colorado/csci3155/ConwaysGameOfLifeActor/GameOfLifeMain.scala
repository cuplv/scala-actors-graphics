package edu.colorado.csci3155.ConwaysGameOfLifeActor

import java.awt.{BasicStroke, Color, Graphics2D}
import java.awt.geom._

import scala.swing.BorderPanel.Position.{Center, South}
import scala.swing.event.{ButtonClicked, MouseClicked}
import scala.swing.{BorderPanel, Button, Dimension, Frame, MainFrame, Panel, SimpleSwingApplication}


class GameOfLifeCanvas extends Panel {
    val n: Int = 50
    val gBoard = new GameOfLifeBoard(n, n)

    override def paintComponent(g: Graphics2D): Unit = {
        val W = size.width
        val H = size.height
        val w: Double = W.toDouble/n.toDouble
        val h: Double = H.toDouble/n.toDouble
        g.setStroke(new BasicStroke())
        for (i <- 0 until n){
            for (j <- 0 until n){
                val isAlive = gBoard.getCellState(i, j)
                if (isAlive) g.setColor(Color.RED)
                else g.setColor(Color.BLUE)
                g.fill(new Rectangle2D.Double(i.toDouble * w, j.toDouble * h, w, h))
                g.setColor(Color.BLACK)
                g.draw(new Rectangle2D.Double(i.toDouble * w, j.toDouble * h, w, h))
            }
        }
    }

    def registerClick(x: Double, y: Double) = {
        val w: Double = size.width.toDouble/n.toDouble
        val h: Double = size.height.toDouble/n.toDouble
        val i = (x/w).toInt
        val j = (y/h).toInt
        println(s"CLICK: ($i, $j)")
        if (i >= 0 && i < n && j >= 0 && j < n) gBoard.setCellState(i, j, true)
        repaint()
    }

    def updateAll() = {
        for (i <- 0 until n) {
            for (j <- 0 until n) {
                gBoard.updateCell(i, j)
            }
        }
        for (i <- 0 until n) {
            for (j <- 0 until n) {
                gBoard.messageNeighbors(i, j)
            }
        }
        repaint()
    }
}

object GameOfLifeMain extends SimpleSwingApplication {
    override def top: Frame = new MainFrame {
        title = "Game Of Life"
        val canvas = new GameOfLifeCanvas {
            preferredSize = new Dimension(300, 300)
        }
        val button = new Button {
            text = "GO!"
            foreground = Color.blue
            background = Color.red
            borderPainted = true
            enabled = true
            tooltip = "Click and See"
        }
        var on = false
        contents = new BorderPanel {
            layout(button) = South
            layout(canvas) = Center
        }
        size = new Dimension(320,350)
        listenTo(canvas.mouse.clicks)
        listenTo(button)
        var t: Option[java.util.Timer]= None
        reactions += {
            case MouseClicked(_, point, _, _, _) =>{
                canvas.registerClick(point.x, point.y)
            }
            case ButtonClicked(c) if c == button =>{
                if (on){
                    on = false
                    assert( t!= None)
                    t.get.cancel()
                    button.text="GO!"
                } else {
                    on = true
                    button.text = "STOP"
                    t = Some(new java.util.Timer())
                    t.get.schedule(new java.util.TimerTask {
                        override def run(): Unit = canvas.updateAll()
                    }, 100L, 100L)
                }
            }
        }
    }
}
