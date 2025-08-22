import React, { useEffect, useState } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Grid,
  Chip,
  List,
  ListItem,
  ListItemText,
  Divider,
  CircularProgress,
  Alert,
  Accordion,
  AccordionSummary,
  AccordionDetails,
  IconButton,
  Tooltip,
} from '@mui/material';
import {
  ExpandMore as ExpandMoreIcon,
  FitnessCenter as FitnessCenterIcon,
  Timer as TimerIcon,
  TrendingUp as TrendingUpIcon,
  Notes as NotesIcon,
} from '@mui/icons-material';

import { getProgrammedWorkout } from '../api/programmedWorkout';
import { getWorkoutStagesByWorkout } from '../api/workoutStage';
import { getProgrammedExercisesByStage } from '../api/programmedExercise';
import { getSetSchemesByExercise } from '../api/setScheme';
import type { ProgrammedWorkout, WorkoutStage, ProgrammedExercise, SetScheme } from '../api/types';

interface WorkoutDetailProps {
  workoutId: number;
  onBack: () => void;
}

interface WorkoutStageWithExercises {
  stage: WorkoutStage;
  exercises: ProgrammedExerciseWithSetSchemes[];
}

interface ProgrammedExerciseWithSetSchemes {
  exercise: ProgrammedExercise;
  setSchemes: SetScheme[];
}

/**
 * Detailed workout display component.
 * 
 * Shows all stages, exercises, and set schemes for a specific workout
 * with a hierarchical accordion layout for easy navigation.
 * 
 * @param workoutId The ID of the workout to display
 * @param onBack Callback to go back to the workout list
 * @returns WorkoutDetail component
 */
