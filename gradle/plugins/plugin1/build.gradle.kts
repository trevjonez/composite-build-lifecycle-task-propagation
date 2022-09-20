import org.gradle.api.tasks.testing.logging.*

plugins {
  `kotlin-dsl`
  `java-gradle-plugin`
}

gradlePlugin {
  plugins {
    create("helpful-plugin") {
      id = "some.helpful-plugin"
      implementationClass = "some.helpful.GradlePlugin"
    }
  }
}

dependencies {
  testImplementation(platform(libs.junit5.bom))
  testImplementation(libs.junit5.jupiter)
  testImplementation(libs.kotlin.test.junit5)
}

tasks.named<Test>("test") {
  useJUnitPlatform()
  testLogging {
    showStandardStreams = true
    events(*TestLogEvent.values())
  }
}

// Obviously we can't use our own output to wire this one up so copy and paste it is!
tasks.register<Task>("unitTestSuite") {
  description = "Testing lifecycle task"
  group = LifecycleBasePlugin.VERIFICATION_GROUP
  dependsOn("test")
}
