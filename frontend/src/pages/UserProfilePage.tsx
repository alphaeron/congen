import { Container, Typography } from '@mui/material';
import React from 'react';
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
  const { user, isLoading } = useAuth();
  const [searchParams] = useSearchParams();

  // Show loading spinner while checking authentication status or creating profile
  if (isLoading) {
    return <LoadingSpinner />;
  }

  // If user doesn't have a profile yet, show a loading message
  if (!user) {
    return (
      <Container component="main" maxWidth="sm" sx={{ mt: 3, textAlign: 'center' }}>
        <LoadingSpinner message="Creating Your Profile" />
        <Typography variant="body2" color="text.secondary">
          Your profile is being created automatically using your Keycloak information...
        </Typography>
      </Container>
    );
  }

  // Extract query parameters
  const section = searchParams.get('section') || 'physical';

  // If user has a profile, show the profile view with query parameters
  return <UserProfile user={user} initialSection={section} />;
};
