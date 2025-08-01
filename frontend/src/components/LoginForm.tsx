import React from 'react';
import { Box, Button, Typography, Alert, CircularProgress } from '@mui/material';
import { useAuth } from '../contexts/AuthContext';

interface LoginFormProps {
  onSuccess?: () => void;
}

export const LoginForm: React.FC<LoginFormProps> = () => {
  const { login, isLoading, error, clearError } = useAuth();

  const handleLogin = async () => {
    clearError();

    try {
      await login();
      // Note: The actual authentication happens in the callback component
      // This function just initiates the OAuth flow
    } catch {
      // Error is handled by the auth context
    }
  };

  return (
    <Box component="form" sx={{ mt: 1 }}>
      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
        Sign in to access your personalized workout programs and track your progress.
      </Typography>

      <Button
        type="button"
        fullWidth
        variant="contained"
        onClick={handleLogin}
        disabled={isLoading}
        sx={{ mt: 3, mb: 2 }}
      >
        {isLoading ? <CircularProgress size={24} /> : 'Sign In'}
      </Button>

      <Typography variant="body2" color="text.secondary" align="center" sx={{ mt: 2 }}>
        You will be redirected to our secure authentication provider.
      </Typography>
    </Box>
  );
};
