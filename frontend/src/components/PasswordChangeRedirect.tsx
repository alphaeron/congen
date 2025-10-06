import { Box } from '@mui/material';
import { useSnackbar } from 'notistack';
import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router';

import { GameText } from './GameTheme';
import { LoadingSpinner } from './LoadingSpinner';

/**
 * Component to handle redirects back from Keycloak after password changes.
 *
 * This component checks if the user has returned from a password change operation
 * and redirects them back to their original location in the application.
 */
export const PasswordChangeRedirect: React.FC = () => {
  const navigate = useNavigate();
  const { enqueueSnackbar } = useSnackbar();
  const [isRedirecting, setIsRedirecting] = useState(false);

  useEffect(() => {
    const handleRedirect = async () => {
      try {
        setIsRedirecting(true);

        // Check if we have a stored redirect path from a password change operation
        const redirectPath = sessionStorage.getItem('congen_redirect_after_password_change');

        if (redirectPath) {
          // Clear the stored path
          sessionStorage.removeItem('congen_redirect_after_password_change');

          // Show success message
          enqueueSnackbar('Password changed successfully!', { variant: 'success' });

          // Small delay to show the success message
          setTimeout(() => {
            // Redirect back to the original location
            navigate(redirectPath, { replace: true });
          }, 1500);
        } else {
          // No redirect path found, go to profile page
          navigate('/profile', { replace: true });
        }
      } catch {
        enqueueSnackbar('Failed to redirect. Please try again.', { variant: 'error' });
        navigate('/profile', { replace: true });
      } finally {
        setIsRedirecting(false);
      }
    };

    // Wait a moment for the component to mount
    const timer = setTimeout(handleRedirect, 100);
    return () => clearTimeout(timer);
  }, [navigate, enqueueSnackbar]);

  if (isRedirecting) {
    return (
      <Box display="flex" flexDirection="column" alignItems="center" gap={2} p={4}>
        <LoadingSpinner />
        <GameText variant="h6">Password changed successfully!</GameText>
        <GameText variant="body2" textVariant="secondary" textAlign="center">
          Redirecting you back to your account settings...
        </GameText>
      </Box>
    );
  }

  return (
    <Box display="flex" flexDirection="column" alignItems="center" gap={2} p={4}>
      <GameText variant="h6">Redirecting...</GameText>
      <GameText variant="body2" textVariant="secondary" textAlign="center">
        Please wait while we process your request.
      </GameText>
    </Box>
  );
};
