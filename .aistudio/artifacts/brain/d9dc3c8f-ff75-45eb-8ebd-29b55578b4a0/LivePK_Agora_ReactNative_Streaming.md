# LivePK — React Native Agora.io Live Streaming Screen

This component provides a complete live broadcasting and viewing experience using **`react-native-agora` (v4.x)**.

It seamlessly supports:
- **Broadcaster Mode ("Go Live")**: Publishes local camera & mic stream, front/back camera toggle, and hardware-accelerated **Agora Beauty Filter**.
- **Audience Mode ("Join Live")**: Subscribes to remote host video stream, live comments, floating likes, and viewer count HUD.

---

### File: `mobile/src/screens/Live/AgoraLiveStreamScreen.js`

```javascript
import React, { useState, useEffect, useRef } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  TextInput,
  FlatList,
  Alert,
  PermissionsAndroid,
  Platform,
  Dimensions,
  Animated
} from 'react-native';
import {
  createAgoraRtcEngine,
  ChannelProfileType,
  ClientRoleType,
  RtcSurfaceView,
  LighteningContrastLevel,
  VideoSourceType
} from 'react-native-agora';
import axios from 'axios';
import io from 'socket.io-client';

const { width, height } = Dimensions.get('window');

// ⚙️ CONFIGURATION
// Replace with your Agora App ID or read from your backend
const DEFAULT_AGORA_APP_ID = 'YOUR_AGORA_APP_ID_HERE';
const BACKEND_URL = 'http://localhost:5000'; // Replace with your IP/domain

export default function AgoraLiveStreamScreen({ route, navigation }) {
  // Parameters passed when navigating: mode ('broadcaster' or 'audience'), channelName, username
  const { mode = 'broadcaster', channelName = 'livepk_room_1', username = 'Creator' } = route?.params || {};

  const isBroadcaster = mode === 'broadcaster';

  // State
  const [isJoined, setIsJoined] = useState(false);
  const [remoteUid, setRemoteUid] = useState(0);
  const [viewerCount, setViewerCount] = useState(isBroadcaster ? 1 : 0);
  const [isMuted, setIsMuted] = useState(false);
  const [isBeautyEnabled, setIsBeautyEnabled] = useState(true);
  const [beautyLevel, setBeautyLevel] = useState('Smooth'); // 'Off' | 'Natural' | 'Smooth' | 'Glow'
  const [chatMessages, setChatMessages] = useState([
    { id: '1', sender: 'LivePK Bot', text: isBroadcaster ? 'You are live! Welcome viewers 🎉' : 'Welcome to the live stream! Say hi 👋' }
  ]);
  const [chatInput, setChatInput] = useState('');
  const [likeCount, setLikeCount] = useState(140);
  const [floatingHearts, setFloatingHearts] = useState([]);

  // References
  const agoraEngineRef = useRef(null);
  const socketRef = useRef(null);

  useEffect(() => {
    setupPermissionsAndStream();
    setupWebSocket();

    return () => {
      leaveLiveChannel();
    };
  }, []);

  // 1. Request Android Permissions (Camera & Mic)
  const requestCameraAndAudioPermissions = async () => {
    if (Platform.OS === 'android') {
      try {
        const granted = await PermissionsAndroid.requestMultiple([
          PermissionsAndroid.PERMISSIONS.CAMERA,
          PermissionsAndroid.PERMISSIONS.RECORD_AUDIO,
        ]);
        return (
          granted[PermissionsAndroid.PERMISSIONS.CAMERA] === PermissionsAndroid.RESULTS.GRANTED &&
          granted[PermissionsAndroid.PERMISSIONS.RECORD_AUDIO] === PermissionsAndroid.RESULTS.GRANTED
        );
      } catch (err) {
        console.warn(err);
        return false;
      }
    }
    return true;
  };

  // 2. Fetch Agora Token from Backend & Initialize Agora Engine
  const setupPermissionsAndStream = async () => {
    const hasPermissions = await requestCameraAndAudioPermissions();
    if (!hasPermissions && isBroadcaster) {
      Alert.alert('Permission Error', 'Camera and Audio permissions are required to go live.');
      navigation.goBack();
      return;
    }

    try {
      // 1. Fetch dynamic token from your Node.js backend
      let token = '';
      let appId = DEFAULT_AGORA_APP_ID;

      try {
        const tokenRes = await axios.post(`${BACKEND_URL}/api/agora/token`, {
          channelName,
          role: mode,
          uid: 0
        });
        token = tokenRes.data.token;
        if (tokenRes.data.appId) appId = tokenRes.data.appId;
      } catch (tokenErr) {
        console.log('Using testing token/fallback:', tokenErr.message);
      }

      // 2. Initialize Agora RTC Engine
      const engine = createAgoraRtcEngine();
      agoraEngineRef.current = engine;

      engine.initialize({ appId });

      // Register Event Callbacks
      engine.registerEventHandler({
        onJoinChannelSuccess: (connection, elapsed) => {
          console.log(`✅ Successfully joined channel: ${connection.channelId}`);
          setIsJoined(true);
        },
        onUserJoined: (connection, uid, elapsed) => {
          console.log(`👤 Remote user joined with UID: ${uid}`);
          setRemoteUid(uid);
        },
        onUserOffline: (connection, uid, reason) => {
          console.log(`👋 User offline: ${uid}`);
          if (uid === remoteUid) {
            setRemoteUid(0);
            if (!isBroadcaster) {
              Alert.alert('Stream Ended', 'The broadcaster has ended the live stream.');
              navigation.goBack();
            }
          }
        },
        onError: (errCode, msg) => {
          console.error(`Agora Error [${errCode}]: ${msg}`);
        }
      });

      // 3. Configure Profiles and Role
      engine.setChannelProfile(ChannelProfileType.ChannelProfileLiveBroadcasting);

      if (isBroadcaster) {
        engine.setClientRole(ClientRoleType.ClientRoleBroadcaster);
        engine.enableVideo();
        engine.enableAudio();
        engine.startPreview();

        // 4. Apply Initial Beauty Filter
        applyAgoraBeautyFilter(engine, true, 0.7, 0.6);
      } else {
        engine.setClientRole(ClientRoleType.ClientRoleAudience);
        engine.enableVideo();
        engine.enableAudio();
      }

      // 5. Join Channel
      engine.joinChannel(token, channelName, 0, {});

    } catch (e) {
      console.error('Stream initialization error:', e);
      Alert.alert('Agora Error', e.message);
    }
  };

  // 3. Agora Native Beauty Filter Engine
  const applyAgoraBeautyFilter = (engine, enabled, smoothness = 0.7, lightening = 0.6) => {
    try {
      if (!engine) return;
      engine.setBeautyEffectOptions(enabled, {
        lighteningContrastLevel: LighteningContrastLevel.LighteningContrastNormal,
        lighteningLevel: lightening, // 0.0 - 1.0 (Skin brightening)
        smoothnessLevel: smoothness, // 0.0 - 1.0 (Skin smoothing)
        rednessLevel: 0.2,           // 0.0 - 1.0 (Rosy glow)
        sharpnessLevel: 0.3          // 0.0 - 1.0 (Detail contrast)
      });
    } catch (err) {
      console.log('Beauty filter error:', err);
    }
  };

  const toggleBeautyMode = () => {
    const engine = agoraEngineRef.current;
    if (beautyLevel === 'Off') {
      applyAgoraBeautyFilter(engine, true, 0.5, 0.4);
      setBeautyLevel('Natural');
    } else if (beautyLevel === 'Natural') {
      applyAgoraBeautyFilter(engine, true, 0.8, 0.7);
      setBeautyLevel('Smooth');
    } else if (beautyLevel === 'Smooth') {
      applyAgoraBeautyFilter(engine, true, 1.0, 0.9);
      setBeautyLevel('Glow ✨');
    } else {
      applyAgoraBeautyFilter(engine, false, 0, 0);
      setBeautyLevel('Off');
    }
  };

  // 4. Real-time WebSocket Synchronization (Chat, Likes, Viewers)
  const setupWebSocket = () => {
    const socket = io(BACKEND_URL);
    socketRef.current = socket;

    socket.emit('join_room', { channelName, username });

    socket.on('viewer_count_update', ({ count }) => {
      setViewerCount(count);
    });

    socket.on('new_chat', (msg) => {
      setChatMessages((prev) => [...prev, msg]);
    });

    socket.on('new_like', () => {
      setLikeCount((prev) => prev + 1);
      spawnHeartAnimation();
    });
  };

  const spawnHeartAnimation = () => {
    const newHeart = {
      id: Date.now() + Math.random(),
      x: Math.random() * 60 - 30
    };
    setFloatingHearts((prev) => [...prev, newHeart]);
    setTimeout(() => {
      setFloatingHearts((prev) => prev.filter((h) => h.id !== newHeart.id));
    }, 2000);
  };

  const sendChatMessage = () => {
    if (!chatInput.trim()) return;
    const msg = {
      id: Date.now().toString(),
      sender: username,
      text: chatInput.trim(),
      isHost: isBroadcaster
    };
    socketRef.current?.emit('send_chat', {
      channelName,
      text: chatInput.trim(),
      isHost: isBroadcaster
    });
    setChatMessages((prev) => [...prev, msg]);
    setChatInput('');
  };

  const sendLike = () => {
    socketRef.current?.emit('send_like', { channelName, emoji: '❤️' });
    setLikeCount((prev) => prev + 1);
    spawnHeartAnimation();
  };

  const toggleMute = () => {
    const nextState = !isMuted;
    setIsMuted(nextState);
    agoraEngineRef.current?.muteLocalAudioStream(nextState);
  };

  const switchCamera = () => {
    agoraEngineRef.current?.switchCamera();
  };

  const leaveLiveChannel = () => {
    try {
      agoraEngineRef.current?.leaveChannel();
      agoraEngineRef.current?.release();
      socketRef.current?.emit('leave_room', { channelName });
      socketRef.current?.disconnect();
    } catch (e) {
      console.log(e);
    }
  };

  return (
    <View style={styles.container}>
      {/* 1. AGORA VIDEO FEED */}
      {isBroadcaster ? (
        // Broadcaster sees own camera preview (uid: 0)
        <RtcSurfaceView
          style={styles.fullScreenVideo}
          canvas={{ uid: 0, sourceType: VideoSourceType.VideoSourceCamera }}
        />
      ) : remoteUid !== 0 ? (
        // Audience sees remote host stream
        <RtcSurfaceView
          style={styles.fullScreenVideo}
          canvas={{ uid: remoteUid }}
        />
      ) : (
        // Waiting for host to broadcast
        <View style={styles.waitingContainer}>
          <Text style={styles.waitingTitle}>Connecting to Live Stream...</Text>
          <Text style={styles.waitingSubtitle}>Waiting for {channelName} to broadcast video</Text>
        </View>
      )}

      {/* Floating Animated Hearts */}
      {floatingHearts.map((heart) => (
        <FloatingHeart key={heart.id} xOffset={heart.x} />
      ))}

      {/* 2. TOP HUD: Streamer info, Live Badge, Viewers, Close */}
      <View style={styles.topHud}>
        <View style={styles.streamerPill}>
          <Text style={styles.streamerName}>{isBroadcaster ? 'You (Host)' : channelName}</Text>
          <Text style={styles.roleTag}>{isBroadcaster ? 'LIVE 🔴' : 'WATCHING'}</Text>
        </View>

        <View style={styles.topRightControls}>
          <View style={styles.viewerBadge}>
            <Text style={styles.viewerText}>👁️ {viewerCount}</Text>
          </View>

          <TouchableOpacity
            style={styles.closeBtn}
            onPress={() => {
              leaveLiveChannel();
              navigation.goBack();
            }}
          >
            <Text style={styles.closeBtnText}>✕</Text>
          </TouchableOpacity>
        </View>
      </View>

      {/* 3. BROADCASTER TOOLBAR (Beauty filter, flip camera, mic mute) */}
      {isBroadcaster && (
        <View style={styles.broadcasterToolbar}>
          <TouchableOpacity style={styles.toolBtn} onPress={toggleBeautyMode}>
            <Text style={styles.toolEmoji}>✨</Text>
            <Text style={styles.toolLabel}>{beautyLevel}</Text>
          </TouchableOpacity>

          <TouchableOpacity style={styles.toolBtn} onPress={switchCamera}>
            <Text style={styles.toolEmoji}>🔄</Text>
            <Text style={styles.toolLabel}>Flip</Text>
          </TouchableOpacity>

          <TouchableOpacity style={styles.toolBtn} onPress={toggleMute}>
            <Text style={styles.toolEmoji}>{isMuted ? '🔇' : '🎙️'}</Text>
            <Text style={styles.toolLabel}>{isMuted ? 'Unmute' : 'Mute'}</Text>
          </TouchableOpacity>
        </View>
      )}

      {/* 4. LIVE CHAT OVERLAY */}
      <View style={styles.chatContainer}>
        <FlatList
          data={chatMessages}
          keyExtractor={(item) => item.id}
          showsVerticalScrollIndicator={false}
          renderItem={({ item }) => (
            <View style={styles.chatBubble}>
              <Text style={item.isHost ? styles.chatHostName : styles.chatSenderName}>
                {item.sender}:{' '}
              </Text>
              <Text style={styles.chatMessageText}>{item.text}</Text>
            </View>
          )}
        />
      </View>

      {/* 5. BOTTOM INTERACTION BAR */}
      <View style={styles.bottomBar}>
        <TextInput
          style={styles.chatInput}
          placeholder="Say something nice..."
          placeholderTextColor="#94A3B8"
          value={chatInput}
          onChangeText={setChatInput}
          onSubmitEditing={sendChatMessage}
        />

        <TouchableOpacity style={styles.sendBtn} onPress={sendChatMessage}>
          <Text style={styles.sendBtnText}>Send</Text>
        </TouchableOpacity>

        {/* Like Button */}
        <TouchableOpacity style={styles.likeBtn} onPress={sendLike}>
          <Text style={styles.likeIcon}>❤️</Text>
          <Text style={styles.likeCount}>{likeCount}</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
}

// Subcomponent: Animated Floating Heart
function FloatingHeart({ xOffset = 0 }) {
  const animY = useRef(new Animated.Value(0)).current;
  const animOpacity = useRef(new Animated.Value(1)).current;

  useEffect(() => {
    Animated.parallel([
      Animated.timing(animY, {
        toValue: -320,
        duration: 1800,
        useNativeDriver: true
      }),
      Animated.timing(animOpacity, {
        toValue: 0,
        duration: 1800,
        useNativeDriver: true
      })
    ]).start();
  }, []);

  return (
    <Animated.View
      style={[
        styles.heartParticle,
        {
          transform: [{ translateY: animY }, { translateX: xOffset }],
          opacity: animOpacity
        }
      ]}
    >
      <Text style={{ fontSize: 32 }}>❤️</Text>
    </Animated.View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#090814' },
  fullScreenVideo: { width, height, position: 'absolute' },
  waitingContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#0F0E26'
  },
  waitingTitle: { color: '#00F0FF', fontSize: 18, fontWeight: 'bold' },
  waitingSubtitle: { color: '#94A3B8', fontSize: 13, marginTop: 8 },
  topHud: {
    position: 'absolute',
    top: 50,
    left: 16,
    right: 16,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center'
  },
  streamerPill: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: 'rgba(15, 14, 38, 0.75)',
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 20
  },
  streamerName: { color: '#FFF', fontWeight: 'bold', fontSize: 13, marginRight: 6 },
  roleTag: { color: '#FF2A85', fontWeight: '900', fontSize: 11 },
  topRightControls: { flexDirection: 'row', alignItems: 'center' },
  viewerBadge: {
    backgroundColor: 'rgba(0,0,0,0.6)',
    paddingHorizontal: 10,
    paddingVertical: 6,
    borderRadius: 14,
    marginRight: 8
  },
  viewerText: { color: '#FFF', fontSize: 12, fontWeight: 'bold' },
  closeBtn: {
    backgroundColor: 'rgba(0,0,0,0.6)',
    width: 32,
    height: 32,
    borderRadius: 16,
    justifyContent: 'center',
    alignItems: 'center'
  },
  closeBtnText: { color: '#FFF', fontSize: 16, fontWeight: 'bold' },
  broadcasterToolbar: {
    position: 'absolute',
    right: 16,
    top: 110,
    alignItems: 'center'
  },
  toolBtn: {
    backgroundColor: 'rgba(25, 23, 48, 0.85)',
    width: 50,
    height: 50,
    borderRadius: 25,
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: 12,
    borderWidth: 1,
    borderColor: '#3730A3'
  },
  toolEmoji: { fontSize: 20 },
  toolLabel: { color: '#FFF', fontSize: 9, fontWeight: 'bold', marginTop: 1 },
  chatContainer: {
    position: 'absolute',
    bottom: 85,
    left: 14,
    width: '75%',
    maxHeight: 220
  },
  chatBubble: {
    flexDirection: 'row',
    backgroundColor: 'rgba(15, 14, 38, 0.8)',
    borderRadius: 10,
    paddingHorizontal: 10,
    paddingVertical: 5,
    marginBottom: 6,
    alignSelf: 'flex-start'
  },
  chatSenderName: { color: '#00F0FF', fontWeight: 'bold', fontSize: 12 },
  chatHostName: { color: '#FF2A85', fontWeight: '900', fontSize: 12 },
  chatMessageText: { color: '#F8FAFC', fontSize: 12 },
  bottomBar: {
    position: 'absolute',
    bottom: 24,
    left: 14,
    right: 14,
    flexDirection: 'row',
    alignItems: 'center'
  },
  chatInput: {
    flex: 1,
    backgroundColor: 'rgba(15, 14, 38, 0.85)',
    color: '#FFF',
    borderRadius: 24,
    paddingHorizontal: 16,
    paddingVertical: 10,
    fontSize: 13,
    borderWidth: 1,
    borderColor: '#2E2B52'
  },
  sendBtn: {
    backgroundColor: '#00F0FF',
    borderRadius: 20,
    paddingHorizontal: 14,
    paddingVertical: 10,
    marginLeft: 8
  },
  sendBtnText: { color: '#090814', fontWeight: 'bold', fontSize: 12 },
  likeBtn: {
    backgroundColor: 'rgba(255, 42, 133, 0.25)',
    borderRadius: 20,
    paddingHorizontal: 12,
    paddingVertical: 8,
    marginLeft: 8,
    alignItems: 'center',
    borderWidth: 1,
    borderColor: '#FF2A85'
  },
  likeIcon: { fontSize: 18 },
  likeCount: { color: '#FFF', fontSize: 10, fontWeight: 'bold' },
  heartParticle: {
    position: 'absolute',
    bottom: 90,
    right: 40
  }
});
```
