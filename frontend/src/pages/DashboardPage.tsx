import { Container, Alert, CircularProgress, Typography } from '@mui/material';
import React, { useEffect } from 'react';
import { useSearchParams } from 'react-router';

import { LoadingSpinner } from '../components/LoadingSpinner';
import { Dashboard } from '../components/Dashboard';
import { useAuth } from '../contexts/AuthContext';

/**
 * Dashboard page component.
 *
 * Handles routing and data gathering for the dashboard page.
 * Shows the Dashboard component for the interface once user is authenticated.
 * Manages URL query parameters for section navigation.
 *
 * @return Dashboard page component
 */
export const DashboardPage: React.FC = () => {
  const { user, isLoading, error, clearError } = useAuth();
  const [searchParams] = useSearchParams();

  // Clear any errors when component mounts
  useEffect(() => {
    if (error) {
      clearError();
    }
  }, [error, clearError]);

  // Show loading spinner while checking authentication status
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
          Loading Dashboard
        </Typography>
        <Typography variant="body2" color="text.secondary">
          Please ensure you have a profile to access the dashboard...
        </Typography>
      </Container>
    );
  }

  // Extract query parameters
  const section = searchParams.get('section') || 'overview';
  const workout = searchParams.get('workout');

  // If user has a profile, show the dashboard view with query parameters
  return <Dashboard user={user} initialSection={section} selectedWorkout={workout} />;
};
