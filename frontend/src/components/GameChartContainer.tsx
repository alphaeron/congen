import React from 'react';
import { Box } from '@mui/material';
import { styled, alpha } from '@mui/material/styles';

/**
 * Shared chart container for consistent styling across game charts.
 * Uses Congen's design system for consistent theming.
 */
const ChartContainer = styled(Box)(({ theme }) => ({
  height: '100%',
  '& .nivo-radar': {
    '& text': {
      fill: 'white !important',
      fontSize: '12px',
      fontWeight: 600, // Matches Congen's font weight
      fontFamily: '"Inter", "system-ui", "sans-serif"', // Matches Congen's font
    },
    '& .nivo-radar-grid': {
      stroke: alpha('#ffffff', 0.3), // Using Congen's alpha approach
    },
    '& .nivo-radar-axes': {
      stroke: alpha('#ffffff', 0.5), // Using Congen's alpha approach
    },
    '& .nivo-radar-dots': {
      fill: 'white',
      stroke: alpha('#ffffff', 0.8), // Using Congen's alpha approach
      strokeWidth: 2,
    },
  },
}));

interface GameChartContainerProps {
  children: React.ReactNode;
  height?: number;
}

/**
 * Container for game-themed charts with consistent styling
 */
export const GameChartContainer: React.FC<GameChartContainerProps> = ({ 
  children, 
  height = 400 
}) => {
  return (
    <ChartContainer sx={{ height }}>
      {children}
    </ChartContainer>
  );
};
