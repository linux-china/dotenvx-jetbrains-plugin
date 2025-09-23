package com.github.linuxchina.dotenvx.marker

import com.intellij.codeInsight.daemon.GutterName
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.psi.PsiElement

class JsonFileLineMarkerProvider : DotenvxBaseLineMarkerProvider() {

    override fun getName(): @GutterName String {
        return "Dotenvx variable"
    }

    override fun getLineMarkerInfo(psiElement: PsiElement): LineMarkerInfo<*>? {
        if (psiElement is JsonStringLiteral) {
            val text = psiElement.text.trim('"')
            if (text.startsWith("encrypted:")) {
                return lineMarkerForVariable(psiElement)
            } else if (text.length == 66 && text.matches(PUBLIC_KEY_REGEX)) {
                return lineMarkerForPublicKey(psiElement)
            }
        }
        return null
    }
}
