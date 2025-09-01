import { default as TrendingUpIcon } from '@mui/icons-material/TrendingUp';
import { Box, Card, CardContent, Typography, useTheme } from '@mui/material';
import { ResponsiveChord } from '@nivo/chord';
import React, { useMemo } from 'react';

import { createCongenNivoTheme } from '../theme/nivoTheme';
import type { UserDataExport, ProgramWithWorkouts } from '../api/types';

interface WorkoutData {
  workout: any;
  stages: WorkoutStageWithExercises[];
}

interface WorkoutStageWithExercises {
  stage: any;
  exercises: ProgrammedExerciseWithSetSchemes[];
}

interface ProgrammedExerciseWithSetSchemes {
  exercise: any;
  set_schemes: any[];
}

interface ChordChartProps {
  userDataExport: UserDataExport | null;
  title?: string;
  description?: string;
  height?: number;
}

/**
 * Chord Chart component for displaying exercise correlations.
 * 
 * This component accepts raw workout data and handles all data transformations
 * internally to calculate exercise correlations and display them in a chord diagram.
 *
 * @param userDataExport The raw user data export containing all workout information
 * @param title Optional title for the chart
 * @param description Optional description for the chart
 * @param height Optional height for the chart container
 * @return Chord Chart component
 */
export const ChordChart: React.FC<ChordChartProps> = ({
  userDataExport,
  title = 'Exercise Correlations',
  description = 'Exercise pairing patterns in your workouts',
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

  // Calculate exercise correlations for chord diagram
  const exerciseCorrelations = useMemo(() => {
    if (!workouts.length) return [];

    const correlations: Array<{ source: string; target: string; value: number }> = [];
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
    <Card variant="outlined">
      <CardContent>
        <Box display="flex" alignItems="center" gap={1} sx={{ mb: 2 }}>
          <TrendingUpIcon color="info" />
          <Typography variant="h6">{title}</Typography>
        </Box>
        <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
          {description}
        </Typography>
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
            theme={{
              ...nivoTheme,
              tooltip: {
                container: {
                  background: '#fff',
                  color: '#333',
                  fontSize: '12px',
                  borderRadius: '4px',
                  boxShadow: '0 2px 8px rgba(0,0,0,0.15)',
                  border: '1px solid #ccc',
                  padding: '12px',
                  whiteSpace: 'nowrap',
                  fontFamily: 'Arial, sans-serif',
                  lineHeight: '1.4',
                },
              },
            }}
            tooltip={({ source, target, value }) => (
              <div
                style={{
                  padding: '12px',
                  color: '#333',
                  background: '#fff',
                  borderRadius: '4px',
                  boxShadow: '0 2px 8px rgba(0,0,0,0.15)',
                  border: '1px solid #ccc',
                  whiteSpace: 'nowrap',
                  fontSize: '12px',
                  fontFamily: 'Arial, sans-serif',
                  lineHeight: '1.4',
                }}
              >
                <div style={{ fontWeight: 'bold', marginBottom: '4px' }}>
                  {source.id} → {target.id}
                </div>
                <div>Value: {value}</div>
              </div>
            )}
          />
        </Box>
      </CardContent>
    </Card>
  );
};
