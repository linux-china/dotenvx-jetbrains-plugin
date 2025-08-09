package com.github.linuxchina.dotenvx.actions

import com.github.linuxchina.dotenvx.DotenvxEncryptor
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
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

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
        val line = if (publicKey.isNullOrEmpty()) {
            "$key=$value"
        } else {
            val encryptedValue = DotenvxEncryptor.encrypt(value, publicKey)
            "$key=$encryptedValue"
        }
        appendLineToFile(project, psiFile, line)
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
    private val valueField = JBTextField()

    val key: String get() = keyField.text
    val value: String get() = valueField.text

    init {
        title = "Add Encrypted Key-Value"
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
        c.fill = java.awt.GridBagConstraints.HORIZONTAL
        valueField.preferredSize = Dimension(480, valueField.preferredSize.height)
        form.add(valueField, c)

        panel.add(form, BorderLayout.CENTER)
        return panel
    }

    override fun getPreferredFocusedComponent(): JComponent {
        return keyField
    }

}