import { useState } from 'react'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import { useForm, useWatch } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { toast } from 'sonner'
import { ArrowLeft, ArrowRight, Check, KeyRound, Loader2, Rocket, Sparkles } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Checkbox } from '@/components/ui/checkbox'
import { CopyField } from './../components/ui-extras/CopyField'
import { onboardSchema, passwordStrength } from '@/lib/validators'
import { useOnboardMutation } from '@/store/api/onboard-api'
import { useAppDispatch } from '@/store/hooks'
import { setCredentials } from '@/store/auth-slice'
import { useAuth, rootRedirectFor } from '@/lib/auth-helpers'
import { cn } from '@/lib/utils'
import { PasswordInput } from './../components/ui-extras/PasswordInput';

const STEPS = [
  { id: 1, title: 'Your Account', description: 'Create your developer profile' },
  { id: 2, title: 'Your Application', description: 'Configure your first realm' },
  { id: 3, title: 'Review & Launch', description: 'Confirm and go live' },
]

const Onboard = () => {
  const { isAuthenticated, user } = useAuth()
  const navigate = useNavigate()
  const dispatch = useAppDispatch()
  const [onboard, { isLoading }] = useOnboardMutation()
  const [step, setStep] = useState(1)
  const [acknowledged, setAcknowledged] = useState(false)
  const [credentials, setCredentialsState] = useState(null)

  const form = useForm({
    resolver: zodResolver(onboardSchema),
    mode: 'onTouched',
    defaultValues: {
      fullName: '',
      email: '',
      password: '',
      confirmPassword: '',
      appName: '',
      realmName: '',
      redirectUri: 'http://localhost:3000/callback',
    },
  })

  // useWatch instead of form.watch() — React Compiler compatible
  const pw = useWatch({ control: form.control, name: 'password' })
  const appName = useWatch({ control: form.control, name: 'appName' })
  const realmName = useWatch({ control: form.control, name: 'realmName' })

  if (isAuthenticated && !credentials) {
    return <Navigate to={rootRedirectFor(user?.roles ?? [])} replace />
  }

  const goNext = async () => {
    const fields = {
      1: ['fullName', 'email', 'password', 'confirmPassword'],
      2: ['appName', 'realmName', 'redirectUri'],
    }
    const ok = await form.trigger(fields[step])
    if (ok) setStep((s) => Math.min(3, s + 1))
  }

  const onLaunch = async () => {
    const valid = await form.trigger()
    if (!valid) return
    if (!acknowledged) {
      toast.error('Please acknowledge the secret notice')
      return
    }
    const v = form.getValues()
    try {
      const res = await onboard({
        fullName: v.fullName,
        email: v.email,
        password: v.password,
        appName: v.appName,
        realmName: v.realmName,
        redirectUri: v.redirectUri,
      }).unwrap()
      setCredentialsState(res)
      dispatch(
        setCredentials({
          accessToken: res.accessToken,
          refreshToken: res.refreshToken,
          user: { ...res.user, roles: res.user.roles ?? ['ROLE_CLIENT'] },
        })
      )
      toast.success('Your realm is live')
    } catch (err) {
      const status = err?.status
      if (status === 409) toast.error('That realm name or email is already taken')
      else toast.error('Could not launch your application')
    }
  }

  if (credentials) {
    return <CredentialsScreen data={credentials} onContinue={() => navigate('/dashboard')} />
  }

  return (
    <div className="min-h-screen bg-background">
      <div className="mx-auto flex min-h-screen max-w-5xl flex-col px-6 py-8">
        <div className="flex items-center justify-between">
          <Link to="/" className="flex items-center gap-2.5">
            <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-primary text-primary-foreground">
              <KeyRound className="h-4 w-4" />
            </div>
            <span className="font-display text-base font-semibold">Aegis IAM</span>
          </Link>
          <Link to="/login" className="text-sm text-muted-foreground hover:text-foreground">
            Already have an account? Sign in
          </Link>
        </div>

        <Stepper current={step} />

        <div className="mt-8 grid flex-1 gap-8 md:grid-cols-[1fr_320px]">
          <div className="rounded-2xl border border-border bg-card p-7 shadow-xl shadow-black/30">
            {step === 1 && <StepAccount form={form} strength={pw ? passwordStrength(pw) : undefined} />}
            {step === 2 && <StepApplication form={form} />}
            {step === 3 && (
              <StepReview
                values={form.getValues()}
                acknowledged={acknowledged}
                setAcknowledged={setAcknowledged}
              />
            )}

            <div className="mt-8 flex items-center justify-between border-t border-border pt-6">
              <Button
                type="button"
                variant="ghost"
                onClick={() => setStep((s) => Math.max(1, s - 1))}
                disabled={step === 1 || isLoading}
              >
                <ArrowLeft className="mr-2 h-4 w-4" />
                Back
              </Button>
              {step < 3 ? (
                <Button type="button" onClick={goNext}>
                  Continue
                  <ArrowRight className="ml-2 h-4 w-4" />
                </Button>
              ) : (
                <Button type="button" onClick={onLaunch} disabled={isLoading || !acknowledged}>
                  {isLoading ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : <Rocket className="mr-2 h-4 w-4" />}
                  Launch My App
                </Button>
              )}
            </div>
          </div>

          <aside className="hidden md:block">
            {/* pass only what SidePanel needs — avoids form.watch() with no args */}
            <SidePanel step={step} appName={appName} realmName={realmName} />
          </aside>
        </div>
      </div>
    </div>
  )
}

