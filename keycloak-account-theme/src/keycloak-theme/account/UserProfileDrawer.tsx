import React from 'react';
import { motion } from 'framer-motion';
import {
  Person as PersonIcon,
  PrivacyTip as PrivacyIcon,
  FitnessCenter as FitnessIcon,
} from '@mui/icons-material';
import {
  Box,
  Drawer,
  List,
  ListItem,
  ListItemButton,
  ListItemIcon,
  ListItemText,
} from '@mui/material';
import type { KcContext } from './KcContext';
import { navigateToFrontend } from './utils';
import { GameText } from '../../components/GameTheme';
import { HoverScale } from '../../components/AnimatedWrapper';

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
  currentSection,
  onSectionChange,
}) => {
  const menuItems = [
    {
      id: 'physical',
      label: 'Physical Attributes',
      icon: <FitnessIcon />,
      isExternal: true,
      path: '/profile?section=physical',
    },
    {
      id: 'privacy',
      label: 'Privacy & Data',
      icon: <PrivacyIcon />,
      isExternal: true,
      path: '/profile?section=privacy',
    },
    {
      id: 'personal-info',
      label: 'Manage Profile',
      icon: <PersonIcon />,
      isExternal: false,
    },
  ];

  const handleItemClick = (item: (typeof menuItems)[0]) => {
    if (item.isExternal) {
      // Open in the same window to maintain the user experience
      navigateToFrontend(item.path);
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
        <motion.div
          initial={{ opacity: 0, x: -30 }}
          animate={{ opacity: 1, x: 0 }}
          transition={{ duration: 0.6, ease: 'easeOut', delay: 0.2 }}
          style={{ 
            padding: '16px', 
            borderBottom: '1px solid',
            borderColor: 'var(--mui-palette-divider)', 
            flexShrink: 0,
          }}
        >
          <motion.div
            animate={{
              scale: [1, 1.02, 1],
              opacity: [1, 0.8, 1]
            }}
            transition={{
              duration: 2,
              repeat: Infinity,
              ease: 'easeInOut'
            }}
          >
            <GameText 
              variant="h6" 
              textVariant="glow"
            >
              User Profile
            </GameText>
          </motion.div>
        </motion.div>
        <List sx={{ flex: 1, overflow: 'auto' }}>
          {menuItems.map((item, index) => (
            <ListItem key={item.id} disablePadding>
              <motion.div
                initial={{ opacity: 0, x: -30 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ duration: 0.6, ease: 'easeOut', delay: 0.3 + index * 0.1 }}
                whileHover={{ x: 2 }}
                whileTap={{ scale: 0.98 }}
                style={{ width: '100%' }}
              >
                <HoverScale>
                  <ListItemButton
                    selected={currentSection === item.id}
                    onClick={() => handleItemClick(item)}
                    sx={{
                      '&.Mui-selected': {
                        backgroundColor: '#00bcd4',
                        color: '#ffffff',
                        boxShadow: '0 4px 15px rgba(0, 188, 212, 0.2)',
                        '&:hover': {
                          backgroundColor: '#00acc1',
                        },
                        '& .MuiListItemIcon-root': {
                          color: '#ffffff',
                        },
                      },
                      '&:hover': {
                        backgroundColor: 'rgba(0, 188, 212, 0.05)',
                      },
                    }}
                  >
                    <ListItemIcon sx={{ minWidth: 40 }}>{item.icon}</ListItemIcon>
                    <ListItemText primary={item.label} />
                  </ListItemButton>
                </HoverScale>
              </motion.div>
            </ListItem>
          ))}
        </List>
      </Box>
    </Drawer>
  );
};
