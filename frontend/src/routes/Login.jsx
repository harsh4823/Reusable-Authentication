import { Link, Navigate, useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { toast } from 'sonner'
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

const Login = () => {
  const { isAuthenticated, user } = useAuth()
  const navigate = useNavigate()
  const dispatch = useAppDispatch()
  const [login, { isLoading }] = useLoginMutation()

  const form = useForm({
    resolver: zodResolver(loginSchema),
    defaultValues: { email: '', password: '', remember: false },
  })

  if (isAuthenticated) return <Navigate to={rootRedirectFor(user?.roles ?? [])} replace />

  const onSubmit = async (values) => {
    try {
      const res = await login({ email: values.email, password: values.password }).unwrap()
      dispatch(
        setCredentials({
          accessToken: res.accessToken,
          refreshToken: res.refreshToken,
          user: { ...res.user, roles: res.user.roles ?? [] },
        })
      )
      toast.success('Welcome back')
      navigate(rootRedirectFor(res.user.roles ?? []))
    } catch (err) {
      const status = err?.status
      if (status === 401) {
        form.setError('password', { message: 'Invalid email or password' })
      } else {
        toast.error('Something went wrong, try again')
      }
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

function GoogleIcon({ className }) {
  return (
    <svg className={className} viewBox="0 0 24 24" aria-hidden="true">
      <path
        fill="#EA4335"
        d="M12 10.2v3.9h5.5c-.24 1.4-1.66 4.1-5.5 4.1-3.31 0-6-2.74-6-6.1s2.69-6.1 6-6.1c1.88 0 3.14.8 3.86 1.49l2.63-2.53C16.9 3.4 14.66 2.4 12 2.4 6.86 2.4 2.7 6.56 2.7 11.7s4.16 9.3 9.3 9.3c5.37 0 8.92-3.77 8.92-9.07 0-.61-.07-1.08-.16-1.55H12z"
      />
    </svg>
  )
}

export default Login