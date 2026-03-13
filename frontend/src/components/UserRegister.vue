<template>
  <div class="register-page">
    <div class="register-card" role="form" aria-labelledby="user-register-title">
      <div class="card-header">
        <span class="logo" aria-hidden="true">🌷</span>
        <div>
          <h1 id="user-register-title">Đăng ký tài khoản</h1>
          <p>Tạo tài khoản mới để mua sắm nhanh hơn</p>
        </div>
      </div>

      <transition name="fade">
        <p v-if="error" class="feedback error" role="alert">
          {{ error }}
        </p>
      </transition>

      <transition name="fade">
        <p v-if="success" class="feedback success" role="status">
          {{ success }}
        </p>
      </transition>

      <form @submit.prevent="handleSubmit" novalidate>
        <label>
          Họ và tên
          <input
            v-model.trim="form.fullName"
            type="text"
            name="fullName"
            autocomplete="name"
            required
            placeholder="Nguyen Van A"
            :class="{ invalid: !!fieldErrors.fullName }"
            @blur="validateField('fullName')"
          />
          <small v-if="fieldErrors.fullName" class="field-error">{{ fieldErrors.fullName }}</small>
        </label>

        <label>
          Email
          <input
            v-model.trim="form.email"
            type="email"
            name="email"
            autocomplete="email"
            required
            placeholder="you@example.com"
            :class="{ invalid: !!fieldErrors.email }"
            @blur="validateField('email')"
          />
          <small v-if="fieldErrors.email" class="field-error">{{ fieldErrors.email }}</small>
        </label>

        <label>
          Số điện thoại (không bắt buộc)
          <input
            v-model.trim="form.phone"
            type="tel"
            name="phone"
            autocomplete="tel"
            placeholder="0901234567"
            :class="{ invalid: !!fieldErrors.phone }"
            @blur="validateField('phone')"
          />
          <small v-if="fieldErrors.phone" class="field-error">{{ fieldErrors.phone }}</small>
        </label>

        <label>
          Mật khẩu
          <input
            v-model="form.password"
            :type="showPassword ? 'text' : 'password'"
            name="password"
            autocomplete="new-password"
            required
            minlength="6"
            placeholder="Tối thiểu 6 ký tự"
            :class="{ invalid: !!fieldErrors.password }"
            @blur="validateField('password')"
          />
          <small v-if="fieldErrors.password" class="field-error">{{ fieldErrors.password }}</small>
        </label>

        <label>
          Xác nhận mật khẩu
          <input
            v-model="form.confirmPassword"
            :type="showPassword ? 'text' : 'password'"
            name="confirmPassword"
            autocomplete="new-password"
            required
            minlength="6"
            placeholder="Nhập lại mật khẩu"
            :class="{ invalid: !!fieldErrors.confirmPassword }"
            @blur="validateField('confirmPassword')"
          />
          <small v-if="fieldErrors.confirmPassword" class="field-error">{{ fieldErrors.confirmPassword }}</small>
        </label>

        <div class="form-extras">
          <label class="toggle">
            <input type="checkbox" v-model="showPassword" />
            <span>Hiển thị mật khẩu</span>
          </label>
          <router-link to="/login" class="back-login">Đã có tài khoản?</router-link>
        </div>

        <button type="submit" class="primary" :disabled="loading">
          {{ loading ? "Đang tạo tài khoản..." : "Đăng ký" }}
        </button>
      </form>

      <p class="hint">
        Sau khi đăng ký thành công, bạn sẽ được chuyển sang trang đăng nhập.
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
  fullName: "",
  email: "",
  phone: "",
  password: "",
  confirmPassword: "",
});

const loading = ref(false);
const error = ref("");
const success = ref("");
const showPassword = ref(false);
const fieldErrors = reactive({
  fullName: "",
  email: "",
  phone: "",
  password: "",
  confirmPassword: "",
});

const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const PHONE_REGEX = /^[0-9+\-\s()]{9,15}$/;

