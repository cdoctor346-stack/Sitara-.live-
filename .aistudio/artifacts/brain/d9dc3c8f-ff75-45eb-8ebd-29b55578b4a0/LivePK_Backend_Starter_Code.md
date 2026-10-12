# LivePK — Backend Starter Code (Node.js + Express + Socket.io + MongoDB)

This backend service powers **LivePK**'s authentication, short video feed, Agora WebRTC live stream token generation, and real-time Socket.io chat, gifts, and likes.

---

### 1. `backend/package.json`
```json
{
  "name": "livepk-backend",
  "version": "1.0.0",
  "description": "Backend API & Realtime Gateway for LivePK",
  "main": "server.js",
  "scripts": {
    "start": "node server.js",
    "dev": "nodemon server.js"
  },
  "dependencies": {
    "agora-access-token": "^2.0.4",
    "bcryptjs": "^2.4.3",
    "cors": "^2.8.5",
    "dotenv": "^16.4.5",
    "express": "^4.19.2",
    "google-auth-library": "^9.11.0",
    "jsonwebtoken": "^9.0.2",
    "mongoose": "^8.4.1",
    "socket.io": "^4.7.5"
  },
  "devDependencies": {
    "nodemon": "^3.1.2"
  }
}
```

---

### 2. `backend/.env.example`
```env
PORT=5000
MONGO_URI=mongodb+srv://<username>:<password>@cluster0.mongodb.net/livepk?retryWrites=true&w=majority
JWT_SECRET=super_secret_jwt_key_livepk_2026
GOOGLE_CLIENT_ID=your-google-oauth-client-id.apps.googleusercontent.com

# Agora.io WebRTC Credentials
AGORA_APP_ID=your_agora_app_id_here
AGORA_APP_CERTIFICATE=your_agora_app_certificate_here

# AWS S3 Storage for Videos
AWS_ACCESS_KEY_ID=your_aws_key
AWS_SECRET_ACCESS_KEY=your_aws_secret
AWS_REGION=us-east-1
AWS_BUCKET_NAME=livepk-video-assets
```

---

### 3. `backend/server.js` (Main Entry Point)
```javascript
const express = require('express');
const http = require('http');
const { Server } = require('socket.io');
const mongoose = require('mongoose');
const cors = require('cors');
require('dotenv').config();

const authRoutes = require('./src/routes/auth');
const liveRoutes = require('./src/routes/live');
const feedRoutes = require('./src/routes/feed');
const adminRoutes = require('./src/routes/admin');
const setupLiveSocket = require('./src/sockets/liveSocket');

const app = express();
const server = http.createServer(app);

// Enable Cross-Origin requests
app.use(cors({ origin: '*' }));
app.use(express.json());

// REST Routes
app.use('/api/auth', authRoutes);
app.use('/api/live', liveRoutes);
app.use('/api/feed', feedRoutes);
app.use('/api/admin', adminRoutes);

app.get('/health', (req, res) => {
  res.json({ status: 'ok', service: 'LivePK API & Socket Gateway' });
});

// Setup Socket.io for Real-time Streaming Chat & Reactions
const io = new Server(server, {
  cors: { origin: '*', methods: ['GET', 'POST'] }
});
setupLiveSocket(io);

// Connect to MongoDB
const PORT = process.env.PORT || 5000;
mongoose
  .connect(process.env.MONGO_URI || 'mongodb://localhost:27017/livepk')
  .then(() => {
    console.log('✅ Connected to MongoDB successfully');
    server.listen(PORT, () => {
      console.log(`🚀 LivePK Server running on http://localhost:${PORT}`);
    });
  })
  .catch((err) => {
    console.error('❌ MongoDB Connection Error:', err.message);
  });
```

---

### 4. Database Models (`backend/src/models/`)

#### `User.js`
```javascript
const mongoose = require('mongoose');

