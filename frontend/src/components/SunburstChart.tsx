import { Box, useTheme, Typography, IconButton } from '@mui/material';
import { ResponsiveSunburst } from '@nivo/sunburst';
import React, { useState } from 'react';
import { default as HomeIcon } from '@mui/icons-material/Home';

import { createCongenNivoTheme } from '../theme/nivoTheme';

// Custom layer to display total volume in the center
const CenterMetric = (props: any) => {
  const theme = useTheme();
  const { centerX, centerY, data } = props;
  
  // Calculate total volume from current data
  const calculateTotalVolume = (node: any): number => {
    if (!node) return 0;
    if (node.children && node.children.length > 0) {
      return node.children.reduce((sum: number, child: any) => sum + calculateTotalVolume(child), 0);
    }
    return node.loc || 0;
  };
  
  const totalVolume = calculateTotalVolume(data);
  
  console.log('CenterMetric - data:', data);
  console.log('CenterMetric - totalVolume:', totalVolume);
  
  return (
    <g transform={`translate(${centerX},${centerY})`}>
      <circle
        r={40}
        fill={theme.palette.background.paper}
        stroke={theme.palette.divider}
        strokeWidth={1}
      />
      <text
        textAnchor="middle"
        dominantBaseline="central"
        style={{
          fontSize: '12px',
          fontWeight: 'bold',
          fill: theme.palette.text.primary,
        }}
        y={-8}
      >
        Total
      </text>
      <text
        textAnchor="middle"
        dominantBaseline="central"
        style={{
          fontSize: '16px',
          fontWeight: 'bold',
          fill: theme.palette.primary.main,
        }}
        y={8}
      >
        {totalVolume.toLocaleString()}
      </text>
    </g>
  );
};

interface SunburstData {
  name: string;
  loc?: number;
  children?: SunburstData[];
}

interface SunburstChartProps {
  data: SunburstData;
}

/**
 * Sunburst Chart component for displaying exercise volume hierarchy with drill-down functionality.
 *
 * @param data The chart data in sunburst format
 * @return Sunburst Chart component
 */
export const SunburstChart: React.FC<SunburstChartProps> = ({ data }) => {
  const theme = useTheme();
  const nivoTheme = createCongenNivoTheme(theme.palette.mode);
  const [currentData, setCurrentData] = useState<SunburstData>(data);

  // Handle click on sunburst segments
  const handleArcClick = (node: any) => {
    console.log('Arc clicked:', node);
    if (node.data?.children && node.data.children.length > 0) {
      setCurrentData(node.data);
    }
  };

  // Handle home button click
  const handleHomeClick = () => {
    setCurrentData(data);
  };

  return (
    <Box>
      {/* Navigation */}
      <Box sx={{ mb: 2, display: 'flex', alignItems: 'center', gap: 1 }}>
        <IconButton 
          onClick={handleHomeClick}
          size="small"
          title="Back to root"
        >
          <HomeIcon />
        </IconButton>
        <Typography variant="body2" color="text.secondary">
          {currentData.name === data.name ? 'Exercise Volume' : currentData.name}
        </Typography>
      </Box>

      {/* Sunburst Chart */}
      <Box sx={{ height: 300 }}>
        <ResponsiveSunburst
          data={currentData}
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
          onClick={handleArcClick}
          layers={['arcs', 'arcLabels', (props: any) => <CenterMetric {...props} data={currentData} />]}
        />
      </Box>
    </Box>
  );
};
