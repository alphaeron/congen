import { default as ShowChartIcon } from '@mui/icons-material/ShowChart';
import {
  Box,
  Card,
  CardContent,
  Typography,
  useTheme,
} from '@mui/material';
import { ResponsiveSunburst } from '@nivo/sunburst';
import React from 'react';

import { createCongenNivoTheme } from '../theme/nivoTheme';

interface SunburstData {
  name: string;
  loc: number;
  children?: SunburstData[];
}

interface SunburstChartProps {
  data: SunburstData;
  title?: string;
  description?: string;
}

/**
 * Sunburst Chart component for displaying exercise volume hierarchy.
 *
 * @param data The chart data in sunburst format
 * @param title Optional title for the chart
 * @param description Optional description for the chart
 * @return Sunburst Chart component
 */
export const SunburstChart: React.FC<SunburstChartProps> = ({ 
  data, 
  title = "Exercise Volume Hierarchy",
  description = "Volume distribution by muscle groups and exercises"
}) => {
  const theme = useTheme();
  const nivoTheme = createCongenNivoTheme(theme.palette.mode);

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
        <Box sx={{ height: 300 }}>
          <ResponsiveSunburst
            data={data}
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
