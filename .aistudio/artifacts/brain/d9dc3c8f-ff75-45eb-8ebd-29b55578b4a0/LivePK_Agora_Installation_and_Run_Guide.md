# LivePK — Agora.io Installation & Run Guide

Follow these steps to install, configure, and run Agora live video streaming in your **LivePK** project.

---

## 1. Package Installation

### A. React Native Mobile App
Navigate to your React Native project directory and install `react-native-agora`:

```bash
cd mobile
npm install react-native-agora@^4.3.2 axios socket.io-client
```

### B. Node.js Backend Server
Navigate to your backend directory and install `agora-access-token`:

```bash
cd backend
npm install agora-access-token express cors dotenv
```

---

## 2. Get Your Agora App ID & Certificate

1. Log in to [Agora Console](https://console.agora.io/).
2. Click **Project Management** -> **Create Project**.
3. Enter Project Name: `LivePK`
4. Under Use Case, select **Live Video Streaming**.
5. Under Authentication Mechanism, choose **Testing Mode (App ID only)** for quick local tests or **Secured Mode (App ID + Token)** for production.
6. Open your project settings:
   - Copy your **App ID** (Paste it in place of `YOUR_AGORA_APP_ID`).
   - Enable and copy your **App Certificate** (Paste it in `backend/.env`).

---

## 3. Native Platform Setup

### A. Android Setup

#### 1. Add Permissions in `android/app/src/main/AndroidManifest.xml`:
```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <!-- Permissions required for Agora Video & Audio Streaming -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.BLUETOOTH" />
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />

    <!-- Camera hardware features -->
    <uses-feature android:name="android.hardware.camera" android:required="false" />
    <uses-feature android:name="android.hardware.camera.autofocus" android:required="false" />

    <application ...>
        ...
    </application>
</manifest>
```

#### 2. ProGuard Configuration in `android/app/proguard-rules.pro`:
If you build release APKs, prevent ProGuard from obfuscating Agora native libraries:
```proguard
-keep class io.agora.**{*;}
-dontwarn io.agora.**
```

---

### B. iOS Setup (Mac)

#### 1. Add Permissions in `ios/LivePK/Info.plist`:
```xml
<key>NSCameraUsageDescription</key>
<string>LivePK needs camera access so you can broadcast live video streams.</string>
<key>NSMicrophoneUsageDescription</key>
<string>LivePK needs microphone access so viewers can hear your live broadcast.</string>
```

#### 2. Install CocoaPods:
```bash
cd ios
pod install
cd ..
```

---

## 4. How to Navigate Between "Go Live" & "Join Live"

In your React Native navigation (e.g. `HomeScreen.js` or `AppNavigator.js`), connect your buttons to `AgoraLiveStreamScreen`:

```javascript
import React from 'react';
import { View, TouchableOpacity, Text, StyleSheet } from 'react-native';

export default function HomeScreen({ navigation }) {
  return (
    <View style={styles.container}>
      {/* 🔴 GO LIVE BUTTON (Broadcaster Mode) */}
      <TouchableOpacity
        style={styles.goLiveBtn}
        onPress={() =>
          navigation.navigate('AgoraLiveStream', {
            mode: 'broadcaster',
            channelName: 'livepk_stage_1',
            username: 'MoinHost'
          })
        }
      >
        <Text style={styles.btnText}>🔴 START BROADCAST (GO LIVE)</Text>
      </TouchableOpacity>

      {/* 👁️ JOIN LIVE BUTTON (Audience Mode) */}
      <TouchableOpacity
        style={styles.joinLiveBtn}
        onPress={() =>
          navigation.navigate('AgoraLiveStream', {
            mode: 'audience',
            channelName: 'livepk_stage_1',
            username: 'Viewer_42'
          })
        }
      >
        <Text style={styles.btnText}>👁️ JOIN LIVE STREAM</Text>
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#090814', justifyContent: 'center', padding: 24 },
  goLiveBtn: { backgroundColor: '#FF2A85', padding: 18, borderRadius: 24, alignItems: 'center', marginBottom: 16 },
  joinLiveBtn: { backgroundColor: '#00F0FF', padding: 18, borderRadius: 24, alignItems: 'center' },
  btnText: { color: '#090814', fontWeight: '900', fontSize: 16 }
});
```

---

## 5. Testing Multi-User Live Streaming

1. **Start the Backend**:
   ```bash
   cd backend
   npm run dev
   ```
2. **Start Broadcaster**:
   - Run the mobile app on **Device 1** (or Android Emulator).
   - Tap **"START BROADCAST (GO LIVE)"**.
   - Grant Camera & Mic permissions when prompted.
   - You will see your local camera preview with beauty smoothing active.
3. **Start Viewer (Join Live)**:
   - Run the mobile app on **Device 2** (or physical phone).
   - Tap **"JOIN LIVE STREAM"** (using the same channel name, e.g. `livepk_stage_1`).
   - The remote video stream appears in real-time (< 400ms latency).
   - Tap ❤️ to test floating heart reaction bursts.
   - Type in the chat bar to test live comment sync.
   - Verify the viewer counter dynamically increments on both devices!
