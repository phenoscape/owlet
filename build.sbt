
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

scalaVersion  := "2.12.14"

crossScalaVersions := Seq("2.12.14", "2.13.6")

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

scalacOptions in Test ++= Seq("-Yrangepos")

libraryDependencies ++= {
    Seq(
      "org.scalaz"                 %% "scalaz-core"              % "7.3.5",
      "net.sourceforge.owlapi"     %  "owlapi-distribution"      % "4.5.20",
      "org.apache.jena"            %  "apache-jena-libs"         % "4.8.0",
      "com.typesafe.scala-logging" %% "scala-logging"            % "3.9.4",
      "org.slf4j"                  %  "slf4j-log4j12"            % "1.7.32",
      "org.scala-lang.modules"     %% "scala-xml"                % "2.0.1",
      "org.semanticweb.elk"        %  "elk-owlapi"               % "0.4.3"  % Test,
      "junit"                      %  "junit"                    % "4.13.2" % Test,
      "com.github.sbt"             %  "junit-interface"          % "0.13.2" % Test
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