import React, { useState } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Button,
  Box,
} from '@mui/material';
import { LoadingSpinner } from '../../components/LoadingSpinner';
import { useSnackbar } from 'notistack';
import { createApiClient } from './api/client';
import type { KcContext } from './KcContext';

interface PasswordChangeDialogProps {
  open: boolean;
  onClose: () => void;
  kcContext: KcContext;
}

export default function PasswordChangeDialog({ open, onClose, kcContext }: PasswordChangeDialogProps) {
  const { enqueueSnackbar } = useSnackbar();
  const [formData, setFormData] = useState({
    currentPassword: '',
    newPassword: '',
    confirmPassword: '',
  });
  const [loading, setLoading] = useState(false);

  const handleInputChange = (field: string) => (event: React.ChangeEvent<HTMLInputElement>) => {
    setFormData(prev => ({
      ...prev,
      [field]: event.target.value,
    }));
  };

  const validateForm = (): boolean => {
    if (!formData.currentPassword) {
      enqueueSnackbar('Current password is required', { variant: 'error' });
      return false;
    }
    if (!formData.newPassword) {
      enqueueSnackbar('New password is required', { variant: 'error' });
      return false;
    }
    if (formData.newPassword.length < 8) {
      enqueueSnackbar('New password must be at least 8 characters long', { variant: 'error' });
      return false;
    }
    if (formData.newPassword !== formData.confirmPassword) {
      enqueueSnackbar('New passwords do not match', { variant: 'error' });
      return false;
    }
    if (formData.currentPassword === formData.newPassword) {
      enqueueSnackbar('New password must be different from current password', { variant: 'error' });
      return false;
    }
    return true;
  };

  const handleSubmit = async () => {
    if (!validateForm()) {
      return;
    }

    setLoading(true);
    try {
      // Create API client from Keycloak context
      const apiClient = createApiClient(kcContext);
      if (!apiClient) {
        enqueueSnackbar('No authentication token available', { variant: 'error' });
        return;
      }

      // Change password using the API client
      const result = await apiClient.changePassword({
        currentPassword: formData.currentPassword,
        newPassword: formData.newPassword,
        confirmPassword: formData.confirmPassword,
      });

      if (result.success) {
        enqueueSnackbar('Password changed successfully!', { variant: 'success' });
        
        // Reset form and close dialog
        setFormData({
          currentPassword: '',
          newPassword: '',
          confirmPassword: '',
        });
        onClose();
      } else {
        enqueueSnackbar('Failed to change password', { variant: 'error' });
      }
    } catch (error) {
      enqueueSnackbar('Failed to change password', { variant: 'error' });
    } finally {
      setLoading(false);
    }
  };

  const handleClose = () => {
    if (!loading) {
      setFormData({
        currentPassword: '',
        newPassword: '',
        confirmPassword: '',
      });
      onClose();
    }
  };

  return (
    <Dialog open={open} onClose={handleClose} maxWidth="sm" fullWidth>
      <DialogTitle>Change Password</DialogTitle>
      <DialogContent>
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: 1 }}>
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
          startIcon={loading ? <LoadingSpinner size={20} /> : undefined}
        >
          {loading ? 'Changing...' : 'Change Password'}
        </Button>
      </DialogActions>
    </Dialog>
  );
}
