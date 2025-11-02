import axios from 'axios';

const authAxios = axios.create({
  baseURL: 'http://localhost:8080',
});

authAxios.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor to clear invalid/expired tokens
authAxios.interceptors.response.use(
  (response) => response,
  (error) => {
    // If token is invalid/expired (401 or 403), silently clear it
    if (error.response && (error.response.status === 401 || error.response.status === 403)) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      // Don't redirect - just let the app handle the logged-out state naturally
    }
    return Promise.reject(error);
  }
);

export default authAxios;
