import { useEffect } from 'react';
import { Outlet, useNavigate } from 'react-router-dom';
import { setAuthFailureHandler } from '@/store/axios-instance';

function AuthFailureBridge() {
  const navigate = useNavigate()

  useEffect(() => {
    setAuthFailureHandler(() => {
      navigate('/login')
    })
  }, [navigate])

  return null
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