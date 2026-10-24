# Security Policy

OpenPOS Guardian is an educational, open-source reference project demonstrating Android Enterprise, dedicated-device management, and POS terminal lockdown architectures.

---

## Supported Versions

| Version | Supported          |
| ------- | ------------------ |
| 1.0.x   | :white_check_mark: |

---

## Reporting Security Vulnerabilities

If you discover a security vulnerability within OpenPOS Guardian:

1. **Do not disclose the issue publicly** via GitHub issues.
2. Please submit a private report via GitHub Private Vulnerability Reporting or contact the project maintainer.
3. Include detailed steps to reproduce the issue, sample log outputs, and affected Android OS versions.

---

## Security Architecture Principles

* **Zero Cloud Dependency**: OpenPOS Guardian contains no remote backend, telemetry, third-party analytics, or cloud sync. All cryptographic hashes, preferences, and audit logs remain strictly on the local device.
* **Least Privilege**: Only necessary Android permissions are declared. No unnecessary accessibility, overlay, or privileged system permissions are used.
* **Encrypted On-Device Ledger**: Audit records are stored in a dedicated Room SQLite database with strict local scoping (`android:allowBackup="false"`).
* **Supported Enterprise APIs**: Device lockdown strictly follows Google's official Android Enterprise guidelines via `DevicePolicyManager`, requiring authentic Device Owner enrollment via ADB or EMM QR provisioning.
