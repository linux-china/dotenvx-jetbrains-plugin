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
import com.github.linuxchina.dotenvx.DotenvxEncryptor

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
        var appName = "app_name"
        var groupName = "group_name"
        val publicKeyName = DotenvxEncryptor.getPublicKeyName(fileName)
        if (fileName.endsWith(".properties")) {
            psiFile.text.lines().forEach { line ->
                if (line.startsWith("spring.application.name")) {
                    appName = line.substringAfter("=").trim().trim('"', '\'')
                } else if (line.startsWith("spring.application.group")) {
                    groupName = line.substringAfter("=").trim().trim('"', '\'')
                }
            }
        }
        val uuid = Generators.timeBasedEpochGenerator().generate().toString()
        val header = if (fileName.endsWith(".properties")) {
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
