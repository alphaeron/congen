import React from 'react';
import { Box, CircularProgress, Typography } from '@mui/material';

interface LoadingSpinnerProps {
  message?: string;
  size?: number;
  fullHeight?: boolean;
}

/**
 * Reusable loading spinner component with optional message.
 *
 * @param message Optional message to display below the spinner
 * @param size Size of the spinner (default: 60)
 * @param fullHeight Whether to take full viewport height (default: false)
 * @return Loading spinner component
 */
export const LoadingSpinner: React.FC<LoadingSpinnerProps> = ({
  message = 'Loading...',
  size = 60,
  fullHeight = false,
}) => {
  return (
    <Box
      sx={{
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'center',
        alignItems: 'center',
        height: fullHeight ? '100vh' : 'auto',
        gap: 2,
        py: fullHeight ? 0 : 4,
      }}
    >
      <CircularProgress size={size} />
      {message && (
        <Typography variant="h6" color="text.secondary">
          {message}
        </Typography>
      )}
    </Box>
  );
};
