package com.github.linuxchina.dotenvx.intentions

import com.github.linuxchina.dotenvx.DotenvxEncryptor
import com.github.linuxchina.dotenvx.DotenvxEncryptor.findPublicKey
import com.github.linuxchina.dotenvx.utils.YamlFileUtils.isYamlOrToml
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import org.jetbrains.yaml.psi.YAMLScalar

/**
 * Intention: Encrypt YAML scala value in YAML files using dotenv.public.key.
 * Shown when the file contains dotenv.public.key and the value is not already encrypted.
 */
class EncryptYamlValueIntention : PsiElementBaseIntentionAction(), DumbAware {

    override fun getFamilyName(): String = "Dotenvx"

    override fun getText(): String = "Encrypt YAML value with dotenv.public.key"


    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
        if (element.parent !is YAMLScalar) return false
        val psiFile = element.containingFile ?: return false
        if (!isYamlOrToml(psiFile)) return false
        val value = element.text?.trim() ?: return false
        if (value.startsWith("encrypted:")) return false
        val publicKey = findPublicKey(psiFile) ?: return false
        return publicKey.isNotEmpty()
    }

    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        val yamlScalar = element.parent as YAMLScalar
        val psiFile = yamlScalar.containingFile ?: return
        val publicKey: String = findPublicKey(psiFile) ?: return
        val propertyPlainValue = yamlScalar.text?.trim()?.trim('"', '\'') ?: return
        val encryptedValue = try {
            DotenvxEncryptor.encrypt(propertyPlainValue, publicKey)
        } catch (_: Exception) {
            return
        }
        val document = editor?.document ?: return
        // Replace the value text
        WriteCommandAction.runWriteCommandAction(project) {
            val startOffset = yamlScalar.textRange.startOffset
            val endOffset = yamlScalar.textRange.endOffset
            document.replaceString(startOffset, endOffset, encryptedValue)
            PsiDocumentManager.getInstance(project).commitDocument(document)
        }
    }

    override fun startInWriteAction(): Boolean = true

}
