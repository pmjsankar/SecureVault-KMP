package com.pmj.securevault

interface SecureVault {
    suspend fun put(key: String, value: String, options: VaultOptions = VaultOptions())
    suspend fun get(key: String, options: VaultOptions = VaultOptions()): String?
    suspend fun delete(key: String)
    suspend fun exists(key: String): Boolean
}

data class VaultOptions(
    val requireBiometric: Boolean = false,
    val passphrase: String? = null,
    val ttlMillis: Long? = null // Optional expiry
)

expect object VaultProvider {
    fun getInstance(): SecureVault
}
