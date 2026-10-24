# OpenPOS Guardian — Device Owner Provisioning Guide

This guide details the exact procedures for provisioning **OpenPOS Guardian** as a Device Owner on a test device or Android Virtual Device (AVD).

---

## ⚠️ Important Prerequisites

1. **Dedicated Test Device or Emulator**:
   * **Do not test Device Owner provisioning on your primary personal device.**
   * Setting a Device Owner changes device-level management policies.
2. **Zero Configured Accounts**:
   * Android Enterprise strictly mandates that a device must have **no existing user accounts** (Google, work, email, or social accounts) before provisioning via ADB.
   * If accounts exist, Android will reject provisioning with:
     `java.lang.IllegalStateException: Not allowed to set the device owner because there are already some accounts on the device`
3. **USB Debugging**:
   * Enable **Developer Options** (tap Build Number 7 times).
   * Enable **USB Debugging**.

---

## Step-by-Step Provisioning via ADB

### Step 1: Verify Connected Device
```bash
adb devices
```
*Ensure your test phone or emulator appears in the device list.*

### Step 2: Remove Existing Accounts (If Not Factory Reset)
If using an existing device, navigate to:
**Settings > Passwords & Accounts** (or **Accounts & Backup**) and remove all listed accounts.
*(If any system account cannot be removed, perform a standard Factory Reset).*

### Step 3: Install OpenPOS Guardian Debug Build
Build and install the application directly from the repository root:
```bash
./gradlew installDebug
```
*Or install the prebuilt APK:*
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Step 4: Execute Device Owner Command
Run the following shell command to register the component as Device Owner:

```bash
adb shell dpm set-device-owner com.openpos.guardian/.device.admin.OpenPOSDeviceAdminReceiver
```

### Step 5: Verify Provisioning Success
If successful, ADB will print:
```
Success: Device owner set to package ComponentInfo{com.openpos.guardian/com.openpos.guardian.device.admin.OpenPOSDeviceAdminReceiver}
Active admin set to component {com.openpos.guardian/com.openpos.guardian.device.admin.OpenPOSDeviceAdminReceiver}
```

Open **OpenPOS Guardian**:
1. Check the **Dashboard**: The `Device Owner` status card will now display **ACTIVE (Provisioned)** in green.
2. Go to **Kiosk Control**: You can now lock the terminal with zero prompt, disable the status bar, and sync the allowlist to `DevicePolicyManager`.

---

## Troubleshooting Provisioning Failures

| Error | Root Cause | Solution |
| :--- | :--- | :--- |
| `java.lang.IllegalStateException: Not allowed to set the device owner because there are already some accounts` | An account is present on the device. | Go to Settings > Accounts and remove all accounts, or factory reset the device. |
| `java.lang.IllegalStateException: User setup already complete` | On older or OEM-modified OS builds, provisioning is only accepted before completing the initial setup wizard. | Factory reset the device. At the initial welcome screen, do not set up accounts. Enable USB debugging via fastboot or factory test mode. |
| `java.lang.SecurityException: Neither user 2000 nor current process has android.permission.MANAGE_DEVICE_ADMINS` | USB debugging is not granted root/shell admin access. | Authorize the USB debugging RSA fingerprint prompt on the device screen. |
| `java.lang.IllegalArgumentException: Unknown component: ...` | The APK is not installed yet or package name is mismatched. | Ensure `com.openpos.guardian` is installed on the target device before running the command. |

---

## De-provisioning and Resetting

To revoke Device Owner status and return the test device to normal unmanaged state:

```bash
adb shell dpm remove-active-admin com.openpos.guardian/.device.admin.OpenPOSDeviceAdminReceiver
```

Or perform a standard factory reset from **Settings > System > Reset options > Erase all data (factory reset)**.
