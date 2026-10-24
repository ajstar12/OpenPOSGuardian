# OpenPOS Guardian 🛡️

**OpenPOS Guardian** is a production-quality, open-source Android application demonstrating **Android Enterprise**, **Dedicated Devices (COSU/Kiosk)**, **DevicePolicyManager lockdown**, and a **custom POS Launcher** built with **Kotlin** and **Jetpack Compose**.

> [!NOTE]
> **Educational & Portfolio Disclaimer:**
> OpenPOS Guardian is an educational, open-source showcase and reference architecture. It is not affiliated with, endorsed by, or certified by Google, the Android Open Source Project (AOSP), or any commercial POS hardware vendor.

---

## 🌟 Key Features

* **🛡️ Android Enterprise Lock-Task Kiosk Mode**:
  * Real-time detection of `LOCK_TASK_MODE_NONE`, `LOCK_TASK_MODE_PINNED`, and `LOCK_TASK_MODE_LOCKED`.
  * Guided 1-click Lock-Task mode activation and safe termination.
  * Direct integration with `DevicePolicyManager.setLockTaskPackages()` for seamless dedicated terminal operation without escape prompts.
  * Configurable kiosk system features (Status Bar Info, Notifications, Home button, Power menu).
* **📱 POS Terminal Launcher**:
  * Clean, responsive application launcher grid displaying approved business and POS applications.
  * Extraction of native application icons and labels using `PackageManager`.
  * Search and filter installed applications.
  * Protected **Admin Portal** shortcut secured by administrator PIN authentication.
* **📋 Application Allowlist Management**:
  * Granular approval system for POS applications stored via **AndroidX DataStore Preferences**.
  * Dynamic app discovery adhering to Android 11+ `<queries>` package-visibility rules.
  * 1-Click policy synchronization between launcher allowlist and Device Owner lock-task whitelist.
  * Clear educational separation between launcher visibility and hardware-level lock-task enforcement.
* **📊 Hardware & Platform Diagnostics**:
  * Real-time device metrics: Manufacturer, Model, Brand, Board, Chipset, Build ID.
  * Battery diagnostics: Percentage, Charging source (AC, USB, Wireless), and Battery Health.
  * Storage metrics: Internal storage capacity, free space, and usage percentages via `StatFs`.
  * Platform verification: Android OS version, API Level, Security Patch level, and System Uptime.
  * **Privacy Guarantee**: Zero collection of IMEI, serial numbers, location, contacts, or sensitive data.
* **📜 Immutable On-Device Audit Ledger**:
  * Structured SQLite ledger backed by **Room Database**.
  * Tracks app starts, configuration updates, allowlist modifications, lock-task state changes, and security events.
  * Filtering by event type and audit status (`SUCCESS`, `FAILURE`, `WARNING`, `INFO`).
  * Local JSON export powered by Android's **Storage Access Framework (SAF)**.
  * **100% Offline**: No network permissions, analytics SDKs, cloud services, or data exfiltration.
* **🎨 Modern Material 3 Enterprise UI**:
  * Full support for Light and Dark themes.
  * Responsive layout adapted for handhelds, tablets, and wide POS terminal touchscreens.
  * High-contrast enterprise color palette (Navy & Cyber Teal) with explicit confirmation dialogs.

---

## 📱 Feature Matrix: Ordinary App vs. Device Owner

| Feature | Ordinary App (Unprovisioned) | Device Owner (Provisioned) |
| :--- | :--- | :--- |
| **POS Launcher UI** | ✅ Fully Functional | ✅ Fully Functional |
| **Allowlist Configuration** | ✅ Stored in local DataStore | ✅ Stored in local DataStore |
| **Hardware Diagnostics** | ✅ Standard Android APIs | ✅ Standard Android APIs |
| **Local Audit Ledger & Export** | ✅ Room Database + SAF | ✅ Room Database + SAF |
| **Lock-Task Kiosk Mode** | ⚠️ Screen Pinning (User Prompted, Escapable via gesture) | 🔒 **True Kiosk** (`LOCK_TASK_MODE_LOCKED`, Zero prompt, Unescapable) |
| **Lock-Task Package Whitelisting** | ❌ Blocked by Android (`SecurityException`) | ✅ Enforced via `dpm.setLockTaskPackages()` |
| **Status Bar Pull-Down Suppression**| ❌ Restricted to Device Owner | ✅ Enforced via `dpm.setStatusBarDisabled()` |
| **Keyguard Suppression** | ❌ Restricted to Device Owner | ✅ Enforced via `dpm.setKeyguardDisabled()` |

