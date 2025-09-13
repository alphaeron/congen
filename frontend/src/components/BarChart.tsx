import { default as ShowChartIcon } from '@mui/icons-material/ShowChart';
import { Box, Card, CardContent, Typography, useTheme } from '@mui/material';
import { ResponsiveBar } from '@nivo/bar';
import React, { useState, useMemo } from 'react';

import type { UserExercisePoolResponse } from '../api/types';
import { createCongenNivoTheme, congenColorSchemes } from '../theme/nivoTheme';

interface BarChartProps {
  exercisePoolData: UserExercisePoolResponse | null;
  title?: string;
  description?: string;
  height?: number;
}

/**
 * Bar Chart component for displaying exercise rotation management.
 *
 * This component accepts exercise pool data and displays available vs
 * previously used exercises in a horizontal bar chart format.
 *
 * @param exercisePoolData The exercise pool data containing exercise information
 * @param title Optional title for the chart
 * @param description Optional description for the chart
 * @param height Optional height for the chart container
 * @return Bar Chart component
 */
export const BarChart: React.FC<BarChartProps> = ({
  exercisePoolData,
  title = 'Rotation Management',
  description = 'Available vs previously used exercises',
  height = 300,
}) => {
  const theme = useTheme();
  const nivoTheme = createCongenNivoTheme(theme.palette.mode);
  const [selectedItems, setSelectedItems] = useState<string[]>([]);

  // Prepare chart data
  const chartData = useMemo(() => {
    if (!exercisePoolData) return [];

    const availableCount = exercisePoolData.available_exercises;
    const previouslyUsedCount = exercisePoolData.previously_used_exercises.length;

    return [
      {
        category: 'Available',
        count: availableCount,
      },
      {
        category: 'Previously Used',
        count: previouslyUsedCount,
      },
    ];
  }, [exercisePoolData]);

  // Filter data based on legend selection
  const filteredData = useMemo(() => {
    if (selectedItems.length === 0) {
      return chartData;
    }
    return chartData.filter(item => selectedItems.includes(item.category));
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
          <ResponsiveBar
            data={filteredData}
            keys={['count']}
            indexBy="category"
            margin={{ top: 20, right: 20, bottom: 80, left: 60 }}
            padding={0.3}
            valueScale={{ type: 'linear' }}
            indexScale={{ type: 'band', round: true }}
            borderColor={{ from: 'color', modifiers: [['darker', 1.6]] }}
            axisTop={null}
            axisRight={null}
            axisBottom={{
              tickSize: 5,
              tickPadding: 5,
              tickRotation: 0,
              legend: 'Exercise Status',
              legendPosition: 'middle',
              legendOffset: 32,
            }}
            axisLeft={{
              tickSize: 5,
              tickPadding: 5,
              tickRotation: 0,
              legend: 'Number of Exercises',
              legendPosition: 'middle',
              legendOffset: -40,
            }}
            labelSkipWidth={12}
            labelSkipHeight={12}
            labelTextColor={{ from: 'color', modifiers: [['darker', 1.6]] }}
            theme={nivoTheme}
            tooltip={({ indexValue, value, color }) => (
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
                    backgroundColor: color,
                    borderRadius: '2px',
                    flexShrink: 0,
                  }}
                />
                <div>
                  <span>{indexValue}: </span>
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
