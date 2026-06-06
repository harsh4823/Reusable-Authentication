import { useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { Loader2 } from 'lucide-react'
import { toast } from 'sonner'
import { useLazyMeQuery } from '@/store/api/auth-api'
import { useAppDispatch } from '@/store/hooks'
import { setSessionUser } from '@/store/auth-slice'
import { rootRedirectFor } from '@/lib/auth-helpers'

export default function OAuthSuccess() {
  const navigate = useNavigate()
  const dispatch = useAppDispatch()
  const [getMe] = useLazyMeQuery()

  useEffect(() => {
    const hydrateUser = async () => {
      try {
        const user = await getMe().unwrap()

        dispatch(setSessionUser(user))

        toast.success('Welcome back')
        navigate(rootRedirectFor(user.roles ?? []), { replace: true })
      } catch {
        toast.error('Google login failed')
        navigate('/login', { replace: true })
      }
    }

    hydrateUser()
  }, [dispatch, getMe, navigate])

  return (
    <div className="flex min-h-screen items-center justify-center bg-background">
      <div className="flex items-center gap-3 text-muted-foreground">
        <Loader2 className="h-5 w-5 animate-spin" />
        <span>Completing Google login...</span>
      </div>
    </div>
  )
}