export const WorkoutDetail: React.FC<WorkoutDetailProps> = ({ workoutId, onBack }) => {
  const [workout, setWorkout] = useState<ProgrammedWorkout | null>(null);
  const [stages, setStages] = useState<WorkoutStageWithExercises[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const loadWorkoutDetails = async () => {
      try {
        setIsLoading(true);
        setError(null);
        
        // Load workout details
        const workoutData = await getProgrammedWorkout(workoutId);
        setWorkout(workoutData);
        
        // Load stages for this workout
        const stagesData = await getWorkoutStagesByWorkout(workoutId);
        
        // Load exercises and set schemes for each stage
        const stagesWithExercises = await Promise.all(
          stagesData.map(async (stage) => {
            const exercises = await getProgrammedExercisesByStage(stage.id);
            
            const exercisesWithSetSchemes = await Promise.all(
              exercises.map(async (exercise) => {
                const setSchemes = await getSetSchemesByExercise(exercise.id);
                return {
                  exercise,
                  setSchemes,
                };
              })
            );
            
            return {
              stage,
              exercises: exercisesWithSetSchemes,
            };
          })
        );
        
        setStages(stagesWithExercises);
      } catch (err) {
        console.error('Error loading workout details:', err);
        setError('Failed to load workout details. Please try again.');
      } finally {
        setIsLoading(false);
      }
    };

    loadWorkoutDetails();
  }, [workoutId]);

  if (isLoading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight={400}>
        <CircularProgress />
      </Box>
    );
  }

  if (error) {
    return (
      <Alert severity="error" sx={{ mb: 3 }}>
        {error}
      </Alert>
    );
  }

  if (!workout) {
    return (
      <Alert severity="warning">
        Workout not found.
      </Alert>
    );
  }

  const formatRestTime = (seconds: number): string => {
    if (seconds < 60) return `${seconds}s`;
    const minutes = Math.floor(seconds / 60);
    const remainingSeconds = seconds % 60;
    return remainingSeconds > 0 ? `${minutes}m ${remainingSeconds}s` : `${minutes}m`;
  };

  const formatTempo = (eccentric: string, isometric: string, concentric: string): string => {
    return `${eccentric}-${isometric}-${concentric}`;
  };

  return (
    <Box>
      {/* Workout Header */}
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Box display="flex" justifyContent="space-between" alignItems="center">
            <Box>
              <Typography variant="h4" gutterBottom>
                {workout.name}
              </Typography>
              <Typography variant="body1" color="text.secondary">
                Day {workout.day_number} • {stages.length} stages
              </Typography>
            </Box>
            <IconButton onClick={onBack} size="large">
              <ExpandMoreIcon sx={{ transform: 'rotate(90deg)' }} />
            </IconButton>
          </Box>
        </CardContent>
      </Card>

      {/* Workout Stages */}
      <Grid container spacing={3}>
        {stages.map((stageData: WorkoutStageWithExercises, stageIndex: number) => (
          <Grid item xs={12} key={stageData.stage.id}>
            <Accordion defaultExpanded={stageIndex === 0}>
              <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                <Box display="flex" alignItems="center" gap={2}>
                  <Chip 
                    label={`Stage ${stageData.stage.position}`}
                    color="primary"
                    size="small"
                  />
                  <Typography variant="h6">
                    {stageData.stage.name}
                  </Typography>
                  <Chip 
                    label={`${stageData.exercises.length} exercises`}
                    variant="outlined"
                    size="small"
                  />
                </Box>
              </AccordionSummary>
              <AccordionDetails>
                <List>
                  {stageData.exercises.map((exerciseData: ProgrammedExerciseWithSetSchemes, exerciseIndex: number) => (
                    <React.Fragment key={exerciseData.exercise.id}>
                      <ListItem sx={{ flexDirection: 'column', alignItems: 'flex-start' }}>
                        <Box display="flex" alignItems="center" gap={2} width="100%" mb={2}>
                          <FitnessCenterIcon color="primary" />
                          <Typography variant="h6">
                            {exerciseData.exercise.exercise_name}
                          </Typography>
                          {exerciseData.exercise.notes && (
                            <Tooltip title={exerciseData.exercise.notes}>
                              <NotesIcon color="action" fontSize="small" />
                            </Tooltip>
                          )}
                        </Box>
                        
                        {exerciseData.exercise.notes && (
                          <Typography variant="body2" color="text.secondary" sx={{ mb: 2, fontStyle: 'italic' }}>
                            "{exerciseData.exercise.notes}"
                          </Typography>
                        )}

                        {/* Set Schemes */}
                        <Box width="100%">
                          <Typography variant="subtitle2" gutterBottom>
                            Sets ({exerciseData.setSchemes.length}):
                          </Typography>
                          <Grid container spacing={1}>
                            {exerciseData.setSchemes.map((setScheme, setIndex) => (
                              <Grid item xs={12} sm={6} md={4} key={setScheme.id}>
                                <Card variant="outlined" sx={{ p: 1 }}>
                                  <Box display="flex" justifyContent="space-between" alignItems="center">
                                    <Typography variant="body2" fontWeight="bold">
                                      Set {setScheme.set_number}
                                    </Typography>
                                    <Box display="flex" gap={0.5}>
                                      {setScheme.is_amrap && (
                                        <Chip label="AMRAP" size="small" color="warning" />
                                      )}
                                      {setScheme.is_emom && (
                                        <Chip label="EMOM" size="small" color="info" />
                                      )}
                                      {setScheme.use_tempo && (
                                        <Chip 
                                          label={formatTempo(
                                            setScheme.eccentric_tempo || '0',
                                            setScheme.isometric_tempo || '0',
                                            setScheme.concentric_tempo || '0'
                                          )}
                                          size="small"
                                          color="secondary"
                                        />
                                      )}
                                    </Box>
                                  </Box>
                                  
                                  <Box mt={1}>
                                    <Typography variant="body2">
                                      <strong>Weight:</strong> {setScheme.target_weight} lbs
                                    </Typography>
                                    <Typography variant="body2">
                                      <strong>Reps:</strong> {setScheme.target_rep_count}
                                    </Typography>
                                    {setScheme.rest_seconds && setScheme.rest_seconds > 0 && (
                                      <Box display="flex" alignItems="center" gap={0.5}>
                                        <TimerIcon fontSize="small" color="action" />
                                        <Typography variant="body2">
                                          {formatRestTime(setScheme.rest_seconds)}
                                        </Typography>
                                      </Box>
                                    )}
                                  </Box>
                                </Card>
                              </Grid>
                            ))}
                          </Grid>
                        </Box>
                      </ListItem>
                      {exerciseIndex < stageData.exercises.length - 1 && <Divider />}
                    </React.Fragment>
                  ))}
                </List>
              </AccordionDetails>
            </Accordion>
          </Grid>
        ))}
      </Grid>
    </Box>
  );
};
