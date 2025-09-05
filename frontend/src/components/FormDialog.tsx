import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Typography,
} from '@mui/material';
import { useForm } from '@tanstack/react-form';
import React from 'react';

interface FormDialogProps<TFormData = any> {
  open: boolean;
  onClose: () => void;
  onSubmit: (data: TFormData) => void | Promise<void>;
  title: string;
  description?: string;
  submitText?: string;
  cancelText?: string;
  submitColor?: 'primary' | 'secondary' | 'error' | 'warning' | 'info' | 'success';
  loading?: boolean;
  disabled?: boolean;
  children: React.ReactNode | ((form: any) => React.ReactNode);
  // TanStack Form integration
  defaultValues?: Partial<TFormData>;
  validate?: (values: TFormData) => Record<string, string> | undefined;
  useTanStackForm?: boolean;
}

/**
 * Reusable form dialog component for create/edit operations.
 *
 * Provides a consistent interface for form dialogs with form content,
 * validation, and action buttons. Supports both traditional controlled
 * components and TanStack Form integration.
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
 * @param children Form content to render or render function for TanStack Form
 * @param defaultValues Default values for TanStack Form
 * @param validate Optional form-level validation function for TanStack Form
 * @param useTanStackForm Whether to use TanStack Form integration
 * @return Form dialog component
 */
export const FormDialog = <TFormData extends Record<string, any>>({
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
  defaultValues,
  validate,
  useTanStackForm = false,
}: FormDialogProps<TFormData>): React.ReactElement => {
  const form = useTanStackForm ? useForm({
    defaultValues: defaultValues as TFormData,
    validators: {
      onChange: validate ? ({ value }: { value: TFormData }) => validate(value) : undefined,
    },
    onSubmit: async ({ value }: { value: TFormData }) => {
      await onSubmit(value);
    },
  }) : null;

  const handleSubmit = (e: React.FormEvent) => {
    if (useTanStackForm && form) {
      e.preventDefault();
      e.stopPropagation();
      form.handleSubmit();
    }
  };

  const isFormValid = form ? form.state.isValid : true;
  const isSubmitting = form ? form.state.isSubmitting : false;

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      {useTanStackForm ? (
        <form onSubmit={handleSubmit}>
          <DialogTitle>{title}</DialogTitle>
          <DialogContent>
            {description && (
              <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                {description}
              </Typography>
            )}
            {typeof children === 'function' ? (children as (form: any) => React.ReactNode)(form!) : children}
          </DialogContent>
          <DialogActions>
            <Button onClick={onClose} disabled={loading || isSubmitting}>
              {cancelText}
            </Button>
            <Button
              type="submit"
              variant="contained"
              color={submitColor}
              disabled={loading || disabled || !isFormValid || isSubmitting}
            >
              {loading || isSubmitting ? 'Processing...' : submitText}
            </Button>
          </DialogActions>
        </form>
      ) : (
        <React.Fragment>
          <DialogTitle>{title}</DialogTitle>
          <DialogContent>
            {description && (
              <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                {description}
              </Typography>
            )}
            {typeof children === 'function' ? (children as (form: any) => React.ReactNode)(form!) : children}
          </DialogContent>
          <DialogActions>
            <Button onClick={onClose} disabled={loading}>
              {cancelText}
            </Button>
            <Button
              onClick={() => onSubmit({} as TFormData)}
              variant="contained"
              color={submitColor}
              disabled={loading || disabled}
            >
              {loading ? 'Processing...' : submitText}
            </Button>
          </DialogActions>
        </React.Fragment>
      )}
    </Dialog>
  );
};
