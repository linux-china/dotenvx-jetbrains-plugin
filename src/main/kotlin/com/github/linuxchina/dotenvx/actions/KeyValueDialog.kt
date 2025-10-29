package com.github.linuxchina.dotenvx.actions

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.util.text.StringUtil
import com.intellij.ui.components.JBTextField
import com.intellij.ui.components.fields.ExpandableTextField
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.Function
import com.intellij.util.execution.ParametersListUtil
import java.awt.Dimension
import javax.swing.JComponent

class KeyValueDialog(project: Project, dialogTitle: String, val focusName: String, keyName: String?, value: String?) :
    DialogWrapper(project) {
    var rawValue = ""
    val keyField = JBTextField().apply {
        preferredSize = Dimension(480, this.preferredSize.height)
    }
    val valueField = ExpandableTextField(
        ParametersListUtil.DEFAULT_LINE_PARSER,
        Function { lines: MutableList<String> ->
            rawValue = StringUtil.join(lines, "\n")
            rawValue
        }).apply {
        this.preferredSize = Dimension(480, keyField.preferredSize.height)
    }

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
        return panel {
            row("Key:") {
                cell(keyField)
            }
            row("Value:") {
                cell(valueField).align(Align.FILL)
            }
        }
    }

    override fun getPreferredFocusedComponent(): JComponent {
        return if (focusName == "key") {
            keyField
        } else {
            valueField
        }
    }

}