# Nodex Android App - Android Studio Setup

## Prerequisites
- Android Studio (latest version recommended)
- Java 11 or higher (Java 21 supported)
- Android SDK API 34

## Setup Instructions

### 1. Open in Android Studio
1. Launch Android Studio
2. Select "Open an existing Android Studio project"
3. Navigate to this project directory and select it
4. Wait for Gradle sync to complete

### 2. Configure Android SDK
1. Go to File > Settings (or Android Studio > Preferences on macOS)
2. Navigate to Appearance & Behavior > System Settings > Android SDK
3. Ensure Android 14 (API 34) is installed
4. Click "Apply" and "OK"

### 3. Build the Project
1. Click "Build" in the menu
2. Select "Make Project" or press Ctrl+F9 (Cmd+F9 on macOS)
3. Wait for the build to complete

### 4. Run the App
1. Connect an Android device or start an emulator
2. Click the "Run" button or press Shift+F10 (Shift+Cmd+R on macOS)
3. Select your device/emulator
4. The app should install and launch

## Project Structure
- `nodex-android/` - Main Android application module
- `nodex-api/` - API interfaces and contracts
- `nodex-core/` - Core business logic and implementation

## Key Features
- **App Name**: Nodex
- **Package**: org.nodex
- **Target SDK**: Android 14 (API 34)
- **Min SDK**: Android 5.0 (API 21)
- **Theme**: Material Design 3 (NodexTheme)

## Build Configuration
- **Gradle**: 7.3.3
- **Android Gradle Plugin**: 7.2.2
- **Java**: 11 (compatible with Java 21)
- **Kotlin**: 1.9.10

## Notes
- The app has been completely rebranded from Briar to Nodex
- All comments have been removed from source code
- Custom app icon has been implemented
- Themes have been updated to use NodexTheme and NodexDialogTheme

## Troubleshooting
If you encounter build issues:
1. Clean the project: Build > Clean Project
2. Rebuild: Build > Rebuild Project
3. Sync Gradle: File > Sync Project with Gradle Files
4. Check SDK configuration in settings

### Gradle JVM Issues
If you see "Incompatible Gradle JVM" errors:
1. Go to File > Settings > Build, Execution, Deployment > Build Tools > Gradle
2. In "Gradle JVM" dropdown, select your Java installation (Java 11 or higher)
3. Click "Apply" and "OK"
4. Sync project again

### Java Version Compatibility
- The project supports Java 11+ (including Java 21)
- If using Java 21, Android Studio will automatically configure compatibility
- Gradle will use Java 11 target compatibility for Android compatibility

## Support
This project is ready for Android Studio development and building.
