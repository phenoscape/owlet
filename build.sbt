
organization  := "org.phenoscape"

name          := "owlet"

version       := "1.9"

publishMavenStyle := true

publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
        Some("snapshots" at nexus + "content/repositories/snapshots")
    else
        Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

publishArtifact in Test := false

licenses := Seq("MIT" -> url("https://opensource.org/licenses/MIT"))

homepage := Some(url("https://github.com/phenoscape/owlet"))

scalaVersion  := "2.12.18"

crossScalaVersions := Seq("2.12.18", "2.13.11")

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

scalacOptions in Test ++= Seq("-Yrangepos")

lazy val jenaVersion = "4.9.0"

libraryDependencies ++= {
    Seq(
      "org.scalaz"                 %% "scalaz-core"              % "7.3.7",
      "net.sourceforge.owlapi"     %  "owlapi-distribution"      % "4.5.26",
      "org.apache.jena"            %  "jena-core"                % jenaVersion,
      "org.apache.jena"            %  "jena-arq"                 % jenaVersion,
      "com.typesafe.scala-logging" %% "scala-logging"            % "3.9.5",
      "org.scala-lang.modules"     %% "scala-xml"                % "2.2.0",
      "org.slf4j"                  %  "slf4j-log4j12"            % "2.0.9" % Test,
      "org.semanticweb.elk"        %  "elk-owlapi"               % "0.4.3"  % Test,
      "junit"                      %  "junit"                    % "4.13.2" % Test,
      "com.github.sbt"             %  "junit-interface"          % "0.13.3" % Test
    )
}

pomExtra := (
    <scm>
        <url>git@github.com:phenoscape/owlet.git</url>
        <connection>scm:git:git@github.com:phenoscape/owlet.git</connection>
    </scm>
    <developers>
        <developer>
            <id>balhoff</id>
            <name>Jim Balhoff</name>
            <email>jim@balhoff.org</email>
        </developer>
    </developers>
)