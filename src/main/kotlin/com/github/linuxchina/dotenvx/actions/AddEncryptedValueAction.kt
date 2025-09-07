package com.github.linuxchina.dotenvx.actions

import com.github.linuxchina.dotenvx.DotenvxEncryptor
import com.github.linuxchina.dotenvx.utils.DotenvxFileUtils
import com.github.linuxchina.dotenvx.utils.DotenvxFileUtils.isYamlOrToml
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiDocumentManager

/**
 * Action to add an encrypted value for YAML or toml
 */
class AddEncryptedValueAction : AnAction(), DumbAware {

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    override fun update(e: AnActionEvent) {
        val psiFile = e.getData(CommonDataKeys.PSI_FILE)
        val visible = psiFile?.let { isYamlOrToml(it.name) } ?: false
        e.presentation.isEnabledAndVisible = visible
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val psiFile = e.getData(CommonDataKeys.PSI_FILE) ?: return
        if (!isYamlOrToml(psiFile.name)) return

        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val document = editor.document
        val caretModel = editor.caretModel
        val offset = caretModel.offset
        val keyName = DotenvxFileUtils.getKeyNameOnLine(psiFile.name, editor)

        val dialog = KeyValueDialog(project, "Add encrypted value", "value", keyName, null)
        dialog.keyField.isEditable = false
        if (!dialog.showAndGet()) return

        val value = dialog.value.trim()
        if (value.isEmpty()) {
            Messages.showErrorDialog(project, "Value must not be empty", "Invalid Input")
            return
        }
        val publicKey = DotenvxEncryptor.findPublicKey(psiFile)
        val newValue = if (publicKey.isNullOrEmpty()) {
            value
        } else {
            DotenvxEncryptor.encrypt(value, publicKey)
        }

        WriteCommandAction.runWriteCommandAction(project) {
            document.insertString(offset, newValue)
            PsiDocumentManager.getInstance(project).commitDocument(document)
        }

    }

}
