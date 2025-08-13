package com.github.linuxchina.dotenvx.actions

import com.fasterxml.uuid.Generators
import com.github.linuxchina.dotenvx.commands.GlobalKeyStore
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile

/**
 * Insert public at the head of .env or .properties file if not present.
 *
 * @author linux_china
 */
class InsertPublicKeyAction : AnAction(), DumbAware {

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        val psiFile = e.getData(CommonDataKeys.PSI_FILE)
        val visible = psiFile?.let { isEnvOrProperties(it) } ?: false
        val enabled = visible && !containsPublicKey(psiFile)
        e.presentation.isVisible = visible
        e.presentation.isEnabled = enabled
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val psiFile = e.getData(CommonDataKeys.PSI_FILE) ?: return
        if (!isEnvOrProperties(psiFile)) return
        if (containsPublicKey(psiFile)) return
        val fileName = psiFile.name
        val keyPair = GlobalKeyStore.generateKeyPair()
        val appName = "app_name"
        val groupName = "group_name"
        val profileName: String? = if (fileName.startsWith(".env.")) {
            fileName.substringAfter(".env.")
        } else if (fileName.endsWith(".properties") && fileName.contains("-")) {
            fileName.substringBeforeLast(".properties").substringAfterLast("-")
        } else {
            null
        }
        var publicKeyName = if (profileName != null) {
            "DOTENV_PUBLIC_KEY_${profileName.uppercase()}"
        } else {
            "DOTENV_PUBLIC_KEY"
        }
        if (fileName.endsWith(".properties")) {
            publicKeyName = publicKeyName.replace('_', '.').lowercase()
        }
        val uuid = Generators.timeBasedEpochGenerator().generate().toString()
        val header = if (fileName.endsWith("properties")) {
            """# ---
# uuid: $uuid
# name: $appName
# group: $groupName
# ---
${publicKeyName}=${keyPair.publicKey}

# Environment variables. MAKE SURE to ENCRYPT them before committing to source control
"""
        } else {
            """# ---
# uuid: $uuid
# name: $appName
# group: $groupName
# ---
${publicKeyName}=${keyPair.publicKey}

# Environment variables. MAKE SURE to ENCRYPT them before committing to source control
"""
        }
        insertAtHead(project, psiFile, header)
        keyPair.path = psiFile.virtualFile.path
        GlobalKeyStore.saveKeyPair(keyPair)
    }

    private fun isEnvOrProperties(psiFile: PsiFile): Boolean {
        val name = psiFile.name
        return name.endsWith(".properties") || name == ".env" || (name.startsWith(".env.") && name != ".env.keys")
    }

    private fun containsPublicKey(psiFile: PsiFile): Boolean {
        val fileText = psiFile.text
        return fileText.contains("dotenv.public.key") || fileText.contains("DOTENV_PUBLIC_KEY")
    }

    private fun insertAtHead(project: Project, psiFile: PsiFile, textToInsert: String) {
        val document = PsiDocumentManager.getInstance(project).getDocument(psiFile) ?: return
        WriteCommandAction.runWriteCommandAction(project) {
            document.insertString(0, textToInsert)
            PsiDocumentManager.getInstance(project).commitDocument(document)
        }
    }
}
