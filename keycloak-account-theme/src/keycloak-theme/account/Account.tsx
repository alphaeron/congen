import React from 'react';
// Import only the specific components we need to reduce bundle size
import Container from '@mui/material/Container';
import Typography from '@mui/material/Typography';
import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
import Avatar from '@mui/material/Avatar';
import Button from '@mui/material/Button';
import Grid from '@mui/material/Grid';
import Box from '@mui/material/Box';
import TextField from '@mui/material/TextField';
import Alert from '@mui/material/Alert';
import { LoadingSpinner } from '../../components/LoadingSpinner';
import Snackbar from '@mui/material/Snackbar';
import Person from '@mui/icons-material/Person';
import Email from '@mui/icons-material/Email';
import type { KcContext } from './KcContext';
import Navigation from './Navigation';
import PasswordChangeDialog from './PasswordChangeDialog';
import { UserProfileDrawer } from './UserProfileDrawer';
import { CongenAppBar } from './CongenAppBar';
import { ProfileOverview } from './ProfileOverview';
import { useKeycloakUser } from './api/useKeycloakUser';
import { useAuth } from './AuthContext';

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
  
  const [currentPage, setCurrentPage] = React.useState('overview');
  const [showSuccessMessage, setShowSuccessMessage] = React.useState(false);
  const [successMessage, setSuccessMessage] = React.useState('');
  const [showPasswordDialog, setShowPasswordDialog] = React.useState(false);
  
  // Form state for personal info
  const [formData, setFormData] = React.useState({
    email: '',
    firstName: '',
    lastName: '',
  });

  // Update form data when user data loads
  React.useEffect(() => {
    if (displayUser) {
      setFormData({
        email: displayUser.email || '',
        firstName: displayUser.firstName || '',
        lastName: displayUser.lastName || '',
      });
    }
  }, [displayUser]);

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

  const handleInputChange = (field: string) => (event: React.ChangeEvent<HTMLInputElement>) => {
    setFormData(prev => ({
      ...prev,
      [field]: event.target.value,
    }));
  };

  const handleSaveProfile = async () => {
    const success = await updateUser({
      email: formData.email,
      firstName: formData.firstName,
      lastName: formData.lastName,
    });

    if (success) {
      setSuccessMessage('Profile updated successfully!');
      setShowSuccessMessage(true);
    }
  };

  const handleCancelProfile = () => {
    if (displayUser) {
      setFormData({
        email: displayUser.email || '',
        firstName: displayUser.firstName || '',
        lastName: displayUser.lastName || '',
      });
    }
  };

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
            <>
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

              <Card sx={{ maxWidth: 600 }}>
                <CardContent sx={{ p: 4 }}>
                  <Typography variant="h6" gutterBottom sx={{ mb: 3, fontWeight: 600 }}>
                    General
                  </Typography>
                  
                  {(userLoading || authLoading) ? (
                    <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
                      <LoadingSpinner size={20} />
                    </Box>
                  ) : (
                    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
                      <TextField
                        label="Username"
                        value={displayUser?.username || 'Not available'}
                        fullWidth
                        InputProps={{
                          readOnly: true,
                        }}
                        variant="outlined"
                      />
                      
                      <TextField
                        label="Email"
                        value={formData.email}
                        onChange={handleInputChange('email')}
                        fullWidth
                        required
                        InputProps={{
                          startAdornment: <Email sx={{ mr: 1, color: 'text.secondary' }} />,
                        }}
                        variant="outlined"
                      />
                      
                      <TextField
                        label="First name"
                        value={formData.firstName}
                        onChange={handleInputChange('firstName')}
                        fullWidth
                        required
                        InputProps={{
                          startAdornment: <Person sx={{ mr: 1, color: 'text.secondary' }} />,
                        }}
                        variant="outlined"
                      />
                      
                      <TextField
                        label="Last name"
                        value={formData.lastName}
                        onChange={handleInputChange('lastName')}
                        fullWidth
                        required
                        InputProps={{
                          startAdornment: <Person sx={{ mr: 1, color: 'text.secondary' }} />,
                        }}
                        variant="outlined"
                      />
                    </Box>
                  )}

                  <Box sx={{ display: 'flex', gap: 2, mt: 4 }}>
                    <Button 
                      variant="contained" 
                      sx={{ borderRadius: '12px', px: 3 }}
                      onClick={handleSaveProfile}
                      disabled={userLoading || authLoading}
                    >
                      {userLoading ? <LoadingSpinner size={20} /> : 'Save'}
                    </Button>
                    <Button 
                      variant="outlined" 
                      sx={{ borderRadius: '12px', px: 3 }}
                      onClick={handleCancelProfile}
                      disabled={userLoading || authLoading}
                    >
                      Cancel
                    </Button>
                  </Box>
                </CardContent>
              </Card>
            </>
          )}


          {currentPage === 'overview' && (
            <ProfileOverview kcContext={kcContext} user={displayUser} />
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
