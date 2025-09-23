package com.github.linuxchina.dotenvx.utils

import com.intellij.openapi.editor.Editor

object DotenvxFileUtils {

    fun isYamlOrToml(fileName: String): Boolean {
        return fileName.endsWith(".yaml") || fileName.endsWith(".yml") || fileName.endsWith(".toml")
    }

    fun getKeyNameOnLine(fileName: String, editor: Editor): String {
        val document = editor.document
        val caretModel = editor.caretModel
        val offset = caretModel.offset
        val lineNumber = document.getLineNumber(offset)
        val lineStartOffset = document.getLineStartOffset(lineNumber)
        val lineEndOffset = document.getLineEndOffset(lineNumber)
        val keyName = document.getText(com.intellij.openapi.util.TextRange(lineStartOffset, lineEndOffset)).trim()
        return if (isYamlOrToml(fileName) && keyName.contains(':')) {
            keyName.substringBefore(':').trim()
        } else if (fileName.endsWith(".properties") && keyName.contains("=")) {
            keyName.substringBefore('=').trim()
        } else if (fileName == ".env" || fileName.startsWith(".env.")) {
            keyName.substringBefore('=').trim()
        } else if (fileName.endsWith(".toml")) {
            keyName.substringBefore('=').trim()
        } else if (fileName.endsWith(".json")) {
            keyName.substringBefore(':').trim('"')
        } else {
            keyName
        }
    }
}