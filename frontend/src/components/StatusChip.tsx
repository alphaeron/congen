import { Chip } from '@mui/material';
import React from 'react';

interface StatusChipProps {
  label: string;
  status: 'active' | 'inactive' | 'success' | 'warning' | 'error' | 'info' | 'default';
  size?: 'small' | 'medium';
  variant?: 'filled' | 'outlined';
}

/**
 * Reusable status chip component with consistent styling for different status types.
 *
 * Provides a standardized way to display status information with appropriate
 * colors and styling based on the status type.
 *
 * @param label Text to display in the chip
 * @param status Status type that determines the color
 * @param size Size of the chip (default: 'small')
 * @param variant Visual variant (default: 'filled')
 * @return Status chip component
 */
export const StatusChip: React.FC<StatusChipProps> = ({
  label,
  status,
  size = 'small',
  variant = 'filled',
}) => {
  const getColor = () => {
    switch (status) {
      case 'active':
      case 'success':
        return 'success';
      case 'inactive':
        return 'default';
      case 'warning':
        return 'warning';
      case 'error':
        return 'error';
      case 'info':
        return 'info';
      default:
        return 'default';
    }
  };

  return <Chip label={label} color={getColor()} size={size} variant={variant} />;
};
