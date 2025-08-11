package com.github.linuxchina.dotenvx.commands

import io.github.cdimascio.dotenv.DotenvxBuilder
import java.nio.file.Path

class DotenvxCmd(private val workDir: String, private val envFile: String?) {

    fun importEnv(): Map<String, String> {
        val dotenvxVariables: MutableMap<String, String> = mutableMapOf()
        if (Path.of(workDir, envFile ?: ".env").toFile().exists()) {
            val dotenv = DotenvxBuilder().directory(workDir).filename(envFile).load()
            dotenv.entries().forEach { entry ->
                dotenvxVariables[entry.key] = entry.value
            }
        }
        return dotenvxVariables
    }
}