import { default as BarChartIcon } from '@mui/icons-material/BarChart';
import { default as FitnessCenterIcon } from '@mui/icons-material/FitnessCenter';
import { default as ShowChartIcon } from '@mui/icons-material/ShowChart';
import { default as TrendingUpIcon } from '@mui/icons-material/TrendingUp';
import {
  Box,
  Card,
  CardContent,
  Grid,
  Typography,
  CircularProgress,
} from '@mui/material';
import { ResponsiveBump } from '@nivo/bump';
import { ResponsiveIcicle } from '@nivo/icicle';
import { ResponsiveStream } from '@nivo/stream';
import { useSnackbar } from 'notistack';
import React, { useEffect, useState, useMemo } from 'react';

import { getUserDataExport } from '../api/gdpr';
import type { 
  User, 
  ProgrammedWorkout, 
  WorkoutStage, 
  ProgrammedExercise, 
  SetScheme,
  Program
} from '../api/types';
import { congenNivoTheme, congenLegendConfig } from '../theme/nivoTheme';

interface WorkoutAnalyticsProps {
  user: User;
}

interface WorkoutData {
  workout: ProgrammedWorkout;
  stages: WorkoutStageWithExercises[];
}

interface WorkoutStageWithExercises {
  stage: WorkoutStage;
  exercises: ProgrammedExerciseWithSetSchemes[];
}

interface ProgrammedExerciseWithSetSchemes {
  exercise: ProgrammedExercise;
  set_schemes: SetScheme[];
}

interface WorkoutVolumeData {
  date: string;
  totalVolume: number;
  maxEffortVolume: number;
  dynamicEffortVolume: number;
  accessoryVolume: number;
}

interface ExerciseRankingData {
  id: string;
  data: Array<{
    x: string;
    y: number;
  }>;
}

/**
 * Workout Analytics component providing workout-specific visualizations.
 *
 * Features:
 * - Icicle charts for training structure hierarchy
 * - Stream charts for volume flow over time
 * - Bump charts for exercise ranking trends
 *
 * @param user The user data
 * @return Workout Analytics component
 */
