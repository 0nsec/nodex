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

## Building the App

```bash
# Build the app
./gradlew build

# Install debug version
./gradlew installDebug
```

## Development Setup

1. Open the project in Android Studio
2. Sync Gradle files
3. Run the app on a device or emulator

The app uses a modern Android architecture with dependency injection (Dagger), ViewModels, and clean separation of concerns.