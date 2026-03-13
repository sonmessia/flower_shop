<template>
  <nav class="site-navbar">
    <div class="navbar-container">
      <!-- Logo -->
      <router-link to="/" class="navbar-logo">
        <img src="@/assets/logo_2.jpg" alt="Logo" class="logo-image" />
      </router-link>

      <!-- Navigation Links -->
      <div class="navbar-links">
        <router-link to="/" class="nav-link" active-class="active">
          <i class="icon">🏠</i>
          <span>Trang chủ</span>
        </router-link>

        <router-link to="/blogs" class="nav-link" active-class="active">
          <i class="icon">📝</i>
          <span>Blog</span>
        </router-link>
      </div>

      <!-- Search Bar -->
      <div class="navbar-search">
        <div class="search-wrapper">
          <input
            v-model="searchQuery"
            type="text"
            placeholder="Tìm kiếm sản phẩm..."
            class="search-input"
            @input="handleSearchInput"
            @keyup.enter="handleSearch"
            @focus="showSearchResults = true"
          />
          <button class="search-btn" @click="handleSearch">
            <svg
              width="20"
              height="20"
              viewBox="0 0 20 20"
              fill="none"
              xmlns="http://www.w3.org/2000/svg"
            >
              <circle
                cx="8"
                cy="8"
                r="6"
                stroke="currentColor"
                stroke-width="2"
              />
              <path
                d="M12 12L17 17"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
              />
            </svg>
          </button>

          <!-- Search Results Dropdown -->
          <div
            v-if="showSearchResults && searchQuery.length > 0"
            class="search-dropdown"
          >
            <div v-if="isSearching" class="search-loading">
              <div class="spinner"></div>
              <span>Đang tìm kiếm...</span>
            </div>

            <div v-else-if="searchResults.length > 0" class="search-results">
              <router-link
                v-for="product in searchResults"
                :key="product.id"
                :to="`/products/${product.id}`"
                class="search-result-item"
                @click="closeSearchDropdown"
              >
                <img
                  v-if="product.imageUrl"
                  :src="product.imageUrl"
                  :alt="product.name"
                  class="result-image"
                />
                <div class="result-info">
                  <div class="result-name">{{ product.name }}</div>
                  <div class="result-code">Mã: {{ product.productCode }}</div>
                  <div class="result-price">
                    {{ formatPrice(product.price) }}
                  </div>
                </div>
              </router-link>

              <div v-if="searchResults.length >= 5" class="search-footer">
                <button @click="handleSearch" class="view-all-btn">
                  Xem tất cả kết quả
                </button>
              </div>
            </div>

            <div v-else class="search-empty">
              <span>Không tìm thấy sản phẩm nào</span>
            </div>
          </div>
        </div>
      </div>

      <div class="auth-actions">
        <template v-if="userSession">
          <router-link to="/account" class="user-badge user-badge-link">
            {{ userDisplayName }}
          </router-link>
          <button class="auth-btn logout-btn" @click="handleUserLogout">
            Đăng xuất
          </button>
        </template>
        <template v-else>
          <router-link to="/register" class="auth-btn register-btn">
            Đăng ký
          </router-link>
          <router-link to="/login" class="auth-btn login-btn">
            Đăng nhập
          </router-link>
        </template>
      </div>

      <!-- Admin Link
      <router-link to="/admin/login" class="admin-link">
        <i class="icon">👤</i>
        <span>Admin</span>
      </router-link> -->
    </div>
  </nav>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from "vue";
import { useRouter } from "vue-router";
import axios from "axios";
import API from "../config/api";

const router = useRouter();

const searchQuery = ref("");
const searchResults = ref([]);
const showSearchResults = ref(false);
const isSearching = ref(false);
const userSession = ref(null);
let searchTimeout = null;

const emit = defineEmits(["search"]);

const userDisplayName = computed(() => {
  if (!userSession.value) return "";
  return userSession.value.fullName || userSession.value.email || "Tài khoản";
});

onMounted(async () => {
  syncUserSession();
  document.addEventListener("click", handleClickOutside);
  window.addEventListener("storage", handleUserAuthChanged);
  window.addEventListener("user-auth-changed", handleUserAuthChanged);
});

