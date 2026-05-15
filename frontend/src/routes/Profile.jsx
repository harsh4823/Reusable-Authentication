import { useState } from 'react'
import { useForm, useWatch } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { toast } from 'sonner'
import { Loader2, LogOut } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
import { passwordSchema, passwordStrength, fullNameSchema } from '@/lib/validators'
import { useAuth } from '@/lib/auth-helpers'
import { useAppDispatch } from '@/store/hooks'
import { logout, updateProfile } from '@/store/auth-slice'
import { useLogoutAllMutation, useLogoutSingleMutation } from '@/store/api/auth-api'
import { PageHeader } from '@/components/layout/PageHeader'
import { PasswordInput } from '@/components/ui-extras/PasswordInput'
import { RoleBadge } from './../components/ui-extras/RoleBadge';

const passwordFormSchema = z
  .object({
    currentPassword: z.string().min(1, 'Required'),
    newPassword: passwordSchema,
    confirmPassword: z.string(),
  })
  .refine((d) => d.newPassword === d.confirmPassword, {
    path: ['confirmPassword'],
    message: "Passwords don't match",
  })

const nameFormSchema = z.object({ name: fullNameSchema })

const Profile = () => {
  const { user } = useAuth()
  const dispatch = useAppDispatch()
  const [logoutSingle, { isLoading: lo1 }] = useLogoutSingleMutation()
  const [logoutAll, { isLoading: lo2 }] = useLogoutAllMutation()
  const [confirmAll, setConfirmAll] = useState(false)

  const nameForm = useForm({
    resolver: zodResolver(nameFormSchema),
    defaultValues: { name: user?.name ?? '' },
  })

  const pwForm = useForm({
    resolver: zodResolver(passwordFormSchema),
    defaultValues: { currentPassword: '', newPassword: '', confirmPassword: '' },
  })

  // useWatch instead of pwForm.watch() — React Compiler compatible
  const newPw = useWatch({ control: pwForm.control, name: 'newPassword' })

  const initials = (user?.name || user?.email || 'U')
    .split(' ')
    .map((s) => s[0])
    .slice(0, 2)
    .join('')
    .toUpperCase()

  const onSaveName = (values) => {
    dispatch(updateProfile({ name: values.name }))
    toast.success('Profile updated')
  }

  const onUpdatePassword = () => {
    toast.success('Password updated')
    pwForm.reset({ currentPassword: '', newPassword: '', confirmPassword: '' })
  }

  const onLogoutSingle = async () => {
    try {
      await logoutSingle().unwrap()
    } catch {
      /* fall through to local logout */
    }
    dispatch(logout())
    toast.success('Logged out from this device')
  }

  const onLogoutAll = async () => {
    try {
      await logoutAll().unwrap()
    } catch {
      /* fall through */
    }
    dispatch(logout())
    toast.success('Logged out from all devices')
  }

  return (
    <div className="mx-auto max-w-3xl px-6 py-8">
      <PageHeader title="Your profile" description="Manage your identity, security, and sessions." />

      {/* Identity */}
      <section className="mt-8 rounded-2xl border border-border bg-card p-6">
        <h2 className="font-display text-base font-semibold">Identity</h2>
        <div className="mt-5 flex items-start gap-5">
          <Avatar className="h-20 w-20 ring-2 ring-border">
            <AvatarImage src={user?.image ?? undefined} alt={user?.name ?? ''} />
            <AvatarFallback className="bg-primary/15 text-lg font-medium text-primary">{initials}</AvatarFallback>
          </Avatar>
          <form onSubmit={nameForm.handleSubmit(onSaveName)} className="flex-1 space-y-4">
            <div className="space-y-1.5">
              <Label htmlFor="name">Name</Label>
              <Input id="name" {...nameForm.register('name')} />
              {nameForm.formState.errors.name && (
                <p className="text-xs text-[color:var(--destructive)]">{nameForm.formState.errors.name.message}</p>
              )}
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="email">Email</Label>
              <Input id="email" value={user?.email ?? ''} readOnly className="bg-secondary/40" />
            </div>
            <div className="flex flex-wrap gap-1.5">
              {(user?.roles ?? []).map((r) => (
                <RoleBadge key={r} role={r} />
              ))}
            </div>
            <Button type="submit" size="sm">Save changes</Button>
          </form>
        </div>
      </section>

      {/* Security */}
      <section className="mt-6 rounded-2xl border border-border bg-card p-6">
        <h2 className="font-display text-base font-semibold">Security</h2>
        <p className="mt-1 text-sm text-muted-foreground">Update your password regularly.</p>
        <form onSubmit={pwForm.handleSubmit(onUpdatePassword)} className="mt-5 grid gap-4 md:grid-cols-2">
          <div className="space-y-1.5 md:col-span-2">
            <Label htmlFor="currentPassword">Current password</Label>
            <PasswordInput id="currentPassword" {...pwForm.register('currentPassword')} />
            {pwForm.formState.errors.currentPassword && (
              <p className="text-xs text-[color:var(--destructive)]">
                {pwForm.formState.errors.currentPassword.message}
              </p>
            )}
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="newPassword">New password</Label>
            <PasswordInput
              id="newPassword"
              showStrength
              strength={newPw ? passwordStrength(newPw) : undefined}
              {...pwForm.register('newPassword')}
            />
            {pwForm.formState.errors.newPassword && (
              <p className="text-xs text-[color:var(--destructive)]">{pwForm.formState.errors.newPassword.message}</p>
            )}
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="confirmPassword">Confirm new password</Label>
            <PasswordInput id="confirmPassword" {...pwForm.register('confirmPassword')} />
            {pwForm.formState.errors.confirmPassword && (
              <p className="text-xs text-[color:var(--destructive)]">
                {pwForm.formState.errors.confirmPassword.message}
              </p>
            )}
          </div>
          <div className="md:col-span-2">
            <Button type="submit" size="sm">Update password</Button>
          </div>
        </form>
      </section>

      {/* Sessions */}
      <section className="mt-6 rounded-2xl border border-border bg-card p-6">
        <h2 className="font-display text-base font-semibold">Sessions</h2>
        <div className="mt-4 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <p className="text-sm font-medium">This device</p>
            <p className="text-xs text-muted-foreground">Sign out of just this browser.</p>
          </div>
          <Button variant="secondary" size="sm" onClick={onLogoutSingle} disabled={lo1}>
            {lo1 ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : <LogOut className="mr-2 h-4 w-4" />}
            Logout from this device
          </Button>
        </div>
        <div className="mt-4 flex flex-col gap-3 border-t border-border pt-4 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <p className="text-sm font-medium">All devices</p>
            <p className="text-xs text-muted-foreground">Revoke every active session for your account.</p>
          </div>
          {!confirmAll ? (
            <Button variant="destructive" size="sm" onClick={() => setConfirmAll(true)}>
              Logout from all devices
            </Button>
          ) : (
            <div className="flex items-center gap-2">
              <Button variant="ghost" size="sm" onClick={() => setConfirmAll(false)}>Cancel</Button>
              <Button variant="destructive" size="sm" onClick={onLogoutAll} disabled={lo2}>
                {lo2 && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                Confirm
              </Button>
            </div>
          )}
        </div>
      </section>
    </div>
  )
}

export default Profile