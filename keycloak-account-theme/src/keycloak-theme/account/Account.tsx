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
import CircularProgress from '@mui/material/CircularProgress';
import Snackbar from '@mui/material/Snackbar';
import Security from '@mui/icons-material/Security';
import Person from '@mui/icons-material/Person';
import Email from '@mui/icons-material/Email';
import type { KcContext } from './KcContext';
import Navigation from './Navigation';
import PasswordChangeDialog from './PasswordChangeDialog';
import { useKeycloakUser } from './api/useKeycloakUser';

interface AccountProps {
  kcContext?: KcContext;
  i18n?: unknown;
}

export default function Account({
  kcContext,
  i18n: _i18n,
}: AccountProps) {
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
  
  // Use API user if available, otherwise fall back to context user
  const finalUser = apiUser || user || globalUser;
  
  console.log('Account - Final user data:', finalUser);
  
  // Use API user data if available, otherwise fall back to context user data
  const displayUser = apiUser || finalUser;
  
  const [currentPage, setCurrentPage] = React.useState('personal-info');
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
      <Navigation kcContext={kcContext} currentPage={currentPage} onPageChange={handlePageChange} />
      
      <Container maxWidth="lg" sx={{ py: 4 }}>
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
                
                {userLoading ? (
                  <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
                    <CircularProgress />
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
                    disabled={userLoading}
                  >
                    {userLoading ? <CircularProgress size={20} /> : 'Save'}
                  </Button>
                  <Button 
                    variant="outlined" 
                    sx={{ borderRadius: '12px', px: 3 }}
                    onClick={handleCancelProfile}
                    disabled={userLoading}
                  >
                    Cancel
                  </Button>
                </Box>
              </CardContent>
            </Card>
          </>
        )}

        {currentPage === 'account-security' && (
          <>
            <Typography
              variant="h4"
              component="h1"
              gutterBottom
              sx={{ mb: 4, fontWeight: 700, color: 'text.primary' }}
            >
              Account security
            </Typography>
            <Typography
              variant="body1"
              color="text.secondary"
              sx={{ mb: 4 }}
            >
              Manage your account security settings
            </Typography>

            <Grid container spacing={3}>
              <Grid size={{ xs: 12, md: 6 }}>
                <Card sx={{ height: '100%' }}>
                  <CardContent>
                    <Box display="flex" alignItems="center" mb={2}>
                      <Avatar sx={{ bgcolor: 'primary.main', mr: 2, width: 56, height: 56 }}>
                        <Security fontSize="large" />
                      </Avatar>
                      <Box>
                        <Typography variant="h6" component="h2" gutterBottom>
                          Password
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                          Change your password
                        </Typography>
                      </Box>
                    </Box>
                    <Button 
                      variant="outlined" 
                      sx={{ mt: 2, borderRadius: '12px' }}
                      onClick={() => setShowPasswordDialog(true)}
                    >
                      Change Password
                    </Button>
                  </CardContent>
                </Card>
              </Grid>

              <Grid size={{ xs: 12, md: 6 }}>
                <Card sx={{ height: '100%' }}>
                  <CardContent>
                    <Box display="flex" alignItems="center" mb={2}>
                      <Avatar sx={{ bgcolor: 'secondary.main', mr: 2, width: 56, height: 56 }}>
                        <Security fontSize="large" />
                      </Avatar>
                      <Box>
                        <Typography variant="h6" component="h2" gutterBottom>
                          Two-Factor Authentication
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                          Add extra security to your account
                        </Typography>
                      </Box>
                    </Box>
                    <Button variant="outlined" sx={{ mt: 2, borderRadius: '12px' }}>
                      Setup 2FA
                    </Button>
                  </CardContent>
                </Card>
              </Grid>
            </Grid>
          </>
        )}
      </Container>

      {/* Password Change Dialog */}
      <PasswordChangeDialog
        open={showPasswordDialog}
        onClose={() => setShowPasswordDialog(false)}
        kcContext={kcContext}
      />
    </Box>
  );
}
