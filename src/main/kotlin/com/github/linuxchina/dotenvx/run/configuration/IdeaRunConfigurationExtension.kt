package com.github.linuxchina.dotenvx.run.configuration

import com.github.linuxchina.dotenvx.settings.DotenvxSettings
import com.intellij.execution.RunConfigurationExtension
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.JavaParameters
import com.intellij.execution.configurations.RunConfigurationBase
import com.intellij.execution.configurations.RunnerSettings
import com.intellij.openapi.options.SettingsEditor
import org.jdom.Element
import com.github.linuxchina.dotenvx.settings.ui.RunConfigSettingsEditor


class IdeaRunConfigurationExtension : RunConfigurationExtension() {

    override fun <T : RunConfigurationBase<*>?> updateJavaParameters(
        configuration: T & Any,
        params: JavaParameters,
        runnerSettings: RunnerSettings?
    ) {
        val workDir = params.workingDirectory
        val sourceEnv = GeneralCommandLine()
            .withEnvironment(params.env)
            .withParentEnvironmentType(
                if (params.isPassParentEnvs) GeneralCommandLine.ParentEnvironmentType.CONSOLE else GeneralCommandLine.ParentEnvironmentType.NONE
            )
            .effectiveEnvironment

        val state: DotenvxSettings? = RunConfigSettingsEditor.getState(configuration)
        if (state != null && state.dotenvxEnabled) {
            val envVars: MutableMap<String, String> =
                RunConfigSettingsEditor.collectEnv(configuration, workDir, sourceEnv)

            params.env = envVars
        }
    }


    protected override fun readExternal(
        runConfiguration: RunConfigurationBase<*>,
        element: Element
    ) {
        RunConfigSettingsEditor.readExternal(runConfiguration, element)
    }

    protected override fun writeExternal(
        runConfiguration: RunConfigurationBase<*>,
        element: Element
    ) {
        RunConfigSettingsEditor.writeExternal(runConfiguration, element)
    }

    override fun <P : RunConfigurationBase<*>?> createEditor(configuration: P & Any): SettingsEditor<P?> {
        return RunConfigSettingsEditor(configuration)
    }

    override fun isApplicableFor(p0: RunConfigurationBase<*>): Boolean {
        return true
    }

}