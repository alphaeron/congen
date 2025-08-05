import React, { useEffect, useState } from 'react';
import { useAuth as useOidcAuth } from 'react-oidc-context';
import { useNavigate } from 'react-router';
import { LoadingSpinner } from './LoadingSpinner';
import { useAuth } from '../contexts/AuthContext';

/**
 * Simplified AuthCallback component that handles OIDC authentication callback.
 * Detects new users and redirects them to profile creation.
 */

/**
 * Component to handle OIDC authentication callback.
 * This component processes the authentication callback from Keycloak.
 * Navigation is handled by the routing configuration, not this component.
 */
export const AuthCallback: React.FC = () => {
  const oidcAuth = useOidcAuth();
  const { isAuthenticated, user, error: authError } = useAuth();
  const navigate = useNavigate();
  const [hasCheckedProfile, setHasCheckedProfile] = useState(false);

  useEffect(() => {
    // Only run this effect once when OIDC finishes loading and AuthContext has processed the user
    if (oidcAuth.isLoading || hasCheckedProfile) {
      return;
    }

    const handleAuthentication = async () => {
      // Clear sensitive URL parameters from browser history
      if (window.history.replaceState) {
        const cleanUrl = window.location.pathname;
        window.history.replaceState({}, document.title, cleanUrl);
      }

      // If OIDC authentication failed, redirect to login
      if (oidcAuth.error) {
        navigate('/login');
        setHasCheckedProfile(true);
        return;
      }

      // If OIDC is authenticated and has a user with access token
      if (oidcAuth.isAuthenticated && oidcAuth.user && oidcAuth.user.access_token) {
        // Wait for AuthContext to process the authentication
        // Give AuthContext time to process the user profile
        await new Promise(resolve => setTimeout(resolve, 500));
        
        if (isAuthenticated && user) {
          // User has a profile, redirect to home
          navigate('/');
        } else if (authError && authError.includes('Profile not found')) {
          // User doesn't have a profile, redirect to profile creation
          navigate('/profile/create');
        } else if (authError && authError.includes('Authentication failed')) {
          // Authentication error, redirect to login
          navigate('/login');
        } else {
          // Still processing, wait a bit more
          setTimeout(() => {
            if (!hasCheckedProfile) {
              handleAuthentication();
            }
          }, 1000);
          return;
        }
      }

      setHasCheckedProfile(true);
    };

    handleAuthentication();
  }, [oidcAuth.isLoading, oidcAuth.isAuthenticated, oidcAuth.user, oidcAuth.user?.access_token, oidcAuth.error, isAuthenticated, user, authError, navigate, hasCheckedProfile]);

  // Show loading spinner while processing the callback
  if (oidcAuth.isLoading || !hasCheckedProfile) {
    return <LoadingSpinner fullHeight />;
  }

  // Once processing is complete, this component doesn't need to render anything
  return null;
};
