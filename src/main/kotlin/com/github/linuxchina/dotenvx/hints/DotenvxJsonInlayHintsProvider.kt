package com.github.linuxchina.dotenvx.hints

import com.github.linuxchina.dotenvx.DotenvxEncryptor
import com.intellij.codeInsight.hints.declarative.*
import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonLiteral
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.guessProjectDir
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.endOffset


/**
 * Inlay hints provider for json files that shows decrypted values for encrypted variables.
 *
 * This provider intentionally avoids binding to specific PSI classes of ru.adelf.idea.dotenv
 * to keep compilation stable across plugin versions. It works on the document text per line.
 */
class DotenvxJsonInlayHintsProvider : InlayHintsProvider, DumbAware {

    override fun createCollector(
        file: PsiFile,
        editor: Editor
    ): InlayHintsCollector? {
        val fileName = file.name.lowercase()
        if (file !is JsonFile) return null
        if (file.text.contains("encrypted:")) {
            val publicKey: String = DotenvxEncryptor.findPublicKey(file) ?: return null
            val profileName: String? = DotenvxEncryptor.getProfileName(fileName)
            val projectDir = file.project.guessProjectDir()?.path!!
            val privateKey = DotenvxEncryptor.getDotenvxPrivateKey(projectDir, profileName, publicKey)
            return DotenvxJsonCollector(publicKey, privateKey)
        }
        return null
    }
}


class DotenvxJsonCollector(val publicKey: String, val privateKey: String?) : SharedBypassCollector {

    override fun collectFromElement(element: PsiElement, sink: InlayTreeSink) {
        if (element is JsonStringLiteral && element.text != null) {
            val textValue = element.text.trim('"')
            if ((textValue.startsWith("encrypted:")) && privateKey != null) {
                try {
                    DotenvxEncryptor.decrypt(textValue, privateKey)
                        .let { decryptedValue ->
                            if (decryptedValue.isNotEmpty()) {
                                sink.addPresentation(
                                    InlineInlayPosition(element.endOffset, false), hintFormat = HintFormat.default,
                                ) {
                                    text(decryptedValue)
                                }
                            }
                        }
                } catch (_: Exception) {

                }
            } else if (textValue == publicKey) {
                if (privateKey.isNullOrEmpty()) {
                    sink.addPresentation(
                        InlineInlayPosition(element.endOffset, false), hintFormat = HintFormat.default,
                    ) {
                        text("Private key not found!")
                    }
                } else {
                    sink.addPresentation(
                        InlineInlayPosition(element.endOffset, false), hintFormat = HintFormat.default,
                    ) {
                        text("Private key: ${privateKey.take(6)}...")
                    }
                }
            }
        }
    }
}
