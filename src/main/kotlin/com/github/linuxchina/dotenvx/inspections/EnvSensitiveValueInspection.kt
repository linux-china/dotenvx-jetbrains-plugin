package com.github.linuxchina.dotenvx.inspections

import com.github.linuxchina.dotenvx.DotenvxEncryptor
import com.intellij.codeInspection.*
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.annotations.Nls
import ru.adelf.idea.dotenv.psi.DotEnvTypes
import ru.adelf.idea.dotenv.psi.DotEnvValue

/**
 * Inspection for .env files: if key name contains password, key or secret, and the value does not start with
 * "encrypted:", report a problem and provide a quick fix to encrypt the value using DOTENV_PUBLIC_KEY.
 */
class EnvSensitiveValueInspection : LocalInspectionTool() {

    override fun getGroupDisplayName(): @Nls(capitalization = Nls.Capitalization.Sentence) String {
        return "Dotenvx inspection"
    }

    override fun getDisplayName(): String = "Sensitive value is not encrypted"

    override fun getShortName(): String = "EnvFileSensitiveValueInspection"

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                // Only process tokens under DotEnvValue (actual text of the value)
                if (element is DotEnvValue) {
                    val valueText = element.text.trim().trim('"', '\'')
                    if (valueText.startsWith("encrypted:")) return
                    val keyElement = PsiTreeUtil.findSiblingBackward(element, DotEnvTypes.KEY, null) ?: return
                    val keyLower = keyElement.text.lowercase()
                    if (DotenvxEncryptor.isSensitiveKey(keyLower)) {
                        if (element.containingFile.text.contains("DOTENV_PUBLIC_KEY")) {
                            holder.registerProblem(
                                element,
                                "Sensitive value is not encrypted",
                                ProblemHighlightType.WARNING
                            )
                        }
                    }
                }
            }
        }
    }

    private class EncryptValueQuickFix : LocalQuickFix {
        override fun getName(): String = "Encrypt value with DOTENV_PUBLIC_KEY"

        override fun getFamilyName(): String = name

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val envValue = descriptor.psiElement as? DotEnvValue ?: return
            val file: PsiFile = envValue.containingFile ?: return
            val publicKey = DotenvxEncryptor.findPublicKey(file) ?: return
            val document = PsiDocumentManager.getInstance(project).getDocument(file) ?: return
            val originalText = envValue.text
            val plainValue = originalText.trim().trim('"', '\'')
            val encrypted = try {
                DotenvxEncryptor.encrypt(plainValue, publicKey)
            } catch (_: Exception) {
                return
            }
            WriteCommandAction.runWriteCommandAction(project) {
                val range = envValue.textRange
                document.replaceString(range.startOffset, range.endOffset, encrypted)
                PsiDocumentManager.getInstance(project).commitDocument(document)
            }
        }
    }
}
