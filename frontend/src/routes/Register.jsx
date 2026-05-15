import { useState } from 'react'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import { useForm, useWatch } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { toast } from 'sonner'
import { Loader2, KeyRound } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { AvatarUpload } from './../components/ui-extras/AvatarUpload'
import { registerSchema, passwordStrength } from '@/lib/validators'
import { useRegisterMutation } from '@/store/api/auth-api'
import { useAuth, rootRedirectFor } from '@/lib/auth-helpers'
import { PasswordInput } from './../components/ui-extras/PasswordInput'


const Register = () => {
  const { isAuthenticated, user } = useAuth()
  const navigate = useNavigate()
  const [registerUser, { isLoading }] = useRegisterMutation()
  const [avatar, setAvatar] = useState(null)

  const form = useForm({
    resolver: zodResolver(registerSchema),
    defaultValues: { fullName: '', email: '', password: '', confirmPassword: '' },
  })

  const pw = useWatch({ control: form.control, name: 'password' })
  const strength = pw ? passwordStrength(pw) : undefined

  if (isAuthenticated) return <Navigate to={rootRedirectFor(user?.roles ?? [])} replace />

  const onSubmit = async (values) => {
    try {
      const fd = new FormData()
      fd.append('fullName', values.fullName)
      fd.append('email', values.email)
      fd.append('password', values.password)
      if (avatar) fd.append('profilePicture', avatar)
      await registerUser(fd).unwrap()
      toast.success('Account created! Please sign in.')
      navigate('/login')
    } catch (err) {
      const status = err?.status
      if (status === 409) {
        form.setError('email', { message: 'An account with this email already exists' })
      } else {
        toast.error('Could not create your account')
      }
    }
  }

  return (
    <div className="grid min-h-screen lg:grid-cols-2">
      {/* Form column */}
      <div className="flex items-center justify-center p-6 lg:p-10">
        <div className="w-full max-w-md">
          <Link to="/" className="mb-6 flex items-center gap-2.5">
            <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-primary text-primary-foreground">
              <KeyRound className="h-4.5 w-4.5" />
            </div>
            <span className="font-display text-base font-semibold">Aegis IAM</span>
          </Link>

          <h1 className="font-display text-2xl font-semibold">Create your account</h1>
          <p className="mt-1 text-sm text-muted-foreground">Set up your developer profile in under a minute.</p>

          <form onSubmit={form.handleSubmit(onSubmit)} className="mt-6 space-y-4" noValidate>
            <AvatarUpload value={avatar} onChange={setAvatar} />

            <div className="space-y-1.5">
              <Label htmlFor="fullName">Full name</Label>
              <Input id="fullName" autoComplete="name" {...form.register('fullName')} />
              {form.formState.errors.fullName && (
                <p className="text-xs text-[color:var(--destructive)]">{form.formState.errors.fullName.message}</p>
              )}
            </div>

            <div className="space-y-1.5">
              <Label htmlFor="email">Email</Label>
              <Input id="email" type="email" autoComplete="email" {...form.register('email')} />
              {form.formState.errors.email && (
                <p className="text-xs text-[color:var(--destructive)]">{form.formState.errors.email.message}</p>
              )}
            </div>

            <div className="space-y-1.5">
              <Label htmlFor="password">Password</Label>
              <PasswordInput
                id="password"
                autoComplete="new-password"
                showStrength
                strength={strength}
                {...form.register('password')}
              />
              {form.formState.errors.password && (
                <p className="text-xs text-[color:var(--destructive)]">{form.formState.errors.password.message}</p>
              )}
            </div>

            <div className="space-y-1.5">
              <Label htmlFor="confirmPassword">Confirm password</Label>
              <PasswordInput
                id="confirmPassword"
                autoComplete="new-password"
                {...form.register('confirmPassword')}
              />
              {form.formState.errors.confirmPassword && (
                <p className="text-xs text-[color:var(--destructive)]">
                  {form.formState.errors.confirmPassword.message}
                </p>
              )}
            </div>

            <Button type="submit" className="w-full" disabled={isLoading}>
              {isLoading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
              Create Account
            </Button>
          </form>

          <p className="mt-6 text-center text-sm text-muted-foreground">
            Already have an account?{' '}
            <Link to="/login" className="font-medium text-primary hover:underline">
              Sign in
            </Link>
          </p>
        </div>
      </div>

      {/* Brand column */}
      <div className="relative hidden overflow-hidden bg-card lg:block">
        <div
          className="absolute inset-0"
          style={{
            backgroundImage:
              'radial-gradient(at 30% 30%, color-mix(in oklab, var(--primary) 50%, transparent) 0px, transparent 60%), radial-gradient(at 70% 80%, color-mix(in oklab, var(--primary) 30%, transparent) 0px, transparent 50%)',
          }}
        />
        <div
          className="absolute inset-0 opacity-[0.05]"
          style={{
            backgroundImage:
              'linear-gradient(var(--border) 1px, transparent 1px), linear-gradient(90deg, var(--border) 1px, transparent 1px)',
            backgroundSize: '40px 40px',
          }}
        />
        <div className="relative flex h-full flex-col justify-end p-12">
          <h2 className="font-display text-3xl font-semibold leading-tight">
            Identity infrastructure built for developers.
          </h2>
          <p className="mt-3 max-w-md text-muted-foreground">
            Realms, roles, OAuth2 clients, and JWT — fully managed, ready in seconds.
          </p>
          <div className="mt-8 grid grid-cols-3 gap-3">
            {['Realms', 'OAuth2', 'RBAC'].map((t) => (
              <div
                key={t}
                className="rounded-lg border border-border bg-background/40 px-3 py-2 text-center text-xs font-medium text-muted-foreground"
              >
                {t}
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  )
}

export default Register
