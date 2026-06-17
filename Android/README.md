# Linkanaizer — Android App

## Quick Install (Testing)

### Option A: Install the pre-built APK
1. Copy `app/build/outputs/apk/debug/app-debug.apk` to your Android phone
2. Open it on the phone → tap "Install" (allow "Install from unknown sources" if asked)
3. Open "Linkanaizer" from your app drawer

### Option B: Build from source
**Requirements:** Java 17, Android SDK (or Android Studio)

```bash
cd Android
gradlew.bat assembleDebug
```

APK will be at: `app/build/outputs/apk/debug/app-debug.apk`

## Before First Run

### 1. Firebase Setup
You need a `google-services.json` file from your Firebase project:
1. Go to https://console.firebase.google.com → your project (links-606df)
2. Click ⚙️ Project Settings → General
3. Under "Your apps", add an Android app with package name: `com.linkanaizer.app`
4. Download `google-services.json`
5. Place it in: `Android/app/google-services.json`
6. Rebuild: `gradlew.bat assembleDebug`

### 2. Backend Server
The app needs the backend server running:
```bash
cd Backend
set PYTHONIOENCODING=utf-8
env\Scripts\python.exe -m uvicorn app.main:app --host 0.0.0.0 --port 8000
```

### 3. Google Sign-In
For Google Sign-In to work:
1. In Firebase Console → Authentication → Sign-in method → enable Google
2. Copy the **Web client ID** from Firebase Console → Authentication → Sign-in method → Google → Web SDK configuration
3. Update `GOOGLE_CLIENT_ID` in `Backend/.env`
4. Update the `setServerClientId()` value in `LoginScreen.kt`

## What Works
- Google Sign-In flow
- Home screen with categories, recent links, action buttons
- Save links via the app (Insert Link screen)
- Save links via Share-to from any app (YouTube, Chrome, Instagram, etc.)
- AI auto-categorization and summarization
- Category view with link cards and thumbnails
- Search across all links
- Create new categories (AI picks the emoji!)
- Pull-to-refresh on home screen
- Settings with logout

## Project Structure
```
Android/
  app/src/main/java/com/linkanaizer/app/
    data/api/          — Retrofit API + auth interceptor
    data/dto/          — Request/response models
    data/repository/   — Auth, Link, Category repositories
    di/                — Hilt dependency injection
    ui/auth/           — Login screen + auth ViewModel
    ui/home/           — Home screen + ViewModel
    ui/category/       — Category detail screen
    ui/links/          — All links screen with search
    ui/insert/         — Insert link screen
    ui/newcategory/    — Create category screen
    ui/settings/       — Settings + logout
    ui/share/          — Share-to receiver (bottom sheet)
    ui/components/     — Reusable composables
    ui/theme/          — Colors, typography, theme
    ui/navigation/     — Nav graph + screen routes
```
