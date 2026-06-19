import { Link } from 'react-router-dom'
import { Building2, KeyRound, Layers, Plus, Sparkles, Users } from 'lucide-react'
import { StatCard } from './../components/ui-extras/StatCard'
import { Button } from '@/components/ui/button'
import { useAuth } from '@/lib/auth-helpers'
import { RoleBadge } from '@/components/ui-extras/RoleBadge'
import { PageHeader } from './../components/layout/PageHeader'
import {
  useGetRealmQuery,
  useGetRealmUsersQuery,
  useGetRealmRolesQuery,
  useGetRealmClientsQuery,
} from '@/store/api/realm-api'

const Dashboard = () => {
  const { user } = useAuth()
  const roles = user?.roles ?? []
  const isAdmin = roles.includes('ROLE_ADMIN')
  const isClient = roles.includes('ROLE_CLIENT') || isAdmin

  // Backend may return `realm`, `realmName`, or `ownedRealm` — check all
  const realmName = user?.realm ?? user?.realmName ?? user?.ownedRealm ?? null
  const hasRealm = Boolean(realmName)

  const firstName = (user?.name || '').split(' ')[0] || 'there'

  // Only load realm data when we have both client role AND a realm name
  const shouldLoadRealmData = isClient && hasRealm

  const { data: realm, isLoading: realmLoading } = useGetRealmQuery(realmName, {
    skip: !shouldLoadRealmData,
  })
  const { data: usersPage } = useGetRealmUsersQuery(
    { realmName, page: 0, size: 1 },
    { skip: !shouldLoadRealmData }
  )
  const { data: realmRoles = [] } = useGetRealmRolesQuery(realmName, {
    skip: !shouldLoadRealmData,
  })
  const { data: clients = [] } = useGetRealmClientsQuery(realmName, {
    skip: !shouldLoadRealmData,
  })

  return (
    <div className="mx-auto max-w-7xl px-6 py-8">
      <PageHeader
        title={`Welcome back, ${firstName}`}
        description="Here's a snapshot of your identity infrastructure."
        actions={
          <div className="flex items-center gap-2">
            {roles.map((r) => (
              <RoleBadge key={r} role={r} />
            ))}
          </div>
        }
      />

      {/* No realm yet — prompt to create one */}
      {!hasRealm && !isAdmin && (
        <div className="mt-8 overflow-hidden rounded-2xl border border-border bg-card">
          <div className="grid gap-6 p-8 md:grid-cols-[1fr_auto] md:items-center">
            <div>
              <div className="inline-flex items-center gap-1.5 rounded-full bg-primary/10 px-2.5 py-1 text-xs font-medium text-primary">
                <Sparkles className="h-3 w-3" />
                Get started
              </div>
              <h2 className="mt-3 font-display text-2xl font-semibold">
                You haven't created a realm yet
              </h2>
              <p className="mt-1.5 max-w-lg text-sm text-muted-foreground">
                A realm is an isolated tenant for your application's users. Create one to start
                issuing tokens and managing roles.
              </p>
            </div>
            <Button size="lg" asChild>
              <Link to="/onboard">
                <Plus className="mr-2 h-4 w-4" />
                Create your first realm
              </Link>
            </Button>
          </div>
        </div>
      )}

      {/* Realm stats — only shown when we have a realm */}
      {shouldLoadRealmData && (
        <div className="mt-8 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
          <StatCard
            icon={Building2}
            label="Realm"
            value={
              realmLoading
                ? 'Loading…'
                : realm?.realmName ?? realmName ?? '—'
            }
            trend={realm?.displayName ?? 'Your tenant'}
          />
          <StatCard
            icon={Users}
            label="Total users"
            value={usersPage?.totalElements ?? 0}
          />
          <StatCard
            icon={KeyRound}
            label="Roles defined"
            value={realmRoles.length}
          />
          <StatCard
            icon={Layers}
            label="OAuth2 clients"
            value={clients.length}
          />
        </div>
      )}

      <div className="mt-8 grid gap-4 lg:grid-cols-2">
        <div className="rounded-2xl border border-border bg-card p-6">
          <h3 className="font-display text-base font-semibold">Quick actions</h3>
          <div className="mt-4 space-y-2">
            {isClient && hasRealm && (
              <ActionLink
                to="/realms"
                icon={Building2}
                title="My Realms"
                subtitle="Manage tenants & users"
              />
            )}
            {!hasRealm && !isAdmin && (
              <ActionLink
                to="/onboard"
                icon={Plus}
                title="Create a realm"
                subtitle="Set up your first tenant"
              />
            )}
            <ActionLink
              to="/profile"
              icon={Users}
              title="Edit profile"
              subtitle="Update your identity & sessions"
            />
            {isAdmin && (
              <ActionLink
                to="/admin"
                icon={KeyRound}
                title="Admin Panel"
                subtitle="Platform-wide controls"
              />
            )}
          </div>
        </div>

        <div className="rounded-2xl border border-border bg-card p-6">
          <h3 className="font-display text-base font-semibold">Connecting your backend</h3>
          <p className="mt-2 text-sm text-muted-foreground">This UI talks to your IAM server at:</p>
          <code className="mt-3 block rounded-lg border border-border bg-secondary/40 px-3 py-2 text-xs">
            {import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'}
          </code>
          <p className="mt-3 text-xs text-muted-foreground">
            Override via <span className="font-mono">VITE_API_BASE_URL</span> in your environment.
          </p>
        </div>
      </div>
    </div>
  )
}

function ActionLink({ to, icon: Icon, title, subtitle }) {
  return (
    <Link
      to={to}
      className="group flex items-center gap-3 rounded-xl border border-border bg-secondary/30 p-3 transition-colors hover:border-primary/50 hover:bg-secondary/60"
    >
      <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-primary/15 text-primary">
        <Icon className="h-4 w-4" />
      </div>
      <div className="flex-1">
        <p className="text-sm font-medium">{title}</p>
        <p className="text-xs text-muted-foreground">{subtitle}</p>
      </div>
    </Link>
  )
}

export default Dashboard