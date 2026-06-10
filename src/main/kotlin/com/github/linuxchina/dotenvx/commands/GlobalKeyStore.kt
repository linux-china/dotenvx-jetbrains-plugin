package com.github.linuxchina.dotenvx.commands

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.uuid.Generators
import com.github.linuxchina.dotenvx.DotenvxEncryptor
import com.intellij.openapi.ui.Messages
import io.github.cdimascio.ecies.Ecies
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.attribute.PosixFilePermissions
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter


object GlobalKeyStore {
    var objectMapper = ObjectMapper()

    fun generateKeyPair(): KeyPair {
        val ecKeyPair = Ecies.generateEcKeyPair()
        var privateKeyHex = ecKeyPair.privateHex
        if (privateKeyHex.length > 64) {
            // If the private key is longer than 64 characters, we truncate it to 64 characters
            privateKeyHex = privateKeyHex.substring(privateKeyHex.length - 64)
        }
        return KeyPair(
            publicKey = ecKeyPair.getPublicHex(true),
            privateKey = privateKeyHex
        )
    }

    fun getGlobalKeyPairs(): Map<String, Any> {
        // read $HOME/.dotenvx/.env.keys.json and return the keyPairs
        val keysFile = File(System.getProperty("user.home"), ".dotenvx/.env.keys.json")
        if (!keysFile.exists()) {
            return emptyMap()
        }
        try {
            var keyStore: Map<String, Any> =
                objectMapper.readValue(
                    keysFile,
                    Map::class.java
                ) as Map<String, Any>
            if (keyStore.containsKey("version") && keyStore.containsKey("keys")) {
                keyStore = keyStore["keys"] as Map<String, Any>
            }
            return keyStore
        } catch (_: Exception) {
        }
        return emptyMap()
    }

    fun saveKeyPair(keyPair: KeyPair) {
        // read $HOME/.dotenvx/.env.keys.json and append the keyPair
        var globalStore = getGlobalKeyPairs().toMutableMap()
        var keyStore = mutableMapOf<String, Any>()
        if (!globalStore.containsKey("version")) {
            keyStore.putAll(globalStore)
            globalStore = mutableMapOf()
            globalStore["version"] = "0.1.0"
            val metadata = mutableMapOf("uuid" to Generators.timeBasedEpochGenerator().generate().toString())
            globalStore["metadata"] = metadata
            globalStore["keys"] = keyStore
        } else {
            keyStore = globalStore["keys"] as MutableMap<String, Any>
        }
        if (!keyStore.containsKey(keyPair.publicKey)) {
            val now = ZonedDateTime.now()
            val timestampText = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX"))
            keyStore[keyPair.publicKey] = mapOf(
                "public_key" to keyPair.publicKey,
                "private_key" to keyPair.privateKey,
                "profile" to keyPair.profile,
                "timestamp" to timestampText,
                "path" to keyPair.path
            )
        }
        val keysFilePath = Paths.get(System.getProperty("user.home"), ".dotenvx", ".env.keys.json")
        val keysFile = keysFilePath.toFile()
        if (!keysFile.parentFile.exists()) {
            keysFile.parentFile.mkdirs()
        }
        try {
            // restrict the .dotenvx directory to the owner only (700)
            setPosixPermissions(keysFile.parentFile.toPath(), "rwx------")
            // create the keys file with 600 permissions before writing any secret,
            // so the private keys are never world-readable, not even briefly
            if (!keysFile.exists()) {
                keysFile.createNewFile()
            }
            setPosixPermissions(keysFilePath, "rw-------")
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(keysFile, globalStore)
            DotenvxEncryptor.cacheKeyPair(keyPair.publicKey, keyPair.privateKey)
        } catch (e: Exception) {
            Messages.showErrorDialog(
                "Failed to save the key pair to $keysFilePath: ${e.message}",
                "Dotenvx Key Store Error"
            )
        }
    }

    /**
     * Apply POSIX permissions (e.g. "rw-------") to the given path.
     * No-op on file systems that do not support POSIX permissions (e.g. Windows).
     */
    private fun setPosixPermissions(path: Path, permissions: String) {
        try {
            if (path.fileSystem.supportedFileAttributeViews().contains("posix")) {
                Files.setPosixFilePermissions(path, PosixFilePermissions.fromString(permissions))
            }
        } catch (_: Exception) {
        }
    }
}

class KeyPair(var publicKey: String, var privateKey: String) {
    var profile: String? = null
    var path: String? = null
}