# scala-actors-graphics

This repo contains some examples we went over in class.

## Actor Test

A simple example wherein we create and send messages to an actor.

The source code is at

~~~
src/main/scala/edu/colorado/csci3155/actorTest/ActorTest.scala
~~~

To run this example try the following command on the main prompt:

`sbt "runMain edu.colorado.csci3155.actorTest.ActorTest"`

Expected output from the example:

~~~
Pre-start of MyActor
Actor received Hello
Actor received World
Actor received Shakespeare Rocks
Child received Child:Hello
Child received Child:World
Child received Child:Shakespeare Rocks
Exit Left!
Child Exit Right!
~~~


## Actor FSM example

Another simple example of finite state machines that pass messages
using Akka actors.

~~~
src/main/scala/edu/colorado/csci3155/actorTest/ActorFSMExample.scala
~~~

To run this from command prompt

~~~
sbt "runMain  edu.colorado.csci3155.actorTest.ActorFSMExample"
~~~

Press CTRL+C to stop it.

## Clock Display Example

This example was presented in class. The source code is at

The actors are defined here
~~~
src/main/scala/edu/colorado/csci3155/clockActor/ClockActor.scala
~~~

The canvas for displaying the clock is here:
~~~
src/main/scala/edu/colorado/csci3155/clockActor/ClockDisplay.scala
~~~

The main swing application is here
~~~
src/main/scala/edu/colorado/csci3155/clockActor/ClockDisplayTest.scala
~~~

To run:

~~~
sbt "runMain edu.colorado.csci3155.clockActor.Graphics2DTest"
~~~

## Conway's Game of Life

See here: [https://en.wikipedia.org/wiki/Conway%27s_Game_of_Life]

To run:

~~~
sbt "runMain edu.colorado.csci3155.ConwaysGameOfLifeActor.GameOfLifeMain"
~~~

You can toggle "GO!" / "STOP"  resume/pause the game of life.
Clicking on a cell turns it alive.


Source code files are here:

~~~
src/main/scala/edu/colorado/csci3155/ConwaysGameOfLifeActor/
~~~


