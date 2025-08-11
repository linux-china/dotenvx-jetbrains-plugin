package com.github.linuxchina.dotenvx.run.configuration

import com.github.linuxchina.dotenvx.settings.ui.RunConfigSettingsEditor
import com.goide.execution.GoRunConfigurationBase
import com.goide.execution.GoRunningState
import com.goide.execution.extension.GoRunConfigurationExtension
import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.RunnerSettings
import com.intellij.execution.target.TargetedCommandLineBuilder
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.util.NlsContexts.TabTitle
import org.jdom.Element

class GolandRunConfigurationExtension : GoRunConfigurationExtension() {
    protected override fun readExternal(runConfiguration: GoRunConfigurationBase<*>, element: Element) {
        RunConfigSettingsEditor.readExternal(runConfiguration, element)
    }

    protected override fun writeExternal(runConfiguration: GoRunConfigurationBase<*>, element: Element) {
        RunConfigSettingsEditor.writeExternal(runConfiguration, element)
    }

    protected override fun <P : GoRunConfigurationBase<*>?> createEditor(configuration: P): SettingsEditor<P?> {
        return RunConfigSettingsEditor()
    }

    override fun getEditorTitle(): @TabTitle String {
        return RunConfigSettingsEditor.editorTitle
    }

    override fun isApplicableFor(configuration: GoRunConfigurationBase<*>): Boolean {
        return true
    }

    override fun isEnabledFor(
        applicableConfiguration: GoRunConfigurationBase<*>,
        runnerSettings: RunnerSettings?
    ): Boolean {
        return true
    }

    @Throws(ExecutionException::class)
    protected override fun patchCommandLine(
        configuration: GoRunConfigurationBase<*>,
        runnerSettings: RunnerSettings?,
        cmdLine: TargetedCommandLineBuilder,
        runnerId: String,
        state: GoRunningState<out GoRunConfigurationBase<*>?>,
        commandLineType: GoRunningState.CommandLineType
    ) {
        val dotenvxSettings = configuration.getCopyableUserData(RunConfigSettingsEditor.USER_DATA_KEY)
        val newEnv: MutableMap<String, String> =
            RunConfigSettingsEditor.collectEnv(dotenvxSettings, configuration.workingDirectory)
        for (set in newEnv.entries) {
            cmdLine.addEnvironmentVariable(set.key, set.value)
        }
    }

}
