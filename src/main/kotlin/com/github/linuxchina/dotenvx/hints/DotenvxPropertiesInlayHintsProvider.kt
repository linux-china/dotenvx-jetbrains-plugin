package com.github.linuxchina.dotenvx.hints

import com.github.linuxchina.dotenvx.DotenvxEncryptor
import com.intellij.codeInsight.hints.declarative.*
import com.intellij.lang.properties.psi.Property
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.guessProjectDir
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.endOffset
import io.github.cdimascio.ecies.Ecies
import java.io.StringReader
import java.util.*


/**
 * Inlay hints provider for .env files that shows decrypted values for encrypted variables.
 *
 * This provider intentionally avoids binding to specific PSI classes of ru.adelf.idea.dotenv
 * to keep compilation stable across plugin versions. It works on the document text per line.
 */
class DotenvxPropertiesInlayHintsProvider : InlayHintsProvider, DumbAware {

    override fun createCollector(
        file: PsiFile,
        editor: Editor
    ): InlayHintsCollector? {
        val fileName = file.name.lowercase()
        if (!(fileName.endsWith(".properties"))) {
            return null
        }
        if (file.text.contains("encrypted:")) {
            val properties = Properties().apply {
                load(StringReader(file.text))
            }
            var publicKey: String? = null
            properties.forEach { (key, value) ->
                if (key.toString().startsWith("dotenvx.public.key")) {
                    publicKey = value.toString()
                }
            }
            val profileName: String? = if (fileName.contains("-")) {
                fileName.substringAfterLast("-").substringBefore(".properties")
            } else {
                null
            }
            val projectDir = file.project.guessProjectDir()?.path!!
            val privateKey = DotenvxEncryptor.getDotenvxPrivateKey(projectDir, profileName, publicKey)
            return DotenvxPropertiesCollector(privateKey)
        }
        return null
    }
}


class DotenvxPropertiesCollector(val privateKey: String?) : SharedBypassCollector {

    override fun collectFromElement(element: PsiElement, sink: InlayTreeSink) {
        if (element is Property) {
            if (element.value?.contains("encrypted:") == true) {
                if (privateKey.isNullOrEmpty()) {
                    sink.addPresentation(
                        InlineInlayPosition(element.endOffset, false), hintFormat = HintFormat.default,
                    ) {
                        text("private key not found!")
                    }
                    return
                }
                try {
                    Ecies.decrypt(privateKey, element.value!!.substringAfter("encrypted:"))
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
