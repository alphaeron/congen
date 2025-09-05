import { default as RefreshIcon } from '@mui/icons-material/Refresh';
import ShowChartIcon from '@mui/icons-material/ShowChart';
import { Box, useTheme, Typography, IconButton, Card, CardContent, Tooltip } from '@mui/material';
import { ResponsiveSunburst } from '@nivo/sunburst';
import React, { useState, useMemo } from 'react';

import { 
  WeightUnit,
  type ProgrammedWorkoutWithStages,
  type WorkoutStageWithExercises,
  type UserWeightUnitPreference,
 } from '../api/types';
import { createCongenNivoTheme } from '../theme/nivoTheme';

// Custom layer to display total volume in the center
interface CenterMetricProps {
  centerX: number;
  centerY: number;
  data: SunburstData;
}

const CenterMetric = (props: CenterMetricProps) => {
  const theme = useTheme();
  const { centerX, centerY, data } = props;

  // Calculate total volume from current data
  const calculateTotalVolume = (node: SunburstData): number => {
    if (!node) return 0;
    if (node.children && node.children.length > 0) {
      return node.children.reduce(
        (sum: number, child: SunburstData) => sum + calculateTotalVolume(child),
        0
      );
    }
    return node.loc || 0;
  };

  const totalVolume = calculateTotalVolume(data);

  return (
    <g transform={`translate(${centerX},${centerY})`}>
      <circle
        r={40}
        fill={theme.palette.background.paper}
        stroke={theme.palette.divider}
        strokeWidth={1}
      />
      <text
        textAnchor="middle"
        dominantBaseline="central"
        style={{
          fontSize: '12px',
          fontWeight: 'bold',
          fill: theme.palette.text.primary,
        }}
        y={-8}
      >
        Total
      </text>
      <text
        textAnchor="middle"
        dominantBaseline="central"
        style={{
          fontSize: '16px',
          fontWeight: 'bold',
          fill: theme.palette.primary.main,
        }}
        y={8}
      >
        {totalVolume.toLocaleString()}
      </text>
    </g>
  );
};

interface SunburstData {
  name: string;
  loc?: number;
  children?: SunburstData[];
}

interface SunburstChartProps {
  workoutData: ProgrammedWorkoutWithStages; // Single workout data
  exerciseMuscleData: Map<string, string[]>;
  weightUnitPreferences: UserWeightUnitPreference[];
  selectedExercise: string;
}

/**
 * Sunburst Chart component for displaying exercise volume hierarchy with drill-down functionality.
 *
 * This component accepts single workout data and handles all data transformations
 * internally to calculate exercise volume hierarchy and display it in a sunburst chart.
 *
 * @param workoutData Single workout data to display
 * @param exerciseData Map of exercise data for categorization
 * @param exerciseMuscleData Map of exercise to muscle group mappings
 * @param weightUnitPreferences User's weight unit preferences
 * @param selectedExercise Selected exercise filter
 * @return Sunburst Chart component
 */
