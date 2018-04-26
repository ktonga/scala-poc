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

libraryDependencies ++= Dependencies.compile ++ Dependencies.tooling ++ Dependencies.test

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
  "-Xmacro-settings:materialize-derivations",
  "-Yrangepos",
  "-Yno-imports",
  "-Yno-predef",
  "-Ywarn-unused:explicits,patvars,imports,privates,locals,implicits",
  "-opt:l:method,inline",
  "-opt-inline-from:scalaz.**"
)

addCompilerPlugin(scalafixSemanticdb)
addCompilerPlugin("com.fommil" %% "deriving-plugin" % Dependencies.vDeriving)

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
