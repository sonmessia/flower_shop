<template>
  <div class="cart-page">
    <SiteNavbar @search="noop" />

    <main class="cart-container">
      <header class="page-header">
        <div>
          <h1>Giỏ hàng của bạn</h1>
          <p>Kiểm tra sản phẩm, cập nhật số lượng và tiến hành đặt hàng.</p>
        </div>
        <router-link to="/" class="back-home">Tiếp tục mua sắm</router-link>
      </header>

      <transition name="fade">
        <p v-if="error" class="feedback error">{{ error }}</p>
      </transition>

      <transition name="fade">
        <p v-if="success" class="feedback success">{{ success }}</p>
      </transition>

      <div v-if="loading" class="loading-block">
        <div class="spinner"></div>
        <span>Đang tải giỏ hàng...</span>
      </div>

      <section v-else-if="cart.items.length === 0" class="empty-cart">
        <h2>Giỏ hàng đang trống</h2>
        <p>Hãy thêm một vài bó hoa xinh đẹp để tiếp tục.</p>
        <router-link to="/" class="cta-btn">Khám phá sản phẩm</router-link>
      </section>

      <section v-else class="cart-layout">
        <div class="cart-items">
          <article v-for="item in cart.items" :key="item.id" class="cart-item">
            <img
              :src="item.imageUrl || fallbackImage"
              :alt="item.productName"
              class="item-image"
              @error="onImageError"
            />

            <div class="item-content">
              <router-link :to="`/products/${item.productId}`" class="item-name">
                {{ item.productName }}
              </router-link>
              <p class="item-code">Mã: {{ item.productCode }}</p>
              <p class="item-price">{{ formatPrice(item.unitPrice) }}</p>
            </div>

            <div class="item-actions">
              <div class="qty-controls">
                <button @click="decreaseQty(item)" :disabled="updatingItemId === item.id">-</button>
                <span>{{ item.quantity }}</span>
                <button @click="increaseQty(item)" :disabled="updatingItemId === item.id">+</button>
              </div>
              <p class="line-total">{{ formatPrice(item.lineTotal) }}</p>
              <button class="remove-btn" @click="removeItem(item.id)" :disabled="updatingItemId === item.id">
                Xóa
              </button>
            </div>
          </article>
        </div>

        <aside class="checkout-card">
          <h2>Thanh toán</h2>
          <div class="summary-row">
            <span>Số lượng sản phẩm</span>
            <strong>{{ cart.totalItems }}</strong>
          </div>
          <div class="summary-row total">
            <span>Tạm tính</span>
            <strong>{{ formatPrice(cart.subtotal) }}</strong>
          </div>

          <form @submit.prevent="checkout" class="checkout-form">
            <label>
              Người nhận
              <input v-model.trim="checkoutForm.shippingName" required placeholder="Nguyen Van A" />
            </label>
            <label>
              Số điện thoại
              <input v-model.trim="checkoutForm.shippingPhone" placeholder="0901234567" />
            </label>
            <label>
              Địa chỉ
              <input v-model.trim="checkoutForm.shippingStreet" required placeholder="Số nhà, đường, phường/xã" />
            </label>
            <div class="form-row">
              <label>
                Thành phố
                <input v-model.trim="checkoutForm.shippingCity" required placeholder="Hồ Chí Minh" />
              </label>
              <label>
                Mã bưu chính
                <input v-model.trim="checkoutForm.shippingPostalCode" placeholder="700000" />
              </label>
            </div>
            <label>
              Ghi chú
              <textarea v-model.trim="checkoutForm.note" rows="3" placeholder="Lưu ý cho cửa hàng"></textarea>
            </label>

            <button type="submit" class="checkout-btn" :disabled="checkingOut">
              {{ checkingOut ? "Đang tạo đơn hàng..." : "Đặt hàng" }}
            </button>
          </form>
        </aside>
      </section>
    </main>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue";
