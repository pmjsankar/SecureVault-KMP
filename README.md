# SecureVault 🔐

A secure, cross-platform vault application built with Kotlin Multiplatform and Compose Multiplatform. SecureVault
provides encrypted storage for sensitive data across Android, iOS, and Desktop platforms.

## Features

- **🔒 End-to-End Encryption**: All data is encrypted using AES-256-GCM encryption
- **📱 Cross-Platform**: Works on Android, iOS, and Desktop (JVM)
- **🛡️ Platform-Specific Security**:
    - **Android**: Uses Android Keystore for secure key storage
    - **iOS**: Uses iOS Keychain for secure data storage
    - **Desktop**: Uses PKCS12 keystore with encrypted file storage
- **⚡ Real-time**: Coroutine-based async operations

## Architecture

```
SecureVault/
├── composeApp/          # Main application with Compose UI
├── shared/             # Shared business logic and platform implementations
│   ├── commonMain/     # Common interfaces and data classes
│   ├── androidMain/    # Android-specific implementation
│   ├── iosMain/        # iOS-specific implementation
│   └── jvmMain/        # Desktop-specific implementation
└── iosApp/             # iOS app entry point
```

## Security Implementation

### Android

- Uses Android Keystore for secure key generation and storage
- AES-256-GCM encryption with random IVs
- Encrypted data stored in SharedPreferences

### iOS

- Uses iOS Keychain for secure data storage
- Data accessible only when device is unlocked
- Device-only access (no iCloud sync)

### Desktop (JVM)

- PKCS12 keystore for key management
- AES-256-GCM encryption
- Encrypted data stored in local file system
- Configurable password protection

## Getting Started

### Prerequisites

- Android Studio Hedgehog or later
- JDK 17
- Kotlin 2.2.0+
- For iOS development: Xcode 15+ and macOS

## Usage

1. **Save Data**: Enter a key and value, then tap "Save to Vault"
2. **Retrieve Data**: Enter a key and tap "Get from Vault"
3. **Security**: All data is automatically encrypted before storage

## Security Considerations

### ✅ Implemented Security Features

- AES-256-GCM encryption for all data
- Platform-specific secure storage
- Random IVs for each encryption operation
- Secure key generation and storage

### ⚠️ Security Notes

- **Desktop Implementation**: Uses hardcoded password "changeit" - should be configurable
- **No Biometric Auth**: Biometric authentication is planned but not yet implemented
- **No Key Rotation**: Keys are persistent and not rotated
- **Local Storage Only**: No cloud sync or backup functionality

### 🔧 Recommended Security Improvements

1. Make desktop keystore password configurable via environment variables
2. Implement biometric authentication
3. Add key rotation capabilities
4. Add secure backup/restore functionality
5. Implement secure deletion (overwrite data before deletion)
6. Add audit logging for security events

## Development

### Project Structure

- `composeApp/src/commonMain/`: Shared UI components
- `shared/src/commonMain/`: Core vault interface
- `shared/src/androidMain/`: Android-specific implementation
- `shared/src/iosMain/`: iOS-specific implementation
- `shared/src/jvmMain/`: Desktop-specific implementation

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

**⚠️ Important**: This is a demonstration project. For production use, please ensure all security recommendations are
implemented and thoroughly tested.