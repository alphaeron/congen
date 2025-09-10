import { default as AccountCircleIcon } from '@mui/icons-material/AccountCircle';
import { default as EditIcon } from '@mui/icons-material/Edit';
import { Box, Card, CardContent, Grid, Typography, Avatar, Button } from '@mui/material';
import React, { useState } from 'react';

import { ConfirmationDialog } from './ConfirmationDialog';
import type { KcContext } from './KcContext';

interface ProfileOverviewProps {
  kcContext: KcContext;
  user?: any;
}

/**
 * Profile overview component displaying user information.
 *
 * Shows user avatar, name, member since date, roles, and edit button.
 * This component is now part of the Keycloak theme.
 *
 * @param kcContext The Keycloak context
 * @param user The user data to display
 * @return Profile overview component
 */
export const ProfileOverview: React.FC<ProfileOverviewProps> = ({ kcContext, user }) => {
  const [editDialogOpen, setEditDialogOpen] = useState(false);

  const handleEditProfile = () => {
    setEditDialogOpen(true);
  };

  const handleConfirmEditProfile = () => {
    // Construct the Keycloak account management URL with redirect back to Congen
    const redirectUri = `${window.location.origin}/profile-edit-redirect`;
    const accountUrl = `${kcContext.authUrl?.realm}/account/#/personal-info?redirect_uri=${encodeURIComponent(redirectUri)}`;

    // Store the current location to redirect back after profile edit
    sessionStorage.setItem('congen_redirect_after_profile_edit', window.location.pathname);

    // Close dialog and redirect to Keycloak account management
    setEditDialogOpen(false);
    window.location.href = accountUrl;
  };

  const handleCancelEditProfile = () => {
    // Just close the dialog, don't redirect
    setEditDialogOpen(false);
  };

  // Format date helper function
  const formatDate = (dateString: string) => {
    try {
      return new Date(dateString).toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
      });
    } catch {
      return 'Unknown';
    }
  };

  // Get user data from Keycloak context
  const displayUser = user || kcContext.user;
  const userName = displayUser?.firstName && displayUser?.lastName 
    ? `${displayUser.firstName} ${displayUser.lastName}`
    : displayUser?.username || 'User';

  return (
    <React.Fragment>
      <Typography variant="h5" gutterBottom>
        Profile Overview
      </Typography>

      <Grid container spacing={3}>
        {/* Profile Header */}
        <Grid size={{ xs: 12 }}>
          <Card>
            <CardContent>
              <Box display="flex" alignItems="center" gap={2}>
                <Avatar sx={{ width: 80, height: 80 }}>
                  <AccountCircleIcon sx={{ fontSize: 60 }} />
                </Avatar>
                <Box flex={1}>
                  <Typography variant="h5" component="h2" gutterBottom>
                    {userName}
                  </Typography>
                  <Typography variant="body1" color="text.secondary">
                    Member since {displayUser?.createdTimestamp ? formatDate(new Date(displayUser.createdTimestamp).toISOString()) : 'Unknown'}
                  </Typography>
                  {displayUser?.email && (
                    <Box sx={{ mt: 1 }}>
                      <Typography variant="body2" color="text.secondary">
                        Email: {displayUser.email}
                      </Typography>
                    </Box>
                  )}
                </Box>
                <Button variant="outlined" startIcon={<EditIcon />} onClick={handleEditProfile}>
                  Edit Profile
                </Button>
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Edit Profile Confirmation Dialog */}
      <ConfirmationDialog
        open={editDialogOpen}
        onClose={handleCancelEditProfile}
        onConfirm={handleConfirmEditProfile}
        title="Edit Profile"
        message="You will be redirected to your account settings. After making changes, you will be brought back to this page."
        confirmText="Continue"
        cancelText="Cancel"
        confirmColor="primary"
      />
    </React.Fragment>
  );
};
