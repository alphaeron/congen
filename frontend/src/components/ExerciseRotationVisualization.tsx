import {
  Speed as SpeedIcon,
  SportsGymnastics as SportsGymnasticsIcon,
  Timeline as TimelineIcon,
  RotateRight as RotateRightIcon,
  Lightbulb as LightbulbIcon,
} from '@mui/icons-material';
import {
  Box,
  Card,
  CardContent,
  Chip,
  Grid,
  Typography,
  Paper,
  Alert,
} from '@mui/material';
import { useSnackbar } from 'notistack';
import React, { useEffect, useState, useMemo } from 'react';

import { LoadingSpinner } from './LoadingSpinner';
import { getPrograms } from '../api/program';
import { getUserExercisePool } from '../api/conjugateWorkoutGenerator';
import type {
  Program,
  UserExercisePoolResponse,
} from '../api/types';
import { useAuth } from '../contexts/AuthContext';

/**
 * Exercise Rotation Visualization component.
 *
 * This component displays the user's exercise rotation patterns, showing how exercises
 * are rotated over time in their programs. It visualizes the undulating periodization
 * and exercise rotation that is central to the conjugate method. It also shows the
 * current exercise pool based on user preferences, equipment, and weak muscles.
 *
 * @return Exercise Rotation Visualization component
 */
