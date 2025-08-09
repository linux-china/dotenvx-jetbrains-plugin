package com.github.linuxchina.dotenvx

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.cdimascio.dotenv.Dotenv
import io.github.cdimascio.ecies.Ecies
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

object DotenvxEncryptor {
    private var objectMapper = ObjectMapper()
    fun getDotenvxPrivateKey(projectDir: String, profileName: String?, publicKeyHex: String?): String? {
        // load the private key from the global store: .env.keys.json
        if (publicKeyHex != null && !publicKeyHex.isEmpty()) {
            val globalFileStore = Paths.get(System.getProperty("user.home"), ".dotenvx", ".env.keys.json")
            if (Files.exists(globalFileStore)) {
                try {
                    val globalStore: Map<String, Any> =
                        objectMapper.readValue(
                            globalFileStore.toFile(),
                            Map::class.java as Class<Map<String, Any>>
                        )
                    if (globalStore.containsKey(publicKeyHex)) {
                        val keyPair = globalStore[publicKeyHex]
                        if (keyPair is Map<*, *>) {
                            return keyPair["private_key"].toString()
                        }
                    }
                } catch (ignore: Exception) {
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

    fun encrypt(text: String, publicKey: String): String {
        Ecies.encrypt(publicKey, text).let { encrypted ->
            return "encrypted:${encrypted}"
        }
    }

    fun decrypt(cipherText: String, privateKey: String): String {
        return Ecies.decrypt(privateKey, cipherText.substringAfter("encrypted:"))
    }
}