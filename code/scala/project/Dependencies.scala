import sbt._

object Dependencies {
  lazy val zio = "dev.zio" %% "zio" % "1.0.0-RC20"
  lazy val zioStreams = "dev.zio" %% "zio-streams" % "1.0.0-RC20"
  lazy val zioNio = "dev.zio" %% "zio-nio" % "1.0.0-RC7"
  lazy val cats = "org.typelevel" %% "cats-core" % "2.2.0-M2"
  lazy val spire = "org.typelevel" %% "spire" % "0.17.0-M1"
  lazy val csv = "com.github.tototoshi" %% "scala-csv" % "1.3.6"
  lazy val breeze = "org.scalanlp" %% "breeze" % "1.0"
  lazy val breezeNatives = "org.scalanlp" %% "breeze-natives" % "1.0"
  lazy val shapeless = "com.chuusai" %% "shapeless" % "2.3.3"

  lazy val zioTest = "dev.zio" %% "zio-test" % "1.0.0-RC20" % Test
  lazy val zioTestSbt = "dev.zio" %% "zio-test-sbt" % "1.0.0-RC20" % Test
  lazy val zioTestMagnolia = "dev.zio" %% "zio-test-magnolia" % "1.0.0-RC20" % Test
}
