import { default as DeleteIcon } from '@mui/icons-material/Delete';
import {
  Box,
  Card,
  CardContent,
  Grid,
  Typography,
  Divider,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  DialogContentText,
  Alert,
} from '@mui/material';
import React, { useState } from 'react';

import { deleteAllPersonalData } from '../api/gdpr';
import type { User } from '../api/types';
import { KEYCLOAK_URL } from '../globals';

interface AccountSecurityProps {
  user: User;
  onAccountDeleted?: () => void;
}

/**
 * Account security component for managing account security settings.
 *
 * Provides options for changing password and account deletion.
 *
 * @param user The user data
 * @param onAccountDeleted Callback function when account is successfully deleted
 * @return Account security component
 */
export const AccountSecurity: React.FC<AccountSecurityProps> = ({ user, onAccountDeleted }) => {
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [deleteError, setDeleteError] = useState<string | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);

  const handleDeleteAccount = () => {
    setDeleteDialogOpen(true);
    setDeleteError(null);
  };

  const handleConfirmDelete = async () => {
    try {
      setIsDeleting(true);
      setDeleteError(null);
      await deleteAllPersonalData(user.keycloak_id);
      setDeleteDialogOpen(false);
      onAccountDeleted?.();
    } catch {
      setDeleteError('Failed to delete account. Please try again.');
    } finally {
      setIsDeleting(false);
    }
  };

  /**
   * Redirects the user to Keycloak's account management interface to change their password.
   * After completing the password change, the user will be redirected back to Congen.
   */
  const handleChangePassword = () => {
    // Construct the Keycloak account management URL with redirect back to Congen
    const redirectUri = `${window.location.origin}/password-change-redirect`;
    const accountUrl = `${KEYCLOAK_URL}/realms/congen/account/#/security/credentials?redirect_uri=${encodeURIComponent(redirectUri)}`;
    
    // Store the current location to redirect back after password change
    sessionStorage.setItem('congen_redirect_after_password_change', window.location.pathname);
    
    // Redirect to Keycloak account management
    window.location.href = accountUrl;
  };

  return (
    <React.Fragment>
      <Typography variant="h5" gutterBottom>
        Account Security
      </Typography>

      <Typography variant="body1" color="text.secondary" paragraph>
        Manage your account security settings and access controls.
      </Typography>

      <Grid container spacing={3}>
        <Grid size={{ xs: 12, md: 6 }}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Security Settings
              </Typography>
              <Divider sx={{ mb: 2 }} />
              <Typography variant="body2" color="text.secondary" paragraph>
                Configure your account security preferences and access controls.
              </Typography>
              <Box display="flex" flexDirection="column" gap={1}>
                <Button 
                  variant="outlined" 
                  fullWidth 
                  onClick={handleChangePassword}
                >
                  Change Password
                </Button>
                <Typography variant="caption" color="text.secondary" sx={{ textAlign: 'center' }}>
                  You will be redirected to Keycloak to change your password securely
                </Typography>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid size={{ xs: 12, md: 6 }}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom color="error">
                Danger Zone
              </Typography>
              <Divider sx={{ mb: 2 }} />

              <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                Once you delete your account, there is no going back. Please be certain.
              </Typography>

              <Button
                variant="outlined"
                color="error"
                startIcon={<DeleteIcon />}
                onClick={handleDeleteAccount}
                fullWidth
              >
                Deactivate Account
              </Button>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Delete Account Dialog */}
      <Dialog
        open={deleteDialogOpen}
        onClose={() => setDeleteDialogOpen(false)}
        aria-labelledby="delete-dialog-title"
        aria-describedby="delete-dialog-description"
      >
        <DialogTitle id="delete-dialog-title">Delete Account</DialogTitle>
        <DialogContent>
          <DialogContentText id="delete-dialog-description">
            Are you sure you want to delete your account? This action cannot be undone. All your
            data will be permanently removed.
          </DialogContentText>
          {deleteError && (
            <Alert severity="error" sx={{ mt: 2 }}>
              {deleteError}
            </Alert>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDeleteDialogOpen(false)} disabled={isDeleting}>
            Cancel
          </Button>
          <Button
            onClick={handleConfirmDelete}
            color="error"
            variant="contained"
            disabled={isDeleting}
          >
            {isDeleting ? 'Deleting...' : 'Delete Account'}
          </Button>
        </DialogActions>
      </Dialog>
    </React.Fragment>
  );
};
