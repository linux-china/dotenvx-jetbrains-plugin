package com.github.linuxchina.dotenvx.inspections

import com.github.linuxchina.dotenvx.DotenvxEncryptor
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.properties.parsing.PropertiesTokenTypes
import com.intellij.lang.properties.psi.impl.PropertyValueImpl
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.annotations.Nls

/**
 * Inspection for properties files: if key name contains password, key or secret, and the value does not start with
 * "encrypted:", report a problem and provide a quick fix to encrypt the value using dotenv.public.keys.
 */
class PropertiesSensitiveValueInspection : LocalInspectionTool() {

    override fun getGroupDisplayName(): @Nls(capitalization = Nls.Capitalization.Sentence) String {
        return "Dotenvx inspection"
    }

    override fun getDisplayName(): String = "Sensitive value is not encrypted"

    override fun getShortName(): String = "PropertiesFileSensitiveValueInspection"

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                // Only process tokens under DotEnvValue (actual text of the value)
                if (element is PropertyValueImpl) {
                    val valueText = element.text.trim().trim('"', '\'')
                    if (valueText.startsWith("encrypted:")) return
                    val keyElement =
                        PsiTreeUtil.findSiblingBackward(element, PropertiesTokenTypes.KEY_CHARACTERS, null) ?: return
                    val keyLower = keyElement.text.lowercase()
                    if (DotenvxEncryptor.isSensitiveKey(keyLower)) {
                        if (element.containingFile.text.contains("dotenv.public.key")) {
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

}
