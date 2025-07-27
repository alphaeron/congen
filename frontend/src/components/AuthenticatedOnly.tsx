import React from 'react';

import { useAuth } from '../auth/AuthContext';

interface AuthenticatedOnlyProps {
  children: React.ReactNode;
  fallback?: React.ReactNode;
}

/**
 * Component that only renders its children when the user is authenticated.
 * If not authenticated, renders a fallback (defaults to null).
 *
 * @example
 * ```tsx
 * // Basic usage - renders nothing when not authenticated
 * <AuthenticatedOnly>
 *   <ProtectedComponent />
 * </AuthenticatedOnly>
 *
 * // With custom fallback
 * <AuthenticatedOnly fallback={<div>Please sign in to view this content</div>}>
 *   <ProtectedComponent />
 * </AuthenticatedOnly>
 *
 * // Multiple children
 * <AuthenticatedOnly>
 *   <Header />
 *   <MainContent />
 *   <Footer />
 * </AuthenticatedOnly>
 * ```
 */
export const AuthenticatedOnly: React.FC<AuthenticatedOnlyProps> = ({
  children,
  fallback = null,
}) => {
  const { authenticated } = useAuth();

  if (!authenticated) {
    return fallback;
  }

  return children;
};
