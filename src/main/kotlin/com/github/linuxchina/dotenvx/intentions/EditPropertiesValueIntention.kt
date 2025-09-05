package com.github.linuxchina.dotenvx.intentions

import com.github.linuxchina.dotenvx.DotenvxEncryptor
import com.github.linuxchina.dotenvx.DotenvxEncryptor.findPublicKey
import com.github.linuxchina.dotenvx.DotenvxEncryptor.getProfileName
import com.github.linuxchina.dotenvx.actions.KeyValueDialog
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.lang.properties.psi.Property
import com.intellij.lang.properties.psi.impl.PropertyValueImpl
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement

/**
 * Intention: edit Property value in .properties files using dotenv.public.key.
 * Shown when the file contains dotenv.public.key and the value is not already encrypted.
 */
class EditPropertiesValueIntention : PsiElementBaseIntentionAction(), DumbAware {

    override fun getFamilyName(): String = "Dotenvx"

    override fun getText(): String = "Edit encrypted property value"


    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
        if (element !is PropertyValueImpl) return false
        val psiFile = element.containingFile ?: return false
        val fileName = psiFile.name
        if (!fileName.endsWith(".properties")) return false
        val property = element.parent as? Property ?: return false
        val value = property.value?.trim()?.trim('"', '\'') ?: return false
        if (!value.startsWith("encrypted:")) return false
        val publicKey = findPublicKey(psiFile) ?: return false
        val profileName: String? = getProfileName(fileName)
        val projectDir = project.guessProjectDir()?.path!!
        val privateKey = DotenvxEncryptor.getDotenvxPrivateKey(projectDir, profileName, publicKey) ?: return false
        return privateKey.isNotEmpty()
    }

    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        val property = element.parent as? Property ?: return
        val psiFile = property.containingFile ?: return
        val fileName = psiFile.name
        val publicKey: String = findPublicKey(psiFile) ?: return
        var encryptedValue = property.value?.trim()?.trim('"', '\'') ?: return
        val profileName: String? = getProfileName(fileName)
        val projectDir = project.guessProjectDir()?.path!!
        val privateKey = DotenvxEncryptor.getDotenvxPrivateKey(projectDir, profileName, publicKey)!!
        var plainValue = DotenvxEncryptor.decrypt(encryptedValue, privateKey)
        val keyName = property.key
        val dialog = KeyValueDialog(project, "Edit encrypted value", "value", keyName, plainValue)
        dialog.keyField.isEditable = false
        if (!dialog.showAndGet()) return
        plainValue = dialog.value.trim()
        encryptedValue = try {
            DotenvxEncryptor.encrypt(plainValue, publicKey)
        } catch (_: Exception) {
            return
        }
        val document = editor?.document ?: return
        // Replace the value text
        WriteCommandAction.runWriteCommandAction(project) {
            property.setValue(encryptedValue)
            PsiDocumentManager.getInstance(project).commitDocument(document)
        }
    }

    override fun startInWriteAction(): Boolean = false

}