import { useRouter } from "vue-router";
import SiteNavbar from "./SiteNavbar.vue";
import API from "../config/api";
import userAxios from "../config/userAxios";

const router = useRouter();
const fallbackImage = "https://via.placeholder.com/300x300/FFE1F0/F36DA1?text=Flower";

const loading = ref(true);
const checkingOut = ref(false);
const updatingItemId = ref(null);
const error = ref("");
const success = ref("");

const cart = reactive({
  items: [],
  totalItems: 0,
  subtotal: 0,
});

const checkoutForm = reactive({
  shippingName: "",
  shippingPhone: "",
  shippingStreet: "",
  shippingCity: "",
  shippingPostalCode: "",
  note: "",
});

const noop = () => {};

const parseError = (err, fallback = "Có lỗi xảy ra. Vui lòng thử lại.") => {
  const fields = err.response?.data?.fields;
  if (fields && typeof fields === "object") return Object.values(fields).join(", ");
  if (err.response?.data?.message) return err.response.data.message;
  return fallback;
};

const fillShippingFromUser = () => {
  try {
    const raw = localStorage.getItem("user");
    if (!raw) return;
    const user = JSON.parse(raw);
    checkoutForm.shippingName = user.fullName || "";
    checkoutForm.shippingPhone = user.phone || "";
    checkoutForm.shippingStreet = user.street || "";
    checkoutForm.shippingCity = user.city || "";
    checkoutForm.shippingPostalCode = user.postalCode || "";
  } catch (e) {
    console.error("Unable to read user info", e);
  }
};

const applyCart = (data) => {
  cart.items = data.items || [];
  cart.totalItems = data.totalItems || 0;
  cart.subtotal = data.subtotal || 0;
  window.dispatchEvent(new Event("cart-updated"));
};

const loadCart = async () => {
  loading.value = true;
  error.value = "";

  try {
    const { data } = await userAxios.get(API.cart.get());
    applyCart(data);
  } catch (err) {
    error.value = parseError(err, "Không thể tải giỏ hàng.");
  } finally {
    loading.value = false;
  }
};

const updateItemQty = async (item, quantity) => {
  if (quantity < 1) return;
  updatingItemId.value = item.id;
  error.value = "";
  success.value = "";

  try {
    const { data } = await userAxios.put(API.cart.updateItem(item.id), { quantity });
    applyCart(data);
  } catch (err) {
    error.value = parseError(err, "Không thể cập nhật số lượng.");
  } finally {
    updatingItemId.value = null;
  }
};

const increaseQty = async (item) => {
  await updateItemQty(item, item.quantity + 1);
};

const decreaseQty = async (item) => {
  await updateItemQty(item, item.quantity - 1);
};

const removeItem = async (itemId) => {
  updatingItemId.value = itemId;
  error.value = "";
  success.value = "";

  try {
    const { data } = await userAxios.delete(API.cart.removeItem(itemId));
    applyCart(data);
  } catch (err) {
    error.value = parseError(err, "Không thể xóa sản phẩm khỏi giỏ hàng.");
  } finally {
    updatingItemId.value = null;
  }
};

const checkout = async () => {
  if (checkingOut.value) return;

  checkingOut.value = true;
  error.value = "";
  success.value = "";

  try {
    const payload = {
      shippingName: checkoutForm.shippingName,
      shippingPhone: checkoutForm.shippingPhone || null,
      shippingStreet: checkoutForm.shippingStreet,
      shippingCity: checkoutForm.shippingCity,
      shippingPostalCode: checkoutForm.shippingPostalCode || null,
      note: checkoutForm.note || null,
    };

    const { data } = await userAxios.post(API.orders.checkout(), payload);
    success.value = `Đặt hàng thành công. Mã đơn #${data.id}`;
    router.push({ path: "/orders", query: { created: String(data.id) } });
  } catch (err) {
    error.value = parseError(err, "Không thể tạo đơn hàng.");
  } finally {
    checkingOut.value = false;
  }
};

