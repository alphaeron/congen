import { Alert, AlertTitle, Box, Chip, Grid, Skeleton, Stack, Tooltip } from '@mui/material';
import { motion } from 'framer-motion';
import React from 'react';

import { BinaryTag } from './BinaryTag';
import { ExercisePreferenceControls } from './ExercisePreferenceControls';
import { GameCard, GameText, GameTextSecondary, GAME_CLASSES } from './GameTheme';
import { LoadingSpinner } from './LoadingSpinner';
import { RichTextDisplay } from './RichTextDisplay';
import { WeightUnitPreferenceControls } from './WeightUnitPreferenceControls';
import type { Exercise, ExerciseEquipment, ExerciseMuscle, Equipment, Muscle } from '../api/types';
import { capitalizeEachWord, formatWeightWithUnit } from '../common/utils';
import { useActiveProgramContext } from '../hooks/useActiveProgramContext';
import { useData } from '../contexts/DataContext';
import { buildExercisePerformanceHistory } from '../utils/performanceAnalyticsUtils';

/**
 * Props for the ExerciseDetails component.
 */
interface ExerciseDetailsProps {
  exerciseName: string;
} // end interface ExerciseDetailsProps

/**
 * Shows an individual exercise.
 *
 * @return The exercise details component.
 */
export function ExerciseDetails(
  props: ExerciseDetailsProps
): React.ReactElement<ExerciseDetailsProps> {
  const {
    getExercise,
    getExerciseEquipmentData,
    getMuscle,
    getEquipment,
    getExerciseMuscles,
    userOneRepMaxes,
    weightUnitPreferences,
  } = useData();
  const { userData, workoutsPerWeek, preferredUnit } = useActiveProgramContext();
  const [exercise, setExercise] = React.useState<Exercise | null>(null);

  const [exerciseMuscles, setExerciseMuscles] = React.useState<ExerciseMuscle[]>([]);
  const [muscles, setMuscles] = React.useState<Muscle[]>([]);
  const [exerciseEquipment, setExerciseEquipment] = React.useState<ExerciseEquipment[]>([]);
  const [equipment, setEquipment] = React.useState<Equipment[]>([]);
  const [isLoading, setIsLoading] = React.useState(true);
  const [error, setError] = React.useState<Error | null>(null);

  // Load exercise data using DataContext
  React.useEffect(() => {
    const loadExerciseData = async () => {
      if (!props.exerciseName) return;

      setIsLoading(true);
      setError(null);

      try {
        // Load exercise data from DataContext
        const exerciseData = await getExercise(props.exerciseName);
        if (!exerciseData) {
          throw new Error('Exercise not found');
        }
        setExercise(exerciseData);

        // Load exercise muscles using DataContext
        const musclesData = await getExerciseMuscles(props.exerciseName);
        if (musclesData) {
          setExerciseMuscles(musclesData);

          // Load individual muscle details using DataContext
          const muscleDetails = await Promise.all(
            musclesData.map(muscle => getMuscle(muscle.muscle_name))
          );
          setMuscles(muscleDetails.filter(Boolean) as Muscle[]);
        }

        // Load exercise equipment using DataContext
        const equipmentData = await getExerciseEquipmentData(props.exerciseName);
        if (equipmentData) {
          setExerciseEquipment(equipmentData);

          // Load individual equipment details using DataContext
          const equipmentDetails = await Promise.all(
            equipmentData.map(eq => getEquipment(eq.equipment_name))
          );
          setEquipment(equipmentDetails.filter(Boolean) as Equipment[]);
        }
      } catch (err) {
        setError(err instanceof Error ? err : new Error('Failed to load exercise data'));
      } finally {
        setIsLoading(false);
      }
    };

    loadExerciseData();
  }, [
    props.exerciseName,
    getExercise,
    getExerciseEquipmentData,
    getMuscle,
    getEquipment,
    getExerciseMuscles,
  ]);

  // Get exercise-specific 1RM data
  const exerciseOneRepMax = React.useMemo(() => {
    if (!userOneRepMaxes || !exercise) return null;
    return userOneRepMaxes.find(orm => orm.exercise_name === exercise.name);
  }, [userOneRepMaxes, exercise]);

  // Get weight unit preference for this exercise
  const weightUnitPreference = React.useMemo(() => {
    if (!weightUnitPreferences || !exercise) return null;
    return weightUnitPreferences.find(pref => pref.exercise_name === exercise.name);
  }, [weightUnitPreferences, exercise]);

  const performanceHistory = React.useMemo(() => {
    if (!exercise) {
      return [];
    }
    const exerciseData = new Map([[exercise.name, exercise]]);
    return buildExercisePerformanceHistory(
      userData,
      exercise.name,
      exerciseData,
      workoutsPerWeek,
      preferredUnit
    );
  }, [userData, exercise, workoutsPerWeek, preferredUnit]);

  if (isLoading || !props.exerciseName || !exercise) {
    return <LoadingSpinner message="Loading exercise details..." fullHeight={true} />;
  } else if (error) {
    // Check if it's a network error or authentication error
    const isNetworkError =
      error?.message?.includes('Network Error') ||
      error?.message?.includes('timeout') ||
      error?.message?.includes('NS_BINDING_ABORTED');

    if (isNetworkError) {
      return (
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6, ease: 'easeOut' }}
        >
          <GameCard className="glassmorphism-card">
            <Alert severity="warning" sx={{ backgroundColor: 'transparent' }}>
              <AlertTitle>Connection Error</AlertTitle>
              <GameText>
                Unable to connect to the server. Please check your internet connection and try
                again.
              </GameText>
              <GameText variant="body2" textVariant="secondary" className={GAME_CLASSES.marginTop1}>
                Error: {error?.message || 'Network error'}
              </GameText>
            </Alert>
          </GameCard>
        </motion.div>
      );
    } else {
      return (
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6, ease: 'easeOut' }}
        >
          <GameCard className="glassmorphism-card">
            <Alert severity="error" sx={{ backgroundColor: 'transparent' }}>
              <AlertTitle>Exercise Not Found</AlertTitle>
              <GameText>The specified exercise could not be found.</GameText>
              {error && (
                <GameText
                  variant="body2"
                  textVariant="secondary"
                  className={GAME_CLASSES.marginTop1}
                >
                  {error.toString()}
                </GameText>
              )}
            </Alert>
          </GameCard>
        </motion.div>
      );
    }
  } else {
    return (
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.6, ease: 'easeOut' }}
      >
        {/* Exercise Header */}
        <GameCard className="glassmorphism-card">
          <Box sx={{ p: 3 }}>
            <Box display="flex" justifyContent="space-between" alignItems="center" mb={3} gap={2}>
              <GameText
                variant="h1"
                className={GAME_CLASSES.textBold}
                sx={{
                  color: 'var(--game-cyan)',
                  textShadow: '0 0 10px var(--game-cyan)',
                }}
              >
                {exercise.name}
              </GameText>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, flexShrink: 0 }}>
                <WeightUnitPreferenceControls exerciseName={exercise.name} size="medium" />
                <ExercisePreferenceControls
                  exerciseName={exercise.name}
                  variant="segmented"
                  size="medium"
                />
              </Box>
            </Box>

            {/* Exercise Tags */}
            <Stack direction="row" spacing={2} flexWrap="wrap" useFlexGap>
              <Chip
                label={`${capitalizeEachWord(exercise.movement_type)} Exercise`}
                sx={{
                  backgroundColor: 'var(--game-cyan)',
                  color: 'var(--game-white)',
                  '&:hover': {
                    backgroundColor: 'var(--game-cyan-light)',
                  },
                }}
              />
              <BinaryTag
                isOn={exercise.is_upper}
                onText="Upper Body"
                offText="Lower Body"
                color="success"
              />
              <BinaryTag
                isOn={exercise.is_accessory}
                onText="Accessory"
                offText="Primary Movement"
                color="secondary"
              />
              <BinaryTag
                isOn={exercise.is_unilateral}
                onText="Unilateral"
                offText="Bilateral"
                color="primary"
              />
            </Stack>
          </Box>
        </GameCard>

        <Box sx={{ mt: 3 }}>
          <Grid container spacing={3}>
            {/* Main Content - 3/4 width */}
            <Grid size={{ xs: 12, lg: 9 }}>
              <motion.div
                initial={{ opacity: 0, x: -20 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ duration: 0.4, delay: 0.1 }}
              >
                <Stack spacing={3}>
                  {/* Exercise Description Card */}
                  <GameCard className="glassmorphism-card">
                    <Box sx={{ p: 3 }}>
                      <Skeleton variant="rectangular" height={200} sx={{ mb: 2 }} />
                      <RichTextDisplay content={exercise.description} />
                    </Box>
                  </GameCard>
                </Stack>
              </motion.div>
            </Grid>

            {/* Sidebar - 1/4 width */}
            <Grid size={{ xs: 12, lg: 3 }}>
              <motion.div
                initial={{ opacity: 0, x: 20 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ duration: 0.4, delay: 0.2 }}
              >
                <Stack spacing={3}>
                  {/* Muscles Worked */}
                  <GameCard className="glassmorphism-card">
                    <Box sx={{ p: 3 }}>
                      <GameText
                        variant="h4"
                        className={GAME_CLASSES.textMedium}
                        sx={{
                          mb: 2,
                          color: 'var(--game-cyan)',
                          textShadow: '0 0 10px var(--game-cyan)',
                        }}
                      >
                        Muscles
                      </GameText>
                      <Stack direction="row" spacing={2} flexWrap="wrap" useFlexGap>
                        {exerciseMuscles?.map((em, index) => {
                          const muscle = muscles.find(elem => elem.name === em.muscle_name);
                          return (
                            <motion.div
                              key={em.muscle_name}
                              initial={{ opacity: 0, x: 20 }}
                              animate={{ opacity: 1, x: 0 }}
                              transition={{ duration: 0.3, delay: 0.2 + index * 0.1 }}
                            >
                              <Tooltip arrow={true} title={muscle?.description}>
                                <Chip
                                  label={`${capitalizeEachWord(em.muscle_name)}`}
                                  sx={{
                                    backgroundColor: 'var(--game-cyan-light)',
                                    color: 'var(--game-white)',
                                    '&:hover': {
                                      backgroundColor: 'var(--game-cyan)',
                                    },
                                  }}
                                />
                              </Tooltip>
                            </motion.div>
                          );
                        })}
                      </Stack>
                    </Box>
                  </GameCard>

                  {/* Equipment Needed */}
                  <GameCard className="glassmorphism-card">
                    <Box sx={{ p: 3 }}>
                      <GameText
                        variant="h4"
                        className={GAME_CLASSES.textMedium}
                        sx={{
                          mb: 2,
                          color: 'var(--game-cyan)',
                          textShadow: '0 0 10px var(--game-cyan)',
                        }}
                      >
                        Equipment
                      </GameText>
                      <Stack spacing={1}>
                        {exerciseEquipment?.map((ee, index) => {
                          const equip = equipment.find(elem => elem.name === ee.equipment_name);
                          return (
                            <motion.div
                              key={ee.equipment_name}
                              initial={{ opacity: 0, x: 20 }}
                              animate={{ opacity: 1, x: 0 }}
                              transition={{ duration: 0.3, delay: 0.3 + index * 0.1 }}
                            >
                              <Tooltip arrow={true} title={equip?.description}>
                                <Chip
                                  label={`${capitalizeEachWord(ee.equipment_name)}`}
                                  sx={{
                                    backgroundColor: 'var(--game-gray)',
                                    color: 'var(--game-white)',
                                    '&:hover': {
                                      backgroundColor: 'var(--game-gray-light)',
                                    },
                                  }}
                                />
                              </Tooltip>
                            </motion.div>
                          );
                        })}
                      </Stack>
                    </Box>
                  </GameCard>

                  {/* Performance Stats */}
                  <GameCard className="glassmorphism-card">
                    <Box sx={{ p: 3 }}>
                      <GameText
                        variant="h4"
                        className={GAME_CLASSES.textMedium}
                        sx={{
                          mb: 2,
                          color: 'var(--game-cyan)',
                          textShadow: '0 0 10px var(--game-cyan)',
                        }}
                      >
                        Performance
                      </GameText>
                      <Box sx={{ textAlign: 'center' }}>
                        {exerciseOneRepMax ? (
                          <React.Fragment>
                            <GameText
                              variant="h3"
                              className={GAME_CLASSES.textBold}
                              color="var(--game-cyan)"
                            >
                              {formatWeightWithUnit(
                                exerciseOneRepMax.one_rep_max,
                                weightUnitPreference?.preferred_unit as 'KG' | 'LBS' | undefined
                              )}
                            </GameText>
                            <GameTextSecondary variant="body2">1RM Record</GameTextSecondary>
                            <GameTextSecondary variant="caption" className={GAME_CLASSES.textMuted}>
                              {new Date(exerciseOneRepMax!.updated_at).toLocaleDateString()}
                            </GameTextSecondary>
                          </React.Fragment>
                        ) : (
                          <GameTextSecondary
                            variant="body2"
                            sx={{
                              fontStyle: 'italic',
                              color: 'var(--game-gray-light)',
                            }}
                          >
                            No One Rep Max Data...
                          </GameTextSecondary>
                        )}
                      </Box>

                      <Box sx={{ mt: 2 }}>
                        <GameText
                          variant="subtitle2"
                          className={GAME_CLASSES.textMedium}
                          sx={{ mb: 1 }}
                        >
                          Logged History
                        </GameText>
                        {performanceHistory.length === 0 ? (
                          <GameTextSecondary variant="body2" sx={{ fontStyle: 'italic' }}>
                            No logged sets for this exercise yet.
                          </GameTextSecondary>
                        ) : (
                          <Stack
                            spacing={1}
                            sx={{ maxHeight: 220, overflowY: 'auto' }}
                            data-testid="exercise-performance-history"
                          >
                            {performanceHistory.map(session => (
                              <Box
                                key={`${session.workoutId}-${session.weekNumber}`}
                                data-testid={`exercise-history-${session.workoutId}`}
                              >
                                <GameText variant="body2">
                                  W{session.weekNumber} Day {session.dayNumber}
                                </GameText>
                                <GameTextSecondary variant="caption">
                                  {session.category} •{' '}
                                  {formatWeightWithUnit(session.topWeightKg, preferredUnit)} ×{' '}
                                  {session.topReps}
                                </GameTextSecondary>
                              </Box>
                            ))}
                          </Stack>
                        )}
                      </Box>
                    </Box>
                  </GameCard>
                </Stack>
              </motion.div>
            </Grid>
          </Grid>
        </Box>
      </motion.div>
    );
  }
} // end component ExerciseOverview
