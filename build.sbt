
organization  := "org.phenoscape"

name          := "owlet"

version       := "1.4"

scalaVersion  := "2.11.8"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

scalacOptions in Test ++= Seq("-Yrangepos")

libraryDependencies ++= {
    Seq(
      "org.scalaz"                 %% "scalaz-core"         % "7.1.1",
       "net.sourceforge.owlapi"    %  "owlapi-distribution" % "4.2.1",
       "org.apache.jena"           %  "apache-jena-libs"    % "2.10.1",
      "com.typesafe.scala-logging" %% "scala-logging"       % "3.4.0",
      "org.slf4j"                  %  "slf4j-log4j12"       % "1.7.21",
      "org.semanticweb.elk"        %  "elk-owlapi"          % "0.4.3" % Test,
      "junit"                       %  "junit"              % "4.10"  % Test
    )
}
