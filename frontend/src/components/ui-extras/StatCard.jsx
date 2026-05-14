import { cn } from '@/lib/utils'

export function StatCard({ icon: Icon, label, value, trend, className }) {
  return (
    <div className={cn('group rounded-xl border border-border bg-card p-5 shadow-lg shadow-black/20 transition-colors hover:border-primary/50', className)}>
      <div className="flex items-center justify-between">
        <p className="text-sm font-medium text-muted-foreground">{label}</p>
        <div className="rounded-lg bg-primary/10 p-2 text-primary">
          <Icon className="h-4 w-4" />
        </div>
      </div>
      <p className="mt-3 font-display text-3xl font-semibold tracking-tight">{value}</p>
      {trend && <p className="mt-1 text-xs text-muted-foreground">{trend}</p>}
    </div>
  )
}