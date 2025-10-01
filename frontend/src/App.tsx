import AccountCircleIcon from '@mui/icons-material/AccountCircle';
import { default as MenuIcon } from '@mui/icons-material/Menu';
import AppBar from '@mui/material/AppBar';
import Avatar from '@mui/material/Avatar';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import Container from '@mui/material/Container';
import CssBaseline from '@mui/material/CssBaseline';
import Divider from '@mui/material/Divider';
import Drawer from '@mui/material/Drawer';
import IconButton from '@mui/material/IconButton';
import Menu from '@mui/material/Menu';
import MenuItem from '@mui/material/MenuItem';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import { alpha } from '@mui/material/styles';
import Toolbar from '@mui/material/Toolbar';
import Typography from '@mui/material/Typography';
import useMediaQuery from '@mui/material/useMediaQuery';
import * as React from 'react';
import { AuthProvider as OidcAuthProvider } from 'react-oidc-context';
import { BrowserRouter, Link, Routes, Route, Navigate, useNavigate } from 'react-router';

import { getAuthProviderConfig } from './auth/OidcConfig';
import { AuthCallback } from './components/AuthCallback';
import { AuthorizedElement } from './components/AuthorizedElement';
import { CookieConsentManager } from './components/CookieConsentManager';
import { LoadingSpinner } from './components/LoadingSpinner';
import { PasswordChangeRedirect } from './components/PasswordChangeRedirect';
import { ProfileEditRedirect } from './components/ProfileEditRedirect';
import { ProtectedRoute } from './components/ProtectedRoute';
import { AuthProvider, useAuth } from './contexts/AuthContext';
import { CookieProvider } from './contexts/CookieContext';
import { DataProvider } from './contexts/DataContext';
import { DashboardPage } from './pages/DashboardPage';
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

// Create named components for links
const ExercisesLink: React.FC<React.ComponentPropsWithRef<'a'>> = props => (
  <Link to="/exercises" {...props} />
);

const ProfileLink: React.FC<React.ComponentPropsWithRef<'a'>> = props => (
  <Link to="/profile" {...props} />
);

const PrivacyPolicyLink: React.FC<React.ComponentPropsWithRef<'a'>> = props => (
  <Link to="/privacy_policy" {...props} />
);

// Drawer context for sharing drawer state across components
interface DrawerContextType {
  drawerOpen: boolean;
  setDrawerOpen: (open: boolean) => void;
  drawerWidth: number;
}

const DrawerContext = React.createContext<DrawerContextType | undefined>(undefined);

export const useDrawer = () => {
  const context = React.useContext(DrawerContext);
  if (!context) {
    throw new Error('useDrawer must be used within a DrawerProvider');
  }
  return context;
};

/**
 * Root redirect component that redirects based on authentication status.
 *
 * When authenticated: redirects to /dashboard
 * When not authenticated: shows the root page
 *
 * @return Redirect component or null
 */
function RootRedirect(): React.ReactElement | null {
  const { isAuthenticated, isLoading } = useAuth();

  // Show loading while determining authentication status
  if (isLoading) {
    return null;
  }

  // Redirect to dashboard if authenticated
  if (isAuthenticated) {
    return <Navigate to="/dashboard?section=overview" replace />;
  }

  // Show root page if not authenticated
  return <RootPage />;
}

/**
 * The main application content.
 *
 * @return A component to render for the main application content.
 */
