package com.github.linuxchina.dotenvx.hints

import com.github.linuxchina.dotenvx.DotenvxEncryptor
import com.intellij.codeInsight.hints.declarative.*
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.guessProjectDir
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.endOffset
import io.github.cdimascio.ecies.Ecies
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
        var publicKey: String? = null
        file.text.lines().forEach { line ->
            if (line.startsWith("DOTENV_PUBLIC_KEY")) {
                publicKey = line.substringAfter('=').trim().trim('"', '\'')
            }
        }
        if (file.text.contains("encrypted:")) {
            val profileName: String? = if (fileName.startsWith(".env.")) {
                fileName.substringAfter(".env.")
            } else {
                null
            }
            val projectDir = file.project.guessProjectDir()?.path!!
            val privateKey = DotenvxEncryptor.getDotenvxPrivateKey(projectDir, profileName, publicKey)
            return DotenvxEnvCollector(privateKey)
        }
        return null
    }


}

class DotenvxEnvCollector(val privateKey: String?) :
    SharedBypassCollector {

    override fun collectFromElement(element: PsiElement, sink: InlayTreeSink) {
        if (element is DotEnvValue) {
            if (element.text.contains("encrypted:")) {
                if (privateKey.isNullOrEmpty()) {
                    sink.addPresentation(
                        InlineInlayPosition(element.endOffset, false), hintFormat = HintFormat.default,
                    ) {
                        text("private key not found!")
                    }
                    return
                }
                try {
                    Ecies.decrypt(privateKey, element.text!!.substringAfter("encrypted:"))
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
            }
        }
    }
}
