package com.github.linuxchina.dotenvx.intentions

import com.github.linuxchina.dotenvx.DotenvxEncryptor
import com.github.linuxchina.dotenvx.DotenvxEncryptor.findPublicKey
import com.github.linuxchina.dotenvx.LOCKER_ICON
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.lang.properties.psi.Property
import com.intellij.lang.properties.psi.impl.PropertyValueImpl
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.util.Iconable
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import javax.swing.Icon

/**
 * Intention: Encrypt Property value in .properties files using dotenv.public.key.
 * Shown when the file contains dotenv.public.key and the value is not already encrypted.
 */
class EncryptPropertiesValueIntention : PsiElementBaseIntentionAction(), DumbAware, Iconable {

    override fun getFamilyName(): String = "Dotenvx"

    override fun getText(): String = "Encrypt property value with dotenv.public.key"



    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
        if (element !is PropertyValueImpl) return false
        val psiFile = element.containingFile ?: return false
        if (!psiFile.name.endsWith(".properties")) return false
        val property = element.parent as? Property ?: return false
        val value = property.value?.trim()?.trim('"', '\'') ?: return false
        if (value.startsWith("encrypted:")) return false
        if(property.name?.contains("dotenv.public.key") == true) {
            return false
        }
        val publicKey = findPublicKey(psiFile) ?: return false
        return publicKey.isNotEmpty()
    }

    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        val property = element.parent as? Property ?: return
        val psiFile = property.containingFile ?: return
        val publicKey: String = findPublicKey(psiFile) ?: return
        val propertyPlainValue = property.value?.trim()?.trim('"', '\'') ?: return
        val encryptedValue = try {
            DotenvxEncryptor.encrypt(propertyPlainValue, publicKey)
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

    override fun getIcon(p0: Int): Icon {
           return LOCKER_ICON
       }
}
