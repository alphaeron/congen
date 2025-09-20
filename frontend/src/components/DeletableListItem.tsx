import { Delete as DeleteIcon } from '@mui/icons-material';
import {
  ListItem,
  ListItemText,
  ListItemSecondaryAction,
  IconButton,
  Tooltip,
} from '@mui/material';
import React from 'react';

interface DeletableListItemProps {
  primary: string;
  secondary?: string;
  onDelete: () => void;
  deleteTooltip: string;
  disabled?: boolean;
}

/**
 * Reusable list item component with delete functionality and tooltip.
 *
 * Provides a standardized list item with primary/secondary text and
 * a delete button with tooltip for consistent UX across preference sections.
 *
 * @param primary Primary text to display
 * @param secondary Optional secondary text to display
 * @param onDelete Function to call when delete button is clicked
 * @param deleteTooltip Tooltip text for the delete button
 * @param disabled Whether the delete button should be disabled
 * @return Deletable list item component
 */
export const DeletableListItem: React.FC<DeletableListItemProps> = ({
  primary,
  secondary,
  onDelete,
  deleteTooltip,
  disabled = false,
}) => {
  return (
    <ListItem>
      <ListItemText primary={primary} secondary={secondary} />
      <ListItemSecondaryAction>
        <Tooltip title={deleteTooltip}>
          {disabled ? (
            <span>
              <IconButton edge="end" aria-label="delete" onClick={onDelete} disabled={disabled}>
                <DeleteIcon />
              </IconButton>
            </span>
          ) : (
            <IconButton edge="end" aria-label="delete" onClick={onDelete} disabled={disabled}>
              <DeleteIcon />
            </IconButton>
          )}
        </Tooltip>
      </ListItemSecondaryAction>
    </ListItem>
  );
};
