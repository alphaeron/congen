import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import PauseCircleIcon from '@mui/icons-material/PauseCircle';
import ScheduleIcon from '@mui/icons-material/Schedule';
import { Box, CardContent, Grid, List, ListItem, ListItemText } from '@mui/material';
import { motion } from 'framer-motion';
import { useSnackbar } from 'notistack';
import React, { useState, useEffect, useMemo } from 'react';
import { useNavigate, useSearchParams } from 'react-router';

import { GameCard, GameText, GAME_CLASSES } from './GameTheme';
import { LoadingSpinner } from './LoadingSpinner';
import { RadarChart } from './RadarChart';
import { SunburstChart } from './SunburstChart';
import { WeekKeyResultsSummary } from './WeekKeyResultsSummary';
import { WorkoutHeader } from './WorkoutHeader';
import { encodeExerciseName } from '../api/endpoint';
import type {
  Exercise,
  ProgrammedWorkoutWithStages,
  WorkoutStageWithExercises,
} from '../api/types';
import { replaceUnderscoresWithSpaces } from '../common/utils';
import { useData } from '../contexts/DataContext';
import { useActiveProgramContext } from '../hooks/useActiveProgramContext';
import { exportWeekToPDF } from '../utils/exportUtils';
import { buildWeekKeyResults } from '../utils/performanceAnalyticsUtils';
import { calculateWeekProgress, calculateWorkoutProgress } from '../utils/progressUtils';
import { buildWeekVolumeTotals } from '../utils/volumeOverviewUtils';

interface WorkoutWeekDetailsProps {
  selectedWorkout?: string | null;
  weekNumber: number;
  showBackButton?: boolean;
  onBack?: () => void;
  onWorkoutClick?: (workoutId: number) => void;
}

