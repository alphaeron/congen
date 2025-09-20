import { Add as AddIcon, Tune as TuneIcon, RotateRight as RotateRightIcon } from '@mui/icons-material';
import {
  Box,
  Tabs,
  Tab,
  Slide,
} from '@mui/material';
import React, { useState, useEffect } from 'react';
import { useSearchParams, useNavigate } from 'react-router';

import { ConjugateProgression } from './ConjugateProgression';
import { ExerciseRotationVisualization } from './ExerciseRotationVisualization';
import { WorkoutPreferencesSection } from './WorkoutPreferencesSection';
import { Workouts } from './Workouts';
import { WorkoutWeekDetails } from './WorkoutWeekDetails';
import { WorkoutDetail } from './WorkoutDetail';
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
}

const TabPanel: React.FC<TabPanelProps> = ({ children, value, index, ...other }) => {
  return (
    <div
      role="tabpanel"
      hidden={value !== index}
      id={`workout-tabpanel-${index}`}
      aria-labelledby={`workout-tab-${index}`}
      {...other}
    >
      {value === index && <Box sx={{ p: 0 }}>{children}</Box>}
    </div>
  );
};

export const WorkoutsOverview: React.FC<WorkoutsOverviewProps> = ({
  user,
  selectedWorkout,
}) => {
  const [searchParams, setSearchParams] = useSearchParams();
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState(0);
  const [workoutsSlideDirection, setWorkoutsSlideDirection] = useState<'left' | 'right'>('left');
  const [weekDetailsSlideDirection, setWeekDetailsSlideDirection] = useState<'left' | 'right'>('left');
  const [workoutDetailSlideDirection, setWorkoutDetailSlideDirection] = useState<'left' | 'right'>('left');

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
        // Conjugate Progression
        newSearchParams.set('subsection', 'progression');
        newSearchParams.delete('category');
        newSearchParams.delete('exercise');
        break;
      case 2:
        // Exercise Rotation
        newSearchParams.set('subsection', 'rotation');
        // Keep category and exercise if they exist
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
    switch (subsection) {
      case 'progression':
        setActiveTab(1);
        break;
      case 'rotation':
        setActiveTab(2);
        break;
      case 'preferences':
        setActiveTab(3);
        break;
      default:
        setActiveTab(0);
        break;
    }
  }, [searchParams]);

  return (
    <React.Fragment>
      {/* Workout Tabs */}
      <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
        <Tabs value={activeTab} onChange={handleTabChange} aria-label="workout sections">
          <Tab
            label="Workout Calendar"
            icon={<AddIcon />}
            iconPosition="start"
            id="workout-tab-0"
            aria-controls="workout-tabpanel-0"
          />
          <Tab
            label="Conjugate Progression"
            icon={<RotateRightIcon />}
            iconPosition="start"
            id="workout-tab-1"
            aria-controls="workout-tabpanel-1"
          />
          <Tab
            label="Exercise Rotation"
            icon={<RotateRightIcon />}
            iconPosition="start"
            id="workout-tab-2"
            aria-controls="workout-tabpanel-2"
          />
          <Tab
            label="Workout Preferences"
            icon={<TuneIcon />}
            iconPosition="start"
            id="workout-tab-3"
            aria-controls="workout-tabpanel-3"
          />
        </Tabs>
      </Box>

      {/* Tab Panels */}
      <TabPanel value={activeTab} index={0}>
        {(() => {
          const selectedWeek = searchParams.get('week');
          const selectedWorkoutId = searchParams.get('workout');
          
          // If both week and workout are selected, show WorkoutDetail
          if (selectedWeek && selectedWorkoutId) {
            return (
              <Slide key={`workout-detail-${workoutDetailSlideDirection}`} direction={workoutDetailSlideDirection} in={true} mountOnEnter unmountOnExit>
                <Box>
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
                </Box>
              </Slide>
            );
          }
          
          // If only week is selected, show WorkoutWeekDetails
          if (selectedWeek) {
            return (
              <Slide key={`workout-week-${weekDetailsSlideDirection}`} direction={weekDetailsSlideDirection} in={true} mountOnEnter unmountOnExit>
                <Box>
                  <WorkoutWeekDetails
                    user={user}
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
                    onWorkoutClick={(workoutId) => {
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
                </Box>
              </Slide>
            );
          }
          
          // No week selected, show the main Workouts calendar
          return (
            <Slide key={`workouts-${workoutsSlideDirection}`} direction={workoutsSlideDirection} in={true} mountOnEnter unmountOnExit>
              <Box>
                <Workouts user={user} selectedWorkout={selectedWorkout} />
              </Box>
            </Slide>
          );
        })()}
      </TabPanel>

      <TabPanel value={activeTab} index={1}>
        <ConjugateProgression
          user={user}
        />
      </TabPanel>

      <TabPanel value={activeTab} index={2}>
        <ExerciseRotationVisualization />
      </TabPanel>

      <TabPanel value={activeTab} index={3}>
        <WorkoutPreferencesSection />
      </TabPanel>
    </React.Fragment>
  );
};
