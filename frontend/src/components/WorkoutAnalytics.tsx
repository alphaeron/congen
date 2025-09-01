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
  Exercise,
} from '../api/types';
import { getUserWeightUnitPreferences } from '../api/userWeightUnitPreference';
import type { UserWeightUnitPreference } from '../api/userWeightUnitPreference';

interface WorkoutAnalyticsProps {
  user: User;
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
  const [isLoading, setIsLoading] = useState(true);
  const [exerciseData, setExerciseData] = useState<Map<string, Exercise>>(new Map());
  const [weightUnitPreferences, setWeightUnitPreferences] = useState<UserWeightUnitPreference[]>(
    []
  );
  const [userDataExport, setUserDataExport] = useState<any>(null);

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
        const dataExport = await getUserDataExport();
        setUserDataExport(dataExport);

        // Extract unique exercises from the export data
        const uniqueExercises = new Set<string>();
        dataExport.training_programs?.forEach((program: any) => {
          program.workouts.forEach((workoutWithStages: any) => {
            workoutWithStages.stages.forEach((stageWithExercises: any) => {
              stageWithExercises.exercises.forEach((exerciseWithSetSchemes: any) => {
                uniqueExercises.add(exerciseWithSetSchemes.exercise.exercise_name);
              });
            });
          });
        });

        // Fetch exercise data for all unique exercises
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

  if (isLoading) {
    return (
      <Card sx={{ mb: 4 }}>
        <CardContent>
          <LoadingSpinner message="Loading workout analytics..." fullHeight={false} />
        </CardContent>
      </Card>
    );
  }

  if (!userDataExport?.training_programs?.length) {
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
          <Grid size={{ xs: 12, lg: 6 }}>
            <ChordChart
              userDataExport={userDataExport}
              title="Exercise Correlations"
              description="Exercise pairing patterns in your workouts"
              height={400}
            />
          </Grid>

          {/* Stream Chart - Volume Flow */}
          <Grid size={{ xs: 12, lg: 6 }}>
            <StreamChart
              userDataExport={userDataExport}
              exerciseData={exerciseData}
              weightUnitPreferences={weightUnitPreferences}
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
