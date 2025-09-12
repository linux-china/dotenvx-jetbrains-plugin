package com.github.linuxchina.dotenvx.marker

import com.intellij.codeInsight.daemon.GutterName
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.lang.properties.psi.impl.PropertyKeyImpl
import com.intellij.lang.properties.psi.impl.PropertyValueImpl
import com.intellij.psi.PsiElement


class PropertiesFileLineMarkerProvider : DotenvxBaseLineMarkerProvider() {
    override fun getName(): @GutterName String {
        return "Dotenvx variable"
    }

    override fun getLineMarkerInfo(psiElement: PsiElement): LineMarkerInfo<*>? {
        if (psiElement is PropertyValueImpl) {
            if (psiElement.text.startsWith("encrypted:")) {
                return lineMarkerForVariable(psiElement)
            }
        } else if (psiElement is PropertyKeyImpl) {
            if (psiElement.text.startsWith("dotenv.public.key")) {
                return lineMarkerForPublicKey(psiElement)
            }
        }
        return null
    }
}