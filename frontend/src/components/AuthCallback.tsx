import React, { useEffect } from 'react';
import { useAuth as useOidcAuth } from 'react-oidc-context';
import { LoadingSpinner } from './LoadingSpinner';

/**
 * Component to handle OIDC authentication callback.
 * This component processes the authentication callback from Keycloak.
 * Navigation is handled by the routing configuration, not this component.
 */
export const AuthCallback: React.FC = () => {
  const oidcAuth = useOidcAuth();

  useEffect(() => {
    // Wait for OIDC library to process the callback
    if (oidcAuth.isLoading) {
      return;
    }

    // Log the authentication result for debugging
    if (oidcAuth.isAuthenticated && oidcAuth.user) {
      console.log('🔐 AuthCallback: Authentication successful');
    } else if (oidcAuth.error) {
      console.log('🔐 AuthCallback: Authentication failed', oidcAuth.error);
    } else {
      console.log('🔐 AuthCallback: Authentication state unclear');
    }
  }, [oidcAuth.isLoading, oidcAuth.isAuthenticated, oidcAuth.user, oidcAuth.error]);

  // Show loading spinner while processing the callback
  if (oidcAuth.isLoading) {
    return <LoadingSpinner fullHeight />;
  }

  // Once processing is complete, this component doesn't need to render anything
  // The routing configuration will handle navigation based on authentication state
  return null;
};
