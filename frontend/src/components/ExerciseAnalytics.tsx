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
import { ResponsiveTreeMap } from '@nivo/treemap';
import { useSnackbar } from 'notistack';
import React, { useEffect, useState, useMemo } from 'react';

import { getUserDataExport } from '../api/gdpr';
import type { 
  User, 
  ProgrammedWorkout, 
  WorkoutStage, 
  ProgrammedExercise, 
  SetScheme,
  UserOneRepMax 
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
      } catch (err) {
        enqueueSnackbar('Failed to load exercise analytics data. Please try again.', { variant: 'error' });
        console.error('Error loading exercise analytics data:', err);
      } finally {
        setIsLoading(false);
      }
    };

    loadWorkoutData();
  }, [user.keycloak_id]);

  // Calculate exercise statistics
  const exerciseStats = useMemo(() => {
    if (!workouts.length) return [];

    const exerciseMap = new Map<string, ExerciseData>();
    
    // Define muscle group and equipment mappings
    const muscleGroupMappings: { [key: string]: string[] } = {
      'Chest': ['bench', 'press', 'fly', 'dip'],
      'Back': ['row', 'pull', 'deadlift', 'lat'],
      'Legs': ['squat', 'leg', 'calf', 'lunge'],
      'Shoulders': ['shoulder', 'overhead', 'lateral', 'rear'],
      'Arms': ['curl', 'extension', 'tricep', 'bicep'],
      'Core': ['ab', 'core', 'plank', 'crunch'],
    };

    const equipmentMappings: { [key: string]: string[] } = {
      'Barbell': ['barbell', 'bar', 'squat', 'bench', 'deadlift', 'press'],
      'Dumbbell': ['dumbbell', 'db'],
      'Cable': ['cable', 'pulley'],
      'Machine': ['machine', 'press', 'leg press'],
      'Bodyweight': ['bodyweight', 'push-up', 'pull-up', 'dip'],
      'Band': ['band', 'resistance'],
      'Kettlebell': ['kettlebell', 'kb'],
    };

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
            muscleGroup: 'Other',
            equipment: 'Other',
          };

          // Determine muscle group and equipment
          const lowerName = exerciseName.toLowerCase();
          for (const [group, keywords] of Object.entries(muscleGroupMappings)) {
            if (keywords.some(keyword => lowerName.includes(keyword))) {
              existing.muscleGroup = group;
              break;
            }
          }

          for (const [equip, keywords] of Object.entries(equipmentMappings)) {
            if (keywords.some(keyword => lowerName.includes(keyword))) {
              existing.equipment = equip;
              break;
            }
          }

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
  }, [workouts]);

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

  const treemapData = useMemo(() => {
    return {
      name: 'Exercise Volume',
      children: exerciseStats.slice(0, 15).map(exercise => ({
        name: exercise.name,
        loc: exercise.totalVolume,
        children: [{
          name: `${exercise.frequency} sessions`,
          loc: exercise.totalVolume,
        }],
      })),
    };
  }, [exerciseStats]);

  const icicleData = useMemo(() => {
    return {
      name: 'Training Structure',
      loc: exerciseStats.reduce((sum, ex) => sum + ex.totalVolume, 0),
      children: [
        {
          name: 'Compound Lifts',
          loc: exerciseStats
            .filter(ex => ['squat', 'bench', 'deadlift', 'press'].some(lift => 
              ex.name.toLowerCase().includes(lift)
            ))
            .reduce((sum, ex) => sum + ex.totalVolume, 0),
          children: exerciseStats
            .filter(ex => ['squat', 'bench', 'deadlift', 'press'].some(lift => 
              ex.name.toLowerCase().includes(lift)
            ))
            .slice(0, 5)
            .map(ex => ({
              name: ex.name,
              loc: ex.totalVolume,
            })),
        },
        {
          name: 'Accessory Work',
          loc: exerciseStats
            .filter(ex => !['squat', 'bench', 'deadlift', 'press'].some(lift => 
              ex.name.toLowerCase().includes(lift)
            ))
            .reduce((sum, ex) => sum + ex.totalVolume, 0),
          children: exerciseStats
            .filter(ex => !['squat', 'bench', 'deadlift', 'press'].some(lift => 
              ex.name.toLowerCase().includes(lift)
            ))
            .slice(0, 5)
            .map(ex => ({
              name: ex.name,
              loc: ex.totalVolume,
            })),
        },
      ],
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

          {/* Treemap Chart - Exercise Volume */}
          <Grid item xs={12} lg={6}>
            <Card variant="outlined">
              <CardContent>
                <Box display="flex" alignItems="center" gap={1} sx={{ mb: 2 }}>
                  <BarChartIcon color="success" />
                  <Typography variant="h6">Exercise Volume Distribution</Typography>
                </Box>
                <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                  Total volume and frequency for each exercise
                </Typography>
                <Box sx={{ height: 300 }}>
                  <ResponsiveTreeMap
                    data={treemapData}
                    identity="name"
                    value="loc"
                    valueFormat=".0f"
                    margin={{ top: 10, right: 10, bottom: 10, left: 10 }}
                    labelSkipSize={12}
                    labelTextColor={{
                      from: 'color',
                      modifiers: [['darker', 1.2]],
                    }}
                    parentLabelPosition="left"
                    parentLabelTextColor={{
                      from: 'color',
                      modifiers: [['darker', 2]],
                    }}
                    borderColor={{
                      from: 'color',
                      modifiers: [['darker', 0.1]],
                    }}
                    colors={{ scheme: 'nivo' }}
                    theme={congenNivoTheme}
                    enableParentLabel={true}
                    parentLabelSize={16}
                    parentLabelPadding={6}
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
        </Grid>
      </CardContent>
    </Card>
  );
};
