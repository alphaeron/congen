import React from 'react';
import {
  Person as PersonIcon,
  PrivacyTip as PrivacyIcon,
} from '@mui/icons-material';
import {
  Box,
  Drawer,
  List,
  ListItem,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Typography,
} from '@mui/material';
import type { KcContext } from './KcContext';

interface UserProfileDrawerProps {
  kcContext: KcContext;
  currentSection: string;
  onSectionChange: (sectionId: string) => void;
}

/**
 * UserProfileDrawer component that mimics the frontend user profile drawer.
 * All items except "Profile Overview" link back to the frontend.
 */
export const UserProfileDrawer: React.FC<UserProfileDrawerProps> = ({
  kcContext,
  currentSection,
  onSectionChange,
}) => {
  // Get the frontend URL - use environment variable or default to localhost
  const frontendUrl = process.env.REACT_APP_FRONTEND_URL || 'http://localhost:3000';

  const menuItems = [
    {
      id: 'privacy',
      label: 'Privacy & Data',
      icon: <PrivacyIcon />,
      isExternal: true,
      url: `${frontendUrl}/user_profile?section=privacy`,
    },
    {
      id: 'overview',
      label: 'Manage Profile',
      icon: <PersonIcon />,
      isExternal: false,
    },
  ];

  const handleItemClick = (item: typeof menuItems[0]) => {
    if (item.isExternal) {
      // Open in the same window to maintain the user experience
      window.location.href = item.url;
    } else {
      onSectionChange(item.id);
    }
  };

  return (
    <Drawer
      variant="permanent"
      sx={{
        width: 240,
        flexShrink: 0,
        '& .MuiDrawer-paper': {
          width: 240,
          boxSizing: 'border-box',
          position: 'relative',
          height: '100%',
          zIndex: 1,
          backgroundColor: 'background.paper',
          borderRight: 1,
          borderColor: 'divider',
          overflow: 'hidden', // Prevent drawer content overflow
          borderRadius: 0, // Remove rounded corners
          top: 0, // Position at the top of the container
        },
      }}
    >
      <Box
        sx={{
          display: 'flex',
          flexDirection: 'column',
          height: '100%',
        }}
      >
        <Box sx={{ p: 2, borderBottom: 1, borderColor: 'divider', flexShrink: 0 }}>
          <Typography variant="h6" color="primary">
            User Profile
          </Typography>
        </Box>
        <List sx={{ flex: 1, overflow: 'auto' }}>
          {menuItems.map(item => (
            <ListItem key={item.id} disablePadding>
              <ListItemButton
                selected={currentSection === item.id}
                onClick={() => handleItemClick(item)}
                sx={{
                  '&.Mui-selected': {
                    backgroundColor: 'primary.main',
                    color: 'primary.contrastText',
                    '&:hover': {
                      backgroundColor: 'primary.dark',
                    },
                    '& .MuiListItemIcon-root': {
                      color: 'primary.contrastText',
                    },
                  },
                }}
              >
                <ListItemIcon sx={{ minWidth: 40 }}>{item.icon}</ListItemIcon>
                <ListItemText primary={item.label} />
              </ListItemButton>
            </ListItem>
          ))}
        </List>
      </Box>
    </Drawer>
  );
};
