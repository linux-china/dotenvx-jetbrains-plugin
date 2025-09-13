package com.github.linuxchina.dotenvx.intentions

import com.github.linuxchina.dotenvx.DotenvxEncryptor
import com.github.linuxchina.dotenvx.DotenvxEncryptor.findPublicKey
import com.github.linuxchina.dotenvx.VARIABLE_ICON
import com.github.linuxchina.dotenvx.actions.KeyValueDialog
import com.github.linuxchina.dotenvx.utils.DotenvxFileUtils
import com.github.linuxchina.dotenvx.utils.DotenvxFileUtils.isYamlOrToml
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.util.Iconable
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import org.jetbrains.yaml.psi.YAMLScalar
import javax.swing.Icon

/**
 * Intention: edit encrypted value in YAML files using dotenv.public.key.
 * Shown when the file contains dotenv.public.key and the value is not already encrypted.
 */
class EditYamlValueIntention : PsiElementBaseIntentionAction(), DumbAware, Iconable {

    override fun getFamilyName(): String = "Dotenvx"

    override fun getText(): String = "Edit encrypted YAML value"

    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
        if (element.parent is YAMLScalar) {
            val psiFile = element.containingFile ?: return false
            val fileName = psiFile.name.lowercase()
            // Only .env or .env.* files (excluding .env.keys)
            if (!(isYamlOrToml(fileName))) return false
            val plainValue = element.text.trim().trim('"', '\'')
            // Do not offer if already encrypted
            if (!plainValue.contains("encrypted:")) return false
            // Require DOTENV_PUBLIC_KEY in file
            val publicKey = findPublicKey(psiFile) ?: return false
            val projectDir = psiFile.project.guessProjectDir()?.path!!
            val profileName: String? = DotenvxEncryptor.getProfileName(fileName)
            val privateKey = DotenvxEncryptor.getDotenvxPrivateKey(projectDir, profileName, publicKey) ?: return false
            return privateKey.isNotEmpty()
        }
        return false
    }

    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        val yamlScalar = element.parent as? YAMLScalar ?: return
        val file = yamlScalar.containingFile ?: return
        val publicKey = findPublicKey(file) ?: return
        val originalText = yamlScalar.text
        var encryptedValue = originalText.trim().trim('"', '\'')
        val projectDir = file.project.guessProjectDir()?.path!!
        val fileName = file.name.lowercase()
        val profileName: String? = DotenvxEncryptor.getProfileName(fileName)
        val privateKey = DotenvxEncryptor.getDotenvxPrivateKey(projectDir, profileName, publicKey)!!
        var plainValue = DotenvxEncryptor.decrypt(encryptedValue, privateKey)
        val document = editor!!.document
        val caretModel = editor.caretModel
        val offset = caretModel.offset
        val keyName = DotenvxFileUtils.getKeyNameOnLine(fileName, editor)
        val dialog = KeyValueDialog(project, "Edit encrypted value", "value", keyName, plainValue)
        dialog.keyField.isEditable = false
        if (!dialog.showAndGet()) return
        plainValue = dialog.value.trim()
        encryptedValue = try {
            DotenvxEncryptor.encrypt(plainValue, publicKey)
        } catch (_: Exception) {
            return
        }
        WriteCommandAction.runWriteCommandAction(project) {
            val range = yamlScalar.textRange
            document.replaceString(range.startOffset, range.endOffset, encryptedValue)
            PsiDocumentManager.getInstance(project).commitDocument(document)
        }
    }

    override fun startInWriteAction(): Boolean = false

    override fun getIcon(p0: Int): Icon {
        return VARIABLE_ICON
    }
}
