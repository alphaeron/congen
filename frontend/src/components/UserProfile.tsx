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
import React from 'react';
import { useNavigate, useSearchParams } from 'react-router';

import { GdprComplianceSection } from './GdprComplianceSection';
import { PhysicalAttributesSection } from './PhysicalAttributesSection';
import { GameText, GameSidebar, GAME_CLASSES } from './GameTheme';

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
    <Box
      sx={{
        display: 'flex',
        height: '100vh', // Use full viewport height
        position: 'relative',
        overflow: 'hidden', // Prevent overflow
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
          <Box sx={{ p: 2, borderBottom: 1, borderColor: 'divider', flexShrink: 0 }}>
            <GameText variant="h6" textVariant="glow">
              User Profile
            </GameText>
          </Box>
          <List sx={{ flex: 1, overflow: 'auto' }}>
            {menuItems.map(item => (
              <ListItem key={item.id} disablePadding>
                <ListItemButton
                  selected={activeSection === item.id}
                  onClick={() => handleSectionChange(item.id)}
                  className={GAME_CLASSES.listItem}
                >
                  <ListItemIcon sx={{ minWidth: 40 }}>{item.icon}</ListItemIcon>
                  <ListItemText primary={item.label} />
                </ListItemButton>
              </ListItem>
            ))}
          </List>
        </Box>
      </Drawer>

      {/* Main Content */}
      <Box
        component="main"
        sx={{
          flexGrow: 1,
          height: '100%',
          overflow: 'auto', // Allow content to scroll if needed
          maxWidth: 'calc(100% - 240px)', // Prevent overflow
        }}
      >
        <Container maxWidth="xl" sx={{ height: '100%' }}>
          <Box sx={{ p: 3 }}>{currentSection.component}</Box>
        </Container>
      </Box>
    </Box>
  );
};
