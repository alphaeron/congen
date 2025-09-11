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
import { useAuth as useOidcAuth } from 'react-oidc-context';
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
        [key: string]: unknown;
      };
    };
  }
}

interface AccountProps {
  kcContext?: KcContext;
  i18n?: unknown;
}

export default function Account({ kcContext, i18n: _i18n }: AccountProps) {
  const { enqueueSnackbar } = useSnackbar();

  // Use the authentication context
  const { isAuthenticated, isLoading: authLoading, login } = useAuth();

  // Get OIDC auth for access token
  const oidcAuth = useOidcAuth();

  const [currentPage, setCurrentPage] = useState('personal-info');
  const [showPasswordDialog, setShowPasswordDialog] = useState(false);
  const [user, setUser] = useState<Record<string, unknown> | null>(null);
  const [userLoading, setUserLoading] = useState(false);
  const [userFetchAttempted, setUserFetchAttempted] = useState(false);

  // TanStack Form for profile editing - moved to top level
  const form = useForm({
    defaultValues: {
      firstName: '',
      lastName: '',
    },
    onSubmit: async ({ value }) => {
      if (!kcContext) return;

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
            const userInfoResponse = await fetch(
              `${kcContext.serverBaseUrl}/realms/${kcContext.realm.name}/protocol/openid-connect/userinfo`,
              {
                headers: {
                  Authorization: `Bearer ${apiClient.getAccessToken()}`,
                  Accept: 'application/json',
                },
              }
            );

            if (userInfoResponse.ok) {
              userInfo = await userInfoResponse.json();
            }
          }
        } catch {
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
      } catch {
        enqueueSnackbar('Failed to update user profile', { variant: 'error' });
      }
    },
  });

  // Fetch user data using OIDC token and Keycloak API
  useEffect(() => {
    if (!kcContext) return;

    const fetchUserData = async () => {
      if (isAuthenticated && !userFetchAttempted && !userLoading) {
        setUserLoading(true);
        setUserFetchAttempted(true);

        try {
          // Check if we have an OIDC access token
          if (!oidcAuth.user?.access_token) {
            throw new Error('No OIDC access token available');
          }

          // Make API call to Keycloak userinfo endpoint
          const response = await fetch(`${kcContext.serverBaseUrl}/realms/congen/protocol/openid-connect/userinfo`, {
            headers: {
              'Authorization': `Bearer ${oidcAuth.user.access_token}`,
              'Content-Type': 'application/json',
            },
          });

          if (!response.ok) {
            throw new Error(`Failed to fetch user info: ${response.status} ${response.statusText}`);
          }

          const userData = await response.json();
          setUser(userData);
        } catch (error) {
          enqueueSnackbar('Failed to load user information', { variant: 'error' });
        } finally {
          setUserLoading(false);
        }
      }
    };

    fetchUserData();
  }, [isAuthenticated, userFetchAttempted, userLoading, kcContext, enqueueSnackbar, oidcAuth.user?.access_token]);

  // Automatically trigger login if not authenticated
  useEffect(() => {
    if (!isAuthenticated && !oidcAuth.isLoading && !oidcAuth.error) {
      // Automatically trigger login without showing the button
      login();
    }
  }, [isAuthenticated, oidcAuth.isLoading, oidcAuth.error, login]);

  // Reset user fetch state when authentication changes
  useEffect(() => {
    if (!isAuthenticated) {
      setUser(null);
      setUserFetchAttempted(false);
      setUserLoading(false);
    }
  }, [isAuthenticated]);

  // Update form data when user data loads
  useEffect(() => {
    if (user) {
      // Use the field names from Keycloak user object
      form.setFieldValue('firstName', (user.firstName as string) || (user.given_name as string) || '');
      form.setFieldValue('lastName', (user.lastName as string) || (user.family_name as string) || '');
    }
  }, [user, form]);

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

  // Handle authentication state - show loading while OIDC is initializing or authenticating
  if (authLoading || oidcAuth.isLoading || (!isAuthenticated && !oidcAuth.error)) {
    return (
      <Box
        sx={{
          minHeight: '100vh',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          bgcolor: 'background.default',
        }}
      >
        <LoadingSpinner
          size={60}
          message={oidcAuth.isLoading ? 'Initializing Authentication...' : 'Authenticating...'}
        />
      </Box>
    );
  }

  const handlePageChange = (page: string) => {
    setCurrentPage(page);
  };

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: 'background.default' }}>
      {/* Congen App Bar */}
      <CongenAppBar kcContext={kcContext} user={user || undefined} />

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
                onSubmit={e => {
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
                            {(user?.email as string)?.[0] ||
                              (user?.preferred_username as string)?.[0] ||
                              (user?.username as string)?.[0] ||
                              'U'}
                          </Avatar>
                          <Box>
                            <Typography variant="body2" color="text.secondary">
                              {(user?.email as string) || 'No email available'}
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
                            form.setFieldValue(
                              'firstName',
                              (user.given_name as string) || (user.firstName as string) || ''
                            );
                            form.setFieldValue('lastName', (user.family_name as string) || (user.lastName as string) || '');
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
