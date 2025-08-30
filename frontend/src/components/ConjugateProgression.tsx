import {
  Box,
  Card,
  CardContent,
  Grid,
  Typography,
  CircularProgress,
} from '@mui/material';
import { useSnackbar } from 'notistack';
import React, { useEffect, useState, useMemo } from 'react';

import { getUserDataExport } from '../api/gdpr';
import { getIndividualExercise } from '../api/exercise';
import type { 
  User, 
  UserDataExport,
  ProgrammedWorkoutWithStages,
  Exercise,
  ProgramWithWorkouts
} from '../api/types';
import { categorizeExerciseVolume } from '../common/utils';
import { LineChart } from './LineChart';
import { PieChart } from './PieChart';

interface ConjugateProgressionProps {
  user: User;
}

interface ExerciseCorrelation {
  exercise: string;
  category: string; // Will be the workout stage name
  volume: number;
  frequency: number;
  maxWeight: number;
}

interface ProgressData {
  date: string;
  exercise: string;
  weight: number;
  type: '1RM' | 'Volume';
}



/**
 * Enhanced Conjugate Progression component displaying actual user statistics and progress.
 *
 * Based on Westside Barbell conjugate method principles, shows:
 * - Volume tracking (total weight lifted including bands)
 * - Exercise volume by workout stage analysis
 * - Progress tracking (1RM improvements over time)
 * - Training intensity distribution
 *
 * @param user The user data
 * @return Enhanced conjugate progression component
 */
