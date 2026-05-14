import { useRef, useState } from 'react'
import { Camera, Upload } from 'lucide-react'
import { cn } from '@/lib/utils'

export function AvatarUpload({ value, onChange, size = 96, className }) {
  const inputRef = useRef(null)
  const [dragOver, setDragOver] = useState(false)
  const [previewUrl, setPreviewUrl] = useState(null)

  const accept = (file) => {
    if (!file) return
    if (!/^image\/(png|jpe?g|webp)$/.test(file.type)) return
    if (file.size > 5 * 1024 * 1024) return
    onChange(file)
    setPreviewUrl(URL.createObjectURL(file))
  }

  return (
    <div className={cn('flex items-center gap-4', className)}>
      <button
        type="button"
        onClick={() => inputRef.current?.click()}
        onDragOver={(e) => { e.preventDefault(); setDragOver(true) }}
        onDragLeave={() => setDragOver(false)}
        onDrop={(e) => { e.preventDefault(); setDragOver(false); accept(e.dataTransfer.files?.[0] ?? null) }}
        className={cn(
          'group relative flex shrink-0 items-center justify-center overflow-hidden rounded-full border-2 border-dashed border-border bg-secondary/40 transition-colors',
          dragOver && 'border-primary bg-primary/10'
        )}
        style={{ width: size, height: size }}
        aria-label="Upload profile picture"
      >
        {previewUrl ? (
          <>
            <img src={previewUrl} alt="Preview" className="h-full w-full object-cover" />
            <span className="absolute inset-0 flex items-center justify-center bg-black/50 opacity-0 transition-opacity group-hover:opacity-100">
              <Camera className="h-5 w-5 text-white" />
            </span>
          </>
        ) : (
          <Upload className="h-6 w-6 text-muted-foreground" />
        )}
      </button>
      <div className="text-sm">
        <p className="font-medium">Profile picture</p>
        <p className="text-xs text-muted-foreground">PNG, JPG, or WEBP — max 5MB</p>
        {value && <p className="mt-1 text-xs text-muted-foreground">{value.name}</p>}
      </div>
      <input
        ref={inputRef}
        type="file"
        accept="image/png,image/jpeg,image/webp"
        className="hidden"
        onChange={(e) => accept(e.target.files?.[0] ?? null)}
      />
    </div>
  )
}