const formatPrice = (price) =>
  new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
  }).format(Number(price) || 0);

const onImageError = (event) => {
  event.target.src = fallbackImage;
};

onMounted(async () => {
  fillShippingFromUser();
  await loadCart();
});
</script>

<style scoped>
.cart-page {
  min-height: 100vh;
}

.cart-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 28px 16px 54px;
}

.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 20px;
}

.page-header h1 {
  margin: 0;
  color: var(--pink-700);
}

.page-header p {
  margin: 8px 0 0;
  color: var(--pink-500);
}

.back-home {
  text-decoration: none;
  border: 1px solid var(--pink-300);
  border-radius: 12px;
  padding: 10px 12px;
  color: var(--pink-700);
  font-weight: 600;
  background: white;
}

.loading-block,
.empty-cart {
  border: 1px solid var(--pink-200);
  background: rgba(255, 255, 255, 0.9);
  border-radius: 18px;
  padding: 30px;
  text-align: center;
}

.empty-cart .cta-btn {
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

.cart-layout {
  display: grid;
  grid-template-columns: 1.8fr 1fr;
  gap: 18px;
}

.cart-items {
  display: grid;
  gap: 12px;
}

.cart-item {
  display: grid;
  grid-template-columns: 100px 1fr auto;
  gap: 14px;
  border: 1px solid var(--pink-200);
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  padding: 12px;
}

.item-image {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: 12px;
}

.item-name {
  text-decoration: none;
  color: var(--pink-700);
  font-weight: 700;
}

.item-code,
.item-price {
  margin: 6px 0 0;
  color: var(--pink-500);
}

.item-actions {
  display: grid;
  justify-items: end;
  align-content: space-between;
  min-width: 130px;
}

.qty-controls {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  border: 1px solid var(--pink-300);
  border-radius: 999px;
  padding: 4px 8px;
}

.qty-controls button {
  border: none;
  background: var(--pink-100);
  color: var(--pink-700);
  border-radius: 999px;
  width: 24px;
  height: 24px;
  cursor: pointer;
  font-weight: 700;
}

.line-total {
  margin: 0;
  color: var(--pink-700);
  font-weight: 700;
}

.remove-btn {
  border: none;
  background: none;
  color: #d6456b;
  cursor: pointer;
  font-weight: 600;
}

.checkout-card {
  border: 1px solid var(--pink-200);
  background: rgba(255, 255, 255, 0.95);
  border-radius: 18px;
  padding: 18px;
  height: fit-content;
  position: sticky;
  top: 92px;
}

.checkout-card h2 {
  margin: 0 0 14px;
  color: var(--pink-700);
}

.summary-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
  color: var(--pink-600);
}

.summary-row.total {
  font-size: 1.1rem;
  color: var(--pink-700);
  border-top: 1px solid var(--pink-200);
  padding-top: 10px;
}

.checkout-form {
  display: grid;
  gap: 10px;
}

.checkout-form label {
  display: grid;
  gap: 6px;
  color: var(--pink-600);
  font-weight: 600;
  font-size: 0.92rem;
}

.checkout-form input,
.checkout-form textarea {
  border: 1px solid rgba(243, 109, 161, 0.25);
  border-radius: 10px;
  padding: 10px 12px;
  font: inherit;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}

.checkout-btn {
  border: none;
  border-radius: 12px;
  background: linear-gradient(135deg, var(--pink-500), var(--pink-400));
  color: white;
  font-weight: 700;
  padding: 11px 14px;
  cursor: pointer;
  margin-top: 6px;
}

.feedback {
  margin: 0 0 14px;
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

@media (max-width: 980px) {
  .cart-layout {
    grid-template-columns: 1fr;
  }

  .checkout-card {
    position: static;
  }
}

@media (max-width: 700px) {
  .cart-item {
    grid-template-columns: 1fr;
  }

  .item-actions {
    justify-items: start;
  }

  .form-row {
    grid-template-columns: 1fr;
  }
}
</style>
