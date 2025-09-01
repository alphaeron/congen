import {
  Box,
  Button,
  Card,
  CardContent,
  Typography,
  Grid,
  List,
  ListItem,
  ListItemText,
  Breadcrumbs,
  Slide,
} from '@mui/material';
import { useSnackbar } from 'notistack';
import React, { useState, useEffect, useCallback, useMemo } from 'react';
import { useNavigate, useSearchParams } from 'react-router';

import { WorkoutDetail } from './WorkoutDetail';
import { LoadingSpinner } from './LoadingSpinner';
import { getProgramsWithPreferences } from '../api/program';
import { getProgrammedWorkoutsByProgram } from '../api/programmedWorkout';
import type { ProgrammedWorkout, ProgramWithPreferences } from '../api/types';
import { replaceUnderscoresWithSpaces } from '../common/utils';

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
  weekNumber,
}) => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { enqueueSnackbar } = useSnackbar();

  const [programsWithPreferences, setProgramsWithPreferences] = useState<
    Array<ProgramWithPreferences>
  >([]);
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
        const programsData = await getProgramsWithPreferences();
        setProgramsWithPreferences(programsData);
        
        const activeProgram = programsData.find(program => program.program.is_active);
        if (activeProgram) {
          const workoutsData = await getProgrammedWorkoutsByProgram(activeProgram.program.id, weekNumber);
          setWorkouts(workoutsData);
        } else {
          setWorkouts([]);
        }
      } catch {
        enqueueSnackbar('Failed to load workout data. Please try again.', { variant: 'error' });
      } finally {
        setIsLoading(false);
      }
    };

    loadWorkoutData();
  }, [weekNumber]); // Reload when week number changes

  const activeProgram = programsWithPreferences.find(program => program.program.is_active);

  // Group workouts by week and filter for the current week
  const weekWorkouts = useMemo(() => {
    if (!activeProgram) return [];
    
    const programWorkouts = workouts.filter(
      workout => workout.program_id === activeProgram.program.id
    );
    
    // Since we're now getting workouts filtered by week from the backend,
    // we just need to sort them by day number within the week
    return programWorkouts
      .map(workout => {
        const workoutsPerWeek = activeProgram.program_preferences.program_days_per_week;
        const dayInWeek = ((workout.day_number - 1) % workoutsPerWeek) + 1;
        return { workout, weekNumber: weekNumber, dayInWeek };
      })
      .sort((a, b) => a.dayInWeek - b.dayInWeek);
  }, [workouts, activeProgram, weekNumber]);

  const handleWorkoutClick = (workoutId: number) => {
    const newSearchParams = new URLSearchParams(searchParams);
    newSearchParams.set('section', 'workouts');
    newSearchParams.set('week', weekNumber.toString());
    newSearchParams.set('workout', workoutId.toString());
    navigate(`/dashboard?${newSearchParams.toString()}`);
  };

  const handleBackToWorkouts = () => {
    const newSearchParams = new URLSearchParams(searchParams);
    newSearchParams.set('section', 'workouts');
    newSearchParams.delete('week');
    newSearchParams.delete('workout');
    navigate(`/dashboard?${newSearchParams.toString()}`);
  };

  const handleBackToWeekList = () => {
    const newSearchParams = new URLSearchParams(searchParams);
    newSearchParams.set('section', 'workouts');
    newSearchParams.set('week', weekNumber.toString());
    newSearchParams.delete('workout');
    navigate(`/dashboard?${newSearchParams.toString()}`);
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
        <Button
          variant="text"
          onClick={() => handleBreadcrumbClick('workouts')}
          sx={{
            color: 'text.secondary',
            textTransform: 'none',
            fontSize: '1rem',
            fontWeight: 'normal',
            p: 0,
            minWidth: 'auto',
          }}
        >
          {activeProgram?.program.name || 'Workouts'}
        </Button>
        {/* Only show Week as a clickable button if we're viewing workout details */}
        {selectedWorkoutId ? (
          <Button
            variant="text"
            onClick={() => handleBreadcrumbClick('week')}
            sx={{
              color: 'text.secondary',
              textTransform: 'none',
              fontSize: '1rem',
              fontWeight: 'normal',
              p: 0,
              minWidth: 'auto',
            }}
          >
            Week {weekNumber}
          </Button>
        ) : (
          <Typography variant="body1" color="text.primary">
            Week {weekNumber}
          </Typography>
        )}
        {selectedWorkoutId && currentWorkoutDetails && (
          <Typography variant="body1" color="text.primary">
            {currentWorkoutDetails.name}
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

  // Show loading state while data is being fetched
  if (isLoading) {
    return (
      <React.Fragment>
        {renderBreadcrumbs()}
        <LoadingSpinner message="Loading week details..." fullHeight={false} />
      </React.Fragment>
    );
  }

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
                {/* Week Workout List */}
                <Grid size={{ xs: 12 }}>
                  <Card>
                    <CardContent>
                      <Typography variant="h6" gutterBottom>
                        Workouts
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                          {weekWorkouts.length} workouts • Week {weekNumber} of{' '}
                          {activeProgram.program.current_week_number}
                        </Typography>
                      {isLoading ? (
                        <Box display="flex" justifyContent="center" p={3}>
                          <LoadingSpinner message="Loading workouts..." size={40} />
                        </Box>
                      ) : weekWorkouts.length === 0 ? (
                        <Typography variant="body2" color="text.secondary">
                          No workouts found for Week {weekNumber}.
                        </Typography>
                      ) : (
                        <List>
                          {weekWorkouts.map(weekWorkout => (
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
                                secondary={`${replaceUnderscoresWithSpaces(weekWorkout.workout.name || `Workout ${weekWorkout.workout.day_number}`)}`}
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
