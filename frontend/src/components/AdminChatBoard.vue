<template>
  <div class="admin-chat-page">
    <aside class="sidebar">
      <div class="brand">
        <img src="@/assets/logo_2.jpg" alt="Logo" class="logo-image" />
        <p>Trung tâm hỗ trợ</p>
      </div>

      <div class="quick-nav">
        <router-link to="/admin/dashboard" class="nav-btn"
          >📊 Dashboard</router-link
        >
        <router-link to="/admin/orders" class="nav-btn"
          >📦 Đơn hàng</router-link
        >
        <router-link to="/admin/chat" class="nav-btn active"
          >💬 Tương tác</router-link
        >
      </div>

      <button type="button" class="logout-btn" @click="handleLogout">
        Đăng xuất
      </button>

      <div class="chat-sidebar-section">
        <h3 class="section-title">Danh sách hỗ trợ</h3>
        <div class="room-list">
          <div 
            v-for="room in activeRooms" 
            :key="room.roomId"
            :class="['room-item', selectedRoomId === room.roomId ? 'active' : '']"
            @click="selectRoom(room)"
          >
            <div class="room-info">
              <span class="room-id">{{ room.customerName || room.roomId }}</span>
              <span class="room-time">{{ formatTime(room.lastActive) }}</span>
            </div>
            <div class="room-subtext">{{ room.roomId }}</div>
          </div>
          <div v-if="activeRooms.length === 0" class="no-rooms">
            Không có khách hàng nào đang yêu cầu hỗ trợ.
          </div>
        </div>
      </div>
    </aside>

    <main class="content">
      <header class="page-header">
        <div>
          <h1>Chat Board</h1>
          <p>Hỗ trợ trực tuyến và giải đáp khách hàng đang hoạt động.</p>
        </div>
      </header>

      <div class="card chat-card">
        <div v-if="selectedRoomId" class="chat-wrapper">
          <div class="chat-header">
            <h3>Đang hỗ trợ: {{ selectedCustomerName || selectedRoomId }}</h3>
            <span class="status-badge" :class="{ connected: connected }">
              {{ connected ? 'Connected' : 'Disconnected' }}
            </span>
          </div>
          
          <div class="messages" ref="messagesContainer">
            <div v-if="loadingHistory" class="loading">Đang tải dữ liệu...</div>
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
              placeholder="Nhập phản hồi của bạn..." 
              :disabled="!connected"
            />
            <button @click="sendMessage" :disabled="!connected || !newMessage.trim()">Gửi</button>
          </div>
        </div>
        
        <div v-else class="no-selection">
          <h3>Chọn một khách hàng từ danh sách bên trái để bắt đầu hỗ trợ.</h3>
        </div>
      </div>
    </main>
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
      selectedCustomerName: '',
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
    handleLogout() {
      localStorage.removeItem("admin");
      this.$router.push("/admin/login");
    },
    async fetchActiveRooms() {
      try {
        const response = await userAxios.get(API.chat.adminRooms());
        this.activeRooms = response.data;
      } catch (error) {
        console.error("Failed to load active rooms:", error);
      }
    },
    async selectRoom(room) {
      const roomId = room?.roomId;
      const customerName = room?.customerName || '';
      if (!roomId) return;
      if (this.selectedRoomId === roomId) return;
      
      // Disconnect from previous room if any
      this.disconnect();
      
      this.selectedRoomId = roomId;
      this.selectedCustomerName = customerName;
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
.admin-chat-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 280px 1fr;
  background: linear-gradient(140deg, #fff8fc 0%, #fff0f7 45%, #ffe6f2 100%);
  font-family: 'Be Vietnam Pro', sans-serif;
}

.sidebar {
  padding: 22px;
  border-right: 1px solid var(--pink-200);
  background: rgba(255, 255, 255, 0.86);
  display: flex;
  flex-direction: column;
  gap: 14px;
  height: 100vh;
  box-sizing: border-box;
}

.brand {
  display: grid;
  gap: 8px;
}

.logo-image {
  width: 180px;
  height: 54px;
  object-fit: cover;
  background: white;
  border-radius: 8px;
  padding: 6px;
}

.brand p {
  margin: 0;
  color: var(--pink-600);
  font-weight: 600;
}

.quick-nav {
  display: grid;
  gap: 8px;
}

.nav-btn {
  text-decoration: none;
  border: 1px solid var(--pink-300);
  border-radius: 10px;
  padding: 9px 12px;
  color: var(--pink-700);
  background: white;
  font-weight: 600;
  transition: all 0.2s;
}

.nav-btn.active,
.nav-btn:hover {
  background: var(--pink-100);
}

.logout-btn {
  border: 1px solid var(--pink-300);
  border-radius: 10px;
  padding: 10px 12px;
  font-weight: 600;
  cursor: pointer;
  color: #c53958;
  background: #fff;
  text-align: left;
  transition: all 0.2s;
}

.logout-btn:hover {
  background: #fff0f3;
}

/* Chat-specific sidebar additions */
.chat-sidebar-section {
  margin-top: 10px;
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.section-title {
  font-size: 1rem;
  color: var(--pink-700);
  margin-bottom: 15px;
  padding-bottom: 5px;
  border-bottom: 1px dashed var(--pink-200);
}

.room-list {
  flex: 1;
  overflow-y: auto;
  display: grid;
  gap: 8px;
  align-content: start;
}

.room-item {
  padding: 12px 15px;
  border-radius: 10px;
  border: 1px solid var(--pink-200);
  background: rgba(255, 255, 255, 0.8);
  cursor: pointer;
  transition: all 0.2s;
}

.room-item:hover {
  background: #fff;
  border-color: var(--pink-300);
}

.room-item.active {
  background-color: var(--pink-50, #fff6fb);
  border: 1px solid var(--pink-500);
  box-shadow: 0 0 0 1px var(--pink-100);
}

.room-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.room-id {
  font-weight: 600;
  color: var(--pink-800);
  font-size: 0.9rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 120px;
}

.room-time {
  font-size: 0.75rem;
  color: var(--pink-500);
}

.room-subtext {
  margin-top: 4px;
  font-size: 0.75rem;
  color: var(--pink-500);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.no-rooms {
  text-align: left;
  color: var(--pink-500);
  font-style: italic;
  font-size: 0.9rem;
  padding: 10px 5px;
}

/* Main Content Area */
.content {
  padding: 24px;
  display: flex;
  flex-direction: column;
  height: 100vh;
  box-sizing: border-box;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 16px;
  flex-shrink: 0;
}

.page-header h1 {
  margin: 0;
  color: var(--pink-700);
}

.page-header p {
  margin: 8px 0 0;
  color: var(--pink-500);
}

.card {
  border: 1px solid var(--pink-200);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 4px 15px rgba(236, 77, 139, 0.05); /* very subtle pink shadow */
}

.chat-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-wrapper {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.chat-header {
  padding: 15px 20px;
  border-bottom: 1px dashed var(--pink-200);
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: rgba(255, 255, 255, 0.5);
  flex-shrink: 0;
}

.chat-header h3 {
  margin: 0;
  color: var(--pink-700);
  font-size: 1.05rem;
}

.status-badge {
  padding: 5px 12px;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: bold;
  background-color: var(--peach-500, #ff9466);
  color: #fff;
}

.status-badge.connected {
  background-color: #4caf50;
}

.messages {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 15px;
  background: #fffafc; /* extremely subtle pink tint */
}

.message {
  max-width: 65%;
  padding: 12px 15px;
  border-radius: 15px;
  position: relative;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
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
  border: 1px solid var(--pink-100);
  border-bottom-left-radius: 2px;
}

.message-sender {
  font-size: 0.75rem;
  opacity: 0.9;
  margin-bottom: 5px;
  font-weight: 600;
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
  padding: 15px 20px;
  background-color: white;
  border-top: 1px dashed var(--pink-200);
  display: flex;
  gap: 10px;
  flex-shrink: 0;
}

.chat-input input {
  flex: 1;
  padding: 10px 18px;
  border: 1px solid var(--pink-300);
  border-radius: 20px;
  outline: none;
  font-size: 1rem;
  transition: border-color 0.2s, box-shadow 0.2s;
  color: var(--pink-800);
}

.chat-input input:focus {
  border-color: var(--pink-500);
  box-shadow: 0 0 0 2px var(--pink-100);
}

.chat-input button {
  background-color: var(--pink-600, #ec4d8b);
  color: white;
  border: none;
  border-radius: 20px;
  padding: 0 25px;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.2s;
}

.chat-input button:hover:not(:disabled) {
  background-color: var(--pink-700, #d63675);
}

.chat-input button:disabled {
  background-color: var(--pink-200);
  cursor: not-allowed;
}

.no-selection {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  color: var(--pink-500);
  font-size: 1.1rem;
}

.loading {
  text-align: center;
  color: var(--pink-500);
  padding: 10px;
  font-style: italic;
}

@media (max-width: 980px) {
  .admin-chat-page {
    grid-template-columns: 1fr;
    height: auto;
  }

  .sidebar {
    border-right: none;
    border-bottom: 1px solid var(--pink-200);
    height: auto;
  }
}
</style>
