package com.github.linuxchina.dotenvx.intentions

import com.github.linuxchina.dotenvx.DotenvxEncryptor
import com.github.linuxchina.dotenvx.DotenvxEncryptor.findPublicKey
import com.github.linuxchina.dotenvx.LOCKER_ICON
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.json.JsonElementTypes.DOUBLE_QUOTED_STRING
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Iconable
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import org.toml.lang.psi.ext.elementType
import javax.swing.Icon

/**
 * Intention: Encrypt JSON literal value in YAML files using dotenv.public.key.
 * Shown when the file contains dotenv.public.key and the value is not already encrypted.
 */
class EncryptJsonValueIntention : PsiElementBaseIntentionAction(), DumbAware, Iconable {

    override fun getFamilyName(): String = "Dotenvx"

    override fun getText(): String = "Encrypt JSON value with dotenv.public.key"


    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
        if (element.elementType == DOUBLE_QUOTED_STRING) {
            val psiFile = element.containingFile ?: return false
            val value = element.text.trim('"')
            if (!value.startsWith("encrypted:")) {
                val publicKey = findPublicKey(psiFile) ?: return false
                return publicKey.isNotEmpty()
            }
        }
        return false
    }

    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        val jsonLiteral = element.parent as? JsonStringLiteral ?: return
        val psiFile = jsonLiteral.containingFile ?: return
        val publicKey: String = findPublicKey(psiFile) ?: return
        val propertyPlainValue = jsonLiteral.text?.trim('"') ?: return
        val encryptedValue = try {
            DotenvxEncryptor.encrypt(propertyPlainValue, publicKey)
        } catch (_: Exception) {
            return
        }
        val document = editor?.document ?: return
        // Replace the value text
        WriteCommandAction.runWriteCommandAction(project) {
            val startOffset = jsonLiteral.textRange.startOffset
            val endOffset = jsonLiteral.textRange.endOffset
            document.replaceString(startOffset, endOffset, "\"${encryptedValue}\"")
            PsiDocumentManager.getInstance(project).commitDocument(document)
        }
    }

    override fun startInWriteAction(): Boolean = false

    override fun getIcon(p0: Int): Icon {
        return LOCKER_ICON
    }
}
