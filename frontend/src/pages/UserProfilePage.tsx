import React from 'react';
import { Container, Alert } from '@mui/material';
import { useAuth } from '../contexts/AuthContext';
import { UserProfile } from '../components/UserProfile';
import { ProfileCreationForm } from '../components/ProfileCreationForm';
import { LoadingSpinner } from '../components/LoadingSpinner';

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

  // If there's an authentication error, show error message
  if (error && error.includes('Authentication failed')) {
    return (
      <Container maxWidth="md" sx={{ mt: 4 }}>
        <Alert severity="error">
          Authentication failed. Please log in to view your profile.
        </Alert>
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
