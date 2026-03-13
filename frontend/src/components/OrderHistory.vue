<template>
  <div class="order-history-page">
    <SiteNavbar @search="noop" />

    <main class="history-container">
      <header class="page-header">
        <h1>Lịch sử đơn hàng</h1>
        <router-link to="/cart" class="to-cart">Quay lại giỏ hàng</router-link>
      </header>

      <transition name="fade">
        <p v-if="success" class="feedback success">{{ success }}</p>
      </transition>

      <transition name="fade">
        <p v-if="error" class="feedback error">{{ error }}</p>
      </transition>

      <div v-if="loading" class="loading-block">
        <div class="spinner"></div>
        <span>Đang tải danh sách đơn hàng...</span>
      </div>

      <section v-else-if="orders.length === 0" class="empty-orders">
        <h2>Bạn chưa có đơn hàng nào</h2>
        <router-link to="/" class="cta-btn">Bắt đầu mua sắm</router-link>
      </section>

      <section v-else class="order-list">
        <article v-for="order in orders" :key="order.id" class="order-card">
          <header class="order-header">
            <div>
              <h2>Đơn #{{ order.id }}</h2>
              <p>{{ formatDate(order.createdAt) }}</p>
            </div>
            <div class="order-meta">
              <span class="status" :class="statusClass(order.status)">{{ statusLabel(order.status) }}</span>
              <strong>{{ formatPrice(order.totalAmount) }}</strong>
            </div>
          </header>

          <div class="ship-info">
            <p><strong>Người nhận:</strong> {{ order.shippingName }}</p>
            <p v-if="order.shippingPhone"><strong>SĐT:</strong> {{ order.shippingPhone }}</p>
            <p v-if="order.shippingStreet || order.shippingCity">
              <strong>Địa chỉ:</strong>
              {{ [order.shippingStreet, order.shippingCity, order.shippingPostalCode].filter(Boolean).join(", ") }}
            </p>
            <p v-if="order.cancellationMessage" class="cancel-message">
              <strong>Thông báo:</strong> {{ order.cancellationMessage }}
            </p>
            <p v-if="order.cancellationBy" class="cancel-by">
              <strong>Hủy bởi:</strong> {{ order.cancellationBy }}
            </p>
          </div>

          <div class="order-actions">
            <button
              v-if="canUserCancel(order)"
              class="cancel-btn"
              :disabled="cancellingOrderId === order.id"
              @click="cancelOrder(order)"
            >
              {{ cancellingOrderId === order.id ? "Đang hủy..." : "Hủy đơn" }}
            </button>
          </div>

          <div class="items">
            <div v-for="item in order.items" :key="item.id" class="item-row">
              <img :src="item.imageUrl || fallbackImage" :alt="item.productName" @error="onImageError" />
              <div>
                <router-link :to="`/products/${item.productId}`" class="name">{{ item.productName }}</router-link>
                <p class="meta">{{ item.quantity }} x {{ formatPrice(item.unitPrice) }}</p>
              </div>
              <strong>{{ formatPrice(item.lineTotal) }}</strong>
            </div>
          </div>
        </article>
      </section>
    </main>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { useRoute } from "vue-router";
import SiteNavbar from "./SiteNavbar.vue";
import API from "../config/api";
import userAxios from "../config/userAxios";

const route = useRoute();
const fallbackImage = "https://via.placeholder.com/300x300/FFE1F0/F36DA1?text=Flower";

const loading = ref(true);
const error = ref("");
const success = ref("");
const orders = ref([]);
const cancellingOrderId = ref(null);

const noop = () => {};

const parseError = (err, fallback = "Không thể tải đơn hàng.") => {
  const fields = err.response?.data?.fields;
  if (fields && typeof fields === "object") return Object.values(fields).join(", ");
  if (err.response?.data?.message) return err.response.data.message;
  return fallback;
};

const loadOrders = async () => {
  loading.value = true;
  error.value = "";

  try {
    const { data } = await userAxios.get(API.orders.mine());
    orders.value = data || [];
  } catch (err) {
    error.value = parseError(err);
  } finally {
    loading.value = false;
  }
};

const formatPrice = (price) =>
  new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
  }).format(Number(price) || 0);

const formatDate = (value) => {
  if (!value) return "";
  return new Date(value).toLocaleString("vi-VN", {
    dateStyle: "medium",
    timeStyle: "short",
  });
};

const statusLabel = (status) => {
  const map = {
    PENDING: "Chờ xác nhận",
    CONFIRMED: "Đã xác nhận",
    PROCESSING: "Đang xử lý",
    SHIPPED: "Đang giao",
    COMPLETED: "Hoàn tất",
    CANCELLED: "Đã hủy",
  };
  return map[status] || status;
};

const statusClass = (status) => status?.toLowerCase() || "pending";

const onImageError = (event) => {
  event.target.src = fallbackImage;
};

const canUserCancel = (order) => {
  return order.status === "PENDING" || order.status === "CONFIRMED";
};

