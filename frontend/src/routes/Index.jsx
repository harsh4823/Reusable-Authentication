import { Navigate } from "react-router-dom";
import { useAuth, rootRedirectFor } from '@/lib/auth-helpers';

const Index = () => {
    const { isAuthenticated, user } = useAuth()

    if (!isAuthenticated) return <Navigate to="/login" replace />
    return <Navigate to={rootRedirectFor(user?.roles ?? [])} replace />
};

export default Index;
