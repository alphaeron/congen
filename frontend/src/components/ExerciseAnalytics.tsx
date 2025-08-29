import { default as BarChartIcon } from '@mui/icons-material/BarChart';
import { default as FitnessCenterIcon } from '@mui/icons-material/FitnessCenter';
import { default as ShowChartIcon } from '@mui/icons-material/ShowChart';
import { default as SpeedIcon } from '@mui/icons-material/Speed';
import { default as TrendingUpIcon } from '@mui/icons-material/TrendingUp';
import {
  Box,
  Card,
  CardContent,
  Grid,
  Typography,
  CircularProgress,
} from '@mui/material';
import { ResponsiveIcicle } from '@nivo/icicle';
import { ResponsiveRadialBar } from '@nivo/radial-bar';
import { ResponsiveSunburst } from '@nivo/sunburst';
import { useSnackbar } from 'notistack';
import React, { useEffect, useState, useMemo } from 'react';

import { getUserDataExport } from '../api/gdpr';
import { getExerciseMuscle } from '../api/exerciseMuscle';
import { getExerciseEquipment } from '../api/exerciseEquipment';
import type { 
  User, 
  ProgrammedWorkout, 
  WorkoutStage, 
  ProgrammedExercise, 
  SetScheme,
  UserOneRepMax,
  ExerciseMuscle,
  ExerciseEquipment
} from '../api/types';
import { congenNivoTheme } from '../theme/nivoTheme';

interface ExerciseAnalyticsProps {
  user: User;
}

