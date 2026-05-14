import { Link, useLocation } from 'react-router-dom'
import {
  LayoutDashboard, Building2, ShieldCheck, UserCircle,
  LogOut, KeyRound, Globe, Layers,
} from 'lucide-react'
import {
  Sidebar, SidebarContent, SidebarFooter, SidebarGroup,
  SidebarGroupContent, SidebarGroupLabel, SidebarHeader,
  SidebarMenu, SidebarMenuButton, SidebarMenuItem, useSidebar,
} from '@/components/ui/sidebar'
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
import { useAuth } from '@/lib/auth-helpers'
import { useAppDispatch } from '@/store/hooks'
import { logout } from '@/store/auth-slice'
import { RoleBadge } from '@/components/ui-extras/role-badge'
import { Button } from '@/components/ui/button'

export function AppSidebar() {
  const { state } = useSidebar()
  const collapsed = state === 'collapsed'
  const { pathname } = useLocation()   // ← replaces useRouterState
  const { user } = useAuth()
  const dispatch = useAppDispatch()
  const roles = user?.roles ?? []
  const isAdmin = roles.includes('ROLE_ADMIN')
  const isClient = roles.includes('ROLE_CLIENT') || isAdmin

  const isActive = (p) => pathname === p || pathname.startsWith(p + '/')

  const mainItems = [
    { title: 'Dashboard', url: '/dashboard', icon: LayoutDashboard, show: true },
    { title: 'My Realms',  url: '/realms',    icon: Building2,       show: isClient },
    { title: 'Profile',    url: '/profile',   icon: UserCircle,      show: true },
  ].filter((i) => i.show)

  const adminItems = [
    { title: 'Overview',     url: '/admin',         icon: LayoutDashboard },
    { title: 'All Realms',   url: '/admin/realms',  icon: Globe },
    { title: 'All Clients',  url: '/admin/clients', icon: Layers },
  ]

  const initials = (user?.name || user?.email || 'U')
    .split(' ')
    .map((s) => s[0])
    .slice(0, 2)
    .join('')
    .toUpperCase()

  return (
    <Sidebar collapsible="icon" className="border-r border-sidebar-border">
      <SidebarHeader className="border-b border-sidebar-border p-4">
        <Link to="/dashboard" className="flex items-center gap-2.5">
          <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-primary text-primary-foreground shadow-lg shadow-primary/20">
            <KeyRound className="h-5 w-5" />
          </div>
          {!collapsed && (
            <div className="leading-tight">
              <p className="font-display text-sm font-semibold">Aegis IAM</p>
              <p className="text-[10px] uppercase tracking-wider text-muted-foreground">Identity Platform</p>
            </div>
          )}
        </Link>
      </SidebarHeader>

      <SidebarContent>
        <SidebarGroup>
          <SidebarGroupLabel>Workspace</SidebarGroupLabel>
          <SidebarGroupContent>
            <SidebarMenu>
              {mainItems.map((item) => (
                <SidebarMenuItem key={item.url}>
                  <SidebarMenuButton asChild isActive={isActive(item.url)}>
                    <Link to={item.url} className="flex items-center gap-2">
                      <item.icon className="h-4 w-4" />
                      {!collapsed && <span>{item.title}</span>}
                    </Link>
                  </SidebarMenuButton>
                </SidebarMenuItem>
              ))}
            </SidebarMenu>
          </SidebarGroupContent>
        </SidebarGroup>

        {isAdmin && (
          <SidebarGroup>
            <SidebarGroupLabel className="flex items-center gap-1.5">
              <ShieldCheck className="h-3 w-3" />
              Administration
            </SidebarGroupLabel>
            <SidebarGroupContent>
              <SidebarMenu>
                {adminItems.map((item) => (
                  <SidebarMenuItem key={item.url}>
                    <SidebarMenuButton asChild isActive={isActive(item.url)}>
                      <Link to={item.url} className="flex items-center gap-2">
                        <item.icon className="h-4 w-4" />
                        {!collapsed && <span>{item.title}</span>}
                      </Link>
                    </SidebarMenuButton>
                  </SidebarMenuItem>
                ))}
              </SidebarMenu>
            </SidebarGroupContent>
          </SidebarGroup>
        )}
      </SidebarContent>

      <SidebarFooter className="border-t border-sidebar-border p-3">
        <div className="flex items-center gap-2.5">
          <Avatar className="h-9 w-9 ring-1 ring-border">
            <AvatarImage src={user?.image ?? undefined} alt={user?.name ?? ''} />
            <AvatarFallback className="bg-primary/15 text-xs font-medium text-primary">{initials}</AvatarFallback>
          </Avatar>
          {!collapsed && (
            <div className="min-w-0 flex-1">
              <p className="truncate text-sm font-medium">{user?.name ?? '—'}</p>
              <div className="mt-0.5 flex flex-wrap gap-1">
                {roles.slice(0, 1).map((r) => (
                  <RoleBadge key={r} role={r} />
                ))}
              </div>
            </div>
          )}
          {!collapsed && (
            <Button
              variant="ghost"
              size="icon"
              className="h-8 w-8 text-muted-foreground hover:text-foreground"
              onClick={() => dispatch(logout())}
              aria-label="Log out"
            >
              <LogOut className="h-4 w-4" />
            </Button>
          )}
        </div>
      </SidebarFooter>
    </Sidebar>
  )
}