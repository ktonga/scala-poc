inThisBuild(
  Seq(
    scalaVersion := "2.12.4",
    scalafmtConfig := Some(file("project/scalafmt.conf")),
    scalafixConfig := Some(file("project/scalafix.conf"))
  )
)

addCommandAlias("fmt", "all scalafmtSbt scalafmt test:scalafmt")
addCommandAlias("check", "all scalafmtSbtCheck scalafmtCheck test:scalafmtCheck")
addCommandAlias("lint", "all compile:scalafixTest test:scalafixTest")
addCommandAlias("fix", "all compile:scalafixCli test:scalafixCli")

val derivingVersion = "0.13.0"
val http4sVersion   = "0.18.9"

libraryDependencies ++= Seq(
  "org.scalaz"                 %% "scalaz-core"            % "7.2.21",
  "org.scalaz"                 %% "scalaz-ioeffect"        % "1.0.0",
  "org.http4s"                 %% "http4s-dsl"             % http4sVersion,
  "org.http4s"                 %% "http4s-blaze-server"    % http4sVersion,
  "org.http4s"                 %% "http4s-blaze-client"    % http4sVersion,
  "org.http4s"                 %% "http4s-argonaut"        % http4sVersion,
  "io.argonaut"                %% "argonaut-scalaz"        % "6.2",
  "com.github.alexarchambault" %% "argonaut-shapeless_6.2" % "1.2.0-M6",
  "com.github.mpilquist"       %% "simulacrum"             % "0.12.0",
  "eu.timepit"                 %% "refined-scalaz"         % "0.8.7",
  "com.fommil"                 %% "deriving-macro"         % derivingVersion % "provided",
  "com.fommil"                 %% "scalaz-deriving"        % derivingVersion,
  "org.specs2"                 %% "specs2-core"            % "4.0.4" % "test"
)

scalacOptions ++= Seq(
  "-language:_",
  "-unchecked",
  "-explaintypes",
  "-Ywarn-value-discard",
  "-Ywarn-numeric-widen",
  "-Ypartial-unification",
  "-Xlog-free-terms",
  "-Xlog-free-types",
  "-Xlog-reflective-calls",
  "-Yrangepos",
  "-Yno-imports",
  "-Yno-predef",
  "-Ywarn-unused:explicits,patvars,imports,privates,locals,implicits",
  "-opt:l:method,inline",
  "-opt-inline-from:scalaz.**"
)

addCompilerPlugin(scalafixSemanticdb)
addCompilerPlugin("com.fommil" %% "deriving-plugin" % derivingVersion)

managedClasspath in Compile := {
  val res = (resourceDirectory in Compile).value
  val old = (managedClasspath in Compile).value
  Attributed.blank(res) +: old
}

addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.6")
addCompilerPlugin(
  ("org.scalamacros" % "paradise" % "2.1.1").cross(CrossVersion.full)
)

resolvers += Resolver.sonatypeRepo("snapshots")
addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.2.0-SNAPSHOT")

scalacOptions in (Compile, console) -= "-Yno-imports"
scalacOptions in (Compile, console) -= "-Yno-predef"
initialCommands in (Compile, console) := Seq("scalaz._, Scalaz._").mkString(
  "import ",
  ",",
  ""
)

scalacOptions in Test ++= Seq("-Yrangepos")
