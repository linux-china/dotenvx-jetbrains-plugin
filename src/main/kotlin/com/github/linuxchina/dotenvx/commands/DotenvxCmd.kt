package com.github.linuxchina.dotenvx.commands

import io.github.cdimascio.dotenv.Dotenv.Filter
import io.github.cdimascio.dotenv.DotenvxBuilder
import java.io.File
import java.nio.file.Path

class DotenvxCmd(private val workDir: String, private val envFileName: String?) {

    fun importEnv(): Map<String, String> {
        val dotenvxVariables: MutableMap<String, String> = mutableMapOf()
        val envFile = File(envFileName ?: ".env")
        if (envFile.isAbsolute) {
            try {
                val dotenv = DotenvxBuilder().directory(envFile.parentFile.canonicalPath).filename(envFile.name).load()
                dotenv.entries().forEach { entry ->
                    dotenvxVariables[entry.key] = entry.value
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            val newWorkDir = if (workDir.startsWith("file://")) {
                Path.of(workDir.removePrefix("file://")).toAbsolutePath().toString()
            } else {
                Path.of(workDir).toAbsolutePath().toString()
            }
            if (Path.of(newWorkDir, envFileName ?: ".env").toFile().exists()) {
                try {
                    val dotenv = DotenvxBuilder().directory(workDir).filename(envFileName).load()
                    dotenv.entries(Filter.DECLARED_IN_ENV_FILE).forEach { entry ->
                        dotenvxVariables[entry.key] = entry.value
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        return dotenvxVariables
    }
}