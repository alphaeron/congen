import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import FitnessCenterIcon from '@mui/icons-material/FitnessCenter';
import PauseCircleIcon from '@mui/icons-material/PauseCircle';
import ScheduleIcon from '@mui/icons-material/Schedule';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Grid,
  List,
  ListItem,
  ListItemText,
  IconButton,
  Slide,
} from '@mui/material';
import { useSnackbar } from 'notistack';
import React, { useState, useEffect, useMemo } from 'react';
import { useNavigate, useSearchParams } from 'react-router';

import { ExportButtons } from './ExportButtons';
import { LoadingSpinner } from './LoadingSpinner';
import { ProgressBar } from './ProgressBar';
import { RadarChart } from './RadarChart';
import { SunburstChart } from './SunburstChart';
import { getIndividualExercise } from '../api/exercise';
import { getExerciseMuscle } from '../api/exerciseMuscle';
import { getUserDataExport } from '../api/gdpr';
import { getProgramsWithPreferences } from '../api/program';
import { getProgrammedWorkouts } from '../api/programmedWorkout';
import type {
  ProgrammedWorkout,
  User,
  ProgramWithPreferences,
  Exercise,
  UserDataExport,
  UserWeightUnitPreference,
  ProgramWithWorkouts,
  ProgrammedWorkoutWithStages,
  WorkoutStageWithExercises,
  ProgrammedExerciseWithSetSchemes,
} from '../api/types';
import { getUserWeightUnitPreferences } from '../api/userWeightUnitPreference';
import { replaceUnderscoresWithSpaces } from '../common/utils';
import { exportWeekToPDF } from '../utils/exportUtils';
import { calculateWeekProgress, calculateWorkoutProgress } from '../utils/progressUtils';

interface WorkoutWeekDetailsProps {
  user: User;
  selectedWorkout?: string | null;
  weekNumber: number;
  showBackButton?: boolean;
  onBack?: () => void;
  onWorkoutClick?: (workoutId: number) => void;
}

