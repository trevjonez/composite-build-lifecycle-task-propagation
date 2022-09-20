plugins {
  alias(libs.plugins.android.lib)
  id("some.helpful-plugin")
}

android {
  namespace = "com.example.repro"
  compileSdk = 33
  defaultConfig {
    minSdk=28
    targetSdk = 33
  }
}
