package com.example.data

/**
 * ============================================================================
 * ⚠️ REPLACE WITH YOUR FIREBASE CONFIG ⚠️
 *
 * Update these constants with your Firebase Realtime Database project credentials.
 * You can also update the database URL in real-time within the in-app Settings dialog!
 *
 * Requirements for Firebase RTDB:
 * 1. Create a Firebase project at https://console.firebase.google.com
 * 2. In Build > Realtime Database, create a database (e.g. us-central1 or europe-west1)
 * 3. Set rules for reading (e.g. read: true, write: true for your ESP32)
 * 4. Paste your Realtime Database URL below.
 * ============================================================================
 */
object FirebaseConfigConstants {
    // ⬇️ REPLACE WITH YOUR FIREBASE REALTIME DATABASE URL ⬇️
    const val DEFAULT_DATABASE_URL = "https://full-pack-chamber-default-rtdb.firebaseio.com"

    // ⬇️ REPLACE WITH YOUR FIREBASE API KEY ⬇️
    const val DEFAULT_API_KEY = "AIzaSyREPLACE_WITH_YOUR_FIREBASE_API_KEY"

    // ⬇️ REPLACE WITH YOUR FIREBASE PROJECT ID ⬇️
    const val DEFAULT_PROJECT_ID = "full-pack-chamber"

    // Path in Firebase Realtime Database
    const val RTDB_PATH_CHAMBER = "chamber"
    const val RTDB_PATH_STATUS = "chamber/status"
    const val RTDB_PATH_CURRENT = "chamber/current"
    const val RTDB_PATH_HISTORY = "chamber/history"
}
