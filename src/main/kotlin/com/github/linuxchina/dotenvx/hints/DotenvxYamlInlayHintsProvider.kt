package com.github.linuxchina.dotenvx.hints

import com.github.linuxchina.dotenvx.DotenvxEncryptor
import com.intellij.codeInsight.hints.declarative.*
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.guessProjectDir
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.endOffset
import org.jetbrains.yaml.psi.YAMLFile
import org.jetbrains.yaml.psi.YAMLScalar


/**
 * Inlay hints provider for YAML files that shows decrypted values for encrypted variables.
 *
 * This provider intentionally avoids binding to specific PSI classes of ru.adelf.idea.dotenv
 * to keep compilation stable across plugin versions. It works on the document text per line.
 */
class DotenvxYamlInlayHintsProvider : InlayHintsProvider, DumbAware {

    override fun createCollector(
        file: PsiFile,
        editor: Editor
    ): InlayHintsCollector? {
        val fileName = file.name.lowercase()
        if (file !is YAMLFile) return null
        if (file.text.contains("encrypted:") || file.text.contains("dotenv.public.key")) {
            val publicKey: String = DotenvxEncryptor.findPublicKey(file) ?: return null
            val profileName: String? = DotenvxEncryptor.getProfileName(fileName)
            val projectDir = file.project.guessProjectDir()?.path!!
            val privateKey = DotenvxEncryptor.getDotenvxPrivateKey(projectDir, profileName, publicKey)
            return DotenvxYamlCollector(publicKey, privateKey)
        }
        return null
    }
}


class DotenvxYamlCollector(val publicKey: String, val privateKey: String?) : SharedBypassCollector {

    override fun collectFromElement(element: PsiElement, sink: InlayTreeSink) {
        if (element is YAMLScalar && element.text != null) {
            val textValue = element.text
            if ((textValue.startsWith("encrypted:") || textValue.startsWith("\"encrypted:")) && privateKey != null) {
                try {
                    DotenvxEncryptor.decrypt(textValue.trim('"'), privateKey)
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
                        text("private key not found!")
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
