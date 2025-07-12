# Nodex - Clean Android App

A clean, simplified Android messaging application based on peer-to-peer communication.

## Project Structure

This project has been to focus on the core app functionality:

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
  - Main entry point: `SplashScreenActivity` 
  - Main navigation: `NavDrawerActivity`
  - Secure messaging interface
  
- **nodex-api**: API layer for the messaging protocols
- **nodex-core**: Core functionality and business logic

## Removed Unnecessary Files

The following files have been removed to create a cleaner development environment:
- Documentation files (CONTRIBUTING.md, TRANSLATION.md, LICENSE.txt)
- Build scripts (update-dependency-pinning.sh, update-translations.sh)
- Test directories (androidTest/, test/ folders)
- Fastlane automation (fastlane/ folder)
- Artwork assets (artwork/ folder)
- Witness verification files (witness.gradle files)

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