package com.github.linuxchina.dotenvx.commands

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
            } catch (_: Exception) {
                // Handle exception if needed, e.g., log it
            }
        } else if (Path.of(workDir, envFileName ?: ".env").toFile().exists()) {
            try {
                val dotenv = DotenvxBuilder().directory(workDir).filename(envFileName).load()
                dotenv.entries().forEach { entry ->
                    dotenvxVariables[entry.key] = entry.value
                }
            } catch (_: Exception) {
                // Handle exception if needed, e.g., log it
            }
        }
        return dotenvxVariables
    }
}