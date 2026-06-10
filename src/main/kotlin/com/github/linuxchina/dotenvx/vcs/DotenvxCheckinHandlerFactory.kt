package com.github.linuxchina.dotenvx.vcs

import com.github.linuxchina.dotenvx.DotenvxEncryptor
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vcs.CheckinProjectPanel
import com.intellij.openapi.vcs.changes.CommitContext
import com.intellij.openapi.vcs.checkin.CheckinHandler
import com.intellij.openapi.vcs.checkin.CheckinHandlerFactory
import com.intellij.openapi.vfs.VirtualFile

/**
 * Registers a commit guard that warns when an .env/.properties file with a dotenvx public key still
 * contains sensitive values in plain text, so secrets are not accidentally committed unencrypted.
 */
class DotenvxCheckinHandlerFactory : CheckinHandlerFactory() {
    override fun createHandler(panel: CheckinProjectPanel, commitContext: CommitContext): CheckinHandler {
        return DotenvxCheckinHandler(panel)
    }
}

class DotenvxCheckinHandler(private val panel: CheckinProjectPanel) : CheckinHandler() {

    override fun beforeCheckin(): ReturnResult {
        val problems = LinkedHashMap<String, List<String>>()
        for (file in panel.virtualFiles) {
            if (!isDotenvxFile(file)) continue
            val content = readContent(file) ?: continue
            if (!(content.contains("DOTENV_PUBLIC_KEY") || content.contains("dotenv.public.key"))) continue
            val keys = DotenvxEncryptor.findUnencryptedSensitiveKeys(content)
            if (keys.isNotEmpty()) {
                problems[file.name] = keys
            }
        }
        if (problems.isEmpty()) return ReturnResult.COMMIT

        val details = problems.entries.joinToString("\n") { (name, keys) ->
            "  $name: ${keys.joinToString(", ")}"
        }
        val answer = Messages.showYesNoDialog(
            panel.project,
            "The following files contain sensitive values that are NOT encrypted:\n\n$details\n\n" +
                    "Committing them would expose secrets in plain text. Commit anyway?",
            "Dotenvx: Unencrypted Sensitive Values",
            "Commit Anyway",
            "Cancel",
            Messages.getWarningIcon()
        )
        return if (answer == Messages.YES) ReturnResult.COMMIT else ReturnResult.CANCEL
    }

    private fun isDotenvxFile(file: VirtualFile): Boolean {
        if (file.isDirectory) return false
        val name = file.name.lowercase()
        return name == ".env" || name.startsWith(".env.") || name.endsWith(".properties")
    }

    private fun readContent(file: VirtualFile): String? {
        FileDocumentManager.getInstance().getDocument(file)?.let { return it.text }
        return try {
            String(file.contentsToByteArray(), file.charset)
        } catch (_: Exception) {
            null
        }
    }
}
