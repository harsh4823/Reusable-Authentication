import { createApi } from '@reduxjs/toolkit/query/react'
import { axiosBaseQuery } from '../axios-base-query'

export const onboardApi = createApi({
  reducerPath: 'onboardApi',
  baseQuery: axiosBaseQuery(),
  endpoints: (builder) => ({
    onboard: builder.mutation({
      query: (body) => ({ url: '/onboard', method: 'POST', data: body }),
    }),
    checkRealmAvailability: builder.query({
      query: (name) => ({ url: '/onboard/realm-available', params: { name } }),
    }),
  }),
})

export const { useOnboardMutation, useLazyCheckRealmAvailabilityQuery } = onboardApi