# Contributing to OpenPOS Guardian

Thank you for your interest in contributing to **OpenPOS Guardian**! We welcome contributions from Android developers, enterprise architects, and POS system specialists.

---

## Code of Conduct

Please maintain a collaborative, respectful, and professional environment across all issues and pull requests.

---

## Development Setup

1. **Prerequisites**:
   * Android Studio (Ladybug / Meerkat or later)
   * JDK 17 or higher
   * Android SDK Platforms up to API 36
2. **Clone and Open**:
   ```bash
   git clone https://github.com/your-username/OpenPOSGuardian.git
   cd OpenPOSGuardian
   ```
3. **Verify Build**:
   ```bash
   ./gradlew assembleDebug test
   ```

---

## Coding Standards & Guidelines

* **Kotlin Style**: Follow standard Kotlin coding conventions (`kotlin.code.style=official`).
* **Architecture**: Strict MVVM with Separation of Concerns.
  * UI layer: Jetpack Compose + ViewModels with `StateFlow`.
  * Domain layer: Pure models and repository contracts.
  * Data layer: DataStore Preferences, Room entities, and device policy wrappers.
* **Strings**: User-facing UI strings must reside in `strings.xml`.
* **Security & Device Owner**:
  * Never attempt to silently bypass Android Enterprise constraints.
  * Always handle `SecurityException` when invoking `DevicePolicyManager`.
  * Distinguish `LOCK_TASK_MODE_LOCKED` (true enterprise kiosk) from `LOCK_TASK_MODE_PINNED` (screen pinning).

---

## Submitting Pull Requests

1. Create a feature branch (`git checkout -b feature/your-feature-name`).
2. Run test suites locally (`./gradlew test`).
3. Commit with concise, descriptive commit messages.
4. Push to your fork and submit a Pull Request against `main`.
