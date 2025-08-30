import {
  Box,
  Card,
  CardContent,
  Typography,
  Grid,
  CircularProgress,
  List,
  ListItem,
  ListItemText,
  Breadcrumbs,
  Link,
  Slide,
} from '@mui/material';
import { useSnackbar } from 'notistack';
import React, { useState, useEffect, useCallback, useMemo } from 'react';
import { useNavigate, useSearchParams } from 'react-router';

import { WorkoutDetail } from './WorkoutDetail';
import { getProgramsWithPreferences } from '../api/program';
import { getProgrammedWorkouts } from '../api/programmedWorkout';
import type { Program, ProgrammedWorkout, ProgramPreferences } from '../api/types';

interface WorkoutWeekDetailsProps {
  selectedWorkout?: string | null;
  weekNumber: number;
}

/**
 * WorkoutWeekDetails component for viewing workouts grouped by week.
 *
 * Features:
 * - Display workouts for a specific week
 * - Generate new workouts for the week
 * - View workout details with slide-left animation
 * - Auto-refresh functionality after workout generation
 * - URL query parameters for workout selection
 * - Breadcrumb navigation with week number
 *
 * @param selectedWorkout The selected workout ID (from URL)
 * @param weekNumber The week number to display
 * @returns WorkoutWeekDetails component
 */
