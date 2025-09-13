import { Chip, Tooltip } from '@mui/material';
import { Delete as DeleteIcon } from '@mui/icons-material';
import React from 'react';

interface DeletableChipProps {
  label: string;
  onDelete: () => void;
  deleteTooltip: string;
  disabled?: boolean;
  color?: 'default' | 'primary' | 'secondary' | 'error' | 'info' | 'success' | 'warning';
  variant?: 'filled' | 'outlined';
}

/**
 * Reusable chip component with delete functionality and tooltip.
 *
 * Provides a standardized chip with delete functionality and tooltip
 * for consistent UX across preference sections.
 *
 * @param label Text to display in the chip
 * @param onDelete Function to call when delete button is clicked
 * @param deleteTooltip Tooltip text for the delete button
 * @param disabled Whether the delete button should be disabled
 * @param color Color variant of the chip
 * @param variant Style variant of the chip
 * @return Deletable chip component
 */
export const DeletableChip: React.FC<DeletableChipProps> = ({
  label,
  onDelete,
  deleteTooltip,
  disabled = false,
  color = 'default',
  variant = 'outlined',
}) => {
  return (
    <Tooltip title={deleteTooltip}>
      <Chip
        label={label}
        onDelete={onDelete}
        disabled={disabled}
        color={color}
        variant={variant}
        deleteIcon={<DeleteIcon />}
      />
    </Tooltip>
  );
};
