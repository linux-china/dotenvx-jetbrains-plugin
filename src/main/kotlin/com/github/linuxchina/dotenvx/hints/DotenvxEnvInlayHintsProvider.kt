package com.github.linuxchina.dotenvx.hints

import com.intellij.codeInsight.hints.declarative.*
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.endOffset
import io.github.cdimascio.dotenv.Dotenv
import io.github.cdimascio.dotenv.DotenvxBuilder
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
        val directory = file.virtualFile?.parent?.path ?: return null
        if (file.text.contains("encrypted:")) {
            val dotenv = DotenvxBuilder().directory(directory).filename(fileName).load()
            return DotenvxEnvCollector(dotenv)
        }
        return null
    }


}

class DotenvxEnvCollector(val dotenv: Dotenv) : SharedBypassCollector {

    override fun collectFromElement(element: PsiElement, sink: InlayTreeSink) {
        if (element is DotEnvValue) {
            if (element.text.contains("encrypted:")) {
                sink.addPresentation(
                    InlineInlayPosition(element.endOffset, false), hasBackground = true,
                ) {
                    val keyElement = element.prevSibling.prevSibling
                    val plainValue = dotenv.get(keyElement.text, "")
                    if (plainValue.isNotEmpty()) {
                        text(plainValue)
                    }
                }
            }
        }
    }
}