---

## 🛠️ Technology Stack & Architecture

* **Language**: Kotlin 2.1.0
* **UI**: Jetpack Compose with Material 3
* **Architecture**: Clean MVVM with Unidirectional Data Flow
* **Dependency Injection**: Hilt 2.60.1
* **Local Persistence**: DataStore Preferences
* **Database**: Room 2.6.1 with KSP code generation
* **Asynchronous Execution**: Kotlin Coroutines & Flow
* **Navigation**: Jetpack Navigation Compose
* **Build System**: Gradle 9.6.0 with Kotlin DSL & Version Catalog (`libs.versions.toml`)
* **CI/CD**: GitHub Actions (`.github/workflows/android.yml`)

---

## 🚀 Setup & Build Instructions

### Prerequisites
* **Android Studio**: Ladybug (2024.2.1) / Meerkat (2024.3+) or later
* **JDK**: Version 17 or higher
* **Android SDK**: Compile SDK 36 (target SDK 36, min SDK 28)

### Building the Project

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/your-username/OpenPOSGuardian.git
   cd OpenPOSGuardian
   ```
2. **Compile Debug APK**:
   ```bash
   ./gradlew assembleDebug
   ```
   *The generated APK is located at: `app/build/outputs/apk/debug/app-debug.apk`.*

3. **Run Automated Unit Tests**:
   ```bash
   ./gradlew test
   ```

---

## 📲 Device Owner Provisioning via ADB

To test true dedicated-device enterprise lockdown on an Android Virtual Device (AVD) or dedicated factory-reset test phone:

1. **Connect Device and Confirm ADB Detection**:
   ```bash
   adb devices
   ```
2. **Install the Application**:
   ```bash
   ./gradlew installDebug
   ```
3. **Enroll OpenPOS Guardian as Device Owner**:
   ```bash
   adb shell dpm set-device-owner com.openpos.guardian/.device.admin.OpenPOSDeviceAdminReceiver
   ```
4. **Verify Enrollment**:
   * Open the app; the **Dashboard** will reflect `Device Owner: ACTIVE (Provisioned)`.

*For in-depth prerequisites, common error codes, and troubleshooting, read the complete [Device Owner Guide](file:///DEVICE_OWNER_GUIDE.md).*

---

## 🔐 Security & Privacy Considerations

1. **Strict Offline Model**: OpenPOS Guardian contains no internet permissions (`android.permission.INTERNET` is omitted from the manifest). Configuration, preferences, and logs never leave the device.
2. **No Data Tampering**: Audit events are persisted to a protected SQLite database with `android:allowBackup="false"`.
3. **No Abuse of Dangerous Permissions**: The app does not request or use accessibility services or system alert overlays to simulate kiosk lockdown. It implements the genuine, supported Android Enterprise `DevicePolicyManager` APIs.

---

## ⚠️ Known Limitations

* **OEM-Specific Lockouts**: Some consumer Android skins (e.g. heavily modified vendor ROMs) restrict ADB Device Owner provisioning unless account-checking verification is disabled via OEM developer settings.
* **Multi-User Environments**: Device Owner provisioning requires that no secondary users or guest profiles exist on the target system.

---

## 🚢 Publishing to GitHub

```bash
git init
git add .
git commit -m "feat: initial implementation of OpenPOS Guardian enterprise POS kiosk"
git branch -M main
git remote add origin https://github.com/<your-username>/OpenPOSGuardian.git
git push -u origin main
```

---

## 📄 License

This project is licensed under the [MIT License](file:///LICENSE).
