import { createApi } from '@reduxjs/toolkit/query/react'
import { axiosBaseQuery } from '../axios-base-query'

export const realmApi = createApi({
  reducerPath: 'realmApi',
  baseQuery: axiosBaseQuery(),
  tagTypes: ['Realm', 'RealmUsers', 'RealmRoles', 'RealmClients'],
  endpoints: (builder) => ({
    getRealm: builder.query({
      query: (realmName) => ({
        url: `/admin/realms/${realmName}`,
        method: 'GET',
      }),
      providesTags: ['Realm'],
    }),

    getRealmUsers: builder.query({
      query: ({ realmName, page = 0, size = 10 }) => ({
        url: `/admin/realms/${realmName}/users`,
        method: 'GET',
        params: { page, size },
      }),
      providesTags: ['RealmUsers'],
    }),

    getRealmRoles: builder.query({
      query: (realmName) => ({
        url: `/admin/realms/${realmName}/roles`,
        method: 'GET',
      }),
      providesTags: ['RealmRoles'],
    }),

    getRealmClients: builder.query({
      query: (realmName) => ({
        url: `/admin/realms/${realmName}/clients`,
        method: 'GET',
      }),
      providesTags: ['RealmClients'],
    }),
  }),
})

export const {
  useGetRealmQuery,
  useGetRealmUsersQuery,
  useGetRealmRolesQuery,
  useGetRealmClientsQuery,
} = realmApi