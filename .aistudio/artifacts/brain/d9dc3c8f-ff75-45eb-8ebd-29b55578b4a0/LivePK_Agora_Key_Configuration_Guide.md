# LivePK — Agora App ID & Certificate Configuration Guide

Here is exactly where and how to configure your **Agora App ID** and **App Certificate** in both the backend and frontend.

---

## 1. How to Get Your Agora Keys (3 Steps)

1. Open [Agora Console](https://console.agora.io/) and sign in.
2. Go to **Project Management** in the left sidebar:
   - Find your **LivePK** project (or create one).
   - Copy your **App ID** (a 32-character alphanumeric string, e.g., `4a8f9c2d1e0b...`).
3. Click the **Edit / Config** icon next to your project:
   - Find the **Features** section -> **App Certificate**.
   - Click **Enable** (if not already enabled) and click the eye icon to copy the **Primary Certificate**.

---

## 2. Backend Configuration (`backend/.env`)

Place both keys in your `backend/.env` file:

```env
# Server Port
PORT=5000

# 🔴 Paste your real Agora Keys below:
AGORA_APP_ID=paste_your_32_character_app_id_here
AGORA_APP_CERTIFICATE=paste_your_primary_certificate_here

# MongoDB Connection
MONGO_URI=mongodb://localhost:27017/livepk
JWT_SECRET=livepk_jwt_secret_2026
```

> **Security Rule**: The `AGORA_APP_CERTIFICATE` must **NEVER** be put in the frontend mobile app. It is a secret signing key used exclusively by your Node.js backend to generate dynamic tokens.

---

## 3. Frontend Centralized Config (`mobile/src/config/agoraConfig.js`)

Instead of hardcoding the App ID in multiple screens, create this centralized config file:

```javascript
// mobile/src/config/agoraConfig.js

export const AGORA_CONFIG = {
  // 🔴 Paste your Agora App ID here:
  appId: 'paste_your_32_character_app_id_here',
  
  // Backend API URL for fetching dynamic RTC tokens:
  tokenServerUrl: 'http://localhost:5000/api/agora/token',

  // Default broadcast channel parameters:
  defaultChannelProfile: 1, // Live Broadcasting
  defaultClientRole: 1,     // Broadcaster
  
  // Default Beauty Filter presets:
  beautyPresets: {
    natural: { lighteningLevel: 0.5, smoothnessLevel: 0.6, rednessLevel: 0.2 },
    smooth: { lighteningLevel: 0.7, smoothnessLevel: 0.8, rednessLevel: 0.3 },
    glow: { lighteningLevel: 0.9, smoothnessLevel: 1.0, rednessLevel: 0.4 }
  }
};
```

---

## 4. How to Use in `AgoraLiveStreamScreen.js`

Import the configuration at the top of your screen:

```javascript
import { AGORA_CONFIG } from '../../config/agoraConfig';

// Initialize Agora Engine:
engine.initialize({ appId: AGORA_CONFIG.appId });

// Fetch dynamic token from your backend:
const response = await axios.post(AGORA_CONFIG.tokenServerUrl, {
  channelName: 'livepk_room_1',
  role: 'broadcaster',
  uid: 0
});
const dynamicToken = response.data.token;

// Join Channel:
engine.joinChannel(dynamicToken, 'livepk_room_1', 0, {});
```

---

## 5. Quick Verification Checklist

| Item | Location | Contains |
|---|---|---|
| **App ID** | `backend/.env` | `AGORA_APP_ID=4a8f9c...` |
| **App Certificate** | `backend/.env` | `AGORA_APP_CERTIFICATE=7b1e4a...` |
| **App ID** | `mobile/src/config/agoraConfig.js` | `appId: '4a8f9c...'` |
| **Token API** | `POST /api/agora/token` | Generates secure dynamic token |