function Stepper({ current }) {
  return (
    <div className="mt-10">
      <div className="flex items-center justify-between gap-3">
        {STEPS.map((s, i) => {
          const done = current > s.id
          const active = current === s.id
          return (
            <div key={s.id} className="flex flex-1 items-center gap-3">
              <div
                className={cn(
                  'flex h-8 w-8 shrink-0 items-center justify-center rounded-full border text-sm font-medium transition-colors',
                  done && 'border-primary bg-primary text-primary-foreground',
                  active && 'border-primary bg-primary/10 text-primary',
                  !done && !active && 'border-border text-muted-foreground'
                )}
              >
                {done ? <Check className="h-4 w-4" /> : s.id}
              </div>
              <div className="hidden min-w-0 sm:block">
                <p className={cn('text-sm font-medium', active ? 'text-foreground' : 'text-muted-foreground')}>
                  {s.title}
                </p>
                <p className="text-xs text-muted-foreground">{s.description}</p>
              </div>
              {i < STEPS.length - 1 && (
                <div className={cn('ml-2 h-px flex-1', done ? 'bg-primary' : 'bg-border')} />
              )}
            </div>
          )
        })}
      </div>
    </div>
  )
}

function StepAccount({ form, strength }) {
  return (
    <div>
      <h2 className="font-display text-xl font-semibold">Your account</h2>
      <p className="mt-1 text-sm text-muted-foreground">We'll use this to sign you in.</p>
      <div className="mt-6 grid gap-4 sm:grid-cols-2">
        <div className="space-y-1.5 sm:col-span-2">
          <Label htmlFor="fullName">Full name</Label>
          <Input id="fullName" {...form.register('fullName')} />
          {form.formState.errors.fullName && (
            <p className="text-xs text-[color:var(--destructive)]">{form.formState.errors.fullName.message}</p>
          )}
        </div>
        <div className="space-y-1.5 sm:col-span-2">
          <Label htmlFor="email">Email</Label>
          <Input id="email" type="email" {...form.register('email')} />
          {form.formState.errors.email && (
            <p className="text-xs text-[color:var(--destructive)]">{form.formState.errors.email.message}</p>
          )}
        </div>
        <div className="space-y-1.5">
          <Label htmlFor="password">Password</Label>
          <PasswordInput id="password" showStrength strength={strength} {...form.register('password')} />
          {form.formState.errors.password && (
            <p className="text-xs text-[color:var(--destructive)]">{form.formState.errors.password.message}</p>
          )}
        </div>
        <div className="space-y-1.5">
          <Label htmlFor="confirmPassword">Confirm password</Label>
          <PasswordInput id="confirmPassword" {...form.register('confirmPassword')} />
          {form.formState.errors.confirmPassword && (
            <p className="text-xs text-[color:var(--destructive)]">
              {form.formState.errors.confirmPassword.message}
            </p>
          )}
        </div>
      </div>
    </div>
  )
}

function StepApplication({ form }) {
  return (
    <div>
      <h2 className="font-display text-xl font-semibold">Your application</h2>
      <p className="mt-1 text-sm text-muted-foreground">A realm is an isolated tenant for your app's users.</p>
      <div className="mt-6 grid gap-4">
        <div className="space-y-1.5">
          <Label htmlFor="appName">App name</Label>
          <Input id="appName" placeholder="Acme Storefront" {...form.register('appName')} />
          {form.formState.errors.appName && (
            <p className="text-xs text-[color:var(--destructive)]">{form.formState.errors.appName.message}</p>
          )}
        </div>
        <div className="space-y-1.5">
          <Label htmlFor="realmName">Realm name</Label>
          <Input id="realmName" placeholder="acme-storefront" className="font-mono" {...form.register('realmName')} />
          <p className="text-xs text-muted-foreground">
            Lowercase letters, numbers, and hyphens. This becomes part of your URLs.
          </p>
          {form.formState.errors.realmName && (
            <p className="text-xs text-[color:var(--destructive)]">{form.formState.errors.realmName.message}</p>
          )}
        </div>
        <div className="space-y-1.5">
          <Label htmlFor="redirectUri">Redirect URI</Label>
          <Input
            id="redirectUri"
            placeholder="https://yourapp.com/callback"
            className="font-mono"
            {...form.register('redirectUri')}
          />
          {form.formState.errors.redirectUri && (
            <p className="text-xs text-[color:var(--destructive)]">{form.formState.errors.redirectUri.message}</p>
          )}
        </div>
      </div>
    </div>
  )
}

