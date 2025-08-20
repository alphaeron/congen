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
import { alpha } from '@mui/material/styles';

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
            boxShadow: 'none',
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
              borderRadius: '16px',
              bgcolor: alpha(theme.palette.background.paper, 0.8),
              backdropFilter: 'blur(20px)',
              border: `1px solid ${alpha(theme.palette.divider, 0.5)}`,
              boxShadow: theme.shadows[2],
              maxHeight: 64,
              px: 3,
              py: 1,
              transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
              '&:hover': {
                boxShadow: theme.shadows[4],
                bgcolor: alpha(theme.palette.background.paper, 0.9),
              },
            })}
          >
            <Box
              sx={{
                flexGrow: 1,
                display: 'flex',
                alignItems: 'center',
                gap: 2,
              }}
            >
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <img src={ConGenIcon} style={logoStyle} alt="ConGen" />
                <Typography
                  variant="h6"
                  className="menuLogo menuLogoText"
                  noWrap={true}
                  component="a"
                  href="/"
                  sx={{
                    fontWeight: 700,
                    background: 'linear-gradient(135deg, #0ea5e9, #f97316)',
                    backgroundClip: 'text',
                    WebkitBackgroundClip: 'text',
                    WebkitTextFillColor: 'transparent',
                    textDecoration: 'none',
                    '&:hover': {
                      textDecoration: 'none',
                    },
                  }}
                >
                  ConGen
                </Typography>
              </Box>
              
              <AuthorizedElement requireAuth={false}>
                <Box sx={{ display: { xs: 'none', md: 'flex' }, gap: 1 }}>
                  <MenuItem 
                    sx={{ 
                      py: 1, 
                      px: 2,
                      borderRadius: 2,
                      fontWeight: 500,
                      '&:hover': {
                        bgcolor: theme => alpha(theme.palette.primary.main, 0.08),
                      },
                    }} 
                    component={ExercisesLink}
                  >
                    Exercises
                  </MenuItem>
                  <MenuItem 
                    sx={{ 
                      py: 1, 
                      px: 2,
                      borderRadius: 2,
                      fontWeight: 500,
                      '&:hover': {
                        bgcolor: theme => alpha(theme.palette.primary.main, 0.08),
                      },
                    }} 
                    component={PrivacyPolicyLink}
                  >
                    Privacy
                  </MenuItem>
                </Box>
              </AuthorizedElement>
            </Box>
            
            <Box
              sx={{
                display: { xs: 'none', md: 'flex' },
                gap: 1,
                alignItems: 'center',
              }}
            >
              <AuthorizedElement
                fallback={
                  <Button 
                    color="primary" 
                    variant="contained" 
                    component={Link} 
                    to="/login"
                    sx={{
                      borderRadius: 2,
                      px: 3,
                      py: 1,
                      fontWeight: 600,
                      textTransform: 'none',
                      boxShadow: theme => `0 4px 14px ${alpha(theme.palette.primary.main, 0.3)}`,
                      '&:hover': {
                        boxShadow: theme => `0 6px 20px ${alpha(theme.palette.primary.main, 0.4)}`,
                        transform: 'translateY(-1px)',
                      },
                    }}
                  >
                    Sign in
                  </Button>
                }
              >
                <Button 
                  color="inherit" 
                  component={ProfileLink} 
                  sx={{ 
                    mr: 1,
                    borderRadius: 2,
                    px: 2,
                    py: 1,
                    fontWeight: 500,
                    textTransform: 'none',
                    '&:hover': {
                      bgcolor: theme => alpha(theme.palette.primary.main, 0.08),
                    },
                  }}
                >
                  Profile
                </Button>
                <Button 
                  color="inherit" 
                  onClick={logout} 
                  sx={{ 
                    ml: 1,
                    borderRadius: 2,
                    px: 2,
                    py: 1,
                    fontWeight: 500,
                    textTransform: 'none',
                    '&:hover': {
                      bgcolor: theme => alpha(theme.palette.error.main, 0.08),
                    },
                  }}
                >
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
                sx={{ 
                  minWidth: '40px', 
                  p: 1,
                  borderRadius: 2,
                  '&:hover': {
                    bgcolor: theme => alpha(theme.palette.primary.main, 0.08),
                  },
                }}
              >
                <MenuIcon />
              </Button>
              <Drawer 
                anchor="right" 
                open={open} 
                onClose={toggleDrawer(false)}
                PaperProps={{
                  sx: {
                    bgcolor: 'background.paper',
                    backdropFilter: 'blur(20px)',
                    borderLeft: theme => `1px solid ${theme.palette.divider}`,
                    width: '280px',
                  },
                }}
              >
                <Box
                  sx={{
                    p: 3,
                    display: 'flex',
                    flexDirection: 'column',
                    gap: 1,
                  }}
                >
                  <AuthorizedElement requireAuth={false}>
                    <MenuItem 
                      component={ExercisesLink}
                      sx={{
                        borderRadius: 2,
                        py: 1.5,
                        fontWeight: 500,
                        '&:hover': {
                          bgcolor: theme => alpha(theme.palette.primary.main, 0.08),
                        },
                      }}
                    >
                      Exercises
                    </MenuItem>
                    <MenuItem 
                      component={PrivacyPolicyLink}
                      sx={{
                        borderRadius: 2,
                        py: 1.5,
                        fontWeight: 500,
                        '&:hover': {
                          bgcolor: theme => alpha(theme.palette.primary.main, 0.08),
                        },
                      }}
                    >
                      Privacy Policy
                    </MenuItem>
                  </AuthorizedElement>
                  <Divider sx={{ my: 2 }} />
                  <AuthorizedElement
                    fallback={
                      <MenuItem 
                        component={Link} 
                        to="/login"
                        sx={{
                          borderRadius: 2,
                          py: 1.5,
                          '&:hover': {
                            bgcolor: theme => alpha(theme.palette.primary.main, 0.08),
                          },
                        }}
                      >
                        <Button 
                          color="primary" 
                          variant="contained" 
                          sx={{ 
                            width: '100%',
                            borderRadius: 2,
                            py: 1,
                            fontWeight: 600,
                            textTransform: 'none',
                          }}
                        >
                          Sign in
                        </Button>
                      </MenuItem>
                    }
                  >
                    <MenuItem 
                      component={ProfileLink}
                      sx={{
                        borderRadius: 2,
                        py: 1.5,
                        fontWeight: 500,
                        '&:hover': {
                          bgcolor: theme => alpha(theme.palette.primary.main, 0.08),
                        },
                      }}
                    >
                      Profile
                    </MenuItem>
                    <MenuItem 
                      onClick={logout}
                      sx={{
                        borderRadius: 2,
                        py: 1.5,
                        fontWeight: 500,
                        color: 'error.main',
                        '&:hover': {
                          bgcolor: theme => alpha(theme.palette.error.main, 0.08),
                        },
                      }}
                    >
                      Sign Out
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
