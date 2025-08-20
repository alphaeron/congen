import { Box, Typography } from '@mui/material';
import React from 'react';

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
        gap: 3,
        py: fullHeight ? 0 : 6,
      }}
    >
      {/* Modern animated spinner */}
      <Box
        sx={{
          position: 'relative',
          width: size,
          height: size,
          '&::before': {
            content: '""',
            position: 'absolute',
            top: 0,
            left: 0,
            width: '100%',
            height: '100%',
            borderRadius: '50%',
            border: `3px solid transparent`,
            borderTop: '3px solid',
            borderTopColor: 'primary.main',
            animation: 'spin 1s linear infinite',
          },
          '&::after': {
            content: '""',
            position: 'absolute',
            top: '10%',
            left: '10%',
            width: '80%',
            height: '80%',
            borderRadius: '50%',
            border: `2px solid transparent`,
            borderTop: '2px solid',
            borderTopColor: 'secondary.main',
            animation: 'spin 1.5s linear infinite reverse',
          },
        }}
      />
      
      {message && (
        <Typography 
          variant="h6" 
          color="text.secondary"
          sx={{
            fontWeight: 500,
            textAlign: 'center',
            opacity: 0.8,
          }}
        >
          {message}
        </Typography>
      )}

      {/* CSS Animation */}
      <style>{`
        @keyframes spin {
          0% { transform: rotate(0deg); }
          100% { transform: rotate(360deg); }
        }
      `}</style>
    </Box>
  );
};