const cancelOrder = async (order) => {
  if (!canUserCancel(order)) return;
  if (!window.confirm(`Bạn có chắc muốn hủy đơn #${order.id}?`)) return;

  const reason = window.prompt("Vui lòng nhập lý do hủy đơn:", "");
  if (reason === null) return;
  if (!reason.trim()) {
    error.value = "Vui lòng nhập lý do hủy đơn.";
    return;
  }

  cancellingOrderId.value = order.id;
  error.value = "";
  success.value = "";

  try {
    const { data } = await userAxios.put(API.orders.cancelMine(order.id), {
      cancelReason: reason.trim(),
    });
    const idx = orders.value.findIndex((item) => item.id === order.id);
    if (idx !== -1) {
      orders.value[idx] = data;
    }
    success.value = `Đã hủy đơn #${order.id}.`;
  } catch (err) {
    error.value = parseError(err, "Không thể hủy đơn hàng.");
  } finally {
    cancellingOrderId.value = null;
  }
};

onMounted(async () => {
  if (route.query.created) {
    success.value = `Đơn #${route.query.created} đã được tạo thành công.`;
  }
  await loadOrders();
});
</script>

<style scoped>
.order-history-page {
  min-height: 100vh;
}

.history-container {
  max-width: 980px;
  margin: 0 auto;
  padding: 28px 16px 54px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
}

.page-header h1 {
  margin: 0;
  color: var(--pink-700);
}

.to-cart {
  text-decoration: none;
  border: 1px solid var(--pink-300);
  border-radius: 12px;
  padding: 8px 12px;
  color: var(--pink-700);
  font-weight: 600;
  background: white;
}

.loading-block,
.empty-orders {
  text-align: center;
  border: 1px solid var(--pink-200);
  background: rgba(255, 255, 255, 0.9);
  border-radius: 18px;
  padding: 28px;
}

.cta-btn {
  display: inline-block;
  margin-top: 12px;
  text-decoration: none;
  color: white;
  background: linear-gradient(135deg, var(--pink-500), var(--pink-400));
  padding: 10px 18px;
  border-radius: 12px;
  font-weight: 600;
}

.spinner {
  width: 28px;
  height: 28px;
  border: 3px solid var(--pink-200);
  border-top-color: var(--pink-500);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin: 0 auto 10px;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.order-list {
  display: grid;
  gap: 12px;
}

.order-card {
  border: 1px solid var(--pink-200);
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  padding: 14px;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 8px;
  border-bottom: 1px dashed var(--pink-200);
  padding-bottom: 10px;
}

.order-header h2 {
  margin: 0;
  color: var(--pink-700);
  font-size: 1.1rem;
}

.order-header p {
  margin: 6px 0 0;
  color: var(--pink-500);
  font-size: 0.9rem;
}

.order-meta {
  text-align: right;
  display: grid;
  gap: 6px;
  color: var(--pink-700);
}

.status {
  display: inline-flex;
  justify-content: center;
  border-radius: 999px;
  padding: 4px 10px;
  font-size: 0.8rem;
  font-weight: 700;
  border: 1px solid transparent;
}

.status.pending {
  background: #fff7e8;
  border-color: #ffcd7e;
  color: #b87200;
}

.status.confirmed,
.status.processing,
.status.shipped {
  background: #eef4ff;
  border-color: #a9c5ff;
  color: #295fb8;
}

.status.completed {
  background: #eafaf0;
  border-color: #92dfaf;
  color: #1f8a4c;
}

.status.cancelled {
  background: #ffeef0;
  border-color: #ffb4bf;
  color: #c53958;
}

.ship-info {
  margin: 10px 0;
  color: var(--pink-600);
  font-size: 0.92rem;
}

.ship-info p {
  margin: 4px 0;
}

.cancel-message {
  color: #c53958;
  font-weight: 600;
}

.cancel-by {
  color: #8b4b5f;
  font-weight: 600;
}

.order-actions {
  margin: 0 0 10px;
}

.cancel-btn {
  border: 1px solid #ffb4bf;
  color: #c53958;
  background: #ffeef0;
  border-radius: 10px;
  padding: 7px 12px;
  font-weight: 700;
  cursor: pointer;
}

.cancel-btn:disabled {
  opacity: 0.65;
  cursor: progress;
}

.items {
  display: grid;
  gap: 8px;
}

.item-row {
  display: grid;
  grid-template-columns: 52px 1fr auto;
  gap: 10px;
  align-items: center;
  border: 1px solid var(--pink-100);
  border-radius: 12px;
  padding: 8px;
}

.item-row img {
  width: 52px;
  height: 52px;
  border-radius: 8px;
  object-fit: cover;
}

.name {
  text-decoration: none;
  color: var(--pink-700);
  font-weight: 600;
}

.meta {
  margin: 3px 0 0;
  color: var(--pink-500);
  font-size: 0.85rem;
}

.feedback {
  margin: 0 0 12px;
  padding: 11px 13px;
  border-radius: 12px;
  font-weight: 600;
}

.feedback.error {
  color: #d6456b;
  background: rgba(255, 237, 244, 0.95);
  border: 1px solid rgba(214, 69, 107, 0.2);
}

.feedback.success {
  color: #1f8a4c;
  background: rgba(235, 255, 242, 0.95);
  border: 1px solid rgba(31, 138, 76, 0.24);
}

@media (max-width: 680px) {
  .order-header {
    flex-direction: column;
  }

  .order-meta {
    text-align: left;
  }

  .item-row {
    grid-template-columns: 1fr;
  }
}
</style>
