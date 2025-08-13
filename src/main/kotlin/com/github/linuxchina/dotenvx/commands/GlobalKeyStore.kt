package com.github.linuxchina.dotenvx.commands

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.cdimascio.ecies.Ecies
import java.io.File
import java.nio.file.Paths
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter


object GlobalKeyStore {
    var objectMapper = ObjectMapper()

    fun generateKeyPair(): KeyPair {
        val ecKeyPair = Ecies.generateEcKeyPair()
        return KeyPair(
            publicKey = ecKeyPair.getPublicHex(true),
            privateKey = ecKeyPair.privateHex
        )
    }

    fun getGlobalKeyPairs(): Map<String, Any> {
        // read $HOME/.dotenvx/.env.keys.json and return the keyPairs
        val keysFile = File(System.getProperty("user.home"), ".dotenvx/.env.keys.json")
        if (!keysFile.exists()) {
            return emptyMap()
        }
        try {
            val globalStore: Map<String, Any> =
                objectMapper.readValue(
                    keysFile,
                    Map::class.java as Class<Map<String, Any>>
                )
            return globalStore
        } catch (_: Exception) {
        }
        return emptyMap()
    }

    fun saveKeyPair(keyPair: KeyPair) {
        // read $HOME/.dotenvx/.env.keys.json and append the keyPair
        val globalStore = getGlobalKeyPairs().toMutableMap()
        if (!globalStore.containsKey(keyPair.publicKey)) {
            val now = ZonedDateTime.now()
            val timestampText = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX"))
            globalStore[keyPair.publicKey] = mapOf(
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
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(keysFile, globalStore)
        } catch (e: Exception) {
        }
    }
}

class KeyPair(var publicKey: String, var privateKey: String) {
    var profile: String? = null
    var path: String? = null
}