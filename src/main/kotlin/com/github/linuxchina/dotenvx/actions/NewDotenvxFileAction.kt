package com.github.linuxchina.dotenvx.actions

import com.fasterxml.uuid.Generators
import com.github.linuxchina.dotenvx.DotenvxEncryptor
import com.github.linuxchina.dotenvx.commands.GlobalKeyStore
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.isFile

/**
 * Create a new dotenvx-related file and insert a public key line based on the file type.
 * - .properties -> dotenv.public.key=xxx
 * - .env or .env.* -> DOTENV_PUBLIC_KEY=xxx
 *
 * @author linux_china
 */
class NewDotenvxFileAction : AnAction(), DumbAware {

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        val targetDir = e.getData(LangDataKeys.PROJECT_FILE_DIRECTORY)
        e.presentation.isEnabledAndVisible = targetDir != null
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        var targetDir = e.getData(LangDataKeys.VIRTUAL_FILE) ?: return
        if (targetDir.isFile) {
            targetDir = targetDir.parent
        }

        val fileName = Messages.showInputDialog(
            project,
            "Enter file name (e.g., .env, .env.test, application.properties etc.):",
            "New Dotenvx File",
            null
        )?.trim()?.takeIf { it.isNotEmpty() } ?: return

        val existing = targetDir.findChild(fileName)
        if (existing != null) {
            Messages.showErrorDialog(
                project,
                "File '$fileName' already exists in ${targetDir.path}",
                "Cannot Create File"
            )
            return
        }

        val keyPair = GlobalKeyStore.generateKeyPair()
        val publicKeyName = DotenvxEncryptor.getPublicKeyName(fileName)
        val uuid = Generators.timeBasedEpochGenerator().generate().toString()
        val header = if (fileName.endsWith(".properties")) {
            """# ---
# uuid: $uuid
# name: app_name
# group: group_name
# ---
${publicKeyName}=${keyPair.publicKey}

# Environment variables. MAKE SURE to ENCRYPT them before committing to source control
"""
        } else if (fileName.endsWith(".yaml") || fileName.endsWith(".yml")) {
            val profileName = DotenvxEncryptor.getProfileName(fileName)
            val publicKeyText = if (profileName == null) {
                """dotenv:
  public:
    key: ${keyPair.publicKey}"""
            } else {
                """dotenv:
  public:
    key:
      ${profileName}: ${keyPair.publicKey}"""
            }
            """# ---
# uuid: $uuid
# name: app_name
# group: group_name
# ---
${publicKeyText.trim()}

"""
        } else if (fileName.endsWith(".xml")) {
            """<?xml version="1.0" encoding="UTF-8"?>
<!-- uuid=$uuid -->
<!-- dotenv.public.key=${keyPair.publicKey} -->
<root>
</root>
"""
        } else {
            """# ---
# uuid: $uuid
# name: app_name
# group: group_name
# ---
${publicKeyName}=${keyPair.publicKey}

# Environment variables. MAKE SURE to ENCRYPT them before committing to source control
"""
        }

        // Create the new file with the header
        WriteCommandAction.runWriteCommandAction(project) {
            val vf = targetDir.createChildData(this, fileName)
            VfsUtil.saveText(vf, header)
        }

        // Save key pair to global keystore for future use
        try {
            keyPair.path = targetDir.path + "/" + fileName
            GlobalKeyStore.saveKeyPair(keyPair)
        } catch (_: Exception) {
            // ignore persistence errors
        }

        // Open the created file in the editor and move the caret to the end for immediate input
        val createdFile = targetDir.findChild(fileName)
        if (createdFile != null) {
            val document = FileDocumentManager.getInstance().getDocument(createdFile)
            if (document != null) {
                val offset = document.textLength
                OpenFileDescriptor(project, createdFile, offset).navigate(true)
            } else {
                FileEditorManager.getInstance(project).openFile(createdFile, true)
            }
        }
    }
}
