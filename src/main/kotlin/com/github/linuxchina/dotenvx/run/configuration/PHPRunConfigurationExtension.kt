package com.github.linuxchina.dotenvx.run.configuration


import com.github.linuxchina.dotenvx.settings.DotenvxSettings
import com.github.linuxchina.dotenvx.settings.ui.RunConfigSettingsEditor
import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.RunnerSettings
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.util.NlsContexts
import com.jetbrains.php.config.interpreters.PhpInterpreter
import com.jetbrains.php.run.PhpRunConfiguration
import com.jetbrains.php.run.PhpRunConfigurationExtension
import org.jdom.Element

class PHPRunConfigurationExtension : PhpRunConfigurationExtension() {
    override fun readExternal(runConfiguration: PhpRunConfiguration<*>, element: Element) {
        RunConfigSettingsEditor.readExternal(runConfiguration, element)
    }

    override fun writeExternal(runConfiguration: PhpRunConfiguration<*>, element: Element) {
        RunConfigSettingsEditor.writeExternal(runConfiguration, element)
    }

    override fun <P : PhpRunConfiguration<*>?> createEditor(configuration: P): SettingsEditor<P?> {
        return RunConfigSettingsEditor()
    }

    override fun getEditorTitle(): @NlsContexts.TabTitle String {
        return RunConfigSettingsEditor.editorTitle
    }

    override fun isApplicable(phpInterpreter: PhpInterpreter?): Boolean {
        return true
    }

    override fun isApplicableFor(configuration: PhpRunConfiguration<*>): Boolean {
        return true
    }

    override fun isEnabledFor(
        applicableConfiguration: PhpRunConfiguration<*>,
        runnerSettings: RunnerSettings?
    ): Boolean {
        return true
    }

    @Throws(ExecutionException::class)
    override fun patchCommandLine(
        configuration: PhpRunConfiguration<*>,
        runnerSettings: RunnerSettings?,
        cmdLine: GeneralCommandLine,
        runnerId: String
    ) {
        val dotenvxSettings: DotenvxSettings? =
            configuration.getCopyableUserData<DotenvxSettings?>(RunConfigSettingsEditor.USER_DATA_KEY)
        val newEnv: MutableMap<String, String> =
            RunConfigSettingsEditor.collectEnv(dotenvxSettings, configuration.project.basePath!!)
        for (set in newEnv.entries) {
            cmdLine.withEnvironment(set.key, set.value)
        }
    }
}
