import React, { useEffect } from 'react';
import { useAuth as useOidcAuth } from 'react-oidc-context';
import { useNavigate, useLocation } from 'react-router-dom';
import { Box, CircularProgress, Typography } from '@mui/material';

/**
 * Component to handle OIDC authentication callback.
 * This component manually processes the authentication callback from Keycloak
 * and redirects the user to the appropriate page.
 */
export const AuthCallback: React.FC = () => {
  const oidcAuth = useOidcAuth();
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    const handleCallback = async () => {
      try {
        console.log('AuthCallback: Processing callback...');
        console.log('AuthCallback: Current URL:', location.pathname + location.search);
        console.log('AuthCallback: OIDC state:', {
          isLoading: oidcAuth.isLoading,
          isAuthenticated: oidcAuth.isAuthenticated,
          user: oidcAuth.user ? 'present' : 'null',
          error: oidcAuth.error ? oidcAuth.error.message : 'none'
        });

        // Check if we have a code parameter in the URL (OIDC callback)
        const urlParams = new URLSearchParams(location.search);
        const code = urlParams.get('code');
        const state = urlParams.get('state');
        const error = urlParams.get('error');

        if (error) {
          console.error('AuthCallback: OIDC error:', error);
          navigate('/login', { 
            state: { 
              error: `Authentication failed: ${error}` 
            },
            replace: true 
          });
          return;
        }

        if (!code) {
          console.log('AuthCallback: No code parameter, redirecting to login');
          navigate('/login', { replace: true });
          return;
        }

        // If we have a code, wait for the OIDC library to process it
        if (oidcAuth.isLoading) {
          console.log('AuthCallback: OIDC is loading, waiting...');
          return;
        }

        // Check if authentication was successful
        if (oidcAuth.isAuthenticated && oidcAuth.user) {
          console.log('AuthCallback: Authentication successful, redirecting to profile');
          // Clean up the URL by removing the callback parameters
          window.history.replaceState({}, document.title, '/profile');
          navigate('/profile', { replace: true });
          return;
        }

        // Check if there was an error
        if (oidcAuth.error) {
          console.error('AuthCallback: OIDC error:', oidcAuth.error);
          navigate('/login', { 
            state: { 
              error: oidcAuth.error.message 
            },
            replace: true 
          });
          return;
        }

        // If we get here, something went wrong
        console.log('AuthCallback: Unexpected state, redirecting to login');
        navigate('/login', { 
          state: { 
            error: 'Authentication failed. Please try again.' 
          },
          replace: true 
        });

      } catch (error) {
        console.error('AuthCallback: Error handling callback:', error);
        navigate('/login', { 
          state: { 
            error: 'Authentication failed. Please try again.' 
          },
          replace: true 
        });
      }
    };

    // Add a small delay to allow the OIDC library to process
    const timer = setTimeout(handleCallback, 100);
    return () => clearTimeout(timer);
  }, [oidcAuth.isLoading, oidcAuth.isAuthenticated, oidcAuth.user, oidcAuth.error, navigate, location]);

  return (
    <Box
      sx={{
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'center',
        alignItems: 'center',
        height: '100vh',
        gap: 2,
      }}
    >
      <CircularProgress size={60} />
      <Typography variant="h6" color="text.secondary">
        Processing authentication...
      </Typography>
    </Box>
  );
}; 