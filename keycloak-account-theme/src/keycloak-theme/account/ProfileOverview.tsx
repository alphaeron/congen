import { default as AccountCircleIcon } from '@mui/icons-material/AccountCircle';
import { default as EditIcon } from '@mui/icons-material/Edit';
import { Box, Card, CardContent, Grid, Typography, Avatar, Button } from '@mui/material';
import React from 'react';

import type { KcContext } from './KcContext';

interface ProfileOverviewProps {
  kcContext: KcContext;
  user?: any;
  onEditProfile?: () => void;
}

/**
 * Profile overview component displaying user information.
 *
 * Shows user avatar, name, member since date, roles, and edit button.
 * This component is now part of the Keycloak theme.
 *
 * @param kcContext The Keycloak context
 * @param user The user data to display
 * @param onEditProfile Callback when edit profile is clicked
 * @return Profile overview component
 */
export const ProfileOverview: React.FC<ProfileOverviewProps> = ({ kcContext, user, onEditProfile }) => {
  const handleEditProfile = () => {
    if (onEditProfile) {
      onEditProfile();
    }
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
    </React.Fragment>
  );
};
