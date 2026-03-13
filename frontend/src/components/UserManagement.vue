<template>
  <div class="user-management-page">
    <SiteNavbar @search="noop" @select-category="noop" />

    <main class="content">
      <section class="profile-card" aria-labelledby="profile-title">
        <header class="profile-header">
          <div>
            <h1 id="profile-title">Quản lý tài khoản</h1>
            <p>Cập nhật thông tin cá nhân và địa chỉ giao hàng của bạn.</p>
          </div>
          <button class="outline-btn" @click="goHome">Về trang chủ</button>
        </header>

        <transition name="fade">
          <p v-if="error" class="feedback error" role="alert">{{ error }}</p>
        </transition>

        <transition name="fade">
          <p v-if="success" class="feedback success" role="status">{{ success }}</p>
        </transition>

        <form @submit.prevent="handleSave" class="profile-form">
          <label>
            Email
            <input v-model="form.email" type="email" disabled />
          </label>

          <label>
            Họ và tên
            <input
              v-model.trim="form.fullName"
              type="text"
              required
              autocomplete="name"
              placeholder="Nguyen Van A"
            />
          </label>

          <label>
            Số điện thoại
            <input
              v-model.trim="form.phone"
              type="tel"
              autocomplete="tel"
              placeholder="0901234567"
            />
          </label>

          <label>
            Địa chỉ
            <input
              v-model.trim="form.street"
              type="text"
              autocomplete="address-line1"
              placeholder="Số nhà, tên đường, phường/xã"
            />
          </label>

          <div class="row">
            <label>
              Thành phố
              <input
                v-model.trim="form.city"
                type="text"
                autocomplete="address-level2"
                placeholder="Hồ Chí Minh"
              />
            </label>

            <label>
              Mã bưu chính
              <input
                v-model.trim="form.postalCode"
                type="text"
                autocomplete="postal-code"
                placeholder="700000"
              />
            </label>
          </div>

          <div class="actions">
            <button type="button" class="ghost-btn" @click="reloadProfile" :disabled="loading">
              Tải lại
            </button>
            <button type="submit" class="primary-btn" :disabled="loading">
              {{ loading ? "Đang lưu..." : "Lưu thay đổi" }}
            </button>
          </div>
        </form>
      </section>
    </main>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue";
import { useRouter } from "vue-router";
import axios from "axios";
import API from "../config/api";
import SiteNavbar from "./SiteNavbar.vue";

const router = useRouter();
const loading = ref(false);
const error = ref("");
const success = ref("");

const form = reactive({
  email: "",
  fullName: "",
  phone: "",
  street: "",
  city: "",
  postalCode: "",
});

const noop = () => {};

const getStoredUser = () => {
  try {
    const raw = localStorage.getItem("user");
    return raw ? JSON.parse(raw) : null;
  } catch (e) {
    return null;
  }
};

const getAuthHeader = () => {
  const user = getStoredUser();
  return user?.accessToken
    ? {
        Authorization: `Bearer ${user.accessToken}`,
      }
    : null;
};

const parseApiError = (err) => {
  const fields = err.response?.data?.fields;
  if (fields && typeof fields === "object") {
    return Object.values(fields).join(", ");
  }
  if (err.response?.data?.message) {
    return err.response.data.message;
  }
  return "Không thể cập nhật thông tin. Vui lòng thử lại.";
};

const loadProfile = async () => {
  const headers = getAuthHeader();
  if (!headers) {
    router.push("/login");
    return;
  }

  loading.value = true;
  error.value = "";

  try {
    const { data } = await axios.get(API.users.me(), { headers });

    form.email = data.email || "";
    form.fullName = data.fullName || "";
    form.phone = data.phone || "";
    form.street = data.street || "";
    form.city = data.city || "";
    form.postalCode = data.postalCode || "";

    const currentUser = getStoredUser() || {};
    localStorage.setItem("user", JSON.stringify({ ...currentUser, ...data }));
    window.dispatchEvent(new Event("user-auth-changed"));
  } catch (err) {
    if (err.response?.status === 401) {
      localStorage.removeItem("user");
      window.dispatchEvent(new Event("user-auth-changed"));
      router.push("/login");
      return;
    }
    error.value = parseApiError(err);
    console.error(err);
  } finally {
    loading.value = false;
  }
};

