import { configureStore } from '@reduxjs/toolkit'
import { setupListeners } from '@reduxjs/toolkit/query'
import authReducer from './auth-slice'
import { authApi } from './api/auth-api'
import { onboardApi } from './api/onboard-api'

export const store = configureStore({
  reducer: {
    auth: authReducer,
    [authApi.reducerPath]: authApi.reducer,
    [onboardApi.reducerPath]: onboardApi.reducer,
  },
  middleware: (getDefault) =>
    getDefault().concat(authApi.middleware, onboardApi.middleware),
})

setupListeners(store.dispatch)