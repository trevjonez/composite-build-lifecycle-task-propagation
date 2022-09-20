rootProject.name = "composite-build-lifecycle-task-propagation-plugins"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
println(startParameter)

pluginManagement {
  repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
  }
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
      from(files("../libs.versions.toml"))
    }
  }
}

include(":plugin1")
