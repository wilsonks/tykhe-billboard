lazy val billboard = project
  .in(file("."))
  .settings(moduleSettings)
  .settings(libraryDependencies += library("config"))
  .settings(libraryDependencies += library("better-files"))
  .settings(libraryDependencies += library("device-usb"))
  .settings(libraryDependencies += library("display-desktop"))
  .settings(libraryDependencies += library("display-ecs"))
  .settings(libraryDependencies += library("gdx-platform").classifier("natives-desktop"))
  .settings(libraryDependencies += library("gdx-freetype-platform").classifier("natives-desktop"))
  .settings(libraryDependencies += library("gdx-box2d-platform").classifier("natives-desktop"))
  .settings(
    mainClass in assembly := Some("roulette.BillboardApp"),
    assemblyJarName in assembly := "billboardRB900x1600test.jar")

lazy val library = Map(
  "config" ->  "com.typesafe" % "config" % "1.2.1",
  "better-files" -> "com.github.pathikrit" % "better-files_2.12" % "3.2.0",
  "device-usb" -> "io.device" %% "device-usb" % versions("device"),
  "display-desktop" -> "io.display" %% "display-desktop" % versions("display"),
  "display-ecs" -> "io.display" %% "display-ecs" % versions("display"),
  "gdx-platform" -> ("com.badlogicgames.gdx" % "gdx-platform" % versions("gdx")),
  "gdx-freetype-platform" -> ("com.badlogicgames.gdx" % "gdx-freetype-platform" % versions("gdx")),
  "gdx-box2d-platform" -> ("com.badlogicgames.gdx" % "gdx-box2d-platform" % versions("gdx")))

lazy val versions = Map(
  "device" -> "0.5.8",
  "display" -> "0.7.3",
  "gdx" -> "1.9.6")

lazy val aggregateSettings = sharedSettings ++ noPublish ++ noSources

lazy val moduleSettings = sharedSettings ++ publishSettings

lazy val sharedSettings = Seq(
  version := "0.0.1",
  organization := "io.device",
  licenses += ("Tykhe Software License Agreement" -> url("https://tykhegaming.github.io/LICENSE.txt")),
  credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
  updateOptions := updateOptions.value.withCachedResolution(true),
  scalaVersion := "2.12.2",
  crossScalaVersions := Seq("2.11.11", "2.12.2"),
  javacOptions ++= (CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, majorVersion)) if majorVersion <= 11 => Seq("-source", "1.6", "-target", "1.6")
    case _                                             => Seq("-source", "1.8", "-target", "1.8")
  }),
  scalacOptions ++= (CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, majorVersion)) if majorVersion <= 11 => Seq("-target:jvm-1.6")
    case _                                             => Seq.empty
  }),
  scalacOptions ++= (CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, majorVersion)) if majorVersion >= 11 => Seq(
      "-Xfatal-warnings", // turns all warnings into errors
      "-Xlint:adapted-args", // warn if an argument list is modified to match the receiver
      "-Xlint:nullary-unit", // warn when nullary methods return Unit
      "-Xlint:inaccessible", // warn about inaccessible types in method signatures
      "-Xlint:nullary-override", // warn when non-nullary `def f()' overrides nullary `def f'
      "-Xlint:infer-any", // warn when a type argument is inferred to be `Any`
      "-Xlint:missing-interpolator", // a string literal appears to be missing an interpolator id
      "-Xlint:doc-detached", // a ScalaDoc comment appears to be detached from its element
      "-Xlint:private-shadow", // a private field (or class parameter) shadows a superclass field
      "-Xlint:type-parameter-shadow", // a local type parameter shadows a type already in scope
      "-Xlint:poly-implicit-overload", // parameterized overloaded implicit methods are not visible as view bounds
      "-Xlint:option-implicit", // Option.apply used implicit view
      "-Xlint:delayedinit-select", // Selecting member of DelayedInit
      "-Xlint:by-name-right-associative", // By-name parameter of right associative operator
      "-Xlint:package-object-classes", // Class or object defined in package object
      "-Xlint:unsound-match" // Pattern match may not be typesafe
    )
    case _                                             => Seq.empty
  }),
  scalacOptions ++= Seq(
    "-unchecked", // enable additional warnings where generated code depends on assumptions
    "-deprecation", // emit warning for usages of deprecated APIs
    "-feature", // emit warning usages of features that should be imported explicitly
    "-language:higherKinds",
    "-language:implicitConversions",
    "-language:experimental.macros",
    "-Ywarn-inaccessible"),
  scalacOptions ++= (CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, 10))           => Seq()
    case Some((2, n)) if n >= 11 => Seq(
      "-Xexperimental", // for SAM support in 2.11
      "-Ywarn-unused-import")
  }),
  scalacOptions in(Compile, console) ~= {
    _.filterNot("-Ywarn-unused-import" == _)
  },
  scalacOptions in(Test, console) ~= {
    _.filterNot("-Ywarn-unused-import" == _)
  },
  autoAPIMappings := true,
  scalacOptions in(Compile, doc) ~= (_ filterNot (_ == "-Xfatal-warnings")), // filter fatal warnings to release
  scalacOptions in ThisBuild ++= Seq(
    "-sourcepath", file(".").getAbsolutePath.replaceAll("[.]$", "") // used by the doc-source-url feature to resolve source file path
  ))

lazy val publishSettings = Seq(
  publishMavenStyle := true,
  publishTo := Option("Tykhe Artifactory" at "http://repo-desktop:8081/artifactory/" + (
    if (isSnapshot.value) s"tykhe-snapshots;build.timestamp=${new java.util.Date().getTime}" else "tykhe-releases")))

lazy val noPublish = Seq(
  publish := {},
  publishLocal := {},
  publishArtifact := false,
  publishTo := None)

lazy val noSources = Seq(
  autoScalaLibrary := false,
  aggregate in update := false,
  sourcesInBase := false,
  sources in Compile := Seq.empty)
