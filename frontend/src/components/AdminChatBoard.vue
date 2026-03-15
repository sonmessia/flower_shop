<template>
  <div class="admin-chat-layout">
    <div class="sidebar">
      <div class="sidebar-header">
        <h2>Active Chats</h2>
      </div>
      <div class="room-list">
        <div 
          v-for="room in activeRooms" 
          :key="room.roomId"
          :class="['room-item', selectedRoomId === room.roomId ? 'active' : '']"
          @click="selectRoom(room.roomId)"
        >
          <div class="room-info">
            <span class="room-id">Customer: {{ room.roomId }}</span>
            <span class="room-time">{{ formatTime(room.lastActive) }}</span>
          </div>
        </div>
        <div v-if="activeRooms.length === 0" class="no-rooms">
          No active chats found.
        </div>
      </div>
    </div>
    
    <div class="main-chat-area">
      <div v-if="selectedRoomId" class="chat-wrapper">
        <div class="chat-header">
          <h3>Chatting with {{ selectedRoomId }}</h3>
          <span class="status-badge" :class="{ connected: connected }">
            {{ connected ? 'Connected' : 'Disconnected' }}
          </span>
        </div>
        
        <div class="messages" ref="messagesContainer">
          <div v-if="loadingHistory" class="loading">Loading history...</div>
          <div 
            v-for="(msg, index) in currentMessages" 
            :key="index" 
            :class="['message', msg.sender === adminName ? 'sent' : 'received']"
          >
            <div class="message-sender">{{ msg.sender }}</div>
            <div class="message-content">{{ msg.content }}</div>
            <div class="message-time">{{ formatTime(msg.timestamp) }}</div>
          </div>
        </div>
        
        <div class="chat-input separator">
          <input 
            v-model="newMessage" 
            @keyup.enter="sendMessage" 
            placeholder="Type a message to customer..." 
            :disabled="!connected"
          />
          <button @click="sendMessage" :disabled="!connected || !newMessage.trim()">Send</button>
        </div>
      </div>
      
      <div v-else class="no-selection">
        <h3>Select a chat room from the sidebar to start messaging.</h3>
      </div>
    </div>
  </div>
</template>

<script>
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
import userAxios from '../config/userAxios';
import API from '../config/api';

export default {
  name: 'AdminChatBoard',
  data() {
    return {
      activeRooms: [],
      selectedRoomId: null,
      currentMessages: [],
      newMessage: '',
      connected: false,
      stompClient: null,
      pollingInterval: null,
      loadingHistory: false,
      // For Admins
      adminName: 'Admin'
    };
  },
  mounted() {
    const adminData = localStorage.getItem('admin');
    if (adminData) {
      try {
        const parsed = JSON.parse(adminData);
        if (parsed.username) this.adminName = parsed.username;
      } catch(e) {
        // use default Admin
      }
    }
    
    this.fetchActiveRooms();
    // Poll for new active rooms every 30 seconds
    this.pollingInterval = setInterval(() => {
      this.fetchActiveRooms();
    }, 30000);
  },
  beforeUnmount() {
    if (this.pollingInterval) clearInterval(this.pollingInterval);
    this.disconnect();
  },
  methods: {
    async fetchActiveRooms() {
      try {
        const response = await userAxios.get(API.chat.adminRooms());
        this.activeRooms = response.data;
      } catch (error) {
        console.error("Failed to load active rooms:", error);
      }
    },
    async selectRoom(roomId) {
      if (this.selectedRoomId === roomId) return;
      
      // Disconnect from previous room if any
      this.disconnect();
      
      this.selectedRoomId = roomId;
      this.currentMessages = [];
      this.loadingHistory = true;
      
      try {
        const response = await userAxios.get(API.chat.history(roomId));
        this.currentMessages = response.data;
        this.scrollToBottom();
      } catch (error) {
        console.error("Failed to fetch history for room:", error);
      } finally {
        this.loadingHistory = false;
      }
      
      this.connect();
    },
    connect() {
      if (!this.selectedRoomId) return;
      
      const wsUrl = API.chat.wsUrl();
      
      this.stompClient = new Client({
        webSocketFactory: () => new SockJS(wsUrl),
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
      });

      this.stompClient.onConnect = () => {
        this.connected = true;
        
        // Subscribe to the chosen room
        this.stompClient.subscribe(`/topic/chat.${this.selectedRoomId}`, (msgOutput) => {
          const msg = JSON.parse(msgOutput.body);
          this.currentMessages.push(msg);
          this.scrollToBottom();
        });
        
        // Announce admin joined
        this.stompClient.publish({
          destination: `/app/chat/${this.selectedRoomId}/addUser`,
          body: JSON.stringify({ sender: this.adminName, type: 'JOIN' })
        });
      };

      this.stompClient.onStompError = (frame) => {
        console.error('STOMP Error:', frame);
        this.connected = false;
      };

      this.stompClient.activate();
    },
    disconnect() {
      if (this.stompClient !== null && this.stompClient.active) {
        this.stompClient.deactivate();
      }
      this.connected = false;
    },
    sendMessage() {
      if (this.newMessage.trim() && this.connected && this.stompClient) {
        const messageBody = {
          sender: this.adminName,
          content: this.newMessage,
          type: 'CHAT'
        };
        this.stompClient.publish({
          destination: `/app/chat/${this.selectedRoomId}/sendMessage`,
          body: JSON.stringify(messageBody)
        });
        this.newMessage = '';
      }
    },
    scrollToBottom() {
      this.$nextTick(() => {
        if (this.$refs.messagesContainer) {
          this.$refs.messagesContainer.scrollTop = this.$refs.messagesContainer.scrollHeight;
        }
      });
    },
    formatTime(timestamp) {
      if (!timestamp) return '';
      const date = new Date(timestamp);
      return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    }
  }
};
</script>

