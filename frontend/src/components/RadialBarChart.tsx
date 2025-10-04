import { default as ShowChartIcon } from '@mui/icons-material/ShowChart';
import { Box, useTheme, CardContent } from '@mui/material';
import { ResponsiveRadialBar } from '@nivo/radial-bar';
import React, { useState, useMemo } from 'react';

import type { UserExercisePoolResponse, Exercise } from '../api/types';
import { capitalizeEachWord } from '../common/utils';
import { createCongenNivoTheme } from '../theme/nivoTheme';
import { GameText, GameCard, GAME_CLASSES } from './GameTheme';

interface RadialBarChartProps {
  exercisePoolData: UserExercisePoolResponse | null;
  title?: string;
  description?: string;
  height?: number;
}

/**
 * Radial Bar Chart component for displaying exercise variety distribution.
 *
 * This component accepts exercise pool data and displays primary vs accessory
 * exercise distribution in a radial bar chart format.
 *
 * @param exercisePoolData The exercise pool data containing categorized exercises
 * @param title Optional title for the chart
 * @param description Optional description for the chart
 * @param height Optional height for the chart container
 * @return Radial Bar Chart component
 */
export const RadialBarChart: React.FC<RadialBarChartProps> = ({
  exercisePoolData,
  title = 'Exercise Variety',
  description = 'Primary vs accessory exercise distribution',
  height = 300,
}) => {
  const theme = useTheme();
  const nivoTheme = createCongenNivoTheme(theme.palette.mode);
  const [selectedItems] = useState<string[]>([]);

  // Prepare chart data
  const chartData = useMemo(() => {
    if (!exercisePoolData) return [];

    const categories: Array<{
      id: string;
      data: Array<{
        x: string;
        y: number;
      }>;
    }> = [];

    // Define all available exercise categories based on workout stages
    const exerciseCategories = [
      { key: 'primary_exercises', id: 'Primary', name: 'Primary' },
      { key: 'accessory_exercises', id: 'Accessory', name: 'Accessory' },
      // Note: The current API only provides primary and accessory exercises
      // If more workout stages become available in the future, they can be added here
    ];

    // Process all exercise categories dynamically
    exerciseCategories.forEach(({ key, id, name }) => {
      const exercises = exercisePoolData[key as keyof UserExercisePoolResponse] as Exercise[];

      if (exercises && exercises.length > 0) {
        categories.push({
          id,
          data: [
            {
              x: capitalizeEachWord(name),
              y: exercises.length,
            },
          ],
        });
      }
    });

    return categories;
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
    <GameCard>
      <CardContent>
        <Box display="flex" alignItems="center" gap={1} sx={{ mb: 2 }}>
          <ShowChartIcon color="secondary" />
          <GameText variant="h6">{title}</GameText>
        </Box>
        <GameText variant="body2" textVariant="secondary" className={GAME_CLASSES.marginBottom2}>
          {description}
        </GameText>
        <Box sx={{ height }}>
          <ResponsiveRadialBar
            data={filteredData}
            valueFormat=">-.0f"
            padding={0.4}
            cornerRadius={2}
            margin={{ top: 40, right: 40, bottom: 40, left: 40 }}
            radialAxisStart={{ tickSize: 5, tickPadding: 5, tickRotation: 0 }}
            circularAxisOuter={{ tickSize: 5, tickPadding: 12, tickRotation: 0 }}
            animate={true}
            motionConfig="gentle"
            theme={nivoTheme}
            colors={{ scheme: 'nivo' }}
            tooltip={(props: {
              bar?: {
                data?: { x?: string; y?: number };
                category?: string;
                value?: number;
                color?: string;
              };
            }) => {
              if (!props?.bar) return null;

              const { bar } = props;
              const label = bar.data?.x || bar.category || 'Unknown';
              const value = bar.data?.y || bar.value || 0;
              const color = bar.color || '#000';

              return (
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
                    <span>{label}: </span>
                    <span style={{ fontWeight: 'bold' }}>{value}</span>
                  </div>
                </div>
              );
            }}
          />
        </Box>
      </CardContent>
    </GameCard>
  );
};
