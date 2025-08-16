package com.github.linuxchina.dotenvx.run.configuration

import com.github.linuxchina.dotenvx.settings.DotenvxSettings
import com.github.linuxchina.dotenvx.settings.ui.RunConfigSettingsEditor
import com.intellij.openapi.project.Project
import com.jetbrains.python.run.AbstractPythonRunConfiguration
import com.jetbrains.python.run.PythonExecution
import com.jetbrains.python.run.PythonRunParams
import com.jetbrains.python.run.target.HelpersAwareTargetEnvironmentRequest
import com.jetbrains.python.run.target.PythonCommandLineTargetEnvironmentProvider

@Suppress("UnstableApiUsage")
class PycharmEnvironmentProvider :
    PythonCommandLineTargetEnvironmentProvider {   //PythonCommandLineTargetEnvironmentProvider

    override fun extendTargetEnvironment(
        project: Project,
        helpersAwareTargetRequest: HelpersAwareTargetEnvironmentRequest,
        pythonExecution: PythonExecution,
        runParams: PythonRunParams
    ) {
        if (runParams !is AbstractPythonRunConfiguration<*>) {
            return
        }
        val dotenvxSettings: DotenvxSettings? =
            runParams.getCopyableUserData<DotenvxSettings?>(RunConfigSettingsEditor.USER_DATA_KEY)
        val dotenvxVariables: MutableMap<String, String> =
            RunConfigSettingsEditor.collectEnv(dotenvxSettings, runParams.workingDirectory)
        for (entry in dotenvxVariables.entries) {
            pythonExecution.addEnvironmentVariable(entry.key, entry.value)
        }
    }
}
