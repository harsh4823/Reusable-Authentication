import { createApi } from '@reduxjs/toolkit/query/react'
import { axiosBaseQuery } from '../axios-base-query'

export const authApi = createApi({
  reducerPath: 'authApi',
  baseQuery: axiosBaseQuery(),
  endpoints: (builder) => ({
    login: builder.mutation({
      query: (body) => ({ url: '/auth/login', method: 'POST', data: body }),
    }),

    register: builder.mutation({
      query: (formData) => ({
        url: '/auth/register',
        method: 'POST',
        data: formData,
        headers: { 'Content-Type': 'multipart/form-data' },
      }),
    }),

    me: builder.query({
      query: () => ({
        url: '/auth/me',
        method: 'GET',
      }),
    }),

    refresh: builder.mutation({
      query: (body) => ({ url: '/auth/refresh', method: 'POST', data: body }),
    }),

    logoutSingle: builder.mutation({
      query: () => ({ url: '/auth/logout/single', method: 'POST' }),
    }),

    logoutAll: builder.mutation({
      query: () => ({ url: '/auth/logout/all', method: 'POST' }),
    }),
  }),
})

export const {
  useLoginMutation,
  useRegisterMutation,
  useMeQuery,
  useLazyMeQuery,
  useRefreshMutation,
  useLogoutSingleMutation,
  useLogoutAllMutation,
} = authApi