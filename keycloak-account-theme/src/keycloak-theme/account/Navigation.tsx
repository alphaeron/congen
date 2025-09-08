import React from 'react';
import { AppBar, Toolbar, Container, Box, Typography, Button, Avatar, Menu, MenuItem, IconButton } from '@mui/material';
import { AccountCircle, Security, Person, Logout } from '@mui/icons-material';
import { alpha } from '@mui/material/styles';
import type { KcContext } from './KcContext';

interface NavigationProps {
  kcContext: KcContext;
  currentPage?: string;
  onPageChange?: (page: string) => void;
}

export default function Navigation({ kcContext, currentPage = 'personal-info', onPageChange }: NavigationProps) {
  const [anchorEl, setAnchorEl] = React.useState<null | HTMLElement>(null);
  const open = Boolean(anchorEl);

  const handleClick = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleClose = () => {
    setAnchorEl(null);
  };

  const handleLogout = () => {
    window.location.href = kcContext.authUrl + '/realms/' + kcContext.realm?.name + '/protocol/openid-connect/logout';
  };

  const navigationItems = [
    { id: 'personal-info', label: 'Personal info', icon: <Person />, url: kcContext.authUrl + '/realms/' + kcContext.realm?.name + '/account' },
    { id: 'account-security', label: 'Account security', icon: <Security />, url: kcContext.authUrl + '/realms/' + kcContext.realm?.name + '/account/password' },
  ];

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

            <Box sx={{ display: 'flex', gap: 1, ml: 4 }}>
              {navigationItems.map((item) => (
                <Button
                  key={item.id}
                  variant={currentPage === item.id ? 'contained' : 'text'}
                  startIcon={item.icon}
                  onClick={() => onPageChange?.(item.id)}
                  sx={{
                    borderRadius: '12px',
                    textTransform: 'none',
                    fontWeight: 500,
                    px: 2,
                    py: 1,
                  }}
                >
                  {item.label}
                </Button>
              ))}
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
              <Avatar sx={{ width: 32, height: 32, bgcolor: 'primary.main' }}>
                <AccountCircle />
              </Avatar>
            </IconButton>
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
                <Avatar /> Profile
              </MenuItem>
              <MenuItem onClick={handleLogout}>
                <Logout sx={{ mr: 1 }} /> Logout
              </MenuItem>
            </Menu>
          </Box>
        </Toolbar>
      </Container>
    </AppBar>
  );
}