const UserSchema = new mongoose.Schema({
  username: { type: String, required: true, unique: true, trim: true },
  email: { type: String, required: true, unique: true },
  password: { type: String }, // Hashed if email/password signup
  googleId: { type: String },
  avatarUrl: { type: String, default: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200' },
  bio: { type: String, default: 'LivePK Streamer & Creator ✨' },
  coins: { type: Number, default: 500 },
  diamonds: { type: Number, default: 0 },
  followersCount: { type: Number, default: 0 },
  followingCount: { type: Number, default: 0 },
  isLive: { type: Boolean, default: false },
  role: { type: String, enum: ['user', 'admin'], default: 'user' },
  isBanned: { type: Boolean, default: false },
  createdAt: { type: Date, default: Date.now }
});

module.exports = mongoose.model('User', UserSchema);
```

#### `LiveStream.js`
```javascript
const mongoose = require('mongoose');

const LiveStreamSchema = new mongoose.Schema({
  channelName: { type: String, required: true, unique: true },
  title: { type: String, required: true },
  hostId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
  hostUsername: { type: String, required: true },
  hostAvatar: { type: String },
  category: { type: String, default: 'Just Chatting' },
  isLive: { type: Boolean, default: true },
  viewerCount: { type: Number, default: 0 },
  totalLikes: { type: Number, default: 0 },
  totalDiamondsEarned: { type: Number, default: 0 },
  startedAt: { type: Date, default: Date.now },
  endedAt: { type: Date }
});

module.exports = mongoose.model('LiveStream', LiveStreamSchema);
```

#### `Video.js` (For You Feed)
```javascript
const mongoose = require('mongoose');

const VideoSchema = new mongoose.Schema({
  creatorId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
  creatorUsername: { type: String, required: true },
  creatorAvatar: { type: String },
  videoUrl: { type: String, required: true },
  caption: { type: String, default: '' },
  soundTitle: { type: String, default: 'Original Audio - LivePK' },
  likesCount: { type: Number, default: 0 },
  commentsCount: { type: Number, default: 0 },
  sharesCount: { type: Number, default: 0 },
  tags: [{ type: String }],
  createdAt: { type: Date, default: Date.now }
});

module.exports = mongoose.model('Video', VideoSchema);
```

---

### 5. Agora RTC Token & Stream Controller (`src/controllers/liveController.js`)
```javascript
const { RtcTokenBuilder, RtcRole } = require('agora-access-token');
const LiveStream = require('../models/LiveStream');
const User = require('../models/User');

const AGORA_APP_ID = process.env.AGORA_APP_ID || 'mock_agora_app_id';
const AGORA_APP_CERTIFICATE = process.env.AGORA_APP_CERTIFICATE || 'mock_certificate';

/**
 * Generates dynamic Agora RTC Token for secure broadcasting or viewing
 */
exports.getAgoraRtcToken = async (req, res) => {
  try {
    const { channelName, role } = req.body;
    const uid = req.user ? req.user.id : Math.floor(Math.random() * 100000);

    if (!channelName) {
      return res.status(400).json({ error: 'Channel name is required' });
    }

    // Role: 1 = Broadcaster (Publisher), 2 = Audience (Subscriber)
    const rtcRole = role === 'broadcaster' ? RtcRole.PUBLISHER : RtcRole.SUBSCRIBER;
    const expirationTimeInSeconds = 3600 * 24; // 24 hours
    const currentTimestamp = Math.floor(Date.now() / 1000);
    const privilegeExpiredTs = currentTimestamp + expirationTimeInSeconds;

    // Generate Agora Token
    const token = RtcTokenBuilder.buildTokenWithUid(
      AGORA_APP_ID,
      AGORA_APP_CERTIFICATE,
      channelName,
      0, // uid 0 lets Agora assign numeric client ID
      rtcRole,
      privilegeExpiredTs
    );

    res.json({
      token,
      channelName,
      appId: AGORA_APP_ID
    });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
};

/**
 * Start a Live Stream session
 */
exports.startStream = async (req, res) => {
  try {
    const { title, category } = req.body;
    const channelName = `live_${req.user.id}_${Date.now()}`;

    const stream = await LiveStream.create({
      channelName,
      title: title || 'Live Broadcast on LivePK',
      hostId: req.user.id,
      hostUsername: req.user.username,
      hostAvatar: req.user.avatarUrl,
      category: category || 'Gaming'
    });

    await User.findByIdAndUpdate(req.user.id, { isLive: true });

    res.json({ stream, channelName });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
};

/**
 * End a Live Stream session
 */
exports.endStream = async (req, res) => {
  try {
    const { channelName } = req.body;
    const stream = await LiveStream.findOneAndUpdate(
      { channelName, hostId: req.user.id },
      { isLive: false, endedAt: new Date() },
      { new: true }
    );

    await User.findByIdAndUpdate(req.user.id, { isLive: false });

    res.json({ message: 'Live stream ended successfully', stream });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
};
```

---

### 6. Real-Time Socket.io Handler (`src/sockets/liveSocket.js`)
```javascript
const User = require('../models/User');
const LiveStream = require('../models/LiveStream');

// In-memory room occupancy map
const roomViewers = new Map();

module.exports = function setupLiveSocket(io) {
  io.on('connection', (socket) => {
    console.log(`🔌 Client connected: ${socket.id}`);

    // Join a live stream room
    socket.on('join_room', ({ channelName, username, avatar }) => {
      socket.join(channelName);
      socket.channelName = channelName;
      socket.username = username || 'Viewer';

      const currentCount = (roomViewers.get(channelName) || 0) + 1;
      roomViewers.set(channelName, currentCount);

      // Broadcast updated viewer count to room
      io.to(channelName).emit('viewer_count_update', { count: currentCount });

      // Send join announcement
      socket.to(channelName).emit('user_joined', {
        username: socket.username,
        avatar: avatar || ''
      });
    });

    // Handle live chat comments
    socket.on('send_chat', ({ channelName, text, isHost }) => {
      const chatPayload = {
        id: Date.now().toString(),
        senderName: socket.username,
        text,
        isHost: !!isHost,
        timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
      };
      io.to(channelName).emit('new_chat', chatPayload);
    });

    // Handle floating likes / heart bursts
    socket.on('send_like', ({ channelName, emoji = '❤️' }) => {
      io.to(channelName).emit('new_like', {
        sender: socket.username,
        emoji
      });
    });

    // Handle animated gifts & wallet deduction
    socket.on('send_gift', async ({ channelName, giftName, iconEmoji, coinCost }) => {
      try {
        io.to(channelName).emit('new_gift', {
          senderName: socket.username,
          giftName,
          iconEmoji,
          coinCost
        });

        // Update streamer diamonds
        await LiveStream.findOneAndUpdate(
          { channelName },
          { $inc: { totalDiamondsEarned: Math.floor(coinCost / 2) } }
        );
      } catch (err) {
        console.error('Gift processing error:', err.message);
      }
    });

    // Leave room
    socket.on('leave_room', ({ channelName }) => {
      socket.leave(channelName);
      const currentCount = Math.max(0, (roomViewers.get(channelName) || 1) - 1);
      roomViewers.set(channelName, currentCount);
      io.to(channelName).emit('viewer_count_update', { count: currentCount });
    });

    socket.on('disconnect', () => {
      if (socket.channelName) {
        const currentCount = Math.max(0, (roomViewers.get(socket.channelName) || 1) - 1);
        roomViewers.set(socket.channelName, currentCount);
        io.to(socket.channelName).emit('viewer_count_update', { count: currentCount });
      }
    });
  });
};
```
