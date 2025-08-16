package com.github.linuxchina.dotenvx

import com.github.linuxchina.dotenvx.commands.GlobalKeyStore
import io.github.cdimascio.dotenv.Dotenv
import io.github.cdimascio.ecies.Ecies
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

object DotenvxEncryptor {
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
            if (Files.exists(Paths.get(".env.keys"))) { // Check in the current directory
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

    fun getPublicKeyName(fileName: String): String {
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