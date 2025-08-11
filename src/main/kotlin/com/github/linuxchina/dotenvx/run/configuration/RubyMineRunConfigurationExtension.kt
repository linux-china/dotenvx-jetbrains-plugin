package com.github.linuxchina.dotenvx.run.configuration


import com.github.linuxchina.dotenvx.settings.DotenvxSettings
import com.github.linuxchina.dotenvx.settings.ui.RunConfigSettingsEditor
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.RunnerSettings
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.util.NlsContexts
import org.jdom.Element
import org.jetbrains.plugins.ruby.ruby.run.configuration.AbstractRubyRunConfiguration
import org.jetbrains.plugins.ruby.ruby.run.configuration.RubyRunConfigurationExtension

class RubyMineRunConfigurationExtension : RubyRunConfigurationExtension() {
    override fun readExternal(runConfiguration: AbstractRubyRunConfiguration<*>, element: Element) {
        RunConfigSettingsEditor.readExternal(runConfiguration, element)
    }

    override fun writeExternal(runConfiguration: AbstractRubyRunConfiguration<*>, element: Element) {
        RunConfigSettingsEditor.writeExternal(runConfiguration, element)
    }

    override fun <P : AbstractRubyRunConfiguration<*>?> createEditor(configuration: P): SettingsEditor<P?>? {
        return RunConfigSettingsEditor()
    }

    override fun getEditorTitle(): @NlsContexts.TabTitle String {
        return RunConfigSettingsEditor.editorTitle
    }

    override fun isApplicableFor(configuration: AbstractRubyRunConfiguration<*>): Boolean {
        return true
    }

    override fun isEnabledFor(
        applicableConfiguration: AbstractRubyRunConfiguration<*>,
        runnerSettings: RunnerSettings?
    ): Boolean {
        return true
    }

    override fun patchCommandLine(
        configuration: AbstractRubyRunConfiguration<*>,
        runnerSettings: RunnerSettings?,
        cmdLine: GeneralCommandLine,
        runnerId: String
    ) {
        val dotenvxSettings = configuration.getCopyableUserData<DotenvxSettings?>(RunConfigSettingsEditor.USER_DATA_KEY)
        val newEnv: MutableMap<String, String> =
            RunConfigSettingsEditor.collectEnv(dotenvxSettings, configuration.project.basePath!!)
        for (set in newEnv.entries) {
            cmdLine.withEnvironment(set.key, set.value)
        }
    }
}
