package com.github.linuxchina.dotenvx.hints

import com.github.linuxchina.dotenvx.DotenvxEncryptor
import com.intellij.codeInsight.hints.declarative.*
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.guessProjectDir
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.endOffset
import ru.adelf.idea.dotenv.psi.DotEnvValue

/**
 * Inlay hints provider for .env files that shows decrypted values for encrypted variables.
 *
 */
class DotenvxEnvInlayHintsProvider : InlayHintsProvider, DumbAware {

    override fun createCollector(
        file: PsiFile,
        editor: Editor
    ): InlayHintsCollector? {
        val fileName = file.name.lowercase()
        if (!(fileName == ".env" || fileName.startsWith(".env."))) {
            return null
        }
        val publicKey: String = DotenvxEncryptor.findPublicKey(file) ?: return null
        if (file.text.contains("encrypted:") || file.text.contains("DOTENV_PUBLIC_KEY")) {
            val profileName: String? = DotenvxEncryptor.getProfileName(fileName)
            val projectDir = file.project.guessProjectDir()?.path!!
            val privateKey = DotenvxEncryptor.getDotenvxPrivateKey(projectDir, profileName, publicKey)
            return DotenvxEnvCollector(publicKey, privateKey)
        }
        return null
    }


}

class DotenvxEnvCollector(val publicKey: String?, val privateKey: String?) :
    SharedBypassCollector {

    override fun collectFromElement(element: PsiElement, sink: InlayTreeSink) {
        if (element is DotEnvValue) {
            val text = element.text
            if (text.contains("encrypted:")) {
                if (privateKey.isNullOrEmpty()) {
                    sink.addPresentation(
                        InlineInlayPosition(element.endOffset, false), hintFormat = HintFormat.default,
                    ) {
                        text("private key not found!")
                    }
                    return
                }
                try {
                    DotenvxEncryptor.decrypt(element.text, privateKey)
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
            } else if (publicKey != null && text.contains(publicKey)) {
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
