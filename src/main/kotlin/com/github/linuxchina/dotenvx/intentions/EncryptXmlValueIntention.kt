package com.github.linuxchina.dotenvx.intentions

import com.github.linuxchina.dotenvx.DotenvxEncryptor
import com.github.linuxchina.dotenvx.DotenvxEncryptor.findPublicKey
import com.github.linuxchina.dotenvx.LOCKER_ICON
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Iconable
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import com.intellij.psi.xml.XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN
import com.intellij.psi.xml.XmlTokenType.XML_DATA_CHARACTERS
import javax.swing.Icon

/**
 * Intention: Encrypt Xml value in Xml files using dotenv.public.key.
 * Shown when the file contains dotenv.public.key and the value is not already encrypted.
 */
class EncryptXmlValueIntention : PsiElementBaseIntentionAction(), DumbAware, Iconable {

    override fun getFamilyName(): String = "Dotenvx"

    override fun getText(): String = "Encrypt XML value with dotenv.public.key"


    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
        if (element.elementType == XML_DATA_CHARACTERS || element.elementType == XML_ATTRIBUTE_VALUE_TOKEN) {
            val psiFile = element.containingFile ?: return false
            val value = element.text?.trim() ?: return false
            if (value.startsWith("encrypted:")) return false
            val publicKey = findPublicKey(psiFile) ?: return false
            return publicKey.isNotEmpty()
        }
        return false
    }

    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        val psiFile = element.containingFile ?: return
        val publicKey: String = findPublicKey(psiFile) ?: return
        val propertyPlainValue = element.text?.trim()?.trim('"', '\'') ?: return
        val encryptedValue = try {
            DotenvxEncryptor.encrypt(propertyPlainValue, publicKey)
        } catch (_: Exception) {
            return
        }
        val document = editor?.document ?: return
        // Replace the value text
        WriteCommandAction.runWriteCommandAction(project) {
            val startOffset = element.textRange.startOffset
            val endOffset = element.textRange.endOffset
            document.replaceString(startOffset, endOffset, encryptedValue)
            PsiDocumentManager.getInstance(project).commitDocument(document)
        }
    }

    override fun startInWriteAction(): Boolean = true

    override fun getIcon(p0: Int): Icon {
        return LOCKER_ICON
    }
}
