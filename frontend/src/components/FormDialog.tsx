import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Typography,
} from '@mui/material';
import React from 'react';

interface FormDialogProps {
  open: boolean;
  onClose: () => void;
  onSubmit: () => void;
  title: string;
  description?: string;
  submitText?: string;
  cancelText?: string;
  submitColor?: 'primary' | 'secondary' | 'error' | 'warning' | 'info' | 'success';
  loading?: boolean;
  disabled?: boolean;
  children: React.ReactNode;
}

/**
 * Reusable form dialog component for create/edit operations.
 *
 * Provides a consistent interface for form dialogs with form content,
 * validation, and action buttons.
 *
 * @param open Whether the dialog is open
 * @param onClose Function to call when dialog should be closed
 * @param onSubmit Function to call when form is submitted
 * @param title Dialog title
 * @param description Optional description text to display
 * @param submitText Text for the submit button (default: "Submit")
 * @param cancelText Text for the cancel button (default: "Cancel")
 * @param submitColor Color of the submit button (default: "primary")
 * @param loading Whether the form submission is in progress
 * @param disabled Whether the submit button should be disabled
 * @param children Form content to render
 * @return Form dialog component
 */
export const FormDialog: React.FC<FormDialogProps> = ({
  open,
  onClose,
  onSubmit,
  title,
  description,
  submitText = 'Submit',
  cancelText = 'Cancel',
  submitColor = 'primary',
  loading = false,
  disabled = false,
  children,
}) => {
  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>{title}</DialogTitle>
      <DialogContent>
        {description && (
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            {description}
          </Typography>
        )}
        {children}
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={loading}>
          {cancelText}
        </Button>
        <Button
          onClick={onSubmit}
          variant="contained"
          color={submitColor}
          disabled={loading || disabled}
        >
          {loading ? 'Processing...' : submitText}
        </Button>
      </DialogActions>
    </Dialog>
  );
};
