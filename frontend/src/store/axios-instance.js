import axios from 'axios'

export const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'

export const GOOGLE_OAUTH_URL =
  import.meta.env.VITE_GOOGLE_OAUTH_URL ||
  'http://localhost:8080/api/oauth2/authorization/google'

export const axiosInstance = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true,
})

const TOKEN_KEY = 'accessToken'
const REFRESH_KEY = 'refreshToken'

export function getStoredTokens() {
  if (typeof window === 'undefined') return { accessToken: null, refreshToken: null }
  return {
    accessToken: localStorage.getItem(TOKEN_KEY),
    refreshToken: localStorage.getItem(REFRESH_KEY),
  }
}

export function setStoredTokens(accessToken, refreshToken) {
  if (typeof window === 'undefined') return
  if (accessToken) localStorage.setItem(TOKEN_KEY, accessToken)
  else localStorage.removeItem(TOKEN_KEY)
  if (refreshToken) localStorage.setItem(REFRESH_KEY, refreshToken)
  else localStorage.removeItem(REFRESH_KEY)
}

export function clearStoredAuth() {
  if (typeof window === 'undefined') return;
  
  // 1. Wipe persistent storage
  localStorage.removeItem('accessToken');
  localStorage.removeItem('refreshToken');
  localStorage.removeItem('user');

  // 2. ALGORITHM: In-Memory Token Purge
  // Erase the zombie token from Axios global memory so subsequent 
  // background fetches (like GuestRoute's useMeQuery) fail cleanly.
  if (axiosInstance.defaults.headers.common['Authorization']) {
    delete axiosInstance.defaults.headers.common['Authorization'];
  }
}

// Request interceptor — attach Bearer token
axiosInstance.interceptors.request.use((config) => {
  const { accessToken } = getStoredTokens()
  if (accessToken) {
    config.headers = config.headers ?? {}
    config.headers.Authorization = `Bearer ${accessToken}`
  }
  return config
})

// Concurrency state handlers
let isRefreshing = false;
let failedQueue = [];
let onAuthFailure = null;

export function setAuthFailureHandler(fn) {
  onAuthFailure = fn
}

// Promise resolver for queued requests
const processQueue = (error, token = null) => {
  failedQueue.forEach(prom => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token);
    }
  });
  failedQueue = [];
};

// Response interceptor — Token Refresh Concurrency Queue
axiosInstance.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    const status = error.response?.status;

    // Intercept both 401 and 403 status codes thrown by Spring Security
    if ((status === 401 || status === 403) && originalRequest && !originalRequest._retry) {
      
      // If a refresh is already in progress, suspend this request into the queue
      if (isRefreshing) {
        return new Promise(function(resolve, reject) {
          failedQueue.push({ resolve, reject });
        }).then(token => {
          originalRequest.headers['Authorization'] = `Bearer ${token}`;
          return axiosInstance(originalRequest);
        }).catch(err => {
          return Promise.reject(err);
        });
      }

      originalRequest._retry = true;
      isRefreshing = true;

      const { refreshToken } = getStoredTokens();
      
      if (!refreshToken) {
          isRefreshing = false;
          clearStoredAuth();
          onAuthFailure?.();
          return Promise.reject(error);
      }

      return new Promise(async (resolve, reject) => {
        try {
          const res = await axios.post(`${API_BASE_URL}/auth/refresh`, { refreshToken });
          const newAccess = res.data?.accessToken;
          const newRefresh = res.data?.refreshToken ?? refreshToken;
          
          if (!newAccess) throw new Error("No access token returned");

          setStoredTokens(newAccess, newRefresh);
          
          // Apply new token to subsequent requests and the original request
          axiosInstance.defaults.headers.common['Authorization'] = `Bearer ${newAccess}`;
          originalRequest.headers['Authorization'] = `Bearer ${newAccess}`;
          
          // Release the queue with the new token
          processQueue(null, newAccess);
          resolve(axiosInstance(originalRequest));
        } catch (refreshError) {
          // Reject all queued requests and trigger explicit logout cascade
          processQueue(refreshError, null);
          clearStoredAuth();
          onAuthFailure?.();
          reject(refreshError);
        } finally {
          isRefreshing = false;
        }
      });
    }
    
    return Promise.reject(error);
  }
);