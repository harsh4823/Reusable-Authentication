import { Routes, Route } from 'react-router-dom'
import RootLayout from './routes/layouts/RootLayout'
import Login from './routes/login'
import Register from './routes/register'
import Onboard from './routes/onboard'
import Dashboard from './routes/Dashboard'
import Profile from './routes/profile'
import NotFound from './components/NotFound'
import AuthLayout from './routes/layouts/AuthLayout'
import { ProtectedRoute } from './components/auth/RouteGuard';
import Index from './routes/Index'
import OAuthSuccess from './routes/OAuthSuccess';


function App() {
  return (
    <Routes>
      <Route element={<RootLayout />}>

        {/* Public routes */}
        <Route path="/" element={<Index />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/onboard" element={<Onboard />} />
        <Route path="/oauth/success" element={<OAuthSuccess />} />

        {/* Protected routes */}
        <Route element={<ProtectedRoute />}>
          <Route element={<AuthLayout />}>
            <Route path="/dashboard" element={<Dashboard />} />
            <Route path="/profile" element={<Profile />} />
          </Route>
        </Route>

        <Route path='*' element={<NotFound/>} />

      </Route>  
    </Routes>
  )
}

export default App
