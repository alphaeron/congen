import { default as AccountCircleIcon } from '@mui/icons-material/AccountCircle';
import { default as EditIcon } from '@mui/icons-material/Edit';
import { Box, Card, CardContent, Grid, Typography, Avatar, Button } from '@mui/material';
import React from 'react';

import type { User } from '../api/types';

interface ProfileOverviewProps {
  user: User;
  onEditProfile?: () => void;
}

/**
 * Profile overview component displaying user information.
 *
 * Shows user avatar, name, member since date, roles, and edit button.
 *
 * @param user The user data to display
 * @param onEditProfile Optional callback function when edit profile is clicked
 * @return Profile overview component
 */
export const ProfileOverview: React.FC<ProfileOverviewProps> = ({ user, onEditProfile }) => {
  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
  };

  const handleEditProfile = () => {
    if (onEditProfile) {
      onEditProfile();
    } else {
      // Default implementation - could be expanded later
    }
  };

  return (
    <React.Fragment>
      <Typography variant="h5" gutterBottom>
        Profile Overview
      </Typography>

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
                  {user.roles && user.roles.length > 0 && (
                    <Box sx={{ mt: 1 }}>
                      <Typography variant="body2" color="text.secondary">
                        Roles: {user.roles.join(', ')}
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
