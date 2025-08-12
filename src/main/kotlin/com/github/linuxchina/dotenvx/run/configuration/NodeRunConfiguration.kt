package com.github.linuxchina.dotenvx.run.configuration


import com.github.linuxchina.dotenvx.settings.ui.RunConfigSettingsEditor
import com.intellij.execution.ExecutionException
import com.intellij.execution.Executor
import com.intellij.execution.Location
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.RunnerSettings
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.javascript.nodejs.execution.AbstractNodeTargetRunProfile
import com.intellij.javascript.nodejs.execution.runConfiguration.AbstractNodeRunConfigurationExtension
import com.intellij.javascript.nodejs.execution.runConfiguration.NodeRunConfigurationLaunchSession
import com.intellij.lang.javascript.buildTools.npm.rc.NpmRunConfiguration
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.guessProjectDir
import com.jetbrains.nodejs.run.NodeJsRunConfiguration
import org.jdom.Element

class NodeRunConfiguration : AbstractNodeRunConfigurationExtension() {
    protected override fun readExternal(runConfiguration: AbstractNodeTargetRunProfile, element: Element) {
        RunConfigSettingsEditor.readExternal(runConfiguration, element)
    }

    protected override fun writeExternal(runConfiguration: AbstractNodeTargetRunProfile, element: Element) {
        RunConfigSettingsEditor.writeExternal(runConfiguration, element)
    }

    override fun <P : AbstractNodeTargetRunProfile> createEditor(configuration: P): SettingsEditor<P> {
        return RunConfigSettingsEditor<NodeJsRunConfiguration>() as SettingsEditor<P>
    }


    public override fun isApplicableFor(configuration: AbstractNodeTargetRunProfile): Boolean {
        return true
    }

    @Throws(ExecutionException::class)
    protected override fun patchCommandLine(
        configuration: AbstractNodeTargetRunProfile,
        runnerSettings: RunnerSettings?,
        cmdLine: GeneralCommandLine,
        runnerId: String,
        executor: Executor
    ) {
        configuration.selectedOptions
    }

    protected override fun extendCreatedConfiguration(
        configuration: AbstractNodeTargetRunProfile,
        location: Location<*>
    ) {
        super.extendCreatedConfiguration(configuration, location)
    }

    override fun getEditorTitle(): String {
        return RunConfigSettingsEditor.editorTitle
    }

    @Throws(ExecutionException::class)
    public override fun createLaunchSession(
        configuration: AbstractNodeTargetRunProfile,
        environment: ExecutionEnvironment
    ): NodeRunConfigurationLaunchSession? {
        if (configuration is NodeJsRunConfiguration) {

            val newEnvs: MutableMap<String, String> = RunConfigSettingsEditor
                .collectEnv(configuration, configuration.workingDirectory!!, configuration.getEnvs())

            configuration.envs = newEnvs
        } else if (configuration is NpmRunConfiguration) {
            val newEnvs: MutableMap<String, String> = RunConfigSettingsEditor
                .collectEnv(
                    configuration,
                    configuration.project.guessProjectDir()!!.toString(),
                    configuration.envData.envs
                )
            configuration.envData.with(newEnvs)
        }


        return null
    }
}