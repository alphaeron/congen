import { Card, CardContent, Typography, Paper, Box } from '@mui/material';
import React from 'react';

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
  const Container = variant === 'paper' ? Paper : Card;
  const containerProps =
    variant === 'paper'
      ? {
          sx: {
            p: 6,
            textAlign: 'center',
            borderRadius: 3,
            background: theme =>
              `linear-gradient(135deg, ${theme.palette.background.paper}, ${theme.palette.background.paper})`,
            border: theme => `1px solid ${theme.palette.divider}`,
          },
        }
      : {};

  return (
    <Container {...containerProps}>
      <CardContent sx={{ textAlign: 'center', py: 4 }}>
        <Typography variant="h6" gutterBottom>
          {title}
        </Typography>
        <Typography variant="body1" color="text.secondary">
          {message}
        </Typography>
        {action && <Box sx={{ mt: 2 }}>{action}</Box>}
      </CardContent>
    </Container>
  );
};
