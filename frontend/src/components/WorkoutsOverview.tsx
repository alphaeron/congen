import { Box, Tabs, Tab } from '@mui/material';
import { motion, AnimatePresence } from 'framer-motion';
import React, { useState, useEffect } from 'react';
import { useSearchParams, useNavigate } from 'react-router';

import { ExerciseRotationVisualization } from './ExerciseRotationVisualization';
import { GAME_CLASSES } from './GameTheme';
import { OneRepMaxRecords } from './OneRepMaxRecords';
import { WorkoutDetail } from './WorkoutDetail';
import { WorkoutPreferencesSection } from './WorkoutPreferencesSection';
import { Workouts } from './Workouts';
import { WorkoutWeekDetails } from './WorkoutWeekDetails';
import type { User } from '../api/types';

interface WorkoutsOverviewProps {
  user: User;
  selectedWorkout?: string | null;
}

// TabPanel component for rendering tab content
interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
  slideDirection?: 'left' | 'right';
}

const TabPanel: React.FC<TabPanelProps> = ({
  children,
  value,
  index,
  slideDirection = 'left',
  ...other
}) => {
  return (
    <div
      role="tabpanel"
      hidden={value !== index}
      id={`workout-tabpanel-${index}`}
      aria-labelledby={`workout-tab-${index}`}
      {...other}
    >
      <AnimatePresence mode="wait">
        {value === index && (
          <motion.div
            key={index}
            initial={{
              opacity: 0,
              x: slideDirection === 'left' ? 50 : -50,
            }}
            animate={{
              opacity: 1,
              x: 0,
            }}
            exit={{
              opacity: 0,
              x: slideDirection === 'left' ? -50 : 50,
            }}
            transition={{
              duration: 0.3,
              ease: 'easeInOut',
            }}
          >
            <Box sx={{ p: 0 }}>{children}</Box>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
};

export const WorkoutsOverview: React.FC<WorkoutsOverviewProps> = ({ selectedWorkout }) => {
  const [searchParams, setSearchParams] = useSearchParams();
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState(0);
  const [slideDirection, setSlideDirection] = useState<'left' | 'right'>('left');
  const [workoutsSlideDirection, setWorkoutsSlideDirection] = useState<'left' | 'right'>('left');
  const [weekDetailsSlideDirection, setWeekDetailsSlideDirection] = useState<'left' | 'right'>(
    'left'
  );
  const [workoutDetailSlideDirection, setWorkoutDetailSlideDirection] = useState<'left' | 'right'>(
    'left'
  );

  // Set initial slide direction based on current state
  useEffect(() => {
    const selectedWeek = searchParams.get('week');
    const selectedWorkoutId = searchParams.get('workout');

    // Only set direction on initial load, not on every URL change
    if (!selectedWeek && !selectedWorkoutId) {
      setWorkoutsSlideDirection('left'); // Default to left for initial load
    }
  }, []); // Empty dependency array - only run on mount

  // Handle tab changes and update URL parameters
  const handleTabChange = (event: React.SyntheticEvent, newValue: number) => {
    // Set slide direction based on tab navigation
    if (newValue > activeTab) {
      setSlideDirection('left'); // Moving forward to next tab
    } else {
      setSlideDirection('right'); // Moving backward to previous tab
    }

    setActiveTab(newValue);

    // Update URL parameters based on tab selection
    const newSearchParams = new URLSearchParams(searchParams);
    newSearchParams.set('section', 'workouts');

    switch (newValue) {
      case 0:
        // Workout Calendar - remove subsection and category
        newSearchParams.delete('subsection');
        newSearchParams.delete('category');
        newSearchParams.delete('exercise');
        break;
      case 1:
        // Exercise Rotation
        newSearchParams.set('subsection', 'rotation');
        // Keep category and exercise if they exist
        break;
      case 2:
        // 1RM Records
        newSearchParams.set('subsection', 'records');
        newSearchParams.delete('category');
        newSearchParams.delete('exercise');
        break;
      case 3:
        // Workout Preferences
        newSearchParams.set('subsection', 'preferences');
        newSearchParams.delete('category');
        newSearchParams.delete('exercise');
        break;
    }

    setSearchParams(newSearchParams);
  };

  // Sync active tab with URL parameters
  useEffect(() => {
    const subsection = searchParams.get('subsection');
    let newTabIndex = 0;

    switch (subsection) {
      case 'rotation':
        newTabIndex = 1;
        break;
      case 'records':
        newTabIndex = 2;
        break;
      case 'preferences':
        newTabIndex = 3;
        break;
      default:
        newTabIndex = 0;
        break;
    }

    // Set slide direction based on tab navigation
    if (newTabIndex > activeTab) {
      setSlideDirection('left'); // Moving forward to next tab
    } else if (newTabIndex < activeTab) {
      setSlideDirection('right'); // Moving backward to previous tab
    }

    setActiveTab(newTabIndex);
  }, [searchParams, activeTab]);

  return (
    <React.Fragment>
      {/* Workout Tabs */}
      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        transition={{ duration: 0.8, ease: 'easeOut' }}
        style={{
          borderBottom: '1px solid',
          borderColor: 'var(--mui-palette-divider)',
        }}
      >
        <Tabs
          value={activeTab}
          onChange={handleTabChange}
          aria-label="workout sections"
          variant="standard"
          scrollButtons={false}
          className={GAME_CLASSES.tabs}
          sx={{
            '& .MuiTabs-flexContainer': {
              flexWrap: 'nowrap',
            },
            '& .MuiTab-root': {
              minWidth: 'auto',
              flexShrink: 0,
              '&:hover': {
                transform: 'translateY(-2px)',
                backgroundColor: 'rgba(0, 188, 212, 0.1)',
                boxShadow: '0 4px 15px rgba(0, 188, 212, 0.2)',
              },
              '&.Mui-selected': {
                color: '#00bcd4',
                textShadow: '0 0 8px rgba(0, 188, 212, 0.5)',
              },
            },
            '& .MuiTabs-indicator': {
              backgroundColor: '#00bcd4',
              boxShadow: '0 0 10px rgba(0, 188, 212, 0.5)',
            },
          }}
        >
          <Tab label="Workout Calendar" id="workout-tab-0" aria-controls="workout-tabpanel-0" />
          <Tab label="Exercise Rotation" id="workout-tab-1" aria-controls="workout-tabpanel-1" />
          <Tab label="1RM Records" id="workout-tab-2" aria-controls="workout-tabpanel-2" />
          <Tab label="Workout Preferences" id="workout-tab-3" aria-controls="workout-tabpanel-3" />
        </Tabs>
      </motion.div>

      {/* Tab Panels */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.6, ease: 'easeOut', delay: 0.6 }}
        style={{ overflow: 'visible' }}
      >
        <TabPanel value={activeTab} index={0} slideDirection={slideDirection}>
          <AnimatePresence mode="wait">
            {(() => {
              const selectedWeek = searchParams.get('week');
              const selectedWorkoutId = searchParams.get('workout');

              // If both week and workout are selected, show WorkoutDetail
              if (selectedWeek && selectedWorkoutId) {
                return (
                  <motion.div
                    key="workout-detail"
                    initial={{ opacity: 0, x: workoutDetailSlideDirection === 'left' ? -50 : 50 }}
                    animate={{ opacity: 1, x: 0 }}
                    exit={{ opacity: 0, x: workoutDetailSlideDirection === 'left' ? 50 : -50 }}
                    transition={{ duration: 0.3, ease: 'easeInOut' }}
                  >
                    <WorkoutDetail
                      workoutId={parseInt(selectedWorkoutId)}
                      onBack={() => {
                        setWorkoutDetailSlideDirection('right'); // WorkoutDetail slides out to the right
                        setWeekDetailsSlideDirection('left'); // WorkoutWeekDetails slides in from the left (going back)
                        const newSearchParams = new URLSearchParams(searchParams);
                        newSearchParams.set('section', 'workouts');
                        newSearchParams.set('subsection', 'calendar');
                        newSearchParams.set('week', selectedWeek);
                        newSearchParams.delete('workout');
                        navigate(`/dashboard?${newSearchParams.toString()}`);
                      }}
                    />
                  </motion.div>
                );
              }

              // If only week is selected, show WorkoutWeekDetails
              if (selectedWeek) {
                return (
                  <motion.div
                    key="workout-week-details"
                    initial={{ opacity: 0, x: weekDetailsSlideDirection === 'left' ? -50 : 50 }}
                    animate={{ opacity: 1, x: 0 }}
                    exit={{ opacity: 0, x: weekDetailsSlideDirection === 'left' ? 50 : -50 }}
                    transition={{ duration: 0.3, ease: 'easeInOut' }}
                  >
                    <WorkoutWeekDetails
                      selectedWorkout={selectedWorkoutId}
                      weekNumber={parseInt(selectedWeek)}
                      showBackButton={true}
                      onBack={() => {
                        setWeekDetailsSlideDirection('right'); // WorkoutWeekDetails slides out to the right
                        setWorkoutsSlideDirection('right'); // Workouts slides in from the right
                        const newSearchParams = new URLSearchParams(searchParams);
                        newSearchParams.set('section', 'workouts');
                        newSearchParams.set('subsection', 'calendar');
                        newSearchParams.delete('week');
                        newSearchParams.delete('workout');
                        navigate(`/dashboard?${newSearchParams.toString()}`);
                      }}
                      onWorkoutClick={workoutId => {
                        setWeekDetailsSlideDirection('left'); // WorkoutWeekDetails slides out to the left
                        setWorkoutDetailSlideDirection('left'); // WorkoutDetail slides in from the left
                        const newSearchParams = new URLSearchParams(searchParams);
                        newSearchParams.set('section', 'workouts');
                        newSearchParams.set('subsection', 'calendar');
                        newSearchParams.set('week', selectedWeek);
                        newSearchParams.set('workout', workoutId.toString());
                        navigate(`/dashboard?${newSearchParams.toString()}`);
                      }}
                    />
                  </motion.div>
                );
              }

              // No week selected, show the main Workouts calendar
              return (
                <motion.div
                  key="workouts"
                  initial={{ opacity: 0, x: workoutsSlideDirection === 'left' ? -50 : 50 }}
                  animate={{ opacity: 1, x: 0 }}
                  exit={{ opacity: 0, x: workoutsSlideDirection === 'left' ? 50 : -50 }}
                  transition={{ duration: 0.3, ease: 'easeInOut' }}
                >
                  <Workouts selectedWorkout={selectedWorkout} />
                </motion.div>
              );
            })()}
          </AnimatePresence>
        </TabPanel>

        <TabPanel value={activeTab} index={1} slideDirection={slideDirection}>
          <ExerciseRotationVisualization />
        </TabPanel>

        <TabPanel value={activeTab} index={2} slideDirection={slideDirection}>
          <OneRepMaxRecords />
        </TabPanel>

        <TabPanel value={activeTab} index={3} slideDirection={slideDirection}>
          <WorkoutPreferencesSection />
        </TabPanel>
      </motion.div>
    </React.Fragment>
  );
};
