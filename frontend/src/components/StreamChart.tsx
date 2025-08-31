import { default as ShowChartIcon } from '@mui/icons-material/ShowChart';
import { Box, Card, CardContent, Typography, useTheme } from '@mui/material';
import { ResponsiveStream } from '@nivo/stream';
import React from 'react';

import { createCongenNivoTheme } from '../theme/nivoTheme';

interface StreamData {
  date: string;
  [key: string]: string | number;
}

interface StreamChartProps {
  data: StreamData[];
  keys: string[];
  title?: string;
  description?: string;
  height?: number;
}

/**
 * Stream Chart component for displaying volume flow over time.
 *
 * @param data The chart data in stream format
 * @param keys The data keys to display
 * @param title Optional title for the chart
 * @param description Optional description for the chart
 * @param height Optional height for the chart container
 * @return Stream Chart component
 */
export const StreamChart: React.FC<StreamChartProps> = ({
  data,
  keys,
  title = 'Volume Flow Over Time',
  description = 'Training volume distribution across workout types',
  height = 400,
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
        <Box sx={{ height }}>
          <ResponsiveStream
            data={data}
            keys={keys}
            margin={{ top: 50, right: 110, bottom: 50, left: 60 }}
            colors={{ scheme: 'nivo' }}
            theme={nivoTheme}
          />
        </Box>
      </CardContent>
    </Card>
  );
};
