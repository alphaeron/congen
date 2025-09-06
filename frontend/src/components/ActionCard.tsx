import { Card, CardContent, Box, Typography } from '@mui/material';
import React from 'react';

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
    <Card
      elevation={2}
      sx={{
        borderRadius: 2,
        cursor: clickable ? 'pointer' : 'default',
        '&:hover': clickable ? { backgroundColor: 'action.hover' } : {},
      }}
      onClick={clickable ? onClick : undefined}
    >
      <CardContent>
        <Box
          display="flex"
          justifyContent="space-between"
          alignItems="flex-start"
          sx={{ mb: children ? 2 : 0 }}
        >
          <Box>
            <Typography variant="h6" component="h3">
              {title}
            </Typography>
            {subtitle && (
              <Typography variant="body2" color="text.secondary">
                {subtitle}
              </Typography>
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
    </Card>
  );
};
