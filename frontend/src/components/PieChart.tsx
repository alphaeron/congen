import { default as ShowChartIcon } from '@mui/icons-material/ShowChart';
import { Box, Card, CardContent, Typography, useTheme } from '@mui/material';
import { ResponsivePie } from '@nivo/pie';
import React, { useState, useMemo } from 'react';

import { createCongenNivoTheme } from '../theme/nivoTheme';
import type { UserDataExport, ProgramWithWorkouts, Exercise } from '../api/types';
import { categorizeExerciseVolume } from '../common/utils';
import { replaceUnderscoresWithSpaces } from '../common/utils';

interface PieData {
  id: string;
  label: string;
  value: number;
}

interface PieChartProps {
  userDataExport: UserDataExport | null;
  exerciseData: Map<string, Exercise>;
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
  exerciseData,
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

    const allWorkouts: any[] = [];
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
      workoutData.stages.forEach((stage: any) => {
        const stageName = stage.stage.name || 'Unknown Stage';

        stage.exercises.forEach((exerciseWithSchemes: any) => {
          // Calculate volume for this exercise in this stage
          let stageVolume = 0;
          exerciseWithSchemes.set_schemes.forEach((setScheme: any) => {
            const weight = setScheme.performed_weight || setScheme.target_weight || 0;
            const reps = setScheme.performed_rep_count || setScheme.target_rep_count || 0;
            const bandWeight = setScheme.band_weight_lbs
              ? (setScheme.band_weight_lbs as { weight_lbs: number })?.weight_lbs || 0
              : 0;

            stageVolume += (weight + bandWeight) * reps;
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

  const handleLegendClick = (data: { id?: string; label?: string }) => {
    const itemId = data.id || data.label;
    if (itemId) {
      setSelectedItems(prev => {
        if (prev.includes(itemId)) {
          return prev.filter(id => id !== itemId);
        } else {
          return [...prev, itemId];
        }
      });
    }
  };

  // Don't render if no data
  if (!chartData.length) {
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
            theme={nivoTheme}
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
                onClick: (datum: any) => {
                  const itemId = typeof datum.id === 'string' ? datum.id : datum.label;
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
    </Card>
  );
};
