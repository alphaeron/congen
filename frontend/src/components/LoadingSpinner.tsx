import { Box } from '@mui/material';
import React from 'react';
import { motion } from 'framer-motion';

import { GameText, GAME_CLASSES } from './GameTheme';

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
        }}
      >
        <motion.div
          style={{
            position: 'absolute',
            top: 0,
            left: 0,
            width: '100%',
            height: '100%',
            borderRadius: '50%',
            border: '3px solid transparent',
            borderTop: '3px solid #00bcd4',
          }}
          animate={{ rotate: 360 }}
          transition={{
            duration: 1,
            repeat: Infinity,
            ease: 'linear'
          }}
        />
        <motion.div
          style={{
            position: 'absolute',
            top: '10%',
            left: '10%',
            width: '80%',
            height: '80%',
            borderRadius: '50%',
            border: '2px solid transparent',
            borderTop: '2px solid #00acc1',
          }}
          animate={{ rotate: -360 }}
          transition={{
            duration: 1.5,
            repeat: Infinity,
            ease: 'linear'
          }}
        />
      </Box>

      {message && (
        <GameText
          variant="h6"
          textVariant="secondary"
          sx={{
            fontWeight: 500,
            textAlign: 'center',
            opacity: 0.8,
          }}
        >
          {message}
        </GameText>
      )}

    </Box>
  );
};
