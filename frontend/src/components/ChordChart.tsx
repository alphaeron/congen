import { Box, useTheme, Tooltip, CardContent } from '@mui/material';
import { ResponsiveChord } from '@nivo/chord';
import React, { useMemo } from 'react';

import { createCongenNivoTheme } from '../theme/nivoTheme';
import { GameText, GameCard, GAME_CLASSES } from './GameTheme';

interface WorkoutStageWithExercises {
  stage: Record<string, unknown>;
  exercises: ProgrammedExerciseWithSetSchemes[];
}

interface ProgrammedExerciseWithSetSchemes {
  exercise: Record<string, unknown>;
  set_schemes: Record<string, unknown>[];
}

interface ChordChartProps {
  workoutData: Record<string, unknown>; // Single workout data
  title?: string;
  height?: number;
}

/**
 * Chord Chart component for displaying exercise correlations.
 *
 * This component accepts single workout data and handles all data transformations
 * internally to calculate exercise correlations and display them in a chord diagram.
 *
 * @param workoutData The single workout data containing exercise information
 * @param title Optional title for the chart
 * @param description Optional description for the chart
 * @param height Optional height for the chart container
 * @return Chord Chart component
 */
export const ChordChart: React.FC<ChordChartProps> = ({
  workoutData,
  title = 'Exercise Correlations',
  height = 400,
}) => {
  const theme = useTheme();
  const nivoTheme = createCongenNivoTheme(theme.palette.mode);

  // Use the single workout data
  const workouts = useMemo(() => {
    return workoutData ? [workoutData] : [];
  }, [workoutData]);

  // Calculate exercise correlations for chord diagram
  const exerciseCorrelations = useMemo(() => {
    if (!workouts.length) return [];

    const correlations: Array<{ source: string; target: string; value: number }> = [];
    const exercisePairs = new Map<string, number>();

    workouts.forEach(workoutData => {
      const workoutExercises = new Set<string>();

      (workoutData.stages as WorkoutStageWithExercises[]).forEach(stage => {
        stage.exercises.forEach(exerciseWithSchemes => {
          const exerciseName = (exerciseWithSchemes.exercise as Record<string, unknown>)
            .exercise_name;
          if (typeof exerciseName === 'string') {
            workoutExercises.add(exerciseName);
          }
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

  // Prepare chord data matrix
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

  // Don't render if no data
  if (!chordData.keys.length) {
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
          <Tooltip title="Exercise pairing patterns in your workouts" arrow>
            <GameText variant="h6">{title}</GameText>
          </Tooltip>
        </Box>
        <Box sx={{ height }}>
          <ResponsiveChord
            data={chordData.matrix}
            keys={chordData.keys}
            margin={{ top: 60, right: 60, bottom: 90, left: 60 }}
            valueFormat=".0f"
            padAngle={0.02}
            innerRadiusRatio={0.96}
            innerRadiusOffset={0.02}
            inactiveArcOpacity={0.25}
            arcBorderWidth={1}
            arcBorderColor={{ from: 'color', modifiers: [['darker', 0.4]] }}
            activeRibbonOpacity={0.75}
            inactiveRibbonOpacity={0.25}
            ribbonBorderWidth={1}
            ribbonBorderColor={{ from: 'color', modifiers: [['darker', 0.4]] }}
            enableLabel={true}
            label="id"
            labelOffset={12}
            labelRotation={-90}
            labelTextColor={{
              from: 'color',
              modifiers: [['darker', 1]],
            }}
            colors={{ scheme: 'nivo' }}
            animate={true}
            motionConfig="gentle"
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
