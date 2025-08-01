package com.pmj.securevault

import kotlinx.cinterop.*
import platform.Foundation.*
import platform.Security.*
import platform.darwin.*

actual object VaultProvider {
    actual fun getInstance(): SecureVault = IOSSecureVault()
}

internal class IOSSecureVault : SecureVault {

    override suspend fun put(key: String, value: String, options: VaultOptions) {
        delete(key) // Remove if already exists

        val encodedValue = value.encodeToByteArray().toNSData()
        val keyString = key.toNSString()

        val attributes = mapOf(
            kSecClass to kSecClassGenericPassword,
            kSecAttrAccount to keyString,
            kSecValueData to encodedValue,
            kSecAttrAccessible to kSecAttrAccessibleWhenUnlockedThisDeviceOnly
        )

        val status = SecItemAdd(attributes.toCFDictionary(), null)
        if (status != errSecSuccess) {
            throw Error("Failed to store key: $key (status=$status)")
        }
    }

    override suspend fun get(key: String, options: VaultOptions): String? {
        val query = mapOf(
            kSecClass to kSecClassGenericPassword,
            kSecAttrAccount to key.toNSString(),
            kSecReturnData to kCFBooleanTrue,
            kSecMatchLimit to kSecMatchLimitOne
        )

        memScoped {
            val result = alloc<CFTypeRefVar>()
            val status = SecItemCopyMatching(query.toCFDictionary(), result.ptr)
            if (status == errSecSuccess) {
                val nsData = result.value as? NSData
                return nsData?.toByteArray()?.decodeToString()
            }
            return null
        }
    }

    override suspend fun delete(key: String) {
        val query = mapOf(
            kSecClass to kSecClassGenericPassword,
            kSecAttrAccount to key.toNSString()
        )
        SecItemDelete(query.toCFDictionary())
    }

    override suspend fun exists(key: String): Boolean {
        val query = mapOf(
            kSecClass to kSecClassGenericPassword,
            kSecAttrAccount to key.toNSString(),
            kSecReturnAttributes to kCFBooleanTrue,
            kSecMatchLimit to kSecMatchLimitOne
        )
        val result = nativeHeap.alloc<CFTypeRefVar>()
        val status = SecItemCopyMatching(query.toCFDictionary(), result.ptr)
        nativeHeap.free(result)
        return status == errSecSuccess
    }
}

// Convert Map<String, *> to CFDictionaryRef
fun Map<Any?, Any?>.toCFDictionary(): CFDictionaryRef =
    CFDictionaryCreate(
        null, keys.toTypedArray().toCValues(), values.toTypedArray().toCValues(),
        size.convert(), null, null
    )!!

fun String.toNSString(): NSString = NSString.create(string = this)
fun ByteArray.toNSData(): NSData = NSData.create(bytes = this.refTo(0), length = this.size.toULong())
