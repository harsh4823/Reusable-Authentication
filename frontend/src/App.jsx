import { Routes, Route } from 'react-router-dom'
import RootLayout from './routes/layouts/RootLayout'
import Index from './routes'
import Login from './routes/login'
import Register from './routes/register'
import Onboard from './routes/onboard'
import AuthLayout from './routes/layouts/AuthLayout'
import Dashboard from './routes/Dashboard'
import Profile from './routes/profile'


function App() {
  return (
    <Routes>
      <Route element={<RootLayout />}>

        {/* Public routes */}
        <Route path="/" element={<Index />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/onboard" element={<Onboard />} />

        {/* Protected routes */}
        <Route element={<ProtectedRoute />}>
          <Route element={<AuthLayout />}>
            <Route path="/dashboard" element={<Dashboard />} />
            <Route path="/profile" element={<Profile />} />
          </Route>
        </Route>

      </Route>  
    </Routes>
  )
}

export default App
