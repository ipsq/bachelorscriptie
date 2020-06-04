import Dependencies._

ThisBuild / scalaVersion     := "2.13.2"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.riemers"

lazy val root = (project in file("."))
  .settings(
    name := "Bachelor Scriptie",
    libraryDependencies ++= Seq(
      zio, zioStreams, zioNio, zioTest, zioTestSbt, zioTestMagnolia,
      cats, spire, breeze, breezeNatives, shapeless
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )
