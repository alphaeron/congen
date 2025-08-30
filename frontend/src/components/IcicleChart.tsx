import { Box, useTheme } from '@mui/material';
import { ResponsiveIcicle } from '@nivo/icicle';
import React from 'react';

import { createCongenNivoTheme } from '../theme/nivoTheme';

interface IcicleData {
  id: string;
  value?: number;
  children?: IcicleData[];
}

interface IcicleChartProps {
  data: IcicleData;
  height?: number;
}

/**
 * Icicle Chart component for displaying training structure analysis.
 *
 * @param data The chart data in icicle format
 * @param height Optional height for the chart container
 * @return Icicle Chart component
 */
export const IcicleChart: React.FC<IcicleChartProps> = ({ 
  data, 
  height = 300
}) => {
  const theme = useTheme();
  const nivoTheme = createCongenNivoTheme(theme.palette.mode);

  return (
    <Box sx={{ height }}>
      <ResponsiveIcicle
        data={data}
        margin={{ top: 10, right: 10, bottom: 10, left: 10 }}
        value="value"
        colors={{ scheme: 'nivo' }}
        theme={nivoTheme}
        enableLabels={true}
        labelTextColor={{
          from: 'color',
          modifiers: [['darker', 2]],
        }}
        labelBoxAnchor="top"
        labelPaddingX={6}
        labelPaddingY={6}
        labelAlign="end"
        labelBaseline="center"
        labelRotation={270}
        labelSkipWidth={16}
        labelSkipHeight={48}
        borderWidth={1}
        borderColor={{
          from: 'color',
          modifiers: [['darker', 0.2]],
        }}
        borderRadius={4}
      />
    </Box>
  );
};
