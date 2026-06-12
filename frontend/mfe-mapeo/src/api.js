import axios from 'axios';

// Create an Axios instance pointing to the API Gateway (KrakenD)
const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || '/api',
});

// Add a request interceptor to attach the JWT token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

export default api;
