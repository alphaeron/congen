import React, { useState, useEffect } from 'react';
// Import only the specific components we need to reduce bundle size
import Container from '@mui/material/Container';
import Typography from '@mui/material/Typography';
import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
import Avatar from '@mui/material/Avatar';
import Button from '@mui/material/Button';
import Box from '@mui/material/Box';
import Alert from '@mui/material/Alert';
import { LoadingSpinner } from '../../components/LoadingSpinner';
import Snackbar from '@mui/material/Snackbar';
import { useForm } from '@tanstack/react-form';
import type { KcContext } from './KcContext';
import PasswordChangeDialog from './PasswordChangeDialog';
import { UserProfileDrawer } from './UserProfileDrawer';
import { CongenAppBar } from './CongenAppBar';
import { ProfileOverview } from './ProfileOverview';
import { useKeycloakUser } from './api/useKeycloakUser';
import { useAuth } from './AuthContext';
import { FormField } from '../../components/FormField';

interface AccountProps {
  kcContext?: KcContext;
  i18n?: unknown;
}

export default function Account({
  kcContext,
  i18n: _i18n,
}: AccountProps) {
  // Use the OIDC authentication context
  const { user: authUser, isAuthenticated, isLoading: authLoading, login } = useAuth();
  
  // Use the Keycloak user hook to fetch user data from the API
  const { 
    user: apiUser, 
    loading: userLoading, 
    error: userError, 
    updateUser
  } = useKeycloakUser(kcContext);
  
  // Extract user data from Keycloak context
  // Keycloak provides user data in different possible locations
  const user = kcContext?.user || kcContext?.profile || kcContext?.account?.user || kcContext?.userProfile;
  
  // Also check global window.kcContext for user data
  const globalUser = (window as any).kcContext?.user;
  
  // Use OIDC user if available, otherwise fall back to API user or context user
  const finalUser = authUser || apiUser || user || globalUser;
  
  console.log('Account - Final user data:', finalUser);
  console.log('Account - Auth state:', { isAuthenticated, authLoading, authUser });
  
  // Use OIDC user data if available, otherwise fall back to other sources
  const displayUser = authUser || apiUser || finalUser;
  
  const [currentPage, setCurrentPage] = useState('personal-info');
  const [showSuccessMessage, setShowSuccessMessage] = useState(false);
  const [successMessage, setSuccessMessage] = useState('');
  const [showPasswordDialog, setShowPasswordDialog] = useState(false);
  

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

  // If we have user data from any source, show the account interface
  // Don't require OIDC authentication if we have user data from Keycloak session
  if (!displayUser && !isAuthenticated) {
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
          <Typography variant="h6" color="primary">
            Secure Profile Access
          </Typography>
          <Typography variant="body2" sx={{ mt: 1, mb: 2 }}>
            Please log in to verify your identity so we can safely update your profile details.
          </Typography>
          <Button variant="contained" onClick={login}>
            Verify Identity
          </Button>
        </Box>
      </Box>
    );
  }

  // TanStack Form for profile editing
  const form = useForm({
    defaultValues: {
      firstName: '',
      lastName: '',
    },
    onSubmit: async ({ value }) => {
      const success = await updateUser({
        firstName: value.firstName,
        lastName: value.lastName,
      });

      if (success) {
        setSuccessMessage('Profile updated successfully!');
        setShowSuccessMessage(true);
      }
    },
  });

  // Update form data when user data loads
  useEffect(() => {
    if (displayUser) {
      form.setFieldValue('firstName', displayUser.firstName || '');
      form.setFieldValue('lastName', displayUser.lastName || '');
    }
  }, [displayUser, form]);

  const handlePageChange = (page: string) => {
    setCurrentPage(page);
  };

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: 'background.default' }}>
      {/* Congen App Bar */}
      <CongenAppBar kcContext={kcContext} user={displayUser} />
      
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
          {/* Success Message Snackbar */}
          <Snackbar
            open={showSuccessMessage}
            autoHideDuration={6000}
            onClose={() => setShowSuccessMessage(false)}
            anchorOrigin={{ vertical: 'top', horizontal: 'center' }}
          >
            <Alert onClose={() => setShowSuccessMessage(false)} severity="success" sx={{ width: '100%' }}>
              {successMessage}
            </Alert>
          </Snackbar>

          {/* Error Messages */}
          {userError && (
            <Alert severity="error" sx={{ mb: 3 }}>
              {userError}
            </Alert>
          )}

          {currentPage === 'personal-info' && (
            <form
              onSubmit={(e) => {
                e.preventDefault();
                e.stopPropagation();
                form.handleSubmit();
              }}
            >
              <Typography
                variant="h4"
                component="h1"
                gutterBottom
                sx={{ mb: 4, fontWeight: 700, color: 'text.primary' }}
              >
                Personal info
              </Typography>
              <Typography
                variant="body1"
                color="text.secondary"
                sx={{ mb: 4 }}
              >
                Manage your basic information
              </Typography>

              {/* Member Summary Section */}
              <Card sx={{ maxWidth: 600, mb: 3 }}>
                <CardContent sx={{ p: 4 }}>
                  <Typography variant="h6" gutterBottom sx={{ mb: 3, fontWeight: 600 }}>
                    Member Summary
                  </Typography>
                  
                  {(userLoading || authLoading) ? (
                    <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
                      <LoadingSpinner size={20} />
                    </Box>
                  ) : (
                    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                        <Avatar sx={{ bgcolor: 'primary.main' }}>
                          {displayUser?.firstName?.[0] || displayUser?.username?.[0] || 'U'}
                        </Avatar>
                        <Box>
                          <Typography variant="h6">
                            {displayUser?.firstName && displayUser?.lastName 
                              ? `${displayUser.firstName} ${displayUser.lastName}`
                              : displayUser?.username || 'User'
                            }
                          </Typography>
                          <Typography variant="body2" color="text.secondary">
                            {displayUser?.email || 'No email available'}
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
                  
                  {(userLoading || authLoading) ? (
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
                      disabled={userLoading || authLoading || !form.state.isValid || form.state.isSubmitting}
                    >
                      {form.state.isSubmitting ? <LoadingSpinner size={20} /> : 'Save Changes'}
                    </Button>
                    <Button 
                      type="button"
                      variant="outlined" 
                      sx={{ borderRadius: '12px', px: 3 }}
                      onClick={() => {
                        if (displayUser) {
                          form.setFieldValue('firstName', displayUser.firstName || '');
                          form.setFieldValue('lastName', displayUser.lastName || '');
                        }
                      }}
                      disabled={userLoading || authLoading}
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
                    disabled={userLoading || authLoading}
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
