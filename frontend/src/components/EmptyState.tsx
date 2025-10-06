import { CardContent, Paper, Box } from '@mui/material';
import React from 'react';

import { GameCard, GameText } from './GameTheme';

interface EmptyStateProps {
  title: string;
  message: string;
  variant?: 'card' | 'paper';
  action?: React.ReactNode;
}

/**
 * Reusable empty state component for displaying when lists are empty or no results found.
 *
 * Provides a consistent interface for empty states with centered text,
 * helpful messages, and optional action buttons.
 *
 * @param title Main title for the empty state
 * @param message Descriptive message explaining the empty state
 * @param variant Visual variant - 'card' for bordered card, 'paper' for elevated paper (default: 'card')
 * @param action Optional action button or element to display
 * @return Empty state component
 */
export const EmptyState: React.FC<EmptyStateProps> = ({
  title,
  message,
  variant = 'card',
  action,
}) => {
  const Container = variant === 'paper' ? Paper : GameCard;

  return (
    <Container sx={{ textAlign: 'center' }}>
      <CardContent sx={{ textAlign: 'center', py: 4 }}>
        <GameText variant="h6" gutterBottom>
          {title}
        </GameText>
        <GameText variant="body1" textVariant="secondary">
          {message}
        </GameText>
        {action && <Box sx={{ mt: 2 }}>{action}</Box>}
      </CardContent>
    </Container>
  );
};