onUnmounted(() => {
  clearTimeout(searchTimeout);
  document.removeEventListener("click", handleClickOutside);
  window.removeEventListener("storage", handleUserAuthChanged);
  window.removeEventListener("user-auth-changed", handleUserAuthChanged);
});

const syncUserSession = () => {
  try {
    const stored = localStorage.getItem("user");
    userSession.value = stored ? JSON.parse(stored) : null;
  } catch (error) {
    console.error("Khong the doc thong tin user trong localStorage", error);
    userSession.value = null;
  }
};

const handleUserAuthChanged = () => {
  syncUserSession();
};

const handleUserLogout = () => {
  localStorage.removeItem("user");
  userSession.value = null;
  window.dispatchEvent(new Event("user-auth-changed"));
  router.push("/");
};

const handleSearchInput = () => {
  // Debounce search
  clearTimeout(searchTimeout);

  if (searchQuery.value.trim().length === 0) {
    searchResults.value = [];
    showSearchResults.value = false;
    return;
  }

  searchTimeout = setTimeout(async () => {
    await performSearch();
  }, 300);
};

const performSearch = async () => {
  if (searchQuery.value.trim().length === 0) return;

  try {
    isSearching.value = true;
    showSearchResults.value = true;

    const response = await axios.get(API.products.getAll(), {
      params: {
        search: searchQuery.value,
      },
    });

    // Limit to 5 results in dropdown
    searchResults.value = response.data.slice(0, 5);
  } catch (error) {
    console.error("Lỗi khi tìm kiếm:", error);
    searchResults.value = [];
  } finally {
    isSearching.value = false;
  }
};

const handleSearch = () => {
  closeSearchDropdown();
  emit("search", searchQuery.value);
};

const closeSearchDropdown = () => {
  showSearchResults.value = false;
};

const formatPrice = (price) => {
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
  }).format(price);
};

// Close dropdown when clicking outside
const handleClickOutside = (event) => {
  if (!event.target.closest(".navbar-search")) {
    showSearchResults.value = false;
  }
};
</script>

<style scoped>
.site-navbar {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid var(--pink-200);
  position: sticky;
  top: 0;
  z-index: 1000;
  box-shadow: 0 2px 12px rgba(243, 109, 161, 0.08);
}

.navbar-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0.8rem 2rem;
  display: flex;
  align-items: center;
  gap: 2rem;
}

.navbar-logo {
  display: flex;
  align-items: center;
  gap: 0.8rem;
  text-decoration: none;
  transition: transform 0.3s ease;
}

.navbar-logo:hover {
  transform: scale(1.05);
}

.logo-image {
  height: 60px;
  width: 200px;
  object-fit: cover;
  border-radius: 0;
  background-color: white;
  padding: 8px;
}

.navbar-links {
  display: flex;
  align-items: center;
  gap: 1.5rem;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.6rem 1.2rem;
  border-radius: 12px;
  text-decoration: none;
  color: var(--pink-700);
  font-weight: 500;
  transition: all 0.3s ease;
  background: transparent;
  border: none;
  cursor: pointer;
  font-size: 1rem;
}

.nav-link:hover {
  background: var(--pink-100);
  color: var(--pink-600);
}

.nav-link.active {
  background: var(--pink-200);
  color: var(--pink-600);
}

