import { default as ShowChartIcon } from '@mui/icons-material/ShowChart';
import { Box, Card, CardContent, Typography, useTheme } from '@mui/material';
import { ResponsivePie } from '@nivo/pie';
import React, { useState, useMemo } from 'react';

import type { UserExercisePoolResponse } from '../api/types';
import { capitalizeEachWord } from '../common/utils';
import { createCongenNivoTheme, congenColorSchemes } from '../theme/nivoTheme';

interface ExercisePoolPieChartProps {
  exercisePoolData: UserExercisePoolResponse | null;
  title?: string;
  description?: string;
  height?: number;
}

/**
 * Exercise Pool Pie Chart component for displaying pool availability.
 *
 * This component accepts exercise pool data and displays available vs
 * unavailable exercises in a pie chart format.
 *
 * @param exercisePoolData The exercise pool data containing availability information
 * @param title Optional title for the chart
 * @param description Optional description for the chart
 * @param height Optional height for the chart container
 * @return Exercise Pool Pie Chart component
 */
export const ExercisePoolPieChart: React.FC<ExercisePoolPieChartProps> = ({
  exercisePoolData,
  title = 'Pool Availability',
  description = 'Available vs unavailable exercises',
  height = 300,
}) => {
  const theme = useTheme();
  const nivoTheme = createCongenNivoTheme(theme.palette.mode);
  const [selectedItems] = useState<string[]>([]);

  // Prepare chart data
  const chartData = useMemo(() => {
    if (!exercisePoolData) return [];

    const available = exercisePoolData.available_exercises;
    const unavailable = exercisePoolData.total_exercises - available;

    return [
      {
        id: 'available',
        label: capitalizeEachWord('available'),
        value: available,
        color: congenColorSchemes.exercise.compound,
      },
      {
        id: 'unavailable',
        label: capitalizeEachWord('unavailable'),
        value: unavailable,
        color: congenColorSchemes.exercise.other,
      },
    ];
  }, [exercisePoolData]);

  // Filter data based on legend selection
  const filteredData = useMemo(() => {
    if (selectedItems.length === 0) {
      return chartData;
    }
    return chartData.filter(item => selectedItems.includes(item.id));
  }, [chartData, selectedItems]);

  // Don't render if no data
  if (!chartData.length || !exercisePoolData) {
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
            arcLinkLabelsTextColor="transparent"
            arcLinkLabelsThickness={0}
            arcLinkLabelsColor="transparent"
            arcLabelsSkipAngle={10}
            arcLabelsTextColor={{ from: 'color', modifiers: [['darker', 2]] }}
            theme={nivoTheme}
            colors={{ scheme: 'nivo' }}
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
          />
        </Box>
      </CardContent>
    </Card>
  );
};
