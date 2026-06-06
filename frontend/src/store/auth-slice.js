import { createSlice } from '@reduxjs/toolkit'
import { clearStoredAuth, setStoredTokens } from './axios-instance'
import { rolesFromToken } from '@/lib/token-utils'

function readInitial() {
  if (typeof window === 'undefined') {
    return { user: null, accessToken: null, refreshToken: null, isAuthenticated: false, isLoading: false }
  }
  const accessToken = localStorage.getItem('accessToken')
  const refreshToken = localStorage.getItem('refreshToken')
  const userRaw = localStorage.getItem('user')
  let user = null
  try {
    user = userRaw ? JSON.parse(userRaw) : null
  } catch {
    user = null
  }
  return {
    user,
    accessToken,
    refreshToken,
    isAuthenticated: Boolean(accessToken && user),
    isLoading: false,
  }
}

const authSlice = createSlice({
  name: 'auth',
  initialState: readInitial(),
  reducers: {
    setCredentials: (state, action) => {
      const { accessToken, refreshToken, user } = action.payload
      const finalUser = {
        ...user,
        roles: user.roles?.length ? user.roles : rolesFromToken(accessToken),
      }
      state.accessToken = accessToken
      state.refreshToken = refreshToken
      state.user = finalUser
      state.isAuthenticated = true
      setStoredTokens(accessToken, refreshToken)
      if (typeof window !== 'undefined') {
        localStorage.setItem('user', JSON.stringify(finalUser))
      }
    },
    setSessionUser: (state, action) => {
      state.user = action.payload
      state.accessToken = null
      state.refreshToken = null
      state.isAuthenticated = true

      if (typeof window !== 'undefined') {
        localStorage.setItem('user', JSON.stringify(action.payload))
      }
    },
    updateAccessToken: (state, action) => {
      state.accessToken = action.payload
      setStoredTokens(action.payload, state.refreshToken)
    },
    updateProfile: (state, action) => {
      if (!state.user) return
      state.user = { ...state.user, ...action.payload }
      if (typeof window !== 'undefined') {
        localStorage.setItem('user', JSON.stringify(state.user))
      }
    },
    logout: (state) => {
      state.user = null
      state.accessToken = null
      state.refreshToken = null
      state.isAuthenticated = false
      clearStoredAuth()
    },
  },
})

export const {
  setCredentials,
  setSessionUser,
  updateAccessToken,
  updateProfile,
  logout
} = authSlice.actions

export default authSlice.reducer