<template>
  <div class="login-page">
    <div class="login-card" role="form" aria-labelledby="user-login-title">
      <div class="card-header">
        <span class="logo" aria-hidden="true">💐</span>
        <div>
          <h1 id="user-login-title">Đăng nhập tài khoản</h1>
          <p>Chào mừng bạn quay trở lại với Ther Florist</p>
        </div>
      </div>

      <transition name="fade">
        <p v-if="error" class="feedback error" role="alert">
          {{ error }}
        </p>
      </transition>

      <form @submit.prevent="handleSubmit">
        <label>
          Email
          <input
            v-model.trim="form.email"
            type="email"
            name="email"
            autocomplete="email"
            required
            placeholder="you@example.com"
          />
        </label>

        <label>
          Mật khẩu
          <input
            v-model="form.password"
            :type="showPassword ? 'text' : 'password'"
            name="password"
            autocomplete="current-password"
            required
            placeholder="••••••••"
            @keyup.enter="handleSubmit"
          />
        </label>

        <div class="form-extras">
          <label class="toggle">
            <input type="checkbox" v-model="showPassword" />
            <span>Hiển thị mật khẩu</span>
          </label>
          <router-link to="/" class="back-home">Về trang chủ</router-link>
        </div>

        <button type="submit" class="primary" :disabled="loading">
          {{ loading ? "Đang đăng nhập..." : "Đăng nhập" }}
        </button>
      </form>

      <p class="hint-text">
        Chưa có tài khoản?
        <router-link to="/register" class="register-link">Đăng ký ngay</router-link>
      </p>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import { useRouter } from "vue-router";
import axios from "axios";
import API from "../config/api";

const router = useRouter();

const form = reactive({
  email: "",
  password: "",
});

const loading = ref(false);
const error = ref("");
const showPassword = ref(false);

const handleSubmit = async () => {
  if (loading.value) return;

  error.value = "";
  loading.value = true;

  try {
    const { data: authData } = await axios.post(API.users.login(), {
      email: form.email,
      password: form.password,
    });

    const { data: profile } = await axios.get(API.users.me(), {
      headers: {
        Authorization: `Bearer ${authData.accessToken}`,
      },
    });

    localStorage.setItem(
      "user",
      JSON.stringify({
        ...profile,
        accessToken: authData.accessToken,
        refreshToken: authData.refreshToken,
        tokenType: authData.tokenType,
        expiresIn: authData.expiresIn,
      })
    );

    window.dispatchEvent(new Event("user-auth-changed"));
    router.push("/");
    form.password = "";
  } catch (err) {
    if (err.response?.data?.message) {
      error.value = err.response.data.message;
    } else if (Array.isArray(err.response?.data)) {
      error.value = err.response.data
        .map((item) => item.message || item)
        .join(", ");
    } else {
      error.value = "Đăng nhập không thành công. Vui lòng thử lại.";
    }
    console.error(err);
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 32px 16px 48px;
}

.login-card {
  width: min(440px, 100%);
  padding: 36px 34px 42px;
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid rgba(243, 109, 161, 0.14);
  box-shadow: 0 46px 120px -64px rgba(243, 109, 161, 0.55);
  display: grid;
  gap: 22px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 18px;
}

.logo {
  width: 64px;
  height: 64px;
  border-radius: 20px;
  display: grid;
  place-items: center;
  font-size: 30px;
  background: linear-gradient(135deg, var(--pink-500), var(--pink-300));
  box-shadow: 0 26px 60px -40px rgba(243, 109, 161, 0.5);
}

.card-header h1 {
  margin: 0;
  font-size: 1.85rem;
  color: var(--pink-700);
}

.card-header p {
  margin: 6px 0 0;
  color: var(--pink-400);
  font-weight: 500;
}

form {
  display: grid;
  gap: 18px;
}

label {
  display: grid;
  gap: 8px;
  font-weight: 600;
  color: var(--pink-500);
}

input[type="text"],
input[type="password"],
input[type="checkbox"],
input[type="email"],
textarea,
select {
  border: 1px solid rgba(243, 109, 161, 0.22);
  border-radius: 14px;
  padding: 12px 14px;
  font: inherit;
  color: var(--pink-700);
  background: rgba(255, 255, 255, 0.9);
  box-shadow: inset 0 10px 24px -24px rgba(243, 109, 161, 0.65);
}

input[type="checkbox"] {
  width: auto;
  padding: 0;
  margin: 0;
  accent-color: var(--pink-500);
}

.form-extras {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.toggle {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 0.95rem;
  font-weight: 500;
  color: var(--pink-400);
}

.back-home {
  color: var(--pink-600);
  font-weight: 600;
  text-decoration: none;
}

.back-home:hover {
  text-decoration: underline;
}

.primary {
  width: 100%;
  border: none;
  border-radius: 16px;
  padding: 14px 18px;
  color: white;
  font-size: 1.05rem;
  background: linear-gradient(135deg, var(--pink-400), var(--pink-500));
  box-shadow: 0 32px 80px -58px rgba(243, 109, 161, 0.75);
  transition: transform 0.2s ease;
  cursor: pointer;
}

.primary:hover:enabled {
  transform: translateY(-2px);
}

.primary:disabled {
  opacity: 0.7;
  cursor: progress;
}

.feedback {
  margin: 0;
  padding: 12px 16px;
  border-radius: 14px;
  background: rgba(255, 237, 244, 0.95);
  color: #d6456b;
  border: 1px solid rgba(214, 69, 107, 0.2);
  font-weight: 600;
}

.hint-text {
  margin: 0;
  text-align: center;
  color: var(--pink-500);
  font-weight: 500;
}

.register-link {
  color: var(--pink-700);
  font-weight: 700;
  text-decoration: none;
}

.register-link:hover {
  text-decoration: underline;
}

@media (max-width: 600px) {
  .login-card {
    padding: 32px 24px 36px;
    border-radius: 22px;
  }

  .card-header h1 {
    font-size: 1.65rem;
  }

  .form-extras {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
