import { useState } from 'react'
import { Eye, EyeOff } from 'lucide-react'
import { Input } from '@/components/ui/input'
import { Button } from '@/components/ui/button'
import { cn } from '@/lib/utils'

const LEVELS = ['weak', 'fair', 'strong']

const strengthColor = {
  weak: 'bg-[color:var(--destructive)]',
  fair: 'bg-[color:var(--warning)]',
  strong: 'bg-[color:var(--success)]',
}

export function PasswordInput({ className, showStrength, strength, ...props }) {
  const [visible, setVisible] = useState(false)

  return (
    <div className="space-y-1.5">
      <div className="relative">
        <Input
          {...props}
          type={visible ? 'text' : 'password'}
          className={cn('pr-10', className)}
        />
        <Button
          type="button"
          variant="ghost"
          size="icon"
          className="absolute right-1 top-1/2 h-8 w-8 -translate-y-1/2 text-muted-foreground"
          onClick={() => setVisible((v) => !v)}
          aria-label={visible ? 'Hide password' : 'Show password'}
          tabIndex={-1}
        >
          {visible ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
        </Button>
      </div>
      {showStrength && strength && (
        <div className="flex items-center gap-2">
          <div className="flex flex-1 gap-1">
            {LEVELS.map((lvl, i) => {
              const active =
                (strength === 'weak' && i === 0) ||
                (strength === 'fair' && i <= 1) ||
                (strength === 'strong' && i <= 2)
              return (
                <span
                  key={lvl}
                  className={cn('h-1 flex-1 rounded-full transition-colors', active ? strengthColor[strength] : 'bg-border')}
                />
              )
            })}
          </div>
          <span className="text-xs capitalize text-muted-foreground">{strength}</span>
        </div>
      )}
    </div>
  )
}