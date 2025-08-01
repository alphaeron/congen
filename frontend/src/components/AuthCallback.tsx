import React, { useEffect } from 'react';
import { useAuth as useOidcAuth } from 'react-oidc-context';
import { useNavigate, useLocation } from 'react-router-dom';
import { LoadingSpinner } from './LoadingSpinner';

/**
 * Component to handle OIDC authentication callback.
 * This component processes the authentication callback from Keycloak
 * and redirects the user to the appropriate page.
 */
export const AuthCallback: React.FC = () => {
  const oidcAuth = useOidcAuth();
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    const handleCallback = async () => {
      // Check for OIDC error in URL
      const urlParams = new URLSearchParams(location.search);
      const error = urlParams.get('error');

      if (error) {
        navigate('/login', {
          state: { error: `Authentication failed: ${error}` },
          replace: true,
        });
        return;
      }

      // Wait for OIDC library to process the callback
      if (oidcAuth.isLoading) {
        return;
      }

      // Check authentication result
      if (oidcAuth.isAuthenticated && oidcAuth.user) {
        // Clean up URL and redirect to profile
        window.history.replaceState({}, document.title, '/profile');
        navigate('/profile', { replace: true });
        return;
      }

      // Handle authentication errors
      if (oidcAuth.error) {
        navigate('/login', {
          state: { error: oidcAuth.error.message },
          replace: true,
        });
        return;
      }

      // Fallback for unexpected states
      navigate('/login', {
        state: { error: 'Authentication failed. Please try again.' },
        replace: true,
      });
    };

    // Add a small delay to allow the OIDC library to process
    const timer = setTimeout(handleCallback, 100);
    return () => clearTimeout(timer);
  }, [
    oidcAuth.isLoading,
    oidcAuth.isAuthenticated,
    oidcAuth.user,
    oidcAuth.error,
    navigate,
    location,
  ]);

  return <LoadingSpinner message="Processing authentication..." fullHeight />;
};
