package com.pmj.securevault

import java.io.File
import java.security.*
import javax.crypto.*
import javax.crypto.spec.GCMParameterSpec

actual object VaultProvider {
    actual fun getInstance(): SecureVault = JvmSecureVault()
}

internal class JvmSecureVault : SecureVault {
    private val keyAlias = "VaultKey"
    private val keystorePath = "vault.keystore"
    private val vaultFile = File("vault.data")
    private val password = "changeit".toCharArray() // todo or load from env/config

    private val keystore: KeyStore = KeyStore.getInstance("PKCS12").apply {
        if (File(keystorePath).exists()) {
            File(keystorePath).inputStream().use { load(it, password) }
        } else {
            load(null, password)
            val key = generateSecretKey()
            val entry = KeyStore.SecretKeyEntry(key)
            val protParam = KeyStore.PasswordProtection(password)
            setEntry(keyAlias, entry, protParam)
            File(keystorePath).outputStream().use { store(it, password) }
        }
    }

    private fun getSecretKey(): SecretKey {
        val entry = keystore.getEntry(keyAlias, KeyStore.PasswordProtection(password))
        return (entry as KeyStore.SecretKeyEntry).secretKey
    }

    override suspend fun put(key: String, value: String, options: VaultOptions) {
        val map = readVault().toMutableMap()
        map[key] = value
        saveVault(map)
    }

    override suspend fun get(key: String, options: VaultOptions): String? {
        return readVault()[key]
    }

    override suspend fun delete(key: String) {
        val map = readVault().toMutableMap()
        map.remove(key)
        saveVault(map)
    }

    override suspend fun exists(key: String): Boolean {
        return readVault().containsKey(key)
    }

    private fun readVault(): Map<String, String> {
        if (!vaultFile.exists()) return emptyMap()
        val encrypted = vaultFile.readBytes()
        val decrypted = decrypt(encrypted)
        return decrypted.decodeToString().split("\n")
            .mapNotNull { it.split("=", limit = 2).takeIf { it.size == 2 } }
            .associate { it[0] to it[1] }
    }

    private fun saveVault(data: Map<String, String>) {
        val plain = data.entries.joinToString("\n") { "${it.key}=${it.value}" }
        val encrypted = encrypt(plain.encodeToByteArray())
        vaultFile.writeBytes(encrypted)
    }

    private fun encrypt(data: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val iv = SecureRandom().generateSeed(12)
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(), spec)
        val encrypted = cipher.doFinal(data)
        return iv + encrypted
    }

    private fun decrypt(data: ByteArray): ByteArray {
        val iv = data.sliceArray(0 until 12)
        val encrypted = data.sliceArray(12 until data.size)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)
        return cipher.doFinal(encrypted)
    }

    private fun generateSecretKey(): SecretKey {
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(256)
        return keyGen.generateKey()
    }
}
