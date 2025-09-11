import React from 'react';
import {
  AppBar,
  Avatar,
  Box,
  Divider,
  IconButton,
  Menu,
  MenuItem,
  Toolbar,
  Typography,
} from '@mui/material';
import { alpha } from '@mui/material/styles';
import type { KcContext } from './KcContext';
import { handleLogout, navigateToFrontend } from './utils';

import ConGenIcon from '../../resources/congen-icon.svg';

interface CongenAppBarProps {
  kcContext: KcContext;
  user?: Record<string, unknown>;
}

/**
 * CongenAppBar component that mimics the frontend app bar.
 * All navigation items link back to the frontend to maintain the user experience.
 */
export const CongenAppBar: React.FC<CongenAppBarProps> = ({ kcContext, user }) => {
  const [anchorEl, setAnchorEl] = React.useState<null | HTMLElement>(null);

  const handleClick = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleClose = () => {
    setAnchorEl(null);
  };

  const handleHomeClick = () => {
    navigateToFrontend('/');
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
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <img
              src={ConGenIcon}
              alt="ConGen"
              style={{
                width: '72px',
                height: 'auto',
                cursor: 'pointer',
              }}
              onClick={handleHomeClick}
            />
            <Typography
              variant="h6"
              className="menuLogo menuLogoText"
              noWrap={true}
              component="a"
              href="#"
              onClick={e => {
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

          <Box sx={{ display: { xs: 'none', md: 'flex' }, gap: 1 }}>
            <MenuItem
              sx={{
                py: 1,
                px: 2,
                borderRadius: 2,
                fontWeight: 500,
                cursor: 'pointer',
                '&:hover': {
                  bgcolor: theme => alpha(theme.palette.primary.main, 0.08),
                },
              }}
              component="a"
              onClick={e => {
                e.preventDefault();
                navigateToFrontend('/exercises');
              }}
            >
              Exercises
            </MenuItem>
            <MenuItem
              sx={{
                py: 1,
                px: 2,
                borderRadius: 2,
                fontWeight: 500,
                cursor: 'pointer',
                '&:hover': {
                  bgcolor: theme => alpha(theme.palette.primary.main, 0.08),
                },
              }}
              component="a"
              onClick={e => {
                e.preventDefault();
                navigateToFrontend('/privacy_policy');
              }}
            >
              Privacy
            </MenuItem>
          </Box>
        </Box>

        <Box
          sx={{
            display: { xs: 'none', md: 'flex' },
            gap: 1,
            alignItems: 'center',
          }}
        >
          <IconButton
            onClick={handleClick}
            sx={{
              p: 1,
              borderRadius: '50%',
              '&:hover': {
                bgcolor: theme => alpha(theme.palette.primary.main, 0.08),
              },
            }}
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
              {user?.firstName?.charAt(0).toUpperCase() ||
                user?.username?.charAt(0).toUpperCase() ||
                'U'}
            </Avatar>
          </IconButton>
          <Menu
            anchorEl={anchorEl}
            open={Boolean(anchorEl)}
            onClose={handleClose}
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
              component="a"
              onClick={e => {
                e.preventDefault();
                handleClose();
                navigateToFrontend('/user_profile?section=privacy');
              }}
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
              onClick={() => {
                handleClose();
                handleLogout(kcContext);
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
            </MenuItem>
          </Menu>
        </Box>
      </Toolbar>
    </AppBar>
  );
};
