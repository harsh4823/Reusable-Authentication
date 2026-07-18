// frontend/src/routes/RealmSettings.jsx
import { useState } from 'react'
import { useParams } from 'react-router-dom'
import { KeyRound, Loader2, Plus, ShieldCheck, Trash2, Users, UserPlus } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog'
import { Checkbox } from '@/components/ui/checkbox'
import { PageHeader } from '@/components/layout/PageHeader'
import {
  useGetRealmUsersQuery,
  useGetRealmRolesQuery,
  useCreateRealmRoleMutation,
  useDeleteRealmRoleMutation,
  useCreateRealmUserMutation,
  useDeleteRealmUserMutation,
  useAssignRoleToUserMutation,
  useRemoveRoleFromUserMutation
} from '@/store/api/realm-api'

// ==========================================
// SUB-COMPONENT: Create New Realm User Modal
// ==========================================
function CreateUserDialog({ realmName, children }) {
  const [open, setOpen] = useState(false)
  const [email, setEmail] = useState('')
  const [name, setName] = useState('')
  const [password, setPassword] = useState('')
  const [createUser, { isLoading }] = useCreateRealmUserMutation()

  const onSubmit = async (e) => {
    e.preventDefault()
    try {
      await createUser({ realmName, userData: { email, name, password } }).unwrap()
      setOpen(false)
      setEmail(''); setName(''); setPassword('')
    } catch (err) {
      console.error("Failed to create user", err)
    }
  }

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>{children}</DialogTrigger>
      <DialogContent>
        <form onSubmit={onSubmit}>
          <DialogHeader>
            <DialogTitle>Add User to {realmName}</DialogTitle>
            <DialogDescription>Create a new localized identity within this tenant.</DialogDescription>
          </DialogHeader>
          <div className="space-y-4 py-4">
            <div className="space-y-2">
              <Label>Email</Label>
              <Input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
            </div>
            <div className="space-y-2">
              <Label>Full Name</Label>
              <Input value={name} onChange={(e) => setName(e.target.value)} required />
            </div>
            <div className="space-y-2">
              <Label>Password</Label>
              <Input type="password" value={password} onChange={(e) => setPassword(e.target.value)} required />
            </div>
          </div>
          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => setOpen(false)}>Cancel</Button>
            <Button type="submit" disabled={isLoading}>
              {isLoading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />} Create User
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}

// ==========================================
// SUB-COMPONENT: Assign/Remove Roles Modal
// ==========================================
function ManageUserRolesDialog({ realmName, user, realmRoles, children }) {
  const [open, setOpen] = useState(false)
  const [assignRole] = useAssignRoleToUserMutation()
  const [removeRole] = useRemoveRoleFromUserMutation()

  // Extract just the role names the user already has for easy lookup
  const userRoleNames = user.roles?.map(r => r.name) || []

  // ALGORITHM: Optimistic Toggle Dispatcher
  const handleToggleRole = async (roleName, isCurrentlyAssigned) => {
    try {
      if (isCurrentlyAssigned) {
        await removeRole({ realmName, userId: user.userId, roleName }).unwrap()
      } else {
        await assignRole({ realmName, userId: user.userId, roleName }).unwrap()
      }
    } catch (err) {
      console.error("Role modification failed", err)
    }
  }

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>{children}</DialogTrigger>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Manage Roles: {user.name}</DialogTitle>
          <DialogDescription>Assign or revoke RBAC authorities for this user.</DialogDescription>
        </DialogHeader>
        <div className="space-y-3 py-4">
          {realmRoles.length === 0 ? (
            <p className="text-sm text-muted-foreground">No roles exist in this realm yet.</p>
          ) : (
            realmRoles.map((role) => {
              const isAssigned = userRoleNames.includes(role.name)
              return (
                <div key={role.id || role.name} className="flex items-center space-x-3 rounded-lg border p-3 shadow-sm">
                  <Checkbox 
                    id={`role-${role.name}`} 
                    checked={isAssigned}
                    onCheckedChange={() => handleToggleRole(role.name, isAssigned)}
                  />
                  <div className="space-y-1 leading-none">
                    <Label htmlFor={`role-${role.name}`} className="font-mono text-sm">{role.name}</Label>
                    <p className="text-xs text-muted-foreground">{role.description || 'No description'}</p>
                  </div>
                </div>
              )
            })
          )}
        </div>
      </DialogContent>
    </Dialog>
  )
}