const reloadProfile = async () => {
  success.value = "";
  await loadProfile();
};

const handleSave = async () => {
  const headers = getAuthHeader();
  if (!headers) {
    router.push("/login");
    return;
  }

  loading.value = true;
  error.value = "";
  success.value = "";

  try {
    const payload = {
      fullName: form.fullName,
      phone: form.phone || null,
      street: form.street || null,
      city: form.city || null,
      postalCode: form.postalCode || null,
    };

    const { data } = await axios.put(API.users.updateMe(), payload, { headers });

    form.email = data.email || form.email;
    form.fullName = data.fullName || "";
    form.phone = data.phone || "";
    form.street = data.street || "";
    form.city = data.city || "";
    form.postalCode = data.postalCode || "";

    const currentUser = getStoredUser() || {};
    localStorage.setItem("user", JSON.stringify({ ...currentUser, ...data }));
    window.dispatchEvent(new Event("user-auth-changed"));

    success.value = "Cập nhật thông tin thành công.";
  } catch (err) {
    error.value = parseApiError(err);
    console.error(err);
  } finally {
    loading.value = false;
  }
};

const goHome = () => {
  router.push("/");
};

onMounted(async () => {
  await loadProfile();
});
</script>

<style scoped>
.user-management-page {
  min-height: 100vh;
}

.content {
  max-width: 1000px;
  margin: 0 auto;
  padding: 28px 16px 56px;
}

.profile-card {
  background: rgba(255, 255, 255, 0.95);
  border: 1px solid var(--pink-200);
  border-radius: 22px;
  padding: 28px;
  box-shadow: 0 24px 70px -48px rgba(243, 109, 161, 0.45);
}

.profile-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 20px;
}

.profile-header h1 {
  margin: 0;
  color: var(--pink-700);
  font-size: 1.8rem;
}

.profile-header p {
  margin: 8px 0 0;
  color: var(--pink-500);
}

.profile-form {
  display: grid;
  gap: 14px;
}

label {
  display: grid;
  gap: 8px;
  font-weight: 600;
  color: var(--pink-600);
}

input {
  border: 1px solid rgba(243, 109, 161, 0.25);
  border-radius: 12px;
  padding: 11px 13px;
  font: inherit;
  color: var(--pink-700);
  background: white;
}

input:disabled {
  background: var(--pink-50);
  color: var(--pink-500);
}

.row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 6px;
}

.primary-btn,
.ghost-btn,
.outline-btn {
  border-radius: 12px;
  padding: 10px 14px;
  font-weight: 600;
  font-size: 0.95rem;
  cursor: pointer;
  transition: all 0.2s ease;
}

.primary-btn {
  border: none;
  color: white;
  background: linear-gradient(135deg, var(--pink-500), var(--pink-400));
}

.primary-btn:hover:enabled {
  transform: translateY(-1px);
}

.primary-btn:disabled,
.ghost-btn:disabled {
  opacity: 0.7;
  cursor: progress;
}

.ghost-btn,
.outline-btn {
  border: 1px solid var(--pink-300);
  color: var(--pink-700);
  background: white;
}

.ghost-btn:hover,
.outline-btn:hover {
  background: var(--pink-100);
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

@media (max-width: 700px) {
  .profile-card {
    padding: 22px 18px;
  }

  .profile-header {
    flex-direction: column;
    align-items: stretch;
  }

  .row {
    grid-template-columns: 1fr;
  }

  .actions {
    flex-direction: column-reverse;
  }
}
</style>
