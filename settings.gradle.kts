rootProject.name = "composite-build-lifecycle-task-propagation"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
println(startParameter)

pluginManagement {
  repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
    google()
  }
  includeBuild("gradle/plugins")
}

dependencyResolutionManagement {
  repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
    google()
  }
}

include(":project1")
include(":project2")

includeBuild("included-project")
