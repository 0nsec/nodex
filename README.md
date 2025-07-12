# Nodex - Clean Android App

A clean, simplified Android messaging application with modern architecture.

## Project Structure

This project has been restructured for clarity and maintainability:

```
nodex/
├── nodex-android/          # Main Android application module
├── nodex-api/             # API definitions and interfaces  
├── nodex-core/            # Core business logic
├── build.gradle           # Root build configuration
├── settings.gradle        # Project structure definition
└── gradle/               # Gradle wrapper files
```

## Key Features

- **nodex-android**: Contains the main Android app with Activities, Fragments, and UI
  - Package: `org.nodex.android`
  - Main entry point: `SplashScreenActivity` 
  - Main navigation: `NavDrawerActivity`
  - Secure messaging interface
  
- **nodex-api**: API layer defining interfaces and data structures
  - Package: `org.nodex.api`
  
- **nodex-core**: Core functionality and business logic
  - Package: `org.nodex.core`

## Code Quality Improvements

- All comments have been removed for cleaner code
- Package structure renamed from `org.briarproject.briar` to `org.nodex`
- Simplified dependencies and removed unused modules
- Clean build configuration without comments
- **Fixed package structure:** Corrected nested `org.nodex.nodex.api` to proper `org.nodex.api`
- **Java 17 compatibility:** Updated all modules to use Java 17 for Android Gradle Plugin 8.2.0 compatibility
- **Added missing core API classes:** Created foundational classes for database, sync, crypto, and event handling
- **Resolved duplicate class errors:** Fixed package structure issues that caused compilation failures

## Building the App

```bash
# Build the app
./gradlew build

# Install debug version
./gradlew installDebug
```

## Development Setup

1. **Prerequisites:**
   - Android Studio Arctic Fox or later
   - Android SDK (API level 21 or higher)
   - Java 17 (configured automatically via gradle.properties)

2. **Setup Steps:**
   - Clone the repository
   - Open the project in Android Studio
   - Let Android Studio automatically configure the Android SDK path in `local.properties`
   - Sync Gradle files
   - Run the app on a device or emulator

3. **Project Configuration:**
   - The project uses Java 17 for compilation (configured in gradle.properties)
   - Android Gradle Plugin 8.2.0
   - Minimum SDK: API 21 (Android 5.0)
   - Target SDK: API 34 (Android 14)

4. **Build Commands:**
   ```bash
   # Build the app
   ./gradlew build

   # Install debug version
   ./gradlew installDebug
   ```

**Note:** The Android SDK path will be automatically configured when you open the project in Android Studio. If building from command line, ensure `ANDROID_HOME` environment variable is set or update `local.properties` with your SDK path.

The app uses a modern Android architecture with dependency injection (Dagger), ViewModels, and clean separation of concerns.