import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '@/lib/auth-helpers'

export function ProtectedRoute() {
  const { isAuthenticated } = useAuth()
  const location = useLocation()

  if (!isAuthenticated) {
    // encode current path as ?redirect= query param so login can send user back
    return <Navigate to={`/login?redirect=${encodeURIComponent(location.pathname)}`} replace />
  }

  return <Outlet />
}

export function RoleGuard({ roles, children }) {
  const { user } = useAuth()
  const userRoles = user?.roles ?? []
  const ok = roles.some((r) => userRoles.includes(r))

  if (!ok) return <Navigate to="/dashboard" replace />

  return <>{children}</>
}