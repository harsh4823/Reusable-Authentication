// frontend/src/routes/layouts/RootLayout.jsx
import { useEffect } from 'react';
import { Outlet, useNavigate } from 'react-router-dom';
import { setAuthFailureHandler } from '@/store/axios-instance';
import { useAppDispatch } from '@/store/hooks';
import { logout } from '@/store/auth-slice';

function AuthFailureBridge() {
  const navigate = useNavigate();
  const dispatch = useAppDispatch(); // Hook into Redux

  useEffect(() => {
    setAuthFailureHandler(() => {
      navigate('/login');
    });
  }, [navigate, dispatch]);

  return null;
}

const RootLayout = () => {
    return (
        <>
        <AuthFailureBridge />
        <Outlet/>   
        </>
    );
};

export default RootLayout;