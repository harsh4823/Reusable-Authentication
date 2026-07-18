import { Routes, Route } from 'react-router-dom'
import RootLayout from './routes/layouts/RootLayout'
import Login from './routes/Login'
import Register from './routes/Register'
import Onboard from './routes/Onboard'
import Dashboard from './routes/Dashboard'
import Profile from './routes/Profile'
import NotFound from './components/NotFound'
import AuthLayout from './routes/layouts/AuthLayout'
import { ProtectedRoute } from './components/auth/RouteGuard'
import Index from './routes/Index'
import OAuthSuccess from './routes/OAuthSuccess'
import { GuestRoute } from './routes/GuestRoute'
import Realms from './routes/Realms'
import RealmSettings from './routes/RealmSettings'

function ComingSoon({ title }) {
  return (
    <div className="mx-auto max-w-7xl px-6 py-8">
      <h1 className="font-display text-2xl font-semibold">{title}</h1>
      <p className="mt-2 text-sm text-muted-foreground">This section is under construction.</p>
    </div>
  )
}

function App() {
  return (
    <Routes>
      <Route element={<RootLayout />}>

        {/* Public routes */}
        <Route path="/" element={<Index />} />

        <Route element={<GuestRoute />}>
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
        </Route>

        <Route path="/onboard" element={<Onboard />} />
        <Route path="/oauth/success" element={<OAuthSuccess />} />

        {/* Protected routes */}
        <Route element={<ProtectedRoute />}>
          <Route element={<AuthLayout />}>
            <Route path="/dashboard" element={<Dashboard />} />
            <Route path="/profile" element={<Profile />} />
            {/* Realm routes (sidebar links) */}
            <Route path="/realms" element={<Realms title="My Realms" />} />
            {/* Admin routes (sidebar links) */}
            <Route path="/admin" element={<ComingSoon title="Admin Overview" />} />
            <Route path="/admin/realms" element={<ComingSoon title="All Realms" />} />
            <Route path="/admin/clients" element={<ComingSoon title="All Clients" />} />
            <Route path="/realms/:realmName/settings" element={<RealmSettings/>} />
          </Route>
        </Route>

        <Route path="*" element={<NotFound />} />

      </Route>
    </Routes>
  )
}

export default App