import React from 'react';

import { useAuth } from '../contexts/AuthContext';
import { LoadingSpinner } from './LoadingSpinner';

interface ProtectedRouteProps {
  children: React.ReactNode;
  requireAuth?: boolean;
}

/**
 * Protected route component that handles authentication routing.
 *
 * @param children The components to render if authentication requirements are met
 * @param requireAuth Whether authentication is required (default: true)
 * @return The protected route component
 */
export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({
  children,
  requireAuth = true,
}) => {
  const { isAuthenticated, isLoading } = useAuth();

  // Show loading state while authentication is being determined
  if (isLoading) {
    return <LoadingSpinner fullHeight />;
  }

  // If authentication is required but user is not authenticated, or
  // if authentication is not required and user is authenticated, don't render children
  if ((requireAuth && !isAuthenticated) || (!requireAuth && isAuthenticated)) {
    return null;
  }

  // Render children if authentication requirements are met
  return <React.Fragment>{children}</React.Fragment>;
};
