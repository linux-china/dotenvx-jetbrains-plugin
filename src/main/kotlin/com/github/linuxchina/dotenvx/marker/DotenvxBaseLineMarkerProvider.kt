package com.github.linuxchina.dotenvx.marker

import com.github.linuxchina.dotenvx.KEY_ICON
import com.github.linuxchina.dotenvx.VARIABLE_ICON
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProviderDescriptor
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement

val PUBLIC_KEY_REGEX = "[a-f0-9]{66}".toRegex()

abstract class DotenvxBaseLineMarkerProvider : LineMarkerProviderDescriptor() {

    fun lineMarkerForVariable(psiElement: PsiElement): LineMarkerInfo<PsiElement> {
        return LineMarkerInfo(
            psiElement,
            psiElement.textRange,
            VARIABLE_ICON,
            { _: PsiElement? ->
                "Dotenvx encrypted variable"
            }, null,
            GutterIconRenderer.Alignment.CENTER,
            {
                "Dotenvx encrypted variable"
            }
        )
    }

    fun lineMarkerForPublicKey(psiElement: PsiElement): LineMarkerInfo<PsiElement> {
        return LineMarkerInfo(
            psiElement,
            psiElement.textRange,
            KEY_ICON,
            { _: PsiElement? ->
                "Dotenvx public key"
            }, null,
            GutterIconRenderer.Alignment.CENTER,
            {
                "Dotenvx public key"
            }
        )
    }
}