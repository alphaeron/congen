import React, { useState, useEffect } from 'react';
// Import only the specific components we need to reduce bundle size
import Container from '@mui/material/Container';
import Typography from '@mui/material/Typography';
import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
import Avatar from '@mui/material/Avatar';
import Button from '@mui/material/Button';
import Box from '@mui/material/Box';
import Grid from '@mui/material/Grid';
import { LoadingSpinner } from '../../components/LoadingSpinner';
import { useForm } from '@tanstack/react-form';
import { useSnackbar } from 'notistack';
import type { KcContext } from './KcContext';
import { UserProfileDrawer } from './UserProfileDrawer';
import { CongenAppBar } from './CongenAppBar';
import { useAuth } from './AuthContext';
import { useAuth as useOidcAuth } from 'react-oidc-context';
import { FormField } from '../../components/FormField';
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

  // Get user data directly from OIDC
  const user = oidcAuth.user?.profile || null;

  // TanStack Form for profile editing - moved to top level
  const form = useForm({
    defaultValues: {
      firstName: (user?.firstName as string) || (user?.given_name as string) || '',
      lastName: (user?.lastName as string) || (user?.family_name as string) || '',
      email: (user?.email as string) || '',
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

        // User data is already available from OIDC context

        // Update user profile using the API client (Keycloak)
        const keycloakResult = await apiClient.updateUserProfile({
          firstName: value.firstName,
          lastName: value.lastName,
          email: value.email,
        });

        if (keycloakResult.success) {
          // Also update the backend user profile
          const backendResult = await apiClient.updateBackendUserProfile(
            value.firstName,
            value.lastName
          );

          if (backendResult.success) {
            enqueueSnackbar('Profile updated successfully!', { variant: 'success' });

            // User data will be updated automatically via OIDC refresh
          } else {
            enqueueSnackbar('Profile updated in Keycloak but failed to update backend profile', { variant: 'warning' });
          }
        } else {
          enqueueSnackbar('Failed to update user profile', { variant: 'error' });
        }
      } catch {
        enqueueSnackbar('Failed to update user profile', { variant: 'error' });
      }
    },
  });

  // Automatically trigger login if not authenticated
  useEffect(() => {
    if (!isAuthenticated && !oidcAuth.isLoading && !oidcAuth.error) {
      // Automatically trigger login without showing the button
      login();
    }
  }, [isAuthenticated, oidcAuth.isLoading, oidcAuth.error, login]);

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

  const handlePasswordChange = () => {
    if (!kcContext) return;

    // Use Application Initiated Actions (AIA) to redirect to password update flow
    // Based on: https://github.com/keycloak/keycloak-community/blob/main/design/application-initiated-actions.md
    const baseUrl = kcContext.serverBaseUrl || kcContext.authUrl;
    const realm = kcContext.realm?.name || 'congen';

    // Generate a proper S256 code challenge for PKCE
    const codeVerifier =
      Math.random().toString(36).substring(2, 15) +
      Math.random().toString(36).substring(2, 15) +
      Math.random().toString(36).substring(2, 15);

    // Create SHA256 hash of the code verifier and base64url encode it
    const encoder = new TextEncoder();
    const data = encoder.encode(codeVerifier);
    crypto.subtle
      .digest('SHA-256', data)
      .then(hashBuffer => {
        const hashArray = Array.from(new Uint8Array(hashBuffer));
        const hashBase64 = btoa(String.fromCharCode.apply(null, hashArray));
        const codeChallenge = hashBase64.replace(/\+/g, '-').replace(/\//g, '_').replace(/=/g, '');

        // Construct the AIA URL for password update
        const aiaUrl = new URL(`${baseUrl}/realms/${realm}/protocol/openid-connect/auth`);
        aiaUrl.searchParams.set('client_id', 'account-console');
        aiaUrl.searchParams.set('redirect_uri', window.location.origin + window.location.pathname);
        aiaUrl.searchParams.set('response_type', 'code');
        aiaUrl.searchParams.set('scope', 'openid');
        aiaUrl.searchParams.set('kc_action', 'UPDATE_PASSWORD');
        aiaUrl.searchParams.set('code_challenge', codeChallenge);
        aiaUrl.searchParams.set('code_challenge_method', 'S256');

        // Redirect to the password update flow
        window.location.href = aiaUrl.toString();
      })
      .catch(() => {
        // Fallback to plain method if crypto.subtle is not available
        const codeChallenge = codeVerifier;
        const aiaUrl = new URL(`${baseUrl}/realms/${realm}/protocol/openid-connect/auth`);
        aiaUrl.searchParams.set('client_id', 'account-console');
        aiaUrl.searchParams.set('redirect_uri', window.location.origin + window.location.pathname);
        aiaUrl.searchParams.set('response_type', 'code');
        aiaUrl.searchParams.set('scope', 'openid');
        aiaUrl.searchParams.set('kc_action', 'UPDATE_PASSWORD');
        aiaUrl.searchParams.set('code_challenge', codeChallenge);
        aiaUrl.searchParams.set('code_challenge_method', 'plain');

        window.location.href = aiaUrl.toString();
      });
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
            width: 'calc(100% - 240px)',
          }}
        >
          <Container maxWidth={false} sx={{ py: 3, px: 4 }}>
            {currentPage === 'personal-info' && (
              <form
                onSubmit={e => {
                  e.preventDefault();
                  e.stopPropagation();
                  form.handleSubmit();
                }}
              >
                <Grid container spacing={3}>
                  {/* Profile Editing Form - Takes up more space */}
                  <Grid size={{ xs: 12, lg: 8 }}>
                    <Card sx={{ height: '100%' }}>
                      <CardContent sx={{ p: 4 }}>
                        <Typography variant="h6" gutterBottom sx={{ mb: 3, fontWeight: 600 }}>
                          Edit Profile
                        </Typography>

                        {authLoading ? (
                          <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
                            <LoadingSpinner size={20} />
                          </Box>
                        ) : (
                          <Grid container spacing={3}>
                            <Grid size={{ xs: 12, sm: 6 }}>
                              <FormField
                                name="firstName"
                                form={form}
                                type="text"
                                label="First name"
                                required
                                fullWidth
                              />
                            </Grid>
                            <Grid size={{ xs: 12, sm: 6 }}>
                              <FormField
                                name="lastName"
                                form={form}
                                type="text"
                                label="Last name"
                                required
                                fullWidth
                              />
                            </Grid>
                            <Grid size={12}>
                              <FormField
                                name="email"
                                form={form}
                                type="email"
                                label="Email"
                                required
                                fullWidth
                              />
                            </Grid>
                          </Grid>
                        )}

                        <Box sx={{ display: 'flex', gap: 2, mt: 4, justifyContent: 'flex-end' }}>
                          <Button
                            type="button"
                            variant="outlined"
                            sx={{ borderRadius: '12px', px: 3 }}
                            onClick={() => {
                              if (user) {
                                form.setFieldValue(
                                  'firstName',
                                  (user.firstName as string) || (user.given_name as string) || ''
                                );
                                form.setFieldValue(
                                  'lastName',
                                  (user.lastName as string) || (user.family_name as string) || ''
                                );
                                form.setFieldValue(
                                  'email',
                                  (user.email as string) || ''
                                );
                              }
                            }}
                            disabled={authLoading}
                          >
                            Reset
                          </Button>
                          <Button
                            type="submit"
                            variant="contained"
                            sx={{ borderRadius: '12px', px: 3 }}
                            disabled={authLoading || !form.state.isValid || form.state.isSubmitting}
                          >
                            {form.state.isSubmitting ? <LoadingSpinner size={20} /> : 'Save Changes'}
                          </Button>
                        </Box>
                      </CardContent>
                    </Card>
                  </Grid>

                  {/* Password Change Section - Sidebar style */}
                  <Grid size={{ xs: 12, lg: 4 }}>
                    <Card>
                      <CardContent sx={{ p: 4 }}>
                        <Typography variant="h6" gutterBottom sx={{ mb: 3, fontWeight: 600 }}>
                          Security
                        </Typography>

                        <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
                          Change your password to keep your account secure
                        </Typography>

                        <Box sx={{ display: 'flex', justifyContent: 'flex-end' }}>
                          <Button
                            variant="outlined"
                            sx={{ borderRadius: '12px', px: 3 }}
                            onClick={handlePasswordChange}
                            disabled={authLoading}
                          >
                            Change Password
                          </Button>
                        </Box>
                      </CardContent>
                    </Card>
                  </Grid>
                </Grid>
              </form>
            )}
          </Container>
        </Box>
      </Box>
    </Box>
  );
}
