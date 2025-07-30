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
import * as React from 'react';
import { BrowserRouter, Link, Routes, Route, Navigate } from 'react-router-dom';

import { AuthProvider, useAuth } from 'react-oidc-context';

import { getAuthProviderConfig } from './auth/OidcConfig';
import { AuthorizedElement } from './components/AuthorizedElement';
import { ProtectedRoute } from './components/ProtectedRoute';
import { UserProfile } from './components/UserProfile';
import { ExerciseDetailsPage } from './pages/ExerciseDetailsPage';
import { ExerciseOverviewPage } from './pages/ExerciseOverviewPage';
import { LoginPage } from './pages/LoginPage';
import { RootPage } from './pages/RootPage';
import { getTheme } from './theme';

import './App.css';
import './styles/menuButton.css';

import ConGenIcon from './resources/congen-icon.svg';
import useMediaQuery from '@mui/material/useMediaQuery';

const logoStyle = {
  width: '72px',
  height: 'auto',
  cursor: 'pointer',
};

/**
 * The main application content.
 *
 * @return A component to render for the main application content.
 */
function AppContent(): React.ReactElement {
  const prefersDarkMode = useMediaQuery('(prefers-color-scheme: dark)');
  const mode = prefersDarkMode ? 'dark' : 'light';
  const [open, setOpen] = React.useState(false);
  const { isAuthenticated, isLoading } = useAuth();

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
          <Box
            sx={{
              display: 'flex',
              justifyContent: 'center',
              alignItems: 'center',
              height: '100vh',
            }}
          >
            <Typography component="h1" variant="h5">
              Loading...
            </Typography>
          </Box>
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
                  theme.palette.mode === 'light'
                    ? 'rgba(255, 255, 255, 0.4)'
                    : 'rgba(0, 0, 0, 0.4)',
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
                    <MenuItem
                      sx={{ py: '6px', px: '12px' }}
                      /* eslint-disable-next-line react/display-name */
                      component={React.forwardRef((props, ref) => (
                        <Link to="/exercises" {...props} ref={ref} />
                      ))}
                    >
                      Exercises
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
                      <MenuItem
                        /* eslint-disable-next-line react/display-name */
                        component={React.forwardRef((props, ref) => (
                          <Link to="/exercises" {...props} ref={ref} />
                        ))}
                      >
                        Exercises
                      </MenuItem>
                    </AuthorizedElement>
                    <Divider />
                    <AuthorizedElement
                      fallback={
                        <React.Fragment>
                          <MenuItem>
                            <Button
                              color="primary"
                              variant="contained"
                              component={Link}
                              to="/login"
                              sx={{ width: '100%' }}
                            >
                              Sign up
                            </Button>
                          </MenuItem>
                          <MenuItem>
                            <Button
                              color="primary"
                              variant="outlined"
                              component={Link}
                              to="/login"
                              sx={{ width: '100%' }}
                            >
                              Sign in
                            </Button>
                          </MenuItem>
                        </React.Fragment>
                      }
                    >
                      <MenuItem>
                        <UserProfile />
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
              path="/login"
              element={
                <ProtectedRoute requireAuth={false} redirectTo="/exercises">
                  <LoginPage />
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
  const authConfig = getAuthProviderConfig();
  
  return (
    <BrowserRouter>
      <AuthProvider {...authConfig}>
        <AppContent />
      </AuthProvider>
    </BrowserRouter>
  );
} // end component App
