# ZeroLag Gesture Bridge

A high-performance Android gesture navigation utility specifically engineered to eliminate the **1-second touch-input freeze** when using **Niagara Launcher** on **OnePlus / OxygenOS 16 (and ColorOS)** devices without root.

---

## The Problem on OxygenOS 16 & OnePlus 15R

In modern OxygenOS (ColorOS codebase), full-screen gesture navigation is deeply hooked into OnePlus's proprietary stock launcher (`com.android.launcher` / Quickstep).

When you swipe up to go Home using the system's built-in gestures:
1. OnePlus's gesture engine animates the closing app.
2. It transitions focus over to Niagara Launcher (`bitpit.launcher`).
3. During this transition, **OnePlus's `InputDispatcher` locks and suppresses touch input for 500ms to 1200ms** waiting for the stock launcher's transition callback.
4. As a result, tapping on any app immediately after swiping home does nothing, making it feel sluggish or broken.

---

## How ZeroLag Gesture Bridge Solves It

1. **Touch Interceptor Overlay**: Sits at the bottom edge (`TYPE_APPLICATION_OVERLAY`) with zero latency. It captures swipes **before** OnePlus's Quickstep gesture engine can see them.
2. **Direct-To-Niagara Intent Dispatch**: When you swipe up, it immediately launches Niagara Launcher with `Intent.FLAG_ACTIVITY_NO_ANIMATION or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED`. This completely bypasses the system Quickstep transition engine—**touch inputs are 100% interactive instantly (0ms delay)**.
3. **Accessibility Service Bridge**: Provides instantaneous system actions for **Recents** (swipe-up & hold) and **Back** (horizontal swipe).

---

## Features

- **0ms Input Latency**: Tap apps immediately upon returning to the home screen.
- **Customizable Gesture Bar**: Adjust height (15dp–60dp), width, and visual pill visibility.
- **Haptic Vibration**: Crisp tactile feedback on gesture triggers.
- **Automatic Startup**: Seamlessly restores after device reboots.
- **No Root Required**: Works completely through standard Android permissions.

---

## ⚡ Essential OnePlus 15R / OxygenOS 16 Setup Checklist

To achieve 100% lag-free performance, follow these settings on your OnePlus phone:

### Step 1: Switch Navigation Mode
1. Open phone **Settings** > **Additional Settings** > **System Navigation**.
2. Select **Buttons** (3-Button Navigation).
   > *Why?* This turns off OnePlus's buggy Quickstep gesture engine. ZeroLag Gesture Bridge provides modern swipe gestures on top, giving you the best of both worlds with zero lag.
   > *(Optional: If you want to hide the 3-button bar completely, you can run `adb shell settings put global policy_control immersive.navigation=*`)*

### Step 2: Disable OxygenOS Battery Throttling
1. Go to **Settings** > **Apps** > **App battery management**.
2. Select **Niagara Launcher**:
   - Turn **ON** `Allow background activity`.
   - Turn **ON** `Allow auto-launch`.
3. Select **ZeroLag Gestures**:
   - Turn **ON** `Allow background activity`.
   - Turn **ON** `Allow auto-launch`.

---

## How to Build the APK

### Option A: Free 1-Click Build via GitHub Actions
1. Push this folder to a new repository on GitHub.
2. GitHub Actions will automatically compile the APK.
3. Download the built `ZeroLag-GestureBridge-debug.apk` directly from the **Actions** tab.

### Option B: Build with Android Studio
1. Open Android Studio.
2. Select **Open** and choose the `zerolag-gesture-bridge` folder.
3. Click **Build** > **Build Bundle(s) / APK(s)** > **Build APK(s)**.
4. Transfer the generated APK to your OnePlus 15R and install it.

---

## License
MIT License.
