import React from 'react';
import {
  AppBar,
  Toolbar,
  Container,
  Box,
  Typography,
  Button,
  Avatar,
  Menu,
  MenuItem,
  IconButton,
  Divider,
} from '@mui/material';
import { AccountCircle } from '@mui/icons-material';
import { alpha } from '@mui/material/styles';
import type { KcContext } from './KcContext';

interface CongenAppBarProps {
  kcContext: KcContext;
  user?: any;
}

/**
 * CongenAppBar component that mimics the frontend app bar.
 * All navigation items link back to the frontend to maintain the user experience.
 */
export const CongenAppBar: React.FC<CongenAppBarProps> = ({ kcContext, user }) => {
  const [anchorEl, setAnchorEl] = React.useState<null | HTMLElement>(null);
  const open = Boolean(anchorEl);

  // Get the frontend URL - use environment variable or default to localhost
  const frontendUrl = process.env.REACT_APP_FRONTEND_URL || 'http://localhost:3000';

  const handleClick = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleClose = () => {
    setAnchorEl(null);
  };

  const handleLogout = () => {
    window.location.href = kcContext.authUrl + '/realms/' + kcContext.realm?.name + '/protocol/openid-connect/logout';
  };

  const handleProfileClick = () => {
    window.location.href = `${frontendUrl}/user_profile?section=privacy`;
  };

  const handleExercisesClick = () => {
    window.location.href = `${frontendUrl}/exercises`;
  };

  const handlePrivacyClick = () => {
    window.location.href = `${frontendUrl}/privacy_policy`;
  };

  const handleHomeClick = () => {
    window.location.href = `${frontendUrl}/`;
  };

  return (
    <AppBar
      position="sticky"
      sx={{
        width: '100%',
        boxShadow: 'none',
        bgcolor: 'transparent',
        backgroundImage: 'none',
      }}
    >
      <Container maxWidth="xl">
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
            mt: 2,
            mb: 2,
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
              <Typography
                variant="h6"
                noWrap={true}
                component="a"
                href="#"
                onClick={(e) => {
                  e.preventDefault();
                  handleHomeClick();
                }}
                sx={{
                  fontWeight: 700,
                  background: 'linear-gradient(135deg, #0ea5e9, #f97316)',
                  backgroundClip: 'text',
                  WebkitBackgroundClip: 'text',
                  WebkitTextFillColor: 'transparent',
                  textDecoration: 'none',
                  cursor: 'pointer',
                  '&:hover': {
                    textDecoration: 'none',
                  },
                }}
              >
                ConGen
              </Typography>
            </Box>

            <Box sx={{ display: { xs: 'none', md: 'flex' }, gap: 1, ml: 4 }}>
              <Button
                variant="text"
                onClick={handleExercisesClick}
                sx={{
                  borderRadius: '12px',
                  textTransform: 'none',
                  fontWeight: 500,
                  px: 2,
                  py: 1,
                  color: 'text.primary',
                  '&:hover': {
                    bgcolor: theme => alpha(theme.palette.primary.main, 0.08),
                  },
                }}
              >
                Exercises
              </Button>
              <Button
                variant="text"
                onClick={handlePrivacyClick}
                sx={{
                  borderRadius: '12px',
                  textTransform: 'none',
                  fontWeight: 500,
                  px: 2,
                  py: 1,
                  color: 'text.primary',
                  '&:hover': {
                    bgcolor: theme => alpha(theme.palette.primary.main, 0.08),
                  },
                }}
              >
                Privacy
              </Button>
            </Box>
          </Box>

          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <IconButton
              onClick={handleClick}
              size="small"
              sx={{ ml: 2 }}
              aria-controls={open ? 'account-menu' : undefined}
              aria-haspopup="true"
              aria-expanded={open ? 'true' : undefined}
            >
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
                {user?.firstName?.charAt(0).toUpperCase() || user?.username?.charAt(0).toUpperCase() || 'U'}
              </Avatar>
            </IconButton>
            <Menu
              anchorEl={anchorEl}
              id="account-menu"
              open={open}
              onClose={handleClose}
              onClick={handleClose}
              sx={{ zIndex: theme => theme.zIndex.drawer + 10 }}
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
              <MenuItem 
                onClick={handleProfileClick}
                sx={{
                  fontWeight: 500,
                  '&:hover': {
                    bgcolor: theme => alpha(theme.palette.primary.main, 0.08),
                  },
                }}
              >
                Profile
              </MenuItem>
              <Divider sx={{ my: 1 }} />
              <MenuItem 
                onClick={handleLogout}
                sx={{
                  fontWeight: 500,
                  color: 'error.main',
                  '&:hover': {
                    bgcolor: theme => alpha(theme.palette.error.main, 0.08),
                  },
                }}
              >
                Sign Out
              </MenuItem>
            </Menu>
          </Box>
        </Toolbar>
      </Container>
    </AppBar>
  );
};
