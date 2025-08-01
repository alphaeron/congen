import React from 'react';
import { Container, Alert } from '@mui/material';
import { useAuth } from '../contexts/AuthContext';
import { UserProfile } from '../components/UserProfile';

/**
 * User profile page component.
 *
 * Handles routing and data gathering for the user profile page.
 * Uses the UserProfile component for the interface.
 *
 * @return User profile page component
 */
export const UserProfilePage: React.FC = () => {
  const { user } = useAuth();

  if (!user) {
    return (
      <Container maxWidth="md" sx={{ mt: 4 }}>
        <Alert severity="error">
          User information not available. Please log in to view your profile.
        </Alert>
      </Container>
    );
  }

  return <UserProfile user={user} />;
};