export const ConjugateProgression: React.FC<ConjugateProgressionProps> = ({ user }) => {
  const { enqueueSnackbar } = useSnackbar();
  const [userData, setUserData] = useState<UserDataExport | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [exerciseData, setExerciseData] = useState<Map<string, Exercise>>(new Map());

  // Load all workout data using optimized single API call
  useEffect(() => {
    const loadWorkoutData = async () => {
      try {
        setIsLoading(true);

        // Load all data in a single optimized call
        const dataExport = await getUserDataExport();
        setUserData(dataExport);

        // Fetch exercise data for all unique exercises
        // Handle case where user has no training programs (empty array)
        const uniqueExercises = new Set<string>();
        dataExport.training_programs?.forEach((program: ProgramWithWorkouts) => {
          program.workouts.forEach(workout => {
            workout.stages.forEach(stage => {
              stage.exercises.forEach(exercise => {
                uniqueExercises.add(exercise.exercise.exercise_name);
              });
            });
          });
        });

        const exerciseMap = new Map<string, Exercise>();
        for (const exerciseName of Array.from(uniqueExercises)) {
          try {
            const exercise = await getIndividualExercise(exerciseName);
            exerciseMap.set(exerciseName, exercise);
          } catch (err) {
            enqueueSnackbar(`Error fetching exercise data for ${exerciseName}`, { variant: 'error' });
          }
        }

        setExerciseData(exerciseMap);
      } catch (err) {
        enqueueSnackbar('Failed to load workout data. Please try again.', { variant: 'error' });
      } finally {
        setIsLoading(false);
      }
    };

    loadWorkoutData();
  }, [user.keycloak_id]);

  // Calculate volume data for charts
  const volumeData = useMemo(() => {
    if (!userData?.training_programs?.length) return [];

    const allWorkouts: ProgrammedWorkoutWithStages[] = [];
    userData.training_programs.forEach(program => {
      allWorkouts.push(...program.workouts);
    });

    return allWorkouts.map((workoutData) => {
      let totalVolume = 0;
      let maxEffortVolume = 0;
      let dynamicEffortVolume = 0;
      let accessoryVolume = 0;

      workoutData.stages.forEach((stage) => {
        stage.exercises.forEach((exerciseWithSchemes) => {
          exerciseWithSchemes.set_schemes.forEach((setScheme) => {
            const weight = setScheme.performed_weight || setScheme.target_weight || 0;
            const reps = setScheme.performed_rep_count || setScheme.target_rep_count || 0;
            const bandWeight = setScheme.band_weight_lbs ? 
              (setScheme.band_weight_lbs as { weight_lbs: number })?.weight_lbs || 0 : 0;
            
            const totalWeight = weight + bandWeight;
            const setVolume = totalWeight * reps;

            // Get exercise data and categorize volume using shared helper
            const exerciseName = exerciseWithSchemes.exercise.exercise_name;
            const exerciseInfo = exerciseData.get(exerciseName);
            const categorizedVolume = categorizeExerciseVolume(
              exerciseInfo,
              workoutData.workout.name,
              setVolume
            );
            
            maxEffortVolume += categorizedVolume.maxEffortVolume;
            dynamicEffortVolume += categorizedVolume.dynamicEffortVolume;
            accessoryVolume += categorizedVolume.accessoryVolume;
          });
        });
      });

      return {
        date: new Date(workoutData.workout.created_at).toLocaleDateString(),
        totalVolume: Math.round(totalVolume + maxEffortVolume + dynamicEffortVolume + accessoryVolume),
        maxEffortVolume: Math.round(maxEffortVolume),
        dynamicEffortVolume: Math.round(dynamicEffortVolume),
        accessoryVolume: Math.round(accessoryVolume),
      };
    }).slice(-10); // Last 10 workouts
  }, [userData, exerciseData]);

  // Calculate exercise volume by workout stage data
  const exerciseCorrelationData = useMemo(() => {
    if (!userData?.training_programs?.length) return [];

    const stageVolumeMap = new Map<string, number>();
    const allWorkouts: ProgrammedWorkoutWithStages[] = [];
    userData.training_programs.forEach(program => {
      allWorkouts.push(...program.workouts);
    });

    allWorkouts.forEach((workoutData) => {
      workoutData.stages.forEach((stage) => {
        const stageName = stage.stage.name || 'Unknown Stage';
        
        stage.exercises.forEach((exerciseWithSchemes) => {
          // Calculate volume for this exercise in this stage
          let stageVolume = 0;
          exerciseWithSchemes.set_schemes.forEach((setScheme) => {
            const weight = setScheme.performed_weight || setScheme.target_weight || 0;
            const reps = setScheme.performed_rep_count || setScheme.target_rep_count || 0;
            const bandWeight = setScheme.band_weight_lbs ? 
              (setScheme.band_weight_lbs as { weight_lbs: number })?.weight_lbs || 0 : 0;
            
            stageVolume += (weight + bandWeight) * reps;
          });

          // Add to stage volume
          const currentVolume = stageVolumeMap.get(stageName) || 0;
          stageVolumeMap.set(stageName, currentVolume + stageVolume);
        });
      });
    });

    // Convert to the expected format for the donut chart
    return Array.from(stageVolumeMap.entries()).map(([stageName, volume]) => ({
      exercise: stageName, // Using stage name as exercise name for the chart
      category: stageName,
      volume,
      frequency: 1, // Not used for donut chart
      maxWeight: 0, // Not used for donut chart
    }));
  }, [userData, exerciseData]);

  // Calculate progress data
  const progressData = useMemo(() => {
    const progress: ProgressData[] = [];

    // Add 1RM data
    if (userData?.user_one_rep_max) {
      userData.user_one_rep_max.forEach((oneRepMax) => {
        const typedOneRepMax = oneRepMax as { updated_at: string; exercise_name: string; one_rep_max: number };
        progress.push({
          date: new Date(typedOneRepMax.updated_at).toLocaleDateString(),
          exercise: typedOneRepMax.exercise_name,
          weight: typedOneRepMax.one_rep_max,
          type: '1RM',
        });
      });
    }

    // Add volume data from recent workouts
    volumeData.forEach((volume) => {
      progress.push({
        date: volume.date,
        exercise: 'Total Volume',
        weight: volume.totalVolume,
        type: 'Volume',
      });
    });

    return progress.sort((a, b) => new Date(a.date).getTime() - new Date(b.date).getTime());
  }, [userData, volumeData]);

  // Prepare chart data
  const volumeChartData = useMemo(() => [
    {
      id: 'Total Volume',
      data: volumeData.map(d => ({ x: d.date, y: d.totalVolume })),
    },
    {
      id: 'Max Effort',
      data: volumeData.map(d => ({ x: d.date, y: d.maxEffortVolume })),
    },
    {
      id: 'Dynamic Effort',
      data: volumeData.map(d => ({ x: d.date, y: d.dynamicEffortVolume })),
    },
    {
      id: 'Accessory',
      data: volumeData.map(d => ({ x: d.date, y: d.accessoryVolume })),
    },
  ], [volumeData]);

  const correlationChartData = useMemo(() => {
    // Aggregate volume by workout stage
    const stageVolumeMap = new Map<string, number>();
    
    exerciseCorrelationData.forEach(ex => {
      const currentVolume = stageVolumeMap.get(ex.category) || 0;
      stageVolumeMap.set(ex.category, currentVolume + ex.volume);
    });
    
    const result = Array.from(stageVolumeMap.entries()).map(([category, volume]) => ({
      category,
      volume,
    }));

    return result;
  }, [exerciseCorrelationData]);

  const progressChartData = useMemo(() => [
    {
      id: '1RM Progress',
      data: progressData
        .filter(d => d.type === '1RM')
        .map(d => ({ x: d.date, y: d.weight })),
    },
    {
      id: 'Volume Progress',
      data: progressData
        .filter(d => d.type === 'Volume')
        .map(d => ({ x: d.date, y: d.weight / 1000 })), // Scale down for visibility
    },
  ], [progressData]);

  if (isLoading) {
    return (
      <Card sx={{ mb: 4 }}>
        <CardContent>
          <Box display="flex" justifyContent="center" alignItems="center" minHeight={400}>
            <CircularProgress />
          </Box>
        </CardContent>
      </Card>
    );
  }



  if (!userData?.training_programs || userData.training_programs.length === 0) {
    return (
      <Card sx={{ mb: 4 }}>
        <CardContent>
          <Typography variant="h6" gutterBottom>
            Conjugate Progress Tracking
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Complete your first workout to see progress statistics and correlations.
          </Typography>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card sx={{ mb: 4 }}>
      <CardContent>
        <Typography variant="h6" gutterBottom>
          Conjugate Progress Tracking
        </Typography>
        <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
          Based on Westside Barbell conjugate method principles - tracking volume, correlations, and progress
        </Typography>

        <Grid container spacing={3}>
          {/* Volume Tracking Chart */}
          <Grid item xs={12} lg={8}>
            <LineChart 
              data={volumeChartData}
              title="Volume Progression"
              description="Total weight lifted over time (including band resistance)"
              xAxisLabel="Workout Date"
              yAxisLabel="Volume (lbs)"
            />
          </Grid>

          {/* Exercise Category Distribution */}
          <Grid item xs={12} lg={4}>
            <PieChart 
              data={correlationChartData.map(d => ({
                id: d.category,
                label: d.category,
                value: d.volume,
              }))}
              title="Exercise Distribution"
              description="Volume by workout stage"
            />
          </Grid>

          {/* Progress Tracking */}
          <Grid item xs={12}>
            <LineChart 
              data={progressChartData}
              title="Progress Tracking"
              description="1RM improvements and volume progression over time"
              xAxisLabel="Date"
              yAxisLabel="Weight (lbs)"
            />
          </Grid>

                            {/* Key Performance Indicators */}
                  <Grid item xs={12}>
                    <Card variant="outlined">
                      <CardContent>
                        <Typography variant="h6" gutterBottom>
                          Key Performance Indicators
                        </Typography>
                        <Grid container spacing={2}>
                          <Grid item xs={12} sm={6} md={3}>
                            <Box textAlign="center">
                              <Typography variant="h4" color="primary">
                                {userData?.training_programs?.reduce((total, program) => total + program.workouts.length, 0) || 0}
                              </Typography>
                              <Typography variant="body2" color="text.secondary">
                                Total Workouts
                              </Typography>
                            </Box>
                          </Grid>
                          <Grid item xs={12} sm={6} md={3}>
                            <Box textAlign="center">
                              <Typography variant="h4" color="secondary">
                                {Math.round(volumeData.reduce((sum, d) => sum + d.totalVolume, 0) / 1000)}k
                              </Typography>
                              <Typography variant="body2" color="text.secondary">
                                Total Volume (lbs)
                              </Typography>
                            </Box>
                          </Grid>
                          <Grid item xs={12} sm={6} md={3}>
                            <Box textAlign="center">
                              <Typography variant="h4" color="success">
                                {userData?.user_one_rep_max?.length || 0}
                              </Typography>
                              <Typography variant="body2" color="text.secondary">
                                1RM Records
                              </Typography>
                            </Box>
                          </Grid>
                          <Grid item xs={12} sm={6} md={3}>
                            <Box textAlign="center">
                              <Typography variant="h4" color="info">
                                {Math.round(volumeData[volumeData.length - 1]?.totalVolume || 0)}
                              </Typography>
                              <Typography variant="body2" color="text.secondary">
                                Latest Volume (lbs)
                              </Typography>
                            </Box>
                          </Grid>
                        </Grid>
                      </CardContent>
                    </Card>
                  </Grid>        
        </Grid>
      </CardContent>
    </Card>
  );
};
