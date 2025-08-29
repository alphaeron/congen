import { default as TrendingUpIcon } from '@mui/icons-material/TrendingUp';
import {
  Box,
  Card,
  CardContent,
  Typography,
  useTheme,
} from '@mui/material';
import { ResponsiveChord } from '@nivo/chord';
import React from 'react';

import { createCongenNivoTheme } from '../theme/nivoTheme';

interface ChordChartProps {
  matrix: number[][];
  keys: string[];
  title?: string;
  description?: string;
  height?: number;
}

/**
 * Chord Chart component for displaying exercise correlations.
 *
 * @param matrix The correlation matrix data
 * @param keys The exercise names
 * @param title Optional title for the chart
 * @param description Optional description for the chart
 * @param height Optional height for the chart container
 * @return Chord Chart component
 */
export const ChordChart: React.FC<ChordChartProps> = ({ 
  matrix, 
  keys,
  title = "Exercise Correlations",
  description = "Exercise pairing patterns in your workouts",
  height = 400
}) => {
  const theme = useTheme();
  const nivoTheme = createCongenNivoTheme(theme.palette.mode);

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
            data={matrix}
            keys={keys}
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
            theme={nivoTheme}
          />
        </Box>
      </CardContent>
    </Card>
  );
};
