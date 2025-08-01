package com.pmj.securevault

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

internal class AndroidSecureVault(
    private val context: Context
) : SecureVault {

    private val keyAlias = "SecureVaultKey"
    private val sharedPrefs: SharedPreferences
        get() = context.getSharedPreferences("secure_vault_prefs", Context.MODE_PRIVATE)

    private val cipherTransformation = "AES/GCM/NoPadding"
    private val androidKeyStore = "AndroidKeyStore"

    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(androidKeyStore).apply { load(null) }
        if (keyStore.containsAlias(keyAlias)) {
            return keyStore.getKey(keyAlias, null) as SecretKey
        }

        val keyGen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, androidKeyStore)
        val spec = KeyGenParameterSpec.Builder(
            keyAlias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .build()

        keyGen.init(spec)
        return keyGen.generateKey()
    }

    override suspend fun put(key: String, value: String, options: VaultOptions) {
        val cipher = Cipher.getInstance(cipherTransformation)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())
        val iv = cipher.iv
        val ciphertext = cipher.doFinal(value.toByteArray(Charsets.UTF_8))

        sharedPrefs.edit()
            .putString("vault_$key", Base64.encodeToString(ciphertext, Base64.DEFAULT))
            .putString("vault_iv_$key", Base64.encodeToString(iv, Base64.DEFAULT))
            .apply()
    }

    override suspend fun get(key: String, options: VaultOptions): String? {
        val encodedCiphertext = sharedPrefs.getString("vault_$key", null) ?: return null
        val encodedIv = sharedPrefs.getString("vault_iv_$key", null) ?: return null

        val ciphertext = Base64.decode(encodedCiphertext, Base64.DEFAULT)
        val iv = Base64.decode(encodedIv, Base64.DEFAULT)

        val cipher = Cipher.getInstance(cipherTransformation)
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateSecretKey(), spec)

        return cipher.doFinal(ciphertext).toString(Charsets.UTF_8)
    }

    override suspend fun delete(key: String) {
        sharedPrefs.edit()
            .remove("vault_$key")
            .remove("vault_iv_$key")
            .apply()
    }

    override suspend fun exists(key: String): Boolean {
        return sharedPrefs.contains("vault_$key")
    }
}

actual object VaultProvider {
    actual fun getInstance(): SecureVault {
        return AndroidSecureVault(MyApp.appContext)
    }
}