package com.github.linuxchina.dotenvx.settings.ui


import com.github.linuxchina.dotenvx.settings.DotenvxSettings
import com.intellij.openapi.ui.panel.ComponentPanelBuilder
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import javax.swing.BoxLayout
import javax.swing.JCheckBox
import javax.swing.JPanel
import javax.swing.JTextField

class RunConfigSettingsPanel() : JPanel() {
    private val useDotenvxCheckbox: JCheckBox = JCheckBox("Enable Dotenvx")
    private val envFileName = JTextField(".env File Name")

    init {
        val optionsPanel = JPanel()
        val bl2 = BoxLayout(optionsPanel, BoxLayout.PAGE_AXIS)
        optionsPanel.setLayout(bl2)
        optionsPanel.setBorder(JBUI.Borders.emptyLeft(20))

        val boxLayoutWrapper = JPanel()
        val bl1 = BoxLayout(boxLayoutWrapper, BoxLayout.PAGE_AXIS)
        boxLayoutWrapper.setLayout(bl1)
        boxLayoutWrapper.add(ComponentPanelBuilder(useDotenvxCheckbox).createPanel())
        boxLayoutWrapper.add(optionsPanel)
        boxLayoutWrapper.add(ComponentPanelBuilder(envFileName).withComment(".env file name").createPanel())

        setLayout(BorderLayout())
        add(boxLayoutWrapper, BorderLayout.NORTH)
    }

    val state: DotenvxSettings
        get() = DotenvxSettings(
            useDotenvxCheckbox.isSelected,
            envFileName.text
        )

    fun setState(state: DotenvxSettings) {
        useDotenvxCheckbox.setSelected(state.dotenvxEnabled)
        envFileName.text = state.envFile ?: ".env"
    }
}