export const WorkoutAnalytics: React.FC<WorkoutAnalyticsProps> = ({ user }) => {
  const { enqueueSnackbar } = useSnackbar();
  const [workouts, setWorkouts] = useState<WorkoutData[]>([]);
  const [programs, setPrograms] = useState<Program[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  // Load all workout data using optimized GDPR export endpoint
  useEffect(() => {
    const loadWorkoutData = async () => {
      try {
        setIsLoading(true);

        // Use optimized single query to get all workout data
        const userDataExport = await getUserDataExport();
        
        // Extract programs and workouts from the export data
        const programsData = userDataExport.training_programs.map((p: any) => p.program);
        const workoutsData = userDataExport.training_programs.flatMap((program: any) => 
          program.workouts.map((workoutWithStages: any) => ({
            workout: workoutWithStages.workout,
            stages: workoutWithStages.stages.map((stageWithExercises: any) => ({
              stage: stageWithExercises.stage,
              exercises: stageWithExercises.exercises.map((exerciseWithSetSchemes: any) => ({
                exercise: exerciseWithSetSchemes.exercise,
                set_schemes: exerciseWithSetSchemes.set_schemes
              }))
            }))
          }))
        );

        setWorkouts(workoutsData);
        setPrograms(programsData);
      } catch (err) {
        enqueueSnackbar('Failed to load workout analytics data. Please try again.', { variant: 'error' });
        console.error('Error loading workout analytics data:', err);
      } finally {
        setIsLoading(false);
      }
    };

    loadWorkoutData();
  }, [user.keycloak_id]);

  // Calculate workout volume data for stream chart
  const volumeData = useMemo(() => {
    if (!workouts.length) return [];

    return workouts.map((workoutData) => {
      let totalVolume = 0;
      let maxEffortVolume = 0;
      let dynamicEffortVolume = 0;
      let accessoryVolume = 0;

      workoutData.stages.forEach((stage) => {
        stage.exercises.forEach((exerciseData) => {
          exerciseData.set_schemes.forEach((setScheme) => {
            const weight = setScheme.performed_weight || setScheme.target_weight || 0;
            const reps = setScheme.performed_rep_count || setScheme.target_rep_count || 0;
            const bandWeight = setScheme.band_weight_lbs ? 
              (setScheme.band_weight_lbs as any)?.weight_lbs || 0 : 0;
            
            const totalWeight = weight + bandWeight;
            const setVolume = totalWeight * reps;

            // Categorize by exercise type
            const exerciseName = exerciseData.exercise.exercise_name.toLowerCase();
            const isBigLift = ['squat', 'bench', 'deadlift', 'overhead press'].some(lift => 
              exerciseName.includes(lift)
            );
            const isAccessory = exerciseName.includes('curl') || exerciseName.includes('extension') || 
                               exerciseName.includes('fly') || exerciseName.includes('raise');

            if (isBigLift) {
              if (weight > 200) { // Assume heavy weight = max effort
                maxEffortVolume += setVolume;
              } else {
                dynamicEffortVolume += setVolume;
              }
            } else if (isAccessory) {
              accessoryVolume += setVolume;
            } else {
              totalVolume += setVolume;
            }
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
  }, [workouts]);

  // Calculate exercise ranking data for bump chart
  const exerciseRankingData = useMemo(() => {
    if (!workouts.length) return [];

    const exerciseStats = new Map<string, { volume: number; frequency: number }>();
    
    workouts.forEach((workoutData) => {
      workoutData.stages.forEach((stage) => {
        stage.exercises.forEach((exerciseData) => {
          const exerciseName = exerciseData.exercise.exercise_name;
          const existing = exerciseStats.get(exerciseName) || {
            volume: 0,
            frequency: 0,
          };

          // Calculate volume for this exercise
          let exerciseVolume = 0;
          exerciseData.set_schemes.forEach((setScheme) => {
            const weight = setScheme.performed_weight || setScheme.target_weight || 0;
            const reps = setScheme.performed_rep_count || setScheme.target_rep_count || 0;
            const bandWeight = setScheme.band_weight_lbs ? 
              (setScheme.band_weight_lbs as any)?.weight_lbs || 0 : 0;
            exerciseVolume += (weight + bandWeight) * reps;
          });

          existing.volume += exerciseVolume;
          existing.frequency += 1;
          exerciseStats.set(exerciseName, existing);
        });
      });
    });

    // Get top 8 exercises by volume
    const topExercises = Array.from(exerciseStats.entries())
      .sort((a, b) => b[1].volume - a[1].volume)
      .slice(0, 8)
      .map(([name]) => name);

    return topExercises.map(exerciseName => ({
      id: exerciseName,
      data: volumeData.map((volume, index) => ({
        x: `Workout ${index + 1}`,
        y: exerciseStats.get(exerciseName)?.volume || 0,
      })),
    }));
  }, [workouts, volumeData]);

  // Calculate training structure data for icicle chart
  const trainingStructureData = useMemo(() => {
    if (!workouts.length || !programs.length) return null;

    const activeProgram = programs.find(program => program.is_active);
    if (!activeProgram) return null;

    const programWorkouts = workouts.filter(workout => 
      workout.workout.program_id === activeProgram.id
    );

    return {
      name: activeProgram.name,
      loc: programWorkouts.reduce((sum, workout) => {
        let workoutVolume = 0;
        workout.stages.forEach(stage => {
          stage.exercises.forEach(exercise => {
            exercise.set_schemes.forEach(setScheme => {
              const weight = setScheme.performed_weight || setScheme.target_weight || 0;
              const reps = setScheme.performed_rep_count || setScheme.target_rep_count || 0;
              const bandWeight = setScheme.band_weight_lbs ? 
                (setScheme.band_weight_lbs as any)?.weight_lbs || 0 : 0;
              workoutVolume += (weight + bandWeight) * reps;
            });
          });
        });
        return sum + workoutVolume;
      }, 0),
      children: programWorkouts.slice(0, 5).map(workout => {
        let workoutVolume = 0;
        const exerciseCategories = new Map<string, number>();

        workout.stages.forEach(stage => {
          stage.exercises.forEach(exercise => {
            exercise.set_schemes.forEach(setScheme => {
              const weight = setScheme.performed_weight || setScheme.target_weight || 0;
              const reps = setScheme.performed_rep_count || setScheme.target_rep_count || 0;
              const bandWeight = setScheme.band_weight_lbs ? 
                (setScheme.band_weight_lbs as any)?.weight_lbs || 0 : 0;
              const setVolume = (weight + bandWeight) * reps;
              workoutVolume += setVolume;

              // Categorize exercise
              const exerciseName = exercise.exercise.exercise_name.toLowerCase();
              let category = 'Other';
              if (['squat', 'bench', 'deadlift', 'press'].some(lift => exerciseName.includes(lift))) {
                category = 'Compound Lifts';
              } else if (['curl', 'extension', 'fly', 'raise'].some(keyword => exerciseName.includes(keyword))) {
                category = 'Accessory Work';
              }

              exerciseCategories.set(category, (exerciseCategories.get(category) || 0) + setVolume);
            });
          });
        });

        return {
          name: workout.workout.name,
          loc: workoutVolume,
          children: Array.from(exerciseCategories.entries()).map(([category, volume]) => ({
            name: category,
            loc: volume,
          })),
        };
      }),
    };
  }, [workouts, programs]);

  // Prepare stream chart data
  const streamData = useMemo(() => {
    return volumeData.map(volume => ({
      date: volume.date,
      'Max Effort': volume.maxEffortVolume,
      'Dynamic Effort': volume.dynamicEffortVolume,
      'Accessory': volume.accessoryVolume,
    }));
  }, [volumeData]);

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

  if (!workouts.length) {
    return (
      <Card sx={{ mb: 4 }}>
        <CardContent>
          <Typography variant="h6" gutterBottom>
            Workout Analytics
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Complete your first workout to see workout analytics and insights.
          </Typography>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card sx={{ mb: 4 }}>
      <CardContent>
        <Typography variant="h6" gutterBottom>
          Workout Analytics
        </Typography>
        <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
          Visualize your workout structure, volume flow, and exercise trends
        </Typography>

        <Grid container spacing={3}>
          {/* Icicle Chart - Training Structure */}
          {trainingStructureData && (
            <Grid item xs={12} lg={6}>
              <Card variant="outlined">
                <CardContent>
                  <Box display="flex" alignItems="center" gap={1} sx={{ mb: 2 }}>
                    <FitnessCenterIcon color="primary" />
                    <Typography variant="h6">Training Structure Hierarchy</Typography>
                  </Box>
                  <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                    Program → Workout → Exercise category breakdown
                  </Typography>
                  <Box sx={{ height: 400 }}>
                    <ResponsiveIcicle
                      data={trainingStructureData}
                      margin={{ top: 10, right: 10, bottom: 10, left: 10 }}
                      value="loc"
                      colors={{ scheme: 'category10' }}
                      theme={congenNivoTheme}
                      enableLabels={true}
                      labelTextColor={{
                        from: 'color',
                        modifiers: [['darker', 1.4]],
                      }}
                      borderWidth={1}
                      borderColor={{
                        from: 'color',
                        modifiers: [['darker', 0.1]],
                      }}
                      borderRadius={3}
                    />
                  </Box>
                </CardContent>
              </Card>
            </Grid>
          )}

          {/* Stream Chart - Volume Flow */}
          <Grid item xs={12} lg={6}>
            <Card variant="outlined">
              <CardContent>
                <Box display="flex" alignItems="center" gap={1} sx={{ mb: 2 }}>
                  <ShowChartIcon color="secondary" />
                  <Typography variant="h6">Volume Flow Over Time</Typography>
                </Box>
                <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                  Training volume distribution across workout types
                </Typography>
                <Box sx={{ height: 400 }}>
                  <ResponsiveStream
                    data={streamData}
                    keys={['Max Effort', 'Dynamic Effort', 'Accessory']}
                    margin={{ top: 50, right: 110, bottom: 50, left: 60 }}
                    colors={{ scheme: 'nivo' }}
                    theme={congenNivoTheme}
                  />
                </Box>
              </CardContent>
            </Card>
          </Grid>

          {/* Bump Chart - Exercise Ranking */}
          <Grid item xs={12}>
            <Card variant="outlined">
              <CardContent>
                <Box display="flex" alignItems="center" gap={1} sx={{ mb: 2 }}>
                  <TrendingUpIcon color="success" />
                  <Typography variant="h6">Exercise Volume Ranking Trends</Typography>
                </Box>
                <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                  How exercise volume rankings change across workouts
                </Typography>
                <Box sx={{ height: 400 }}>
                  <ResponsiveBump
                    data={exerciseRankingData}
                    margin={{ top: 40, right: 100, bottom: 40, left: 60 }}
                    colors={{ scheme: 'nivo' }}
                    theme={congenNivoTheme}
                  />
                </Box>
              </CardContent>
            </Card>
          </Grid>
        </Grid>
      </CardContent>
    </Card>
  );
};
