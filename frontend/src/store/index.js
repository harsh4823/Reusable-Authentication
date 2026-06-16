import { configureStore } from '@reduxjs/toolkit'
import { setupListeners } from '@reduxjs/toolkit/query'
import authReducer from './auth-slice'
import { authApi } from './api/auth-api'
import { onboardApi } from './api/onboard-api'
import { realmApi } from './api/realm-api'

export const store = configureStore({
  reducer: {
    auth: authReducer,
    [authApi.reducerPath]: authApi.reducer,
    [onboardApi.reducerPath]: onboardApi.reducer,
    [realmApi.reducerPath]: realmApi.reducer,
  },
  middleware: (getDefault) =>
    getDefault().concat(authApi.middleware, onboardApi.middleware,realmApi.middleware,),
})

setupListeners(store.dispatch)