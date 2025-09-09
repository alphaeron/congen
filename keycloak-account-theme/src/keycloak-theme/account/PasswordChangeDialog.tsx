import React, { useState } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Button,
  Box,
  Alert,
  CircularProgress,
} from '@mui/material';
import { useKeycloakUser } from './api/useKeycloakUser';
import type { KcContext } from './KcContext';

interface PasswordChangeDialogProps {
  open: boolean;
  onClose: () => void;
  kcContext: KcContext;
}

export default function PasswordChangeDialog({ open, onClose, kcContext }: PasswordChangeDialogProps) {
  const { changePassword, loading, error } = useKeycloakUser(kcContext);
  const [formData, setFormData] = useState({
    currentPassword: '',
    newPassword: '',
    confirmPassword: '',
  });
  const [validationError, setValidationError] = useState<string | null>(null);

  const handleInputChange = (field: string) => (event: React.ChangeEvent<HTMLInputElement>) => {
    setFormData(prev => ({
      ...prev,
      [field]: event.target.value,
    }));
    // Clear validation error when user starts typing
    if (validationError) {
      setValidationError(null);
    }
  };

  const validateForm = (): boolean => {
    if (!formData.currentPassword) {
      setValidationError('Current password is required');
      return false;
    }
    if (!formData.newPassword) {
      setValidationError('New password is required');
      return false;
    }
    if (formData.newPassword.length < 8) {
      setValidationError('New password must be at least 8 characters long');
      return false;
    }
    if (formData.newPassword !== formData.confirmPassword) {
      setValidationError('New passwords do not match');
      return false;
    }
    if (formData.currentPassword === formData.newPassword) {
      setValidationError('New password must be different from current password');
      return false;
    }
    return true;
  };

  const handleSubmit = async () => {
    if (!validateForm()) {
      return;
    }

    const success = await changePassword({
      currentPassword: formData.currentPassword,
      newPassword: formData.newPassword,
    });
    
    if (success) {
      // Reset form and close dialog
      setFormData({
        currentPassword: '',
        newPassword: '',
        confirmPassword: '',
      });
      setValidationError(null);
      onClose();
    }
  };

  const handleClose = () => {
    if (!loading) {
      setFormData({
        currentPassword: '',
        newPassword: '',
        confirmPassword: '',
      });
      setValidationError(null);
      onClose();
    }
  };

  return (
    <Dialog open={open} onClose={handleClose} maxWidth="sm" fullWidth>
      <DialogTitle>Change Password</DialogTitle>
      <DialogContent>
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: 1 }}>
          {(error || validationError) && (
            <Alert severity="error">
              {error || validationError}
            </Alert>
          )}

          <TextField
            label="Current Password"
            type="password"
            value={formData.currentPassword}
            onChange={handleInputChange('currentPassword')}
            fullWidth
            required
            disabled={loading}
          />

          <TextField
            label="New Password"
            type="password"
            value={formData.newPassword}
            onChange={handleInputChange('newPassword')}
            fullWidth
            required
            disabled={loading}
            helperText="Password must be at least 8 characters long"
          />

          <TextField
            label="Confirm New Password"
            type="password"
            value={formData.confirmPassword}
            onChange={handleInputChange('confirmPassword')}
            fullWidth
            required
            disabled={loading}
          />
        </Box>
      </DialogContent>
      <DialogActions>
        <Button onClick={handleClose} disabled={loading}>
          Cancel
        </Button>
        <Button 
          onClick={handleSubmit} 
          variant="contained" 
          disabled={loading}
          startIcon={loading ? <CircularProgress size={20} /> : undefined}
        >
          {loading ? 'Changing...' : 'Change Password'}
        </Button>
      </DialogActions>
    </Dialog>
  );
}