interface ExerciseData {
  name: string;
  totalVolume: number;
  frequency: number;
  maxWeight: number;
  avgWeight: number;
  totalSets: number;
  muscleGroup: string;
  equipment: string;
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

/**
 * Exercise Analytics component providing advanced visualizations for exercise data.
 *
 * Features:
 * - Radial bar charts for exercise performance
 * - Sunburst charts for exercise hierarchy
 * - Treemap for exercise volume distribution
 * - Icicle charts for program structure
 *
 * @param user The user data
 * @return Exercise Analytics component
 */
export const ExerciseAnalytics: React.FC<ExerciseAnalyticsProps> = ({ user }) => {
  const { enqueueSnackbar } = useSnackbar();
  const [workouts, setWorkouts] = useState<WorkoutData[]>([]);
  const [oneRepMaxes, setOneRepMaxes] = useState<UserOneRepMax[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [exerciseMuscleData, setExerciseMuscleData] = useState<Map<string, string[]>>(new Map());
  const [exerciseEquipmentData, setExerciseEquipmentData] = useState<Map<string, string[]>>(new Map());

  // Load all workout data using optimized GDPR export endpoint
  useEffect(() => {
    const loadWorkoutData = async () => {
      try {
        setIsLoading(true);

        // Use optimized single query to get all workout data
        const userDataExport = await getUserDataExport();
        
        // Extract workouts and one-rep maxes from the export data
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
        setOneRepMaxes(userDataExport.user_one_rep_max as unknown as UserOneRepMax[]);

        // Fetch all exercise muscle and equipment data in bulk (only 2 API calls instead of 278)
        try {
          const [allExerciseMuscles, allExerciseEquipment] = await Promise.all([
            getExerciseMuscle(),
            getExerciseEquipment()
          ]);

          // Transform the bulk data into Maps for efficient lookup
          const muscleData = new Map<string, string[]>();
          const equipmentData = new Map<string, string[]>();

          // Group muscles by exercise name
          allExerciseMuscles.forEach((exerciseMuscle: ExerciseMuscle) => {
            const exerciseName = exerciseMuscle.exercise_name;
            const existing = muscleData.get(exerciseName) || [];
            if (!existing.includes(exerciseMuscle.muscle_name)) {
              existing.push(exerciseMuscle.muscle_name);
            }
            muscleData.set(exerciseName, existing);
          });

          // Group equipment by exercise name
          allExerciseEquipment.forEach((exerciseEquipment: ExerciseEquipment) => {
            const exerciseName = exerciseEquipment.exercise_name;
            const existing = equipmentData.get(exerciseName) || [];
            if (!existing.includes(exerciseEquipment.equipment_name)) {
              existing.push(exerciseEquipment.equipment_name);
            }
            equipmentData.set(exerciseName, existing);
          });

          setExerciseMuscleData(muscleData);
          setExerciseEquipmentData(equipmentData);
        } catch (err) {
          enqueueSnackbar('Failed to load exercise muscle and equipment data. Some categories may be missing.', { variant: 'warning' });
          console.error('Error loading exercise muscle and equipment data:', err);
        }
      } catch (err) {
        enqueueSnackbar('Failed to load exercise analytics data. Please try again.', { variant: 'error' });
        console.error('Error loading exercise analytics data:', err);
      } finally {
        setIsLoading(false);
      }
    };

    loadWorkoutData();
  }, [user.keycloak_id, enqueueSnackbar]);

  // Calculate exercise statistics
  const exerciseStats = useMemo(() => {
    if (!workouts.length) return [];

    const exerciseMap = new Map<string, ExerciseData>();

    workouts.forEach((workoutData) => {
      workoutData.stages.forEach((stage) => {
        stage.exercises.forEach((exerciseData) => {
          const exerciseName = exerciseData.exercise.exercise_name;
          const existing = exerciseMap.get(exerciseName) || {
            name: exerciseName,
            totalVolume: 0,
            frequency: 0,
            maxWeight: 0,
            avgWeight: 0,
            totalSets: 0,
            muscleGroup: exerciseMuscleData.get(exerciseName)?.join(', ') || 'Unknown',
            equipment: exerciseEquipmentData.get(exerciseName)?.join(', ') || 'Unknown',
          };

          // Calculate stats
          let totalWeight = 0;
          let weightCount = 0;
          exerciseData.set_schemes.forEach((setScheme) => {
            const weight = setScheme.performed_weight || setScheme.target_weight || 0;
            const reps = setScheme.performed_rep_count || setScheme.target_rep_count || 0;
            const bandWeight = setScheme.band_weight_lbs ? 
              (setScheme.band_weight_lbs as any)?.weight_lbs || 0 : 0;
            
            const totalSetWeight = weight + bandWeight;
            existing.totalVolume += totalSetWeight * reps;
            existing.maxWeight = Math.max(existing.maxWeight, totalSetWeight);
            totalWeight += totalSetWeight;
            weightCount += 1;
            existing.totalSets += 1;
          });

          existing.avgWeight = weightCount > 0 ? totalWeight / weightCount : 0;
          existing.frequency += 1;
          exerciseMap.set(exerciseName, existing);
        });
      });
    });

    return Array.from(exerciseMap.values())
      .sort((a, b) => b.totalVolume - a.totalVolume)
      .slice(0, 20); // Top 20 exercises
  }, [workouts, exerciseMuscleData, exerciseEquipmentData]);

  // Prepare chart data
  const radialBarData = useMemo(() => {
    return exerciseStats.slice(0, 8).map(exercise => ({
      id: exercise.name,
      data: [
        {
          x: 'Volume',
          y: Math.round(exercise.totalVolume / 1000), // Convert to thousands
        },
        {
          x: 'Frequency',
          y: exercise.frequency,
        },
        {
          x: 'Max Weight',
          y: Math.round(exercise.maxWeight),
        },
      ],
    }));
  }, [exerciseStats]);

  const sunburstData = useMemo(() => {
    const muscleGroups = new Map<string, { volume: number; exercises: string[] }>();
    
    exerciseStats.forEach(exercise => {
      const existing = muscleGroups.get(exercise.muscleGroup) || {
        volume: 0,
        exercises: [],
      };
      existing.volume += exercise.totalVolume;
      if (!existing.exercises.includes(exercise.name)) {
        existing.exercises.push(exercise.name);
      }
      muscleGroups.set(exercise.muscleGroup, existing);
    });

    return {
      name: 'Exercise Volume',
      children: Array.from(muscleGroups.entries()).map(([group, data]) => ({
        name: group,
        loc: data.volume,
        children: data.exercises.slice(0, 5).map(exercise => ({
          name: exercise,
          loc: data.volume / data.exercises.length,
        })),
      })),
    };
  }, [exerciseStats]);

  const icicleData = useMemo(() => {
    if (!exerciseStats.length) return null;

    // Group by actual muscle groups from the exercise data
    const muscleGroups = new Map<string, { volume: number; exercises: Map<string, number> }>();
    
    exerciseStats.forEach(exercise => {
      // Use the actual muscle group from the exercise data
      const group = exercise.muscleGroup;
      const existing = muscleGroups.get(group) || { volume: 0, exercises: new Map() };
      existing.volume += exercise.totalVolume;
      existing.exercises.set(exercise.name, exercise.totalVolume);
      muscleGroups.set(group, existing);
    });

    return {
      id: 'Exercise Volume Analysis',
      children: Array.from(muscleGroups.entries()).map(([group, data]) => ({
        id: group,
        children: Array.from(data.exercises.entries()).slice(0, 8).map(([exercise, volume]) => ({
          id: exercise,
          value: volume,
        })),
      })),
    };
  }, [exerciseStats]);

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
            Exercise Analytics
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Complete your first workout to see exercise analytics and insights.
          </Typography>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card sx={{ mb: 4 }}>
      <CardContent>
        <Typography variant="h6" gutterBottom>
          Exercise Analytics
        </Typography>
        <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
          Advanced visualizations of your exercise performance and training patterns
        </Typography>

        <Grid container spacing={3}>
          {/* Radial Bar Chart - Exercise Performance */}
          <Grid item xs={12} lg={6}>
            <Card variant="outlined">
              <CardContent>
                <Box display="flex" alignItems="center" gap={1} sx={{ mb: 2 }}>
                  <FitnessCenterIcon color="primary" />
                  <Typography variant="h6">Exercise Performance Metrics</Typography>
                </Box>
                <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                  Volume, frequency, and max weight for top exercises
                </Typography>
                <Box sx={{ height: 300 }}>
                  <ResponsiveRadialBar
                    data={radialBarData}
                    valueFormat=">-0"
                    padding={0.4}
                    cornerRadius={2}
                    margin={{ top: 40, right: 120, bottom: 40, left: 40 }}
                    radialAxisStart={{ tickSize: 5, tickPadding: 5, tickRotation: 0 }}
                    circularAxisOuter={{ tickSize: 2, tickPadding: 2, tickRotation: 0 }}
                    labelsSkipAngle={10}
                    colors={{ scheme: 'nivo' }}
                    theme={congenNivoTheme}
                    enableLabels={true}
                    labelsRadiusOffset={0.5}
                    legends={[
                      {
                        anchor: 'top-left',
                        direction: 'column',
                        justify: false,
                        translateX: -40,
                        translateY: 0,
                        itemsSpacing: 2,
                        itemDirection: 'left-to-right',
                        itemWidth: 80,
                        itemHeight: 20,
                        itemTextColor: '#999',
                        symbolSize: 12,
                        symbolShape: 'circle',
                        onClick: (data) => {
                          console.log('Legend clicked:', data);
                        },
                      },
                    ]}
                  />
                </Box>
              </CardContent>
            </Card>
          </Grid>

          {/* Sunburst Chart - Exercise Hierarchy */}
          <Grid item xs={12} lg={6}>
            <Card variant="outlined">
              <CardContent>
                <Box display="flex" alignItems="center" gap={1} sx={{ mb: 2 }}>
                  <ShowChartIcon color="secondary" />
                  <Typography variant="h6">Exercise Volume Hierarchy</Typography>
                </Box>
                <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                  Volume distribution by muscle groups and exercises
                </Typography>
                <Box sx={{ height: 300 }}>
                  <ResponsiveSunburst
                    data={sunburstData}
                    margin={{ top: 10, right: 10, bottom: 10, left: 10 }}
                    id="name"
                    value="loc"
                    cornerRadius={2}
                    borderColor={{ theme: 'background' }}
                    colors={{ scheme: 'nivo' }}
                    childColor={{
                      from: 'color',
                      modifiers: [['brighter', 0.1]],
                    }}
                    enableArcLabels={true}
                    arcLabelsSkipAngle={10}
                    arcLabelsTextColor={{
                      from: 'color',
                      modifiers: [['darker', 1.4]],
                    }}
                    theme={congenNivoTheme}
                  />
                </Box>
              </CardContent>
            </Card>
          </Grid>

          {/* Icicle Chart - Training Structure */}
          <Grid item xs={12} lg={6}>
            <Card variant="outlined">
              <CardContent>
                <Box display="flex" alignItems="center" gap={1} sx={{ mb: 2 }}>
                  <TrendingUpIcon color="info" />
                  <Typography variant="h6">Training Structure Analysis</Typography>
                </Box>
                <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                  Volume breakdown by exercise categories
                </Typography>
                <Box sx={{ height: 300 }}>
                  <ResponsiveIcicle
                    data={icicleData}
                    margin={{ top: 10, right: 10, bottom: 10, left: 10 }}
                    value="value"
                    colors={{ scheme: 'nivo' }}
                    theme={congenNivoTheme}
                    enableLabels={true}
                    labelTextColor={{
                      from: 'color',
                      modifiers: [['darker', 2]],
                    }}
                    labelBoxAnchor="top"
                    labelPaddingX={6}
                    labelPaddingY={6}
                    labelAlign="end"
                    labelBaseline="center"
                    labelRotation={270}
                    labelSkipWidth={12}
                    labelSkipHeight={32}
                    borderWidth={1}
                    borderColor={{
                      from: 'color',
                      modifiers: [['darker', 0.2]],
                    }}
                    borderRadius={4}
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
