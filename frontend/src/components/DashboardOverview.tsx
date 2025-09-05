import { Box, Card, CardContent, Grid, Typography } from '@mui/material';
import { useSnackbar } from 'notistack';
import React, { useEffect, useState, useMemo } from 'react';
import { useNavigate } from 'react-router';

import { ConjugateProgression } from './ConjugateProgression';
import { LoadingSpinner } from './LoadingSpinner';
import { ActionCard } from './ActionCard';
import { StatusChip } from './StatusChip';
import { getIndividualExercise } from '../api/exercise';
import { getUserDataExport } from '../api/gdpr';
import { getPrograms } from '../api/program';
import { getProgrammedWorkouts } from '../api/programmedWorkout';
import type {
  User,
  Program,
  UserOneRepMax,
  Exercise,
  ProgramWithWorkouts,
  ProgrammedWorkoutWithStages,
  WorkoutStageWithExercises,
  ProgrammedExerciseWithSetSchemes,
  SetScheme,
  UserDataExport,
  UserWeightUnitPreference,
} from '../api/types';
import { getUserOneRepMaxes } from '../api/userOneRepMax';
import { getUserWeightUnitPreferences } from '../api/userWeightUnitPreference';
import {
  formatDate,
  categorizeExerciseVolume,
  replaceUnderscoresWithSpaces,
} from '../common/utils';

interface DashboardOverviewProps {
  user: User;
}

/**
 * Dashboard overview component displaying user progress and statistics.
 *
 * Shows user progress over time, 1RM graphs, exercise trends,
 * and key statistics like volume, frequency, and PRs.
 *
 * @param user The user data to display
 * @return Dashboard overview component
 */
