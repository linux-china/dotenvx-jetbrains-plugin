package com.github.linuxchina.dotenvx.marker

import com.intellij.codeInsight.daemon.GutterName
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import ru.adelf.idea.dotenv.psi.DotEnvKey
import ru.adelf.idea.dotenv.psi.DotEnvTokenType
import ru.adelf.idea.dotenv.psi.DotEnvValue


class EnvFileLineMarkerProvider : DotenvxBaseLineMarkerProvider() {
    override fun getName(): @GutterName String {
        return "Dotenvx variable"
    }

    override fun getLineMarkerInfo(psiElement: PsiElement): LineMarkerInfo<*>? {
        if (psiElement.elementType is DotEnvTokenType) {
            if (psiElement.parent is DotEnvValue) {
                if (psiElement.text.startsWith("encrypted:")) {
                    return lineMarkerForVariable(psiElement)
                }
            } else if (psiElement.parent is DotEnvKey) {
                if (psiElement.text.startsWith("DOTENV_PUBLIC_KEY")) {
                    return lineMarkerForPublicKey(psiElement)
                }
            }
        }
        return null
    }
}