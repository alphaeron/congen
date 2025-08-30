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
import { getExerciseMuscle } from '../api/exerciseMuscle';
import { getExerciseEquipment } from '../api/exerciseEquipment';
import { getUserWeightUnitPreferences, WeightUnit } from '../api/userWeightUnitPreference';
import type { 
  User, 
  ProgrammedWorkout, 
  WorkoutStage, 
  ProgrammedExercise, 
  SetScheme,
  UserOneRepMax,
  ExerciseMuscle,
  ExerciseEquipment,
  ProgramWithWorkouts
} from '../api/types';
import type { UserWeightUnitPreference } from '../api/userWeightUnitPreference';
import { RadialBarChart } from './RadialBarChart';
import { SunburstChart } from './SunburstChart';
import { IcicleChart } from './IcicleChart';

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
          enqueueSnackbar('Failed to load weight unit preferences. Some categories may be missing.', { variant: 'warning' });
          setWeightUnitPreferences([]);
        }

        // Use optimized single query to get all workout data
        const userDataExport = await getUserDataExport();
        
        // Extract workouts and one-rep maxes from the export data
        // Handle case where user has no training programs (empty array)
        const workoutsData = userDataExport.training_programs?.flatMap((program: ProgramWithWorkouts) => 
          program.workouts.map((workoutWithStages) => ({
            workout: workoutWithStages.workout,
            stages: workoutWithStages.stages.map((stageWithExercises) => ({
              stage: stageWithExercises.stage,
              exercises: stageWithExercises.exercises.map((exerciseWithSetSchemes) => ({
                exercise: exerciseWithSetSchemes.exercise,
                set_schemes: exerciseWithSetSchemes.set_schemes
              }))
            }))
          }))
        ) || [];

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
        }
      } catch (err) {
        enqueueSnackbar('Failed to load exercise analytics data. Please try again.', { variant: 'error' });
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

          // Calculate stats with weight unit conversion
          let totalWeight = 0;
          let weightCount = 0;
          exerciseData.set_schemes.forEach((setScheme) => {
            const rawWeight = setScheme.performed_weight || setScheme.target_weight || 0;
            const reps = setScheme.performed_rep_count || setScheme.target_rep_count || 0;
            const rawBandWeight = setScheme.band_weight_lbs ? 
              (setScheme.band_weight_lbs as any)?.weight_lbs || 0 : 0;
            
            // Convert weights to user's preferred unit
            const convertedWeight = convertWeightToUserUnit(rawWeight, exerciseName);
            const convertedBandWeight = convertWeightToUserUnit(rawBandWeight, exerciseName);
            
            const totalSetWeight = convertedWeight + convertedBandWeight;
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
  }, [workouts, exerciseMuscleData, exerciseEquipmentData, weightUnitPreferences]);

  // Prepare chart data
  const radialBarData = useMemo(() => {
    return exerciseStats.slice(0, 8).map(exercise => {
      const unit = getDisplayUnit(exercise.name);
      return {
        id: exercise.name,
        data: [
          {
            x: `Volume (${unit})`,
            y: Math.round(exercise.totalVolume / 1000), // Convert to thousands
          },
          {
            x: 'Frequency',
            y: exercise.frequency,
          },
          {
            x: `Max Weight (${unit})`,
            y: Math.round(exercise.maxWeight),
          },
        ],
      };
    });
  }, [exerciseStats, weightUnitPreferences]);

  const sunburstData = useMemo(() => {
    const muscleGroups = new Map<string, { volume: number; exercises: string[] }>();
    
    exerciseStats.forEach(exercise => {
      // Split the muscle group string into individual muscles
      const individualMuscles = exercise.muscleGroup.split(', ').filter(muscle => muscle.trim() !== '');
      
      // For each individual muscle, add the exercise volume
      individualMuscles.forEach(muscle => {
        const existing = muscleGroups.get(muscle) || {
          volume: 0,
          exercises: [],
        };
        existing.volume += exercise.totalVolume;
        if (!existing.exercises.includes(exercise.name)) {
          existing.exercises.push(exercise.name);
        }
        muscleGroups.set(muscle, existing);
      });
    });

    return {
      name: 'Exercise Volume',
      loc: 0, // Root node doesn't need a value
      children: Array.from(muscleGroups.entries()).map(([muscle, data]) => ({
        name: muscle,
        loc: data.volume,
        children: data.exercises.slice(0, 5).map(exercise => ({
          name: exercise,
          loc: data.volume / data.exercises.length,
        })),
      })),
    };
  }, [exerciseStats, weightUnitPreferences]);

  const icicleData = useMemo(() => {
    if (!exerciseStats.length) {
      return {
        id: 'Volume',
        children: [],
      };
    }

    // Group by individual muscles, not combined muscle groups
    const muscleGroups = new Map<string, { volume: number; exercises: Map<string, number> }>();
    
    exerciseStats.forEach(exercise => {
      // Split the muscle group string into individual muscles
      const individualMuscles = exercise.muscleGroup.split(', ').filter(muscle => muscle.trim() !== '');
      
      // For each individual muscle, add the exercise volume
      individualMuscles.forEach(muscle => {
        const existing = muscleGroups.get(muscle) || { volume: 0, exercises: new Map() };
        existing.volume += exercise.totalVolume;
        existing.exercises.set(exercise.name, exercise.totalVolume);
        muscleGroups.set(muscle, existing);
      });
    });

    return {
      id: 'Volume',
      children: Array.from(muscleGroups.entries()).map(([muscle, data]) => ({
        id: muscle,
        children: Array.from(data.exercises.entries()).slice(0, 8).map(([exercise, volume]) => ({
          id: exercise,
          value: volume,
        })),
      })),
    };
  }, [exerciseStats, weightUnitPreferences]);

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
            <RadialBarChart data={radialBarData} />
          </Grid>

          {/* Sunburst Chart - Exercise Hierarchy */}
          <Grid item xs={12} lg={6}>
            <SunburstChart data={sunburstData} />
          </Grid>

          {/* Icicle Chart - Training Structure */}
          <Grid item xs={12} lg={6}>
            <IcicleChart data={icicleData} />
          </Grid>
        </Grid>
      </CardContent>
    </Card>
  );
};
