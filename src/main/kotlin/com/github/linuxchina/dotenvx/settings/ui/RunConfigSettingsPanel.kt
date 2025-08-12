package com.github.linuxchina.dotenvx.settings.ui


import com.github.linuxchina.dotenvx.settings.DotenvxSettings
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.FlowLayout
import javax.swing.*

class RunConfigSettingsPanel() : JPanel() {
    private val useDotenvxCheckbox: JCheckBox = JCheckBox("Enable Dotenvx")
    private val envFileName = JTextField(".env", 32)

    init {
        val optionsPanel = JPanel()
        val bl2 = BoxLayout(optionsPanel, BoxLayout.PAGE_AXIS)
        optionsPanel.setLayout(bl2)
        optionsPanel.setBorder(JBUI.Borders.emptyLeft(20))

        val boxLayoutWrapper = JPanel()
        val bl1 = BoxLayout(boxLayoutWrapper, BoxLayout.PAGE_AXIS)
        boxLayoutWrapper.setLayout(bl1)
        // Add label before envFileName field
        val optionsRow = JPanel(FlowLayout(FlowLayout.LEFT, 5, 0))
        optionsRow.add(useDotenvxCheckbox)
        boxLayoutWrapper.add(optionsRow)
        // Add label before envFileName field
        val envRow = JPanel(FlowLayout(FlowLayout.LEFT, 5, 0))
        envRow.add(JLabel(".env file name:"))
        envRow.add(envFileName)
        boxLayoutWrapper.add(envRow)

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
