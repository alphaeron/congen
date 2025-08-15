import { default as MenuIcon } from '@mui/icons-material/Menu';
import AppBar from '@mui/material/AppBar';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import Container from '@mui/material/Container';
import CssBaseline from '@mui/material/CssBaseline';
import Divider from '@mui/material/Divider';
import Drawer from '@mui/material/Drawer';
import MenuItem from '@mui/material/MenuItem';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import Toolbar from '@mui/material/Toolbar';
import Typography from '@mui/material/Typography';
import useMediaQuery from '@mui/material/useMediaQuery';
import * as React from 'react';
import { AuthProvider as OidcAuthProvider } from 'react-oidc-context';
import { BrowserRouter, Link, Routes, Route } from 'react-router';

import { getAuthProviderConfig } from './auth/OidcConfig';
import { AuthCallback } from './components/AuthCallback';
import { AuthorizedElement } from './components/AuthorizedElement';
import { LoadingSpinner } from './components/LoadingSpinner';
import { ProtectedRoute } from './components/ProtectedRoute';
import { AuthProvider, useAuth } from './contexts/AuthContext';
import { ExerciseDetailsPage } from './pages/ExerciseDetailsPage';
import { ExerciseOverviewPage } from './pages/ExerciseOverviewPage';
import { LoginPage } from './pages/LoginPage';
import { PrivacyPolicyPage } from './pages/PrivacyPolicyPage';
import { RootPage } from './pages/RootPage';
import { UserProfilePage } from './pages/UserProfilePage';
import ConGenIcon from './resources/congen-icon.svg';
import { getTheme } from './theme';

import './App.css';
import './styles/menuButton.css';

const logoStyle = {
  width: '72px',
  height: 'auto',
  cursor: 'pointer',
};

// Create named forwardRef components for links
const ExercisesLink = React.forwardRef<HTMLAnchorElement, React.ComponentPropsWithRef<'a'>>(
  (props, ref) => <Link to="/exercises" {...props} ref={ref} />
);
ExercisesLink.displayName = 'ExercisesLink';

const ProfileLink = React.forwardRef<HTMLAnchorElement, React.ComponentPropsWithRef<'a'>>(
  (props, ref) => <Link to="/profile" {...props} ref={ref} />
);
ProfileLink.displayName = 'ProfileLink';

const PrivacyPolicyLink = React.forwardRef<HTMLAnchorElement, React.ComponentPropsWithRef<'a'>>(
  (props, ref) => <Link to="/privacy_policy" {...props} ref={ref} />
);
PrivacyPolicyLink.displayName = 'PrivacyPolicyLink';

/**
 * The main application content.
 *
 * @return A component to render for the main application content.
 */
