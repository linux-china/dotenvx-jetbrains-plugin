package com.github.linuxchina.dotenvx.intentions

import com.github.linuxchina.dotenvx.DotenvxEncryptor
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.lang.properties.psi.Property
import com.intellij.lang.properties.psi.impl.PropertyValueImpl
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile

/**
 * Intention: Encrypt Property value in .properties files using dotenv.public.key.
 * Shown when the file contains dotenv.public.key and the value is not already encrypted.
 */
class EncryptPropertiesValueIntention : PsiElementBaseIntentionAction(), DumbAware {

    override fun getFamilyName(): String = "Dotenvx"

    override fun getText(): String = "Encrypt value with dotenv.public.key"



    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
        if (element !is PropertyValueImpl) return false
        val psiFile = element.containingFile ?: return false
        if (!psiFile.name.endsWith(".properties")) return false
        val property = element.parent as? Property ?: return false
        val value = property.value?.trim()?.trim('"', '\'') ?: return false
        if (value.startsWith("encrypted:")) return false
        val publicKey = findPublicKeyFromProperties(psiFile) ?: return false
        return publicKey.isNotEmpty()
    }

    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        val property = element.parent as? Property ?: return
        val psiFile = property.containingFile ?: return
        val publicKey: String = findPublicKeyFromProperties(psiFile) ?: return
        val propertyValue = property.value?.trim()?.trim('"', '\'') ?: return
        val encrypted = try {
            DotenvxEncryptor.encrypt(propertyValue, publicKey)
        } catch (_: Exception) {
            return
        }
        val document = editor?.document ?: return
        // Replace the value text
        WriteCommandAction.runWriteCommandAction(project) {
            property.setValue(encrypted)
            PsiDocumentManager.getInstance(project).commitDocument(document)
        }
    }

    override fun startInWriteAction(): Boolean = true

    private fun findPublicKeyFromProperties(file: PsiFile): String? {
        file.text.lines().forEach { line ->
            if (line.startsWith("dotenv.public.key")) {
                return line.substringAfter("=").trim().trim('"', '\'')
            }
        }
        return null
    }
}
