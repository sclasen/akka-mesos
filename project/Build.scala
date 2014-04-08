import sbt._
import Keys._

import com.typesafe.sbt.SbtScalariform._
import scalariform.formatter.preferences._
import sbtunidoc.Plugin._

object AkkaMesosBuild extends Build {

//////////////////////////////////////////////////////////////////////////////
// PROJECT INFO
//////////////////////////////////////////////////////////////////////////////

  val ORGANIZATION    = "akka.mesos"
  val PROJECT_NAME    = "akka-mesos"
  val PROJECT_VERSION = "0.1.0"
  val SCALA_VERSION   = "2.10.4"


//////////////////////////////////////////////////////////////////////////////
// DEPENDENCY VERSIONS
//////////////////////////////////////////////////////////////////////////////

  val MESOS_VERSION           = "0.16.0"
  val AKKA_VERSION            = "2.2.3"
  val TYPESAFE_CONFIG_VERSION = "1.0.2"
  val SCALATEST_VERSION       = "2.0.M5b"
  val SLF4J_VERSION           = "1.7.2"
  val LOGBACK_VERSION         = "1.0.9"


//////////////////////////////////////////////////////////////////////////////
// NATIVE LIBRARY PATHS
//////////////////////////////////////////////////////////////////////////////

  val pathToMesosLibs = "/usr/local/lib"


//////////////////////////////////////////////////////////////////////////////
// PROJECTS
//////////////////////////////////////////////////////////////////////////////

  lazy val root = Project(
    id = PROJECT_NAME,
    base = file("."),
    settings = commonSettings
  ) dependsOn (
    core, cluster
  ) aggregate (
    core, cluster
  )

  def subproject(suffix: String) = s"${PROJECT_NAME}-$suffix"

  lazy val core = Project(
    id = subproject("core"),
    base = file("core"),
    settings = commonSettings
  )

  lazy val cluster = Project(
    id = subproject("cluster"),
    base = file("cluster"),
    settings = commonSettings ++ Seq (
      libraryDependencies ++= Seq(
        "com.typesafe.akka" %% "akka-cluster" % AKKA_VERSION % "provided"
      )
    )
  ) dependsOn(core)



//////////////////////////////////////////////////////////////////////////////
// SHARED SETTINGS
//////////////////////////////////////////////////////////////////////////////

  lazy val commonSettings = Project.defaultSettings ++
                            basicSettings ++
                            formatSettings ++
                            net.virtualvoid.sbt.graph.Plugin.graphSettings

  lazy val basicSettings = Seq(
    version := PROJECT_VERSION,
    organization := ORGANIZATION,
    scalaVersion := SCALA_VERSION,

    resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",

    libraryDependencies ++= Seq(
      "com.typesafe"       % "config"          % TYPESAFE_CONFIG_VERSION,
      "org.slf4j"          % "slf4j-api"       % SLF4J_VERSION,
      "org.apache.mesos"   % "mesos"           % MESOS_VERSION,
      "com.typesafe.akka" %% "akka-actor"      % AKKA_VERSION      % "provided",
      "com.typesafe.akka" %% "akka-testkit"    % AKKA_VERSION      % "test",
      "ch.qos.logback"     % "logback-classic" % LOGBACK_VERSION   % "runtime",
      "org.scalatest"     %% "scalatest"       % SCALATEST_VERSION % "test"
    ),

    scalacOptions in Compile ++= Seq(
      "-unchecked",
      "-deprecation",
      "-feature"
    ),

    javaOptions += "-Djava.library.path=%s:%s".format(
      sys.props("java.library.path"),
      pathToMesosLibs
    ),

    fork in Test := true
  )

  lazy val formatSettings = scalariformSettings ++ Seq(
    ScalariformKeys.preferences := FormattingPreferences()
      .setPreference(IndentWithTabs, false)
      .setPreference(IndentSpaces, 2)
      .setPreference(AlignParameters, false)
      .setPreference(DoubleIndentClassDeclaration, true)
      .setPreference(MultilineScaladocCommentsStartOnFirstLine, false)
      .setPreference(PlaceScaladocAsterisksBeneathSecondAsterisk, true)
      .setPreference(PreserveDanglingCloseParenthesis, true)
      .setPreference(CompactControlReadability, true)
      .setPreference(AlignSingleLineCaseStatements, true)
      .setPreference(PreserveSpaceBeforeArguments, true)
      .setPreference(SpaceBeforeColon, false)
      .setPreference(SpaceInsideBrackets, false)
      .setPreference(SpaceInsideParentheses, false)
      .setPreference(SpacesWithinPatternBinders, true)
      .setPreference(FormatXml, true)
  )

}