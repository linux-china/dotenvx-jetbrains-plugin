package com.github.linuxchina.dotenvx.marker

import com.github.linuxchina.dotenvx.VARIABLE_ICON
import com.intellij.codeInsight.daemon.GutterName
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProviderDescriptor
import com.intellij.lang.properties.psi.impl.PropertyValueImpl
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import org.jdom.filter2.Filters.element
import ru.adelf.idea.dotenv.psi.DotEnvTokenType
import ru.adelf.idea.dotenv.psi.DotEnvValue


class PropertiesFileLineMarkerProvider : LineMarkerProviderDescriptor() {
    override fun getName(): @GutterName String {
        return "Dotenvx variable"
    }

    override fun getLineMarkerInfo(psiElement: PsiElement): LineMarkerInfo<*>? {
        if (psiElement is PropertyValueImpl) {
            if (psiElement.text.startsWith("encrypted:")) {
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
        }
        return null
    }
}