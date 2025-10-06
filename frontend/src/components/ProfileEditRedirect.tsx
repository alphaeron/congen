import { Box } from '@mui/material';
import { useSnackbar } from 'notistack';
import React, { useEffect, useState } from 'react';
import { useAuth } from 'react-oidc-context';
import { useNavigate } from 'react-router';

import { GameText } from './GameTheme';
import { LoadingSpinner } from './LoadingSpinner';
import { updateUserProfile } from '../api/user';

/**
 * Component to handle redirects back from Keycloak after profile edits.
 *
 * This component checks if the user has returned from a profile edit operation
 * and syncs any changes with the backend before redirecting them back to
 * their original location in the application.
 */
export const ProfileEditRedirect: React.FC = () => {
  const navigate = useNavigate();
  const auth = useAuth();
  const { enqueueSnackbar } = useSnackbar();
  const [isSyncing, setIsSyncing] = useState(false);

  useEffect(() => {
    const syncProfileChanges = async () => {
      try {
        setIsSyncing(true);

        // Check if we have a stored redirect path from a profile edit operation
        const redirectPath = sessionStorage.getItem('congen_redirect_after_profile_edit');

        if (redirectPath) {
          // Clear the stored path
          sessionStorage.removeItem('congen_redirect_after_profile_edit');

          // Get the user's current profile from Keycloak token
          if (auth.user?.profile) {
            const { name } = auth.user.profile;

            // Sync the profile changes with the backend
            if (name) {
              await updateUserProfile({ name });
            }
          }

          // Show success message
          enqueueSnackbar('Profile updated successfully!', { variant: 'success' });

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
        enqueueSnackbar('Failed to sync profile changes. Please try again.', { variant: 'error' });
      } finally {
        setIsSyncing(false);
      }
    };

    // Wait a moment for the auth context to be ready
    const timer = setTimeout(syncProfileChanges, 100);
    return () => clearTimeout(timer);
  }, [navigate, auth.user]);

  if (isSyncing) {
    return (
      <Box display="flex" flexDirection="column" alignItems="center" gap={2} p={4}>
        <LoadingSpinner />
        <GameText variant="h6">Profile updated successfully!</GameText>
        <GameText variant="body2" textVariant="secondary" textAlign="center">
          Redirecting you back to your profile...
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
