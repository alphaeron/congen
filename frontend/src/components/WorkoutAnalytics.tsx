import { Card, CardContent, Grid, Typography } from '@mui/material';
import { useSnackbar } from 'notistack';
import React, { useEffect, useState, useMemo } from 'react';

import { ChordChart } from './ChordChart';
import { StreamChart } from './StreamChart';
import { LoadingSpinner } from './LoadingSpinner';
import { getIndividualExercise } from '../api/exercise';
import { getUserDataExport } from '../api/gdpr';
import type {
  User,
  ProgrammedWorkout,
  WorkoutStage,
  ProgrammedExercise,
  SetScheme,
  Exercise,
  ProgramWithWorkouts,
} from '../api/types';
import { getUserWeightUnitPreferences } from '../api/userWeightUnitPreference';
import type { UserWeightUnitPreference } from '../api/userWeightUnitPreference';
import { categorizeExerciseVolume, convertWeightToPounds } from '../common/utils';
import { replaceUnderscoresWithSpaces, formatDate } from '../common/utils';

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
  const [workouts, setWorkouts] = useState<WorkoutData[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [exerciseData, setExerciseData] = useState<Map<string, Exercise>>(new Map());
  const [weightUnitPreferences, setWeightUnitPreferences] = useState<UserWeightUnitPreference[]>(
    []
  );

  // Load all workout data using optimized GDPR export endpoint
  useEffect(() => {
    const loadWorkoutData = async () => {
      try {
        setIsLoading(true);

        // Load weight unit preferences first
        try {
          const unitResponse = await getUserWeightUnitPreferences(user.keycloak_id);
          setWeightUnitPreferences(unitResponse.data);
        } catch {
          enqueueSnackbar('No weight unit preferences found, using defaults', {
            variant: 'warning',
          });
          setWeightUnitPreferences([]);
        }

        // Use optimized single query to get all workout data
        const userDataExport = await getUserDataExport();

        // Extract programs and workouts from the export data
        // Handle case where user has no training programs (empty array)
        const workoutsData =
          userDataExport.training_programs?.flatMap((program: ProgramWithWorkouts) =>
            program.workouts.map(workoutWithStages => ({
              workout: workoutWithStages.workout,
              stages: workoutWithStages.stages.map(stageWithExercises => ({
                stage: stageWithExercises.stage,
                exercises: stageWithExercises.exercises.map(exerciseWithSetSchemes => ({
                  exercise: exerciseWithSetSchemes.exercise,
                  set_schemes: exerciseWithSetSchemes.set_schemes,
                })),
              })),
            }))
          ) || [];

        setWorkouts(workoutsData);

        // Fetch exercise data for all unique exercises
        const uniqueExercises = new Set<string>();
        workoutsData.forEach(workoutData => {
          workoutData.stages.forEach(stage => {
            stage.exercises.forEach(exerciseData => {
              uniqueExercises.add(exerciseData.exercise.exercise_name);
            });
          });
        });

        const exerciseMap = new Map<string, Exercise>();
        for (const exerciseName of Array.from(uniqueExercises)) {
          try {
            const exercise = await getIndividualExercise(exerciseName);
            exerciseMap.set(exerciseName, exercise);
          } catch {
            enqueueSnackbar(`Error fetching exercise data for ${exerciseName}`, {
              variant: 'error',
            });
          }
        }

        setExerciseData(exerciseMap);
      } catch {
        enqueueSnackbar('Failed to load workout analytics data. Please try again.', {
          variant: 'error',
        });
      } finally {
        setIsLoading(false);
      }
    };

    loadWorkoutData();
  }, [user.keycloak_id]);

  // Calculate workout volume data for stream chart
  const volumeData = useMemo(() => {
    if (!workouts.length) return [];

    return workouts
      .map(workoutData => {
        let maxEffortVolume = 0;
        let dynamicEffortVolume = 0;
        let accessoryVolume = 0;

        workoutData.stages.forEach(stage => {
          stage.exercises.forEach(exerciseWithSchemes => {
            exerciseWithSchemes.set_schemes.forEach(setScheme => {
              const weight = setScheme.performed_weight || setScheme.target_weight || 0;
              const reps = setScheme.performed_rep_count || setScheme.target_rep_count || 0;
              const bandWeight = setScheme.band_weight_lbs
                ? (setScheme.band_weight_lbs as { weight_lbs?: number })?.weight_lbs || 0
                : 0;

              // Get user's preferred weight unit for this exercise
              const exerciseName = exerciseWithSchemes.exercise.exercise_name;
              const weightUnitPreference = weightUnitPreferences.find(
                pref => pref.exercise_name === exerciseName
              );

              // Convert weight to pounds for consistent calculations
              const convertedWeight = convertWeightToPounds(
                weight,
                weightUnitPreference?.preferred_unit
              );
              const totalWeight = convertedWeight + bandWeight; // bandWeight is already in lbs
              const setVolume = totalWeight * reps;

              // Get exercise data and categorize volume using shared helper
              const exerciseInfo = exerciseData.get(exerciseName);
              const categorizedVolume = categorizeExerciseVolume(
                exerciseInfo,
                replaceUnderscoresWithSpaces(workoutData.workout.name),
                setVolume
              );

              maxEffortVolume += categorizedVolume.maxEffortVolume;
              dynamicEffortVolume += categorizedVolume.dynamicEffortVolume;
              accessoryVolume += categorizedVolume.accessoryVolume;
            });
          });
        });

        return {
          date: formatDate(workoutData.workout.created_at),
          totalVolume: Math.round(maxEffortVolume + dynamicEffortVolume + accessoryVolume),
          maxEffortVolume: Math.round(maxEffortVolume),
          dynamicEffortVolume: Math.round(dynamicEffortVolume),
          accessoryVolume: Math.round(accessoryVolume),
        };
      })
      .slice(-10); // Last 10 workouts
  }, [workouts, exerciseData, weightUnitPreferences]);

  // Calculate exercise correlations for chord diagram
  const exerciseCorrelations = useMemo(() => {
    if (!workouts.length) return [];

    const correlations: ExerciseCorrelationData[] = [];
    const exercisePairs = new Map<string, number>();

    workouts.forEach(workoutData => {
      const workoutExercises = new Set<string>();

      workoutData.stages.forEach(stage => {
        stage.exercises.forEach(exerciseWithSchemes => {
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

    return correlations.sort((a, b) => b.value - a.value).slice(0, 10); // Top 10 correlations
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
            corr =>
              (corr.source === source && corr.target === target) ||
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
      Accessory: volume.accessoryVolume,
    }));
  }, [volumeData]);

  if (isLoading) {
    return (
      <Card sx={{ mb: 4 }}>
        <CardContent>
          <LoadingSpinner message="Loading workout analytics..." fullHeight={false} />
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
            <Grid size={{ xs: 12, lg: 6 }}>
              <ChordChart
                matrix={chordData.matrix}
                keys={chordData.keys}
                title="Exercise Correlations"
                description="Exercise pairing patterns in your workouts"
                height={400}
              />
            </Grid>
          )}

          {/* Stream Chart - Volume Flow */}
          <Grid size={{ xs: 12, lg: 6 }}>
            <StreamChart
              data={streamData}
              keys={['Max Effort', 'Dynamic Effort', 'Accessory']}
              title="Volume Flow Over Time"
              description="Training volume distribution across workout types"
              height={400}
            />
          </Grid>
        </Grid>
      </CardContent>
    </Card>
  );
};
