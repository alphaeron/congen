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
  Container,
} from '@mui/material';
import { motion } from 'framer-motion';
import React from 'react';
import { useNavigate, useSearchParams } from 'react-router';

import { GameText, GAME_CLASSES } from './GameTheme';
import { GdprComplianceSection } from './GdprComplianceSection';
import { PhysicalAttributesSection } from './PhysicalAttributesSection';

interface UserProfileProps {
  initialSection?: string;
}

/**
 * UserProfile component with sidebar navigation and URL query parameter support.
 *
 * Features:
 * - Left sidebar navigation with URL state persistence
 * - Multiple sections: Overview, Account Security
 * - URL query parameters for bookmarkable navigation
 * - Consistent layout with Dashboard component
 *
 * @param initialSection The initial section to display (from URL)
 * @returns UserProfile component
 */
export const UserProfile: React.FC<UserProfileProps> = ({ initialSection = 'physical' }) => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const activeSection = searchParams.get('section') || initialSection;

  const menuItems = [
    {
      id: 'physical',
      label: 'Physical Attributes',
      icon: <FitnessIcon />,
      component: <PhysicalAttributesSection />,
    },
    {
      id: 'privacy',
      label: 'Privacy & Data',
      icon: <PrivacyIcon />,
      component: <GdprComplianceSection />,
    },
    {
      id: 'overview',
      label: 'Manage Profile',
      icon: <PersonIcon />,
      isExternal: true,
      url: `${process.env.REACT_APP_KEYCLOAK_URL || 'http://localhost:8080'}/realms/congen/account/#/personal-info`,
    },
  ];

  const handleSectionChange = (sectionId: string) => {
    const item = menuItems.find(item => item.id === sectionId);
    if (item?.isExternal && item?.url) {
      // Open external link
      window.location.href = item.url;
    } else {
      // Handle internal navigation
      const newSearchParams = new URLSearchParams(searchParams);
      newSearchParams.set('section', sectionId);
      navigate(`?${newSearchParams.toString()}`);
    }
  };

  const currentSection =
    menuItems.find(item => item.id === activeSection && !item.isExternal) || menuItems[0];

  return (
    <motion.div
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      transition={{ duration: 0.8, ease: 'easeOut' }}
      style={{
        display: 'flex',
        height: '100%',
        position: 'relative',
        maxWidth: '100%', // Ensure it doesn't exceed container width
      }}
    >
      {/* Left Sidebar */}
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
                opacity: [1, 0.8, 1],
              }}
              transition={{
                duration: 2,
                repeat: Infinity,
                ease: 'easeInOut',
              }}
            >
              <GameText variant="h6" textVariant="glow">
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
                  style={{ width: '100%' }}
                >
                  <motion.div
                    whileHover={{ x: 2 }}
                    whileTap={{ scale: 0.98 }}
                    style={{ width: '100%' }}
                  >
                    <ListItemButton
                      selected={activeSection === item.id}
                      onClick={() => handleSectionChange(item.id)}
                      className={GAME_CLASSES.listItem}
                      sx={{
                        color: activeSection === item.id ? '#00bcd4' : 'rgba(255, 255, 255, 0.8)',
                        transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
                        '&.Mui-selected': {
                          backgroundColor: 'rgba(0, 188, 212, 0.1)',
                          color: '#00bcd4',
                          '&:hover': {
                            backgroundColor: 'rgba(0, 188, 212, 0.15)',
                          },
                        },
                        '&:hover': {
                          backgroundColor: 'rgba(0, 188, 212, 0.05)',
                        },
                      }}
                    >
                      <ListItemIcon sx={{ minWidth: 40, color: 'inherit' }}>
                        {item.icon}
                      </ListItemIcon>
                      <ListItemText
                        primary={item.label}
                        sx={{
                          '& .MuiListItemText-primary': {
                            color: 'inherit',
                            fontWeight: activeSection === item.id ? 600 : 400,
                          },
                        }}
                      />
                    </ListItemButton>
                  </motion.div>
                </motion.div>
              </ListItem>
            ))}
          </List>
        </Box>
      </Drawer>

      {/* Main Content */}
      <motion.div
        initial={{ opacity: 0, x: 30 }}
        animate={{ opacity: 1, x: 0 }}
        transition={{ duration: 0.6, ease: 'easeOut', delay: 0.4 }}
        style={{
          flexGrow: 1,
          height: '100%',
          overflow: 'auto', // Allow content to scroll if needed
          maxWidth: 'calc(100% - 240px)', // Prevent overflow
        }}
      >
        <Container maxWidth="xl" sx={{ height: '100%' }}>
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ duration: 0.6, ease: 'easeOut', delay: 0.6 }}
            style={{ padding: '24px' }}
          >
            {currentSection.component}
          </motion.div>
        </Container>
      </motion.div>
    </motion.div>
  );
};
