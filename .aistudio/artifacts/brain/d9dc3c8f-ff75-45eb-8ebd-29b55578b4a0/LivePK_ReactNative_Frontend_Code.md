# LivePK — React Native Cross-Platform Frontend Starter Code

This codebase powers the **iOS and Android** client app for **LivePK**, featuring TikTok-style vertical short video feeds, Google & Email authentication, and real-time live broadcasting with Agora.io.

---

### 1. `mobile/package.json`
```json
{
  "name": "LivePK",
  "version": "1.0.0",
  "scripts": {
    "android": "react-native run-android",
    "ios": "react-native run-ios",
    "start": "react-native start"
  },
  "dependencies": {
    "@react-native-async-storage/async-storage": "^1.23.1",
    "@react-native-google-signin/google-signin": "^11.0.1",
    "@react-navigation/bottom-tabs": "^6.5.20",
    "@react-navigation/native": "^6.1.17",
    "@react-navigation/native-stack": "^6.9.26",
    "axios": "^1.7.2",
    "react": "18.2.0",
    "react-native": "0.74.2",
    "react-native-agora": "^4.3.2",
    "react-native-fast-image": "^8.6.3",
    "react-native-gesture-handler": "^2.16.2",
    "react-native-reanimated": "^3.12.0",
    "react-native-safe-area-context": "^4.10.1",
    "react-native-screens": "^3.31.1",
    "react-native-vector-icons": "^10.1.0",
    "react-native-video": "^6.0.0",
    "socket.io-client": "^4.7.5"
  }
}
```

---

### 2. Login Screen (`src/screens/Auth/LoginScreen.js`)
```javascript
import React, { useState } from 'react';
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  StyleSheet,
  ActivityIndicator,
  Alert,
  Image
} from 'react-native';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { GoogleSignin } from '@react-native-google-signin/google-signin';
import axios from 'axios';

// Backend API Base URL
const API_URL = 'http://localhost:5000/api/auth';

export default function LoginScreen({ navigation }) {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);

  const handleEmailLogin = async () => {
    if (!email || !password) {
      Alert.alert('Required', 'Please enter your email and password');
      return;
    }
    setLoading(true);
    try {
      const res = await axios.post(`${API_URL}/login`, { email, password });
      await AsyncStorage.setItem('userToken', res.data.token);
      await AsyncStorage.setItem('userData', JSON.stringify(res.data.user));
      navigation.replace('MainTabs');
    } catch (err) {
      Alert.alert('Login Failed', err.response?.data?.error || 'Invalid credentials');
    } finally {
      setLoading(false);
    }
  };

  const handleGoogleLogin = async () => {
    try {
      await GoogleSignin.hasPlayServices();
      const userInfo = await GoogleSignin.signIn();
      setLoading(true);
      const res = await axios.post(`${API_URL}/google`, { idToken: userInfo.idToken });
      await AsyncStorage.setItem('userToken', res.data.token);
      await AsyncStorage.setItem('userData', JSON.stringify(res.data.user));
      navigation.replace('MainTabs');
    } catch (err) {
      Alert.alert('Google Sign-In Error', err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <View style={styles.container}>
      {/* Brand Header */}
      <View style={styles.header}>
        <Text style={styles.logoTitle}>
          Live<Text style={styles.logoHighlight}>PK</Text>
        </Text>
        <Text style={styles.subtitle}>Stream, Connect & Go Live Globally</Text>
      </View>

      {/* Form Fields */}
      <View style={styles.form}>
        <TextInput
          style={styles.input}
          placeholder="Email address"
          placeholderTextColor="#64748B"
          value={email}
          onChangeText={setEmail}
          autoCapitalize="none"
          keyboardType="email-address"
        />

        <TextInput
          style={styles.input}
          placeholder="Password"
          placeholderTextColor="#64748B"
          value={password}
          onChangeText={setPassword}
          secureTextEntry
        />

        <TouchableOpacity style={styles.loginBtn} onPress={handleEmailLogin} disabled={loading}>
          {loading ? (
            <ActivityIndicator color="#fff" />
          ) : (
            <Text style={styles.loginBtnText}>Log In</Text>
          )}
        </TouchableOpacity>

        {/* Divider */}
        <View style={styles.dividerRow}>
          <View style={styles.dividerLine} />
          <Text style={styles.dividerText}>OR</Text>
          <View style={styles.dividerLine} />
        </View>

        {/* Google OAuth Button */}
        <TouchableOpacity style={styles.googleBtn} onPress={handleGoogleLogin}>
          <Text style={styles.googleBtnText}>Continue with Google 🚀</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#090814', justifyContent: 'center', padding: 24 },
  header: { alignItems: 'center', marginBottom: 36 },
  logoTitle: { fontSize: 38, fontWeight: '900', color: '#F8FAFC' },
  logoHighlight: { color: '#FF2A85' },
  subtitle: { fontSize: 14, color: '#00F0FF', marginTop: 6 },
  form: { width: '100%' },
  input: {
    backgroundColor: '#1E1B38',
    borderRadius: 14,
    color: '#FFF',
    paddingHorizontal: 16,
    paddingVertical: 14,
    marginBottom: 14,
    borderWidth: 1,
    borderColor: '#2E2B52'
  },
  loginBtn: {
    backgroundColor: '#FF2A85',
    borderRadius: 14,
    paddingVertical: 15,
    alignItems: 'center',
    marginTop: 8
  },
  loginBtnText: { color: '#FFF', fontWeight: 'bold', fontSize: 16 },
  dividerRow: { flexDirection: 'row', alignItems: 'center', marginVertical: 20 },
  dividerLine: { flex: 1, height: 1, backgroundColor: '#2E2B52' },
  dividerText: { color: '#64748B', marginHorizontal: 12, fontSize: 12 },
  googleBtn: {
    backgroundColor: '#131127',
    borderRadius: 14,
    paddingVertical: 15,
    alignItems: 'center',
    borderWidth: 1,
    borderColor: '#3730A3'
  },
  googleBtnText: { color: '#F8FAFC', fontWeight: '600', fontSize: 15 }
});
```

