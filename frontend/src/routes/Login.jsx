import { Link, Navigate, useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { KeyRound, Loader2 } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Checkbox } from '@/components/ui/checkbox'
import { PasswordInput } from './../components/ui-extras/PasswordInput'
import { loginSchema } from '@/lib/validators'        
import { useLoginMutation } from '@/store/api/auth-api'
import { useAppDispatch } from '@/store/hooks'
import { setCredentials } from '@/store/auth-slice'
import { useAuth, rootRedirectFor } from '@/lib/auth-helpers'
import { GOOGLE_OAUTH_URL } from '@/store/axios-instance'
import { GoogleIcon } from './../components/auth/GoogleIcon';
import { useSearchParams } from 'react-router-dom'
import { axiosInstance } from '@/store/axios-instance'

const Login = () => {
  const { isAuthenticated, user } = useAuth()
  const navigate = useNavigate()
  const dispatch = useAppDispatch()
  const [login, { isLoading }] = useLoginMutation()
  const [searchParams] = useSearchParams()
  const continueUrl = searchParams.get('continue')

  const form = useForm({
    resolver: zodResolver(loginSchema),
    defaultValues: { email: '', password: '', remember: false },
  })

  if (isAuthenticated) return <Navigate to={rootRedirectFor(user?.roles ?? [])} replace />

  const onSubmit = async (values) => {
  try {
    await axiosInstance.post('/auth/session-login', {
      email: values.email,
      password: values.password,
    })

    if (continueUrl) {
      window.location.replace(decodeURIComponent(continueUrl))
      return
    }

    // normal dashboard login fallback
    const res = await login({
      email: values.email,
      password: values.password,
    }).unwrap()

    dispatch(setCredentials({
      accessToken: res.accessToken,
      refreshToken: res.refreshToken,
      user: res.user,
    }))

    navigate(rootRedirectFor(res.user.roles ?? []))
  } catch (err) {
    form.setError('password', {
      message: 'Invalid email or password',
    })
  }
}

  return (
    <div className="relative flex min-h-screen items-center justify-center overflow-hidden bg-background p-4">
      <BackgroundPattern />
      <div className="relative w-full max-w-md">
        <div className="mb-6 flex items-center justify-center gap-2.5">
          <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-primary text-primary-foreground shadow-lg shadow-primary/30">
            <KeyRound className="h-5 w-5" />
          </div>
          <span className="font-display text-lg font-semibold tracking-tight">Aegis IAM</span>
        </div>

        <div className="rounded-2xl border border-border bg-card p-7 shadow-2xl shadow-black/40">
          <div className="mb-6 text-center">
            <h1 className="font-display text-2xl font-semibold">Sign in to your account</h1>
            <p className="mt-1 text-sm text-muted-foreground">Welcome back. Enter your credentials.</p>
          </div>

          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4" noValidate>
            <div className="space-y-1.5">
              <Label htmlFor="email">Email</Label>
              <Input id="email" type="email" autoComplete="email" {...form.register('email')} />
              {form.formState.errors.email && (
                <p className="text-xs text-[color:var(--destructive)]">{form.formState.errors.email.message}</p>
              )}
            </div>

            <div className="space-y-1.5">
              <div className="flex items-center justify-between">
                <Label htmlFor="password">Password</Label>
              </div>
              <PasswordInput id="password" autoComplete="current-password" {...form.register('password')} />
              {form.formState.errors.password && (
                <p className="text-xs text-[color:var(--destructive)]">{form.formState.errors.password.message}</p>
              )}
            </div>

            <div className="flex items-center gap-2">
              <Checkbox id="remember" {...form.register('remember')} />
              <Label htmlFor="remember" className="text-sm text-muted-foreground">
                Remember me
              </Label>
            </div>

            <Button type="submit" className="w-full" disabled={isLoading}>
              {isLoading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
              Sign In
            </Button>
          </form>

          <div className="my-5 flex items-center gap-3">
            <div className="h-px flex-1 bg-border" />
            <span className="text-xs uppercase tracking-wider text-muted-foreground">or continue with</span>
            <div className="h-px flex-1 bg-border" />
          </div>

          <Button variant="secondary" className="w-full" asChild>
            <a href={GOOGLE_OAUTH_URL}>
              <GoogleIcon className="mr-2 h-4 w-4" />
              Continue with Google
            </a>
          </Button>

          <div className="mt-6 space-y-1.5 text-center text-sm">
            <p className="text-muted-foreground">
              Don't have an account?{' '}
              <Link to="/onboard" className="font-medium text-primary hover:underline">
                Get started
              </Link>
            </p>
            <p className="text-muted-foreground">
              Or{' '}
              <Link to="/register" className="font-medium text-primary hover:underline">
                register with email
              </Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  )
}

function BackgroundPattern() {
  return (
    <div
      aria-hidden
      className="pointer-events-none absolute inset-0 opacity-40"
      style={{
        backgroundImage:
          'radial-gradient(at 20% 10%, color-mix(in oklab, var(--primary) 35%, transparent) 0px, transparent 50%), radial-gradient(at 80% 80%, color-mix(in oklab, var(--primary) 25%, transparent) 0px, transparent 50%)',
      }}
    >
      <div
        className="absolute inset-0 opacity-[0.06]"
        style={{
          backgroundImage:
            'linear-gradient(var(--border) 1px, transparent 1px), linear-gradient(90deg, var(--border) 1px, transparent 1px)',
          backgroundSize: '32px 32px',
        }}
      />
    </div>
  )
}


export default Login