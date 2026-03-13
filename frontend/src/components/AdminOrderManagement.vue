<template>
  <div class="admin-orders-page">
    <aside class="sidebar">
      <div class="brand">
        <img src="@/assets/logo_2.jpg" alt="Logo" class="logo-image" />
        <p>Quản lý đơn hàng</p>
      </div>

      <div class="quick-nav">
        <router-link to="/admin/dashboard" class="nav-btn"
          >📊 Dashboard</router-link
        >
        <router-link to="/admin/orders" class="nav-btn active"
          >📦 Đơn hàng</router-link
        >
        <router-link to="/" class="nav-btn">🏠 Trang chủ</router-link>
      </div>

      <button
        type="button"
        class="refresh-btn"
        :disabled="loading"
        @click="fetchOrders"
      >
        {{ loading ? "Đang tải..." : "Làm mới" }}
      </button>

      <button type="button" class="logout-btn" @click="handleLogout">
        Đăng xuất
      </button>
    </aside>

    <main class="content">
      <header class="page-header">
        <div>
          <h1>Quản lý đơn hàng</h1>
          <p>Theo dõi và cập nhật trạng thái xử lý đơn của khách hàng.</p>
        </div>
        <div class="header-meta">
          <span
            >Tổng đơn: <strong>{{ orders.length }}</strong></span
          >
        </div>
      </header>

      <transition name="fade">
        <p v-if="feedback" class="feedback">{{ feedback }}</p>
      </transition>

      <section class="filters">
        <label>
          Tìm theo mã đơn hoặc người nhận
          <input
            v-model.trim="searchQuery"
            type="search"
            placeholder="Ví dụ: 1024 hoặc Nguyen"
          />
        </label>

        <label>
          Trạng thái
          <select v-model="statusFilter">
            <option value="ALL">Tất cả</option>
            <option
              v-for="status in statusOptions"
              :key="status"
              :value="status"
            >
              {{ statusLabel(status) }}
            </option>
          </select>
        </label>
      </section>

      <section v-if="loading" class="loading-state">
        <div class="spinner"></div>
        <p>Đang tải danh sách đơn hàng...</p>
      </section>

      <section v-else-if="filteredOrders.length === 0" class="empty-state">
        <h2>Không có đơn hàng phù hợp</h2>
      </section>

      <section v-else class="orders-list">
        <article
          v-for="order in filteredOrders"
          :key="order.id"
          class="order-card"
        >
          <header class="order-header">
            <div>
              <h2>Đơn #{{ order.id }}</h2>
              <p>{{ formatDate(order.createdAt) }}</p>
            </div>
            <div class="header-right">
              <strong>{{ formatPrice(order.totalAmount) }}</strong>
              <select
                class="status-select"
                :value="order.status"
                :disabled="updatingOrderId === order.id"
                @change="onStatusChange(order, $event.target.value)"
              >
                <option
                  v-for="status in statusOptions"
                  :key="status"
                  :value="status"
                >
                  {{ statusLabel(status) }}
                </option>
              </select>
            </div>
          </header>

          <div class="shipping-info">
            <p><strong>Người nhận:</strong> {{ order.shippingName }}</p>
            <p v-if="order.shippingPhone">
              <strong>SĐT:</strong> {{ order.shippingPhone }}
            </p>
            <p>
              <strong>Địa chỉ:</strong>
              {{
                [
                  order.shippingStreet,
                  order.shippingCity,
                  order.shippingPostalCode,
                ]
                  .filter(Boolean)
                  .join(", ")
              }}
            </p>
            <p v-if="order.note"><strong>Ghi chú:</strong> {{ order.note }}</p>
            <p v-if="showUserNotification(order)" class="cancel-message">
              <strong>Thông báo cho khách:</strong>
              {{ order.cancellationMessage }}
            </p>
            <p
              v-if="showUserNotification(order) && order.cancellationBy"
              class="cancel-by"
            >
              <strong
                >Hủy bởi: {{ cancellationActorLabel(order.cancellationBy) }}</strong
              >
              <span v-if="formatCancellationBy(order.cancellationBy)">
                ({{ formatCancellationBy(order.cancellationBy) }})
              </span>
            </p>
          </div>

          <div class="items">
            <div v-for="item in order.items" :key="item.id" class="item-row">
              <img
                :src="item.imageUrl || fallbackImage"
                :alt="item.productName"
                @error="onImageError"
              />
              <div>
                <p class="item-name">{{ item.productName }}</p>
                <p class="item-sub">
                  {{ item.quantity }} x {{ formatPrice(item.unitPrice) }}
                </p>
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
import { computed, onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import axios from "../config/axiosConfig";
import API from "../config/api";

const router = useRouter();
const fallbackImage =
  "https://via.placeholder.com/300x300/FFE1F0/F36DA1?text=Flower";

const orders = ref([]);
const loading = ref(true);
const feedback = ref("");
const updatingOrderId = ref(null);
const searchQuery = ref("");
const statusFilter = ref("ALL");

const statusOptions = [
  "PENDING",
  "CONFIRMED",
  "PROCESSING",
  "SHIPPED",
  "COMPLETED",
  "CANCELLED",
];

const filteredOrders = computed(() => {
  let result = [...orders.value];

  if (statusFilter.value !== "ALL") {
    result = result.filter((order) => order.status === statusFilter.value);
  }

  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase();
    result = result.filter(
      (order) =>
        String(order.id).includes(query) ||
        (order.shippingName || "").toLowerCase().includes(query)
    );
  }

  return result;
});

