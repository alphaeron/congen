import {
  Dashboard as DashboardIcon,
  FitnessCenter as FitnessCenterIcon,
  Settings as SettingsIcon,
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

import { DashboardOverview } from './DashboardOverview';
import { ProgramManagement } from './ProgramManagement';
import { WorkoutsOverview } from './WorkoutsOverview';
import { GameText, GAME_CLASSES } from './GameTheme';
import type { User } from '../api/types';

interface DashboardProps {
  user: User;
  initialSection?: string;
  selectedWorkout?: string | null;
}

/**
 * Dashboard component with sidebar navigation and URL query parameter support.
 *
 * Features:
 * - Left sidebar navigation with URL state persistence
 * - Multiple sections: Overview, Programs, Workouts, Exercise History
 * - URL query parameters for bookmarkable navigation
 * - Proper drawer positioning and layout
 *
 * @param user The current user object
 * @param initialSection The initial section to display (from URL)
 * @param selectedWorkout The selected workout ID (from URL)
 * @returns Dashboard component
 */
export const Dashboard: React.FC<DashboardProps> = ({
  user,
  initialSection = 'overview',
  selectedWorkout,
}) => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const activeSection = searchParams.get('section') || initialSection;

  const menuItems = [
    {
      id: 'overview',
      label: 'Overview',
      icon: <DashboardIcon />,
      component: <DashboardOverview user={user} />,
    },
    {
      id: 'programs',
      label: 'Programs',
      icon: <SettingsIcon />,
      component: <ProgramManagement user={user} />,
    },
    {
      id: 'workouts',
      label: 'Workouts',
      icon: <FitnessCenterIcon />,
      component: <WorkoutsOverview user={user} selectedWorkout={selectedWorkout} />,
    },
  ];

  const handleSectionChange = (sectionId: string) => {
    const newSearchParams = new URLSearchParams();
    newSearchParams.set('section', sectionId);
    navigate(`?${newSearchParams.toString()}`);
  };

  const currentSection = menuItems.find(item => item.id === activeSection) || menuItems[0];

  return (
    <Box
      sx={{
        display: 'flex',
        height: '100%',
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
          <Box className={`${GAME_CLASSES.padding2} ${GAME_CLASSES.borderBottom1} ${GAME_CLASSES.borderColorDivider} ${GAME_CLASSES.flexShrink0}`}>
            <GameText variant="h6" textVariant="glow">
              Dashboard
            </GameText>
          </Box>
          <List className={`${GAME_CLASSES.flex1} ${GAME_CLASSES.overflowAuto}`}>
            {menuItems.map(item => (
              <ListItem key={item.id} disablePadding>
                <ListItemButton
                  selected={activeSection === item.id}
                  onClick={() => handleSectionChange(item.id)}
                  className={GAME_CLASSES.listItem}
                >
                  <ListItemIcon className={GAME_CLASSES.minWidth40}>{item.icon}</ListItemIcon>
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
