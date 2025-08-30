import { Box, useTheme } from '@mui/material';
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
}

/**
 * Sunburst Chart component for displaying exercise volume hierarchy.
 *
 * @param data The chart data in sunburst format
 * @return Sunburst Chart component
 */
export const SunburstChart: React.FC<SunburstChartProps> = ({ data }) => {
  const theme = useTheme();
  const nivoTheme = createCongenNivoTheme(theme.palette.mode);

  return (
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
  );
};
