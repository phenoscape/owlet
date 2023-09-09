
organization  := "org.phenoscape"

name          := "owlet"

version       := "2.0.0"

publishMavenStyle := true

publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
        Some("snapshots" at nexus + "content/repositories/snapshots")
    else
        Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

Test / publishArtifact := false

licenses := Seq("MIT" -> url("https://opensource.org/licenses/MIT"))

homepage := Some(url("https://github.com/phenoscape/owlet"))

scalaVersion  := "2.13.11"

//crossScalaVersions := Seq("2.13.11", "3")

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

Test / scalacOptions ++= Seq("-Yrangepos")

testFrameworks += new TestFramework("utest.runner.Framework")

Test / parallelExecution := false

lazy val jenaVersion = "4.9.0"

libraryDependencies ++= {
    Seq(
      "net.sourceforge.owlapi"     %  "owlapi-distribution"      % "4.5.26",
      "org.apache.jena"            %  "jena-core"                % jenaVersion,
      "org.apache.jena"            %  "jena-arq"                 % jenaVersion,
      "com.typesafe.scala-logging" %% "scala-logging"            % "3.9.5",
      "org.slf4j"                  %  "slf4j-log4j12"            % "2.0.9" % Test,
      "org.semanticweb.elk"        %  "elk-owlapi"               % "0.4.3"  % Test,
      "com.lihaoyi"                %% "utest"                    % "0.8.1"  % Test
    )
}

pomExtra :=
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
