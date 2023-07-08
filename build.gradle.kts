plugins {
  id("java")
  id("org.jetbrains.kotlin.jvm") version "1.7.20"
  id("org.jetbrains.intellij") version "1.5.2"
}

group = "com.linrol.cn"
version = "1.0.5"

repositories {
  //TODO change them according every develop requirements
  maven { url = uri("https://www.jetbrains.com/intellij-repository/releases") }
  maven { url = uri("https://cache-redirector.jetbrains.com/intellij-dependencies") }
  maven {
    url = uri("https://maven.aliyun.com/repository/public")
  }
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
  version.set("2021.2")
  type.set("IC") // Target IDE Platform

  plugins.set(listOf("Git4Idea"))
  // plugins.set(listOf("org.jetbrains.intellij:v1.15.0"))
}

tasks {
  // Set the JVM compatibility versions
  withType<JavaCompile> {
    sourceCompatibility = "8"
    targetCompatibility = "8"
  }

  buildSearchableOptions {
    enabled = false
  }

  patchPluginXml {
    sinceBuild.set("182.3")
    untilBuild.set("231.*")
  }

  runPluginVerifier {
    ideVersions.set(listOf("IC-2018.3.6"))
  }

  signPlugin {
    certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
    privateKey.set(System.getenv("PRIVATE_KEY"))
    password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
  }

  publishPlugin {
    token.set(System.getenv("PUBLISH_TOKEN"))
  }
}
