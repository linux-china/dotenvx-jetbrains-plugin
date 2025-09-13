package com.github.linuxchina.dotenvx.intentions

import com.github.linuxchina.dotenvx.DotenvxEncryptor
import com.github.linuxchina.dotenvx.DotenvxEncryptor.findPublicKey
import com.github.linuxchina.dotenvx.VARIABLE_ICON
import com.github.linuxchina.dotenvx.actions.KeyValueDialog
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.util.Iconable
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import ru.adelf.idea.dotenv.psi.DotEnvTokenType
import ru.adelf.idea.dotenv.psi.DotEnvTypes
import ru.adelf.idea.dotenv.psi.DotEnvValue
import javax.swing.Icon

/**
 * Intention: edit encrypted value in .env files using DOTENV_PUBLIC_KEY.
 * Shown when the file contains DOTENV_PUBLIC_KEY and the value is not already encrypted.
 */
class EditEnvValueIntention : PsiElementBaseIntentionAction(), DumbAware, Iconable {

    override fun getFamilyName(): String = "Dotenvx"

    override fun getText(): String = "Edit encrypted env value"

    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
        if (element.elementType  == DotEnvTypes.VALUE_CHARS && element.parent is DotEnvValue) {
            val psiFile = element.containingFile ?: return false
            val fileName = psiFile.name.lowercase()
            // Only .env or .env.* files (excluding .env.keys)
            if (!(fileName == ".env" || (fileName.startsWith(".env.") && fileName != ".env.keys"))) return false
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
        val envValue = element.parent as? DotEnvValue ?: return
        val psiFile = envValue.containingFile ?: return
        val publicKey = findPublicKey(psiFile) ?: return
        val document = PsiDocumentManager.getInstance(project).getDocument(psiFile) ?: return
        val originalText = envValue.text
        var encryptedValue = originalText.trim().trim('"', '\'')
        val projectDir = psiFile.project.guessProjectDir()?.path!!
        val fileName = psiFile.name.lowercase()
        val profileName: String? = DotenvxEncryptor.getProfileName(fileName)
        val privateKey = DotenvxEncryptor.getDotenvxPrivateKey(projectDir, profileName, publicKey)!!
        var plainValue = DotenvxEncryptor.decrypt(encryptedValue, privateKey)
        val keyName = envValue.parent.firstChild.text
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
            val range = envValue.textRange
            document.replaceString(range.startOffset, range.endOffset, encryptedValue)
            PsiDocumentManager.getInstance(project).commitDocument(document)
        }
    }

    override fun startInWriteAction(): Boolean = false

    override fun getIcon(p0: Int): Icon {
        return VARIABLE_ICON
    }
}
