package some.helpful

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.gradle.language.base.plugins.LifecycleBasePlugin

abstract class GradlePlugin : Plugin<Project> {

  override fun apply(target: Project) {
    target.run {
      val utsTask by lazy {
        tasks.register<DefaultTask>("unitTestSuite") {
          description = "Testing lifecycle task"
          group = LifecycleBasePlugin.VERIFICATION_GROUP
        }
      }
      pluginManager.withPlugin("com.android.application") {
        utsTask.configure { dependsOn(testSuiteTaskName("testDebugUnitTest")) }
      }
      pluginManager.withPlugin("com.android.library") {
        utsTask.configure { dependsOn(testSuiteTaskName("testDebugUnitTest")) }
      }
      pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
        utsTask.configure { dependsOn(testSuiteTaskName("test")) }
      }
      afterEvaluate {
        runCatching { tasks.named("unitTestSuite") }.onFailure {
          throw GradleException("No supported target plugin found", it)
        }
      }
    }
  }
}

private fun Project.testSuiteTaskName(default: String): String {
  return findProperty("testSuiteTask")?.toString() ?: default
}
