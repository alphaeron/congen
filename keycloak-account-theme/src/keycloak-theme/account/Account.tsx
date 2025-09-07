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
import AccountCircle from '@mui/icons-material/AccountCircle';
import Security from '@mui/icons-material/Security';
import Notifications from '@mui/icons-material/Notifications';
import Help from '@mui/icons-material/Help';
import type { KcContext } from './KcContext';

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
  kcContext: KcContextWithUser;
  i18n: unknown;
}

export default function Account({
  kcContext,
  i18n: _i18n,
}: AccountProps) {
  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      <Typography
        variant="h3"
        component="h1"
        gutterBottom
        sx={{ mb: 4, fontWeight: 700, color: 'primary.main' }}
      >
        Account Management
      </Typography>

      <Grid container spacing={3}>
        {/* User Profile Card */}
        <Grid size={{ xs: 12, md: 6 }}>
          <Card sx={{ height: '100%' }}>
            <CardContent>
              <Box display="flex" alignItems="center" mb={2}>
                <Avatar sx={{ bgcolor: 'primary.main', mr: 2, width: 56, height: 56 }}>
                  <AccountCircle fontSize="large" />
                </Avatar>
                <Box>
                  <Typography variant="h5" component="h2" gutterBottom>
                    Personal Information
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    Manage your profile details
                  </Typography>
                </Box>
              </Box>
              <Divider sx={{ my: 2 }} />
              <List>
                <ListItem>
                  <ListItemText
                    primary="Username"
                    secondary={kcContext.user?.username || 'Not set'}
                  />
                </ListItem>
                <ListItem>
                  <ListItemText primary="Email" secondary={kcContext.user?.email || 'Not set'} />
                </ListItem>
                <ListItem>
                  <ListItemText
                    primary="Name"
                    secondary={
                      `${kcContext.user?.firstName || ''} ${kcContext.user?.lastName || ''}`.trim() ||
                      'Not set'
                    }
                  />
                </ListItem>
              </List>
              <Button variant="contained" sx={{ mt: 2 }}>
                Edit Profile
              </Button>
            </CardContent>
          </Card>
        </Grid>

        {/* Security Settings Card */}
        <Grid size={{ xs: 12, md: 6 }}>
          <Card sx={{ height: '100%' }}>
            <CardContent>
              <Box display="flex" alignItems="center" mb={2}>
                <Avatar
                  sx={{
                    bgcolor: 'secondary.main',
                    mr: 2,
                    width: 56,
                    height: 56,
                  }}
                >
                  <Security fontSize="large" />
                </Avatar>
                <Box>
                  <Typography variant="h5" component="h2" gutterBottom>
                    Security
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    Manage your account security
                  </Typography>
                </Box>
              </Box>
              <Divider sx={{ my: 2 }} />
              <List>
                <ListItem>
                  <ListItemText primary="Password" secondary="Change your password" />
                </ListItem>
                <ListItem>
                  <ListItemText
                    primary="Two-Factor Authentication"
                    secondary="Add extra security to your account"
                  />
                </ListItem>
                <ListItem>
                  <ListItemText
                    primary="Active Sessions"
                    secondary="View and manage active sessions"
                  />
                </ListItem>
              </List>
              <Button variant="outlined" sx={{ mt: 2 }}>
                Security Settings
              </Button>
            </CardContent>
          </Card>
        </Grid>

        {/* Quick Actions */}
        <Grid size={{ xs: 12 }}>
          <Card>
            <CardContent>
              <Typography
                variant="h5"
                component="h2"
                gutterBottom
                sx={{ display: 'flex', alignItems: 'center' }}
              >
                <Help sx={{ mr: 1, color: 'primary.main' }} />
                Quick Actions
              </Typography>
              <Divider sx={{ my: 2 }} />
              <Grid container spacing={2}>
                <Grid size={{ xs: 12, sm: 6, md: 3 }}>
                  <Button variant="outlined" fullWidth sx={{ py: 2, flexDirection: 'column' }}>
                    <AccountCircle sx={{ mb: 1 }} />
                    Edit Profile
                  </Button>
                </Grid>
                <Grid size={{ xs: 12, sm: 6, md: 3 }}>
                  <Button variant="outlined" fullWidth sx={{ py: 2, flexDirection: 'column' }}>
                    <Security sx={{ mb: 1 }} />
                    Change Password
                  </Button>
                </Grid>
                <Grid size={{ xs: 12, sm: 6, md: 3 }}>
                  <Button variant="outlined" fullWidth sx={{ py: 2, flexDirection: 'column' }}>
                    <Notifications sx={{ mb: 1 }} />
                    Notifications
                  </Button>
                </Grid>
                <Grid size={{ xs: 12, sm: 6, md: 3 }}>
                  <Button variant="outlined" fullWidth sx={{ py: 2, flexDirection: 'column' }}>
                    <Help sx={{ mb: 1 }} />
                    Support
                  </Button>
                </Grid>
              </Grid>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Container>
  );
}
