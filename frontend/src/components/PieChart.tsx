import { Box, CardContent, useTheme } from '@mui/material';
import { ResponsivePie } from '@nivo/pie';
import React, { useState, useMemo } from 'react';

import { GameText, GameCard, GAME_CLASSES } from './GameTheme';
import type {
  UserDataExport,
  Exercise,
  ProgrammedWorkoutWithStages,
  WorkoutStageWithExercises,
  UserWeightUnitPreference,
} from '../api/types';
import { KG_TO_LBS } from '../common/utils';
import { createCongenNivoTheme } from '../theme/nivoTheme';

interface PieChartProps {
  userDataExport: UserDataExport | null;
  exerciseData: Map<string, Exercise>;
  weightUnitPreferences?: UserWeightUnitPreference[];
  title?: string;
  description?: string;
  height?: number;
}

/**
 * Pie Chart component for displaying exercise distribution.
 *
 * This component accepts raw workout data and handles all data transformations
 * internally to calculate exercise distribution and display it in a pie chart.
 *
 * @param userDataExport The raw user data export containing all workout information
 * @param exerciseData Map of exercise data for categorization
 * @param title Optional title for the chart
 * @param description Optional description for the chart
 * @param height Optional height for the chart container
 * @return Pie Chart component
 */
export const PieChart: React.FC<PieChartProps> = ({
  userDataExport,
  title = 'Exercise Distribution',
  description = 'Volume by workout stage',
  height = 300,
}) => {
  const theme = useTheme();
  const nivoTheme = createCongenNivoTheme(theme.palette.mode);
  const [selectedItems, setSelectedItems] = useState<string[]>([]);

  // Extract workouts from the raw data
  const workouts = useMemo(() => {
    if (!userDataExport?.training_programs?.length) return [];

    const allWorkouts: ProgrammedWorkoutWithStages[] = [];
    userDataExport.training_programs.forEach(program => {
      allWorkouts.push(...program.workouts);
    });

    return allWorkouts;
  }, [userDataExport]);

  // Calculate exercise volume by workout stage data
  const exerciseCorrelationData = useMemo(() => {
    if (!workouts.length) return [];

    const stageVolumeMap = new Map<string, number>();

    workouts.forEach(workoutData => {
      (workoutData.stages as WorkoutStageWithExercises[]).forEach(stage => {
        const stageName =
          ((stage.stage as Record<string, unknown>).name as string) || 'Unknown Stage';

        stage.exercises.forEach(exerciseWithSchemes => {
          // Calculate volume for this exercise in this stage
          let stageVolume = 0;
          exerciseWithSchemes.set_schemes.forEach(setScheme => {
            const weight = setScheme.performed_weight || setScheme.target_weight || 0;
            const reps = setScheme.performed_rep_count || setScheme.target_rep_count || 0;
            const bandWeight = setScheme.band_weight_lbs
              ? (setScheme.band_weight_lbs as { weight_lbs: number })?.weight_lbs || 0
              : 0;

            const convertedWeight = weight * KG_TO_LBS;
            stageVolume += (convertedWeight + bandWeight) * reps;
          });

          // Add to stage volume
          const currentVolume = stageVolumeMap.get(stageName) || 0;
          stageVolumeMap.set(stageName, currentVolume + stageVolume);
        });
      });
    });

    // Convert to the expected format for the pie chart
    return Array.from(stageVolumeMap.entries()).map(([stageName, volume]) => ({
      exercise: stageName, // Using stage name as exercise name for the chart
      category: stageName,
      volume,
      frequency: 1, // Not used for pie chart
      maxWeight: 0, // Not used for pie chart
    }));
  }, [workouts]);

  // Prepare chart data
  const chartData = useMemo(() => {
    // Aggregate volume by workout stage
    const stageVolumeMap = new Map<string, number>();

    exerciseCorrelationData.forEach(ex => {
      const currentVolume = stageVolumeMap.get(ex.category) || 0;
      stageVolumeMap.set(ex.category, currentVolume + ex.volume);
    });

    return Array.from(stageVolumeMap.entries()).map(([category, volume]) => ({
      id: category,
      label: category,
      value: volume,
    }));
  }, [exerciseCorrelationData]);

  // Filter data based on legend selection
  const filteredData = useMemo(() => {
    if (selectedItems.length === 0) {
      return chartData;
    }
    return chartData.filter(item => selectedItems.includes(item.id));
  }, [chartData, selectedItems]);

  // Don't render if no data
  if (!chartData.length) {
    return null;
  }

  return (
    <GameCard>
      <CardContent>
        <Box display="flex" alignItems="center" gap={1} sx={{ mb: 2 }}>
          <GameText variant="h6">{title}</GameText>
        </Box>
        <GameText variant="body2" textVariant="secondary" className={GAME_CLASSES.marginBottom2}>
          {description}
        </GameText>
        <Box sx={{ height }}>
          <ResponsivePie
            data={filteredData}
            margin={{ top: 20, right: 20, bottom: 20, left: 20 }}
            innerRadius={0.5}
            padAngle={0.7}
            cornerRadius={3}
            activeOuterRadiusOffset={8}
            borderWidth={1}
            borderColor={{ from: 'color', modifiers: [['darker', 0.2]] }}
            arcLinkLabelsSkipAngle={10}
            arcLinkLabelsTextColor="#333333"
            arcLinkLabelsThickness={2}
            arcLinkLabelsColor={{ from: 'color' }}
            arcLabelsSkipAngle={10}
            arcLabelsTextColor={{ from: 'color', modifiers: [['darker', 2]] }}
            animate={true}
            motionConfig="gentle"
            theme={nivoTheme}
            tooltip={({ datum }) => (
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
                  fontFamily: '"Inter", "Roboto", "Helvetica", "Arial", sans-serif',
                  lineHeight: '1.4',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '8px',
                }}
              >
                <div
                  style={{
                    width: '12px',
                    height: '12px',
                    backgroundColor: datum.color,
                    borderRadius: '2px',
                    flexShrink: 0,
                  }}
                />
                <div>
                  <span>{datum.label}: </span>
                  <span style={{ fontWeight: 'bold' }}>{datum.value}</span>
                </div>
              </div>
            )}
            legends={[
              {
                anchor: 'bottom',
                direction: 'row',
                justify: false,
                translateX: 0,
                translateY: 56,
                itemsSpacing: 0,
                itemWidth: 100,
                itemHeight: 18,
                itemTextColor: '#333333',
                itemDirection: 'left-to-right',
                itemOpacity: 1,
                symbolSize: 18,
                symbolShape: 'circle',
                onClick: (datum: { id?: string | number; label?: string | number }) => {
                  const itemId =
                    typeof datum.id === 'string' ? datum.id : String(datum.label || '');
                  if (itemId) {
                    setSelectedItems(prev => {
                      if (prev.includes(itemId)) {
                        return prev.filter(id => id !== itemId);
                      } else {
                        return [...prev, itemId];
                      }
                    });
                  }
                },
                effects: [
                  {
                    on: 'hover',
                    style: {
                      itemTextColor: '#000',
                    },
                  },
                ],
              },
            ]}
          />
        </Box>
      </CardContent>
    </GameCard>
  );
};
