import { Alert, AlertTitle, Box, Chip, Divider, Grid, Skeleton, Stack, Tooltip } from '@mui/material';
import React from 'react';
import { motion } from 'framer-motion';
import { createEditor } from 'slate';
import { Slate, Editable, withReact } from 'slate-react';

import { BinaryTag } from './BinaryTag';
import { ExercisePreferenceControls } from './ExercisePreferenceControls';
import { LoadingSpinner } from './LoadingSpinner';
import { GameCard, GameText, GAME_CLASSES } from './GameTheme';
import type { Exercise, ExerciseEquipment, ExerciseMuscle, Equipment, Muscle } from '../api/types';
import { capitalizeEachWord } from '../common/utils';
import { useData } from '../contexts/DataContext';

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
  const { getExercise, getExerciseEquipmentData, getMuscle, getEquipment, getExerciseMuscles } =
    useData();
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
        setExerciseMuscles(musclesData);

        // Load individual muscle details using DataContext
        const muscleDetails = await Promise.all(
          musclesData.map(muscle => getMuscle(muscle.muscle_name))
        );
        setMuscles(muscleDetails.filter(Boolean) as Muscle[]);

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

  const editor = React.useMemo(() => withReact(createEditor()), []);

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
                Unable to connect to the server. Please check your internet connection and try again.
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
                <GameText variant="body2" textVariant="secondary" className={GAME_CLASSES.marginTop1}>
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
        <GameCard className="glassmorphism-card">
          <Box sx={{ p: 3 }}>
            {/* Header Section */}
            <motion.div
              initial={{ opacity: 0, y: -20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.4, delay: 0.1 }}
            >
              <Box display="flex" justifyContent="space-between" alignItems="flex-start" mb={3}>
                <GameText variant="h1" className={GAME_CLASSES.textBold}>
                  {exercise.name}
                </GameText>
                <ExercisePreferenceControls
                  exerciseName={exercise.name}
                  variant="segmented"
                  size="medium"
                />
              </Box>
            </motion.div>

            {/* Exercise Tags */}
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.4, delay: 0.2 }}
            >
              <Stack direction="row" spacing={2} sx={{ mb: 3 }}>
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
            </motion.div>

            <Grid container spacing={3}>
              {/* Main Content */}
              <Grid size={{ xs: 12, md: 8 }}>
                <motion.div
                  initial={{ opacity: 0, x: -20 }}
                  animate={{ opacity: 1, x: 0 }}
                  transition={{ duration: 0.4, delay: 0.3 }}
                >
                  <GameCard className="glassmorphism-card">
                    <Box sx={{ p: 3 }}>
                      <GameText variant="h4" className={GAME_CLASSES.textMedium} sx={{ mb: 2 }}>
                        Exercise Description
                      </GameText>
                      <Skeleton variant="rectangular" height={200} sx={{ mb: 2 }} />
                      <Slate
                        editor={editor}
                        initialValue={[
                          {
                            children: [
                              {
                                text: exercise.description,
                              },
                            ],
                          },
                        ]}
                      >
                        <Editable 
                          readOnly={true} 
                          placeholder="No description provided."
                          style={{
                            minHeight: '100px',
                            padding: '12px',
                            backgroundColor: 'var(--game-gray-dark)',
                            borderRadius: '8px',
                            color: 'var(--game-white)',
                            border: '1px solid var(--game-cyan-border)',
                          }}
                        />
                      </Slate>
                    </Box>
                  </GameCard>
                </motion.div>
              </Grid>

              {/* Sidebar */}
              <Grid size={{ xs: 12, md: 4 }}>
                <motion.div
                  initial={{ opacity: 0, x: 20 }}
                  animate={{ opacity: 1, x: 0 }}
                  transition={{ duration: 0.4, delay: 0.4 }}
                >
                  <Stack spacing={3}>
                    {/* Muscles Worked */}
                    <GameCard className="glassmorphism-card">
                      <Box sx={{ p: 3 }}>
                        <GameText variant="h4" className={GAME_CLASSES.textMedium} sx={{ mb: 2 }}>
                          Muscles Worked
                        </GameText>
                        <Stack spacing={1}>
                          {exerciseMuscles?.map((em, index) => {
                            const muscle = muscles.find(elem => elem.name === em.muscle_name);
                            return (
                              <motion.div
                                key={em.muscle_name}
                                initial={{ opacity: 0, x: 20 }}
                                animate={{ opacity: 1, x: 0 }}
                                transition={{ duration: 0.3, delay: 0.5 + index * 0.1 }}
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
                        <GameText variant="h4" className={GAME_CLASSES.textMedium} sx={{ mb: 2 }}>
                          Equipment Needed
                        </GameText>
                        <Stack spacing={1}>
                          {exerciseEquipment?.map((ee, index) => {
                            const equip = equipment.find(elem => elem.name === ee.equipment_name);
                            return (
                              <motion.div
                                key={ee.equipment_name}
                                initial={{ opacity: 0, x: 20 }}
                                animate={{ opacity: 1, x: 0 }}
                                transition={{ duration: 0.3, delay: 0.6 + index * 0.1 }}
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
                  </Stack>
                </motion.div>
              </Grid>
            </Grid>
          </Box>
        </GameCard>
      </motion.div>
    );
  }
} // end component ExerciseOverview
