import { useAppSelector } from '@/store/hooks'

export function useAuth() {
  return useAppSelector((s) => s.auth)
}

export function hasRole(roles, role) {
  return roles.includes(role)
}

export function hasAnyRole(roles, required) {
  return required.some((r) => roles.includes(r))
}

export function rootRedirectFor(roles, realm = null) {
  if (roles.includes('ROLE_ADMIN')) {
    return '/admin'
  }
  
  if (roles.includes('ROLE_CLIENT')) {
    if (!realm) {
      return '/realms' 
    }
    return `/realms/${realm}/settings`
  }

  if (realm) {
      return `/app/${realm}/dashboard`
  }
  
  return '/dashboard'
}