package com.github.linuxchina.dotenvx.actions

import com.github.linuxchina.dotenvx.DotenvxEncryptor
import com.intellij.lang.properties.psi.PropertiesFile
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent

/**
 * Action to add a key=value entry to the end of a .env or .properties file.
 */
class AddKeyValueAction : AnAction(), DumbAware {

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    override fun update(e: AnActionEvent) {
        val psiFile = e.getData(CommonDataKeys.PSI_FILE)
        val visible = psiFile?.let { isEnvOrProperties(it) } ?: false
        e.presentation.isEnabledAndVisible = visible
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val psiFile = e.getData(CommonDataKeys.PSI_FILE) ?: return
        if (!isEnvOrProperties(psiFile)) return

        val dialog = KeyValueDialog(project)
        if (!dialog.showAndGet()) return
        var key = dialog.key.trim()

        val fileName = psiFile.name
        if (fileName.startsWith(".env")) {
            key = key.replace("-", "_").replace(".", "_").uppercase()
        }
        val value = dialog.value
        if (key.isEmpty() || value.isEmpty()) {
            Messages.showErrorDialog(project, "Key/Value must not be empty", "Invalid Input")
            return
        }
        var publicKeyName = "DOTENV_PUBLIC_KEY"
        if (fileName.endsWith(".properties")) {
            publicKeyName = "dotenv.public.key"
        }
        val publicKey =
            psiFile.text.lines().find { it.startsWith(publicKeyName) }?.substringAfter('=')?.trim()?.trim('"', '\'')
        val newValue = if (publicKey.isNullOrEmpty()) {
            value
        } else {
            DotenvxEncryptor.encrypt(value, publicKey)
        }
        if (fileName.endsWith(".properties")) {
            // Try using PSI for .properties
            val propertiesFile = psiFile as? PropertiesFile
            val existing = propertiesFile?.findPropertyByKey(key)
            if (existing != null) {
                WriteCommandAction.runWriteCommandAction(project) {
                    existing.setValue(newValue)
                }
            } else {
                appendLineToFile(project, psiFile, "$key=$newValue")
            }
        } else {
            // .env: update line-based if key exists; otherwise append
            // escape new value for shell
            var escapedNewValue = if (newValue.startsWith("encrypted:")) {
                newValue
            } else if (newValue.contains("\"")) {
                "'" + newValue.replace("'", "'\\''") + "'"
            } else if (newValue.contains("'") || newValue.contains(" ")) {
                "\"" + newValue.replace("\"", "\\\"") + "\""
            } else {
                newValue
            }
            if (escapedNewValue.contains('\n')) {
                escapedNewValue = escapedNewValue.replace("\n", "\\n")
            }
            val document = psiFile.fileDocument
            val kvRegex = Regex("^\\s*" + Regex.escape(key) + "=.*$", RegexOption.MULTILINE)
            val match = kvRegex.find(document.text)
            if (match != null) {
                WriteCommandAction.runWriteCommandAction(project) {
                    val newLine = "$key=$escapedNewValue"
                    val start = match.range.first
                    val end = match.range.last + 1
                    document.replaceString(start, end, newLine)
                    PsiDocumentManager.getInstance(project).commitDocument(document)
                }
            } else {
                appendLineToFile(project, psiFile, "$key=$escapedNewValue")
            }
        }
    }

    private fun isEnvOrProperties(psiFile: PsiFile): Boolean {
        val name = psiFile.name
        if (name == ".env" || (name.startsWith(".env.") && name != ".env.keys")) {
            return psiFile.text.contains("DOTENV_PUBLIC_KEY");
        } else if (name.endsWith(".properties")) {
            return psiFile.text.contains("dotenv.public.key")
        }
        return false
    }

    private fun appendLineToFile(project: Project, psiFile: PsiFile, line: String) {
        val document = psiFile.fileDocument
        WriteCommandAction.runWriteCommandAction(project) {
            val text = document.text
            val needsNewline = text.isNotEmpty() && !text.endsWith("\n")
            val toAppend = buildString {
                if (needsNewline) append('\n')
                append(line)
                append('\n')
            }
            document.insertString(document.textLength, toAppend)
            PsiDocumentManager.getInstance(project).commitDocument(document)
        }
    }
}

private class KeyValueDialog(project: Project) : DialogWrapper(project) {
    private val keyField = JBTextField()
    private val valueField = JBTextArea()

    val key: String get() = keyField.text
    val value: String get() = valueField.text.trimEnd()

    init {
        title = "Add Encrypted Key-Value"
        isResizable = true
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
        valueField.lineWrap = true
        valueField.wrapStyleWord = true
        // Make it visually similar to text field initially (single-line height)
        valueField.border = keyField.border
        valueField.preferredSize = Dimension(480, keyField.preferredSize.height)

        val valueScroll = JBScrollPane(valueField)
        valueScroll.border = null
        form.add(valueScroll, c)

        panel.add(form, BorderLayout.CENTER)
        return panel
    }

    override fun getPreferredFocusedComponent(): JComponent {
        return keyField
    }

}