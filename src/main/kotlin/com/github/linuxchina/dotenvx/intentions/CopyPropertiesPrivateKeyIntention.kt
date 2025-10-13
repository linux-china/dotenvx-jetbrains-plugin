package com.github.linuxchina.dotenvx.intentions

import com.github.linuxchina.dotenvx.DotenvxEncryptor
import com.github.linuxchina.dotenvx.DotenvxEncryptor.findPublicKey
import com.github.linuxchina.dotenvx.DotenvxEncryptor.getProfileName
import com.github.linuxchina.dotenvx.VARIABLE_ICON
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.lang.properties.psi.Property
import com.intellij.lang.properties.psi.impl.PropertyValueImpl
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.util.Iconable
import com.intellij.psi.PsiElement
import javax.swing.Icon

/**
 * Intention: copy the private key for dotenv.public.key in properties file.
 */
class CopyPropertiesPrivateKeyIntention : PsiElementBaseIntentionAction(), DumbAware, Iconable {

    override fun getFamilyName(): String = "Dotenvx"

    override fun getText(): String = "Copy public key"


    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
        if (element !is PropertyValueImpl) return false
        val psiFile = element.containingFile ?: return false
        val fileName = psiFile.name
        if (!fileName.endsWith(".properties")) return false
        val property = element.parent as? Property ?: return false
        if (property.key?.contains("dotenv.public.key") == false) {
            return false
        }
        val publicKey = findPublicKey(psiFile) ?: return false
        val profileName: String? = getProfileName(fileName)
        val projectDir = project.guessProjectDir()?.path!!
        val privateKey = DotenvxEncryptor.getDotenvxPrivateKey(projectDir, profileName, publicKey) ?: return false
        return privateKey.isNotEmpty()
    }

    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        val property = element.parent as? Property ?: return
        val psiFile = property.containingFile ?: return
        val fileName = psiFile.name
        val publicKey: String = findPublicKey(psiFile) ?: return
        val profileName: String? = getProfileName(fileName)
        val projectDir = project.guessProjectDir()?.path!!
        val privateKey = DotenvxEncryptor.getDotenvxPrivateKey(projectDir, profileName, publicKey)!!
        CopyPasteManager.copyTextToClipboard(privateKey)
    }

    override fun startInWriteAction(): Boolean = false

    override fun getIcon(p0: Int): Icon {
        return VARIABLE_ICON
    }
}
