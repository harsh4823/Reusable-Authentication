// frontend/src/routes/Realms.jsx
import { useState } from 'react'
import { Link } from 'react-router-dom'
import { Building2, Plus, HardHat, Settings2, ShieldCheck, Users, Loader2 } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Checkbox } from '@/components/ui/checkbox'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog'
import { PageHeader } from '@/components/layout/PageHeader'
import { useGetRealmsQuery, useCreateRealmMutation } from '@/store/api/realm-api'

function CreateRealmDialog({ children }) {
  const [open, setOpen] = useState(false)
  const [realmName, setRealmName] = useState('')
  const [displayName, setDisplayName] = useState('')
  const [enabled, setEnabled] = useState(true)
  
  const [createRealm, { isLoading, error }] = useCreateRealmMutation()

  const onSubmit = async (e) => {
    e.preventDefault()
    try {
      await createRealm({ realmName, displayName, enabled }).unwrap()
      
      // We removed the manual Redux update here! 
      // RTK Query's invalidatesTags: ['Realm'] handles the UI update automatically.
      
      setOpen(false)
      setRealmName('')
      setDisplayName('')
    } catch (err) {
      console.error("Failed to create realm:", err)
    }
  }

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        {children}
      </DialogTrigger>
      <DialogContent className="sm:max-w-[425px]">
        <form onSubmit={onSubmit}>
          <DialogHeader>
            <DialogTitle>Create New Realm</DialogTitle>
            <DialogDescription>
              Provision a new isolated tenant for your application. Realm names must be unique.
            </DialogDescription>
          </DialogHeader>
          <div className="grid gap-4 py-4">
            <div className="space-y-2">
              <Label htmlFor="realmName">Realm Name (ID)</Label>
              <Input
                id="realmName"
                placeholder="my-company-app"
                value={realmName}
                onChange={(e) => setRealmName(e.target.value.toLowerCase().replace(/\s+/g, '-'))}
                required
              />
              <p className="text-[10px] text-muted-foreground">Used in API URLs. No spaces allowed.</p>
            </div>
            <div className="space-y-2">
              <Label htmlFor="displayName">Display Name</Label>
              <Input
                id="displayName"
                placeholder="My Company App"
                value={displayName}
                onChange={(e) => setDisplayName(e.target.value)}
                required
              />
            </div>
            <div className="flex items-center space-x-2 pt-2">
              <Checkbox 
                id="enabled" 
                checked={enabled} 
                onCheckedChange={setEnabled} 
              />
              <Label htmlFor="enabled" className="text-sm font-medium leading-none">
                Enable Realm immediately
              </Label>
            </div>
            {error && (
               <p className="text-sm text-destructive font-medium mt-2">
                 {error?.data?.message || 'Failed to create realm. It might already exist.'}
               </p>
            )}
          </div>
          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => setOpen(false)}>
              Cancel
            </Button>
            <Button type="submit" disabled={isLoading || !realmName}>
              {isLoading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
              Create Tenant
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}

const Realms = () => {
  // ALGORITHM: Dynamic Data Hydration
  const { data: realmsData = [], isLoading } = useGetRealmsQuery()
  
  // Safely extract the array whether Spring Boot returns a raw List<> or a Page<> object
  const realms = Array.isArray(realmsData) ? realmsData : (realmsData?.content || [])
  const hasRealms = realms.length > 0

  if (isLoading) {
    return (
      <div className="flex min-h-[50vh] items-center justify-center">
        <Loader2 className="h-8 w-8 animate-spin text-primary/50" />
      </div>
    )
  }
  
  return (
    <div className="mx-auto max-w-7xl px-6 py-8">
      <PageHeader
        title="My Realms"
        description="Manage your isolated authentication tenants, users, and clients."
        actions={
          <CreateRealmDialog>
            <Button size="sm">
              <Plus className="mr-2 h-4 w-4" />
              New Realm
            </Button>
          </CreateRealmDialog>
        }
      />

      {!hasRealms ? (
        <div className="mt-8 flex flex-col items-center justify-center rounded-2xl border border-dashed border-border bg-card/50 py-24 text-center">
          <div className="flex h-16 w-16 items-center justify-center rounded-2xl bg-primary/10 text-primary shadow-inner">
            <HardHat className="h-8 w-8" />
          </div>
          <h2 className="mt-6 font-display text-xl font-semibold">Under Construction</h2>
          <p className="mt-2 max-w-md text-sm text-muted-foreground">
            You haven't provisioned any realms yet. Create a tenant to start managing your users, roles, and OAuth2 policies.
          </p>
          
          <CreateRealmDialog>
            <Button size="lg" className="mt-8 shadow-md">
              <Plus className="mr-2 h-5 w-5" />
              Build Your First Realm
            </Button>
          </CreateRealmDialog>
          
        </div>
      ) : (
        <div className="mt-8 grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
          {/* ALGORITHM: Grid Mapping Iteration - O(N) where N is number of realms */}
          {realms.map((realm) => (
            <div key={realm.realmName} className="group relative flex flex-col justify-between overflow-hidden rounded-2xl border border-border bg-card p-6 shadow-sm transition-all hover:shadow-md">
              <div>
                <div className="flex items-center justify-between">
                  <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10 text-primary">
                    <Building2 className="h-5 w-5" />
                  </div>
                  <div className={`rounded-full px-2.5 py-1 text-[10px] font-semibold uppercase tracking-wider ${realm.enabled ? 'bg-green-500/10 text-green-600 dark:text-green-400' : 'bg-red-500/10 text-red-600 dark:text-red-400'}`}>
                    {realm.enabled ? 'Active' : 'Disabled'}
                  </div>
                </div>
                <h3 className="mt-4 font-display text-lg font-semibold">{realm.displayName || realm.realmName}</h3>
                <p className="text-sm text-muted-foreground">ID: {realm.realmName}</p>
              </div>
              
              <div className="mt-6 flex flex-col gap-2">
                <div className="flex items-center gap-4 text-sm text-muted-foreground">
                  <div className="flex items-center gap-1.5"><Users className="h-4 w-4"/> Users</div>
                  <div className="flex items-center gap-1.5"><ShieldCheck className="h-4 w-4"/> Roles</div>
                </div>
                <Button variant="secondary" className="mt-4 w-full group-hover:bg-primary group-hover:text-primary-foreground" asChild>
                  <Link to={`/realms/${realm.realmName}/settings`}>
                    <Settings2 className="mr-2 h-4 w-4" />
                    Manage Tenant
                  </Link>
                </Button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

export default Realms