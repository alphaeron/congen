import { Box, useTheme } from '@mui/material';
import { ResponsiveRadialBar } from '@nivo/radial-bar';
import React from 'react';

import { createCongenNivoTheme } from '../theme/nivoTheme';

interface RadialBarData {
  id: string;
  data: Array<{
    x: string;
    y: number;
  }>;
}

interface RadialBarChartProps {
  data: RadialBarData[];
}

/**
 * Radial Bar Chart component for displaying exercise performance metrics.
 *
 * @param data The chart data in radial bar format
 * @return Radial Bar Chart component
 */
export const RadialBarChart: React.FC<RadialBarChartProps> = ({ data }) => {
  const theme = useTheme();
  const nivoTheme = createCongenNivoTheme(theme.palette.mode);

  return (
    <Box sx={{ height: 300 }}>
      <ResponsiveRadialBar
        data={data}
        valueFormat=">-0"
        padding={0.4}
        cornerRadius={2}
        margin={{ top: 40, right: 120, bottom: 40, left: 40 }}
        radialAxisStart={{ tickSize: 5, tickPadding: 5, tickRotation: 0 }}
        circularAxisOuter={{ tickSize: 2, tickPadding: 2, tickRotation: 0 }}
        labelsSkipAngle={10}
        colors={{ scheme: 'nivo' }}
        theme={nivoTheme}
        enableLabels={true}
        labelsRadiusOffset={0.5}
        legends={[
          {
            anchor: 'top-left',
            direction: 'column',
            justify: false,
            translateX: -40,
            translateY: 0,
            itemsSpacing: 2,
            itemDirection: 'left-to-right',
            itemWidth: 80,
            itemHeight: 20,
            itemTextColor: '#999',
            symbolSize: 12,
            symbolShape: 'circle',
            onClick: () => {},
          },
        ]}
      />
    </Box>
  );
};
