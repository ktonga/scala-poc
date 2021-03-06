import sbt._

object Dependencies {

  val vDeriving = "0.13.0"

  lazy val compile = Seq(
    dep("org.scalaz", "7.2.21", "scalaz-core"),
    dep("org.scalaz", "2.2.0", "scalaz-ioeffect", "scalaz-ioeffect-cats"),
    dep("com.codecommit", "1.2.1", "shims"),
    dep("co.fs2", "0.10.3", "fs2-core"),
    dep("eu.timepit", "0.8.7", "refined-scalaz"),
    dep("com.github.pureconfig", "0.9.1", "pureconfig", "pureconfig-http4s"),
    dep("org.http4s",
        "0.18.9",
        "http4s-dsl",
        "http4s-blaze-server",
        "http4s-blaze-client",
        "http4s-argonaut"),
    dep("io.argonaut", "6.2", "argonaut-scalaz"),
    dep("org.tpolecat", "0.5.2", "doobie-core", "doobie-postgres"),
    Seq("org.apache.kafka" % "kafka-clients"   % "1.1.0"),
    Seq("org.flywaydb"     % "flyway-core"     % "5.0.7"),
    Seq("ch.qos.logback"   % "logback-classic" % "1.2.3")
  ).flatten

  lazy val tooling = Seq(
    dep("com.github.mpilquist", "0.12.0", "simulacrum"),
    dep("com.fommil", vDeriving, "scalaz-deriving", "deriving-macro")
  ).flatten

  lazy val test = Seq(
    dep("org.specs2", "4.0.4", "specs2-core", "specs2-scalaz", "specs2-scalacheck")
  ).flatten.map(_ % "it,test")

  def dep(g: String, v: String, as: String*) = as.map(g %% _ % v).toSeq
}
