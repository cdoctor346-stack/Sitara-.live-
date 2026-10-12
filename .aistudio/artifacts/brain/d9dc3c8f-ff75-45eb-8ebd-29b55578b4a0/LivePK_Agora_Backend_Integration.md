# LivePK — Agora.io Token Generation Backend

This module provides the secure Agora RTC token generation service for **LivePK**. It issues short-lived, role-based WebRTC tokens using your Agora App ID and App Certificate.

---

## 1. Installation

Install the official Agora Access Token builder for Node.js:
```bash
npm install agora-access-token express dotenv cors
```

---

## 2. Environment Configuration (`backend/.env`)

Add your Agora credentials to `.env`:
```env
PORT=5000
# Replace with your actual Agora credentials from console.agora.io
AGORA_APP_ID=YOUR_AGORA_APP_ID
AGORA_APP_CERTIFICATE=YOUR_AGORA_APP_CERTIFICATE
```

---

## 3. Agora Token Controller (`backend/src/controllers/agoraController.js`)

```javascript
const { RtcTokenBuilder, RtcRole } = require('agora-access-token');

/**
 * Generate Agora RTC Dynamic Token
 * Endpoint: POST /api/agora/token
 * Body: { channelName: string, uid?: number, role: 'broadcaster' | 'audience' }
 */
exports.generateRtcToken = (req, res) => {
  const { channelName, uid, role } = req.body;

  const appId = process.env.AGORA_APP_ID;
  const appCertificate = process.env.AGORA_APP_CERTIFICATE;

  // 1. Validation
  if (!appId || !appCertificate) {
    return res.status(500).json({
      error: 'Agora App ID or App Certificate is missing in server environment.'
    });
  }

  if (!channelName) {
    return res.status(400).json({
      error: 'channelName is required.'
    });
  }

  // 2. Assign UID (Numeric UID is recommended by Agora RTC Engine; 0 allows Agora to auto-assign)
  const numericUid = uid ? parseInt(uid, 10) : 0;

  // 3. Set Role (Publisher for Streamer, Subscriber for Viewer)
  let rtcRole = RtcRole.SUBSCRIBER;
  if (role === 'broadcaster' || role === 'publisher') {
    rtcRole = RtcRole.PUBLISHER;
  }

  // 4. Token Expiration (Default: 24 hours = 86,400 seconds)
  const expirationTimeInSeconds = 3600 * 24;
  const currentTimestamp = Math.floor(Date.now() / 1000);
  const privilegeExpiredTs = currentTimestamp + expirationTimeInSeconds;

  try {
    // 5. Build Token
    const token = RtcTokenBuilder.buildTokenWithUid(
      appId,
      appCertificate,
      channelName,
      numericUid,
      rtcRole,
      privilegeExpiredTs
    );

    return res.json({
      success: true,
      token,
      appId,
      channelName,
      uid: numericUid,
      role: rtcRole === RtcRole.PUBLISHER ? 'broadcaster' : 'audience',
      expiresIn: expirationTimeInSeconds
    });
  } catch (error) {
    console.error('Error generating Agora token:', error);
    return res.status(500).json({
      error: 'Failed to generate RTC token',
      details: error.message
    });
  }
};
```

---

## 4. Route Definition (`backend/src/routes/agora.js`)

```javascript
const express = require('express');
const router = express.Router();
const { generateRtcToken } = require('../controllers/agoraController');

// POST /api/agora/token
router.post('/token', generateRtcToken);

module.exports = router;
```

---

## 5. Mounting in `backend/server.js`

```javascript
const express = require('express');
const cors = require('cors');
require('dotenv').config();

const agoraRoutes = require('./src/routes/agora');

const app = express();
app.use(cors());
app.use(express.json());

// Mount Agora token endpoint
app.use('/api/agora', agoraRoutes);

const PORT = process.env.PORT || 5000;
app.listen(PORT, () => {
  console.log(`🚀 LivePK Agora Token Service running on port ${PORT}`);
});
```

---

## 6. Testing with cURL / Postman

### Broadcaster Token Request:
```bash
curl -X POST http://localhost:5000/api/agora/token \
  -H "Content-Type: application/json" \
  -d '{
    "channelName": "livepk_channel_101",
    "role": "broadcaster",
    "uid": 1234
  }'
```

### Response:
```json
{
  "success": true,
  "token": "006d8a2...sampleAgoraTokenString...",
  "appId": "YOUR_AGORA_APP_ID",
  "channelName": "livepk_channel_101",
  "uid": 1234,
  "role": "broadcaster",
  "expiresIn": 86400
}
```
