import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  DialogContentText,
} from '@mui/material';
import React from 'react';

import { GameText, GameAlert, GAME_CLASSES } from './GameTheme';

interface ConfirmationDialogProps {
  open: boolean;
  onClose: () => void;
  onConfirm: () => void;
  title: string;
  message?: string;
  confirmText?: string;
  cancelText?: string;
  confirmColor?: 'primary' | 'secondary' | 'error' | 'warning' | 'info' | 'success';
  loading?: boolean;
  error?: string | null;
  children?: React.ReactNode;
  disabled?: boolean;
}

/**
 * Reusable confirmation dialog component for delete, stop, resume, and other confirmation actions.
 *
 * Provides a consistent interface for confirmation dialogs with optional error display
 * and loading states.
 *
 * @param open Whether the dialog is open
 * @param onClose Function to call when dialog should be closed
 * @param onConfirm Function to call when user confirms the action
 * @param title Dialog title
 * @param message Optional confirmation message to display
 * @param confirmText Text for the confirm button (default: "Confirm")
 * @param cancelText Text for the cancel button (default: "Cancel")
 * @param confirmColor Color of the confirm button (default: "primary")
 * @param loading Whether the action is in progress
 * @param error Error message to display if any
 * @param children Optional custom content to render in the dialog
 * @param disabled Whether the confirm button should be disabled
 * @return Confirmation dialog component
 */
export const ConfirmationDialog: React.FC<ConfirmationDialogProps> = ({
  open,
  onClose,
  onConfirm,
  title,
  message,
  confirmText = 'Confirm',
  cancelText = 'Cancel',
  confirmColor = 'primary',
  loading = false,
  error = null,
  children = null,
  disabled = false,
}) => {
  return (
    <Dialog
      open={open}
      onClose={onClose}
      aria-labelledby="confirmation-dialog-title"
      className={GAME_CLASSES.dialog}
    >
      <DialogTitle id="confirmation-dialog-title">
        <GameText variant="h6" textVariant="glow">
          {title}
        </GameText>
      </DialogTitle>
      <DialogContent>
        {message && <DialogContentText className={GAME_CLASSES.text}>{message}</DialogContentText>}
        {children && children}
        {error && (
          <GameAlert className={GAME_CLASSES.marginBottom2}>
            <GameText variant="body2">{error}</GameText>
          </GameAlert>
        )}
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={loading} className={GAME_CLASSES.button}>
          {cancelText}
        </Button>
        <Button
          onClick={onConfirm}
          color={confirmColor}
          variant="contained"
          disabled={loading || disabled}
          className={GAME_CLASSES.button}
        >
          {loading ? 'Processing...' : confirmText}
        </Button>
      </DialogActions>
    </Dialog>
  );
};
