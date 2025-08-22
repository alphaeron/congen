import React, { useEffect, useState } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Grid,
  Chip,
  Divider,
  CircularProgress,
  Alert,
  Accordion,
  AccordionSummary,
  AccordionDetails,
  IconButton,
  Tooltip,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
} from '@mui/material';
import {
  ExpandMore as ExpandMoreIcon,
  Timer as TimerIcon,
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
  onWorkoutDetailsUpdate?: (workoutDetails: { name: string; day_number: number; stages: number }) => void;
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
export const WorkoutDetail: React.FC<WorkoutDetailProps> = ({ workoutId, onBack, onWorkoutDetailsUpdate }) => {
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

  // Update parent component with workout details for breadcrumb
  useEffect(() => {
    if (workout && stages.length > 0 && onWorkoutDetailsUpdate) {
      onWorkoutDetailsUpdate({
        name: workout.name,
        day_number: workout.day_number,
        stages: stages.length
      });
    }
  }, [workout, stages, onWorkoutDetailsUpdate]);

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
      {/* Sticky Table Header */}
      <Box 
        position="sticky" 
        top={48} 
        zIndex={999} 
        sx={{ 
          bgcolor: 'background.paper', 
          boxShadow: 1,
          borderBottom: 1,
          borderColor: 'divider'
        }}
      >
        <TableContainer component={Paper} sx={{ width: '100%' }}>
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell sx={{ width: '25%', fontWeight: 'bold' }}>Exercise</TableCell>
                <TableCell sx={{ width: '10%', fontWeight: 'bold' }}>Sets</TableCell>
                <TableCell sx={{ width: '10%', fontWeight: 'bold' }}>Reps</TableCell>
                <TableCell sx={{ width: '15%', fontWeight: 'bold' }}>Tempo</TableCell>
                <TableCell sx={{ width: '15%', fontWeight: 'bold' }}>Weight</TableCell>
                <TableCell sx={{ width: '15%', fontWeight: 'bold' }}>Rest</TableCell>
                <TableCell sx={{ width: '10%', fontWeight: 'bold' }}>Notes</TableCell>
              </TableRow>
            </TableHead>
          </Table>
        </TableContainer>
      </Box>

      {/* Workout Stages */}
      <Box sx={{ mt: 2 }}>
        {stages.map((stageData: WorkoutStageWithExercises, stageIndex: number) => (
          <Accordion key={stageData.stage.id} defaultExpanded={stageIndex === 0} sx={{ mb: 2 }}>
            <AccordionSummary expandIcon={<ExpandMoreIcon />}>
              <Box display="flex" alignItems="center" gap={2}>
                <Typography variant="h6">
                  {stageData.stage.name}
                </Typography>
                <Chip 
                  label={`${stageData.exercises.length} exercises`}
                  size="small"
                  color="secondary"
                />
              </Box>
            </AccordionSummary>
            <AccordionDetails>
              <TableContainer component={Paper} sx={{ width: '100%' }}>
                <Table size="small">
                  <TableBody>
                    {stageData.exercises.map((exercise: ProgrammedExerciseWithSetSchemes) => {
                      const setSchemes = exercise.setSchemes || [];
                      if (setSchemes.length === 0) return null;

                      // Aggregate set scheme data
                      const firstSetScheme = setSchemes[0];
                      const totalSets = setSchemes.length;
                      const reps = firstSetScheme.target_rep_count;
                      const weight = firstSetScheme.target_weight;
                      const rest = firstSetScheme.rest_seconds;
                      
                      // Format tempo if available
                      const tempo = firstSetScheme.use_tempo && firstSetScheme.eccentric_tempo && firstSetScheme.isometric_tempo && firstSetScheme.concentric_tempo
                        ? `${firstSetScheme.eccentric_tempo}-${firstSetScheme.isometric_tempo}-${firstSetScheme.concentric_tempo}`
                        : '-';

                      return (
                        <TableRow key={exercise.exercise.id}>
                          <TableCell sx={{ width: '25%' }}>
                            <Box display="flex" alignItems="center" gap={1}>
                              <Typography variant="body2">
                                {exercise.exercise.exercise_name}
                              </Typography>
                              {exercise.exercise.notes && (
                                <Tooltip title={exercise.exercise.notes} arrow>
                                  <IconButton size="small">
                                    <NotesIcon fontSize="small" />
                                  </IconButton>
                                </Tooltip>
                              )}
                            </Box>
                          </TableCell>
                          <TableCell sx={{ width: '10%' }}>
                            <Typography variant="body2">
                              {totalSets}
                            </Typography>
                          </TableCell>
                          <TableCell sx={{ width: '10%' }}>
                            <Typography variant="body2">
                              {reps || '-'}
                            </Typography>
                          </TableCell>
                          <TableCell sx={{ width: '15%' }}>
                            <Box display="flex" alignItems="center" gap={0.5}>
                              <TimerIcon fontSize="small" />
                              <Typography variant="body2">
                                {tempo}
                              </Typography>
                            </Box>
                          </TableCell>
                          <TableCell sx={{ width: '15%' }}>
                            <Typography variant="body2">
                              {weight ? `${weight} lbs` : '-'}
                            </Typography>
                          </TableCell>
                          <TableCell sx={{ width: '15%' }}>
                            <Typography variant="body2">
                              {rest ? `${rest}s` : '-'}
                            </Typography>
                          </TableCell>
                          <TableCell sx={{ width: '10%' }}>
                            <Typography variant="body2">
                              -
                            </Typography>
                          </TableCell>
                        </TableRow>
                      );
                    })}
                  </TableBody>
                </Table>
              </TableContainer>
            </AccordionDetails>
          </Accordion>
        ))}
      </Box>
    </Box>
  );
};
