package com.github.linuxchina.dotenvx.utils

import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile

object YamlFileUtils {

    fun isYamlOrToml(psiFile: PsiFile): Boolean {
        val name = psiFile.name
        return name.endsWith(".yaml") || name.endsWith(".yml") || name.endsWith(".toml")
    }

    fun getKeyNameOnLine(editor: Editor): String {
        val document = editor.document
        val caretModel = editor.caretModel
        val offset = caretModel.offset
        val lineNumber = document.getLineNumber(offset)
        val lineStartOffset = document.getLineStartOffset(lineNumber)
        val lineEndOffset = document.getLineEndOffset(lineNumber)
        val keyName = document.getText(com.intellij.openapi.util.TextRange(lineStartOffset, lineEndOffset)).trim()
        return if (keyName.contains(':')) {
            keyName.substringBefore(':').trim()
        } else {
            keyName
        }
    }
}