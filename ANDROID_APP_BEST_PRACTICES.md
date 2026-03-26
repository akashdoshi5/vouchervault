# VoucherVault: Native Android Architecture & Deployment Best Practices
*A blueprint for the new `VoucherVault` app (`com.addmrp.vault`), building on the architectural and deployment lessons from RemindMe.*

When developing **VoucherVault**, strictly adhere to these Kotlin/Java + Firebase core patterns to ensure a successful Play Store launch and data stability.

---

## 1. Project Identity (Production Metadata)
The following constants **must** be used across the app to prevent the "stale package" or "webview wrapper" flags encountered previously:

- **App Name**: `VoucherVault: Smart Cards & Storage`
- **Package ID**: `com.addmrp.vault`
- **Ownership Domain**: Ensure a matching domain (e.g., `vouchervault-app.web.app`) is used for the Privacy Policy and to prove the app is not an unauthorizated web wrapper.

---

## 2. Firebase Sync & Data Integrity (Offline vs. Online)

### The "Single Listener" Rule
- **Centralize Listeners**: ONLY set up Firestore `addSnapshotListener` connections in **one central repository or data service layer**. 
- **The Core Threat**: Duplicate listeners in multiple fragments/viewmodels cause race conditions. If one listener performs a "smart merge" and another does a "blind overwrite", the user suffers massive data loss when coming back online.

### Smart Merging over Blind Overwriting
- **Text/String Fields**: Use a three-way merge strategy. If timestamps conflict, *concatenate* both versions (e.g., `\n--- [Synced Version] ---\n`) to ensure zero user data is lost.
- **Arrays/Lists**: (Checklists, shared users) Use **Union** logic. Never drop items that exist locally but not remotely during an active session unless explicitly deleted.

### The "Zombification" Deletion Guard
- **The Problem**: A user deletes a record on Device A. The cloud deletes it. Device B (stale cache) connects, pushes the local record, and "resurrects" the deleted data.
- **The Solution**: When deleting locally, write the logical document ID to a `deletedRecords` subcollection. On app startup/login, **first** fetch `deletedRecords`, filter your local Room cache, and ONLY THEN migrate offline data online.

---

## 3. Play Store "Wrapper" & Policy Protection (CRITICAL)
Based on the `RemindMe` rejection history, **VoucherVault** MUST follow these rules to avoid suspension:

1. **Break Out of Webview**: If the app displays any web content or attachments, NEVER use an `iframe` or internal `WebView` without explicit domain ownership declaration. Always use Intent-based external browser launching (`window.open(url, '_system')` equivalent in native).
2. **Advance Notice / Authorization**: Before submitting to review, prepare a signed **Authorization Declaration** proving the developer account is authorized to represent the `vouchervault` branding.
3. **App Access**: Provide clear reviewer credentials (e.g., `test@test.com` / `test123`) in the Play Console to avoid "Incomplete App" rejections.

---

## 4. Google Play Production Deployments

### The Google Sign-in Production Blackout
- **The Crisis**: Uploading an AAB to Google Play triggers "Google Play App Signing." This replaces your local key with a cloud key. Firebase Google Auth will stop working in production.
- **The Fix**: Go to `Google Play Console -> App Integrity -> App Signing`, copy the **App Signing Certificate SHA-1 and SHA-256**, and paste them into your **Firebase Project Settings** under the Android App configuration.

### Window Insets & Keyboard UX
- Use Jetpack Compose `WindowInsets.ime.padding()` or XML `android:windowSoftInputMode="adjustResize"` to ensure the UI natively floats on top of the keyboard. Samsung and Pixel keyboards vary in height; dynamic insets are essential for the `VoucherVault` input fields.

### Haptics & Vibration
- **Alarms**: Standard taptic haptics are too weak for notifications. Use the `Vibrator` system service with an aggressive waveform pattern (`[500, 200, 500, 200, 1000]`) for important vault-related alerts.
