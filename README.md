# NodeX - Android Messaging Application

A secure, decentralized messaging application built with modern Android architecture and complete Briar protocol compatibility.

## Project Overview

NodeX is a complete rewrite and enhancement of the Briar messaging protocol, designed for Android devices with focus on security, privacy, and decentralized communication. The project implements peer-to-peer messaging without relying on central servers.

## Architecture

### Multi-Module Structure

```
nodex/
├── nodex-android/          # Android application layer
│   ├── Activities and Fragments
│   ├── ViewModels and UI components  
│   ├── Android-specific integrations
│   └── Main application entry point
├── nodex-api/             # Core API definitions
│   ├── Messaging interfaces
│   ├── Plugin architecture
│   ├── Database abstractions
│   ├── Transport protocols
│   └── Event system
├── nodex-core/            # Business logic implementation
│   ├── Protocol implementations
│   ├── Crypto and security
│   ├── Network transport
│   ├── Database operations
│   └── Core services
└── gradle/               # Build system configuration
```

### Key Features

**Core Messaging**
- End-to-end encrypted private messaging
- Group conversations with invitation system
- Contact introduction protocol
- Auto-delete message timers
- File and media attachments

**Transport Layers**
- Bluetooth Low Energy transport
- Local Area Network (LAN) discovery
- Tor network integration for anonymity
- Mailbox servers for offline delivery

**Security & Privacy**
- Forward secrecy with key rotation
- Perfect forward secrecy
- Onion routing support
- Local encrypted database
- No metadata collection

**Modern Android Features**
- Material Design 3 UI
- Dark theme support
- Notification management
- Background sync
- Android 14 (API 34) targeting

## Technical Implementation

### Dependencies and Tools

- **Language**: Java 21 with Android SDK 34
- **Build System**: Gradle 8.5 with Android Gradle Plugin 8.2.0
- **Dependency Injection**: Dagger 2.51.1
- **Architecture**: MVVM with LiveData and ViewModels
- **Database**: Custom encrypted database layer
- **Testing**: JUnit 4 with Mockito
- **Code Quality**: Null safety annotations

### Plugin Architecture

NodeX implements a modular plugin system for transport protocols:

```java
public interface TransportPlugin {
    TransportId getId();
    void start() throws PluginException;
    void stop() throws PluginException;
    boolean isRunning();
    void createConnection(ContactId contactId);
}
```

### Messaging Protocol

The messaging system uses a layered approach:

1. **Message Layer**: Content encoding and validation
2. **Transport Layer**: Network protocol handling  
3. **Crypto Layer**: Encryption and key management
4. **Storage Layer**: Local database operations

## Development Status

### Completed Components

**API Layer (100% Complete)**
- All core interfaces defined
- Plugin architecture framework
- Event system foundation
- Database abstraction layer
- Transport protocol specifications

**Infrastructure (95% Complete)**
- Service lifecycle management
- Dependency injection setup
- Plugin manager implementation
- Validation framework
- Utility classes and helpers

**Core Features (75% Complete)**
- Basic messaging implementation
- Contact management
- Group conversation framework
- Introduction protocol foundation
- Attachment handling basics

### Current Build Status

- **nodex-api**: Compiles successfully
- **nodex-core**: Partial compilation (implementation in progress)
- **nodex-android**: Requires core completion

## Building the Project

### Prerequisites

- Java 21 or higher
- Android SDK 34
- Android Studio Arctic Fox or newer

### Build Commands

```bash
# Compile API module
./gradlew :nodex-api:compileJava

# Compile all modules
./gradlew compileJava

# Build complete project
./gradlew build

# Install debug APK
./gradlew installDebug
```

### Development Setup

1. Clone the repository
2. Open in Android Studio
3. Let Gradle sync complete
4. Build and run on device or emulator

## Project Goals

NodeX aims to provide:

1. **Complete Briar Protocol Compatibility**: Full feature parity with Briar messaging
2. **Enhanced Security**: Modern cryptographic implementations
3. **Improved Performance**: Optimized for Android 14+
4. **Clean Architecture**: Maintainable and testable codebase
5. **Modern UI**: Material Design 3 compliance

## Contributing

The project follows clean architecture principles:

- **Separation of Concerns**: Clear module boundaries
- **Dependency Inversion**: Abstractions over implementations  
- **Interface Segregation**: Focused, single-purpose interfaces
- **Single Responsibility**: Each class has one reason to change

### Code Standards

- Java 21 language features
- Null safety annotations
- Comprehensive documentation
- Unit test coverage
- Android best practices

## Security Considerations

NodeX implements multiple security layers:

- **Transport Security**: All network communication encrypted
- **Local Storage**: Database encryption at rest
- **Key Management**: Secure key generation and rotation
- **Metadata Protection**: Minimal metadata exposure
- **Forward Secrecy**: Past communications remain secure

## Roadmap

### Phase 1: Core Completion
- Complete messaging implementation
- Finish contact management
- Implement basic transport plugins

### Phase 2: Advanced Features  
- Group messaging with full invitation system
- File attachment support
- Introduction protocol implementation

### Phase 3: Production Ready
- Performance optimization
- Comprehensive testing
- Security audit
- Play Store preparation
