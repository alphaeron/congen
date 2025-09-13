import { default as ShowChartIcon } from '@mui/icons-material/ShowChart';
import { Box, Card, CardContent, Typography, useTheme } from '@mui/material';
import { ResponsiveSunburst } from '@nivo/sunburst';
import React, { useState, useMemo } from 'react';

import type { UserExercisePoolResponse } from '../api/types';
import { createCongenNivoTheme, congenColorSchemes } from '../theme/nivoTheme';
import { capitalizeEachWord } from '../common/utils';

interface ExercisePoolSunburstChartProps {
  exercisePoolData: UserExercisePoolResponse | null;
  title?: string;
  description?: string;
  height?: number;
}

/**
 * Exercise Pool Sunburst Chart component for displaying exercise selection hierarchy.
 *
 * This component accepts exercise pool data and displays the hierarchical structure
 * of exercise categories, movement types, and equipment requirements.
 *
 * @param exercisePoolData The exercise pool data containing categorized exercises
 * @param title Optional title for the chart
 * @param description Optional description for the chart
 * @param height Optional height for the chart container
 * @return Exercise Pool Sunburst Chart component
 */
export const ExercisePoolSunburstChart: React.FC<ExercisePoolSunburstChartProps> = ({
  exercisePoolData,
  title = 'Exercise Selection',
  description = 'Hierarchical view of exercise pool structure',
  height = 300,
}) => {
  const theme = useTheme();
  const nivoTheme = createCongenNivoTheme(theme.palette.mode);
  const [selectedItems, setSelectedItems] = useState<string[]>([]);

  // Prepare sunburst data
  const sunburstData = useMemo(() => {
    if (!exercisePoolData) {
      return {
        name: 'Exercise Pool',
        children: [],
      };
    }

    // Group exercises by movement type within categories
    const categoryMap = new Map<string, Map<string, string[]>>();

    // Define all available exercise categories based on workout stages
    const exerciseCategories = [
      { key: 'primary_exercises', name: 'Primary' },
      { key: 'accessory_exercises', name: 'Accessory' },
      // Note: The current API only provides primary and accessory exercises
      // If more workout stages become available in the future, they can be added here
    ];

    // Process all exercise categories dynamically
    exerciseCategories.forEach(({ key, name }) => {
      const exercises = exercisePoolData[key as keyof UserExercisePoolResponse] as any[];
      
      if (exercises && exercises.length > 0) {
        exercises.forEach(exercise => {
          const category = name;
          const movementType = exercise.movement_type || 'Other';
          
          if (!categoryMap.has(category)) {
            categoryMap.set(category, new Map());
          }
          
          const movementMap = categoryMap.get(category)!;
          if (!movementMap.has(movementType)) {
            movementMap.set(movementType, []);
          }
          
          movementMap.get(movementType)!.push(exercise.name);
        });
      }
    });

    // Convert to sunburst data structure
    const children = Array.from(categoryMap.entries()).map(([category, movementMap]) => ({
      name: capitalizeEachWord(category),
      children: Array.from(movementMap.entries()).map(([movementType, exercises]) => ({
        name: capitalizeEachWord(movementType),
        loc: exercises.length,
        children: exercises.map(exerciseName => ({
          name: capitalizeEachWord(exerciseName),
          loc: 1,
        })),
      })),
    }));

    return {
      name: 'Exercise Pool',
      children,
    };
  }, [exercisePoolData]);

  // Filter data based on selection
  const filteredData = useMemo(() => {
    if (selectedItems.length === 0) {
      return sunburstData;
    }
    
    // This is a simplified filter - in a real implementation you'd want more sophisticated filtering
    return sunburstData;
  }, [sunburstData, selectedItems]);

  // Don't render if no data
  if (!filteredData.children?.length || !exercisePoolData) {
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
          <ResponsiveSunburst
            data={filteredData}
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
          />
        </Box>
      </CardContent>
    </Card>
  );
};
