import React from 'react';
// Import only the specific components we need to reduce bundle size
import Container from '@mui/material/Container';
import Typography from '@mui/material/Typography';
import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
import Avatar from '@mui/material/Avatar';
import List from '@mui/material/List';
import ListItem from '@mui/material/ListItem';
import ListItemText from '@mui/material/ListItemText';
import Divider from '@mui/material/Divider';
import Button from '@mui/material/Button';
import Grid from '@mui/material/Grid';
import Box from '@mui/material/Box';
import TextField from '@mui/material/TextField';
import AccountCircle from '@mui/icons-material/AccountCircle';
import Security from '@mui/icons-material/Security';
import Person from '@mui/icons-material/Person';
import Email from '@mui/icons-material/Email';
import type { KcContext } from './KcContext';
import Navigation from './Navigation';

// Extended KcContext with user information
type KcContextWithUser = KcContext & {
  user?: {
    username?: string;
    email?: string;
    firstName?: string;
    lastName?: string;
  };
};

interface AccountProps {
  kcContext?: KcContextWithUser;
  i18n?: unknown;
}

export default function Account({
  kcContext,
  i18n: _i18n,
}: AccountProps) {
  // Debug logging to see what's in kcContext
  console.log('KcContext:', kcContext);
  
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
  
  console.log('User:', kcContext.user);
  const [currentPage, setCurrentPage] = React.useState('personal-info');

  const handlePageChange = (page: string) => {
    setCurrentPage(page);
  };

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: 'background.default' }}>
      <Navigation kcContext={kcContext} currentPage={currentPage} onPageChange={handlePageChange} />
      
      <Container maxWidth="lg" sx={{ py: 4 }}>
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
                
                <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
                  <TextField
                    label="Username"
                    value={kcContext.user?.username || 'Not available'}
                    fullWidth
                    InputProps={{
                      readOnly: true,
                    }}
                    variant="outlined"
                  />
                  
                  <TextField
                    label="Email"
                    value={kcContext.user?.email || 'Not available'}
                    fullWidth
                    required
                    InputProps={{
                      startAdornment: <Email sx={{ mr: 1, color: 'text.secondary' }} />,
                    }}
                    variant="outlined"
                  />
                  
                  <TextField
                    label="First name"
                    value={kcContext.user?.firstName || 'Not available'}
                    fullWidth
                    required
                    InputProps={{
                      startAdornment: <Person sx={{ mr: 1, color: 'text.secondary' }} />,
                    }}
                    variant="outlined"
                  />
                  
                  <TextField
                    label="Last name"
                    value={kcContext.user?.lastName || 'Not available'}
                    fullWidth
                    required
                    InputProps={{
                      startAdornment: <Person sx={{ mr: 1, color: 'text.secondary' }} />,
                    }}
                    variant="outlined"
                  />
                </Box>

                <Box sx={{ display: 'flex', gap: 2, mt: 4 }}>
                  <Button variant="contained" sx={{ borderRadius: '12px', px: 3 }}>
                    Save
                  </Button>
                  <Button variant="outlined" sx={{ borderRadius: '12px', px: 3 }}>
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
              <Grid xs={12} md={6}>
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
                    <Button variant="outlined" sx={{ mt: 2, borderRadius: '12px' }}>
                      Change Password
                    </Button>
                  </CardContent>
                </Card>
              </Grid>

              <Grid xs={12} md={6}>
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
    </Box>
  );
}
