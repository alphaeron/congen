import { Container, Alert, CircularProgress, Typography } from '@mui/material';
import React, { useEffect } from 'react';
import { useSearchParams } from 'react-router';

import { LoadingSpinner } from '../components/LoadingSpinner';
import { UserProfile } from '../components/UserProfile';
import { useAuth } from '../contexts/AuthContext';

/**
 * User profile page component.
 *
 * Handles routing and data gathering for the user profile page.
 * Automatically creates user profile from Keycloak information if needed,
 * otherwise shows the UserProfile component for the interface.
 * Manages URL query parameters for section navigation.
 *
 * @return User profile page component
 */
export const UserProfilePage: React.FC = () => {
  const { user, isLoading, error, clearError } = useAuth();
  const [searchParams] = useSearchParams();

  // Clear any errors when component mounts
  useEffect(() => {
    if (error) {
      clearError();
    }
  }, [error, clearError]);

  // Show loading spinner while checking authentication status or creating profile
  if (isLoading) {
    return <LoadingSpinner />;
  }

  // If there's an error, show the error
  if (error) {
    return (
      <Container maxWidth="md" sx={{ mt: 4 }}>
        <Alert severity="error">{error}</Alert>
      </Container>
    );
  }

  // If user doesn't have a profile yet, show a loading message
  if (!user) {
    return (
      <Container component="main" maxWidth="sm" sx={{ mt: 4, textAlign: 'center' }}>
        <CircularProgress sx={{ mb: 2 }} />
        <Typography variant="h6" component="h1" gutterBottom>
          Creating Your Profile
        </Typography>
        <Typography variant="body2" color="text.secondary">
          Your profile is being created automatically using your Keycloak information...
        </Typography>
      </Container>
    );
  }

  // Extract query parameters
  const section = searchParams.get('section') || 'overview';

  // If user has a profile, show the profile view with query parameters
  return <UserProfile user={user} initialSection={section} />;
};
