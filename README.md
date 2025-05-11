# RateBeer App

A social beer tasting app built with Kotlin, Jetpack Compose, and Firebase.

## Features

- User Authentication (Login/Register)
- Create and join tasting groups
- Real-time updates through Firebase
- Integration with Untappd API
- Beer rating system

## Setup Instructions

### Prerequisites

- Android Studio Hedgehog or newer
- JDK 11 or newer
- Firebase account

### Firebase Setup

1. Go to the [Firebase Console](https://console.firebase.google.com/)
2. Create a new project
3. Add an Android app to your Firebase project
   - Use the package name `dk.grp30.ratebeer`
   - Download the `google-services.json` file and place it in the `app/` directory
4. Enable Firebase Authentication in the Firebase Console
   - Activate Email/Password authentication
5. Create a Firestore database in the Firebase Console
   - Start in test mode for simplicity during development

### Untappd API

1. Register for an Untappd API key at [https://untappd.com/api/docs](https://untappd.com/api/docs)
2. Replace the placeholder values in `UntappdApiService.kt` with your actual API credentials:
   ```kotlin
   private const val CLIENT_ID = "YOUR_CLIENT_ID"
   private const val CLIENT_SECRET = "YOUR_CLIENT_SECRET"
   ```

### Building and Running

1. Open the project in Android Studio
2. Sync Gradle files
3. Build and run the application on an emulator or physical device

## Project Structure

- `app/src/main/java/dk/grp30/ratebeer/`
  - `ui/`: UI components using Jetpack Compose
    - `screens/`: Individual screens of the application
    - `theme/`: App theme definitions
    - `navigation/`: Navigation components
  - `data/`: Data layer
    - `auth/`: Firebase Authentication
    - `firestore/`: Firestore database operations
    - `api/`: Untappd API service

## Technology Stack

- Kotlin
- Jetpack Compose for UI
- Firebase Authentication and Firestore
- Retrofit and OkHttp for API calls
- Kotlin Coroutines for asynchronous programming

## Requirements

- Language: Kotlin
- Frameworks: Jetpack Compose for UI, Compose Navigation
- Asynchronous Programming: Kotlin Coroutines
- Authentication: Firebase Authentication
- Database: Firestore
- External API: Untappd via HTTP client (Retrofit) 