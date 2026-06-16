import { Navigate, Outlet } from 'react-router-dom'
import { Loader2 } from 'lucide-react'
import { useEffect } from 'react'
import { useAuth, rootRedirectFor } from '@/lib/auth-helpers'
import { useMeQuery } from '@/store/api/auth-api'
import { useAppDispatch } from '@/store/hooks'
import { setSessionUser } from '@/store/auth-slice'

export function GuestRoute() {
  const { isAuthenticated, user: reduxUser } = useAuth()
  const dispatch = useAppDispatch()

  const {
    data: cookieUser,
    isLoading,
    isError,
  } = useMeQuery(undefined, {
    skip: isAuthenticated,
  })

  useEffect(() => {
    if (cookieUser) {
      dispatch(setSessionUser(cookieUser))
    }
  }, [cookieUser, dispatch])

  if (isAuthenticated) {
    return <Navigate to={rootRedirectFor(reduxUser?.roles ?? [])} replace />
  }

  if (isLoading) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <Loader2 className="h-5 w-5 animate-spin" />
      </div>
    )
  }

  if (cookieUser) {
    return <Navigate to={rootRedirectFor(cookieUser.roles ?? [])} replace />
  }

  if (isError) {
    return <Outlet />
  }

  return null
}