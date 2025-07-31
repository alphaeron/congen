import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Avatar,
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  Container,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  Divider,
  Grid,
  Typography,
  Alert,
} from '@mui/material';
import { default as AccountCircleIcon } from '@mui/icons-material/AccountCircle';
import { default as DeleteIcon } from '@mui/icons-material/Delete';
import { default as EditIcon } from '@mui/icons-material/Edit';
import { User } from '../api/types';
import { deleteUser } from '../api/user';
import { useAuth } from '../contexts/AuthContext';

interface UserProfileProps {
  user: User;
}

/**
 * User profile component.
 *
 * Displays user information, roles, groups, and provides account management options.
 * Users can view their profile details, edit their profile, and deactivate their account.
 *
 * @param user The user data to display
 * @return User profile component
 */
export const UserProfile: React.FC<UserProfileProps> = ({ user }) => {
  const { logout } = useAuth();
  const navigate = useNavigate();
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleDeleteAccount = async () => {
    if (!user.id) {
      return;
    }

    setIsDeleting(true);
    setError(null);

    try {
      await deleteUser(user.id);
      setDeleteDialogOpen(false);
      // Logout after successful deletion
      await logout();
      navigate('/');
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to delete account');
    } finally {
      setIsDeleting(false);
    }
  };

  const handleEditProfile = () => {
    navigate('/profile/edit');
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
  };

  return (
    <Container maxWidth="md" sx={{ mt: 4, mb: 4 }}>
      <Typography variant="h4" component="h1" gutterBottom>
        User Profile
      </Typography>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      <Grid container spacing={3}>
        {/* Profile Header */}
        <Grid item xs={12}>
          <Card>
            <CardContent>
              <Box display="flex" alignItems="center" gap={2}>
                <Avatar sx={{ width: 80, height: 80 }}>
                  <AccountCircleIcon sx={{ fontSize: 60 }} />
                </Avatar>
                <Box flex={1}>
                  <Typography variant="h5" component="h2" gutterBottom>
                    {user.name}
                  </Typography>
                  <Typography variant="body1" color="text.secondary">
                    Member since {user.created_at ? formatDate(user.created_at) : 'N/A'}
                  </Typography>
                </Box>
                <Button
                  variant="outlined"
                  startIcon={<EditIcon />}
                  onClick={handleEditProfile}
                >
                  Edit Profile
                </Button>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        {/* Personal Information */}
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Personal Information
              </Typography>
              <Divider sx={{ mb: 2 }} />
              
              <Box sx={{ mb: 2 }}>
                <Typography variant="body2" color="text.secondary">
                  Age
                </Typography>
                <Typography variant="body1">
                  {user.age} years old
                </Typography>
              </Box>

              <Box sx={{ mb: 2 }}>
                <Typography variant="body2" color="text.secondary">
                  Height
                </Typography>
                <Typography variant="body1">
                  {user.height} cm
                </Typography>
              </Box>

              <Box>
                <Typography variant="body2" color="text.secondary">
                  Weight
                </Typography>
                <Typography variant="body1">
                  {user.weight} kg
                </Typography>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        {/* Roles and Groups */}
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Roles & Groups
              </Typography>
              <Divider sx={{ mb: 2 }} />

              {user.roles && user.roles.length > 0 && (
                <Box sx={{ mb: 3 }}>
                  <Typography variant="body2" color="text.secondary" gutterBottom>
                    Roles
                  </Typography>
                  <Box display="flex" flexWrap="wrap" gap={1}>
                    {user.roles.map((role, index) => (
                      <Chip
                        key={index}
                        label={role}
                        color="primary"
                        variant="outlined"
                        size="small"
                      />
                    ))}
                  </Box>
                </Box>
              )}

              {user.groups && user.groups.length > 0 && (
                <Box>
                  <Typography variant="body2" color="text.secondary" gutterBottom>
                    Groups
                  </Typography>
                  <Box display="flex" flexWrap="wrap" gap={1}>
                    {user.groups.map((group, index) => (
                      <Chip
                        key={index}
                        label={group}
                        color="secondary"
                        variant="outlined"
                        size="small"
                      />
                    ))}
                  </Box>
                </Box>
              )}

              {(!user.roles || user.roles.length === 0) && 
               (!user.groups || user.groups.length === 0) && (
                <Typography variant="body2" color="text.secondary">
                  No roles or groups assigned
                </Typography>
              )}
            </CardContent>
          </Card>
        </Grid>

        {/* Account Management */}
        <Grid item xs={12}>
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
                onClick={() => setDeleteDialogOpen(true)}
              >
                Deactivate Account
              </Button>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Delete Confirmation Dialog */}
      <Dialog
        open={deleteDialogOpen}
        onClose={() => setDeleteDialogOpen(false)}
        aria-labelledby="delete-dialog-title"
        aria-describedby="delete-dialog-description"
      >
        <DialogTitle id="delete-dialog-title">
          Deactivate Account
        </DialogTitle>
        <DialogContent>
          <DialogContentText id="delete-dialog-description">
            Are you sure you want to deactivate your account? This action cannot be undone.
            All your data, including workout preferences and exercise history, will be permanently deleted.
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button 
            onClick={() => setDeleteDialogOpen(false)}
            disabled={isDeleting}
          >
            Cancel
          </Button>
          <Button
            onClick={handleDeleteAccount}
            color="error"
            variant="contained"
            disabled={isDeleting}
          >
            {isDeleting ? 'Deactivating...' : 'Deactivate Account'}
          </Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};
