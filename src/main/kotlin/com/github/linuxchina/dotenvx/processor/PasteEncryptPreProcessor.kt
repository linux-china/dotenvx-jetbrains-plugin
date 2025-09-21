package com.github.linuxchina.dotenvx.processor

import com.github.linuxchina.dotenvx.DotenvxEncryptor
import com.github.linuxchina.dotenvx.DotenvxEncryptor.findPublicKey
import com.github.linuxchina.dotenvx.utils.DotenvxFileUtils
import com.intellij.codeInsight.editorActions.CopyPastePreProcessor
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.RawText
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile


class PasteEncryptPreProcessor : CopyPastePreProcessor {

    override fun preprocessOnCopy(
        file: PsiFile?,
        startOffsets: IntArray?,
        endOffsets: IntArray?,
        text: String?
    ): String? {
        return null
    }

    override fun preprocessOnPaste(
        project: Project,
        file: PsiFile,
        editor: Editor,
        text: String,
        rawText: RawText?
    ): String {
        val fileName = file.name
        if (!isDotenvxSupportFile(fileName)) return text
        val publicKey = findPublicKey(file) ?: return text
        val keyName = DotenvxFileUtils.getKeyNameOnLine(fileName, editor)
        if (keyName.isEmpty() || !DotenvxEncryptor.isSensitiveKey(keyName.lowercase())) return text
        if (text.trim().startsWith("encrypted:")) return text
        return try {
            DotenvxEncryptor.encrypt(text, publicKey)
        } catch (_: Exception) {
            text
        }
    }

    fun isDotenvxSupportFile(fileName: String): Boolean {
        return fileName.endsWith(".properties") || fileName.endsWith(".yaml") || fileName.endsWith(".yml")
                || fileName == ".env" || fileName.startsWith(".env.") || fileName.endsWith(".xml")
    }

}