package com.github.linuxchina.dotenvx.hints

import com.github.linuxchina.dotenvx.DotenvxEncryptor
import com.intellij.codeInsight.hints.declarative.*
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.guessProjectDir
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.endOffset
import com.intellij.psi.util.startOffset
import org.toml.lang.psi.TomlFile
import org.toml.lang.psi.TomlLiteral


/**
 * Inlay hints provider for XML files that shows decrypted values for encrypted variables.
 *
 * This provider intentionally avoids binding to specific PSI classes of ru.adelf.idea.dotenv
 * to keep compilation stable across plugin versions. It works on the document text per line.
 */
class DotenvxTomlInlayHintsProvider : InlayHintsProvider, DumbAware {

    override fun createCollector(
        file: PsiFile,
        editor: Editor
    ): InlayHintsCollector? {
        val fileName = file.name.lowercase()
        if (file !is TomlFile) return null
        if (file.text.contains("encrypted:")) {
            val publicKey: String = DotenvxEncryptor.findPublicKey(file) ?: return null
            val profileName: String? = DotenvxEncryptor.getProfileName(fileName)
            val projectDir = file.project.guessProjectDir()?.path!!
            val privateKey = DotenvxEncryptor.getDotenvxPrivateKey(projectDir, profileName, publicKey)
            return DotenvxTomlCollector(publicKey, privateKey)
        }
        return null
    }
}


class DotenvxTomlCollector(val publicKey: String, val privateKey: String?) : SharedBypassCollector {

    override fun collectFromElement(element: PsiElement, sink: InlayTreeSink) {
        if ((element is TomlLiteral) && element.text != null) {
            val textValue = element.text.trim('"', '\'')
            if ((textValue.startsWith("encrypted:")) && privateKey != null) {
                try {
                    DotenvxEncryptor.decrypt(textValue, privateKey)
                        .let { decryptedValue ->
                            if (decryptedValue.isNotEmpty()) {
                                sink.addPresentation(
                                    InlineInlayPosition(element.endOffset, true),
                                    hintFormat = HintFormat.default,
                                ) {
                                    text(decryptedValue)
                                }
                            }
                        }
                } catch (_: Exception) {

                }
            }
        } else if (element is PsiComment && element.text.contains(publicKey)) {
            val offset = element.startOffset + element.text.indexOf(publicKey) + publicKey.length
            if (privateKey.isNullOrEmpty()) {
                sink.addPresentation(
                    InlineInlayPosition(offset, false), hintFormat = HintFormat.default,
                ) {
                    text("Private key not found!")
                }
            } else {
                sink.addPresentation(
                    InlineInlayPosition(offset, false), hintFormat = HintFormat.default,
                ) {
                    text("Private key: ${privateKey.take(6)}...")
                }
            }
        }
    }
}
