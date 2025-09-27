import { default as RadarIcon } from '@mui/icons-material/Radar';
import { Box, useTheme, Tooltip } from '@mui/material';
import { ResponsiveRadar } from '@nivo/radar';
import React, { useMemo } from 'react';

import type {
  Exercise,
  ProgrammedWorkoutWithStages,
  WorkoutStageWithExercises,
} from '../api/types';
import { createCongenNivoTheme } from '../theme/nivoTheme';
import { GameText, GameCard, GAME_CLASSES } from './GameTheme';

interface RadarChartProps {
  weekWorkouts: ProgrammedWorkoutWithStages[]; // Array of week workout data
  exerciseData: Map<string, Exercise>;
  title?: string;
  height?: number;
}

/**
 * Radar Chart component for displaying movement type distribution.
 *
 * This component accepts week workout data and exercise data, then calculates
 * the distribution of movement types across all exercises in the week.
 *
 * @param weekWorkouts Array of workout data for the week
 * @param exerciseData Map of exercise names to exercise details
 * @param title Optional title for the chart
 * @param description Optional description for the chart
 * @param height Optional height for the chart container
 * @return Radar Chart component
 */
export const RadarChart: React.FC<RadarChartProps> = ({
  weekWorkouts,
  exerciseData,
  title = 'Movement Type Distribution',
  height = 300,
}) => {
  const theme = useTheme();
  const nivoTheme = createCongenNivoTheme(theme.palette.mode);

  // Calculate movement type distribution
  const movementTypeData = useMemo(() => {
    const movementTypeCounts = new Map<string, number>();

    // Iterate through all workouts in the week
    weekWorkouts.forEach(weekWorkout => {
      // The weekWorkout is already the ProgrammedWorkoutWithStages
      const workout = weekWorkout;

      // If the workout has stages with exercises, process them
      if (workout.stages) {
        (workout.stages as WorkoutStageWithExercises[]).forEach(stage => {
          if (stage.exercises) {
            stage.exercises.forEach(exerciseWithSchemes => {
              const exerciseName = exerciseWithSchemes.exercise.exercise_name;
              const exercise = exerciseData.get(exerciseName);

              if (exercise && exercise.movement_type) {
                const movementType = exercise.movement_type;
                movementTypeCounts.set(
                  movementType,
                  (movementTypeCounts.get(movementType) || 0) + 1
                );
              }
            });
          }
        });
      }
    });

    // Convert to radar chart format
    const data = Array.from(movementTypeCounts.entries()).map(([movementType, count]) => ({
      movementType: movementType.replace(/_/g, ' ').replace(/\b\w/g, l => l.toUpperCase()),
      count,
    }));

    return data.sort((a, b) => b.count - a.count); // Sort by count descending
  }, [weekWorkouts, exerciseData]);

  // Don't render if no data
  if (!movementTypeData.length) {
    return null;
  }

  return (
    <GameCard
      sx={{
        '&:hover': {
          transform: 'none',
          boxShadow: 'none',
        },
      }}
    >
      <CardContent>
        <Box display="flex" alignItems="center" gap={1} sx={{ mb: 2 }}>
          <RadarIcon color="secondary" />
          <Tooltip title="Distribution of movement types across the week" arrow>
            <GameText variant="h6">{title}</GameText>
          </Tooltip>
        </Box>
        <Box sx={{ height }}>
          <ResponsiveRadar
            data={movementTypeData}
            keys={['count']}
            indexBy="movementType"
            valueFormat=".0f"
            margin={{ top: 70, right: 80, bottom: 40, left: 80 }}
            borderColor={{ from: 'color' }}
            gridLabelOffset={36}
            dotSize={10}
            dotColor={{ theme: 'background' }}
            dotBorderWidth={2}
            colors={{ scheme: 'nivo' }}
            blendMode="multiply"
            motionConfig="wobbly"
            theme={{
              ...nivoTheme,
              tooltip: {
                container: {
                  ...nivoTheme.tooltip.container,
                  whiteSpace: 'nowrap',
                },
              },
            }}
          />
        </Box>
      </CardContent>
    </GameCard>
  );
};
