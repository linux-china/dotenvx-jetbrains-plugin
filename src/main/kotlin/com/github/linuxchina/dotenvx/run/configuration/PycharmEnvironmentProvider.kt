package com.github.linuxchina.dotenvx.run.configuration

import com.github.linuxchina.dotenvx.settings.DotenvxSettings
import com.github.linuxchina.dotenvx.settings.ui.RunConfigSettingsEditor
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.SdkAdditionalData
import com.jetbrains.python.run.AbstractPythonRunConfiguration
import com.jetbrains.python.run.PythonCommandLineEnvironmentProvider
import com.jetbrains.python.run.PythonRunParams
import java.util.function.Consumer

class PycharmEnvironmentProvider : PythonCommandLineEnvironmentProvider {   //PythonCommandLineTargetEnvironmentProvider

    override fun extendEnvironment(
        project: Project,
        sdkAdditionalData: SdkAdditionalData,
        generalCommandLine: GeneralCommandLine,
        pythonRunParams: PythonRunParams
    ) {
        if (pythonRunParams !is AbstractPythonRunConfiguration<*>) {
            return
        }
        val dotenvxSettings: DotenvxSettings? =
            pythonRunParams.getCopyableUserData<DotenvxSettings?>(RunConfigSettingsEditor.USER_DATA_KEY)
        val dotenvxVariables: MutableMap<String, String> =
            RunConfigSettingsEditor.collectEnv(dotenvxSettings, pythonRunParams.workingDirectory)
        val envs = mutableMapOf<String, String>().apply {
            putAll(pythonRunParams.envs)
            putAll(dotenvxVariables)
        }
        pythonRunParams.envs = envs
    }

    private fun addEnvironmentVariableToPythonExecution(generalCommandLine: GeneralCommandLine): Consumer<MutableMap.MutableEntry<String, String>> {
        return Consumer { entry: MutableMap.MutableEntry<String, String> ->
            generalCommandLine.environment[entry.key] = entry.value
        }
    }
}
