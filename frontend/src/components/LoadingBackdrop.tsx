import { Backdrop } from '@mui/material';
import React from 'react';

import { LoadingSpinner } from './LoadingSpinner';
import { GameText, GAME_CLASSES } from './GameTheme';

interface LoadingBackdropProps {
  open: boolean;
  message: string;
  subMessage?: string;
  spinnerSize?: number;
}

/**
 * Reusable loading backdrop component for full-screen loading states.
 *
 * Provides a consistent interface for showing loading states during
 * async operations like data creation, updates, or generation.
 *
 * @param open Whether the backdrop is visible
 * @param message Primary loading message
 * @param subMessage Optional secondary message for additional context
 * @param spinnerSize Size of the loading spinner (default: 60)
 * @return Loading backdrop component
 */
export const LoadingBackdrop: React.FC<LoadingBackdropProps> = ({
  open,
  message,
  subMessage,
  spinnerSize = 60,
}) => {
  return (
    <Backdrop
      sx={{
        color: '#fff',
        zIndex: theme => theme.zIndex.drawer + 1,
        display: 'flex',
        flexDirection: 'column',
        gap: 2,
      }}
      open={open}
    >
      <LoadingSpinner message={message} size={spinnerSize} />
      {subMessage && (
        <GameText variant="body2" textVariant="secondary" className={GAME_CLASSES.opacity80}>
          {subMessage}
        </GameText>
      )}
    </Backdrop>
  );
};
