import { default as AccountCircleIcon } from '@mui/icons-material/AccountCircle';
import { default as DeleteIcon } from '@mui/icons-material/Delete';
import { default as EditIcon } from '@mui/icons-material/Edit';
import { default as SettingsIcon } from '@mui/icons-material/Settings';
import { default as PrivacyIcon } from '@mui/icons-material/PrivacyTip';
import { default as FitnessCenterIcon } from '@mui/icons-material/FitnessCenter';
import { default as SecurityIcon } from '@mui/icons-material/Security';
import {
  Avatar,
  Box,
  Button,
  Card,
  CardContent,
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
  Tabs,
  Tab,
  Paper,
  List,
  ListItem,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  useTheme,
  useMediaQuery,
} from '@mui/material';
import React, { useState } from 'react';
import { useNavigate } from 'react-router';

import { GdprComplianceSection } from './GdprComplianceSection';
import { WorkoutPreferencesSection } from './WorkoutPreferencesSection';
import { deleteAllPersonalData } from '../api/gdpr';
import type { User } from '../api/types';
import { useAuth } from '../contexts/AuthContext';

interface UserProfileProps {
  user: User;
}

interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
}

/**
 * Tab panel component for the tabbed interface.
 */
function TabPanel(props: TabPanelProps) {
  const { children, value, index, ...other } = props;

  return (
    <div
      role="tabpanel"
      hidden={value !== index}
      id={`profile-tabpanel-${index}`}
      aria-labelledby={`profile-tab-${index}`}
      {...other}
    >
      {value === index && <Box sx={{ p: 3 }}>{children}</Box>}
    </div>
  );
}

/**
 * User profile component with modern tabbed interface.
 *
 * Displays user information, roles, and provides account management options
 * in a modern tabbed layout with sidebar navigation for better organization.
 * Users can view their profile details, edit their profile, manage preferences,
 * and handle account settings.
 *
 * @param user The user data to display
 * @return User profile component with tabbed interface
 */
export const UserProfile: React.FC<UserProfileProps> = ({ user }) => {
  const { logout } = useAuth();
  const navigate = useNavigate();
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));
  
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState(0);

  const handleDeleteAccount = async () => {
    if (!user.keycloak_id) {
      return;
    }

    setIsDeleting(true);
    setError(null);

    try {
      await deleteAllPersonalData('DELETE_ALL_MY_DATA');
      setDeleteDialogOpen(false);
      // Logout after successful deletion
      await logout();
      navigate('/login');
    } catch (err: unknown) {
      const errorMessage =
        err &&
        typeof err === 'object' &&
        'response' in err &&
        err.response &&
        typeof err.response === 'object' &&
        'data' in err.response &&
        err.response.data &&
        typeof err.response.data === 'object' &&
        'message' in err.response.data
          ? String(err.response.data.message)
          : 'Failed to delete account';
      setError(errorMessage);
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

  const handleTabChange = (event: React.SyntheticEvent, newValue: number) => {
    setActiveTab(newValue);
  };

  const tabItems = [
    {
      label: 'Overview',
      icon: <AccountCircleIcon />,
      content: (
        <React.Fragment>
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
      ),
    },
    {
      label: 'Workout Preferences',
      icon: <FitnessCenterIcon />,
      content: <WorkoutPreferencesSection />,
    },
    {
      label: 'Privacy & Data',
      icon: <PrivacyIcon />,
      content: <GdprComplianceSection />,
    },
    {
      label: 'Account Security',
      icon: <SecurityIcon />,
      content: (
        <React.Fragment>
          <Typography variant="h5" gutterBottom>
            Account Security
          </Typography>
          <Typography variant="body1" color="text.secondary" paragraph>
            Manage your account security settings and access controls.
          </Typography>
          
          <Grid container spacing={3}>
            <Grid item xs={12} md={6}>
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
                    <Button variant="outlined" fullWidth>
                      Change Password
                    </Button>
                  </Box>
                </CardContent>
              </Card>
            </Grid>

            <Grid item xs={12} md={6}>
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
                    fullWidth
                  >
                    Deactivate Account
                  </Button>
                </CardContent>
              </Card>
            </Grid>
          </Grid>
        </React.Fragment>
      ),
    },
  ];

  return (
    <Container maxWidth="xl" sx={{ mt: 4, mb: 4 }}>
      <Typography variant="h4" component="h1" gutterBottom>
        User Profile
      </Typography>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      <Paper sx={{ display: 'flex', height: 'calc(100vh - 200px)', minHeight: 600 }}>
        {/* Sidebar Navigation */}
        <Box
          sx={{
            width: 280,
            borderRight: 1,
            borderColor: 'divider',
            display: { xs: 'none', md: 'block' },
          }}
        >
          <List>
            {tabItems.map((item, index) => (
              <ListItem key={index} disablePadding>
                <ListItemButton
                  selected={activeTab === index}
                  onClick={() => setActiveTab(index)}
                  sx={{
                    '&.Mui-selected': {
                      backgroundColor: 'primary.main',
                      color: 'primary.contrastText',
                      '&:hover': {
                        backgroundColor: 'primary.dark',
                      },
                    },
                  }}
                >
                  <ListItemIcon
                    sx={{
                      color: activeTab === index ? 'primary.contrastText' : 'inherit',
                    }}
                  >
                    {item.icon}
                  </ListItemIcon>
                  <ListItemText primary={item.label} />
                </ListItemButton>
              </ListItem>
            ))}
          </List>
        </Box>

        {/* Mobile Tabs */}
        <Box sx={{ display: { xs: 'block', md: 'none' }, width: '100%' }}>
          <Tabs
            value={activeTab}
            onChange={handleTabChange}
            variant="scrollable"
            scrollButtons="auto"
            sx={{ borderBottom: 1, borderColor: 'divider' }}
          >
            {tabItems.map((item, index) => (
              <Tab
                key={index}
                label={item.label}
                icon={item.icon}
                iconPosition="start"
                sx={{ minHeight: 64 }}
              />
            ))}
          </Tabs>
        </Box>

        {/* Content Area */}
        <Box sx={{ flexGrow: 1, overflow: 'auto' }}>
          {tabItems.map((item, index) => (
            <TabPanel key={index} value={activeTab} index={index}>
              {item.content}
            </TabPanel>
          ))}
        </Box>
      </Paper>

      {/* Delete Confirmation Dialog */}
      <Dialog
        open={deleteDialogOpen}
        onClose={() => setDeleteDialogOpen(false)}
        aria-labelledby="delete-dialog-title"
        aria-describedby="delete-dialog-description"
      >
        <DialogTitle id="delete-dialog-title">Deactivate Account</DialogTitle>
        <DialogContent>
          <DialogContentText id="delete-dialog-description">
            Are you sure you want to deactivate your account? This action cannot be undone. All your
            data, including workout preferences and exercise history, will be permanently deleted.
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDeleteDialogOpen(false)} disabled={isDeleting}>
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
