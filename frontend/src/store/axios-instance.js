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
  if (typeof window === 'undefined') return
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(REFRESH_KEY)
  localStorage.removeItem('user')
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

// Response interceptor — silent refresh on 401
let refreshPromise = null
let onAuthFailure = null

export function setAuthFailureHandler(fn) {
  onAuthFailure = fn
}

async function performRefresh() {
  const { refreshToken } = getStoredTokens()
  if (!refreshToken) return null
  try {
    const res = await axios.post(`${API_BASE_URL}/auth/refresh`, { refreshToken })
    const newAccess = res.data?.accessToken
    const newRefresh = res.data?.refreshToken ?? refreshToken
    if (!newAccess) return null
    setStoredTokens(newAccess, newRefresh ?? null)
    return newAccess
  } catch {
    return null
  }
}

axiosInstance.interceptors.response.use(
  (r) => r,
  async (error) => {
    const original = error.config
    if (error.response?.status === 401 && original && !original._retry) {
      original._retry = true
      if (!refreshPromise) refreshPromise = performRefresh().finally(() => (refreshPromise = null))
      const newToken = await refreshPromise
      if (newToken) {
        original.headers = original.headers ?? {}
        original.headers.Authorization = `Bearer ${newToken}`
        return axiosInstance(original)
      }
      clearStoredAuth()
      onAuthFailure?.()
    }
    return Promise.reject(error)
  }
)