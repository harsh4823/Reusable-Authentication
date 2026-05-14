import { cn } from '@/lib/utils'

const styles = {
  enabled: 'bg-[color:color-mix(in_oklab,var(--success)_25%,transparent)] text-[color:var(--success)] ring-1 ring-inset ring-[color:color-mix(in_oklab,var(--success)_40%,transparent)]',
  active: 'bg-[color:color-mix(in_oklab,var(--success)_25%,transparent)] text-[color:var(--success)] ring-1 ring-inset ring-[color:color-mix(in_oklab,var(--success)_40%,transparent)]',
  disabled: 'bg-muted text-muted-foreground ring-1 ring-inset ring-border',
  revoked: 'bg-[color:color-mix(in_oklab,var(--destructive)_25%,transparent)] text-[color:var(--destructive)] ring-1 ring-inset ring-[color:color-mix(in_oklab,var(--destructive)_40%,transparent)]',
}

export function StatusBadge({ status, className }) {
  return (
    <span className={cn('inline-flex items-center gap-1.5 rounded-full px-2.5 py-0.5 text-xs font-medium capitalize', styles[status], className)}>
      <span className="h-1.5 w-1.5 rounded-full bg-current" />
      {status}
    </span>
  )
}