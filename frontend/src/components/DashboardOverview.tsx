import { Box, Card, CardContent, Grid, Typography } from '@mui/material';
import { useSnackbar } from 'notistack';
import React, { useEffect, useState, useMemo } from 'react';
import { useNavigate } from 'react-router';

import { ActionCard } from './ActionCard';
import { LoadingSpinner } from './LoadingSpinner';
import { StatusChip } from './StatusChip';
import { AdventurerStatusCard } from './AdventurerStatusCard';
import { WeeklyTestTracker } from './WeeklyTestTracker';
import type {
  User,
  UserOneRepMax,
  Exercise,
  ProgramWithWorkouts,
  ProgrammedWorkoutWithStages,
  WorkoutStageWithExercises,
  ProgrammedExerciseWithSetSchemes,
  SetScheme,
} from '../api/types';
import {
  formatDate,
  categorizeExerciseVolume,
  replaceUnderscoresWithSpaces,
} from '../common/utils';
import { useData } from '../contexts/DataContext';

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
  const { 
    userData, 
    isLoading: isDataLoading, 
    getExercise, 
    performanceScores, 
    performanceMetrics,
    weeklyTests,
    wilksScore,
    refreshPerformanceData 
  } = useData();

  const [oneRepMaxes, setOneRepMaxes] = useState<UserOneRepMax[]>([]);
  const [exerciseData, setExerciseData] = useState<Map<string, Exercise>>(new Map());
  const [isLoading, setIsLoading] = useState(true);

  const handleActiveProgramClick = () => {
    if (activeProgram) {
      navigate('/dashboard?section=workouts');
    }
  };

  // Load additional data that's not in DataContext
  useEffect(() => {
    const loadAdditionalData = async () => {
      if (!userData) return;

      setIsLoading(true);
      try {
        // Extract one rep maxes from userData
        setOneRepMaxes((userData.user_one_rep_max as unknown as UserOneRepMax[]) || []);

        // Fetch exercise data for all unique exercises using DataContext
        const uniqueExercises = new Set<string>();
        userData.training_programs?.forEach((program: ProgramWithWorkouts) => {
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
            const exercise = await getExercise(exerciseName);
            if (exercise) {
              exerciseMap.set(exerciseName, exercise);
            }
          } catch {
            enqueueSnackbar(`Error fetching exercise data for ${exerciseName}`, {
              variant: 'error',
            });
          }
        }

        setExerciseData(exerciseMap);
      } catch {
        enqueueSnackbar('Failed to load additional dashboard data. Please try again.', {
          variant: 'error',
        });
      } finally {
        setIsLoading(false);
      }
    };

    loadAdditionalData();
  }, [userData, enqueueSnackbar, getExercise]);

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

  // Find active program from userData
  const activeProgram = useMemo(() => {
    if (!userData?.training_programs) return null;
    const activeProgramData = userData.training_programs.find(p => p.program.is_active);
    return activeProgramData?.program || null;
  }, [userData]);

  if (isDataLoading || isLoading) {
    return <LoadingSpinner message="Loading dashboard..." fullHeight={false} />;
  }

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
      {/* Performance Tracking Section */}
      {performanceScores ? (
        <Box sx={{ mb: 3 }}>
          <AdventurerStatusCard
            scores={performanceScores}
            metrics={performanceMetrics}
            weeklyTest={weeklyTests?.[0] || null}
            wilksScore={wilksScore}
            userName={user.name}
          />
        </Box>) : (
          <React.Fragment />
        )}

      {/* Weekly Test Tracker */}
      {performanceScores && (
        <Box sx={{ mb: 3 }}>
          <WeeklyTestTracker 
            weeklyTests={weeklyTests} 
            onTestUpdate={refreshPerformanceData}
          />
        </Box>
      )}

      {/* Key Performance Indicators */}
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Box display="flex" alignItems="center" gap={1} sx={{ mb: 2 }}>
            <Typography variant="h6">Key Performance Indicators</Typography>
          </Box>
          <Grid container spacing={2}>
            <Grid size={{ xs: 12, sm: 6, md: 3 }}>
              <Box
                textAlign="center"
                sx={{
                  p: 2,
                  borderRadius: 1,
                  backgroundColor: 'primary.light',
                  color: 'primary.contrastText',
                }}
              >
                <Typography variant="h4" fontWeight="bold">
                  {totalWorkouts}
                </Typography>
                <Typography variant="body2" sx={{ opacity: 0.9 }}>
                  Total Workouts
                </Typography>
              </Box>
            </Grid>
            <Grid size={{ xs: 12, sm: 6, md: 3 }}>
              <Box
                textAlign="center"
                sx={{
                  p: 2,
                  borderRadius: 1,
                  backgroundColor: 'secondary.light',
                  color: 'secondary.contrastText',
                }}
              >
                <Typography variant="h4" fontWeight="bold">
                  {Math.round(totalVolume / 1000)}k
                </Typography>
                <Typography variant="body2" sx={{ opacity: 0.9 }}>
                  Total Volume (lbs)
                </Typography>
              </Box>
            </Grid>
            <Grid size={{ xs: 12, sm: 6, md: 3 }}>
              <Box
                textAlign="center"
                sx={{
                  p: 2,
                  borderRadius: 1,
                  backgroundColor: 'success.light',
                  color: 'success.contrastText',
                }}
              >
                <Typography variant="h4" fontWeight="bold">
                  {oneRepMaxes.length}
                </Typography>
                <Typography variant="body2" sx={{ opacity: 0.9 }}>
                  1RM Records
                </Typography>
              </Box>
            </Grid>
            <Grid size={{ xs: 12, sm: 6, md: 3 }}>
              <Box
                textAlign="center"
                sx={{
                  p: 2,
                  borderRadius: 1,
                  backgroundColor: 'info.light',
                  color: 'info.contrastText',
                }}
              >
                <Typography variant="h4" fontWeight="bold">
                  {Math.round(latestVolume)}
                </Typography>
                <Typography variant="body2" sx={{ opacity: 0.9 }}>
                  Latest Volume (lbs)
                </Typography>
              </Box>
            </Grid>
          </Grid>
        </CardContent>
      </Card>

      {/* Active Program Section */}
      {activeProgram && (
        <Box sx={{ mb: 3 }}>
          <ActionCard title="Active Program" clickable onClick={handleActiveProgramClick}>
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
        </Box>
      )}

      {/* Recent Achievements Section */}
      {recentOneRepMaxes.length > 0 && (
        <Card sx={{ mb: 3 }}>
          <CardContent>
            <Box display="flex" alignItems="center" gap={1} sx={{ mb: 2 }}>
              <Typography variant="h6">Recent Achievements</Typography>
              <Typography variant="body2" color="text.secondary">
                • Your latest personal records
              </Typography>
            </Box>
            <Grid container spacing={2}>
              {recentOneRepMaxes.slice(0, 6).map((oneRepMax, index) => (
                <Grid size={{ xs: 12, sm: 6, md: 4 }} key={index}>
                  <Box
                    sx={{
                      p: 2,
                      border: 1,
                      borderColor: 'divider',
                      borderRadius: 1,
                      backgroundColor: 'success.light',
                      color: 'success.contrastText',
                      transition: 'all 0.2s ease-in-out',
                      '&:hover': {
                        transform: 'translateY(-2px)',
                        boxShadow: 2,
                      },
                    }}
                  >
                    <Typography variant="body1" fontWeight="medium" sx={{ mb: 1 }}>
                      ✅ New 1RM: {oneRepMax.exercise_name}
                    </Typography>
                    <Typography variant="h6" fontWeight="bold">
                      {oneRepMax.one_rep_max} {oneRepMax.unit}
                    </Typography>
                    <Typography variant="caption" sx={{ opacity: 0.9 }}>
                      Achieved: {formatDate(oneRepMax.updated_at)}
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
