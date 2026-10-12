# LivePK — System Architecture & Folder Structure

LivePK is a TikTok-style Live Streaming and Short Video mobile application designed for cross-platform iOS and Android deployment with real-time Agora/WebRTC broadcasting, interactive live chat, dynamic gifts, and personalized video feeds.

---

## 1. High-Level Architecture

```
┌────────────────────────────────────────────────────────────────────────┐
│                   React Native Client (iOS & Android)                  │
│                                                                        │
│   ┌───────────────┐   ┌───────────────────┐   ┌────────────────────┐   │
│   │ For You Feed  │   │  Go Live Screen   │   │  Interactive HUD   │   │
│   │ (FlatList/    │   │ (Agora RTC Engine │   │ (Chat, Flying      │   │
│   │  Reanimated)  │   │  & Face Filters)  │   │  Hearts, Gifts)    │   │
│   └───────┬───────┘   └─────────┬─────────┘   └──────────┬─────────┘   │
└───────────┼─────────────────────┼────────────────────────┼─────────────┘
            │ HTTPS               │ WebRTC                 │ WSS (Socket.io)
            ▼                     ▼                        ▼
┌───────────────────────┐ ┌───────────────┐ ┌────────────────────────────┐
│   Node.js / Express   │ │ Agora.io SD-  │ │ Socket.io Cluster Engine   │
│   REST API Gateway    │ │ RTN (WebRTC)  │ │ (Real-Time Pub/Sub)        │
│                       │ │ Video Ingest  │ │                            │
│  • Auth (JWT/OAuth)   │ │ & Broadcast   │ │  • Live Chat Relay         │
│  • Feed & Algorithms  │ └───────┬───────┘ │  • Animated Gift Events    │
│  • Stream Token Gen   │         │         │  • Heart Bursts & Likes    │
│  • Admin Moderation   │         │ HLS/VOD │  • Real-time Viewer Counts │
└───────────┬───────────┘         ▼         └──────────────┬─────────────┘
            │             ┌───────────────┐                │
            │             │ AWS S3 / Mux  │                │
            ▼             │ Cloud Storage │                ▼
┌───────────────────────┐ └───────────────┘ ┌────────────────────────────┐
│   MongoDB Database    │                   │ Redis Cache / Socket Store │
│                       │                   │                            │
│  • Users & Profiles   │                   │  • Active Room Occupancy   │
│  • Videos & Metadata  │                   │  • Rate Limiting           │
│  • Streams & VODs     │                   │  • Leaderboards            │
│  • Gifts & Wallets    │                   │                            │
└───────────────────────┘                   └────────────────────────────┘
```

---

## 2. Full Project Folder Structure

### A. Monorepo Organization
```
livepk-project/
├── backend/                       # Node.js + Express + Socket.io server
│   ├── src/
│   │   ├── config/                # Environment variables, DB & Agora setup
│   │   │   ├── db.js
│   │   │   ├── agora.js
│   │   │   └── s3.js
│   │   ├── controllers/           # Request handlers
│   │   │   ├── authController.js
│   │   │   ├── feedController.js
│   │   │   ├── liveController.js
│   │   │   └── adminController.js
│   │   ├── models/                # MongoDB Mongoose schemas
│   │   │   ├── User.js
│   │   │   ├── Video.js
│   │   │   ├── LiveStream.js
│   │   │   └── GiftTransaction.js
│   │   ├── routes/                # Express API routes
│   │   │   ├── auth.js
│   │   │   ├── feed.js
│   │   │   ├── live.js
│   │   │   └── admin.js
│   │   ├── sockets/               # Real-time WebSocket handlers
│   │   │   └── liveSocket.js
│   │   ├── middleware/            # JWT verification & role validation
│   │   │   ├── authMiddleware.js
│   │   │   └── adminMiddleware.js
│   │   └── app.js                 # Express app initialization
│   ├── server.js                  # HTTP & Socket.io server entrypoint
│   ├── package.json
│   └── .env.example
│
├── mobile/                        # React Native cross-platform app (iOS & Android)
│   ├── src/
│   │   ├── api/                   # Axios client & REST endpoints
│   │   │   └── client.js
│   │   ├── assets/                # App icons, gifts, animations, fonts
│   │   │   ├── gifts/
│   │   │   └── sounds/
│   │   ├── components/            # Reusable UI widgets
│   │   │   ├── VideoPlayer.js
│   │   │   ├── LiveChatOverlay.js
│   │   │   ├── GiftModal.js
│   │   │   ├── FloatingHearts.js
│   │   │   └── ViewerCountBadge.js
│   │   ├── context/               # Global state (Auth, Socket, Stream)
│   │   │   ├── AuthContext.js
│   │   │   └── SocketContext.js
│   │   ├── navigation/            # React Navigation (BottomTabs & NativeStack)
│   │   │   └── AppNavigator.js
│   │   ├── screens/               # Main Application Screens
│   │   │   ├── Auth/
│   │   │   │   ├── LoginScreen.js
│   │   │   │   └── SignupScreen.js
│   │   │   ├── Feed/
│   │   │   │   └── ForYouFeedScreen.js
│   │   │   ├── Live/
│   │   │   │   ├── GoLiveScreen.js
│   │   │   │   └── LiveAudienceScreen.js
│   │   │   └── Profile/
│   │   │       └── UserProfileScreen.js
│   │   ├── services/              # Agora RTC Video Engine wrapper
│   │   │   └── AgoraService.js
│   │   └── theme/                 # Cyber dark styling & color tokens
│   │       └── colors.js
│   ├── App.js                     # Root entry component
│   ├── package.json
│   ├── android/                   # Native Android project files
│   └── ios/                       # Native iOS project & Podfile
│
└── admin-dashboard/               # Web-based Admin Panel (React/Vite)
    ├── src/
    │   ├── pages/
    │   │   ├── UsersManagement.jsx
    │   │   ├── LiveStreamsMonitor.jsx
    │   │   └── ReportsModeration.jsx
    │   └── App.jsx
    └── package.json
```
