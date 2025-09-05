package com.github.linuxchina.dotenvx.run.configuration

import com.github.linuxchina.dotenvx.commands.DotenvxCmd
import com.intellij.openapi.project.Project
import com.intellij.util.EnvironmentUtil
import org.jetbrains.plugins.terminal.LocalTerminalCustomizer
import java.nio.file.Files
import java.nio.file.Paths

class DotenvxTerminalCustomizer : LocalTerminalCustomizer() {
    override fun customizeCommandAndEnvironment(
        project: Project,
        workingDirectory: String?,
        command: Array<out String>,
        envs: MutableMap<String, String>
    ): Array<out String> {
        super.customizeCommandAndEnvironment(project, workingDirectory, command, envs)
        if (!workingDirectory.isNullOrEmpty()) {
            var fileName = ".env"
            val environmentMap = EnvironmentUtil.getEnvironmentMap()
            arrayOf("NODE_ENV", "APP_ENV", "RUN_ENV","SPRING_PROFILES_ACTIVE","STELA_ENV").forEach {
                if (environmentMap.containsKey(it)) {
                    fileName = environmentMap[it]!!
                }
            }
            if (Files.exists(Paths.get(workingDirectory, fileName))) {
                DotenvxCmd(workingDirectory, fileName).importEnv().forEach {
                    envs[it.key] = it.value
                }
            }
        }
        return command
    }

}