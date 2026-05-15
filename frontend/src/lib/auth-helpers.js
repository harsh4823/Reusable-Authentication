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

export function rootRedirectFor(roles) {
  if (roles.includes('ROLE_ADMIN')) return '/admin'
  return '/dashboard'
}