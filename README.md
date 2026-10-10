# Buds Popup (prototype)

Android source project for a One UI-inspired floating card for Galaxy Buds Core on HyperOS 3.

## Cloud build without Android Studio

1. Create a repository on GitHub and upload the contents of this folder to the repository (the `app`, `gradle`-related project files, `build.gradle.kts`, `settings.gradle.kts`, and `.github` folder should be at the repository root).
2. Open the repository's **Actions** tab and enable Actions if GitHub asks.
3. Select **Build Buds Popup APK** → **Run workflow**.
4. When the run finishes, open the run page and download the `BudsPopup-debug-apk` artifact.
5. Extract the ZIP on your phone and install `app-debug.apk`. Android may ask you to allow installation from the app you used to download it.

The workflow builds a debug APK in GitHub's cloud; Android Studio is not needed on the phone or PC.

## What it currently does
- Runs a foreground service and listens for Bluetooth ACL connection events.
- Shows a floating card when a connected device name contains Buds, Samsung, or Core.
- Requests the standard Android Bluetooth battery level where the OS exposes it.
- Does not fabricate a case percentage: case is marked unavailable.
- Root / KernelSU Next is not required for this prototype.

## Important limitations
- This is a starter prototype, not a tested release. The cloud workflow has been added but has not been run here.
- Standard Android APIs may expose only a single aggregate headset battery level, or no level at all. Separate left/right/case levels may require Samsung-specific data; this app does not decode proprietary Samsung BLE/GATT packets.
- Bluetooth ACL connection is not the same as detecting the physical opening of the case. The card appears after the buds connect.
- The test popup uses mock values for preview only; those are not real battery readings.
- HyperOS background and overlay restrictions may vary. Grant overlay and notification permissions and disable battery optimization if needed.
