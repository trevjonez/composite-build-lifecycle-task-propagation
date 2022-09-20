rootProject.name = "included-project"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
println(startParameter)

pluginManagement {
  repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
    google()
  }
  includeBuild("../gradle/plugins")
}

dependencyResolutionManagement {
  repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
    google()
  }
  versionCatalogs {
    create("libs") {
      from(files("../gradle/libs.versions.toml"))
    }
  }
}
