package com.github.linuxchina.dotenvx

import com.github.linuxchina.dotenvx.commands.GlobalKeyStore
import com.intellij.psi.PsiFile
import io.github.cdimascio.dotenv.Dotenv
import io.github.cdimascio.ecies.Ecies
import org.jetbrains.yaml.YAMLUtil
import org.jetbrains.yaml.psi.YAMLFile
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

object DotenvxEncryptor {

    fun findPublicKey(file: PsiFile): String? {
        if (file is YAMLFile) {
            val publicKeyElement = YAMLUtil.getQualifiedKeyInFile(file, "dotenv", "public", "key")
            val text = publicKeyElement?.lastChild?.text
            return if (text?.contains(":") == true) {
                text.substringAfter(":").trim()
            } else {
                text
            }
        } else {
            for (rawLine in file.text.lines()) {
                if (rawLine.startsWith("DOTENV_PUBLIC_KEY") || rawLine.startsWith("dotenv.public.key")) {
                    return rawLine.substringAfter('=').trim().trim('"', '\'')
                }
            }
        }
        return null
    }

    fun getDotenvxPrivateKey(projectDir: String, profileName: String?, publicKeyHex: String?): String? {
        // load the private key from the global store: .env.keys.json
        if (publicKeyHex != null && !publicKeyHex.isEmpty()) {
            val globalStore: Map<String, Any> = GlobalKeyStore.getGlobalKeyPairs()
            if (globalStore.containsKey(publicKeyHex)) {
                val keyPair = globalStore[publicKeyHex]
                if (keyPair is Map<*, *>) {
                    return keyPair["private_key"].toString()
                }
            }
        }
        // load from environment variables
        var privateKeyEnvName = "DOTENV_PRIVATE_KEY"
        if (!profileName.isNullOrEmpty()) {
            privateKeyEnvName = "DOTENV_PRIVATE_KEY_" + profileName.uppercase(Locale.getDefault())
        }
        var privateKey = System.getenv(privateKeyEnvName)
        // load from .env.keys file
        if (privateKey == null || privateKey.isEmpty()) {
            if (Files.exists(Paths.get(projectDir, ".env.keys"))) { // Check in the project's directory
                val keysEnv = Dotenv.configure().directory(projectDir).filename(".env.keys").load()
                privateKey = keysEnv.get(privateKeyEnvName)
            } else if (Files.exists(
                    Paths.get(
                        System.getProperty("user.home"),
                        ".env.keys"
                    )
                )
            ) { // Check in the user's home directory
                val keysEnv =
                    Dotenv.configure().directory(System.getProperty("user.home")).filename(".env.keys").load()
                privateKey = keysEnv.get(privateKeyEnvName)
            }
        }
        return privateKey
    }

    fun getProfileName(fileName: String): String? {
        if (fileName.endsWith(".properties") && fileName.contains("-")) {
            return fileName.substringAfterLast("-").substringBefore(".")
        } else if (fileName.startsWith(".env.")) {
            return fileName.substringAfter(".env.")
        } else if (fileName.endsWith(".yaml") && fileName.contains("-")) {
            return fileName.substringAfterLast("-").substringBefore(".yaml")
        } else if (fileName.endsWith(".yml") && fileName.contains("-")) {
            return fileName.substringAfterLast("-").substringBefore(".yml")
        }
        return null
    }

    fun getPublicKeyName(fileName: String): String {
        val profileName: String? = if (fileName.startsWith(".env.")) {
            fileName.substringAfter(".env.")
        } else if (fileName.endsWith(".properties") && fileName.contains("-")) {
            fileName.substringBeforeLast(".properties").substringAfterLast("-")
        } else if (fileName.endsWith(".yaml") && fileName.contains("-")) {
            fileName.substringBeforeLast(".yaml").substringAfterLast("-")
        } else if (fileName.endsWith(".yml") && fileName.contains("-")) {
            fileName.substringBeforeLast(".yml").substringAfterLast("-")
        } else {
            null
        }
        var publicKeyName = if (profileName != null) {
            "DOTENV_PUBLIC_KEY_${profileName.uppercase()}"
        } else {
            "DOTENV_PUBLIC_KEY"
        }
        if (fileName.endsWith(".properties") || fileName.endsWith(".yaml") || fileName.endsWith(".yml")) {
            publicKeyName = publicKeyName.replace('_', '.').lowercase()
        }
        return publicKeyName
    }

    fun encrypt(text: String, publicKey: String): String {
        Ecies.encrypt(publicKey, text).let { encrypted ->
            return "encrypted:${encrypted}"
        }
    }

    fun decrypt(cipherText: String, privateKey: String): String {
        val cleanPrivateKey = if (privateKey.length > 64) {
            privateKey.substring(privateKey.length - 64)
        } else {
            privateKey
        }
        return Ecies.decrypt(cleanPrivateKey, cipherText.substringAfter("encrypted:"))
    }
}