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
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile

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

        val dialog = KeyValueDialog(project, "Add encrypted Key-Value", "key", null, null)
        if (!dialog.showAndGet()) return
        var key = dialog.key.trim()

        val fileName = psiFile.name
        if (fileName.startsWith(".env")) {
            key = key.replace("-", "_").replace(".", "_").uppercase()
        }
        val value = dialog.value.trim()
        if (key.isEmpty() || value.isEmpty()) {
            Messages.showErrorDialog(project, "Key/Value must not be empty", "Invalid Input")
            return
        }
        val publicKey = DotenvxEncryptor.findPublicKey(psiFile)
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
            val escapedNewValue = escapeShellValue(newValue)
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

    fun escapeShellValue(shellValue: String): String {
        var escapedNewValue = if (shellValue.startsWith("encrypted:")) {
            shellValue
        } else if (shellValue.contains("\"")) {
            "'" + shellValue.replace("'", "'\\''") + "'"
        } else if (shellValue.contains("'") || shellValue.contains(" ")) {
            "\"" + shellValue.replace("\"", "\\\"") + "\""
        } else {
            shellValue
        }
        if (escapedNewValue.contains('\n')) {
            escapedNewValue = escapedNewValue.replace("\n", "\\n")
        }
        return escapedNewValue
    }
}
