import React, { useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import Container from '@mui/material/Container';
import Typography from '@mui/material/Typography';

import { useAuth } from '../auth/AuthContext';

/**
 * Login page component.
 *
 * Handles the authentication flow and redirects users to their intended
 * destination after successful login.
 *
 * @return Login page component
 */
export const LoginPage: React.FC = () => {
  const { authenticated, login, loading } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  // Get the intended destination from location state
  const from = (location.state as { from?: { pathname: string } })?.from?.pathname || '/';

  useEffect(() => {
    // If already authenticated, redirect to intended destination
    if (authenticated && !loading) {
      navigate(from, { replace: true });
    }
  }, [authenticated, loading, navigate, from]);

  const handleLogin = () => {
    login();
  };

  if (loading) {
    return (
      <Container maxWidth="sm">
        <Box
          sx={{
            marginTop: 8,
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
          }}
        >
          <Typography component="h1" variant="h5">
            Loading...
          </Typography>
        </Box>
      </Container>
    );
  }

  return (
    <Container maxWidth="sm">
      <Box
        sx={{
          marginTop: 8,
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
        }}
      >
        <Typography component="h1" variant="h4" gutterBottom>
          Welcome to ConGen
        </Typography>
        <Typography component="h2" variant="h6" color="text.secondary" gutterBottom>
          Sign in to access your workout programs
        </Typography>
        <Button
          variant="contained"
          size="large"
          onClick={handleLogin}
          sx={{ mt: 3, mb: 2 }}
        >
          Sign In
        </Button>
      </Box>
    </Container>
  );
}; 