<style scoped>
.admin-chat-layout {
  display: flex;
  height: calc(100vh - 64px); 
  background-color: #f4f6f9;
  font-family: 'Be Vietnam Pro', sans-serif;
  margin-top: 64px; /* Assuming a top navbar exists, adjust if needed */
}

/* Sidebar Styles */
.sidebar {
  width: 300px;
  background-color: #ffffff;
  border-right: 1px solid #e0e0e0;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 20px;
  background-color: var(--pink-600, #ec4d8b);
  color: white;
}

.sidebar-header h2 {
  margin: 0;
  font-size: 1.2rem;
}

.room-list {
  flex: 1;
  overflow-y: auto;
}

.room-item {
  padding: 15px 20px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background-color 0.2s;
}

.room-item:hover {
  background-color: #f9f9f9;
}

.room-item.active {
  background-color: var(--pink-50, #fff6fb);
  border-left: 4px solid var(--pink-600, #ec4d8b);
}

.room-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.room-id {
  font-weight: 600;
  color: #333;
  font-size: 0.9rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 180px;
}

.room-time {
  font-size: 0.75rem;
  color: #888;
}

.no-rooms {
  padding: 20px;
  text-align: center;
  color: #888;
  font-style: italic;
}

/* Main Chat Area Styles */
.main-chat-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  background-color: #f4f6f9;
}

.chat-wrapper {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.chat-header {
  padding: 20px;
  background-color: white;
  border-bottom: 1px solid #e0e0e0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chat-header h3 {
  margin: 0;
  color: #333;
}

.status-badge {
  padding: 5px 10px;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: bold;
  background-color: #ffcc00;
  color: #fff;
}

.status-badge.connected {
  background-color: #28a745;
}

.messages {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.message {
  max-width: 60%;
  padding: 12px 15px;
  border-radius: 15px;
  position: relative;
  box-shadow: 0 1px 2px rgba(0,0,0,0.1);
}

.message.sent {
  align-self: flex-end;
  background-color: var(--pink-500, #f36da1);
  color: white;
  border-bottom-right-radius: 2px;
}

.message.received {
  align-self: flex-start;
  background-color: white;
  color: #333;
  border-bottom-left-radius: 2px;
}

.message-sender {
  font-size: 0.7rem;
  opacity: 0.8;
  margin-bottom: 5px;
  font-weight: bold;
}

.message-content {
  font-size: 0.95rem;
  line-height: 1.4;
}

.message-time {
  font-size: 0.7rem;
  opacity: 0.7;
  text-align: right;
  margin-top: 5px;
}

.chat-input {
  padding: 20px;
  background-color: white;
  border-top: 1px solid #e0e0e0;
  display: flex;
  gap: 10px;
}

.chat-input input {
  flex: 1;
  padding: 12px 20px;
  border: 1px solid #ddd;
  border-radius: 25px;
  outline: none;
  font-size: 1rem;
  transition: border-color 0.2s;
}

.chat-input input:focus {
  border-color: var(--pink-500, #f36da1);
}

.chat-input button {
  background-color: var(--pink-600, #ec4d8b);
  color: white;
  border: none;
  border-radius: 25px;
  padding: 0 25px;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.2s;
}

.chat-input button:hover:not(:disabled) {
  background-color: var(--pink-700, #d63675);
}

.chat-input button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.no-selection {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  color: #888;
}

.loading {
  text-align: center;
  color: #888;
  padding: 10px;
  font-style: italic;
}
</style>
