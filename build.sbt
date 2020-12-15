import com.typesafe.sbt.packager.docker._

lazy val akkaHttpVersion = "10.2.1"
lazy val akkaVersion    = "2.6.10"

lazy val appName = "skeleton-http"
lazy val appVersion = "0.0.1"
lazy val jarPrefix = "server-"
lazy val appBootClass = "io.syspulse.auth.App"
lazy val appDockerRoot = "/app"

parallelExecution in Test := true

initialize ~= { _ =>
  System.setProperty("config.file", "conf/application.conf")
}

fork := true
connectInput in run := true

enablePlugins(JavaAppPackaging)
enablePlugins(DockerPlugin)
enablePlugins(AshScriptPlugin)
//enablePlugins(JavaAppPackaging, AshScriptPlugin)
maintainer := "Dev0 <dev0@syspulse.io>"
dockerBaseImage := "openjdk:8-jre-alpine"
dockerUpdateLatest := true
dockerUsername := Some("syspulse")
dockerExposedVolumes := Seq(s"${appDockerRoot}/logs",s"${appDockerRoot}/conf","/data")
//dockerRepository := "docker.io"
dockerExposedPorts := Seq(8080)

defaultLinuxInstallLocation in Docker := appDockerRoot

mappings in Universal += file("conf/application.conf") -> "conf/application.conf"
mappings in Universal += file("conf/logback.xml") -> "conf/logback.xml"

bashScriptExtraDefines += s"""addJava "-Dconfig.file=${appDockerRoot}/conf/application.conf""""
bashScriptExtraDefines += s"""addJava "-Dlogback.configurationFile=${appDockerRoot}/conf/logback.xml""""

// bashScriptExtraDefines += """addJava "-Dconfig.file=${app_home}/../conf/application.conf""""
// bashScriptExtraDefines += """addJava "-Dlogback.configurationFile=${app_home}/../conf/logback.xml""""

//s"${(defaultLinuxInstallLocation in Docker).value}/bin/${executableScriptName.value}")
// dockerCommands ++= Seq(
//   ExecCmd("RUN",
//     "mv", 
//      s"${(defaultLinuxInstallLocation in Docker).value}/conf",
//      s"${(defaultLinuxInstallLocation in Docker).value}/${appName}/")
// )

daemonUserUid in Docker := None
daemonUser in Docker := "daemon"


lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization    := "io.syspulse",
      scalaVersion    := "2.13.3"
    )),
    name := appName,
    version := appVersion,

    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http"                    % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json"         % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-actor-typed"             % akkaVersion,
      "com.typesafe.akka" %% "akka-stream"                  % akkaVersion,
      "com.typesafe"      %  "config"                       % "1.4.1",
      "ch.qos.logback"    % "logback-classic"               % "1.2.3",

      "javax.ws.rs"       % "javax.ws.rs-api"               % "2.0.1",
      "com.github.swagger-akka-http" %% "swagger-akka-http" % "2.3.0",
      "nl.grons"          %% "metrics4-scala"               % "4.1.14",

      // "org.backuity.clist" %% "clist-core"               % "3.5.1",
      // "org.backuity.clist" %% "clist-macros"             % "3.5.1" % "provided",
      "com.github.scopt" %% "scopt"                         % "4.0.0",

      "io.jvm.uuid" %% "scala-uuid" % "0.3.1",

      "com.typesafe.akka" %% "akka-http-testkit"            % akkaHttpVersion % Test,
      "com.typesafe.akka" %% "akka-actor-testkit-typed"     % akkaVersion     % Test,
      "org.scalatest"     %% "scalatest"                    % "3.0.8"         % Test
    ),

    mainClass in assembly := Some(appBootClass),
    assemblyJarName in assembly := jarPrefix + appName + "-"+ appVersion + ".jar",
    assemblyMergeStrategy in assembly := {
      case x if x.contains("module-info.class") => MergeStrategy.discard
      case x => {
        val oldStrategy = (assemblyMergeStrategy in assembly).value
        oldStrategy(x)
      }
    }

  )
