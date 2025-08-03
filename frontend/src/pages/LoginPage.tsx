import React from 'react';
import { useAuth } from '../contexts/AuthContext';
import { Button, Container, Typography, Box } from '@mui/material';
import { LoadingSpinner } from '../components/LoadingSpinner';

/**
 * Login page component that provides a sign in button.
 *
 * @return The login page component
 */
export const LoginPage: React.FC = () => {
  const { isLoading, login } = useAuth();

  const handleSignIn = async () => {
    try {
      await login();
    } catch {
      // Error is handled by the auth context
    }
  };

  // Show loading state while authentication is being determined
  if (isLoading) {
    return <LoadingSpinner fullHeight />;
  }

  return (
    <Container maxWidth="sm">
      <Box
        sx={{
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center',
          minHeight: '60vh',
          gap: 3,
        }}
      >
        <Typography variant="h4" component="h1" gutterBottom>
          Sign In
        </Typography>
        <Typography variant="body1" color="text.secondary" textAlign="center" gutterBottom>
          Please sign in to access your account
        </Typography>
        <Button
          color="primary"
          variant="contained"
          size="large"
          onClick={handleSignIn}
          sx={{ minWidth: 200 }}
        >
          Sign In
        </Button>
      </Box>
    </Container>
  );
}; 