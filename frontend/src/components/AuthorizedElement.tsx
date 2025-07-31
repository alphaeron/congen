import React from 'react';

import { useAuth } from '../contexts/AuthContext';

interface AuthorizedElementProps {
  roles?: string[];
  children: React.ReactNode;
  requireAuth?: boolean;
  fallback?: React.ReactNode;
}

/**
 * Authorized element component that handles authentication and role-based access control.
 * 
 * This component checks if the user is authenticated and has the required roles.
 * It follows a pattern similar to the user's suggested approach.
 * 
 * @param roles Array of roles that the user must have (optional)
 * @param children The components to render if authorization requirements are met
 * @param requireAuth Whether authentication is required (default: true)
 * @return The authorized element component
 */
export const AuthorizedElement: React.FC<AuthorizedElementProps> = ({
  roles,
  children,
  requireAuth = true,
  fallback = null,
}) => {
  const { isAuthenticated, isLoading, user } = useAuth();

  // Show loading state while authentication is being determined
  if (isLoading) {
    return null;
  }

  // If authentication is not required, render children
  if (!requireAuth) {
    return <>{children}</>;
  }

  // If authentication is required but user is not authenticated, show fallback or nothing
  if (!isAuthenticated || !user) {
    return fallback;
  }

  // If no roles are specified, just check if user is authenticated
  if (!roles || roles.length === 0) {
    return <>{children}</>;
  }

  // Check if user has any of the required roles
  const isAuthorized = () => {
    if (user && roles) {
      // Check user's groups and roles
      const userGroups = user.groups || [];
      const userRoles = user.roles || [];
      
      // Check if user has any of the required roles
      return roles.some(role => userGroups.includes(role) || userRoles.includes(role));
    }
    return false;
  };

  return isAuthorized() ? <>{children}</> : fallback;
}; 