const fetchOrders = async () => {
  loading.value = true;
  feedback.value = "";

  try {
    const { data } = await axios.get(API.adminOrders.getAll());
    orders.value = data || [];
  } catch (error) {
    feedback.value = "Không thể tải danh sách đơn hàng.";
  } finally {
    loading.value = false;
  }
};

const onStatusChange = async (order, nextStatus) => {
  if (!nextStatus || nextStatus === order.status) return;

  updatingOrderId.value = order.id;
  feedback.value = "";

  let adminMessage = null;
  if (nextStatus === "CANCELLED") {
    const input = window.prompt(
      "Nhập lý do hủy đơn để gửi cho khách hàng:",
      order.cancellationMessage || ""
    );

    if (input === null) {
      updatingOrderId.value = null;
      return;
    }

    if (!input.trim()) {
      feedback.value = "Vui lòng nhập lý do hủy đơn.";
      updatingOrderId.value = null;
      return;
    }

    adminMessage = input.trim();
  }

  try {
    const { data } = await axios.put(API.adminOrders.updateStatus(order.id), {
      status: nextStatus,
      cancelReason: adminMessage,
    });

    const idx = orders.value.findIndex((item) => item.id === order.id);
    if (idx !== -1) {
      orders.value[idx] = data;
    }
    feedback.value = `Đã cập nhật trạng thái đơn #${order.id}.`;
  } catch (error) {
    feedback.value = "Cập nhật trạng thái thất bại.";
  } finally {
    updatingOrderId.value = null;
  }
};

const handleLogout = () => {
  localStorage.removeItem("admin");
  router.push("/admin/login");
};

