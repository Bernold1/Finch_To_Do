val finchVersion = "0.32.1"
val circeVersion = "0.10.1"
val scalatestVersion = "3.0.5"
val finchgenericVersion = "0.31.0"

lazy val root = (project in file("."))
  .settings(
    organization := "Bernold",
    name := "todo",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.12.7",
    libraryDependencies ++= Seq(
      "com.github.finagle" %% "finchx-core"  % finchVersion,
      "com.github.finagle" %% "finchx-circe"  % finchVersion,
      "com.github.finagle" %% "finch-generic" % "0.31.0",
      "io.circe" %% "circe-generic" % circeVersion,
      "org.scalatest"      %% "scalatest"    % scalatestVersion % "test"
    )
  )