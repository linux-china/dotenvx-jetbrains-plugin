package com.github.linuxchina.dotenvx.marker

import com.intellij.codeInsight.daemon.GutterName
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.psi.PsiElement
import org.jetbrains.yaml.psi.YAMLScalar


class YamlFileLineMarkerProvider : DotenvxBaseLineMarkerProvider() {

    override fun getName(): @GutterName String {
        return "Dotenvx variable"
    }

    override fun getLineMarkerInfo(psiElement: PsiElement): LineMarkerInfo<*>? {
        if (psiElement.parent != null) {
            val parentElement = psiElement.parent
            if (parentElement is YAMLScalar) {
                val text = parentElement.text
                if (text.startsWith("encrypted:")) {
                    return lineMarkerForVariable(psiElement)
                } else if (text.length == 66 && text.matches(PUBLIC_KEY_REGEX)) {
                    return lineMarkerForPublicKey(psiElement)
                }
            }
        }
        return null
    }
}
