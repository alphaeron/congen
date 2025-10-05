import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import PauseCircleIcon from '@mui/icons-material/PauseCircle';
import ScheduleIcon from '@mui/icons-material/Schedule';
import {
  Box,
  Card,
  CardContent,
  Grid,
  List,
  ListItem,
  ListItemText,
  IconButton,
} from '@mui/material';
import { useSnackbar } from 'notistack';
import React, { useState, useEffect, useMemo } from 'react';
import { useNavigate, useSearchParams } from 'react-router';
import { motion } from 'framer-motion';

import { ExportButtons } from './ExportButtons';
import { LoadingSpinner } from './LoadingSpinner';
import { ProgressBar } from './ProgressBar';
import { RadarChart } from './RadarChart';
import { SunburstChart } from './SunburstChart';
import { GameCard, GameText, GAME_CLASSES } from './GameTheme';
import type {
  ProgramWithPreferences,
  Exercise,
  ProgrammedWorkoutWithStages,
  WorkoutStageWithExercises,
} from '../api/types';
import { replaceUnderscoresWithSpaces } from '../common/utils';
import { useData } from '../contexts/DataContext';
import { exportWeekToPDF } from '../utils/exportUtils';
import { calculateWeekProgress, calculateWorkoutProgress } from '../utils/progressUtils';

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
    userData,
    exerciseMuscleData,
    weightUnitPreferences,
    isLoading: isDataLoading,
    getExercise,
    loadProgramPreferences,
  } = useData();

  const [programsWithPreferences, setProgramsWithPreferences] = useState<
    Array<ProgramWithPreferences>
  >([]);
  const [isLoading, setIsLoading] = useState(true);
  const [exerciseData, setExerciseData] = useState<Map<string, Exercise>>(new Map());

  useEffect(() => {
    const loadAdditionalData = async () => {
      setIsLoading(true);
      try {
        // Load program preferences using DataContext
        const programsData = await loadProgramPreferences();
        setProgramsWithPreferences(programsData);

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
  }, [userData, enqueueSnackbar, getExercise, loadProgramPreferences]);

  const activeProgram = programsWithPreferences?.find(program => program.program.is_active);

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
    >
        {!activeProgram ? (
          <GameCard>
            <CardContent>
              <motion.div
                initial={{ opacity: 0, x: -30 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ duration: 0.6, ease: 'easeOut', delay: 0.2 }}
              >
                <GameText 
                  variant="h6" 
                  gutterBottom
                >
                  No Active Program
                </GameText>
              </motion.div>
              <motion.div
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                transition={{ duration: 0.6, ease: 'easeOut', delay: 0.4 }}
              >
                <GameText 
                  variant="body2" 
                  textVariant="secondary" 
                  paragraph
                >
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
            {/* Back button for week details */}
            {showBackButton && (
              <motion.div
                initial={{ opacity: 0, x: -30 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ duration: 0.6, ease: 'easeOut', delay: 0.1 }}
                whileHover={{ scale: 1.1 }}
                whileTap={{ scale: 0.95 }}
                style={{
                  position: 'absolute',
                  top: 24,
                  left: -32,
                  zIndex: 1001,
                }}
              >
                <IconButton
                  onClick={onBack || handleBackToWeekList}
                  sx={{
                    backgroundColor: 'background.paper',
                    boxShadow: 2,
                    '&:hover': {
                      backgroundColor: 'rgba(0, 188, 212, 0.1)',
                      boxShadow: 4,
                    },
                  }}
                >
                  <ArrowBackIcon />
                </IconButton>
              </motion.div>
            )}

            {/* Progress Bar and Export Buttons */}
            <Box 
              sx={{ 
                p: 3, 
                pb: 0,
              }}
            >
              <Box display="flex" justifyContent="space-between" alignItems="center" sx={{ mb: 2 }}>
                {/* Progress Bar on the left */}
                <Box 
                  sx={{ 
                    flex: 1, 
                    mr: 2,
                  }}
                >
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
                <Box
                  sx={{
                  }}
                >
                  <ExportButtons onExportPDF={handleExportPDF} disabled={weekWorkouts.length === 0} />
                </Box>
              </Box>
            </Box>

            <Box 
              id="week-details-content" 
              sx={{ 
                p: 3, 
                pt: 0,
              }}
            >
              <Grid container spacing={3} sx={{ height: 'calc(100vh - 200px)' }}>
                {/* Workout List - 2/3 width */}
                <Grid size={{ xs: 12, lg: 8 }}>
                  <Box 
                    sx={{ 
                      height: '100%',
                    }}
                  >
                    <Card
                      sx={{
                        mt: 3,
                        height: '100%',
                        '&:hover': {
                          transform: 'translateY(-2px)',
                          boxShadow: '0 8px 25px rgba(0, 188, 212, 0.15)',
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
                        <GameText variant="body2" textVariant="secondary" className={GAME_CLASSES.marginBottom2}>
                          Week {weekNumber} of {activeProgram.program.current_week_number} • Click
                          any workout to view details
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
                                      transform: 'translateX(4px) scale(1.02)',
                                      boxShadow: '0 4px 15px rgba(0, 188, 212, 0.2)',
                                      borderColor: '#00bcd4',
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
                                        <GameText variant="subtitle1" className={GAME_CLASSES.textMedium}>
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
                                          sx={{ ml: 1 }}
                                        >
                                          • {workoutProgress.completedExercises}/
                                          {workoutProgress.totalExercises} exercises
                                        </GameText>
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
                        <Box
                          sx={{
                            '&:hover': {
                              transform: 'translateY(-2px)',
                              boxShadow: '0 8px 25px rgba(0, 188, 212, 0.1)',
                            }
                          }}
                        >
                          <SunburstChart
                            workoutData={aggregatedWorkoutData}
                            exerciseMuscleData={exerciseToMusclesData}
                            weightUnitPreferences={weightUnitPreferences}
                            selectedExercise="all"
                          />
                        </Box>
                        <Box
                          sx={{
                            '&:hover': {
                              transform: 'translateY(-2px)',
                              boxShadow: '0 8px 25px rgba(0, 188, 212, 0.1)',
                            }
                          }}
                        >
                          <RadarChart
                            weekWorkouts={weekWorkouts.map(ww => ww.workout)}
                            exerciseData={exerciseData}
                            title="Exercise Movement Type"
                            height={300}
                          />
                        </Box>
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
