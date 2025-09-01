import { Box, useTheme } from '@mui/material';
import { ResponsiveRadialBar } from '@nivo/radial-bar';
import React, { useMemo } from 'react';

import { createCongenNivoTheme } from '../theme/nivoTheme';
import type { UserDataExport, ProgramWithWorkouts, Exercise, UserOneRepMax } from '../api/types';
import { getUserOneRepMaxes } from '../api/userOneRepMax';
import { getUserWeightUnitPreferences, WeightUnit } from '../api/userWeightUnitPreference';
import type { UserWeightUnitPreference } from '../api/userWeightUnitPreference';
import { formatDate } from '../common/utils';

interface RadialBarData {
  id: string;
  data: Array<{
    x: string;
    y: number;
  }>;
}

interface RadialBarChartProps {
  userDataExport: UserDataExport | null;
  exerciseData: Map<string, Exercise>;
  oneRepMaxes: UserOneRepMax[];
  weightUnitPreferences: UserWeightUnitPreference[];
  selectedExercise: string;
}

/**
 * Radial Bar Chart component for displaying exercise performance metrics.
 * 
 * This component accepts raw workout data and handles all data transformations
 * internally to calculate exercise performance metrics and display them in a radial bar chart.
 *
 * @param userDataExport The raw user data export containing all workout information
 * @param exerciseData Map of exercise data for categorization
 * @param oneRepMaxes User's one rep max data
 * @param weightUnitPreferences User's weight unit preferences
 * @param selectedExercise Selected exercise filter
 * @return Radial Bar Chart component
 */
export const RadialBarChart: React.FC<RadialBarChartProps> = ({
  userDataExport,
  exerciseData,
  oneRepMaxes,
  weightUnitPreferences,
  selectedExercise,
}) => {
  const theme = useTheme();
  const nivoTheme = createCongenNivoTheme(theme.palette.mode);

  // Extract workouts from the raw data
  const workouts = useMemo(() => {
    if (!userDataExport?.training_programs?.length) return [];

    const allWorkouts: any[] = [];
    userDataExport.training_programs.forEach(program => {
      allWorkouts.push(...program.workouts);
    });

    return allWorkouts;
  }, [userDataExport]);

  // Helper function to convert weight to user's preferred unit
  const convertWeightToUserUnit = (weight: number, exerciseName: string): number => {
    const preference = weightUnitPreferences.find(pref => pref.exercise_name === exerciseName);
    const userUnit = preference?.preferred_unit || WeightUnit.LBS;

    if (userUnit === WeightUnit.LBS) {
      return weight;
    } else if (userUnit === WeightUnit.KG) {
      return weight * 0.453592;
    }

    return weight;
  };

  // Create exerciseMap from workout data for volume and frequency calculations
  const exerciseMap = useMemo(() => {
    const map = new Map<
      string,
      { totalVolume: number; frequency: number; lastPerformed: Date | null }
    >();

    if (!workouts.length) return map;

    workouts.forEach(workout => {
      workout.stages?.forEach((stage: any) => {
        stage.exercises?.forEach((exercise: any) => {
          const exerciseName = exercise.exercise.exercise_name;
          const existing = map.get(exerciseName) || {
            totalVolume: 0,
            frequency: 0,
            lastPerformed: null,
          };

          // Calculate volume from set schemes with weight unit conversion
          let exerciseVolume = 0;
          exercise.set_schemes?.forEach((setScheme: any) => {
            // Use performed values if available, otherwise use target values
            const weight = setScheme.performed_weight || setScheme.target_weight;
            const reps = setScheme.performed_rep_count || setScheme.target_rep_count;

            if (weight && reps) {
              const convertedWeight = convertWeightToUserUnit(weight, exerciseName);
              exerciseVolume += convertedWeight * reps;
            }
          });

          existing.totalVolume += exerciseVolume;
          // Count frequency if there are any set schemes (programmed or performed)
          if (exercise.set_schemes && exercise.set_schemes.length > 0) {
            existing.frequency += 1;
          }

          // Update last performed date
          if (
            !existing.lastPerformed ||
            (workout.workout.created_at &&
              workout.workout.created_at > existing.lastPerformed)
          ) {
            existing.lastPerformed = workout.workout.created_at;
          }

          map.set(exerciseName, existing);
        });
      });
    });

    return map;
  }, [workouts, weightUnitPreferences]);

  // Calculate exercise statistics based on filtered data
  const exerciseStats = useMemo(() => {
    const uniqueExercises = Array.from(
      new Set([
        ...oneRepMaxes.map(orm => orm.exercise_name),
        ...Array.from(exerciseMap.keys()),
      ])
    ).sort();

    const exercisesToProcess = selectedExercise === 'all' ? uniqueExercises : [selectedExercise];

    return exercisesToProcess.map(exerciseName => {
      const exerciseOneRepMax = oneRepMaxes.find(orm => orm.exercise_name === exerciseName);
      const performanceData = exerciseMap.get(exerciseName);

      return {
        name: exerciseName,
        oneRepMax: exerciseOneRepMax,
        totalVolume: performanceData?.totalVolume || 0,
        frequency: performanceData?.frequency || 0,
        lastPerformed: performanceData?.lastPerformed || null,
      };
    });
  }, [oneRepMaxes, exerciseMap, selectedExercise]);

  // Prepare chart data
  const chartData = useMemo(() => {
    if (!exerciseStats.length) return [];

    return exerciseStats
      .map(exercise => {
        return {
          id: exercise.name,
          data: [
            {
              x: 'Volume',
              y: exercise.totalVolume,
            },
            {
              x: 'Frequency',
              y: exercise.frequency,
            },
            {
              x: '1RM Weight',
              y: exercise.oneRepMax?.one_rep_max || 0,
            },
          ],
        };
      })
      .slice(0, 10); // Top 10 exercises
  }, [exerciseStats]);

  // Don't render if no data
  if (!chartData.length) {
    return null;
  }

  return (
    <Box sx={{ height: 300 }}>
      <ResponsiveRadialBar
        data={chartData}
        valueFormat=">-0"
        padding={0.4}
        cornerRadius={2}
        margin={{ top: 40, right: 120, bottom: 40, left: 40 }}
        radialAxisStart={{ tickSize: 5, tickPadding: 5, tickRotation: 0 }}
        circularAxisOuter={{ tickSize: 2, tickPadding: 2, tickRotation: 0 }}
        labelsSkipAngle={10}
        colors={{ scheme: 'nivo' }}
        theme={nivoTheme}
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
            onClick: () => {},
          },
        ]}
      />
    </Box>
  );
};
