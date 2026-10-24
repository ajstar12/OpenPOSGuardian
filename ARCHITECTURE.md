# OpenPOS Guardian — Architecture & Technical Specifications

**OpenPOS Guardian** is engineered as a modern, native Android application showcasing enterprise-grade kiosk management, dedicated-device lockdown, and POS launcher execution on Android (API 28 - API 36).

---

## 1. Architectural Pattern: Clean MVVM

The project enforces a strict unidirectional data flow (UDF) and clean separation of concerns:

```
┌────────────────────────────────────────────────────────┐
│                      UI Layer                          │
│  Jetpack Compose + Material 3 + ViewModels (StateFlow) │
└───────────────────────────▲────────────────────────────┘
                            │ (Observes UI State / Triggers Intents)
┌───────────────────────────┴────────────────────────────┐
│                    Domain Layer                        │
│   Immutable Models + Repository Contracts (Pure Kotlin)│
└───────────────────────────▲────────────────────────────┘
                            │ (Implements Contracts)
┌───────────────────────────┴────────────────────────────┐
│                     Data Layer                         │
│  Room DB (Audit) | DataStore (Prefs) | DPM Gateway     │
└────────────────────────────────────────────────────────┘
```

### Component Breakdown

* **`ui/`**: Declarative UI using Jetpack Compose with Material 3 design tokens.
  * Adaptive design: Handles phone vertical displays as well as POS landscape terminal matrices.
  * Components: Stat cards, status badges, confirmation dialogs, admin PIN modals, empty state views.
* **`domain/`**: Pure Kotlin business models (`DeviceInfo`, `KioskPolicyState`, `AuditEvent`, `LauncherApp`, `UserSettings`) and repository interfaces.
* **`data/`**:
  * `local/datastore/`: AndroidX DataStore Preferences storing approved app package names, grid columns, theme mode, and admin credentials.
  * `local/db/`: Room SQLite database storing append-only immutable audit ledger events.
  * `repository/`: Repository implementations coordinating local cache, device policy gateways, and package managers.
* **`device/`**:
  * `admin/OpenPOSDeviceAdminReceiver`: Android Enterprise Device Admin receiver intercepting lifecycle callbacks.
  * `kiosk/KioskManager`: Hardware-level Lock-Task coordinator managing `DevicePolicyManager` and `ActivityManager`.
  * `info/DeviceInfoProvider`: System diagnostics reading battery state, internal storage (`StatFs`), and hardware IDs without sensitive privacy permissions.

---

## 2. Android Enterprise & Lock-Task Mode Mechanics

Android provides two distinct locking concepts:

| Attribute | Screen Pinning (`LOCK_TASK_MODE_PINNED`) | True Lock-Task Kiosk (`LOCK_TASK_MODE_LOCKED`) |
| :--- | :--- | :--- |
| **Prerequisites** | Ordinary application (no admin required) | **Device Owner** privileges |
| **User Prompt** | Displays system confirmation dialog | **Silent & Immediate** (Zero user prompt) |
| **User Escape** | Escapable via Back + Recents button combo | **Unescapable** (System buttons disabled or mapped) |
| **Whitelisting** | Not supported | Supported via `dpm.setLockTaskPackages()` |
| **Status Bar Control** | Status bar is pulled down with limited items | Status bar can be **completely disabled** |
| **Keyguard Control** | Standard lock screen applies on timeout | Keyguard can be **permanently suppressed** |

### How OpenPOS Guardian Handles Both

1. **Automatic Detection**: Queries `ActivityManager.getLockTaskModeState()` to report whether the terminal is in `NONE`, `PINNED`, or `LOCKED` mode.
2. **Graceful Degradation**: If the application is **not** enrolled as Device Owner, invoking `startLockTask()` triggers standard Screen Pinning, and attempting policy configuration surfaces clear educational guidance rather than crashing.
3. **1-Click Policy Sync**: If enrolled as Device Owner, administrators can sync all launcher-approved applications directly to the `DevicePolicyManager` lock-task whitelist.

---

## 3. Data Persistence Architecture

* **DataStore Preferences**:
  * Thread-safe, non-blocking asynchronous preferences via Kotlin Coroutines.
  * Stores non-relational terminal settings: approved launcher packages (`Set<String>`), launcher column counts, appearance theme, and auto-lock flag.
* **Room Database (`audit_logs`)**:
  * SQLite database with table indexing on `timestamp`, `eventType`, and `status`.
  * Fully offline; no external network or cloud sync.
  * Supports local file export via Android's Storage Access Framework (`CreateDocument("application/json")`).

---

## 4. Package Visibility & Discovery (Android 11+)

In compliance with Android 11+ package visibility restrictions, `AndroidManifest.xml` explicitly declares:

```xml
<queries>
    <intent>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent>
</queries>
```

This ensures `PackageManager.queryIntentActivities(...)` discovers installed business and POS applications on the device without requiring broad, dangerous permissions like `QUERY_ALL_PACKAGES`.
