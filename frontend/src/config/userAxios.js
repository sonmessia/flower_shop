import axios from "axios";
import API from "./api";

const userAxios = axios.create({
  baseURL: API.baseURL,
  headers: {
    "Content-Type": "application/json",
  },
});

const readUser = () => {
  try {
    const raw = localStorage.getItem("user");
    return raw ? JSON.parse(raw) : null;
  } catch (error) {
    console.error("Unable to parse user from localStorage", error);
    return null;
  }
};

const persistUser = (user) => {
  localStorage.setItem("user", JSON.stringify(user));
  window.dispatchEvent(new Event("user-auth-changed"));
};

const clearUserSession = () => {
  localStorage.removeItem("user");
  window.dispatchEvent(new Event("user-auth-changed"));
  if (window.location.pathname !== "/login") {
    window.location.href = "/login";
  }
};

userAxios.interceptors.request.use(
  (config) => {
    const user = readUser();
    if (user?.accessToken) {
      config.headers.Authorization = `Bearer ${user.accessToken}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

userAxios.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    if (error.response?.status === 401 && !originalRequest?._retry) {
      originalRequest._retry = true;

      const user = readUser();
      if (!user?.refreshToken) {
        clearUserSession();
        return Promise.reject(error);
      }

      try {
        const { data } = await axios.post(API.users.refresh(), {
          refreshToken: user.refreshToken,
        });

        const nextUser = {
          ...user,
          accessToken: data.accessToken,
          refreshToken: data.refreshToken || user.refreshToken,
          tokenType: data.tokenType,
          expiresIn: data.expiresIn,
        };

        persistUser(nextUser);
        originalRequest.headers.Authorization = `Bearer ${nextUser.accessToken}`;
        return userAxios(originalRequest);
      } catch (refreshError) {
        clearUserSession();
        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);
  }
);

export default userAxios;