export const SunburstChart: React.FC<SunburstChartProps> = ({
  workoutData,
  exerciseMuscleData,
  weightUnitPreferences,
  selectedExercise,
}) => {
  const theme = useTheme();
  const nivoTheme = createCongenNivoTheme(theme.palette.mode);
  const [currentData, setCurrentData] = useState<SunburstData | null>(null);

  // Use the single workout data
  const workouts = useMemo(() => {
    return workoutData ? [workoutData] : [];
  }, [workoutData]);

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

  // Create exerciseMap from workout data for volume calculations
  const exerciseMap = useMemo(() => {
    const map = new Map<string, { totalVolume: number }>();

    if (!workouts.length) return map;

    workouts.forEach(workout => {
      (workout.stages as WorkoutStageWithExercises[])?.forEach(stage => {
        stage.exercises?.forEach(exercise => {
          const exerciseName = (exercise.exercise as Record<string, unknown>)
            .exercise_name as string;
          const existing = map.get(exerciseName) || { totalVolume: 0 };

          // Calculate volume from set schemes with weight unit conversion
          let exerciseVolume = 0;
          exercise.set_schemes?.forEach(setScheme => {
            // Use performed values if available, otherwise use target values
            const weight = setScheme.performed_weight || setScheme.target_weight;
            const reps = setScheme.performed_rep_count || setScheme.target_rep_count;

            if (weight && reps) {
              const convertedWeight = convertWeightToUserUnit(weight, exerciseName);
              exerciseVolume += convertedWeight * reps;
            }
          });

          existing.totalVolume += exerciseVolume;
          map.set(exerciseName, existing);
        });
      });
    });

    return map;
  }, [workouts, weightUnitPreferences]);

  // Calculate exercise statistics for sunburst chart
  const exerciseStats = useMemo(() => {
    const uniqueExercises = Array.from(exerciseMap.keys()).sort();
    const exercisesToProcess = selectedExercise === 'all' ? uniqueExercises : [selectedExercise];

    return exercisesToProcess.map(exerciseName => {
      const performanceData = exerciseMap.get(exerciseName);

      return {
        name: exerciseName,
        totalVolume: performanceData?.totalVolume || 0,
      };
    });
  }, [exerciseMap, selectedExercise]);

  // Prepare sunburst data
  const sunburstData = useMemo(() => {
    if (!exerciseStats.length) {
      return {
        name: 'Exercise Volume',
        children: [],
      };
    }

    // Build the Nivo data structure following the correct pattern
    // Group exercises by muscle groups to avoid duplicates
    const muscleGroups = new Map<
      string,
      { name: string; children: Array<{ name: string; loc: number }> }
    >();

    exerciseStats.forEach(exercise => {
      const individualMuscles = exerciseMuscleData.get(exercise.name) || [];

      // If no muscle data, create a default group
      if (individualMuscles.length === 0) {
        const defaultGroup = muscleGroups.get('Other') || { name: 'Other', children: [] };
        defaultGroup.children.push({
          name: exercise.name,
          loc: exercise.totalVolume,
        });
        muscleGroups.set('Other', defaultGroup);
      } else {
        // For exercises that belong to multiple muscle groups, create unique keys
        // by combining exercise name with muscle group
        individualMuscles.forEach(muscle => {
          const existing = muscleGroups.get(muscle);
          const uniqueExerciseName =
            individualMuscles.length > 1 ? `${exercise.name} (${muscle})` : exercise.name;

          if (existing) {
            // Check if this exercise already exists in this muscle group
            const existingExercise = existing.children.find(
              child => child.name === uniqueExerciseName
            );
            if (existingExercise) {
              existingExercise.loc += exercise.totalVolume;
            } else {
              existing.children.push({
                name: uniqueExerciseName,
                loc: exercise.totalVolume,
              });
            }
          } else {
            muscleGroups.set(muscle, {
              name: muscle,
              children: [
                {
                  name: uniqueExerciseName,
                  loc: exercise.totalVolume,
                },
              ],
            });
          }
        });
      }
    });

    // Convert to array and ensure unique names at all levels
    const children = Array.from(muscleGroups.values()).map(group => ({
      name: group.name,
      children: group.children,
    }));

    return {
      name: 'Exercise Volume',
      children,
    };
  }, [exerciseStats, exerciseMuscleData]);

  // Initialize current data
  React.useEffect(() => {
    if (sunburstData && !currentData) {
      setCurrentData(sunburstData);
    }
  }, [sunburstData, currentData]);

  // Handle click on sunburst segments
  const handleArcClick = (node: { data?: SunburstData }) => {
    if (node.data?.children && node.data.children.length > 0) {
      setCurrentData(node.data);
    }
  };

  // Handle home button click
  const handleHomeClick = () => {
    setCurrentData(sunburstData);
  };

  // Don't render if no data
  if (!currentData || !currentData.children?.length) {
    return null;
  }

  return (
    <Card
      sx={{
        '&:hover': {
          transform: 'none',
          boxShadow: 'none',
        },
      }}
    >
      <CardContent>
        <Box display="flex" alignItems="center" gap={1} sx={{ mb: 2 }}>
          <ShowChartIcon color="primary" />
          <Tooltip title="Volume distribution by muscle groups for this workout" arrow>
            <Typography variant="h6">Exercise Volume Hierarchy</Typography>
          </Tooltip>
        </Box>

        {/* Navigation */}
        <Box sx={{ display: 'flex', alignItems: 'center' }}>
          <IconButton onClick={handleHomeClick} size="small" title="Reset chart">
            <RefreshIcon />
          </IconButton>
        </Box>

        {/* Sunburst Chart */}
        <Box sx={{ height: 300 }}>
          <ResponsiveSunburst
            data={currentData}
            margin={{ top: 10, right: 10, bottom: 10, left: 10 }}
            id="name"
            value="loc"
            cornerRadius={2}
            borderColor={{ theme: 'background' }}
            colors={{ scheme: 'nivo' }}
            childColor={{
              from: 'color',
              modifiers: [['brighter', 0.1]],
            }}
            enableArcLabels={true}
            arcLabelsSkipAngle={10}
            arcLabelsTextColor={{
              from: 'color',
              modifiers: [['darker', 1.4]],
            }}
            theme={nivoTheme}
            onClick={handleArcClick}
            layers={[
              'arcs',
              'arcLabels',
              (props: { centerX: number; centerY: number }) => (
                <CenterMetric {...props} data={currentData} />
              ),
            ]}
          />
        </Box>
      </CardContent>
    </Card>
  );
};