export const DashboardOverview: React.FC<DashboardOverviewProps> = ({ user }) => {
  const navigate = useNavigate();
  const { enqueueSnackbar } = useSnackbar();
  const [programs, setPrograms] = useState<Program[]>([]);
  const [oneRepMaxes, setOneRepMaxes] = useState<UserOneRepMax[]>([]);
  const [userData, setUserData] = useState<UserDataExport | null>(null);
  const [exerciseData, setExerciseData] = useState<Map<string, Exercise>>(new Map());
  const [isLoading, setIsLoading] = useState(true);
  const [weightUnitPreferences, setWeightUnitPreferences] = useState<UserWeightUnitPreference[]>([]);

  const handleActiveProgramClick = () => {
    if (activeProgram) {
      navigate('/dashboard?section=workouts');
    }
  };

  useEffect(() => {
    const loadDashboardData = async () => {
      try {
        setIsLoading(true);

        // Load all dashboard data in parallel
        const [programsData, , oneRepMaxesData, dataExport, weightUnitData] = await Promise.all([
          getPrograms(),
          getProgrammedWorkouts(),
          getUserOneRepMaxes(user.keycloak_id),
          getUserDataExport(),
          getUserWeightUnitPreferences(user.keycloak_id),
        ]);

        setPrograms(programsData);
        setOneRepMaxes(oneRepMaxesData);
        setUserData(dataExport);
        setWeightUnitPreferences(weightUnitData || []);

        // Fetch exercise data for all unique exercises
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
          } catch {
            enqueueSnackbar(`Error fetching exercise data for ${exerciseName}`, {
              variant: 'error',
            });
          }
        }

        setExerciseData(exerciseMap);
      } catch {
        enqueueSnackbar('Failed to load dashboard data. Please try again.', { variant: 'error' });
      } finally {
        setIsLoading(false);
      }
    };

    loadDashboardData();
  }, [user.keycloak_id]);

  // Calculate volume data for accurate metrics
  const volumeData = useMemo(() => {
    if (!userData?.training_programs?.length) return [];

    const allWorkouts: ProgrammedWorkoutWithStages[] = [];
    userData.training_programs.forEach((program: ProgramWithWorkouts) => {
      allWorkouts.push(...program.workouts);
    });

    return allWorkouts
      .map(workoutData => {
        const totalVolume = 0;
        let maxEffortVolume = 0;
        let dynamicEffortVolume = 0;
        let accessoryVolume = 0;

        workoutData.stages.forEach((stage: WorkoutStageWithExercises) => {
          stage.exercises.forEach((exerciseWithSchemes: ProgrammedExerciseWithSetSchemes) => {
            exerciseWithSchemes.set_schemes.forEach((setScheme: SetScheme) => {
              const weight = setScheme.performed_weight || setScheme.target_weight || 0;
              const reps = setScheme.performed_rep_count || setScheme.target_rep_count || 0;
              const bandWeight = setScheme.band_weight_lbs
                ? (setScheme.band_weight_lbs as { weight_lbs: number })?.weight_lbs || 0
                : 0;

              const totalWeight = weight + bandWeight;
              const setVolume = totalWeight * reps;

              // Get exercise data and categorize volume using shared helper
              const exerciseName = exerciseWithSchemes.exercise.exercise_name;
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
          totalVolume: Math.round(
            totalVolume + maxEffortVolume + dynamicEffortVolume + accessoryVolume
          ),
          maxEffortVolume: Math.round(maxEffortVolume),
          dynamicEffortVolume: Math.round(dynamicEffortVolume),
          accessoryVolume: Math.round(accessoryVolume),
        };
      })
      .slice(-10); // Last 10 workouts
  }, [userData, exerciseData]);

  if (isLoading) {
    return <LoadingSpinner message="Loading dashboard..." fullHeight={false} />;
  }

  const activeProgram = programs.find(program => program.is_active);

  // Calculate actual total workouts across all programs
  const totalWorkouts =
    userData?.training_programs?.reduce(
      (total: number, program: ProgramWithWorkouts) => total + program.workouts.length,
      0
    ) || 0;

  // Calculate current week based on actual workout count (assuming 3-4 workouts per week)
  const currentWeek = activeProgram?.current_week_number ?? 0;

  const recentOneRepMaxes = oneRepMaxes.slice(-5); // Last 5 1RMs

  // Calculate total volume and latest volume
  const totalVolume = volumeData.reduce((sum, d) => sum + d.totalVolume, 0);
  const latestVolume = volumeData[volumeData.length - 1]?.totalVolume || 0;

  return (
    <React.Fragment>
      {/* Key Performance Indicators */}
      <Card sx={{ mb: 4 }}>
        <CardContent>
          <Typography variant="h6" gutterBottom>
            Key Performance Indicators
          </Typography>
          <Grid container spacing={2}>
            <Grid size={{ xs: 12, sm: 6, md: 3 }}>
              <Box textAlign="center">
                <Typography variant="h4" color="primary">
                  {totalWorkouts}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  Total Workouts
                </Typography>
              </Box>
            </Grid>
            <Grid size={{ xs: 12, sm: 6, md: 3 }}>
              <Box textAlign="center">
                <Typography variant="h4" color="secondary">
                  {Math.round(totalVolume / 1000)}k
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  Total Volume (lbs)
                </Typography>
              </Box>
            </Grid>
            <Grid size={{ xs: 12, sm: 6, md: 3 }}>
              <Box textAlign="center">
                <Typography variant="h4" color="success">
                  {oneRepMaxes.length}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  1RM Records
                </Typography>
              </Box>
            </Grid>
            <Grid size={{ xs: 12, sm: 6, md: 3 }}>
              <Box textAlign="center">
                <Typography variant="h4" color="info">
                  {Math.round(latestVolume)}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  Latest Volume (lbs)
                </Typography>
              </Box>
            </Grid>
          </Grid>
        </CardContent>
      </Card>

      {/* Active Program Section */}
      {activeProgram && (
        <ActionCard
          title="Active Program"
          clickable
          onClick={handleActiveProgramClick}
        >
          <Box display="flex" alignItems="center" gap={2} flexWrap="wrap" sx={{ mb: 1 }}>
            <Typography variant="body1" fontWeight="medium">
              {activeProgram.name}
            </Typography>
            <StatusChip label={`Week ${currentWeek}`} status="info" />
            <StatusChip label="Active" status="active" />
          </Box>
          <Typography variant="body2" color="text.secondary">
            Click to view workouts
          </Typography>
        </ActionCard>
      )}

      {/* Conjugate Progression Section */}
      <ConjugateProgression
        user={user}
        userData={userData}
        exerciseData={exerciseData}
        oneRepMaxes={oneRepMaxes}
        weightUnitPreferences={weightUnitPreferences}
      />

      {/* Recent 1RM Section */}
      {recentOneRepMaxes.length > 0 && (
        <Card sx={{ mb: 4 }}>
          <CardContent>
            <Typography variant="h6" gutterBottom>
              Recent 1RM Records
            </Typography>
            <Grid container spacing={2}>
              {recentOneRepMaxes.map((oneRepMax, index) => (
                <Grid size={{ xs: 12, sm: 6, md: 4 }} key={index}>
                  <Box
                    sx={{
                      p: 2,
                      border: 1,
                      borderColor: 'divider',
                      borderRadius: 1,
                    }}
                  >
                    <Typography variant="body1" fontWeight="medium">
                      {oneRepMax.exercise_name}
                    </Typography>
                    <Typography variant="h6" color="primary">
                      {oneRepMax.one_rep_max} {oneRepMax.unit}
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                      Updated: {formatDate(oneRepMax.updated_at)}
                    </Typography>
                  </Box>
                </Grid>
              ))}
            </Grid>
          </CardContent>
        </Card>
      )}
    </React.Fragment>
  );
};
