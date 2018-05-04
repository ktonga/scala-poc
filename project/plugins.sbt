scalacOptions ++= Seq("-unchecked", "-deprecation")
ivyLoggingLevel := UpdateLogging.Quiet

addSbtPlugin("com.geirsson"  % "sbt-scalafmt" % "1.4.0")
addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.6.0-M5")