export const WorkoutWeekDetails: React.FC<WorkoutWeekDetailsProps> = ({ 
  selectedWorkout, 
  weekNumber 
}) => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { enqueueSnackbar } = useSnackbar();

  const [programsWithPreferences, setProgramsWithPreferences] = useState<Array<ProgramPreferences>>([]);
  const [workouts, setWorkouts] = useState<ProgrammedWorkout[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [currentWorkoutDetails, setCurrentWorkoutDetails] = useState<{
    name: string;
    day_number: number;
    stages: number;
  } | null>(null);

  // URL query parameters
  const selectedWorkoutId = searchParams.get('workout') || selectedWorkout;

  // Reset workout details when workout selection changes
  useEffect(() => {
    if (!selectedWorkoutId) {
      setCurrentWorkoutDetails(null);
    }
  }, [selectedWorkoutId]);

  // Load workout data
  useEffect(() => {
    const loadWorkoutData = async () => {
      setIsLoading(true);
      try {
        const [programsData, workoutsData] = await Promise.all([
          getProgramsWithPreferences(),
          getProgrammedWorkouts(),
        ]);

        setProgramsWithPreferences(programsData);
        setWorkouts(workoutsData);
      } catch {
        enqueueSnackbar('Failed to load workout data. Please try again.', { variant: 'error' });
      } finally {
        setIsLoading(false);
      }
    };

    loadWorkoutData();
  }, []); // Only load once on mount

  const activeProgram = programsWithPreferences.find(program => program.is_active);
  
  // Group workouts by week and filter for the current week
  const weekWorkouts = useMemo(() => {
    if (!activeProgram) return [];
    
    const programWorkouts = workouts.filter(
      workout => workout.program_id === activeProgram.id
    );
    
    // Use program preferences
    const workoutsPerWeek = activeProgram.program_preferences.program_days_per_week;
    
    return programWorkouts
      .map(workout => {
        const weekNum = Math.ceil(workout.day_number / workoutsPerWeek);
        const dayInWeek = ((workout.day_number - 1) % workoutsPerWeek) + 1;
        return { workout, weekNumber: weekNum, dayInWeek };
      })
      .filter(weekWorkout => weekWorkout.weekNumber === weekNumber)
      .sort((a, b) => a.dayInWeek - b.dayInWeek);
  }, [workouts, activeProgram, weekNumber]);

  const handleWorkoutClick = (workoutId: number) => {
    const newSearchParams = new URLSearchParams(searchParams);
    newSearchParams.set('workout', workoutId.toString());
    navigate(`?${newSearchParams.toString()}`);
  };

  const handleBackToWorkouts = () => {
    const newSearchParams = new URLSearchParams(searchParams);
    newSearchParams.delete('workout');
    newSearchParams.delete('week');
    navigate(`?${newSearchParams.toString()}`);
  };

  const handleBackToWeekList = () => {
    const newSearchParams = new URLSearchParams(searchParams);
    newSearchParams.delete('workout');
    navigate(`?${newSearchParams.toString()}`);
  };

  // Render breadcrumbs
  const renderBreadcrumbs = () => (
    <Box
      position="sticky"
      top={0}
      zIndex={1001}
      sx={{
        backgroundColor: 'background.default',
        pt: 2,
        pb: 2,
        borderBottom: 1,
        borderColor: 'divider',
      }}
    >
      <Breadcrumbs sx={{ mb: 2 }}>
        <Link
          component="button"
          variant="body1"
          onClick={() => handleBreadcrumbClick('workouts')}
          sx={{ color: 'text.secondary' }}
        >
          Workouts
        </Link>
        <Link
          component="button"
          variant="body1"
          onClick={() => handleBreadcrumbClick('week')}
          sx={{ color: 'text.secondary' }}
        >
          Week {weekNumber}
        </Link>
        {selectedWorkoutId && currentWorkoutDetails && (
          <Typography variant="body1" color="text.primary">
            {currentWorkoutDetails.name} (Day {currentWorkoutDetails.day_number}) •{' '}
            {currentWorkoutDetails.stages} stages
          </Typography>
        )}
        {selectedWorkoutId && !currentWorkoutDetails && (
          <Typography variant="body1" color="text.primary">
            Workout Details
          </Typography>
        )}
      </Breadcrumbs>
    </Box>
  );

  const handleBreadcrumbClick = (path: string) => {
    if (path === 'workouts') {
      handleBackToWorkouts();
    } else if (path === 'week') {
      handleBackToWeekList();
    }
  };

  const handleWorkoutDetailsUpdate = useCallback(
    (workoutDetails: { name: string; day_number: number; stages: number }) => {
      setCurrentWorkoutDetails(workoutDetails);
    },
    []
  );

  return (
    <React.Fragment>
      {renderBreadcrumbs()}
      {!activeProgram ? (
        <Card>
          <CardContent>
            <Typography variant="h6" gutterBottom>
              No Active Program
            </Typography>
            <Typography variant="body2" color="text.secondary" paragraph>
              You need to create a program first before you can generate and view workouts. Please
              go to the Programs section to create a program.
            </Typography>
          </CardContent>
        </Card>
      ) : (
        <Box sx={{ display: 'flex', gap: 3 }}>
          {/* Workout List - Slides right when workout is selected */}
          <Slide direction="right" in={!selectedWorkoutId} mountOnEnter unmountOnExit>
            <Box sx={{ flex: 1, minWidth: 0 }}>
              <Grid container spacing={3}>
                {/* Week Overview and Generation */}
                <Grid item xs={12}>
                  <Card>
                    <CardContent>
                      <Box>
                        <Typography variant="h6" gutterBottom>
                          {activeProgram.name} - Week {weekNumber}
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                          {weekWorkouts.length} workouts • Week {weekNumber} of {activeProgram.current_week_number}
                        </Typography>
                      </Box>
                    </CardContent>
                  </Card>
                </Grid>

                {/* Week Workout List */}
                <Grid item xs={12}>
                  <Card>
                    <CardContent>
                      <Typography variant="h6" gutterBottom>
                        Week {weekNumber} Workouts
                      </Typography>
                      {isLoading ? (
                        <Box display="flex" justifyContent="center" p={3}>
                          <CircularProgress />
                        </Box>
                      ) : weekWorkouts.length === 0 ? (
                        <Typography variant="body2" color="text.secondary">
                          No workouts found for Week {weekNumber}.
                        </Typography>
                      ) : (
                        <List>
                          {weekWorkouts.map((weekWorkout) => (
                            <ListItem
                              key={weekWorkout.workout.id}
                              disablePadding
                              sx={{
                                cursor: 'pointer',
                                '&:hover': { backgroundColor: 'action.hover' },
                              }}
                              onClick={() => handleWorkoutClick(weekWorkout.workout.id)}
                            >
                              <ListItemText
                                primary={`Day ${weekWorkout.dayInWeek}`}
                                secondary={`${weekWorkout.workout.name || `Workout ${weekWorkout.workout.day_number}`}`}
                              />
                            </ListItem>
                          ))}
                        </List>
                      )}
                    </CardContent>
                  </Card>
                </Grid>
              </Grid>
            </Box>
          </Slide>

          {/* Workout Details - Slides in from left when workout is selected */}
          {selectedWorkoutId && (
            <Slide direction="left" in={!!selectedWorkoutId} mountOnEnter unmountOnExit>
              <Box sx={{ flex: 1, minWidth: 0 }}>
                <WorkoutDetail
                  workoutId={parseInt(selectedWorkoutId)}
                  onBack={handleBackToWeekList}
                  onWorkoutDetailsUpdate={handleWorkoutDetailsUpdate}
                />
              </Box>
            </Slide>
          )}
        </Box>
      )}

    </React.Fragment>
  );
};
