import React, { useState, useEffect } from 'react';
// Import only the specific components we need to reduce bundle size
import Container from '@mui/material/Container';
import Typography from '@mui/material/Typography';
import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
import Avatar from '@mui/material/Avatar';
import Button from '@mui/material/Button';
import Box from '@mui/material/Box';
import { LoadingSpinner } from '../../components/LoadingSpinner';
import { useForm } from '@tanstack/react-form';
import { useSnackbar } from 'notistack';
import type { KcContext } from './KcContext';
import { UserProfileDrawer } from './UserProfileDrawer';
import { CongenAppBar } from './CongenAppBar';
import { useAuth } from './AuthContext';
import { FormField } from '../../components/FormField';
import PasswordChangeDialog from './PasswordChangeDialog';
import { createApiClient } from './api/client';

// Global type declaration for OIDC user
declare global {
  interface Window {
    oidcUser?: {
      profile?: {
        sub?: string;
        email?: string;
        given_name?: string;
        family_name?: string;
        [key: string]: any;
      };
    };
  }
}

interface AccountProps {
  kcContext?: KcContext;
  i18n?: unknown;
}

export default function Account({
  kcContext,
  i18n: _i18n,
}: AccountProps) {
  
  const { enqueueSnackbar } = useSnackbar();
  
  // Use the authentication context
  const { isAuthenticated, isLoading: authLoading, login } = useAuth();
  
  const [currentPage, setCurrentPage] = useState('personal-info');
  const [showPasswordDialog, setShowPasswordDialog] = useState(false);
  const [user, setUser] = useState<any>(null);
  const [userLoading, setUserLoading] = useState(false);
  const [userFetchAttempted, setUserFetchAttempted] = useState(false);

  // Guard clause to handle undefined kcContext
  if (!kcContext) {
    return (
      <Box sx={{ p: 3, textAlign: 'center' }}>
        <Typography variant="h6" color="error">
          Error: No Keycloak context available
        </Typography>
        <Typography variant="body2" sx={{ mt: 1 }}>
          Please refresh the page or contact support if the issue persists.
        </Typography>
      </Box>
    );
  }

  // Handle authentication state
  if (authLoading) {
    return (
      <Box 
        sx={{ 
          minHeight: '100vh',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          bgcolor: 'background.default'
        }}
      >
        <LoadingSpinner size={60} />
      </Box>
    );
  }

  // Fetch user data when authenticated
  useEffect(() => {
    const fetchUserData = async () => {
      if (isAuthenticated && !userFetchAttempted && !userLoading) {
        setUserLoading(true);
        setUserFetchAttempted(true);
        try {
          // Create API client from Keycloak context
          const apiClient = createApiClient(kcContext);
          if (apiClient) {
            // Try to get user profile from Keycloak
            const result = await apiClient.getUserProfile();
            if (result.success && result.data) {
              setUser(result.data);
            } else {
              enqueueSnackbar('Failed to load user information', { variant: 'error' });
            }
          } else {
            enqueueSnackbar('Failed to create API client', { variant: 'error' });
          }
        } catch (error) {
          enqueueSnackbar('Failed to load user information', { variant: 'error' });
        } finally {
          setUserLoading(false);
        }
      }
    };

    fetchUserData();
  }, [isAuthenticated, userFetchAttempted, userLoading, kcContext, enqueueSnackbar]);

  // Reset user fetch state when authentication changes
  useEffect(() => {
    if (!isAuthenticated) {
      setUser(null);
      setUserFetchAttempted(false);
      setUserLoading(false);
    }
  }, [isAuthenticated]);

  // Handle unauthenticated state
  if (!isAuthenticated) {
    return (
      <Box 
        sx={{ 
          minHeight: '100vh',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          bgcolor: 'background.default'
        }}
      >
        <Box sx={{ textAlign: 'center', p: 3 }}>
          <Typography variant="h5" gutterBottom>
            Authentication Required
          </Typography>
          <Typography variant="body1" sx={{ mb: 3 }}>
            Please log in to access your account.
          </Typography>
          <Button 
            variant="contained" 
            onClick={login}
            size="large"
          >
            Log In
          </Button>
        </Box>
      </Box>
    );
  }

  // We're authenticated, show the account page

  // TanStack Form for profile editing
  const form = useForm({
    defaultValues: {
      firstName: '',
      lastName: '',
    },
    onSubmit: async ({ value }) => {
      try {
        // Create API client from Keycloak context
        const apiClient = createApiClient(kcContext);
        if (!apiClient) {
          enqueueSnackbar('No authentication token available', { variant: 'error' });
          return;
        }

        // Get user information from the OIDC context or userinfo endpoint
        let userInfo = null;
        try {
          // Try to get user info from the OIDC context first
          if (window.oidcUser) {
            userInfo = window.oidcUser.profile;
          } else {
            // Fallback: get user info from the userinfo endpoint
            const userInfoResponse = await fetch(`${kcContext.serverBaseUrl}/realms/${kcContext.realm.name}/protocol/openid-connect/userinfo`, {
              headers: {
                'Authorization': `Bearer ${apiClient.getAccessToken()}`,
                'Accept': 'application/json',
              },
            });
            
            if (userInfoResponse.ok) {
              userInfo = await userInfoResponse.json();
            } else {
              // Failed to get user info from userinfo endpoint
            }
          }
        } catch (error) {
          // Error getting user info - will use fallback data
        }

        if (!userInfo) {
          enqueueSnackbar('Unable to get user information', { variant: 'error' });
          return;
        }

        // Update user profile using the API client
        const result = await apiClient.updateUserProfile({
          firstName: value.firstName,
          lastName: value.lastName,
        });

        if (result.success) {
          enqueueSnackbar('Profile updated successfully!', { variant: 'success' });
          
          // Profile updated successfully
          
          // Update the Keycloak context user data
          if (kcContext?.user) {
            Object.assign(kcContext.user, {
              firstName: value.firstName,
              lastName: value.lastName,
            });
          }
        } else {
          enqueueSnackbar('Failed to update user profile', { variant: 'error' });
        }
      } catch (error) {
        enqueueSnackbar('Failed to update user profile', { variant: 'error' });
      }
    },
  });

  // Update form data when user data loads
  useEffect(() => {
    if (user) {
      form.setFieldValue('firstName', user.given_name || user.firstName || '');
      form.setFieldValue('lastName', user.family_name || user.lastName || '');
    }
  }, [user, form]);

  const handlePageChange = (page: string) => {
    setCurrentPage(page);
  };

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: 'background.default' }}>
        {/* Congen App Bar */}
        <CongenAppBar kcContext={kcContext} user={user} />
      
      <Box sx={{ display: 'flex', height: 'calc(100vh - 64px)' }}>
        {/* User Profile Drawer */}
        <UserProfileDrawer 
          kcContext={kcContext} 
          currentSection={currentPage} 
          onSectionChange={handlePageChange} 
        />
        
        {/* Main Content */}
        <Box
          component="main"
          sx={{
            flexGrow: 1,
            height: '100%',
            overflow: 'auto',
            maxWidth: 'calc(100% - 240px)',
          }}
        >
        <Container maxWidth="xl" sx={{ py: 2 }}>
          {currentPage === 'personal-info' && (
            <form
              onSubmit={(e) => {
                e.preventDefault();
                e.stopPropagation();
                form.handleSubmit();
              }}
            >
              {/* Member Summary Section */}
              <Card sx={{ maxWidth: 600, mb: 3 }}>
                <CardContent sx={{ p: 4 }}>
                  <Typography variant="h6" gutterBottom sx={{ mb: 3, fontWeight: 600 }}>
                    Manage Profile
                  </Typography>
                  
                  {authLoading ? (
                    <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
                      <LoadingSpinner size={20} />
                    </Box>
                  ) : (
                    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                        <Avatar sx={{ bgcolor: 'primary.main' }}>
                          {user?.email?.[0] || user?.preferred_username?.[0] || user?.username?.[0] || 'U'}
                        </Avatar>
                        <Box>
                          <Typography variant="body2" color="text.secondary">
                            {user?.email || 'No email available'}
                          </Typography>
                        </Box>
                      </Box>
                    </Box>
                  )}
                </CardContent>
              </Card>

              {/* Profile Editing Form */}
              <Card sx={{ maxWidth: 600, mb: 3 }}>
                <CardContent sx={{ p: 4 }}>
                  <Typography variant="h6" gutterBottom sx={{ mb: 3, fontWeight: 600 }}>
                    Edit Profile
                  </Typography>
                  
                  {authLoading ? (
                    <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
                      <LoadingSpinner size={20} />
                    </Box>
                  ) : (
                    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
                      <FormField
                        name="firstName"
                        form={form}
                        type="text"
                        label="First name"
                        required
                        fullWidth
                      />
                      
                      <FormField
                        name="lastName"
                        form={form}
                        type="text"
                        label="Last name"
                        required
                        fullWidth
                      />
                    </Box>
                  )}

                  <Box sx={{ display: 'flex', gap: 2, mt: 4 }}>
                    <Button 
                      type="submit"
                      variant="contained" 
                      sx={{ borderRadius: '12px', px: 3 }}
                      disabled={authLoading || !form.state.isValid || form.state.isSubmitting}
                    >
                      {form.state.isSubmitting ? <LoadingSpinner size={20} /> : 'Save Changes'}
                    </Button>
                    <Button 
                      type="button"
                      variant="outlined" 
                      sx={{ borderRadius: '12px', px: 3 }}
                      onClick={() => {
                        if (user) {
                          form.setFieldValue('firstName', user.given_name || user.firstName || '');
                          form.setFieldValue('lastName', user.family_name || user.lastName || '');
                        }
                      }}
                      disabled={authLoading}
                    >
                      Reset
                    </Button>
                  </Box>
                </CardContent>
              </Card>

              {/* Password Change Section */}
              <Card sx={{ maxWidth: 600 }}>
                <CardContent sx={{ p: 4 }}>
                  <Typography variant="h6" gutterBottom sx={{ mb: 3, fontWeight: 600 }}>
                    Security
                  </Typography>
                  
                  <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
                    Change your password to keep your account secure
                  </Typography>
                  
                  <Button 
                    variant="outlined" 
                    sx={{ borderRadius: '12px', px: 3 }}
                    onClick={() => setShowPasswordDialog(true)}
                    disabled={authLoading}
                  >
                    Change Password
                  </Button>
                </CardContent>
              </Card>
            </form>
          )}
        </Container>
        </Box>
      </Box>

      {/* Password Change Dialog */}
      <PasswordChangeDialog
        open={showPasswordDialog}
        onClose={() => setShowPasswordDialog(false)}
        kcContext={kcContext}
      />
      </Box>
  );
}
