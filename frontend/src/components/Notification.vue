<template>
  <div class="notification-container" v-if="notifications.length > 0">
    <div v-for="notif in notifications" :key="notif.id" class="notification-toast">
      <div class="notification-header">
        <strong>New message from {{ notif.sender }}</strong>
        <button class="close-btn" @click="removeNotification(notif.id)">&times;</button>
      </div>
      <div class="notification-body">
        {{ notif.content }}
      </div>
    </div>
  </div>
</template>

<script>
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
import API from '../config/api';

export default {
  name: 'GlobalNotification',
  data() {
    return {
      stompClient: null,
      notifications: [],
      // For a real app, this should only connect for logged in users (Admin/Customer)
      // and roomId should ideally filter out messages meant for the active room 
      // where the user is currently chatting.
      currentUser: localStorage.getItem('username') || 'Guest',
      activeRoomId: localStorage.getItem('roomId')
    };
  },
  mounted() {
    this.connect();
  },
  beforeUnmount() {
    this.disconnect();
  },
  methods: {
    connect() {
      const wsUrl = API.chat.wsUrl();
      
      this.stompClient = new Client({
        webSocketFactory: () => new SockJS(wsUrl),
        reconnectDelay: 10000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
      });

      this.stompClient.onConnect = () => {
        // Subscribe to global notifications
        this.stompClient.subscribe(`/topic/notifications`, (messageOutput) => {
          const msg = JSON.parse(messageOutput.body);
          
          // Don't notify if the message is sent by the current user
          if (msg.sender === this.currentUser) return;
          
          // Don't notify if the message is in the room the user is currently viewing/chatting in
          // This would ideally be handled via a vuex store or event bus, but for now we'll match localStorage
          if (msg.roomId === this.activeRoomId) {
             // Let Chat.vue handle this message if it's open.
             // If we really want smart notifications, we should check if Chat.vue is open.
             // For the sake of this feature, we'll still show a transient toast.
          }

          msg.id = Date.now() + Math.random();
          this.notifications.push(msg);
          
          // Auto remove after 5 seconds
          setTimeout(() => {
            this.removeNotification(msg.id);
          }, 5000);
        });
      };

      this.stompClient.activate();
    },
    disconnect() {
      if (this.stompClient) {
        this.stompClient.deactivate();
      }
    },
    removeNotification(id) {
      this.notifications = this.notifications.filter(n => n.id !== id);
    }
  }
};
</script>

<style scoped>
.notification-container {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 1050;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.notification-toast {
  background-color: white;
  border-left: 4px solid #007bff;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
  border-radius: 4px;
  width: 300px;
  overflow: hidden;
  animation: slideIn 0.3s ease-out;
}

.notification-header {
  padding: 10px 15px;
  background-color: #f8f9fa;
  border-bottom: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.close-btn {
  background: none;
  border: none;
  font-size: 18px;
  cursor: pointer;
  color: #999;
}

.close-btn:hover {
  color: #333;
}

.notification-body {
  padding: 15px;
  font-size: 14px;
  color: #333;
}

@keyframes slideIn {
  from {
    transform: translateX(100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}
</style>
