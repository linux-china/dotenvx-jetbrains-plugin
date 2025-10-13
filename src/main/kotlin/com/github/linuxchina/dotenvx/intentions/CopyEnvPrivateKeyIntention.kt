package com.github.linuxchina.dotenvx.intentions

import com.github.linuxchina.dotenvx.DotenvxEncryptor
import com.github.linuxchina.dotenvx.DotenvxEncryptor.findPublicKey
import com.github.linuxchina.dotenvx.VARIABLE_ICON
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.util.Iconable
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import ru.adelf.idea.dotenv.psi.DotEnvProperty
import ru.adelf.idea.dotenv.psi.DotEnvTypes
import ru.adelf.idea.dotenv.psi.DotEnvValue
import javax.swing.Icon

/**
 * Intention: copy a private key for DOTENV_PUBLIC_KEY in .env files.
 */
class CopyEnvPrivateKeyIntention : PsiElementBaseIntentionAction(), DumbAware, Iconable {

    override fun getFamilyName(): String = "Dotenvx"

    override fun getText(): String = "Copy private key"

    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
        if (element.elementType == DotEnvTypes.VALUE_CHARS && element.parent is DotEnvValue) {
            val psiFile = element.containingFile ?: return false
            val fileName = psiFile.name.lowercase()
            val envValue = element.parent as DotEnvValue
            val envProperty = envValue.parent as? DotEnvProperty ?: return false
            // Do not offer if already encrypted
            if (!envProperty.key.text.contains("DOTENV_PUBLIC_KEY")) return false
            // Require DOTENV_PUBLIC_KEY in file
            val publicKey = findPublicKey(psiFile) ?: return false
            val projectDir = psiFile.project.guessProjectDir()?.path!!
            val profileName: String? = DotenvxEncryptor.getProfileName(fileName)
            val privateKey = DotenvxEncryptor.getDotenvxPrivateKey(projectDir, profileName, publicKey) ?: return false
            return privateKey.isNotEmpty()
        }
        return false
    }

    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        val envValue = element.parent as? DotEnvValue ?: return
        val psiFile = envValue.containingFile ?: return
        val publicKey = findPublicKey(psiFile) ?: return
        val projectDir = psiFile.project.guessProjectDir()?.path!!
        val fileName = psiFile.name.lowercase()
        val profileName: String? = DotenvxEncryptor.getProfileName(fileName)
        val privateKey = DotenvxEncryptor.getDotenvxPrivateKey(projectDir, profileName, publicKey) ?: return
        CopyPasteManager.copyTextToClipboard(privateKey)
    }

    override fun startInWriteAction(): Boolean = false

    override fun getIcon(p0: Int): Icon {
        return VARIABLE_ICON
    }
}
