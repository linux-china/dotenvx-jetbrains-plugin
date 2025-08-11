package com.github.linuxchina.dotenvx.run.configuration

import com.github.linuxchina.dotenvx.settings.DotenvxSettings
import com.github.linuxchina.dotenvx.settings.ui.RunConfigSettingsEditor
import com.intellij.openapi.project.Project
import com.jetbrains.python.run.AbstractPythonRunConfiguration
import com.jetbrains.python.run.PythonExecution
import com.jetbrains.python.run.PythonRunParams
import com.jetbrains.python.run.target.HelpersAwareTargetEnvironmentRequest
import com.jetbrains.python.run.target.PythonCommandLineTargetEnvironmentProvider
import java.util.function.Consumer
import java.util.stream.Stream

class PycharmEnvironmentProvider : PythonCommandLineTargetEnvironmentProvider {
    override fun extendTargetEnvironment(
        project: Project,
        helpersAwareTargetEnvironmentRequest: HelpersAwareTargetEnvironmentRequest,
        pythonExecution: PythonExecution,
        pythonRunParams: PythonRunParams
    ) {
        if (pythonRunParams !is AbstractPythonRunConfiguration<*>) {
            return
        }
        val dotenvxSettings: DotenvxSettings? =
            pythonRunParams.getCopyableUserData<DotenvxSettings?>(RunConfigSettingsEditor.USER_DATA_KEY)
        val dotenvxVariables: MutableMap<String, String> =
            RunConfigSettingsEditor.collectEnv(dotenvxSettings, pythonRunParams.workingDirectory)
        val runConfigurationVariables = pythonRunParams.envs

        Stream.concat(dotenvxVariables.entries.stream(), runConfigurationVariables.entries.stream())
            .forEach(addEnvironmentVariableToPythonExecution(pythonExecution))
    }

    companion object {
        private fun addEnvironmentVariableToPythonExecution(pythonExecution: PythonExecution): Consumer<MutableMap.MutableEntry<String, String>> {
            return Consumer { entry: MutableMap.MutableEntry<String, String> ->
                pythonExecution.addEnvironmentVariable(
                    entry.key,
                    entry.value
                )
            }
        }
    }
}