export const WorkoutWeekDetails: React.FC<WorkoutWeekDetailsProps> = ({
  user,
  weekNumber,
  showBackButton = true,
  onBack,
  onWorkoutClick,
}) => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { enqueueSnackbar } = useSnackbar();

  const [programsWithPreferences, setProgramsWithPreferences] = useState<
    Array<ProgramWithPreferences>
  >([]);
  const [workouts, setWorkouts] = useState<ProgrammedWorkout[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [exerciseData, setExerciseData] = useState<Map<string, Exercise>>(new Map());
  const [exerciseMuscleData, setExerciseMuscleData] = useState<Map<string, string[]>>(new Map());
  const [userDataExport, setUserDataExport] = useState<UserDataExport | null>(null);
  const [weightUnitPreferences, setWeightUnitPreferences] = useState<UserWeightUnitPreference[]>(
    []
  );

  useEffect(() => {
    const loadWeekData = async () => {
      setIsLoading(true);
      try {
        const [programsData, workoutsData, userData, exerciseMuscleDataResponse, weightUnitData] =
          await Promise.all([
            getProgramsWithPreferences(),
            getProgrammedWorkouts(),
            getUserDataExport(),
            getExerciseMuscle(),
            getUserWeightUnitPreferences(user.keycloak_id),
          ]);

        setProgramsWithPreferences(programsData);
        setWorkouts(workoutsData);
        setUserDataExport(userData);
        setWeightUnitPreferences(weightUnitData || []);

        const uniqueExercises = new Set<string>();
        (userData.training_programs as ProgramWithWorkouts[])?.forEach(program => {
          program.workouts.forEach((workoutWithStages: ProgrammedWorkoutWithStages) => {
            workoutWithStages.stages.forEach((stageWithExercises: WorkoutStageWithExercises) => {
              stageWithExercises.exercises.forEach(
                (exerciseWithSetSchemes: ProgrammedExerciseWithSetSchemes) => {
                  uniqueExercises.add(exerciseWithSetSchemes.exercise.exercise_name);
                }
              );
            });
          });
        });

        const exerciseDetailsPromises = Array.from(uniqueExercises).map(name =>
          getIndividualExercise(name)
        );
        const exerciseDetails = await Promise.all(exerciseDetailsPromises);
        const exerciseMap = new Map<string, Exercise>();
        exerciseDetails.forEach(ex => {
          if (ex && ex.name) exerciseMap.set(ex.name, ex);
        });
        setExerciseData(exerciseMap);

        // Convert exercise muscle data to Map<string, string[]> format (same as useData hook)
        const exerciseMuscleMap = new Map<string, string[]>();
        if (exerciseMuscleDataResponse && Array.isArray(exerciseMuscleDataResponse)) {
          exerciseMuscleDataResponse.forEach(
            (em: { exercise_name: string; muscle_name: string }) => {
              const existing = exerciseMuscleMap.get(em.exercise_name) || [];
              if (!existing.includes(em.muscle_name)) {
                existing.push(em.muscle_name);
              }
              exerciseMuscleMap.set(em.exercise_name, existing);
            }
          );
        }
        setExerciseMuscleData(exerciseMuscleMap);
      } catch {
        enqueueSnackbar('Failed to load week data.', { variant: 'error' });
      } finally {
        setIsLoading(false);
      }
    };

    loadWeekData();
  }, [user.keycloak_id, enqueueSnackbar]);

  const activeProgram = programsWithPreferences.find(program => program.program.is_active);

  const weekWorkouts = useMemo(() => {
    if (!activeProgram || !workouts.length || !userDataExport?.training_programs?.length) return [];

    const activeProgramData = userDataExport.training_programs.find(
      p => p.program.id === activeProgram.program.id
    );

    if (!activeProgramData) return [];

    const workoutsPerWeek = activeProgram.program_preferences?.program_days_per_week || 3;
    const weekStartDay = (weekNumber - 1) * workoutsPerWeek + 1;
    const weekEndDay = weekNumber * workoutsPerWeek;

    // Use userDataExport.training_programs directly instead of programWorkouts
    // This ensures we get all the workout data with stages and exercises
    const allWorkouts = activeProgramData.workouts;
    const filteredWorkouts = allWorkouts
      .map(workoutWithStages => {
        const dayInWeek = workoutWithStages.workout.day_number;
        // Only include workouts that fall within the current week's day range
        if (dayInWeek >= weekStartDay && dayInWeek <= weekEndDay) {
          return { workout: workoutWithStages, weekNumber: weekNumber, dayInWeek };
        }
        return null;
      })
      .filter(Boolean) as {
      workout: ProgrammedWorkoutWithStages;
      weekNumber: number;
      dayInWeek: number;
    }[];

    return filteredWorkouts;
  }, [activeProgram, weekNumber, userDataExport]);

  // Aggregate workout data for charts - combine at stage and exercise level
  const aggregatedWorkoutData = useMemo((): ProgrammedWorkoutWithStages | null => {
    if (!weekWorkouts.length) return null;

    // Collect all stages from all workouts
    const allStages: WorkoutStageWithExercises[] = [];
    weekWorkouts.forEach(weekWorkout => {
      if (weekWorkout.workout.stages) {
        weekWorkout.workout.stages.forEach((stage: WorkoutStageWithExercises) => {
          allStages.push(stage);
        });
      }
    });

    // Group stages by stage type/name and merge exercises within each stage
    const stageMap = new Map<string, WorkoutStageWithExercises>();

    allStages.forEach(stage => {
      const stageKey = `${stage.stage.stage_type_id}-${stage.stage.name}`;

      if (stageMap.has(stageKey)) {
        // Merge exercises from this stage
        const existingStage = stageMap.get(stageKey)!;

        // Don't merge exercises - preserve all exercises as they exist in source data
        // This maintains the same duplication that exists in WorkoutDetail
        existingStage.exercises = [...existingStage.exercises, ...stage.exercises];
      } else {
        // First occurrence of this stage type
        stageMap.set(stageKey, { ...stage });
      }
    });

    const mergedStages: WorkoutStageWithExercises[] = Array.from(stageMap.values());

    return {
      workout: {
        id: weekNumber * 1000,
        program_id: activeProgram?.program.id || 0,
        day_number: weekNumber,
        name: `Week ${weekNumber}`,
        created_at: new Date(),
        updated_at: new Date(),
      },
      stages: mergedStages,
    };
  }, [weekWorkouts, weekNumber, activeProgram]);

  // Create exercise-to-muscles mapping for SunburstChart
  const exerciseToMusclesData = useMemo(() => {
    // exerciseMuscleData is already in the correct format (Map<string, string[]>)
    // Just return it directly
    return exerciseMuscleData;
  }, [exerciseMuscleData]);

  const handleWorkoutClick = (workoutId: number) => {
    const newSearchParams = new URLSearchParams(searchParams);
    newSearchParams.set('section', 'workouts');
    newSearchParams.set('subsection', 'calendar');
    newSearchParams.set('week', weekNumber.toString());
    newSearchParams.set('workout', workoutId.toString());
    navigate(`/dashboard?${newSearchParams.toString()}`);
  };

  const handleBackToWeekList = () => {
    const newSearchParams = new URLSearchParams(searchParams);
    newSearchParams.set('section', 'workouts');
    newSearchParams.set('subsection', 'calendar');
    newSearchParams.delete('week'); // Go back to the main workout calendar
    newSearchParams.delete('workout');
    navigate(`/dashboard?${newSearchParams.toString()}`);
  };

  const getWeekProgressMetrics = () => {
    if (!weekWorkouts.length) return null;

    return calculateWeekProgress(weekWorkouts.map(ww => ww.workout));
  };

  // Get progress metrics for the component
  const progressMetrics = getWeekProgressMetrics();

  const handleExportPDF = async () => {
    if (!activeProgram || !userDataExport) {
      enqueueSnackbar('No active program or user data for export.', { variant: 'warning' });
      return;
    }

    // Export entire week
    const weekWorkoutsData = weekWorkouts.map(ww => ww.workout);
    await exportWeekToPDF(weekWorkoutsData, weightUnitPreferences, {
      title: `Week ${weekNumber}`,
      filename: `week-${weekNumber}-workouts`,
    });
  };

  // Show loading state while data is being fetched
  if (isLoading) {
    return <LoadingSpinner message="Loading week details..." fullHeight={false} />;
  }

  return (
    <Slide direction="left" in={true} mountOnEnter unmountOnExit>
      <Box>
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
          <Box sx={{ position: 'relative' }}>
            {/* Back button for week details */}
            {showBackButton && (
              <IconButton
                onClick={onBack || handleBackToWeekList}
                sx={{
                  position: 'absolute',
                  top: 24,
                  left: -32,
                  zIndex: 1001,
                  backgroundColor: 'background.paper',
                  boxShadow: 2,
                  '&:hover': {
                    backgroundColor: 'action.hover',
                    boxShadow: 4,
                  },
                }}
              >
                <ArrowBackIcon />
              </IconButton>
            )}

            {/* Progress Bar and Export Buttons */}
            <Box sx={{ p: 3, pb: 0 }}>
              <Box display="flex" justifyContent="space-between" alignItems="center" sx={{ mb: 2 }}>
                {/* Progress Bar on the left */}
                <Box sx={{ flex: 1, mr: 2 }}>
                  {progressMetrics && (
                    <ProgressBar
                      value={progressMetrics.completionRate}
                      status={progressMetrics.status}
                      current={progressMetrics.completedWorkouts}
                      total={progressMetrics.totalWorkouts}
                      showTooltip={true}
                      showTicks={true}
                      steps={Array.from(
                        { length: progressMetrics.totalWorkouts + 1 },
                        (_, i) => (i / progressMetrics.totalWorkouts) * 100
                      )}
                      ticks={Array.from(
                        { length: progressMetrics.totalWorkouts + 1 },
                        (_, i) => (i / progressMetrics.totalWorkouts) * 100
                      )}
                      width="100%"
                      height={8}
                      smooth={true}
                      animationDuration={400}
                    />
                  )}
                </Box>

                {/* Export Buttons on the right */}
                <ExportButtons onExportPDF={handleExportPDF} disabled={weekWorkouts.length === 0} />
              </Box>
            </Box>

            <Box id="week-details-content" sx={{ p: 3, pt: 0 }}>
              <Grid container spacing={3} sx={{ height: 'calc(100vh - 200px)' }}>
                {/* Workout List - 2/3 width */}
                <Grid size={{ xs: 12, lg: 8 }}>
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
                        <Box
                          sx={{
                            display: 'flex',
                            alignItems: 'center',
                            gap: 1,
                            mb: 2,
                          }}
                        >
                          <FitnessCenterIcon color="primary" />
                          <Typography variant="h6">Workouts</Typography>
                          {weekWorkouts.length > 0 && (
                            <Box
                              sx={{
                                backgroundColor: 'primary.main',
                                color: 'primary.contrastText',
                                borderRadius: '50%',
                                width: 24,
                                height: 24,
                                display: 'flex',
                                alignItems: 'center',
                                justifyContent: 'center',
                                fontSize: '0.75rem',
                                fontWeight: 'bold',
                              }}
                            >
                              {weekWorkouts.length}
                            </Box>
                          )}
                        </Box>
                        <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                          Week {weekNumber} of {activeProgram.program.current_week_number} • Click
                          any workout to view details
                        </Typography>
                        {isLoading ? (
                          <Box display="flex" justifyContent="center" p={3}>
                            <LoadingSpinner message="Loading workouts..." size={40} />
                          </Box>
                        ) : weekWorkouts.length === 0 ? (
                          <Box sx={{ textAlign: 'center', py: 4 }}>
                            <Typography variant="body2" color="text.secondary">
                              No workouts found for Week {weekNumber}.
                            </Typography>
                          </Box>
                        ) : (
                          <List>
                            {weekWorkouts.map(weekWorkout => {
                              // Calculate workout progress using proper logic
                              const workoutProgress = calculateWorkoutProgress(weekWorkout.workout);
                              return (
                                <ListItem
                                  key={weekWorkout.workout.workout.id}
                                  disablePadding
                                  sx={{
                                    cursor: 'pointer',
                                    borderRadius: 1,
                                    mb: 1,
                                    border: 1,
                                    borderColor: 'divider',
                                    backgroundColor: 'transparent',
                                    '&:hover': {
                                      backgroundColor: 'action.hover',
                                      transform: 'translateX(4px)',
                                      transition: 'all 0.2s ease',
                                    },
                                  }}
                                  onClick={() =>
                                    (onWorkoutClick || handleWorkoutClick)(
                                      weekWorkout.workout.workout.id
                                    )
                                  }
                                >
                                  <ListItemText
                                    primary={
                                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                                        <Typography variant="subtitle1" fontWeight="medium">
                                          Day {weekWorkout.dayInWeek}
                                        </Typography>
                                        {workoutProgress.status === 'completed' ? (
                                          <CheckCircleIcon
                                            sx={{
                                              fontSize: 18,
                                              color: 'success.main',
                                            }}
                                          />
                                        ) : workoutProgress.status === 'in-progress' ? (
                                          <ScheduleIcon
                                            sx={{
                                              fontSize: 18,
                                              color: 'warning.main',
                                            }}
                                          />
                                        ) : (
                                          <PauseCircleIcon
                                            sx={{
                                              fontSize: 18,
                                              color: 'text.disabled',
                                            }}
                                          />
                                        )}
                                      </Box>
                                    }
                                    secondary={
                                      <React.Fragment>
                                        <Typography
                                          variant="body2"
                                          color="text.secondary"
                                          component="span"
                                        >
                                          {replaceUnderscoresWithSpaces(
                                            weekWorkout.workout.workout.name ||
                                              `Workout ${weekWorkout.workout.workout.day_number}`
                                          )}
                                        </Typography>
                                        <Typography
                                          variant="caption"
                                          color="text.secondary"
                                          component="span"
                                          sx={{ ml: 1 }}
                                        >
                                          • {workoutProgress.completedExercises}/
                                          {workoutProgress.totalExercises} exercises
                                        </Typography>
                                      </React.Fragment>
                                    }
                                  />
                                </ListItem>
                              );
                            })}
                          </List>
                        )}
                      </CardContent>
                    </Card>
                  </Box>
                </Grid>

                {/* Charts - 1/3 width */}
                <Grid size={{ xs: 12, lg: 4 }}>
                  <Box sx={{ mt: 3, display: 'flex', flexDirection: 'column', gap: 3 }}>
                    {weekWorkouts.length > 0 && aggregatedWorkoutData && (
                      <React.Fragment>
                        <SunburstChart
                          workoutData={aggregatedWorkoutData}
                          exerciseMuscleData={exerciseToMusclesData}
                          weightUnitPreferences={weightUnitPreferences}
                          selectedExercise="all"
                        />
                        <RadarChart
                          weekWorkouts={weekWorkouts.map(ww => ww.workout)}
                          exerciseData={exerciseData}
                          title="Exercise Movement Type"
                          height={300}
                        />
                      </React.Fragment>
                    )}
                  </Box>
                </Grid>
              </Grid>
            </Box>
          </Box>
        )}
      </Box>
    </Slide>
  );
};
