import React, { useState } from 'react';
import Avatar from '@mui/material/Avatar';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import Divider from '@mui/material/Divider';
import IconButton from '@mui/material/IconButton';
import ListItemIcon from '@mui/material/ListItemIcon';
import Menu from '@mui/material/Menu';
import MenuItem from '@mui/material/MenuItem';
import Tooltip from '@mui/material/Tooltip';
import Typography from '@mui/material/Typography';
import { default as AccountCircleIcon } from '@mui/icons-material/AccountCircle';
import { default as LogoutIcon } from '@mui/icons-material/Logout';
import { default as PersonIcon } from '@mui/icons-material/Person';
import { default as SettingsIcon } from '@mui/icons-material/Settings';

import { useAuth } from '../contexts/AuthContext';
import { AuthorizedElement } from './AuthorizedElement';

/**
 * User profile component.
 *
 * Displays user information and provides logout functionality.
 * Shows a menu with user details, account management, and logout option.
 *
 * @return User profile component
 */
export const UserProfile: React.FC = () => {
  const { logout, user } = useAuth();
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const open = Boolean(anchorEl);

  const handleClick = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleClose = () => {
    setAnchorEl(null);
  };

  const handleLogout = () => {
    handleClose();
    logout();
  };

  const handleAccountManagement = () => {
    handleClose();
    // Account management is not directly available in react-oidc-context
    // You may need to implement this differently
  };

  // Extract user information from our user object
  const username = user?.name || 'User';
  const name = user?.name || username;
  const groups = user?.groups || [];
  const roles = user?.roles || [];

  return (
    <AuthorizedElement requireAuth={true}>
      <Box sx={{ display: 'flex', alignItems: 'center', textAlign: 'center' }}>
        <Tooltip title="Account settings">
          <IconButton
            onClick={handleClick}
            size="small"
            sx={{ ml: 2 }}
            aria-controls={open ? 'account-menu' : undefined}
            aria-haspopup="true"
            aria-expanded={open ? 'true' : undefined}
          >
            <Avatar sx={{ width: 32, height: 32 }}>
              <AccountCircleIcon />
            </Avatar>
          </IconButton>
        </Tooltip>
      </Box>
      <Menu
        anchorEl={anchorEl}
        id="account-menu"
        open={open}
        onClose={handleClose}
        onClick={handleClose}
        PaperProps={{
          elevation: 0,
          sx: {
            overflow: 'visible',
            filter: 'drop-shadow(0px 2px 8px rgba(0,0,0,0.32))',
            mt: 1.5,
            '& .MuiAvatar-root': {
              width: 32,
              height: 32,
              ml: -0.5,
              mr: 1,
            },
            '&:before': {
              content: '""',
              display: 'block',
              position: 'absolute',
              top: 0,
              right: 14,
              width: 10,
              height: 10,
              bgcolor: 'background.paper',
              transform: 'translateY(-50%) rotate(45deg)',
              zIndex: 0,
            },
          },
        }}
        transformOrigin={{ horizontal: 'right', vertical: 'top' }}
        anchorOrigin={{ horizontal: 'right', vertical: 'bottom' }}
      >
        <MenuItem onClick={handleClose}>
          <ListItemIcon>
            <PersonIcon fontSize="small" />
          </ListItemIcon>
          <Box>
            <Typography variant="body2" fontWeight="bold">
              {name}
            </Typography>
            {(groups.length > 0 || roles.length > 0) && (
              <Typography variant="caption" color="text.secondary">
                {[...groups, ...roles].join(', ')}
              </Typography>
            )}
          </Box>
        </MenuItem>
        <Divider />
        {false && (
          <MenuItem onClick={handleAccountManagement}>
            <ListItemIcon>
              <SettingsIcon fontSize="small" />
            </ListItemIcon>
            Account Settings
          </MenuItem>
        )}
        <MenuItem onClick={handleLogout}>
          <ListItemIcon>
            <LogoutIcon fontSize="small" />
          </ListItemIcon>
          Logout
        </MenuItem>
      </Menu>
    </AuthorizedElement>
  );
};
