# LivePK — Step-by-Step Setup & Deployment Guide

This guide details how to configure, run, and test the **LivePK** full stack application locally and deploy it to production.

---

## 1. Prerequisites

- **Node.js**: v18.x or v20.x
- **MongoDB**: Local MongoDB instance or free cloud cluster on [MongoDB Atlas](https://www.mongodb.com/atlas)
- **Agora.io Developer Account**: Free tier provides 10,000 free minutes per month on [Agora Console](https://console.agora.io/)
- **Mobile Development Tools**:
  - Android Studio with Android SDK 33+ and Emulator / Physical device
  - Xcode 15+ (for iOS Mac development)
  - React Native CLI & CocoaPods (`sudo gem install cocoapods`)

---

## 2. Backend Setup (Node.js + Express + Socket.io + MongoDB)

### Step 1: Clone & Install Dependencies
```bash
cd livepk-project/backend
npm install
```

### Step 2: Configure Environment Variables
Create `.env` inside `backend/`:
```env
PORT=5000
MONGO_URI=mongodb+srv://admin:yourpassword@cluster0.mongodb.net/livepk?retryWrites=true&w=majority
JWT_SECRET=livepk_jwt_secret_key_2026
GOOGLE_CLIENT_ID=your-google-oauth-client-id.apps.googleusercontent.com

# Obtain from Agora Console (Projects -> Edit -> App Certificate)
AGORA_APP_ID=YOUR_AGORA_APP_ID
AGORA_APP_CERTIFICATE=YOUR_AGORA_APP_CERTIFICATE

# S3 or Cloudinary storage for videos
AWS_ACCESS_KEY_ID=YOUR_AWS_KEY
AWS_SECRET_ACCESS_KEY=YOUR_AWS_SECRET
AWS_REGION=us-east-1
AWS_BUCKET_NAME=livepk-video-assets
```

### Step 3: Run the Server
```bash
# Development mode with hot reload
npm run dev

# Or production start
npm start
```
You should see:
```
✅ Connected to MongoDB successfully
🚀 LivePK Server running on http://localhost:5000
```

---

## 3. Agora.io WebRTC Setup

1. Go to [Agora Console](https://console.agora.io/) and create a project named **LivePK**.
2. Select **Testing Mode** or **Secured Mode (APP ID + Token)**.
3. Enable **App Certificate** in the project settings.
4. Copy `App ID` and `App Certificate` into `backend/.env`.
5. The backend endpoint `/api/live/rtc-token` will now dynamically issue RTC tokens for video publishers and viewers.

---

## 4. Mobile Client Setup (React Native for Android & iOS)

### Step 1: Install Dependencies
```bash
cd ../mobile
npm install
```

### Step 2: iOS Native Setup (macOS only)
```bash
cd ios
pod install
cd ..
```

### Step 3: Camera & Microphone Permissions

#### Android (`android/app/src/main/AndroidManifest.xml`):
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-feature android:name="android.hardware.camera" />
<uses-feature android:name="android.hardware.camera.autofocus" />
```

#### iOS (`ios/LivePK/Info.plist`):
```xml
<key>NSCameraUsageDescription</key>
<string>LivePK requires camera access to broadcast live video streams.</string>
<key>NSMicrophoneUsageDescription</key>
<string>LivePK requires microphone access to stream live audio to viewers.</string>
```

### Step 4: Run Mobile App

#### Run on Android:
```bash
npx react-native run-android
```

#### Run on iOS:
```bash
npx react-native run-ios
```

---

## 5. Testing the Core User Journey

1. **User Sign Up & Login**:
   - Launch app on device. Sign in with email or tap "Continue with Google".
2. **For You Feed**:
   - Swipe up and down through full-screen short videos with synchronized audio, likes, comments, and creator follow badges.
3. **Go Live Broadcasting**:
   - Tap the central "+" / "Go Live" action.
   - Switch camera (front/back), choose face filters, and tap "START LIVE STREAM".
   - Agora initializes local video publish over WebRTC.
4. **Viewer Interactivity**:
   - Open a second device or simulator.
   - Join the live room: real-time viewer counter updates instantly via Socket.io.
   - Tap hearts for floating reaction physics.
   - Open Gift Drawer to send animated roses, rockets, and crowns.
5. **Stream Summary**:
   - Streamer taps "End Stream" to view duration, total likes, and diamonds earned.

---

## 6. Production Cloud Deployment Architecture

- **Backend**: Deploy on AWS ECS / Render / Railway using Docker.
- **WebSocket Scaling**: Use Redis Adapter with Socket.io (`@socket.io/redis-adapter`) to scale horizontally across multiple container instances.
- **Video Storage & Transcoding**: Store short videos on AWS S3 and distribute globally via CloudFront CDN.
- **Live Video Network**: Agora SD-RTN (Software Defined Real-time Network) automatically routes WebRTC streams across global edge servers with sub-400ms latency.
