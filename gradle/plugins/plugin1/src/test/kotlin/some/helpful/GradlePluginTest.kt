package some.helpful

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class GradlePluginTest {

  @field:TempDir
  lateinit var testProjectDir: File

  lateinit var settingsFile: File
  lateinit var buildFile: File

  @BeforeEach
  fun setup() {
    settingsFile = File(testProjectDir, "settings.gradle.kts")

    //language=kts
    settingsFile.writeText(
        """rootProject.name = "Helpful-Plugin-Integration-Test"
          |pluginManagement {
          |    repositories {
          |        mavenLocal()
          |        mavenCentral()
          |        google()
          |        gradlePluginPortal()
          |    }
          |}
          |
          |dependencyResolutionManagement {
          |  repositories {
          |    mavenLocal()
          |    mavenCentral()
          |    google()
          |  }
          |}
          |
        """.trimMargin()
    )
    buildFile = File(testProjectDir, "build.gradle.kts")
  }

  @Test
  fun `clean apply to kotlin jvm project`() {
    //language=kts
    buildFile.writeText(
        """plugins {
          |  kotlin("jvm") version "1.7.10"
          |  id("some.helpful-plugin")
          |}
          |
        """.trimMargin()
    )

    val result = GradleRunner.create()
        .withProjectDir(testProjectDir)
        .withPluginClasspath()
        .forwardOutput()
        .withArguments("tasks", "--all")
        .build()

    assertTrue { result.task(":tasks")?.outcome == TaskOutcome.SUCCESS }
    assertTrue { result.output.contains("unitTestSuite") }
  }

  @Test
  fun `fail on missing plugin project`() {
    //language=kts
    buildFile.writeText(
        """plugins {
          |  id("some.helpful-plugin")
          |}
          |
        """.trimMargin()
    )

    val result = GradleRunner.create()
        .withProjectDir(testProjectDir)
        .withPluginClasspath()
        .forwardOutput()
        .withArguments("tasks", "--all")
        .buildAndFail()

    assertTrue { result.output.contains("No supported target plugin found") }
  }
}
