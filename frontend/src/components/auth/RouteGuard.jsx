import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { Loader2 } from 'lucide-react'
import { useAuth } from '@/lib/auth-helpers'
import { useMeQuery } from '@/store/api/auth-api'
import { useAppDispatch } from '@/store/hooks'
import { setSessionUser } from '@/store/auth-slice'
import { useEffect } from 'react'

export function ProtectedRoute() {
  const { isAuthenticated } = useAuth()
  const location = useLocation()
  const dispatch = useAppDispatch()

  const {
    data: user,
    isLoading,
    isError,
  } = useMeQuery(undefined, {
    skip: isAuthenticated,
  })

  useEffect(() => {
    if (user) {
      dispatch(setSessionUser(user))
    }
  }, [user, dispatch])

  if (isAuthenticated) {
    return <Outlet />
  }

  if (isLoading) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <Loader2 className="h-5 w-5 animate-spin" />
      </div>
    )
  }

  if (user) {
    return <Outlet />
  }

  if (isError) {
    return <Navigate to={`/login?redirect=${encodeURIComponent(location.pathname)}`} replace />
  }

  return null
}