const validateField = (fieldName) => {
  const value = form[fieldName];

  if (fieldName === "fullName") {
    if (!value || value.trim().length < 2) {
      fieldErrors.fullName = "Họ và tên phải có ít nhất 2 ký tự.";
      return false;
    }
    fieldErrors.fullName = "";
    return true;
  }

  if (fieldName === "email") {
    if (!value) {
      fieldErrors.email = "Email là bắt buộc.";
      return false;
    }
    if (!EMAIL_REGEX.test(value)) {
      fieldErrors.email = "Email không đúng định dạng.";
      return false;
    }
    fieldErrors.email = "";
    return true;
  }

  if (fieldName === "phone") {
    if (value && !PHONE_REGEX.test(value)) {
      fieldErrors.phone = "Số điện thoại không hợp lệ.";
      return false;
    }
    fieldErrors.phone = "";
    return true;
  }

  if (fieldName === "password") {
    if (!value) {
      fieldErrors.password = "Mật khẩu là bắt buộc.";
      return false;
    }
    if (value.length < 6) {
      fieldErrors.password = "Mật khẩu phải có ít nhất 6 ký tự.";
      return false;
    }
    fieldErrors.password = "";
    return true;
  }

  if (fieldName === "confirmPassword") {
    if (!value) {
      fieldErrors.confirmPassword = "Bạn cần xác nhận mật khẩu.";
      return false;
    }
    if (value !== form.password) {
      fieldErrors.confirmPassword = "Mật khẩu xác nhận không khớp.";
      return false;
    }
    fieldErrors.confirmPassword = "";
    return true;
  }

  return true;
};

const validateForm = () => {
  const fields = ["fullName", "email", "phone", "password", "confirmPassword"];
  return fields.every((field) => validateField(field));
};

const applyServerFieldErrors = (serverFieldErrors) => {
  Object.keys(fieldErrors).forEach((key) => {
    fieldErrors[key] = serverFieldErrors?.[key] || "";
  });
};

const getApiErrorMessage = (err) => {
  const serverFieldErrors =
    err.response?.data?.fieldErrors || err.response?.data?.fields;

  if (serverFieldErrors && typeof serverFieldErrors === "object") {
    applyServerFieldErrors(serverFieldErrors);
    return Object.values(serverFieldErrors).join(", ");
  }

  if (err.response?.data?.message) {
    return err.response.data.message;
  }

  return "Đăng ký không thành công. Vui lòng thử lại.";
};

const handleSubmit = async () => {
  if (loading.value) return;

  error.value = "";
  success.value = "";

  if (!validateForm()) {
    error.value = "Vui lòng kiểm tra lại các thông tin đã nhập.";
    return;
  }

  loading.value = true;

  try {
    await axios.post(API.users.register(), {
      email: form.email,
      password: form.password,
      fullName: form.fullName,
      phone: form.phone || null,
    });

    success.value = "Đăng ký thành công. Đang chuyển đến trang đăng nhập...";

    setTimeout(() => {
      router.push("/login");
    }, 1200);
  } catch (err) {
    error.value = getApiErrorMessage(err);
    console.error("Register request failed", err);
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 32px 16px 48px;
}

.register-card {
  width: min(500px, 100%);
  padding: 36px 34px 42px;
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid rgba(243, 109, 161, 0.14);
  box-shadow: 0 46px 120px -64px rgba(243, 109, 161, 0.55);
  display: grid;
  gap: 20px;
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
  font-size: 1.8rem;
  color: var(--pink-700);
}

.card-header p {
  margin: 6px 0 0;
  color: var(--pink-400);
  font-weight: 500;
}

form {
  display: grid;
  gap: 16px;
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
input[type="tel"],
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

input.invalid {
  border-color: #d6456b;
  box-shadow: 0 0 0 2px rgba(214, 69, 107, 0.12);
}

.field-error {
  margin-top: -2px;
  color: #d6456b;
  font-size: 0.84rem;
  font-weight: 500;
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

.back-login {
  color: var(--pink-600);
  font-weight: 600;
  text-decoration: none;
}

.back-login:hover {
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
  border: 1px solid transparent;
  font-weight: 600;
}

.feedback.error {
  background: rgba(255, 237, 244, 0.95);
  color: #d6456b;
  border-color: rgba(214, 69, 107, 0.2);
}

.feedback.success {
  background: rgba(235, 255, 242, 0.95);
  color: #1f8a4c;
  border-color: rgba(31, 138, 76, 0.24);
}

.hint {
  margin: 0;
  text-align: center;
  color: var(--pink-400);
  font-size: 0.95rem;
}

@media (max-width: 600px) {
  .register-card {
    padding: 32px 24px 36px;
    border-radius: 22px;
  }

  .card-header h1 {
    font-size: 1.6rem;
  }

  .form-extras {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
