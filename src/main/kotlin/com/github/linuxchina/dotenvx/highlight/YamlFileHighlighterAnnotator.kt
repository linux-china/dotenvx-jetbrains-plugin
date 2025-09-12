package com.github.linuxchina.dotenvx.highlight

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.psi.PsiElement
import org.jetbrains.yaml.psi.YAMLScalar
import ru.adelf.idea.dotenv.psi.DotEnvValue


class YamlFileHighlighterAnnotator : Annotator {
    override fun annotate(psiElement: PsiElement, annotationHolder: AnnotationHolder) {
        if (psiElement is YAMLScalar) {
            val text: String = psiElement.text
            if (text.startsWith("encrypted:")) {
                val range = psiElement.textRange
                annotationHolder.newAnnotation(HighlightSeverity.INFORMATION, "Dotenvx encrypted value")
                    .range(range)
                    .textAttributes(DefaultLanguageHighlighterColors.CONSTANT)
                    .create()
            }

        }
    }
}