---

### 3. TikTok-Style For You Feed (`src/screens/Feed/ForYouFeedScreen.js`)
```javascript
import React, { useState, useRef } from 'react';
import {
  View,
  Text,
  FlatList,
  Dimensions,
  StyleSheet,
  TouchableOpacity,
  Image,
  Share
} from 'react-native';
import Video from 'react-native-video';

const { height, width } = Dimensions.get('window');

const SAMPLE_FEED_VIDEOS = [
  {
    id: '1',
    videoUrl: 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4',
    creatorName: '@maya_gamer',
    caption: 'Neon Battlegrounds Grand Finals Highlight! Clutch 1v3 🔥 #LivePK #gaming',
    musicTitle: 'Cyber Trap Beat - Maya Plays',
    likes: '142.5K',
    comments: '3,840',
    shares: '820',
    avatar: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100'
  },
  {
    id: '2',
    videoUrl: 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4',
    creatorName: '@tokyo_kairo',
    caption: 'Midnight Tokyo Shibuya Synthwave vibes 🌆 Tune in to my live tonight!',
    musicTitle: 'Midnight Drive - Kairo',
    likes: '89.1K',
    comments: '1,210',
    shares: '450',
    avatar: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=100'
  }
];

export default function ForYouFeedScreen({ navigation }) {
  const [currentIndex, setCurrentIndex] = useState(0);
  const [likedMap, setLikedMap] = useState({});

  const toggleLike = (id) => {
    setLikedMap((prev) => ({ ...prev, [id]: !prev[id] }));
  };

  const handleShare = async (item) => {
    try {
      await Share.share({
        message: `Watch ${item.creatorName} on LivePK: ${item.caption}`
      });
    } catch (e) {
      console.log(e);
    }
  };

  const renderItem = ({ item, index }) => {
    const isPlaying = currentIndex === index;
    const isLiked = likedMap[item.id];

    return (
      <View style={styles.videoContainer}>
        {/* Full-screen Video Player */}
        <Video
          source={{ uri: item.videoUrl }}
          style={styles.fullVideo}
          resizeMode="cover"
          repeat
          paused={!isPlaying}
          muted={false}
        />

        {/* Right Interaction Sidebar */}
        <View style={styles.sidebar}>
          {/* Creator Avatar with Follow plus badge */}
          <TouchableOpacity style={styles.avatarWrap}>
            <Image source={{ uri: item.avatar }} style={styles.avatarImg} />
            <View style={styles.followBadge}>
              <Text style={styles.plusIcon}>+</Text>
            </View>
          </TouchableOpacity>

          {/* Like Button */}
          <TouchableOpacity style={styles.sideBtn} onPress={() => toggleLike(item.id)}>
            <Text style={[styles.sideIcon, isLiked && { color: '#FF2A85' }]}>
              {isLiked ? '❤️' : '🤍'}
            </Text>
            <Text style={styles.sideLabel}>{item.likes}</Text>
          </TouchableOpacity>

          {/* Comment Button */}
          <TouchableOpacity style={styles.sideBtn}>
            <Text style={styles.sideIcon}>💬</Text>
            <Text style={styles.sideLabel}>{item.comments}</Text>
          </TouchableOpacity>

          {/* Share Button */}
          <TouchableOpacity style={styles.sideBtn} onPress={() => handleShare(item)}>
            <Text style={styles.sideIcon}>↗️</Text>
            <Text style={styles.sideLabel}>{item.shares}</Text>
          </TouchableOpacity>
        </View>

        {/* Bottom Details Overlay */}
        <View style={styles.bottomInfo}>
          <Text style={styles.creatorTag}>{item.creatorName}</Text>
          <Text style={styles.captionText}>{item.caption}</Text>
          <Text style={styles.musicRow}>🎵 {item.musicTitle}</Text>
        </View>
      </View>
    );
  };

  return (
    <View style={styles.container}>
      <FlatList
        data={SAMPLE_FEED_VIDEOS}
        renderItem={renderItem}
        keyExtractor={(item) => item.id}
        pagingEnabled
        showsVerticalScrollIndicator={false}
        onMomentumScrollEnd={(e) => {
          const newIndex = Math.round(e.nativeEvent.contentOffset.y / height);
          setCurrentIndex(newIndex);
        }}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#000' },
  videoContainer: { width, height },
  fullVideo: { width, height, position: 'absolute' },
  sidebar: {
    position: 'absolute',
    right: 12,
    bottom: 110,
    alignItems: 'center'
  },
  avatarWrap: { marginBottom: 20, alignItems: 'center' },
  avatarImg: { width: 48, height: 48, borderRadius: 24, borderWidth: 2, borderColor: '#FF2A85' },
  followBadge: {
    position: 'absolute',
    bottom: -6,
    backgroundColor: '#FF2A85',
    borderRadius: 10,
    width: 20,
    height: 20,
    justifyContent: 'center',
    alignItems: 'center'
  },
  plusIcon: { color: '#fff', fontSize: 13, fontWeight: 'bold' },
  sideBtn: { alignItems: 'center', marginBottom: 18 },
  sideIcon: { fontSize: 28 },
  sideLabel: { color: '#fff', fontSize: 12, fontWeight: '600', marginTop: 4 },
  bottomInfo: {
    position: 'absolute',
    bottom: 30,
    left: 14,
    right: 80
  },
  creatorTag: { color: '#fff', fontWeight: 'bold', fontSize: 16, marginBottom: 6 },
  captionText: { color: '#E2E8F0', fontSize: 14, lineHeight: 18, marginBottom: 8 },
  musicRow: { color: '#00F0FF', fontSize: 13, fontWeight: '600' }
});
```

