import { default as MenuIcon } from '@mui/icons-material/Menu';
import { PaletteMode } from '@mui/material';
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
import { BrowserRouter, Link, Routes, Route } from 'react-router-dom';

import { ToggleColorMode } from './components/ToggleColorMode';
import { ExerciseDetailsPage } from './pages/ExerciseDetailsPage';
import { ExerciseOverviewPage } from './pages/ExerciseOverviewPage';
import { RootPage } from './pages/RootPage';
import { getTheme } from './theme';

import './App.css';
import './styles/menuButton.css';

import ConGenIcon from './resources/congen-icon.svg';

const logoStyle = {
  width: '72px',
  height: 'auto',
  cursor: 'pointer',
};

/**
 * The main application.
 *
 * @return A component to render for the main application.
 */
export function App(): React.ReactElement {
  const [open, setOpen] = React.useState(false);
  const [mode, setMode] = React.useState<PaletteMode>('light');

  const toggleColorMode = () => {
    setMode(prev => (prev === 'dark' ? 'light' : 'dark'));
  };

  const toggleDrawer = (newOpen: boolean) => () => {
    setOpen(newOpen);
  };

  const theme = createTheme(getTheme(mode));

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <BrowserRouter>
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
                    ? `0 0 1px rgba(85, 166, 246, 0.1), 1px 1.5px 2px -1px rgba(85, 166, 246, 0.15), 4px 4px 12px -2.5px rgba(85, 166, 246, 0.15)`
                    : '0 0 1px rgba(2, 31, 59, 0.7), 1px 1.5px 2px -1px rgba(2, 31, 59, 0.65), 4px 4px 12px -2.5px rgba(2, 31, 59, 0.65)',
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
              </Box>
              <Box
                sx={{
                  display: { xs: 'none', md: 'flex' },
                  gap: 0.5,
                  alignItems: 'center',
                }}
              >
                <ToggleColorMode mode={mode} toggleColorMode={toggleColorMode} />
                <Button color="primary" variant="text" size="small" component="a" href="#">
                  Sign in
                </Button>
                <Button color="primary" variant="contained" size="small" component="a" href="#">
                  Sign up
                </Button>
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
                    <Box
                      sx={{
                        display: 'flex',
                        flexDirection: 'column',
                        alignItems: 'end',
                        flexGrow: 1,
                      }}
                    >
                      <ToggleColorMode mode={mode} toggleColorMode={toggleColorMode} />
                    </Box>
                    <MenuItem
                      /* eslint-disable-next-line react/display-name */
                      component={React.forwardRef((props, ref) => (
                        <Link to="/exercises" {...props} ref={ref} />
                      ))}
                    >
                      Exercises
                    </MenuItem>
                    <Divider />
                    <MenuItem>
                      <Button
                        color="primary"
                        variant="contained"
                        component="a"
                        href="#"
                        sx={{ width: '100%' }}
                      >
                        Sign up
                      </Button>
                    </MenuItem>
                    <MenuItem>
                      <Button
                        color="primary"
                        variant="outlined"
                        component="a"
                        href="#"
                        sx={{ width: '100%' }}
                      >
                        Sign in
                      </Button>
                    </MenuItem>
                  </Box>
                </Drawer>
              </Box>
            </Toolbar>
          </AppBar>
          <Routes>
            <Route path="/" element={<RootPage />} />
            <Route path="/exercises" element={<ExerciseOverviewPage />} />
            <Route path="/exercises/:exerciseName" element={<ExerciseDetailsPage />} />
          </Routes>
        </Container>
      </BrowserRouter>
    </ThemeProvider>
  );
} // end component App
