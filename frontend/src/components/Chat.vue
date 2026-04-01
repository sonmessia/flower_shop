<template>
  <div class="chat-container">
    <div class="chat-header" @click="toggleChat">
      <span>Chat with Support</span>
      <span v-if="unreadCount > 0" class="badge">{{ unreadCount }}</span>
      <i :class="isOpen ? 'arrow down' : 'arrow up'"></i>
    </div>
    
    <div v-if="isOpen" class="chat-body">
      <div class="messages" ref="messagesContainer">
        <div v-for="(msg, index) in messages" :key="index" :class="['message', msg.sender === currentUser ? 'sent' : 'received']">
          <div class="message-sender">{{ msg.sender }}</div>
          <div class="message-content">{{ msg.content }}</div>
          <div class="message-time">{{ formatTime(msg.timestamp) }}</div>
        </div>
      </div>
      
      <div v-if="connectionError" class="connection-error">
        Could not connect to chat server.
      </div>
      <div v-if="!connected && !connectionError" class="connecting">
        Connecting...
      </div>
      
      <div class="chat-input separator">
        <input 
          v-model="newMessage" 
          @keyup.enter="sendMessage" 
          placeholder="Type a message..." 
          :disabled="!connected"
        />
        <button @click="sendMessage" :disabled="!connected || !newMessage.trim()">Send</button>
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
  name: 'ChatWidget',
  data() {
    return {
      isOpen: false,
      connected: false,
      connectionError: false,
      stompClient: null,
      messages: [],
      newMessage: '',
      // For simplicity, generate a random ID if not logged in
      currentUser: localStorage.getItem('username') || `Guest_${Math.floor(Math.random() * 1000)}`,
      roomId: localStorage.getItem('roomId') || `room_${Math.floor(Math.random() * 1000)}`,
      unreadCount: 0
    };
  },
  mounted() {
    // If we want a persistent guest room per session, we can save it.
    if (!localStorage.getItem('roomId')) {
      localStorage.setItem('roomId', this.roomId);
    }
    if (!localStorage.getItem('username')) {
      localStorage.setItem('username', this.currentUser);
    }
    
    this.connect();
    this.fetchHistory();
  },
  beforeUnmount() {
    this.disconnect();
  },
  methods: {
    toggleChat() {
      this.isOpen = !this.isOpen;
      if (this.isOpen) {
        this.unreadCount = 0;
        this.scrollToBottom();
      }
    },
    async fetchHistory() {
      try {
        const response = await userAxios.get(API.chat.history(this.roomId));
        this.messages = response.data;
        this.scrollToBottom();
      } catch (error) {
        console.error("Failed to load chat history:", error);
      }
    },
    connect() {
      // Connect to the WebSocket without SockJS since we have STOMP, but we configure SockJS fallback if needed.
      const wsUrl = API.chat.wsUrl();
      
      this.stompClient = new Client({
        webSocketFactory: () => new SockJS(wsUrl),
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
      });

      this.stompClient.onConnect = (frame) => {
        this.connected = true;
        this.connectionError = false;
        console.log('Connected to Chat STOMP: ' + frame);
        
        // Subscribe to the private room
        this.stompClient.subscribe(`/topic/chat.${this.roomId}`, (messageOutput) => {
          const msg = JSON.parse(messageOutput.body);
          this.messages.push(msg);
          
          if (!this.isOpen) {
            this.unreadCount++;
          }
          
          this.scrollToBottom();
        });

        // Announce user joining
        this.stompClient.publish({
          destination: `/app/chat/${this.roomId}/addUser`,
          body: JSON.stringify({ sender: this.currentUser, type: 'JOIN' })
        });
      };

      this.stompClient.onStompError = (frame) => {
        console.error('Broker reported error: ' + frame.headers['message']);
        console.error('Additional details: ' + frame.body);
        this.connectionError = true;
      };
      
      this.stompClient.onWebSocketError = (event) => {
        console.error('WebSocket error:', event);
        this.connectionError = true;
      };

      this.stompClient.activate();
    },
    disconnect() {
      if (this.stompClient !== null) {
        this.stompClient.deactivate();
      }
      this.connected = false;
      console.log("Disconnected");
    },
    sendMessage() {
      if (this.newMessage.trim() !== '' && this.stompClient && this.stompClient.connected) {
        const messageBody = {
          sender: this.currentUser,
          content: this.newMessage,
          type: 'CHAT'
        };
        this.stompClient.publish({
          destination: `/app/chat/${this.roomId}/sendMessage`,
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
.chat-container {
  position: fixed;
  bottom: 20px;
  right: 20px;
  width: 350px;
  background-color: white;
  border-radius: 10px;
  box-shadow: 0 5px 15px rgba(0,0,0,0.2);
  display: flex;
  flex-direction: column;
  z-index: 1000;
  overflow: hidden;
}

.chat-header {
  background-color: #007bff;
  color: white;
  padding: 15px;
  font-weight: bold;
  cursor: pointer;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.badge {
  background-color: red;
  color: white;
  border-radius: 50%;
  padding: 2px 8px;
  font-size: 12px;
}

.arrow {
  border: solid white;
  border-width: 0 3px 3px 0;
  display: inline-block;
  padding: 3px;
}

.up { transform: rotate(-135deg); }
.down { transform: rotate(45deg); }

.chat-body {
  height: 400px;
  display: flex;
  flex-direction: column;
  background-color: #f9f9f9;
}

.messages {
  flex: 1;
  padding: 15px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.message {
  max-width: 80%;
  padding: 10px;
  border-radius: 10px;
  position: relative;
}

.message.sent {
  align-self: flex-end;
  background-color: #dcf8c6;
}

.message.received {
  align-self: flex-start;
  background-color: white;
  border: 1px solid #ddd;
}

.message-sender {
  font-size: 10px;
  color: #888;
  margin-bottom: 3px;
}

.message-content {
  font-size: 14px;
}

.message-time {
  font-size: 10px;
  color: #aaa;
  text-align: right;
  margin-top: 5px;
}

.chat-input {
  display: flex;
  padding: 10px;
  background-color: white;
  border-top: 1px solid #ddd;
}

.chat-input input {
  flex: 1;
  padding: 10px;
  border: 1px solid #ccc;
  border-radius: 20px;
  outline: none;
}

.chat-input button {
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 20px;
  padding: 0 15px;
  margin-left: 10px;
  cursor: pointer;
  font-weight: bold;
}

.chat-input button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.connection-error {
  color: red;
  font-size: 12px;
  text-align: center;
  padding: 5px;
  background: #ffeeee;
}

.connecting {
  color: #888;
  font-size: 12px;
  text-align: center;
  padding: 5px;
}
</style>
