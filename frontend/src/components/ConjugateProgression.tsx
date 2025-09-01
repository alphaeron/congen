import { Box, Card, CardContent, Grid, Typography } from '@mui/material';
import { useSnackbar } from 'notistack';
import React, { useEffect, useState } from 'react';

import { LineChart } from './LineChart';
import { PieChart } from './PieChart';
import { LoadingSpinner } from './LoadingSpinner';
import { getIndividualExercise } from '../api/exercise';
import { getUserDataExport } from '../api/gdpr';
import type {
  User,
  Exercise,
} from '../api/types';

interface ConjugateProgressionProps {
  user: User;
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
  const [userData, setUserData] = useState<any>(null);
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
        dataExport.training_programs?.forEach((program: any) => {
          program.workouts.forEach((workout: any) => {
            workout.stages.forEach((stage: any) => {
              stage.exercises.forEach((exercise: any) => {
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
          } catch {
            enqueueSnackbar(`Error fetching exercise data for ${exerciseName}`, {
              variant: 'error',
            });
          }
        }

        setExerciseData(exerciseMap);
      } catch {
        enqueueSnackbar('Failed to load workout data. Please try again.', { variant: 'error' });
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
          <LoadingSpinner message="Loading conjugate progression..." fullHeight={false} />
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
    <React.Fragment>
      <Grid container spacing={3}>
        {/* Volume Tracking Chart */}
        <Grid size={{ xs: 12, lg: 8 }}>
          <LineChart
            userDataExport={userData}
            exerciseData={exerciseData}
            chartType="volume"
            title="Volume Progression"
            description="Total weight lifted over time (including band resistance)"
            xAxisLabel="Workout Date"
            yAxisLabel="Volume (lbs)"
          />
        </Grid>

        {/* Exercise Category Distribution */}
        <Grid size={{ xs: 12, lg: 4 }}>
          <PieChart
            userDataExport={userData}
            exerciseData={exerciseData}
            title="Exercise Distribution"
            description="Volume by workout stage"
          />
        </Grid>

        {/* Progress Tracking */}
        <Grid size={{ xs: 12 }}>
          <LineChart
            userDataExport={userData}
            exerciseData={exerciseData}
            chartType="progress"
            title="Progress Tracking"
            description="1RM improvements and volume progression over time"
            xAxisLabel="Date"
            yAxisLabel="Weight (lbs)"
          />
        </Grid>
      </Grid>
    </React.Fragment>
  );
};
