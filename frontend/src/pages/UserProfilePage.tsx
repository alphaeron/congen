import { Container, Alert } from '@mui/material';
import React from 'react';

import { LoadingSpinner } from '../components/LoadingSpinner';
import { ProfileCreationForm } from '../components/ProfileCreationForm';
import { UserProfile } from '../components/UserProfile';
import { useAuth } from '../contexts/AuthContext';

/**
 * User profile page component.
 *
 * Handles routing and data gathering for the user profile page.
 * Shows profile creation form if user doesn't have a profile,
 * otherwise shows the UserProfile component for the interface.
 *
 * @return User profile page component
 */
export const UserProfilePage: React.FC = () => {
  const { user, isLoading, error } = useAuth();

  // Show loading spinner while checking authentication status
  if (isLoading) {
    return <LoadingSpinner />;
  }

  // If there's an error, show error message
  if (error) {
    return (
      <Container maxWidth="md" sx={{ mt: 4 }}>
        <Alert severity="error">{error}</Alert>
      </Container>
    );
  }

  // If user doesn't have a profile, show the creation form
  if (!user) {
    return (
      <Container component="main" maxWidth="sm">
        <ProfileCreationForm />
      </Container>
    );
  }

  // If user has a profile, show the profile view
  return <UserProfile user={user} />;
};
