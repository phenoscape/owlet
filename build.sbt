
organization  := "org.phenoscape"

name          := "owlet"

version       := "1.5-SNAPSHOT"

scalaVersion  := "2.11.8"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

scalacOptions in Test ++= Seq("-Yrangepos")

libraryDependencies ++= {
    Seq(
      "org.scalaz"                 %% "scalaz-core"              % "7.2.9",
      "net.sourceforge.owlapi"     %  "owlapi-distribution"      % "4.2.8",
      "org.apache.jena"            %  "apache-jena-libs"         % "3.2.0",
      "com.typesafe.scala-logging" %% "scala-logging"            % "3.4.0",
      "org.slf4j"                  %  "slf4j-log4j12"            % "1.7.21",
      "org.scala-lang.modules"     %% "scala-parser-combinators" % "1.0.5",
      "org.scala-lang.modules"     %% "scala-xml"                % "1.0.6",
      "org.semanticweb.elk"        %  "elk-owlapi"               % "0.4.3" % Test,
      "junit"                      %  "junit"                    % "4.10"  % Test
    )
}
