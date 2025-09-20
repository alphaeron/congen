import { Box, Card, CardContent, Typography, Button, Divider } from '@mui/material';
import React from 'react';

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
    <Card sx={{ mb: 3 }}>
      <CardContent>
        <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
          <Typography variant="h6">{title}</Typography>
          <Button variant="outlined" size="small" onClick={onAddClick}>
            {addButtonText}
          </Button>
        </Box>
        <Typography variant="body2" color="text.secondary" paragraph>
          {description}
        </Typography>

        <Divider sx={{ mb: 2 }} />

        {!hasItems && emptyMessage ? (
          <Typography variant="body2" color="text.secondary" sx={{ textAlign: 'center', py: 2 }}>
            {emptyMessage}
          </Typography>
        ) : (
          children
        )}
      </CardContent>
    </Card>
  );
};
