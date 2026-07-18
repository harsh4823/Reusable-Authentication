// frontend/src/store/api/realm-api.js
import { createApi } from '@reduxjs/toolkit/query/react'
import { axiosBaseQuery } from '../axios-base-query'

export const realmApi = createApi({
  reducerPath: 'realmApi',
  baseQuery: axiosBaseQuery(),
  tagTypes: ['Realm', 'RealmUsers', 'RealmRoles', 'RealmClients'],
  endpoints: (builder) => ({
    getRealms: builder.query({
      query: () => ({
        url: `/admin/realms/me`,
        method: 'GET',
      }),
      providesTags: ['Realm'], // Tag this list so we can invalidate it later
    }),

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

    // ALGORITHM: State Mutation & Cache Invalidation
    createRealm: builder.mutation({
      query: (realmData) => ({
        url: `/admin/realms`,
        method: 'POST',
        data: realmData, // Axios uses 'data' for the request body
      }),
      invalidatesTags: ['Realm'], // Force queries depending on 'Realm' to refetch
    }),

    createRealmRole: builder.mutation({
      query: ({ realmName, roleData }) => ({
        url: `/admin/realms/${realmName}/roles`,
        method: 'POST',
        data: roleData,
      }),
      invalidatesTags: ['RealmRoles'], // Only refreshes the roles tab
    }),

    deleteRealmRole: builder.mutation({
      query: ({ realmName, roleName }) => ({
        url: `/admin/realms/${realmName}/roles/${roleName}`,
        method: 'DELETE',
      }),
      invalidatesTags: ['RealmRoles'],
    }),

    createRealmUser: builder.mutation({
      query: ({ realmName, userData }) => ({
        url: `/admin/realms/${realmName}/users`,
        method: 'POST',
        data: userData,
      }),
      invalidatesTags: ['RealmUsers'], // Only refreshes the users tab
    }),

    // frontend/src/store/api/realm-api.js
// ... (Add these into your endpoints builder) ...

    deleteRealmUser: builder.mutation({
      query: ({ realmName, userId }) => ({
        url: `/admin/realms/${realmName}/users/${userId}`,
        method: 'DELETE',
      }),
      invalidatesTags: ['RealmUsers'],
    }),

    assignRoleToUser: builder.mutation({
      query: ({ realmName, userId, roleName }) => ({
        url: `/admin/realms/${realmName}/users/${userId}/roles`,
        method: 'POST',
        data: { roleName }, // Assuming your Java backend expects a body with roleName
      }),
      invalidatesTags: ['RealmUsers'], // Refreshes the user table to show the new role tag
    }),

    removeRoleFromUser: builder.mutation({
      query: ({ realmName, userId, roleName }) => ({
        url: `/admin/realms/${realmName}/users/${userId}/roles/${roleName}`,
        method: 'DELETE',
      }),
      invalidatesTags: ['RealmUsers'],
    }),
  }),
})

export const {
  useGetRealmsQuery,
  useGetRealmQuery,
  useGetRealmUsersQuery,
  useGetRealmRolesQuery,
  useGetRealmClientsQuery,
  useCreateRealmMutation, 
  useCreateRealmRoleMutation,
  useDeleteRealmRoleMutation,
  useCreateRealmUserMutation
} = realmApi