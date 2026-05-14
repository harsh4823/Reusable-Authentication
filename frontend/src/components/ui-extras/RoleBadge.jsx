import { cn } from '@/lib/utils'
import { Shield, ShieldCheck, User } from 'lucide-react'

const roleConfig = {
  ROLE_ADMIN: {
    label: 'Admin',
    color: 'bg-[color:color-mix(in_oklab,var(--primary)_25%,transparent)] text-[color:var(--primary)]',
    Icon: ShieldCheck,
  },
  ROLE_CLIENT: {
    label: 'Developer',
    color: 'bg-[color:color-mix(in_oklab,var(--warning)_25%,transparent)] text-[color:var(--warning)]',
    Icon: Shield,
  },
  ROLE_USER: {
    label: 'User',
    color: 'bg-muted text-muted-foreground',
    Icon: User,
  },
}

export function RoleBadge({ role, className }) {
  const cfg = roleConfig[role] ?? { label: role, color: 'bg-muted text-muted-foreground', Icon: User }
  const Icon = cfg.Icon

  return (
    <span className={cn('inline-flex items-center gap-1 rounded-full px-2 py-0.5 text-xs font-medium', cfg.color, className)}>
      <Icon className="h-3 w-3" />
      {cfg.label}
    </span>
  )
}