export const WorkoutWeekDetails: React.FC<WorkoutWeekDetailsProps> = ({
  weekNumber,
  showBackButton = true,
  onBack,
  onWorkoutClick,
}) => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { enqueueSnackbar } = useSnackbar();
  const {
    exerciseMuscleData,
    weightUnitPreferences = [],
    isLoading: isDataLoading,
    getExercise,
    loadProgramPreferences,
    programPreferences = [],
    userOneRepMaxes,
  } = useData();
  const {
    userData,
    workoutsPerWeek,
    preferredUnit,
    activeProgramPreferences: activeProgram,
  } = useActiveProgramContext();

  const [isLoading, setIsLoading] = useState(true);
  const [exerciseData, setExerciseData] = useState<Map<string, Exercise>>(new Map());

  useEffect(() => {
    const loadAdditionalData = async () => {
      setIsLoading(true);
      try {
        if (programPreferences.length === 0) {
          await loadProgramPreferences();
        }

        // Extract unique exercises from userData and fetch exercise details
        const uniqueExercises = new Set<string>();
        userData?.training_programs?.forEach(program => {
          program.workouts.forEach(workoutWithStages => {
            workoutWithStages.stages.forEach(stageWithExercises => {
              stageWithExercises.exercises.forEach(exerciseWithSetSchemes => {
                uniqueExercises.add(exerciseWithSetSchemes.exercise.exercise_name);
              });
            });
          });
        });

        const exerciseMap = new Map<string, Exercise>();
        for (const exerciseName of Array.from(uniqueExercises)) {
          try {
            const exercise = await getExercise(exerciseName);
            if (exercise) {
              exerciseMap.set(exerciseName, exercise);
            }
          } catch {
            // There was an error fetching the exercise, don't add it to the map.
          }
        }
        setExerciseData(exerciseMap);
      } catch {
        enqueueSnackbar('Failed to load additional week data. Please try again.', {
          variant: 'error',
        });
      } finally {
        setIsLoading(false);
      }
    };

    loadAdditionalData();
  }, [userData, programPreferences.length, enqueueSnackbar, getExercise, loadProgramPreferences]);

  const weekKeyResults = useMemo(() => {
    return buildWeekKeyResults(userData, exerciseData, workoutsPerWeek, weekNumber, preferredUnit);
  }, [userData, exerciseData, workoutsPerWeek, weekNumber, preferredUnit]);

  const totalVolumeTrend = useMemo(() => {
    if (!activeProgram || !userData?.training_programs?.length) {
      return [];
    }
    const activeProgramData = userData.training_programs.find(
      program => program.program.id === activeProgram.program.id
    );
    if (!activeProgramData) {
      return [];
    }
    const weekVolumes = buildWeekVolumeTotals(
      activeProgramData.workouts,
      exerciseData,
      workoutsPerWeek,
      preferredUnit
    );
    return weekVolumes.map(week => ({
      x: `W${week.weekNumber}`,
      y: week.totalVolume,
    }));
  }, [activeProgram, userData, exerciseData, workoutsPerWeek, preferredUnit]);

  const handleExerciseClick = (exerciseName: string) => {
    navigate(`/exercises/${encodeExerciseName(exerciseName)}`);
  };

  const weekWorkouts = useMemo(() => {
    if (!activeProgram || !userData?.training_programs?.length) return [];

    const activeProgramData = userData.training_programs.find(
      p => p.program.id === activeProgram.program.id
    );

    if (!activeProgramData) return [];

    const workoutsPerWeek = activeProgram.program_preferences?.program_days_per_week || 3;
    const weekStartDay = (weekNumber - 1) * workoutsPerWeek + 1;
    const weekEndDay = weekNumber * workoutsPerWeek;

    // Use userData.training_programs directly instead of programWorkouts
    // This ensures we get all the workout data with stages and exercises
    const allWorkouts = activeProgramData.workouts;
    const filteredWorkouts = allWorkouts
      .map(workoutWithStages => {
        const dayNumber = workoutWithStages.workout.day_number;
        // Only include workouts that fall within the current week's day range
        if (dayNumber >= weekStartDay && dayNumber <= weekEndDay) {
          // Calculate the day within the week (1-based for display)
          // Formula: dayInWeek = dayNumber - workoutsPerWeek * (weekNumber - 1)
          const dayInWeek = dayNumber - workoutsPerWeek * (weekNumber - 1);
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
  }, [activeProgram, weekNumber, userData]);

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
    if (!activeProgram || !userData) {
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
  if (isDataLoading || isLoading) {
    return <LoadingSpinner message="Loading week details..." fullHeight={false} />;
  }

  return (
    <motion.div
      initial={{ opacity: 0, x: -50 }}
      animate={{ opacity: 1, x: 0 }}
      transition={{ duration: 0.6, ease: 'easeOut' }}
      style={{
        marginTop: '24px',
      }}
    >
      {!activeProgram ? (
        <GameCard>
          <CardContent>
            <motion.div
              initial={{ opacity: 0, x: -30 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ duration: 0.6, ease: 'easeOut', delay: 0.2 }}
            >
              <GameText variant="h6" gutterBottom>
                No Active Program
              </GameText>
            </motion.div>
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              transition={{ duration: 0.6, ease: 'easeOut', delay: 0.4 }}
            >
              <GameText variant="body2" textVariant="secondary" paragraph>
                You need to create a program first before you can generate and view workouts. Please
                go to the Programs section to create a program.
              </GameText>
            </motion.div>
          </CardContent>
        </GameCard>
      ) : (
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ duration: 0.8, ease: 'easeOut' }}
          style={{ position: 'relative' }}
        >
          {/* Header Section */}
          <WorkoutHeader
            context="week"
            weekNumber={weekNumber}
            totalWorkouts={weekWorkouts.length}
            completedWeekWorkouts={progressMetrics?.completedWorkouts || 0}
            onExportPDF={handleExportPDF}
            onBack={showBackButton ? onBack || handleBackToWeekList : undefined}
            disabled={weekWorkouts.length === 0}
          />

          <Box
            id="week-details-content"
            sx={{
              px: 3,
            }}
          >
            <Grid container spacing={3}>
              {/* Workout list and key results - 2/3 width */}
              <Grid size={{ xs: 12, lg: 8 }}>
                <Box
                  sx={{
                    mt: 3,
                    display: 'flex',
                    flexDirection: 'column',
                    gap: 3,
                  }}
                >
                  <motion.div
                    initial={{ opacity: 0, y: 20 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ duration: 0.4, delay: 0.2 }}
                    whileHover={{ y: -8 }}
                  >
                    <GameCard className="glassmorphism-card">
                      <Box sx={{ p: 2 }}>
                        <Box
                          sx={{
                            display: 'flex',
                            alignItems: 'center',
                            gap: 1,
                            mb: 2,
                          }}
                        >
                          <GameText variant="h6">Workouts</GameText>
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
                        <GameText
                          variant="body2"
                          textVariant="secondary"
                          className={GAME_CLASSES.marginBottom2}
                        >
                          Week {weekNumber} of{' '}
                          {Math.max(activeProgram.program.current_week_number, 1)} • Click any
                          workout to view details
                        </GameText>
                        {isLoading ? (
                          <Box display="flex" justifyContent="center" p={3}>
                            <LoadingSpinner message="Loading workouts..." size={40} />
                          </Box>
                        ) : weekWorkouts.length === 0 ? (
                          <Box sx={{ textAlign: 'center', py: 4 }}>
                            <GameText variant="body2" textVariant="secondary">
                              No workouts found for Week {weekNumber}.
                            </GameText>
                          </Box>
                        ) : (
                          <List>
                            {weekWorkouts.map((weekWorkout, index) => {
                              // Calculate workout progress using proper logic
                              const workoutProgress = calculateWorkoutProgress(weekWorkout.workout);
                              return (
                                <motion.div
                                  key={weekWorkout.workout.workout.id}
                                  initial={{ opacity: 0, x: -20 }}
                                  animate={{ opacity: 1, x: 0 }}
                                  transition={{ duration: 0.3, delay: index * 0.1 }}
                                  whileHover={{ x: 4, scale: 1.02 }}
                                  whileTap={{ scale: 0.98 }}
                                >
                                  <ListItem
                                    disablePadding
                                    sx={{
                                      cursor: 'pointer',
                                      borderRadius: 1,
                                      mb: 1,
                                      px: 1,
                                      py: 0.5,
                                      border: 1,
                                      borderColor: 'divider',
                                      backgroundColor: 'transparent',
                                      '&:hover': {
                                        backgroundColor: 'action.hover',
                                        boxShadow: 'var(--game-cyan-shadow)',
                                        borderColor: 'var(--game-cyan)',
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
                                          <GameText
                                            variant="subtitle1"
                                            className={GAME_CLASSES.textMedium}
                                          >
                                            Day {weekWorkout.dayInWeek}
                                          </GameText>
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
                                          <GameText
                                            variant="body2"
                                            textVariant="secondary"
                                            component="span"
                                          >
                                            {replaceUnderscoresWithSpaces(
                                              weekWorkout.workout.workout.name ||
                                                `Workout ${weekWorkout.workout.workout.day_number}`
                                            )}
                                          </GameText>
                                          <GameText
                                            variant="caption"
                                            textVariant="secondary"
                                            component="span"
                                            sx={{ ml: 0.5 }}
                                          >
                                            • {workoutProgress.completedExercises}/
                                            {workoutProgress.totalExercises} exercises
                                          </GameText>
                                        </React.Fragment>
                                      }
                                    />
                                  </ListItem>
                                </motion.div>
                              );
                            })}
                          </List>
                        )}
                      </Box>
                    </GameCard>
                  </motion.div>
                  {weekWorkouts.length > 0 && weekKeyResults ? (
                    <motion.div
                      initial={{ opacity: 0, y: 20 }}
                      animate={{ opacity: 1, y: 0 }}
                      transition={{ duration: 0.4, delay: 0.25 }}
                      whileHover={{ y: -8 }}
                    >
                      <WeekKeyResultsSummary
                        results={weekKeyResults}
                        preferredUnit={preferredUnit}
                        userOneRepMaxes={userOneRepMaxes}
                        totalVolumeTrend={totalVolumeTrend}
                        onWorkoutClick={onWorkoutClick || handleWorkoutClick}
                        onExerciseClick={handleExerciseClick}
                      />
                    </motion.div>
                  ) : null}
                </Box>
              </Grid>

              {/* Charts - 1/3 width */}
              <Grid size={{ xs: 12, lg: 4 }}>
                <Box
                  sx={{
                    mt: 3,
                    display: 'flex',
                    flexDirection: 'column',
                    gap: 3,
                  }}
                >
                  {weekWorkouts.length > 0 && aggregatedWorkoutData && (
                    <React.Fragment>
                      <motion.div
                        initial={{ opacity: 0, y: 20 }}
                        animate={{ opacity: 1, y: 0 }}
                        transition={{ duration: 0.4, delay: 0.3 }}
                        whileHover={{ y: -8 }}
                      >
                        <SunburstChart
                          workoutData={aggregatedWorkoutData}
                          exerciseMuscleData={exerciseToMusclesData}
                          weightUnitPreferences={weightUnitPreferences}
                          selectedExercise="all"
                        />
                      </motion.div>
                      <motion.div
                        initial={{ opacity: 0, y: 20 }}
                        animate={{ opacity: 1, y: 0 }}
                        transition={{ duration: 0.4, delay: 0.4 }}
                        whileHover={{ y: -8 }}
                      >
                        <RadarChart
                          weekWorkouts={weekWorkouts.map(ww => ww.workout)}
                          exerciseData={exerciseData}
                          title="Exercise Movement Type"
                          height={300}
                        />
                      </motion.div>
                    </React.Fragment>
                  )}
                </Box>
              </Grid>
            </Grid>
          </Box>
        </motion.div>
      )}
    </motion.div>
  );
};
