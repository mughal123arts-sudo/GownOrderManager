# Gown Order Manager

An offline Android app for managing gown orders for MUGHAL ARTS.

## Main features
- Dashboard with order counts (Total, Pending, Paid, Total Gowns)
- Create, edit and delete orders
- Four gown colors (Blue, Green, Mehroon, Black) with quantities
- Payment tracking with two statuses only: Pending and Paid
- Saved orders with search and filters
- Customer slip as PNG image, with Save and Share
- Business settings (name, phone, address, logo, footer text)

## Technology used
Kotlin, Jetpack Compose, Material 3, MVVM, Room, DataStore. Works fully offline.

## Open in Android Studio
1. Clone this repository.
2. In Android Studio choose File > Open and select the project folder.
3. Wait for Gradle sync to finish.

## Build the APK
- Android Studio: Build > Build Bundle(s) / APK(s) > Build APK(s)
- GitHub: open the Actions tab, open the latest "Build APK" run and download the
  `GownOrderManager-debug-apk` artifact.

## Run the application
Install the debug APK on an Android phone (Android 8.0 or newer), or run it from
Android Studio on an emulator or connected device.