const statusLabel = (status) => {
  const labels = {
    PENDING: "Chờ xác nhận",
    CONFIRMED: "Đã xác nhận",
    PROCESSING: "Đang xử lý",
    SHIPPED: "Đang giao",
    COMPLETED: "Hoàn tất",
    CANCELLED: "Đã hủy",
  };
  return labels[status] || status;
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

const onImageError = (event) => {
  event.target.src = fallbackImage;
};

const showUserNotification = (order) => {
  if (order.status !== "CANCELLED" || !order.cancellationMessage) return false;
  const by = order.cancellationBy || "";
  if (by.startsWith("USER:")) return true;
  if (by.startsWith("ADMIN:")) return false;

  // Backward compatibility for old records without role prefix.
  return order.cancellationMessage.startsWith("Lý do hủy đơn:");
};

const formatCancellationBy = (value) => {
  if (!value) return "";
  if (value.startsWith("ADMIN:")) return value.slice("ADMIN:".length);
  if (value.startsWith("USER:")) return value.slice("USER:".length);
  return value;
};

const cancellationActorLabel = (value) => {
  if (!value) return "Hệ thống";
  if (value.startsWith("ADMIN:")) return "Admin";
  if (value.startsWith("USER:")) return "Khách hàng";
  return "Hệ thống";
};

onMounted(async () => {
  await fetchOrders();
});
</script>

<style scoped>
.admin-orders-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 280px 1fr;
  background: linear-gradient(140deg, #fff8fc 0%, #fff0f7 45%, #ffe6f2 100%);
}

.sidebar {
  padding: 22px;
  border-right: 1px solid var(--pink-200);
  background: rgba(255, 255, 255, 0.86);
  display: grid;
  align-content: start;
  gap: 14px;
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
}

.nav-btn.active,
.nav-btn:hover {
  background: var(--pink-100);
}

.refresh-btn,
.logout-btn {
  border: 1px solid var(--pink-300);
  border-radius: 10px;
  padding: 10px 12px;
  font-weight: 600;
  cursor: pointer;
}

.refresh-btn {
  background: white;
  color: var(--pink-700);
}

.logout-btn {
  color: #c53958;
  background: #fff;
}

.content {
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 16px;
}

.page-header h1 {
  margin: 0;
  color: var(--pink-700);
}

.page-header p {
  margin: 8px 0 0;
  color: var(--pink-500);
}

.header-meta {
  color: var(--pink-600);
  font-weight: 600;
}

.feedback {
  margin: 0 0 14px;
  padding: 10px 12px;
  border-radius: 10px;
  background: rgba(255, 247, 204, 0.8);
  color: #8a6a00;
  border: 1px solid #f3da84;
}

.filters {
  display: grid;
  grid-template-columns: 1.2fr 240px;
  gap: 10px;
  margin-bottom: 14px;
}

.filters label {
  display: grid;
  gap: 6px;
  color: var(--pink-600);
  font-weight: 600;
  font-size: 0.92rem;
}

.filters input,
.filters select {
  border: 1px solid var(--pink-300);
  border-radius: 10px;
  padding: 10px 12px;
  font: inherit;
}

.loading-state,
.empty-state {
  border: 1px solid var(--pink-200);
  background: rgba(255, 255, 255, 0.9);
  border-radius: 14px;
  padding: 24px;
  text-align: center;
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

.orders-list {
  display: grid;
  gap: 12px;
}

.order-card {
  border: 1px solid var(--pink-200);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.95);
  padding: 12px;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 10px;
  padding-bottom: 10px;
  border-bottom: 1px dashed var(--pink-200);
}

.order-header h2 {
  margin: 0;
  color: var(--pink-700);
  font-size: 1.05rem;
}

.order-header p {
  margin: 6px 0 0;
  color: var(--pink-500);
  font-size: 0.9rem;
}

.header-right {
  display: grid;
  gap: 8px;
  justify-items: end;
}

.status-select {
  border: 1px solid var(--pink-300);
  border-radius: 8px;
  padding: 6px 8px;
  font: inherit;
  color: var(--pink-700);
}

.shipping-info {
  margin-top: 10px;
  color: var(--pink-600);
  font-size: 0.92rem;
}

.shipping-info p {
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

.items {
  margin-top: 10px;
  display: grid;
  gap: 8px;
}

.item-row {
  display: grid;
  grid-template-columns: 52px 1fr auto;
  gap: 10px;
  align-items: center;
  border: 1px solid var(--pink-100);
  border-radius: 10px;
  padding: 8px;
}

.item-row img {
  width: 52px;
  height: 52px;
  border-radius: 8px;
  object-fit: cover;
}

.item-name {
  margin: 0;
  color: var(--pink-700);
  font-weight: 600;
}

.item-sub {
  margin: 4px 0 0;
  color: var(--pink-500);
  font-size: 0.86rem;
}

@media (max-width: 980px) {
  .admin-orders-page {
    grid-template-columns: 1fr;
  }

  .sidebar {
    border-right: none;
    border-bottom: 1px solid var(--pink-200);
  }

  .filters {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 680px) {
  .page-header,
  .order-header {
    flex-direction: column;
  }

  .header-right {
    justify-items: start;
  }

  .item-row {
    grid-template-columns: 1fr;
  }
}
</style>
