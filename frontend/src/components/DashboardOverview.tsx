import { Box, Card, CardContent, Grid, Typography, Stack } from '@mui/material';
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
      {/* Main Dashboard Layout: 3/4 Status Card + 1/4 Sidebar */}
      {performanceScores ? (
        <Grid container spacing={2} sx={{ mb: 3 }}>
          {/* Status Card - 3/4 width */}
          <Grid size={{ xs: 12, lg: 9 }}>
            <AdventurerStatusCard
              scores={performanceScores}
              metrics={performanceMetrics}
              weeklyTests={weeklyTests}
              wilksScore={wilksScore}
              userName={user.name}
            />
          </Grid>
          
          {/* Sidebar - 1/4 width */}
          <Grid size={{ xs: 12, lg: 3 }}>
            {/* Weekly Test Tracker */}
            <WeeklyTestTracker 
              weeklyTests={weeklyTests} 
              onTestUpdate={refreshPerformanceData}
            />
          </Grid>
        </Grid>
      ) : (
        <React.Fragment />
      )}
    </React.Fragment>
  );
};
