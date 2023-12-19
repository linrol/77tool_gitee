package org.intellij.tool.branch.version

import com.intellij.openapi.project.Project
import org.intellij.tool.utils.GitLabUtil
import org.intellij.tool.utils.XPathHelper
import org.jdom2.Document
import org.jdom2.input.SAXBuilder
import org.jdom2.output.Format
import org.jdom2.output.XMLOutputter
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.FileInputStream
import java.io.FileWriter
import java.io.InputStream
import java.nio.file.Paths
import java.util.*


class ChangeVersion(val project: Project) {

    private val versions = getConfigVersion()

    private val ignore = listOf("autotest-frame-starter", "dbtools", "grpc-clients")

    private fun getConfigVersion(): Map<Any, Any> {
        val yaml = Yaml()
        val configFile: File = Paths.get("${project.basePath}/apps/build", "config.yaml").toFile()
        return try {
            val input: InputStream = FileInputStream(configFile)
            (yaml.load(input) as Map<String, Any>).values.flatMap { (it as Map<*, *>).toList() }.associateBy ( { it.first!! }, {it.second!!} ).toMutableMap().apply {
                this["base-common"]?.let {
                    this["base-common-test"] = it
                    this["base-common-test-api"] = it
                }
                this["framework"]?.let {
                    this["testapp"] = it
                    this["testapp-api"] = it
                }
            }
        } catch (e: Exception) {
            Collections.emptyMap()
        }
    }

    fun run(branch: String) {
        val todos = GitLabUtil.getCommonRepositories(project, branch).associateBy ( {it.root.name}, {it.root.path})
        todos.forEach { (name, path) ->
            val poms = searchPoms(path, 0)
            poms.forEach {
                changeVersion(name, it)
            }
        }
    }

    private fun searchPoms(path: String, level: Int): List<File> {
        val files = File(path).listFiles()
        if (files == null || level > 3) {
            return emptyList()
        }
        return files.filter { it.isDirectory || it.name.contains("pom.xml") }.flatMap {
            if (it.isDirectory) {
                searchPoms(it.path, level + 1)
            } else{
                listOf(it)
            }
        }.toList()
    }

    private fun changeVersion(module: String, file: File) {
        // 1. 使用 SAXBuilder 创建 Document 对象
        val saxBuilder = SAXBuilder()
        val document = saxBuilder.build(file)

        val name = module.takeIf { !file.path.contains("platform") } ?: "platform"
        updateParent(document)
        updateSelf(document, name)
        updateProperties(document)
        updateDependencies(document)
        updatePlugins(document)

        writeFile(document, file)
    }

    private fun updateParent(document: Document) {
        val group = XPathHelper.getElement(document, "/ns:project/ns:parent/ns:groupId") ?: return
        val version = XPathHelper.getElement(document, "/ns:project/ns:parent/ns:version") ?: return
        takeIf { group.text.contains("com.q7link") } ?. let {
           version.run {
               versions["framework"]?.let {
                   this.setText(it.toString())
               }
           }
        }
    }

    private fun updateSelf(document: Document, module: String) {
        val version = XPathHelper.getElement(document, "/ns:project/ns:version") ?: return
        version.run {
            versions[module]?.let {
                this.setText(it.toString())
            }
        }
    }

    private fun updateProperties(document: Document) {
        XPathHelper.getElements(document, "/ns:project/ns:properties").forEach {
            val elementName = it.text.replace("Version","")
            versions[elementName]?.let { version ->
                it.setText(version.toString())
            }
        }
    }

    private fun updateDependencies(document: Document) {
        XPathHelper.getElements(document, "/ns:project/ns:dependencies/ns:dependency").forEach {
            val groupNode = it.getChild("groupId", it.namespace) ?:return
            val artifactNode = it.getChild("artifactId", it.namespace) ?:return
            val versionNode = it.getChild("version", it.namespace) ?:return
            val scopeNode = it.getChild("scope", it.namespace)
            if (scopeNode != null && scopeNode.text == "test") {
                return@forEach
            }
            if (groupNode.text != "com.q7link.application") {
                return@forEach
            }
            val artifactId = artifactNode.text.replace("-private", "")
            if (ignore.contains(artifactId)) {
                return@forEach
            }
            if (versionNode.text.startsWith("\${")) {
                return@forEach
            }
            versions[artifactId]?.let { version ->
                versionNode.setText(version.toString())
            }
        }
    }

    private fun updatePlugins(document: Document) {
        XPathHelper.getElements(document, "/ns:project/ns:build/ns:plugins/ns:plugin").forEach {
            val groupNode = it.getChild("groupId", it.namespace) ?:return
            val versionNode = it.getChild("version", it.namespace) ?:return
            val artifactNode = it.getChild("artifactId", it.namespace) ?:return
            if (groupNode.text != "com.q7link.framework") {
                return@forEach
            }
            if (versionNode.text.startsWith("\${")) {
                return@forEach
            }
            versions[artifactNode.text]?.let { version ->
                versionNode.setText(version.toString())
            }
        }
    }

    private fun writeFile(document: Document, file: File) {
        XMLOutputter(Format.getRawFormat()).output(document, FileWriter(file.path))
    }
}