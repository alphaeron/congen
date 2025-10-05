import { Box, Divider, CardContent } from '@mui/material';
import React from 'react';

import { GameText, GameCard, GameButton, GAME_CLASSES } from './GameTheme';

interface PreferenceSectionProps {
  title: string;
  description: string;
  addButtonText: string;
  onAddClick: () => void;
  children: React.ReactNode;
  emptyMessage?: string;
  hasItems: boolean;
}

/**
 * Reusable preference section component with consistent layout and styling.
 *
 * Provides a standardized layout for preference sections including title,
 * description, add button, and content area with empty state handling.
 *
 * @param title Section title
 * @param description Section description text
 * @param addButtonText Text for the add button
 * @param onAddClick Function to call when add button is clicked
 * @param children Content to display in the section
 * @param emptyMessage Message to show when no items are present
 * @param hasItems Whether there are items to display
 * @return Preference section component
 */
export const PreferenceSection: React.FC<PreferenceSectionProps> = ({
  title,
  description,
  addButtonText,
  onAddClick,
  children,
  emptyMessage,
  hasItems,
}) => {
  return (
    <GameCard className={GAME_CLASSES.marginBottom3} sx={{ marginTop: 3 }}>
      <CardContent>
        <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
          <GameText variant="h6">{title}</GameText>
          <GameButton variant="outlined" size="small" onClick={onAddClick}>
            {addButtonText}
          </GameButton>
        </Box>
        <GameText variant="body2" textVariant="secondary" paragraph>
          {description}
        </GameText>

        <Divider sx={{ mb: 2 }} />

        {!hasItems && emptyMessage ? (
          <GameText variant="body2" textVariant="secondary" className={`${GAME_CLASSES.textCenter} ${GAME_CLASSES.padding2}`}>
            {emptyMessage}
          </GameText>
        ) : (
          children
        )}
      </CardContent>
    </GameCard>
  );
};
