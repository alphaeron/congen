import React, { useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import Container from '@mui/material/Container';
import Typography from '@mui/material/Typography';
import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
import Grid from '@mui/material/Grid';

import { useAuth } from '../auth/AuthContext';

/**
 * Login page component.
 *
 * Handles the authentication flow and provides information about account creation.
 * Users can sign in with existing accounts or create new ones through Keycloak.
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
    <Container maxWidth="md">
      <Box
        sx={{
          marginTop: 8,
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
        }}
      >
        <Typography component="h1" variant="h3" gutterBottom>
          Welcome to ConGen
        </Typography>
        <Typography component="h2" variant="h6" color="text.secondary" gutterBottom>
          Your AI-powered workout companion
        </Typography>

        <Grid container spacing={3} sx={{ mt: 4 }}>
          <Grid item xs={12} md={6}>
            <Card>
              <CardContent>
                <Typography variant="h5" component="h3" gutterBottom>
                  Sign In
                </Typography>
                <Typography variant="body2" color="text.secondary" paragraph>
                  Access your personalized workout programs and track your progress.
                </Typography>
                <Button
                  variant="contained"
                  size="large"
                  onClick={handleLogin}
                  sx={{ width: '100%' }}
                >
                  Sign In
                </Button>
              </CardContent>
            </Card>
          </Grid>

          <Grid item xs={12} md={6}>
            <Card>
              <CardContent>
                <Typography variant="h5" component="h3" gutterBottom>
                  Create Account
                </Typography>
                <Typography variant="body2" color="text.secondary" paragraph>
                  New to ConGen? Create an account to get started with personalized workout
                  programs.
                </Typography>
                <Button
                  variant="outlined"
                  size="large"
                  onClick={handleLogin}
                  sx={{ width: '100%' }}
                >
                  Sign Up
                </Button>
              </CardContent>
            </Card>
          </Grid>
        </Grid>

        <Box sx={{ mt: 4, textAlign: 'center' }}>
          <Typography variant="body2" color="text.secondary">
            By signing in or creating an account, you'll be able to:
          </Typography>
          <Box component="ul" sx={{ mt: 2, textAlign: 'left', display: 'inline-block' }}>
            <Typography component="li" variant="body2" color="text.secondary">
              Generate personalized workout programs
            </Typography>
            <Typography component="li" variant="body2" color="text.secondary">
              Track your exercise preferences and equipment
            </Typography>
            <Typography component="li" variant="body2" color="text.secondary">
              Monitor your progress and one-rep maxes
            </Typography>
            <Typography component="li" variant="body2" color="text.secondary">
              Access a comprehensive exercise database
            </Typography>
          </Box>
        </Box>
      </Box>
    </Container>
  );
};