// ==========================================
// MAIN COMPONENT
// ==========================================
const RealmSettings = () => {
  const { realmName } = useParams()
  const { data: usersPage, isLoading: usersLoading } = useGetRealmUsersQuery({ realmName, page: 0, size: 50 })
  const { data: roles = [], isLoading: rolesLoading } = useGetRealmRolesQuery(realmName)
  
  const [createRole, { isLoading: isCreatingRole }] = useCreateRealmRoleMutation()
  const [deleteRole] = useDeleteRealmRoleMutation()
  const [deleteUser] = useDeleteRealmUserMutation()
  
  const [newRoleName, setNewRoleName] = useState('')

  const handleCreateRole = async (e) => {
    e.preventDefault()
    if (!newRoleName.trim()) return
    try {
      await createRole({ 
        realmName, 
        roleData: { name: newRoleName.toUpperCase(), description: "Custom Realm Role" } 
      }).unwrap()
      setNewRoleName('')
    } catch (err) {
      console.error("Failed to create role:", err)
    }
  }

  const users = usersPage?.content || []

  return (
    <div className="mx-auto max-w-7xl px-6 py-8">
      <PageHeader title={`Tenant Settings: ${realmName}`} description="Configure RBAC policies, manage users, and assign roles." />

      <Tabs defaultValue="users" className="mt-8 space-y-6">
        <TabsList className="bg-secondary/50">
          <TabsTrigger value="users"><Users className="mr-2 h-4 w-4" /> Users Mapping</TabsTrigger>
          <TabsTrigger value="roles"><ShieldCheck className="mr-2 h-4 w-4" /> RBAC Roles</TabsTrigger>
        </TabsList>

        {/* --- USERS TAB --- */}
        <TabsContent value="users" className="space-y-4 rounded-2xl border border-border bg-card p-6 shadow-sm">
          <div className="flex items-center justify-between">
            <h3 className="font-display text-lg font-semibold">Registered Users</h3>
            
            {/* INJECTED MODAL HERE */}
            <CreateUserDialog realmName={realmName}>
              <Button size="sm"><Plus className="mr-2 h-4 w-4" /> Add User</Button>
            </CreateUserDialog>
            
          </div>
          <div className="rounded-md border">
            <Table>
              <TableHeader className="bg-secondary/30">
                <TableRow>
                  <TableHead>Email</TableHead>
                  <TableHead>Assigned Roles</TableHead>
                  <TableHead className="text-right">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {users.map((user) => (
                  <TableRow key={user.userId}>
                    <TableCell className="font-medium">
                      {user.email}<br/><span className="text-xs text-muted-foreground">{user.name}</span>
                    </TableCell>
                    <TableCell>
                      <div className="flex flex-wrap gap-1">
                        {user.roles?.map(role => (
                          <span key={role.name} className="rounded-md bg-secondary px-2 py-0.5 text-[10px] font-medium font-mono text-secondary-foreground">
                            {role.name}
                          </span>
                        ))}
                      </div>
                    </TableCell>
                    <TableCell className="text-right">
                      <div className="flex justify-end gap-2">
                        
                        {/* INJECTED ROLES MODAL HERE */}
                        <ManageUserRolesDialog realmName={realmName} user={user} realmRoles={roles}>
                          <Button variant="outline" size="sm">
                            <UserPlus className="mr-2 h-4 w-4" /> Manage Roles
                          </Button>
                        </ManageUserRolesDialog>
                        
                        <Button variant="ghost" size="icon" onClick={() => deleteUser({ realmName, userId: user.userId })} className="text-destructive hover:bg-destructive/10">
                          <Trash2 className="h-4 w-4" />
                        </Button>
                      </div>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </div>
        </TabsContent>

        {/* --- ROLES TAB --- */}
        <TabsContent value="roles" className="space-y-4 rounded-2xl border border-border bg-card p-6 shadow-sm">
          <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
            <h3 className="font-display text-lg font-semibold">Realm Roles</h3>
            <form onSubmit={handleCreateRole} className="flex items-center gap-2">
              <Input placeholder="ROLE_NAME" value={newRoleName} onChange={(e) => setNewRoleName(e.target.value)} className="w-48"/>
              <Button type="submit" disabled={isCreatingRole || !newRoleName}>
                {isCreatingRole ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : <Plus className="mr-2 h-4 w-4" />} Create Role
              </Button>
            </form>
          </div>
          <div className="rounded-md border">
            <Table>
              <TableHeader className="bg-secondary/30">
                <TableRow>
                  <TableHead className="w-[50px]"></TableHead>
                  <TableHead>Role Name (Authority)</TableHead>
                  <TableHead>Description</TableHead>
                  <TableHead className="text-right">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {roles.map((role) => (
                  <TableRow key={role.id || role.name}>
                    <TableCell><KeyRound className="h-4 w-4 text-muted-foreground" /></TableCell>
                    <TableCell className="font-medium font-mono text-xs">{role.name}</TableCell>
                    <TableCell className="text-muted-foreground text-sm">{role.description}</TableCell>
                    <TableCell className="text-right">
                      <Button variant="ghost" size="icon" onClick={() => deleteRole({ realmName, roleName: role.name })} className="text-destructive hover:bg-destructive/10">
                        <Trash2 className="h-4 w-4" />
                      </Button>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </div>
        </TabsContent>
      </Tabs>
    </div>
  )
}

export default RealmSettings