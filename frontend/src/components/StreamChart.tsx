import { default as ShowChartIcon } from '@mui/icons-material/ShowChart';
import { Box, Card, CardContent, Typography, useTheme } from '@mui/material';
import { ResponsiveStream } from '@nivo/stream';
import React, { useMemo } from 'react';

import { createCongenNivoTheme } from '../theme/nivoTheme';
import type { UserDataExport, ProgramWithWorkouts, Exercise } from '../api/types';
import { getUserWeightUnitPreferences } from '../api/userWeightUnitPreference';
import type { UserWeightUnitPreference } from '../api/userWeightUnitPreference';
import { categorizeExerciseVolume, convertWeightToPounds } from '../common/utils';
import { replaceUnderscoresWithSpaces, formatDate } from '../common/utils';

interface StreamData {
  date: string;
  [key: string]: string | number;
}

interface StreamChartProps {
  userDataExport: UserDataExport | null;
  exerciseData: Map<string, Exercise>;
  weightUnitPreferences: UserWeightUnitPreference[];
  title?: string;
  description?: string;
  height?: number;
}

/**
 * Stream Chart component for displaying volume flow over time.
 * 
 * This component accepts raw workout data and handles all data transformations
 * internally to calculate volume data and display it in a stream chart.
 *
 * @param userDataExport The raw user data export containing all workout information
 * @param exerciseData Map of exercise data for categorization
 * @param weightUnitPreferences User's weight unit preferences for conversion
 * @param title Optional title for the chart
 * @param description Optional description for the chart
 * @param height Optional height for the chart container
 * @return Stream Chart component
 */
export const StreamChart: React.FC<StreamChartProps> = ({
  userDataExport,
  exerciseData,
  weightUnitPreferences,
  title = 'Volume Flow Over Time',
  description = 'Training volume distribution across workout types',
  height = 400,
}) => {
  const theme = useTheme();
  const nivoTheme = createCongenNivoTheme(theme.palette.mode);

  // Extract workouts from the raw data
  const workouts = useMemo(() => {
    if (!userDataExport?.training_programs?.length) return [];

    return userDataExport.training_programs.flatMap((program: ProgramWithWorkouts) =>
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
    );
  }, [userDataExport]);

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

  // Prepare stream chart data
  const streamData = useMemo(() => {
    return volumeData.map(volume => ({
      date: volume.date,
      'Max Effort': volume.maxEffortVolume,
      'Dynamic Effort': volume.dynamicEffortVolume,
      Accessory: volume.accessoryVolume,
    }));
  }, [volumeData]);

  const keys = ['Max Effort', 'Dynamic Effort', 'Accessory'];

  // Don't render if no data
  if (!streamData.length) {
    return null;
  }

  return (
    <Card variant="outlined">
      <CardContent>
        <Box display="flex" alignItems="center" gap={1} sx={{ mb: 2 }}>
          <ShowChartIcon color="secondary" />
          <Typography variant="h6">{title}</Typography>
        </Box>
        <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
          {description}
        </Typography>
        <Box sx={{ height }}>
          <ResponsiveStream
            data={streamData}
            keys={keys}
            margin={{ top: 50, right: 110, bottom: 50, left: 60 }}
            colors={{ scheme: 'nivo' }}
            theme={nivoTheme}
            tooltip={({ id, value, color }) => (
              <div
                style={{
                  padding: '8px 12px',
                  color: nivoTheme.tooltip.container.color,
                  background: nivoTheme.tooltip.container.background,
                  borderRadius: nivoTheme.tooltip.container.borderRadius,
                  boxShadow: nivoTheme.tooltip.container.boxShadow,
                  border: nivoTheme.tooltip.container.border,
                  whiteSpace: 'nowrap',
                  fontSize: nivoTheme.tooltip.container.fontSize,
                  fontFamily: nivoTheme.tooltip.container.fontFamily,
                  lineHeight: nivoTheme.tooltip.container.lineHeight,
                  display: 'flex',
                  alignItems: 'center',
                  gap: '8px',
                }}
              >
                <div
                  style={{
                    width: '12px',
                    height: '12px',
                    backgroundColor: color,
                    borderRadius: '2px',
                    flexShrink: 0,
                  }}
                />
                <div>
                  <span>{id}: </span>
                  <span style={{ fontWeight: 'bold' }}>{value}</span>
                </div>
              </div>
            )}
          />
        </Box>
      </CardContent>
    </Card>
  );
};
