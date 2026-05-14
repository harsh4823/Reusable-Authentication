import { AppShell } from '@/components/layout/AppShell';
import { Navigate } from 'react-router-dom';
import { useAuth } from '@/lib/auth-helpers';

const AuthLayout = () => {
    const { isAuthenticated } = useAuth()

    if (!isAuthenticated) return <Navigate to="/login" replace />

  return <AppShell />
};

export default AuthLayout;
