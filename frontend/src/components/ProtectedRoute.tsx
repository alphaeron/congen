import React from 'react';
import { Navigate } from 'react-router';

import { useAuth } from '../contexts/AuthContext';
import { LoadingSpinner } from './LoadingSpinner';

interface ProtectedRouteProps {
  children: React.ReactNode;
  requireAuth?: boolean;
  requireProfile?: boolean;
  requireOidcOnly?: boolean;
}

/**
 * Protected route component that handles authentication routing.
 *
 * @param children The components to render if authentication requirements are met
 * @param requireAuth Whether authentication is required (default: true)
 * @param requireProfile Whether a user profile is required (default: true)
 * @param requireOidcOnly Whether only OIDC authentication is required (ignores profile requirement)
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

  // If authentication is required but user is not authenticated, redirect to login
  if (requireAuth && !isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  // Render children if authentication requirements are met
  return <React.Fragment>{children}</React.Fragment>;
};
