import axios from 'axios';
import API from './api';

// Create axios instance with base configuration
const axiosInstance = axios.create({
  baseURL: API.baseURL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor - add Authorization header
axiosInstance.interceptors.request.use(
  (config) => {
    const adminData = localStorage.getItem('admin');
    if (adminData) {
      try {
        const admin = JSON.parse(adminData);
        if (admin.accessToken) {
          config.headers.Authorization = `Bearer ${admin.accessToken}`;
        }
      } catch (e) {
        console.error('Error parsing admin data from localStorage:', e);
      }
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor - handle 401 errors
axiosInstance.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    // If 401 and not already retrying, try to refresh token
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        const adminData = localStorage.getItem('admin');
        if (adminData) {
          const admin = JSON.parse(adminData);
          if (admin.refreshToken) {
            // Try to refresh the token
            const response = await axios.post(`${API.baseURL}/admins/refresh`, {
              refreshToken: admin.refreshToken,
            });

            const newTokens = response.data;

            // Update stored tokens (response now includes id, username, accessToken, refreshToken)
            localStorage.setItem('admin', JSON.stringify(newTokens));

            // Retry original request with new token
            originalRequest.headers.Authorization = `Bearer ${newTokens.accessToken}`;
            return axiosInstance(originalRequest);
          }
        }
      } catch (refreshError) {
        console.error('Token refresh failed:', refreshError);
        // Clear admin data and redirect to login
        localStorage.removeItem('admin');
        window.location.href = '/admin/login';
        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);
  }
);

export default axiosInstance;