function AppContent(): React.ReactElement {
  const prefersDarkMode = useMediaQuery('(prefers-color-scheme: dark)');
  const mode = prefersDarkMode ? 'dark' : 'light';
  const [open, setOpen] = React.useState(false);
  const { isLoading, logout } = useAuth();

  const toggleDrawer = (newOpen: boolean) => () => {
    setOpen(newOpen);
  };

  const theme = createTheme(getTheme(mode));

  // Show loading state while authentication is being determined
  if (isLoading) {
    return (
      <ThemeProvider theme={theme}>
        <CssBaseline />
        <Container maxWidth="xl">
          <LoadingSpinner fullHeight />
        </Container>
      </ThemeProvider>
    );
  }

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <Container maxWidth="xl">
        <AppBar
          position="static"
          sx={{
            boxShadow: 0,
            bgcolor: 'transparent',
            backgroundImage: 'none',
            mt: 2,
            mb: 2,
          }}
        >
          <Toolbar
            variant="regular"
            sx={theme => ({
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              flexShrink: 0,
              borderRadius: '999px',
              bgcolor:
                theme.palette.mode === 'light' ? 'rgba(255, 255, 255, 0.4)' : 'rgba(0, 0, 0, 0.4)',
              backdropFilter: 'blur(24px)',
              maxHeight: 40,
              border: '1px solid',
              borderColor: 'divider',
              boxShadow:
                theme.palette.mode === 'light'
                  ? `0 0 1px rgba(34, 54, 204, 0.1), 1px 1.5px 2px -1px rgba(34, 54, 204, 0.15), 4px 4px 12px -2.5px rgba(34, 54, 204, 0.15)`
                  : '0 0 1px rgba(32, 32, 32, 0.7), 1px 1.5px 2px -1px rgba(32, 32, 32, 0.65), 4px 4px 12px -2.5px rgba(32, 32, 32, 0.65)',
            })}
          >
            <Box
              sx={{
                flexGrow: 1,
                display: 'flex',
                alignItems: 'center',
                ml: '-18px',
                px: 0,
              }}
            >
              <img src={ConGenIcon} style={logoStyle} alt="ConGen" />
              <Typography
                variant="h6"
                className="menuLogo menuLogoText"
                noWrap={true}
                component="a"
                href="/"
              >
                ConGen
              </Typography>
              <AuthorizedElement requireAuth={false}>
                <Box sx={{ display: { xs: 'none', md: 'flex' } }}>
                  <MenuItem sx={{ py: '6px', px: '12px' }} component={ExercisesLink}>
                    Exercises
                  </MenuItem>
                  <MenuItem sx={{ py: '6px', px: '12px' }} component={PrivacyPolicyLink}>
                    Privacy
                  </MenuItem>
                </Box>
              </AuthorizedElement>
            </Box>
            <Box
              sx={{
                display: { xs: 'none', md: 'flex' },
                gap: 0.5,
                alignItems: 'center',
              }}
            >
              <AuthorizedElement
                fallback={
                  <Button color="primary" variant="contained" component={Link} to="/login">
                    Sign in
                  </Button>
                }
              >
                <Button color="inherit" component={ProfileLink} sx={{ mr: 1 }}>
                  Profile
                </Button>
                <Button color="inherit" onClick={logout} sx={{ ml: 1 }}>
                  Sign Out
                </Button>
              </AuthorizedElement>
            </Box>
            <Box sx={{ display: { sm: '', md: 'none' } }}>
              <Button
                variant="text"
                color="primary"
                aria-label="menu"
                onClick={toggleDrawer(true)}
                sx={{ minWidth: '30px', p: '4px' }}
              >
                <MenuIcon />
              </Button>
              <Drawer anchor="right" open={open} onClose={toggleDrawer(false)}>
                <Box
                  sx={{
                    minWidth: '60dvw',
                    p: 2,
                    backgroundColor: 'background.paper',
                    flexGrow: 1,
                  }}
                >
                  <AuthorizedElement requireAuth={false}>
                    <MenuItem component={ExercisesLink}>Exercises</MenuItem>
                    <MenuItem component={PrivacyPolicyLink}>Privacy Policy</MenuItem>
                  </AuthorizedElement>
                  <Divider />
                  <AuthorizedElement
                    fallback={
                      <MenuItem component={Link} to="/login">
                        <Button color="primary" variant="contained" sx={{ width: '100%' }}>
                          Sign in
                        </Button>
                      </MenuItem>
                    }
                  >
                    <MenuItem component={ProfileLink}>Profile</MenuItem>
                    <MenuItem>
                      <Button color="inherit" onClick={logout} sx={{ width: '100%' }}>
                        Sign Out
                      </Button>
                    </MenuItem>
                  </AuthorizedElement>
                </Box>
              </Drawer>
            </Box>
          </Toolbar>
        </AppBar>
        <Routes>
          <Route
            path="/"
            element={
              <ProtectedRoute requireAuth={false}>
                <RootPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/exercises"
            element={
              <ProtectedRoute>
                <ExerciseOverviewPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/exercises/:exerciseName"
            element={
              <ProtectedRoute>
                <ExerciseDetailsPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/profile"
            element={
              <ProtectedRoute>
                <UserProfilePage />
              </ProtectedRoute>
            }
          />
          <Route path="/auth/callback" element={<AuthCallback />} />
          <Route path="/login" element={<LoginPage />} />
          <Route
            path="/privacy_policy"
            element={
              <ProtectedRoute requireAuth={false}>
                <PrivacyPolicyPage />
              </ProtectedRoute>
            }
          />
        </Routes>
      </Container>
    </ThemeProvider>
  );
} // end component AppContent

/**
 * The main application wrapper with authentication provider.
 *
 * @return A component to render for the main application.
 */
export function App(): React.ReactElement {
  return (
    <BrowserRouter>
      <OidcAuthProvider {...getAuthProviderConfig()}>
        <AuthProvider>
          <AppContent />
        </AuthProvider>
      </OidcAuthProvider>
    </BrowserRouter>
  );
} // end component App