---

### 4. "Go Live" Broadcaster Screen (`src/screens/Live/GoLiveScreen.js`)
```javascript
import React, { useState, useEffect, useRef } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
  TextInput,
  FlatList,
  Alert
} from 'react-native';
import {
  createAgoraRtcEngine,
  ChannelProfileType,
  ClientRoleType,
  RtcSurfaceView
} from 'react-native-agora';
import io from 'socket.io-client';

const SOCKET_SERVER_URL = 'http://localhost:5000';

export default function GoLiveScreen({ navigation }) {
  const [isLive, setIsLive] = useState(false);
  const [viewerCount, setViewerCount] = useState(0);
  const [channelName, setChannelName] = useState('live_studio_pk');
  const [chatMessages, setChatMessages] = useState([
    { id: '1', senderName: 'LivePK_Bot', text: 'You are live! Engage your audience 🎉' }
  ]);
  const [inputMsg, setInputMsg] = useState('');
  const [filterMode, setFilterMode] = useState('Cyber');

  const agoraEngineRef = useRef(null);
  const socketRef = useRef(null);

  useEffect(() => {
    initAgora();
    initSocket();
    return () => {
      agoraEngineRef.current?.leaveChannel();
      socketRef.current?.disconnect();
    };
  }, []);

  const initAgora = async () => {
    try {
      const engine = createAgoraRtcEngine();
      agoraEngineRef.current = engine;
      engine.initialize({ appId: 'YOUR_AGORA_APP_ID' });

      engine.setChannelProfile(ChannelProfileType.ChannelProfileLiveBroadcasting);
      engine.setClientRole(ClientRoleType.ClientRoleBroadcaster);
      engine.enableVideo();
      engine.startPreview();
    } catch (e) {
      console.error('Agora Init Error', e);
    }
  };

  const initSocket = () => {
    const socket = io(SOCKET_SERVER_URL);
    socketRef.current = socket;

    socket.on('viewer_count_update', ({ count }) => {
      setViewerCount(count);
    });

    socket.on('new_chat', (msg) => {
      setChatMessages((prev) => [...prev, msg]);
    });

    socket.on('new_gift', ({ senderName, giftName, iconEmoji }) => {
      setChatMessages((prev) => [
        ...prev,
        { id: Date.now().toString(), senderName: 'GIFT 🎁', text: `${senderName} sent ${giftName} ${iconEmoji}!` }
      ]);
    });
  };

  const handleStartBroadcast = async () => {
    try {
      // In production, fetch dynamic token from /api/live/rtc-token
      agoraEngineRef.current?.joinChannel('', channelName, 0, {});
      socketRef.current?.emit('join_room', { channelName, username: 'Host' });
      setIsLive(true);
    } catch (err) {
      Alert.alert('Broadcast Error', err.message);
    }
  };

  const handleEndBroadcast = () => {
    agoraEngineRef.current?.leaveChannel();
    socketRef.current?.emit('leave_room', { channelName });
    setIsLive(false);
    navigation.goBack();
  };

  const handleSendHostChat = () => {
    if (!inputMsg.trim()) return;
    socketRef.current?.emit('send_chat', {
      channelName,
      text: inputMsg,
      isHost: true
    });
    setInputMsg('');
  };

  return (
    <View style={styles.container}>
      {/* Agora Local Camera Broadcast View */}
      <RtcSurfaceView
        style={styles.cameraView}
        canvas={{ uid: 0 }}
      />

      {/* Top Streamer Telemetry Header */}
      <View style={styles.topHud}>
        <View style={styles.liveBadge}>
          <Text style={styles.liveText}>{isLive ? '🔴 LIVE' : 'PREVIEW'}</Text>
        </View>
        <Text style={styles.viewerBadge}>👁️ {viewerCount} Viewers</Text>
        <TouchableOpacity style={styles.filterBtn} onPress={() => setFilterMode(filterMode === 'Cyber' ? 'Glow' : 'Cyber')}>
          <Text style={styles.filterText}>✨ {filterMode}</Text>
        </TouchableOpacity>
      </View>

      {/* Live Chat Overlay */}
      <View style={styles.chatSection}>
        <FlatList
          data={chatMessages}
          keyExtractor={(item) => item.id}
          renderItem={({ item }) => (
            <View style={styles.chatBubble}>
              <Text style={styles.chatSender}>{item.senderName}: </Text>
              <Text style={styles.chatText}>{item.text}</Text>
            </View>
          )}
        />
      </View>

      {/* Bottom Action Bar */}
      <View style={styles.bottomBar}>
        {!isLive ? (
          <TouchableOpacity style={styles.goLiveBtn} onPress={handleStartBroadcast}>
            <Text style={styles.goLiveText}>START LIVE STREAM 🚀</Text>
          </TouchableOpacity>
        ) : (
          <View style={styles.liveActionRow}>
            <TextInput
              style={styles.chatInput}
              placeholder="Host chat..."
              placeholderTextColor="#94A3B8"
              value={inputMsg}
              onChangeText={setInputMsg}
            />
            <TouchableOpacity style={styles.sendBtn} onPress={handleSendHostChat}>
              <Text style={styles.sendBtnText}>Send</Text>
            </TouchableOpacity>
            <TouchableOpacity style={styles.endBtn} onPress={handleEndBroadcast}>
              <Text style={styles.endBtnText}>End</Text>
            </TouchableOpacity>
          </View>
        )}
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#000' },
  cameraView: { ...StyleSheet.absoluteFillObject },
  topHud: {
    position: 'absolute',
    top: 50,
    left: 16,
    right: 16,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center'
  },
  liveBadge: { backgroundColor: '#FF1744', paddingHorizontal: 12, paddingVertical: 6, borderRadius: 12 },
  liveText: { color: '#FFF', fontWeight: 'bold', fontSize: 12 },
  viewerBadge: { backgroundColor: 'rgba(0,0,0,0.6)', color: '#FFF', paddingHorizontal: 10, paddingVertical: 6, borderRadius: 12, fontWeight: 'bold' },
  filterBtn: { backgroundColor: 'rgba(139,92,246,0.8)', paddingHorizontal: 10, paddingVertical: 6, borderRadius: 12 },
  filterText: { color: '#FFF', fontWeight: '600' },
  chatSection: {
    position: 'absolute',
    bottom: 90,
    left: 14,
    width: '75%',
    maxHeight: 200
  },
  chatBubble: {
    flexDirection: 'row',
    backgroundColor: 'rgba(19,17,39,0.75)',
    borderRadius: 8,
    paddingHorizontal: 8,
    paddingVertical: 4,
    marginBottom: 4
  },
  chatSender: { color: '#FF2A85', fontWeight: 'bold', fontSize: 12 },
  chatText: { color: '#F8FAFC', fontSize: 12 },
  bottomBar: { position: 'absolute', bottom: 25, left: 16, right: 16 },
  goLiveBtn: { backgroundColor: '#FF2A85', paddingVertical: 16, borderRadius: 28, alignItems: 'center' },
  goLiveText: { color: '#FFF', fontWeight: '900', fontSize: 16 },
  liveActionRow: { flexDirection: 'row', alignItems: 'center' },
  chatInput: { flex: 1, backgroundColor: 'rgba(0,0,0,0.6)', borderRadius: 20, color: '#fff', paddingHorizontal: 14, paddingVertical: 8, borderWidth: 1, borderColor: '#334155' },
  sendBtn: { backgroundColor: '#00F0FF', borderRadius: 20, paddingHorizontal: 14, paddingVertical: 10, marginLeft: 8 },
  sendBtnText: { color: '#090814', fontWeight: 'bold' },
  endBtn: { backgroundColor: '#FF1744', borderRadius: 20, paddingHorizontal: 14, paddingVertical: 10, marginLeft: 8 },
  endBtnText: { color: '#FFF', fontWeight: 'bold' }
});
```