function StepReview({ values, acknowledged, setAcknowledged }) {
  return (
    <div>
      <h2 className="font-display text-xl font-semibold">Review & launch</h2>
      <p className="mt-1 text-sm text-muted-foreground">Confirm your setup. You'll receive your credentials next.</p>
      <div className="mt-6 grid gap-3">
        <ReviewRow label="Name" value={values.fullName} />
        <ReviewRow label="Email" value={values.email} />
        <ReviewRow label="App name" value={values.appName} />
        <ReviewRow label="Realm" value={values.realmName} mono />
        <ReviewRow label="Redirect URI" value={values.redirectUri} mono />
      </div>
      <label className="mt-6 flex cursor-pointer items-start gap-3 rounded-lg border border-border bg-secondary/40 p-4">
        <Checkbox
          checked={acknowledged}
          onCheckedChange={(c) => setAcknowledged(Boolean(c))}
          className="mt-0.5"
        />
        <div className="text-sm">
          <p className="font-medium">I understand my client secret will only be shown once.</p>
          <p className="mt-0.5 text-xs text-muted-foreground">
            Save it securely as soon as it appears. You can regenerate it later if lost.
          </p>
        </div>
      </label>
    </div>
  )
}

function ReviewRow({ label, value, mono }) {
  return (
    <div className="flex items-center justify-between rounded-lg border border-border bg-secondary/30 px-4 py-2.5">
      <span className="text-xs uppercase tracking-wider text-muted-foreground">{label}</span>
      <span className={cn('text-sm', mono && 'font-mono')}>{value || '—'}</span>
    </div>
  )
}

// Receives specific values instead of form.watch() — avoids stale memoization
function SidePanel({ step, appName, realmName }) {
  return (
    <div className="sticky top-8 space-y-4">
      <div className="rounded-2xl border border-border bg-card p-5">
        <div className="flex items-center gap-2 text-sm font-medium">
          <Sparkles className="h-4 w-4 text-primary" />
          One-click setup
        </div>
        <p className="mt-2 text-sm text-muted-foreground">
          We'll create your account, your first realm, and an OAuth2 client in a single request.
        </p>
      </div>
      <div className="rounded-2xl border border-border bg-card p-5">
        <p className="text-xs uppercase tracking-wider text-muted-foreground">Preview</p>
        <p className="mt-3 font-display text-lg font-semibold">{appName || 'Your application'}</p>
        <p className="font-mono text-xs text-muted-foreground">{realmName || 'your-realm'}</p>
        <div className="mt-4 space-y-2 text-xs text-muted-foreground">
          <div>Step {step} of 3</div>
        </div>
      </div>
    </div>
  )
}

function CredentialsScreen({ data, onContinue }) {
  return (
    <div className="min-h-screen bg-background">
      <div className="mx-auto max-w-3xl px-6 py-12">
        <div className="mb-6 flex items-center gap-2.5">
          <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-primary text-primary-foreground">
            <KeyRound className="h-4 w-4" />
          </div>
          <span className="font-display text-base font-semibold">Aegis IAM</span>
        </div>
        <div className="rounded-2xl border border-border bg-card p-7 shadow-xl shadow-black/30">
          <div className="flex items-center gap-3">
            <div className="flex h-10 w-10 items-center justify-center rounded-full bg-[color:color-mix(in_oklab,var(--success)_25%,transparent)] text-[color:var(--success)]">
              <Check className="h-5 w-5" />
            </div>
            <div>
              <h1 className="font-display text-xl font-semibold">Your realm is live</h1>
              <p className="text-sm text-muted-foreground">Save these credentials — the secret is shown only once.</p>
            </div>
          </div>
          <div className="mt-6 space-y-4">
            <CopyField label="Client ID" value={data.clientId} />
            <CopyField label="Client Secret (one-time)" value={data.clientSecret} />
            <div className="grid gap-4 md:grid-cols-2">
              <CopyField label="Access Token" value={data.accessToken} />
              <CopyField label="Refresh Token" value={data.refreshToken} />
            </div>
            <CopyField label="Well-Known URL" value={data.wellKnownUrl} mono={false} />
            <CopyField label="Token Endpoint" value={data.tokenEndpoint} mono={false} />
          </div>
          <div className="mt-7 flex justify-end">
            <Button onClick={onContinue}>
              Go to Dashboard
              <ArrowRight className="ml-2 h-4 w-4" />
            </Button>
          </div>
        </div>
      </div>
    </div>
  )
}

export default Onboard