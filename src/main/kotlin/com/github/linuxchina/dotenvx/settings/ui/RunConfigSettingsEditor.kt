package com.github.linuxchina.dotenvx.settings.ui


import com.github.linuxchina.dotenvx.commands.DotenvxCmd
import com.github.linuxchina.dotenvx.settings.DotenvxSettings
import com.intellij.execution.configurations.RunConfigurationBase
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.util.JDOMExternalizerUtil
import com.intellij.openapi.util.Key
import org.jdom.Element
import javax.swing.JComponent

class RunConfigSettingsEditor<T : RunConfigurationBase<*>?>(configuration: RunConfigurationBase<*>) :
    SettingsEditor<T?>() {
    private val editor: RunConfigSettingsPanel = RunConfigSettingsPanel()

    override fun resetEditorFrom(configuration: T & Any) {
        val state: DotenvxSettings? = configuration.getCopyableUserData<DotenvxSettings>(USER_DATA_KEY)
        if (state != null) {
            editor.setState(state)
        }
    }

    override fun applyEditorTo(configuration: T & Any) {
        configuration.putCopyableUserData(USER_DATA_KEY, editor.state)
    }

    override fun createEditor(): JComponent {
        return editor
    }

    companion object {
        val USER_DATA_KEY: Key<DotenvxSettings?> = Key<DotenvxSettings?>("Dotenvx Settings")
        private const val FIELD_DOTENVX_ENABLED = "DOTENVX_ENABLED"
        private const val FIELD_ENV_FILE = "ENV_FILE"
        fun readExternal(configuration: RunConfigurationBase<*>, element: Element) {
            val isDotenvxEnabled = readBool(element, FIELD_DOTENVX_ENABLED)
            val envFileName = readText(element, FIELD_ENV_FILE)
            val state = DotenvxSettings(isDotenvxEnabled, envFileName)
            configuration.putCopyableUserData<DotenvxSettings?>(USER_DATA_KEY, state)
        }

        fun writeExternal(configuration: RunConfigurationBase<*>, element: Element) {
            val state: DotenvxSettings? = configuration.getCopyableUserData<DotenvxSettings?>(USER_DATA_KEY)
            if (state != null) {
                writeBool(element, FIELD_DOTENVX_ENABLED, state.dotenvxEnabled)
                writeText(element, FIELD_ENV_FILE, state.envFile ?: ".env")
            }
        }

        private fun readBool(element: Element, field: String): Boolean {
            return JDOMExternalizerUtil.readField(element, field).toBoolean()
        }

        private fun writeBool(element: Element, field: String, value: Boolean) {
            JDOMExternalizerUtil.writeField(element, field, value.toString())
        }

        private fun writeText(element: Element, field: String, value: String) {
            JDOMExternalizerUtil.writeField(element, field, value)
        }

        private fun readText(element: Element, field: String): String? {
            return JDOMExternalizerUtil.readField(element, field)
        }


        fun collectEnv(
            runConfigurationBase: RunConfigurationBase<*>,
            workingDirectory: String,
            runConfigEnv: MutableMap<String, String>
        ): MutableMap<String, String> {
            val envVars: MutableMap<String, String> = HashMap(runConfigEnv)

            val state: DotenvxSettings? = runConfigurationBase.getCopyableUserData<DotenvxSettings?>(USER_DATA_KEY)
            envVars.putAll(collectEnv(state, workingDirectory))

            return envVars
        }

        fun collectEnv(state: DotenvxSettings?, workingDirectory: String): MutableMap<String, String> {
            val envVars: MutableMap<String, String> = mutableMapOf()
            if (state != null && state.dotenvxEnabled) {
                val cmd = DotenvxCmd(workingDirectory, state.envFile)
                envVars.putAll(cmd.importEnv())
            }
            return envVars
        }

        fun getState(configuration: RunConfigurationBase<*>): DotenvxSettings? {
            val state: DotenvxSettings? = configuration.getCopyableUserData<DotenvxSettings?>(USER_DATA_KEY)
            return state
        }

        val editorTitle: String
            get() = "Dotenvx"
    }
}
