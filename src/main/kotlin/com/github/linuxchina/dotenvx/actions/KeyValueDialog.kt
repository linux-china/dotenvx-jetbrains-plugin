package com.github.linuxchina.dotenvx.actions

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.util.text.StringUtil
import com.intellij.ui.components.JBTextField
import com.intellij.ui.components.fields.ExpandableTextField
import com.intellij.util.Function
import com.intellij.util.execution.ParametersListUtil
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

class KeyValueDialog(project: Project, dialogTitle: String, val focusName: String, keyName: String?, value: String?) :
    DialogWrapper(project) {
    var rawValue = ""
    val keyField = JBTextField()
    val valueField = ExpandableTextField(
        ParametersListUtil.DEFAULT_LINE_PARSER,
        Function { lines: MutableList<String> ->
            rawValue = StringUtil.join(lines, "\n")
            rawValue
        })

    val key: String get() = keyField.text
    val value: String get() = rawValue.ifEmpty { valueField.text.trimEnd() }

    init {
        title = dialogTitle
        isResizable = true
        if (keyName != null) {
            keyField.text = keyName
        }
        if (value != null) {
            valueField.text = value
        }
        init()
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel()
        panel.layout = BorderLayout(0, 8)

        val form = JPanel()
        form.layout = java.awt.GridBagLayout()
        val c = java.awt.GridBagConstraints()
        c.gridx = 0
        c.gridy = 0
        c.anchor = java.awt.GridBagConstraints.WEST
        c.insets = JBUI.insets(4, 0, 4, 8)
        form.add(JLabel("Key:"), c)
        c.gridx = 1
        c.weightx = 1.0
        c.fill = java.awt.GridBagConstraints.HORIZONTAL
        keyField.preferredSize = Dimension(480, keyField.preferredSize.height)
        form.add(keyField, c)

        c.gridx = 0
        c.gridy = 1
        c.weightx = 0.0
        c.fill = java.awt.GridBagConstraints.NONE
        form.add(JLabel("Value:"), c)
        c.gridx = 1
        c.weightx = 1.0
        c.weighty = 1.0
        c.fill = java.awt.GridBagConstraints.BOTH

        // Configure textarea to look like a text field initially, but allow multi-line and resizing
        // Make it visually similar to text field initially (single-line height)
        valueField.border = keyField.border
        valueField.preferredSize = Dimension(480, keyField.preferredSize.height)
        form.add(valueField, c)

        panel.add(form, BorderLayout.CENTER)
        return panel
    }

    override fun getPreferredFocusedComponent(): JComponent {
        return if (focusName == "key") {
            keyField
        } else {
            valueField
        }
    }

}