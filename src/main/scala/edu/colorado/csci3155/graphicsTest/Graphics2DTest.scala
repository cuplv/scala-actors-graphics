package edu.colorado.csci3155.graphicsTest
import scala.swing._
import event._
import scala.swing.BorderPanel.Position._
import java.awt.{Graphics2D,Color}
import java.awt.geom._
import scala.util.Random

class MyCanvas extends Panel {
    var parts = List[(Int,Int)]()

    override def paintComponent(g: Graphics2D) = {
        // Clear this canvas
        g.clearRect(0,0, size.width, size.height)
        g.setColor(Color.blue)
        g.fillOval(0,0,100,100)
        g.setColor(Color.red)
        g.fillRect(50,50,150,150)

        for (coords <- parts) {
            g.setColor(Color.green)
            g.fillRect(coords._1, coords._2, 10, 10)
        }
    }

    def addNewCoord(x: Int, y: Int) = {
        parts = (x,y)::parts
        repaint()
    }
}

object Graphics2DTest extends SimpleSwingApplication {
    def top = new MainFrame {
        title = "Sriram's Basic Test Application"
        val canvas = new MyCanvas {
            preferredSize = new Dimension(300,300)
        }
        val button = new Button {
            text = "Throw!"
            foreground = Color.blue
            background = Color.red
            borderPainted = true
            enabled = true
            tooltip = "Click and See"
        }
        contents = new BorderPanel {
            layout(button) = South
            layout(canvas) = Center
        }
        size = new Dimension(320,350)
        listenTo(button)
        listenTo(canvas.mouse.clicks)
        reactions += {
            case ButtonClicked(c) if c == button =>
                val x = Random.nextInt(300)
                val y = Random.nextInt(300)
                canvas.addNewCoord(x, y)
            case MouseClicked(_, point, _, _, _) =>
                canvas.addNewCoord(point.x, point.y)
        }
    }
}