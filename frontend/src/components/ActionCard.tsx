import { CardContent, Box } from '@mui/material';
import React from 'react';

import { GameCard, GameText, GAME_CLASSES } from './GameTheme';

interface ActionCardProps {
  title: string;
  subtitle?: string;
  actions?: React.ReactNode;
  children?: React.ReactNode;
  onClick?: () => void;
  clickable?: boolean;
}

/**
 * Reusable action card component with consistent layout for cards with action buttons.
 *
 * Provides a standardized layout for cards that contain a title, optional subtitle,
 * action buttons in the top-right corner, and content area.
 *
 * @param title Card title
 * @param subtitle Optional subtitle text
 * @param actions Action buttons or icons to display in the top-right
 * @param children Content to display in the card body
 * @param onClick Optional click handler for the entire card
 * @param clickable Whether the card should appear clickable (default: false)
 * @return Action card component
 */
export const ActionCard: React.FC<ActionCardProps> = ({
  title,
  subtitle,
  actions,
  children,
  onClick,
  clickable = false,
}) => {
  return (
    <GameCard interactive={clickable} onClick={clickable ? onClick : undefined}>
      <CardContent>
        <Box
          display="flex"
          justifyContent="space-between"
          alignItems="flex-start"
          sx={{ mb: children ? 2 : 0 }}
        >
          <Box>
            <GameText variant="h6">{title}</GameText>
            {subtitle && (
              <GameText variant="body2" className={GAME_CLASSES.opacity80}>
                {subtitle}
              </GameText>
            )}
          </Box>
          {actions && (
            <Box display="flex" gap={1}>
              {actions}
            </Box>
          )}
        </Box>
        {children}
      </CardContent>
    </GameCard>
  );
};
