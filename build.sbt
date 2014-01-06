
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"



libraryDependencies += "com.typesafe.play" %% "play-json" % "2.2.1"

libraryDependencies += "net.databinder.dispatch" %% "dispatch-core" % "0.11.0"

libraryDependencies += "org.scalaz" %% "scalaz-core" % "7.0.5"



libraryDependencies += "org.scalatest" %% "scalatest" % "2.0.M5b" % "test"

libraryDependencies += "junit" % "junit" % "4.11" % "test"

libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.5" % "test"

libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.7.5" % "test"



autoAPIMappings := true

apiMappings ++= {
  val cp: Seq[Attributed[File]] = (fullClasspath in Compile).value
  def findManagedDependency(organization: String, name: String): File = {
    ( for {
        entry <- cp
        module <- entry.get(moduleID.key)
        if module.organization == organization
        if module.name.startsWith(name)
        jarFile = entry.data
      } yield jarFile
    ).head
  }
  Map(
      findManagedDependency("org.scalaz",        "scalaz-core") -> url("https://scalazproject.ci.cloudbees.com/job/nightly_2.10/ws/target/scala-2.10/unidoc/")
    , findManagedDependency("com.typesafe.play", "play-json")   -> url("http://www.playframework.com/documentation/2.2.1/api/scala/")
  )
}
