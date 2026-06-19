// frontend/src/components/auth/RouteGuard.jsx
import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { Loader2 } from 'lucide-react';
import { useEffect } from 'react';
import { useAuth } from '@/lib/auth-helpers';
import { useMeQuery } from '@/store/api/auth-api';
import { useAppDispatch } from '@/store/hooks';
import { setSessionUser, logout } from '@/store/auth-slice';

export function ProtectedRoute() {
  const { isAuthenticated } = useAuth();
  const location = useLocation();
  const dispatch = useAppDispatch();

  const { data: user, isLoading, isError } = useMeQuery(undefined, {
    // Only attempt to fetch data if Redux considers the user authenticated
    skip: !isAuthenticated,
  });

  // Hydrate Redux state if the fetch is successful
  useEffect(() => {
    if (user) {
      dispatch(setSessionUser(user));
    }
  }, [user, dispatch]);

  // Algorithm: Eager Cache Invalidation
  // If the backend rejects the token (e.g., 403 Forbidden), strictly purge state.
  useEffect(() => {
    if (isError) {
      dispatch(logout()); // Sets isAuthenticated to false, breaking the GuestRoute bounce
    }
  }, [isError, dispatch]);

  // 1. Guard against unauthenticated users
  if (!isAuthenticated) {
    return <Navigate to={`/login?redirect=${encodeURIComponent(location.pathname)}`} replace />;
  }

  // 2. Prevent blank data by holding the render until hydration completes
  if (isLoading) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <Loader2 className="h-5 w-5 animate-spin text-blue-500" />
      </div>
    );
  }

  // 3. Prevent rendering the Dashboard briefly if an error occurs
  if (isError) {
    return null; 
  }

  // 4. Safe to render children (Dashboard)
  return <Outlet />;
}