export const ExerciseRotationVisualization: React.FC = () => {
  const { user } = useAuth();
  const { enqueueSnackbar } = useSnackbar();
  const [isLoading, setIsLoading] = useState(true);
  const [programs, setPrograms] = useState<Program[]>([]);
  const [exercisePoolData, setExercisePoolData] = useState<UserExercisePoolResponse | null>(null);

  useEffect(() => {
    if (user?.keycloak_id) {
      loadData();
    }
  }, [user?.keycloak_id]);

  const loadData = async () => {
    try {
      setIsLoading(true);

      const [programsData, exercisePoolData] = await Promise.allSettled([
        getPrograms(),
        getUserExercisePool(),
      ]);

      // Handle successful data loads
      if (programsData.status === 'fulfilled') {
        setPrograms(programsData.value);
      }
      if (exercisePoolData.status === 'fulfilled') {
        setExercisePoolData(exercisePoolData.value);
      }

      // Handle any errors
      const errors = [programsData, exercisePoolData].filter(result => result.status === 'rejected');
      if (errors.length > 0) {
        enqueueSnackbar('Some data failed to load. Please refresh the page.', { variant: 'warning' });
      }
    } catch (error) {
      enqueueSnackbar('Failed to load exercise pool data. Please try again.', { variant: 'error' });
    } finally {
      setIsLoading(false);
    }
  };


  // Exercise pool analysis
  const exercisePoolAnalysis = useMemo(() => {
    if (!exercisePoolData) return null;

    return {
      totalExercises: exercisePoolData.total_exercises,
      availableExercises: exercisePoolData.available_exercises,
      categorizedExercises: {
        primary: exercisePoolData.primary_exercises,
        accessory: exercisePoolData.accessory_exercises,
      },
      userEquipment: exercisePoolData.user_equipment,
      userPreferences: exercisePoolData.user_preferences,
      previouslyUsedExercises: exercisePoolData.previously_used_exercises,
    };
  }, [exercisePoolData]);

  if (isLoading) {
    return <LoadingSpinner message="Loading exercise pool data..." />;
  }

  if (!exercisePoolAnalysis) {
    return (
      <Box sx={{ p: 3 }}>
        <Alert severity="info">
          No exercise pool data available. Please check your preferences and equipment settings.
        </Alert>
      </Box>
    );
  }

  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h4" gutterBottom>
        Exercise Pool & Rotation
      </Typography>
      <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
        Your current exercise pool based on preferences, equipment, and rotation patterns.
      </Typography>

      <Grid container spacing={3}>
        {/* Comprehensive Exercise Rotation Overview */}
        {exercisePoolAnalysis && (
          <Grid size={{ xs: 12 }}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                  <RotateRightIcon />
                  Exercise Rotation Overview
                </Typography>
                <Alert severity="info" sx={{ mb: 2 }}>
                  Your exercise pool is filtered based on your available equipment ({exercisePoolAnalysis.userEquipment.length} items) 
                  and exercise preferences ({exercisePoolAnalysis.userPreferences.filter(p => p.should_avoid).length} avoided).
                </Alert>
                <Grid container spacing={2}>
                  <Grid size={{ xs: 12, sm: 6, md: 2 }}>
                    <Paper sx={{ p: 2, textAlign: 'center' }}>
                      <Typography variant="h4" color="primary">
                        {exercisePoolAnalysis.totalExercises}
                      </Typography>
                      <Typography variant="body2">Total Exercises</Typography>
                    </Paper>
                  </Grid>
                  <Grid size={{ xs: 12, sm: 6, md: 2 }}>
                    <Paper sx={{ p: 2, textAlign: 'center' }}>
                      <Typography variant="h4" color="success.main">
                        {exercisePoolAnalysis.availableExercises}
                      </Typography>
                      <Typography variant="body2">Available to You</Typography>
                    </Paper>
                  </Grid>
                  <Grid size={{ xs: 12, sm: 6, md: 2 }}>
                    <Paper sx={{ p: 2, textAlign: 'center' }}>
                      <Typography variant="h4" color="info.main">
                        {exercisePoolAnalysis.categorizedExercises.primary.length}
                      </Typography>
                      <Typography variant="body2">Primary Exercises</Typography>
                    </Paper>
                  </Grid>
                  <Grid size={{ xs: 12, sm: 6, md: 2 }}>
                    <Paper sx={{ p: 2, textAlign: 'center' }}>
                      <Typography variant="h4" color="warning.main">
                        {exercisePoolAnalysis.categorizedExercises.accessory.length}
                      </Typography>
                      <Typography variant="body2">Accessory Exercises</Typography>
                    </Paper>
                  </Grid>
                  <Grid size={{ xs: 12, sm: 6, md: 2 }}>
                    <Paper sx={{ p: 2, textAlign: 'center' }}>
                      <Typography variant="h4" color="error.main">
                        {exercisePoolAnalysis.previouslyUsedExercises.length}
                      </Typography>
                      <Typography variant="body2">Recently Used</Typography>
                    </Paper>
                  </Grid>
                  <Grid size={{ xs: 12, sm: 6, md: 2 }}>
                    <Paper sx={{ p: 2, textAlign: 'center' }}>
                      <Typography variant="h4" color="secondary">
                        {programs.length}
                      </Typography>
                      <Typography variant="body2">Active Programs</Typography>
                    </Paper>
                  </Grid>
                </Grid>
              </CardContent>
            </Card>
          </Grid>
        )}

        {/* Previously Used Exercises */}
        {exercisePoolAnalysis && exercisePoolAnalysis.previouslyUsedExercises.length > 0 && (
          <Grid size={{ xs: 12 }}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                  <TimelineIcon />
                  Recently Used Exercises (Sliding Window)
                </Typography>
                <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                  These exercises have been used in recent weeks and are temporarily excluded from selection to promote variety.
                </Typography>
                <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                  {exercisePoolAnalysis.previouslyUsedExercises.map((exerciseName) => (
                    <Chip
                      key={exerciseName}
                      label={exerciseName}
                      variant="outlined"
                      color="warning"
                      size="small"
                    />
                  ))}
                </Box>
                <Alert severity="info" sx={{ mt: 2 }}>
                  {exercisePoolAnalysis.previouslyUsedExercises.length} exercises are currently in the sliding window exclusion period.
                </Alert>
              </CardContent>
            </Card>
          </Grid>
        )}

        {/* Available Exercise Categories */}
        {exercisePoolAnalysis && (
          <Grid size={{ xs: 12 }}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                  <SpeedIcon />
                  Available Exercise Categories
                </Typography>
                <Grid container spacing={2}>
                  <Grid size={{ xs: 12, sm: 6, md: 3 }}>
                    <Paper sx={{ p: 2, textAlign: 'center' }}>
                      <Typography variant="h4" color="error.main">
                        {exercisePoolAnalysis.categorizedExercises.primary.length}
                      </Typography>
                      <Typography variant="body2">Primary Exercises</Typography>
                      <Box sx={{ mt: 1, display: 'flex', flexWrap: 'wrap', gap: 0.5, justifyContent: 'center' }}>
                        {exercisePoolAnalysis.categorizedExercises.primary.slice(0, 3).map((exercise) => (
                          <Chip
                            key={exercise.name}
                            label={exercise.name}
                            size="small"
                            variant="outlined"
                            color="error"
                          />
                        ))}
                        {exercisePoolAnalysis.categorizedExercises.primary.length > 3 && (
                          <Chip
                            label={`+${exercisePoolAnalysis.categorizedExercises.primary.length - 3}`}
                            size="small"
                            variant="outlined"
                          />
                        )}
                      </Box>
                    </Paper>
                  </Grid>
                  <Grid size={{ xs: 12, sm: 6, md: 3 }}>
                    <Paper sx={{ p: 2, textAlign: 'center' }}>
                      <Typography variant="h4" color="info.main">
                        {exercisePoolAnalysis.categorizedExercises.accessory.length}
                      </Typography>
                      <Typography variant="body2">Accessory Exercises</Typography>
                      <Box sx={{ mt: 1, display: 'flex', flexWrap: 'wrap', gap: 0.5, justifyContent: 'center' }}>
                        {exercisePoolAnalysis.categorizedExercises.accessory.slice(0, 3).map((exercise) => (
                          <Chip
                            key={exercise.name}
                            label={exercise.name}
                            size="small"
                            variant="outlined"
                            color="info"
                          />
                        ))}
                        {exercisePoolAnalysis.categorizedExercises.accessory.length > 3 && (
                          <Chip
                            label={`+${exercisePoolAnalysis.categorizedExercises.accessory.length - 3}`}
                            size="small"
                            variant="outlined"
                          />
                        )}
                      </Box>
                    </Paper>
                  </Grid>
                  <Grid size={{ xs: 12, sm: 6, md: 3 }}>
                    <Paper sx={{ p: 2, textAlign: 'center' }}>
                      <Typography variant="h4" color="success.main">
                        {exercisePoolAnalysis.userEquipment.length}
                      </Typography>
                      <Typography variant="body2">Available Equipment</Typography>
                      <Box sx={{ mt: 1, display: 'flex', flexWrap: 'wrap', gap: 0.5, justifyContent: 'center' }}>
                        {exercisePoolAnalysis.userEquipment.slice(0, 3).map((equipment) => (
                          <Chip
                            key={equipment.equipment_name}
                            label={equipment.equipment_name}
                            size="small"
                            variant="outlined"
                            color="success"
                          />
                        ))}
                        {exercisePoolAnalysis.userEquipment.length > 3 && (
                          <Chip
                            label={`+${exercisePoolAnalysis.userEquipment.length - 3}`}
                            size="small"
                            variant="outlined"
                          />
                        )}
                      </Box>
                    </Paper>
                  </Grid>
                  <Grid size={{ xs: 12, sm: 6, md: 3 }}>
                    <Paper sx={{ p: 2, textAlign: 'center' }}>
                      <Typography variant="h4" color="warning.main">
                        {exercisePoolAnalysis.userPreferences.filter(p => p.should_avoid).length}
                      </Typography>
                      <Typography variant="body2">Avoided Exercises</Typography>
                      <Box sx={{ mt: 1, display: 'flex', flexWrap: 'wrap', gap: 0.5, justifyContent: 'center' }}>
                        {exercisePoolAnalysis.userPreferences.filter(p => p.should_avoid).slice(0, 3).map((pref) => (
                          <Chip
                            key={pref.exercise_name}
                            label={pref.exercise_name}
                            size="small"
                            variant="outlined"
                            color="warning"
                          />
                        ))}
                        {exercisePoolAnalysis.userPreferences.filter(p => p.should_avoid).length > 3 && (
                          <Chip
                            label={`+${exercisePoolAnalysis.userPreferences.filter(p => p.should_avoid).length - 3}`}
                            size="small"
                            variant="outlined"
                          />
                        )}
                      </Box>
                    </Paper>
                  </Grid>
                </Grid>
              </CardContent>
            </Card>
          </Grid>
        )}

        {/* Exercise Rotation Insights */}
        {exercisePoolAnalysis && (
          <Grid size={{ xs: 12 }}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                  <LightbulbIcon />
                  Exercise Rotation Insights
                </Typography>
                <Grid container spacing={2}>
                  <Grid size={{ xs: 12, md: 6 }}>
                    <Alert severity="info">
                      <Typography variant="subtitle2" gutterBottom>
                        Pool Availability
                      </Typography>
                      <Typography variant="body2">
                        You have access to {exercisePoolAnalysis.availableExercises} out of {exercisePoolAnalysis.totalExercises} total exercises 
                        ({((exercisePoolAnalysis.availableExercises / exercisePoolAnalysis.totalExercises) * 100).toFixed(1)}% availability).
                      </Typography>
                    </Alert>
                  </Grid>
                  <Grid size={{ xs: 12, md: 6 }}>
                    <Alert severity="success">
                      <Typography variant="subtitle2" gutterBottom>
                        Exercise Variety
                      </Typography>
                      <Typography variant="body2">
                        Your pool includes {exercisePoolAnalysis.categorizedExercises.primary.length} primary exercises and {exercisePoolAnalysis.categorizedExercises.accessory.length} accessory exercises, 
                        providing good variety for conjugate training.
                      </Typography>
                    </Alert>
                  </Grid>
                  <Grid size={{ xs: 12, md: 6 }}>
                    <Alert severity="warning">
                      <Typography variant="subtitle2" gutterBottom>
                        Rotation Management
                      </Typography>
                      <Typography variant="body2">
                        {exercisePoolAnalysis.previouslyUsedExercises.length} exercises are currently in the sliding window exclusion period, 
                        ensuring proper exercise rotation and preventing accommodation.
                      </Typography>
                    </Alert>
                  </Grid>
                  <Grid size={{ xs: 12, md: 6 }}>
                    <Alert severity="info">
                      <Typography variant="subtitle2" gutterBottom>
                        Program Integration
                      </Typography>
                      <Typography variant="body2">
                        Your exercise pool is integrated with {programs.length} active program{programs.length !== 1 ? 's' : ''}, 
                        ensuring consistent exercise selection across your training.
                      </Typography>
                    </Alert>
                  </Grid>
                </Grid>
              </CardContent>
            </Card>
          </Grid>
        )}
      </Grid>
    </Box>
  );
};
