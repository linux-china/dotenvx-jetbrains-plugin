package com.github.linuxchina.dotenvx.run.configuration


import com.github.linuxchina.dotenvx.settings.DotenvxSettings
import com.github.linuxchina.dotenvx.settings.ui.RunConfigSettingsEditor
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.RunnerSettings
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.util.NlsContexts
import com.jetbrains.python.run.AbstractPythonRunConfiguration
import com.jetbrains.python.run.PythonRunConfiguration
import com.jetbrains.python.run.PythonRunConfigurationExtension
import org.jdom.Element

class PycharmRunConfigurationExtension : PythonRunConfigurationExtension() {
    override fun readExternal(runConfiguration: AbstractPythonRunConfiguration<*>, element: Element) {
        RunConfigSettingsEditor.readExternal(runConfiguration, element)
    }

    override fun writeExternal(runConfiguration: AbstractPythonRunConfiguration<*>, element: Element) {
        RunConfigSettingsEditor.writeExternal(runConfiguration, element)
    }

    override fun <P : AbstractPythonRunConfiguration<*>?> createEditor(configuration: P): SettingsEditor<P?> {
        return RunConfigSettingsEditor<PythonRunConfiguration>() as SettingsEditor<P?>
    }

    override fun getEditorTitle(): @NlsContexts.TabTitle String {
        return RunConfigSettingsEditor.editorTitle
    }

    override fun isApplicableFor(configuration: AbstractPythonRunConfiguration<*>): Boolean {
        return true
    }

    override fun isEnabledFor(
        applicableConfiguration: AbstractPythonRunConfiguration<*>,
        runnerSettings: RunnerSettings?
    ): Boolean {
        return true
    }

    override fun patchCommandLine(
        configuration: AbstractPythonRunConfiguration<*>,
        runnerSettings: RunnerSettings?,
        cmdLine: GeneralCommandLine,
        runnerId: String
    ) {
        val dotenvxSettings: DotenvxSettings? =
            configuration.getCopyableUserData<DotenvxSettings?>(RunConfigSettingsEditor.USER_DATA_KEY)
        val dotenvVariables: MutableMap<String, String> =
            RunConfigSettingsEditor.collectEnv(dotenvxSettings, configuration.workingDirectory)
        val envs = mutableMapOf<String, String>().apply {
            putAll(configuration.envs)
            putAll(dotenvVariables)
        }
        configuration.envs = envs
        cmdLine.environment.putAll(dotenvVariables)

    }
}
