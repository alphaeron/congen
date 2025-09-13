import { Typography } from '@mui/material';
import React from 'react';

interface NavigationItemProps {
  label: string;
  isActive: boolean;
  onClick: () => void;
}

/**
 * Reusable navigation item component with active state styling.
 *
 * Provides a standardized navigation item with hover effects and
 * active state styling for consistent navigation UX.
 *
 * @param label Text to display for the navigation item
 * @param isActive Whether this navigation item is currently active
 * @param onClick Function to call when the item is clicked
 * @return Navigation item component
 */
export const NavigationItem: React.FC<NavigationItemProps> = ({
  label,
  isActive,
  onClick,
}) => {
  return (
    <Typography
      variant="body1"
      onClick={onClick}
      sx={{
        cursor: 'pointer',
        color: isActive ? 'primary.main' : 'text.primary',
        textDecoration: isActive ? 'underline' : 'none',
        fontWeight: isActive ? 'bold' : 'normal',
        '&:hover': {
          color: 'primary.main',
          textDecoration: 'underline',
        },
      }}
    >
      {label}
    </Typography>
  );
};
