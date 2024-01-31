import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML

fun properties(key: String) = project.findProperty(key).toString()

plugins {
  id("java")
  // Kotlin support
  id("org.jetbrains.kotlin.jvm") version "1.8.0"
  // Gradle IntelliJ Plugin
  // https://github.com/jetbrains/gradle-intellij-plugin/releases/latest
  id("org.jetbrains.intellij") version "1.5.2"
  // Gradle Changelog Plugin
  id("org.jetbrains.changelog") version "2.0.0"
  // dependenciesUpdates
  id("com.github.ben-manes.versions") version "0.39.0"
  // shadow build jar
  id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = properties("pluginGroup")
version = properties("pluginVersion")

repositories {
  //TODO change them according every develop requirements
  maven { url = uri("https://www.jetbrains.com/intellij-repository/releases") }
  maven { url = uri("https://cache-redirector.jetbrains.com/intellij-dependencies") }
  maven { url = uri("https://maven.aliyun.com/repository/public") }
  mavenCentral()
}

dependencies {
  implementation ("org.jdom:jdom2:2.0.6.1")
  implementation ("com.squareup.okhttp3:okhttp:4.9.1")
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
  pluginName.set(properties("pluginName"))
  version.set(properties("platformVersion"))
  type.set(properties("platformType")) // Target IDE Platform

  plugins.set(properties("platformPlugins").split(',').map(String::trim).filter(String::isNotEmpty))
}

// Configure Gradle Changelog Plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
changelog {
  version.set(properties("pluginVersion"))
  groups.set(emptyList())
}


tasks {
  // Set the JVM compatibility versions
  properties("javaVersion").let {
    withType<JavaCompile> {
      sourceCompatibility = it
      targetCompatibility = it
    }
  }

  buildSearchableOptions {
    enabled = false
  }

  patchPluginXml {
    version.set(properties("pluginVersion"))
    sinceBuild.set(properties("pluginSinceBuild"))
    untilBuild.set(properties("pluginUntilBuild"))

    // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
    pluginDescription.set(
            file("README.md").readText().lines().run {
              val start = "<!-- Plugin description -->"
              val end = "<!-- Plugin description end -->"

              if (!containsAll(listOf(start, end))) {
                throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
              }
              subList(indexOf(start) + 1, indexOf(end))
            }.joinToString("\n").let { markdownToHTML(it) }
    )

    // Get the latest available change notes from the changelog file
    changeNotes.set(provider {
      with(changelog) {
        renderItem(
                getOrNull(properties("pluginVersion")) ?: getLatest(),
                Changelog.OutputType.HTML,
        )
      }
    })
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
    dependsOn("patchChangelog")
    token.set(System.getenv("PUBLISH_TOKEN"))
    channels.set(listOf(properties("pluginVersion").split('-').getOrElse(1) { "default" }.split('.').first()))
  }

  jar {
    manifest {
      attributes["Main-Class"] = "org.intellij.tool.branch.merge.local.CommonMergeAction"
    }
    // 包含所有依赖项
    from(configurations.runtimeClasspath)
  }

  shadowJar {
    manifest {
      attributes["Main-Class"] = "org.intellij.tool.branch.merge.local.CommonMergeAction"
    }
    archiveClassifier.set("")
  }
}