function AppContent(): React.ReactElement {
  const navigate = useNavigate();
  const prefersDarkMode = useMediaQuery('(prefers-color-scheme: dark)');
  const mode = prefersDarkMode ? 'dark' : 'light';
  const [open, setOpen] = React.useState(false);
  const [userMenuAnchor, setUserMenuAnchor] = React.useState<null | HTMLElement>(null);
  const { isLoading, logout, isAuthenticated, user } = useAuth();

  // Drawer state for dashboard/profile pages
  const [drawerOpen, setDrawerOpen] = React.useState(false);
  const drawerWidth = 280;
  const toggleDrawer = (newOpen: boolean) => () => {
    setOpen(newOpen);
  };

  const handleUserMenuOpen = (event: React.MouseEvent<HTMLElement>) => {
    setUserMenuAnchor(event.currentTarget);
  };

  const handleUserMenuClose = () => {
    setUserMenuAnchor(null);
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

  const appContent = (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <DrawerContext.Provider value={{ drawerOpen, setDrawerOpen, drawerWidth }}>
        <AppBar
          position="sticky"
          sx={{
            width: '100%',
            boxShadow: 'none',
            bgcolor: 'transparent',
            backgroundImage: 'none',
          }}
        >
          <Toolbar
            variant="regular"
            sx={theme => ({
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              flexShrink: 0,
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
              <Box sx={{ display: 'flex', alignItems: 'center' }}>
                <img 
                  src={ConGenIcon} 
                  style={{
                    ...logoStyle,
                    filter: 'brightness(0) saturate(100%) invert(70%) sepia(100%) saturate(1000%) hue-rotate(180deg) brightness(1.2) contrast(1.2)',
                    cursor: 'pointer',
                  }} 
                  alt="ConGen"
                  onClick={() => navigate('/')}
                />
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
              <IconButton
                onClick={handleUserMenuOpen}
                sx={{
                  p: 1,
                  borderRadius: '50%',
                  '&:hover': {
                    bgcolor: theme => alpha(theme.palette.primary.main, 0.08),
                  },
                }}
              >
                {isAuthenticated && user ? (
                  <Avatar
                    sx={{
                      width: 32,
                      height: 32,
                      bgcolor: theme => alpha(theme.palette.primary.main, 0.1),
                      color: 'primary.main',
                      fontSize: '0.875rem',
                      fontWeight: 600,
                    }}
                  >
                    {user.name?.charAt(0).toUpperCase() || 'U'}
                  </Avatar>
                ) : (
                  <AccountCircleIcon
                    sx={{
                      fontSize: 32,
                      color: 'text.primary',
                    }}
                  />
                )}
              </IconButton>
              <Menu
                anchorEl={userMenuAnchor}
                open={Boolean(userMenuAnchor)}
                onClose={handleUserMenuClose}
                sx={{ zIndex: theme.zIndex.drawer + 10 }}
                PaperProps={{
                  sx: {
                    mt: 1,
                    minWidth: 180,
                    borderRadius: 2,
                    boxShadow: theme => `0 8px 32px ${alpha(theme.palette.common.black, 0.12)}`,
                    '& .MuiMenuItem-root': {
                      borderRadius: 1,
                      mx: 0.5,
                      my: 0.25,
                    },
                  },
                }}
                transformOrigin={{ horizontal: 'right', vertical: 'top' }}
                anchorOrigin={{ horizontal: 'right', vertical: 'bottom' }}
              >
                {isAuthenticated ? (
                  [
                    <MenuItem
                      key="profile"
                      component={ProfileLink}
                      onClick={handleUserMenuClose}
                      sx={{
                        fontWeight: 500,
                        '&:hover': {
                          bgcolor: theme => alpha(theme.palette.primary.main, 0.08),
                        },
                      }}
                    >
                      Profile
                    </MenuItem>,
                    <Divider key="divider" sx={{ my: 1 }} />,
                    <MenuItem
                      key="signout"
                      onClick={() => {
                        handleUserMenuClose();
                        logout();
                      }}
                      sx={{
                        fontWeight: 500,
                        color: 'error.main',
                        '&:hover': {
                          bgcolor: theme => alpha(theme.palette.error.main, 0.08),
                        },
                      }}
                    >
                      Sign Out
                    </MenuItem>,
                  ]
                ) : (
                  <MenuItem
                    component={Link}
                    to="/login"
                    onClick={handleUserMenuClose}
                    sx={{
                      fontWeight: 500,
                      '&:hover': {
                        bgcolor: theme => alpha(theme.palette.primary.main, 0.08),
                      },
                    }}
                  >
                    Sign in
                  </MenuItem>
                )}
              </Menu>
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

        {/* Main content area */}
        <Box
          component="main"
          sx={{
            flexGrow: 1,
            minHeight: 'calc(100vh - 64px)',
            overflow: 'hidden',
          }}
        >
          <Routes>
            <Route path="/" element={<RootRedirect />} />
            <Route
              path="/dashboard"
              element={
                <ProtectedRoute>
                  <DataProvider>
                    <DashboardPage />
                  </DataProvider>
                </ProtectedRoute>
              }
            />
            <Route
              path="/exercises"
              element={
                <ProtectedRoute>
                  <DataProvider>
                    <ExerciseOverviewPage />
                  </DataProvider>
                </ProtectedRoute>
              }
            />
            <Route
              path="/exercises/:exerciseName"
              element={
                <ProtectedRoute>
                  <DataProvider>
                    <Container maxWidth="xl" sx={{ py: 2 }}>
                      <ExerciseDetailsPage />
                    </Container>
                  </DataProvider>
                </ProtectedRoute>
              }
            />
            <Route
              path="/profile"
              element={
                <ProtectedRoute>
                  <DataProvider>
                    <UserProfilePage />
                  </DataProvider>
                </ProtectedRoute>
              }
            />
            <Route path="/auth/callback" element={<AuthCallback />} />
            <Route path="/password-change-redirect" element={<PasswordChangeRedirect />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/privacy_policy" element={<PrivacyPolicyPage />} />
            <Route path="/profile-edit-redirect" element={<ProfileEditRedirect />} />
          </Routes>
        </Box>

        {/* Cookie Consent Banner */}
        <CookieConsentManager />
      </DrawerContext.Provider>
    </ThemeProvider>
  );

  return appContent;
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
          <CookieProvider>
            <AppContent />
          </CookieProvider>
        </AuthProvider>
      </OidcAuthProvider>
    </BrowserRouter>
  );
} // end component App
