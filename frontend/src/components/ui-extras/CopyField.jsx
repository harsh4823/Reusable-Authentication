import { useState } from 'react'
import { Copy, Check } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { cn } from '@/lib/utils'

export function CopyField({ value, label, mono = true, className }) {
  const [copied, setCopied] = useState(false)

  const onCopy = async () => {
    try {
      await navigator.clipboard.writeText(value)
      setCopied(true)
      setTimeout(() => setCopied(false), 1600)
    } catch {
      /* noop */
    }
  }

  return (
    <div className={cn('space-y-1.5', className)}>
      {label && <label className="text-xs font-medium text-muted-foreground">{label}</label>}
      <div className="flex items-stretch gap-2">
        <div className={cn('flex-1 truncate rounded-lg border border-border bg-secondary/50 px-3 py-2 text-sm', mono && 'font-mono')}>
          {value}
        </div>
        <Button type="button" variant="secondary" size="icon" onClick={onCopy} aria-label="Copy">
          {copied ? <Check className="h-4 w-4 text-[oklch(var(--success))]" /> : <Copy className="h-4 w-4" />}
        </Button>
      </div>
    </div>
  )
}