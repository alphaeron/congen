import { default as DashboardIcon } from '@mui/icons-material/Dashboard';
import { default as FitnessCenterIcon } from '@mui/icons-material/FitnessCenter';
import { default as TimelineIcon } from '@mui/icons-material/Timeline';
import { default as ShowChartIcon } from '@mui/icons-material/ShowChart';
import { default as CalendarTodayIcon } from '@mui/icons-material/CalendarToday';
import { default as SettingsIcon } from '@mui/icons-material/Settings';
import {
  Box,
  Container,
  Typography,
  List,
  ListItem,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  useTheme,
  useMediaQuery,
  Drawer,
  Toolbar,
} from '@mui/material';
import React, { useState, useEffect } from 'react';

import { DashboardOverview } from './DashboardOverview';
import { ProgramManagement } from './ProgramManagement';
import { WorkoutFlow } from './WorkoutFlow';
import { VisualizationPage } from './VisualizationPage';
import { WorkoutCalendar } from './WorkoutCalendar';
import { useDrawer } from '../App';
import type { User } from '../api/types';

interface DashboardProps {
  user: User;
}

/**
 * Dashboard component with modern drawer-based interface.
 *
 * Displays user dashboard with progress tracking, program management,
 * workout flow, and visualization tools using MUI Drawer for navigation.
 *
 * @param user The user data to display
 * @return Dashboard component with drawer interface
 */
export const Dashboard: React.FC<DashboardProps> = ({ user }) => {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));
  const [activeTab, setActiveTab] = useState(0);
  const { drawerOpen, setDrawerOpen, drawerWidth } = useDrawer();

  // Initialize drawer as open on desktop
  useEffect(() => {
    if (!isMobile) {
      setDrawerOpen(true);
    }
  }, [isMobile, setDrawerOpen]);

  const handleDrawerToggle = () => {
    setDrawerOpen(!drawerOpen);
  };

  const menuItems = [
    {
      label: 'Overview',
      icon: <DashboardIcon />,
      content: <DashboardOverview user={user} />,
    },
    {
      label: 'Program Management',
      icon: <FitnessCenterIcon />,
      content: <ProgramManagement user={user} />,
    },
    {
      label: 'Workout Flow',
      icon: <TimelineIcon />,
      content: <WorkoutFlow user={user} />,
    },
    {
      label: 'Visualization',
      icon: <ShowChartIcon />,
      content: <VisualizationPage user={user} />,
    },
    {
      label: 'Calendar',
      icon: <CalendarTodayIcon />,
      content: <WorkoutCalendar user={user} />,
    },
  ];

  return (
    <Box sx={{ 
      display: 'flex', 
      height: 'calc(100vh - 64px)', // Account for AppBar height
      position: 'relative',
      overflow: 'hidden', // Prevent overflow
      maxWidth: '100%', // Ensure it doesn't exceed container width
      mt: 0, // Remove any top margin since App.tsx handles it
    }}>
      {/* Drawer */}
      <Drawer
        variant={isMobile ? 'temporary' : 'permanent'}
        open={drawerOpen}
        onClose={handleDrawerToggle}
        sx={{
          width: drawerWidth,
          flexShrink: 0,
          '& .MuiDrawer-paper': {
            width: drawerWidth,
            boxSizing: 'border-box',
            position: 'relative',
            height: '100%',
            zIndex: 1,
            backgroundColor: 'background.paper',
            borderRight: `1px solid ${theme.palette.divider}`,
            overflow: 'hidden', // Prevent drawer content overflow
            mt: 0, // Ensure no top margin
          },
        }}
        ModalProps={{
          keepMounted: true, // Better open performance on mobile.
        }}
      >
        <Toolbar>
          <Typography variant="h6" noWrap component="div">
            Dashboard
          </Typography>
        </Toolbar>
        <List sx={{ overflow: 'auto', flex: 1 }}>
          {menuItems.map((item, index) => (
            <ListItem key={index} disablePadding>
              <ListItemButton
                selected={activeTab === index}
                onClick={() => {
                  setActiveTab(index);
                  if (isMobile) {
                    setDrawerOpen(false);
                  }
                }}
                sx={{
                  '&.Mui-selected': {
                    backgroundColor: 'primary.main',
                    color: 'primary.contrastText',
                    '&:hover': {
                      backgroundColor: 'primary.dark',
                    },
                  },
                }}
              >
                <ListItemIcon
                  sx={{
                    color: activeTab === index ? 'primary.contrastText' : 'inherit',
                  }}
                >
                  {item.icon}
                </ListItemIcon>
                <ListItemText primary={item.label} />
              </ListItemButton>
            </ListItem>
          ))}
        </List>
      </Drawer>

      {/* Main content */}
      <Box
        component="main"
        sx={{
          flexGrow: 1,
          p: 3,
          width: { sm: `calc(100% - ${drawerWidth}px)` },
          minHeight: '100%',
          overflow: 'auto', // Allow content to scroll if needed
          maxWidth: `calc(100% - ${drawerOpen ? drawerWidth : 0}px)`, // Prevent overflow
        }}
      >
        <Container maxWidth="xl" sx={{ height: '100%' }}>
          {menuItems[activeTab]?.content}
        </Container>
      </Box>
    </Box>
  );
};
