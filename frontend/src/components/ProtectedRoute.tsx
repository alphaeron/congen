import React from 'react';
import { Navigate, useLocation, useNavigate } from 'react-router-dom';

import { useAuth } from '../contexts/AuthContext';

interface ProtectedRouteProps {
  children: React.ReactNode;
  requireAuth?: boolean;
  redirectTo?: string;
}

/**
 * Protected route component that handles authentication routing.
 * 
 * @param children The components to render if authentication requirements are met
 * @param requireAuth Whether authentication is required (default: true)
 * @param redirectTo Where to redirect if authentication requirements are not met
 * @return The protected route component
 */
export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({
  children,
  requireAuth = true,
  redirectTo = '/login',
}) => {
  const { isAuthenticated, isLoading } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();

  React.useEffect(() => {
    if (!isLoading) {
      if (requireAuth && !isAuthenticated) {
        navigate(redirectTo, { state: { from: location }, replace: true });
      } else if (!requireAuth && isAuthenticated) {
        navigate(redirectTo || '/profile', { replace: true });
      }
    }
  }, [isLoading, requireAuth, isAuthenticated, navigate, redirectTo, location]);

  // Show loading state while authentication is being determined
  if (isLoading) {
    return (
      <div style={{ 
        display: 'flex', 
        justifyContent: 'center', 
        alignItems: 'center', 
        height: '100vh' 
      }}>
        Loading...
      </div>
    );
  }

  // If authentication is required but user is not authenticated, or
  // if authentication is not required and user is authenticated, don't render children
  if ((requireAuth && !isAuthenticated) || (!requireAuth && isAuthenticated)) {
    return null;
  }

  // Render children if authentication requirements are met
  return <>{children}</>;
}; 