.icon {
  font-size: 1.2rem;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.navbar-search {
  flex: 1;
  max-width: 500px;
  margin-left: auto;
  position: relative;
}

.search-wrapper {
  position: relative;
  display: flex;
  gap: 0.5rem;
}

.search-input {
  flex: 1;
  padding: 0.8rem 1.2rem;
  border: 2px solid var(--pink-200);
  border-radius: 12px;
  font-size: 0.95rem;
  outline: none;
  transition: all 0.3s ease;
  font-family: "Be Vietnam Pro", sans-serif;
}

.search-input:focus {
  border-color: var(--pink-400);
  box-shadow: 0 0 0 3px rgba(243, 109, 161, 0.1);
}

.search-btn {
  padding: 0.8rem 1.2rem;
  background: linear-gradient(135deg, var(--pink-500), var(--pink-400));
  border: none;
  border-radius: 12px;
  color: white;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.search-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(243, 109, 161, 0.3);
}

.search-dropdown {
  position: absolute;
  top: calc(100% + 8px);
  left: 0;
  right: 60px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(243, 109, 161, 0.2);
  border: 1px solid var(--pink-200);
  max-height: 500px;
  overflow-y: auto;
  z-index: 1001;
  animation: slideDown 0.3s ease;
}

.search-loading {
  padding: 2rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
  color: var(--pink-600);
}

.spinner {
  width: 32px;
  height: 32px;
  border: 3px solid var(--pink-200);
  border-top-color: var(--pink-500);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.search-results {
  padding: 0.5rem 0;
}

.search-result-item {
  display: flex;
  gap: 1rem;
  padding: 0.8rem 1rem;
  text-decoration: none;
  color: var(--pink-700);
  transition: all 0.2s ease;
  border-bottom: 1px solid var(--pink-100);
}

.search-result-item:last-child {
  border-bottom: none;
}

.search-result-item:hover {
  background: var(--pink-50);
}

.result-image {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid var(--pink-200);
}

.result-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.result-name {
  font-weight: 600;
  color: var(--pink-700);
  font-size: 0.95rem;
}

.result-code {
  font-size: 0.85rem;
  color: var(--pink-500);
}

.result-price {
  font-weight: 700;
  color: var(--pink-600);
  font-size: 0.9rem;
}

.search-empty {
  padding: 2rem;
  text-align: center;
  color: var(--pink-500);
  font-style: italic;
}

.search-footer {
  padding: 0.8rem 1rem;
  border-top: 1px solid var(--pink-200);
  background: var(--pink-50);
}

.view-all-btn {
  width: 100%;
  padding: 0.6rem;
  background: linear-gradient(135deg, var(--pink-500), var(--pink-400));
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.view-all-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(243, 109, 161, 0.3);
}

.admin-link {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.6rem 1.2rem;
  border-radius: 12px;
  text-decoration: none;
  color: var(--pink-700);
  font-weight: 500;
  transition: all 0.3s ease;
  border: 1px solid var(--pink-300);
}

.admin-link:hover {
  background: var(--pink-100);
  border-color: var(--pink-400);
}

.auth-actions {
  display: flex;
  align-items: center;
  gap: 0.7rem;
}

.user-badge {
  max-width: 180px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  padding: 0.45rem 0.8rem;
  border-radius: 999px;
  border: 1px solid var(--pink-300);
  color: var(--pink-700);
  background: var(--pink-75);
  font-weight: 600;
  font-size: 0.9rem;
}

.user-badge-link {
  text-decoration: none;
  transition: all 0.22s ease;
}

.user-badge-link:hover {
  background: var(--pink-100);
}

.auth-btn {
  border: none;
  border-radius: 12px;
  padding: 0.55rem 0.95rem;
  text-decoration: none;
  font-weight: 600;
  font-size: 0.92rem;
  cursor: pointer;
  transition: all 0.22s ease;
}

.login-btn {
  color: white;
  background: linear-gradient(135deg, var(--pink-500), var(--pink-400));
}

.register-btn {
  color: var(--pink-700);
  border: 1px solid var(--pink-300);
  background: white;
}

.register-btn:hover {
  background: var(--pink-100);
}

.login-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 8px 20px rgba(243, 109, 161, 0.28);
}

.logout-btn {
  color: var(--pink-700);
  border: 1px solid var(--pink-300);
  background: white;
}

.logout-btn:hover {
  background: var(--pink-100);
}

@media (max-width: 968px) {
  .navbar-container {
    flex-wrap: wrap;
    gap: 1rem;
  }

  .logo-image {
    height: 50px;
    width: 160px;
  }

  .navbar-search {
    order: 3;
    flex-basis: 100%;
    max-width: 100%;
  }

  .auth-actions {
    margin-left: auto;
  }
}
</style>
