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
  useTheme,
} from '@mui/material';
import { ResponsiveBump } from '@nivo/bump';
import { ResponsiveChord } from '@nivo/chord';
import { ResponsiveIcicle } from '@nivo/icicle';
import { ResponsiveStream } from '@nivo/stream';
import { useSnackbar } from 'notistack';
import React, { useEffect, useState, useMemo } from 'react';

import { getUserDataExport } from '../api/gdpr';
import { getIndividualExercise } from '../api/exercise';
import { getUserWeightUnitPreferences, WeightUnit } from '../api/userWeightUnitPreference';
import type { 
  User, 
  ProgrammedWorkout, 
  WorkoutStage, 
  ProgrammedExercise, 
  SetScheme,
  Exercise,
  Program
} from '../api/types';
import type { UserWeightUnitPreference } from '../api/userWeightUnitPreference';
import { createCongenNivoTheme, congenLegendConfig } from '../theme/nivoTheme';
import { categorizeExerciseVolume } from '../common/utils';

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

interface ExerciseCorrelationData {
  source: string;
  target: string;
  value: number;
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
  const theme = useTheme();
  const nivoTheme = createCongenNivoTheme(theme.palette.mode);
  const [workouts, setWorkouts] = useState<WorkoutData[]>([]);
  const [programs, setPrograms] = useState<Program[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [exerciseData, setExerciseData] = useState<Map<string, Exercise>>(new Map());
  const [weightUnitPreferences, setWeightUnitPreferences] = useState<UserWeightUnitPreference[]>([]);

  // Helper function to convert weight to user's preferred unit
  const convertWeightToUserUnit = (weight: number, exerciseName: string): number => {
    // Find user's preferred unit for this exercise
    const preference = weightUnitPreferences.find(pref => pref.exercise_name === exerciseName);
    const userUnit = preference?.preferred_unit || WeightUnit.LBS; // Default to LBS
    
    // If weight is already in user's preferred unit, return as is
    // For now, assume all weights in database are in LBS (this should be improved in backend)
    if (userUnit === WeightUnit.LBS) {
      return weight;
    } else if (userUnit === WeightUnit.KG) {
      // Convert from LBS to KG
      return weight * 0.453592;
    }
    
    return weight;
  };

  // Helper function to get display unit for an exercise
  const getDisplayUnit = (exerciseName: string): string => {
    const preference = weightUnitPreferences.find(pref => pref.exercise_name === exerciseName);
    const userUnit = preference?.preferred_unit || WeightUnit.LBS;
    return userUnit === WeightUnit.KG ? 'kg' : 'lbs';
  };

  // Load all workout data using optimized GDPR export endpoint
  useEffect(() => {
    const loadWorkoutData = async () => {
      try {
        setIsLoading(true);

        // Load weight unit preferences first
        try {
          const unitResponse = await getUserWeightUnitPreferences(user.keycloak_id);
          setWeightUnitPreferences(unitResponse.data);
        } catch (err) {
          console.warn('No weight unit preferences found, using defaults');
          setWeightUnitPreferences([]);
        }

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

        // Fetch exercise data for all unique exercises
        const uniqueExercises = new Set<string>();
        workoutsData.forEach((workoutData) => {
          workoutData.stages.forEach((stage) => {
            stage.exercises.forEach((exerciseData) => {
              uniqueExercises.add(exerciseData.exercise.exercise_name);
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
        stage.exercises.forEach((exerciseWithSchemes) => {
          exerciseWithSchemes.set_schemes.forEach((setScheme) => {
            const weight = setScheme.performed_weight || setScheme.target_weight || 0;
            const reps = setScheme.performed_rep_count || setScheme.target_rep_count || 0;
            const bandWeight = setScheme.band_weight_lbs ? 
              (setScheme.band_weight_lbs as any)?.weight_lbs || 0 : 0;
            
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
  }, [workouts, exerciseData]);

  // Calculate exercise ranking data for bump chart
  const exerciseRankingData = useMemo(() => {
    if (!workouts.length) return [];

    // Calculate volume for each exercise per workout
    const workoutExerciseVolumes: Array<Map<string, number>> = [];
    
    workouts.forEach((workoutData) => {
      const workoutVolumes = new Map<string, number>();
      
      workoutData.stages.forEach((stage) => {
        stage.exercises.forEach((exerciseWithSchemes) => {
          const exerciseName = exerciseWithSchemes.exercise.exercise_name;
          const existing = workoutVolumes.get(exerciseName) || 0;

          // Calculate volume for this exercise in this workout with weight unit conversion
          let exerciseVolume = 0;
          exerciseWithSchemes.set_schemes.forEach((setScheme) => {
            const rawWeight = setScheme.performed_weight || setScheme.target_weight || 0;
            const reps = setScheme.performed_rep_count || setScheme.target_rep_count || 0;
            const rawBandWeight = setScheme.band_weight_lbs ? 
              (setScheme.band_weight_lbs as any)?.weight_lbs || 0 : 0;
            
            // Convert weights to user's preferred unit
            const convertedWeight = convertWeightToUserUnit(rawWeight, exerciseName);
            const convertedBandWeight = convertWeightToUserUnit(rawBandWeight, exerciseName);
            
            exerciseVolume += (convertedWeight + convertedBandWeight) * reps;
          });

          workoutVolumes.set(exerciseName, existing + exerciseVolume);
        });
      });
      
      workoutExerciseVolumes.push(workoutVolumes);
    });

    // Get all unique exercises that appear in any workout
    const allExercises = new Set<string>();
    workoutExerciseVolumes.forEach(workoutVolumes => {
      workoutVolumes.forEach((volume, exerciseName) => {
        if (volume > 0) {
          allExercises.add(exerciseName);
        }
      });
    });

    // Calculate total volume for each exercise across all workouts to determine top exercises
    const totalExerciseVolumes = new Map<string, number>();
    allExercises.forEach(exerciseName => {
      let totalVolume = 0;
      workoutExerciseVolumes.forEach(workoutVolumes => {
        totalVolume += workoutVolumes.get(exerciseName) || 0;
      });
      totalExerciseVolumes.set(exerciseName, totalVolume);
    });

    // Get top 8 exercises by total volume
    const topExercises = Array.from(totalExerciseVolumes.entries())
      .sort((a, b) => b[1] - a[1])
      .slice(0, 8)
      .map(([name]) => name);

    // Create ranking data showing volume per workout for top exercises
    return topExercises.map(exerciseName => ({
      id: exerciseName,
      data: workoutExerciseVolumes.map((workoutVolumes, index) => ({
        x: `Workout ${index + 1}`,
        y: workoutVolumes.get(exerciseName) || 0,
      })),
    }));
  }, [workouts, weightUnitPreferences]);

  // Calculate training structure data for icicle chart
  const trainingStructureData = useMemo(() => {
    if (!workouts.length || !programs.length || !exerciseData.size) return null;

    const activeProgram = programs.find(program => program.is_active);
    if (!activeProgram) return null;

    const programWorkouts = workouts.filter(workout => 
      workout.workout.program_id === activeProgram.id
    );

    // Group by exercise categories based on real exercise data
    const categoryVolumes = new Map<string, number>();
    const categoryExercises = new Map<string, Map<string, number>>();

    programWorkouts.forEach(workout => {
      workout.stages.forEach(stage => {
        stage.exercises.forEach(exercise => {
          // Get exercise data to properly categorize using the is_accessory field
          const exerciseName = exercise.exercise.exercise_name;
          const exerciseInfo = exerciseData.get(exerciseName);
          let category = 'Other';
          
          if (exerciseInfo) {
            if (exerciseInfo.is_accessory) {
              category = 'Accessory Work';
            } else {
              // Use the actual movement type as the category
              category = exerciseInfo.movement_type
                .split('_')
                .map(word => word.charAt(0).toUpperCase() + word.slice(1))
                .join(' ');
            }
          }

          // Calculate volume for this exercise with weight unit conversion
          let exerciseVolume = 0;
          exercise.set_schemes.forEach(setScheme => {
            const rawWeight = setScheme.performed_weight || setScheme.target_weight || 0;
            const reps = setScheme.performed_rep_count || setScheme.target_rep_count || 0;
            const rawBandWeight = setScheme.band_weight_lbs ? 
              (setScheme.band_weight_lbs as any)?.weight_lbs || 0 : 0;
            
            // Convert weights to user's preferred unit
            const convertedWeight = convertWeightToUserUnit(rawWeight, exerciseName);
            const convertedBandWeight = convertWeightToUserUnit(rawBandWeight, exerciseName);
            
            exerciseVolume += (convertedWeight + convertedBandWeight) * reps;
          });

          // Add to category totals
          categoryVolumes.set(category, (categoryVolumes.get(category) || 0) + exerciseVolume);
          
          // Track exercises and their individual volumes in each category
          if (!categoryExercises.has(category)) {
            categoryExercises.set(category, new Map());
          }
          const exerciseMap = categoryExercises.get(category)!;
          exerciseMap.set(exerciseName, (exerciseMap.get(exerciseName) || 0) + exerciseVolume);
        });
      });
    });

    return {
      id: activeProgram.name,
      children: Array.from(categoryVolumes.entries()).map(([category, totalVolume]) => ({
        id: category,
        children: Array.from(categoryExercises.get(category)?.entries() || []).map(([exerciseName, volume]) => ({
          id: exerciseName,
          value: volume,
        })),
      })),
    };
  }, [workouts, programs, exerciseData, weightUnitPreferences]);

  // Calculate exercise correlations for chord diagram
  const exerciseCorrelations = useMemo(() => {
    if (!workouts.length) return [];

    const correlations: ExerciseCorrelationData[] = [];
    const exercisePairs = new Map<string, number>();

    workouts.forEach((workoutData) => {
      const workoutExercises = new Set<string>();
      
      workoutData.stages.forEach((stage) => {
        stage.exercises.forEach((exerciseWithSchemes) => {
          workoutExercises.add(exerciseWithSchemes.exercise.exercise_name);
        });
      });

      // Count exercise pairs in the same workout
      const exerciseArray = Array.from(workoutExercises);
      for (let i = 0; i < exerciseArray.length; i++) {
        for (let j = i + 1; j < exerciseArray.length; j++) {
          const pair = [exerciseArray[i], exerciseArray[j]].sort().join('|');
          exercisePairs.set(pair, (exercisePairs.get(pair) || 0) + 1);
        }
      }
    });

    // Convert to chord diagram format
    exercisePairs.forEach((value, pair) => {
      const [source, target] = pair.split('|');
      correlations.push({ source, target, value });
    });

    return correlations
      .sort((a, b) => b.value - a.value)
      .slice(0, 10); // Top 10 correlations
  }, [workouts]);

  const chordData = useMemo(() => {
    const uniqueExercises = new Set<string>();
    exerciseCorrelations.forEach(corr => {
      uniqueExercises.add(corr.source);
      uniqueExercises.add(corr.target);
    });

    return {
      matrix: Array.from(uniqueExercises).map(source => 
        Array.from(uniqueExercises).map(target => {
          const correlation = exerciseCorrelations.find(
            corr => (corr.source === source && corr.target === target) ||
                   (corr.source === target && corr.target === source)
          );
          return correlation?.value || 0;
        })
      ),
      keys: Array.from(uniqueExercises),
    };
  }, [exerciseCorrelations]);

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
          {/* Chord Diagram - Exercise Correlations */}
          {chordData.keys.length > 0 && (
            <Grid item xs={12} lg={6}>
              <Card variant="outlined">
                <CardContent>
                  <Box display="flex" alignItems="center" gap={1} sx={{ mb: 2 }}>
                    <TrendingUpIcon color="info" />
                    <Typography variant="h6">Exercise Correlations</Typography>
                  </Box>
                  <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                    Exercise pairing patterns in your workouts
                  </Typography>
                  <Box sx={{ height: 400 }}>
                    <ResponsiveChord
                      data={chordData.matrix}
                      keys={chordData.keys}
                      margin={{ top: 60, right: 60, bottom: 90, left: 60 }}
                      valueFormat=".0f"
                      padAngle={0.02}
                      innerRadiusRatio={0.96}
                      innerRadiusOffset={0.02}
                      inactiveArcOpacity={0.25}
                      arcBorderWidth={1}
                      arcBorderColor={{ from: 'color', modifiers: [['darker', 0.4]] }}
                      activeRibbonOpacity={0.75}
                      inactiveRibbonOpacity={0.25}
                      ribbonBorderWidth={1}
                      ribbonBorderColor={{ from: 'color', modifiers: [['darker', 0.4]] }}
                      enableLabel={true}
                      label="id"
                      labelOffset={12}
                      labelRotation={-90}
                      labelTextColor={{
                        from: 'color',
                        modifiers: [['darker', 1]],
                      }}
                      colors={{ scheme: 'nivo' }}
                      theme={nivoTheme}
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
                    theme={nivoTheme}
                  />
                </Box>
              </CardContent>
            </Card>
          </Grid>

          {/* Training Structure Hierarchy */}
          {trainingStructureData && (
            <Grid item xs={12}>
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
                      value="value"
                      colors={{ scheme: 'nivo' }}
                      theme={nivoTheme}
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
                      labelSkipWidth={16}
                      labelSkipHeight={48}
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
          )}
        </Grid>
      </CardContent>
    </Card>
  );
};
