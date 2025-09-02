import FitnessCenterIcon from '@mui/icons-material/FitnessCenter';
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

import { LoadingSpinner } from './LoadingSpinner';
import { RadarChart } from './RadarChart';
import { SunburstChart } from './SunburstChart';
import { WorkoutDetail } from './WorkoutDetail';
import { getExercises } from '../api/exercise';
import { getExerciseMuscle } from '../api/exerciseMuscle';
import { getUserDataExport } from '../api/gdpr';
import { getProgramsWithPreferences } from '../api/program';
import type { ProgramWithPreferences, Exercise, ProgramWithWorkouts } from '../api/types';
import { getUserWeightUnitPreferences } from '../api/userWeightUnitPreference';
import type { UserWeightUnitPreference } from '../api/userWeightUnitPreference';
import { replaceUnderscoresWithSpaces } from '../common/utils';
import { useAuth } from '../contexts/AuthContext';

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
  const { user } = useAuth();

  const [programsWithPreferences, setProgramsWithPreferences] = useState<
    Array<ProgramWithPreferences>
  >([]);
  const [userDataExport, setUserDataExport] = useState<Record<string, unknown> | null>(null);
  const [exerciseData, setExerciseData] = useState<Map<string, Exercise>>(new Map());
  const [exerciseMuscleData, setExerciseMuscleData] = useState<Map<string, string[]>>(new Map());
  const [weightUnitPreferences, setWeightUnitPreferences] = useState<
    UserWeightUnitPreference[] | null
  >(null);
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
        const [programsData, exercisesData, userData, exerciseMuscleData, weightUnitData] =
          await Promise.all([
            getProgramsWithPreferences(),
            getExercises(),
            getUserDataExport(),
            getExerciseMuscle(),
            getUserWeightUnitPreferences(user?.keycloak_id || ''),
          ]);

        setProgramsWithPreferences(programsData);
        setUserDataExport(userData);
        setWeightUnitPreferences(weightUnitData || []);

        // Convert exercises array to Map for easy lookup
        const exerciseMap = new Map<string, Exercise>();
        exercisesData.forEach(exercise => {
          exerciseMap.set(exercise.name, exercise);
        });
        setExerciseData(exerciseMap);

        // Convert exercise muscle data array to Map for easy lookup
        const exerciseMuscleMap = new Map<string, string[]>();
        exerciseMuscleData.forEach(muscleData => {
          const exerciseName = muscleData.exercise_name;
          const muscleName = muscleData.muscle_name;

          if (!exerciseMuscleMap.has(exerciseName)) {
            exerciseMuscleMap.set(exerciseName, []);
          }
          exerciseMuscleMap.get(exerciseName)!.push(muscleName);
        });
        setExerciseMuscleData(exerciseMuscleMap);
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
    if (!activeProgram || !userDataExport?.training_programs?.length) return [];

    // Find the active program in the user data export
    const activeProgramData = (userDataExport.training_programs as ProgramWithWorkouts[])?.find(
      program => (program.program as Record<string, unknown>).id === activeProgram.program.id
    );

    if (!activeProgramData) return [];

    // Filter workouts for the current week
    const workoutsPerWeek = activeProgram.program_preferences.program_days_per_week;
    const weekStartDay = (weekNumber - 1) * workoutsPerWeek + 1;
    const weekEndDay = weekNumber * workoutsPerWeek;

    const weekWorkouts = activeProgramData.workouts.filter(workoutWithStages => {
      const dayNumber = workoutWithStages.workout.day_number;
      return dayNumber >= weekStartDay && dayNumber <= weekEndDay;
    });

    // Sort by day number within the week
    return weekWorkouts
      .map(workoutWithStages => {
        const dayInWeek = ((workoutWithStages.workout.day_number - 1) % workoutsPerWeek) + 1;
        return { workout: workoutWithStages, weekNumber: weekNumber, dayInWeek };
      })
      .sort((a, b) => a.dayInWeek - b.dayInWeek);
  }, [userDataExport, activeProgram, weekNumber]);

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
        <Grid container spacing={3} sx={{ height: 'calc(100vh - 200px)' }}>
          {/* Workout List and Details - Full width when workout selected, 2/3 when not */}
          <Grid size={{ xs: 12, lg: selectedWorkoutId ? 12 : 8 }}>
            <Slide direction="right" in={!selectedWorkoutId} mountOnEnter unmountOnExit>
              <Box sx={{ height: '100%' }}>
                <Card
                  sx={{
                    mt: 3,
                    height: '100%',
                    '&:hover': {
                      transform: 'none',
                      boxShadow: 'none',
                    },
                  }}
                >
                  <CardContent>
                    <Box display="flex" alignItems="center" gap={1} sx={{ mb: 2 }}>
                      <FitnessCenterIcon color="primary" />
                      <Typography variant="h6">Workouts</Typography>
                    </Box>
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
                            key={weekWorkout.workout.workout.id}
                            disablePadding
                            sx={{
                              cursor: 'pointer',
                              '&:hover': { backgroundColor: 'action.hover' },
                            }}
                            onClick={() => handleWorkoutClick(weekWorkout.workout.workout.id)}
                          >
                            <ListItemText
                              primary={`Day ${weekWorkout.dayInWeek}`}
                              secondary={`${replaceUnderscoresWithSpaces(weekWorkout.workout.workout.name || `Workout ${weekWorkout.workout.workout.day_number}`)}`}
                            />
                          </ListItem>
                        ))}
                      </List>
                    )}
                  </CardContent>
                </Card>
              </Box>
            </Slide>

            {/* Workout Details - Slides in from left when workout is selected */}
            {selectedWorkoutId && (
              <Slide direction="left" in={!!selectedWorkoutId} mountOnEnter unmountOnExit>
                <Box sx={{ height: '100%' }}>
                  <WorkoutDetail
                    workoutId={parseInt(selectedWorkoutId)}
                    onBack={handleBackToWeekList}
                    onWorkoutDetailsUpdate={handleWorkoutDetailsUpdate}
                  />
                </Box>
              </Slide>
            )}
          </Grid>

          {/* Charts - 1/3 width - Only show when no workout is selected */}
          {!selectedWorkoutId && (
            <Grid size={{ xs: 12, lg: 4 }}>
              <Box sx={{ mt: 3, display: 'flex', flexDirection: 'column', gap: 3 }}>
                {weekWorkouts.length > 0 && (
                  <SunburstChart
                    workoutData={{
                      id: `week-${weekNumber}`,
                      name: `Week ${weekNumber} Aggregated`,
                      day_number: weekNumber,
                      stages: weekWorkouts.reduce((acc, weekWorkout) => {
                        const workoutWithStages = weekWorkout.workout;
                        if (workoutWithStages.stages) {
                          return [...acc, ...workoutWithStages.stages];
                        }
                        return acc;
                      }, []),
                    }}
                    exerciseData={exerciseData}
                    exerciseMuscleData={exerciseMuscleData}
                    weightUnitPreferences={weightUnitPreferences}
                    selectedExercise="all"
                  />
                )}
                <RadarChart
                  weekWorkouts={weekWorkouts}
                  exerciseData={exerciseData}
                  title="Exercise Movement Type"
                  height={300}
                />
              </Box>
            </Grid>
          )}
        </Grid>
      )}
    </React.Fragment>
  );
};
