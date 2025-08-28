import { brand, secondary, gray, green, warning } from '../theme';

/**
 * Comprehensive Nivo theme that matches Congen's design system
 * Based on Nivo theming guide: https://nivo.rocks/guides/theming/
 */
export const congenNivoTheme = {
  // Typography - matches Congen's font system
  fontSize: 12,
  fontFamily: '"Inter", "Roboto", "Helvetica", "Arial", sans-serif',
  
  // Colors - using Congen's brand colors with better contrast
  text: {
    fontSize: 12,
    fill: gray[800],
    outlineWidth: 0,
    outlineColor: 'transparent',
  },
  
  // Axes styling - clean and modern with better contrast
  axis: {
    domain: {
      line: {
        stroke: gray[400],
        strokeWidth: 1,
      },
    },
    ticks: {
      line: {
        stroke: gray[400],
        strokeWidth: 1,
      },
      text: {
        fontSize: 11,
        fill: gray[800],
        outlineWidth: 0,
        outlineColor: 'transparent',
        fontWeight: 500,
      },
    },
    legend: {
      text: {
        fontSize: 12,
        fill: gray[900],
        outlineWidth: 0,
        outlineColor: 'transparent',
        fontWeight: 600,
      },
    },
  },
  
  // Grid styling
  grid: {
    line: {
      stroke: gray[200],
      strokeWidth: 1,
      strokeDasharray: '4 4',
    },
  },
  
  // Legends - clean and readable with better contrast
  legends: {
    text: {
      fontSize: 11,
      fill: gray[800],
      outlineWidth: 0,
      outlineColor: 'transparent',
      fontFamily: '"Inter", "Roboto", "Helvetica", "Arial", sans-serif',
      outlineOpacity: 0,
      fontWeight: 500,
    },
  },
  
  // Labels styling with better contrast
  labels: {
    text: {
      fontSize: 11,
      fill: gray[800],
      outlineWidth: 0,
      outlineColor: 'transparent',
      fontWeight: 500,
    },
  },
  
  // Markers styling
  markers: {
    lineColor: gray[300],
    lineStrokeWidth: 1,
    text: {
      fontSize: 11,
      fill: gray[600],
      outlineWidth: 0,
      outlineColor: 'transparent',
      fontFamily: '"Inter", "Roboto", "Helvetica", "Arial", sans-serif',
      outlineOpacity: 0,
    },
  },
  
  // Dots styling
  dots: {
    text: {
      fontSize: 11,
      fill: gray[600],
      outlineWidth: 0,
      outlineColor: 'transparent',
      fontFamily: '"Inter", "Roboto", "Helvetica", "Arial", sans-serif',
      outlineOpacity: 0,
    },
  },
  
  // Tooltip styling with better visibility
  tooltip: {
    container: {
      background: 'white',
      color: gray[900],
      fontSize: 12,
      borderRadius: 8,
      boxShadow: '0 4px 12px rgba(0, 0, 0, 0.15)',
      border: `1px solid ${gray[300]}`,
      padding: '8px 12px',
    },
  },
  
  // Crosshair styling
  crosshair: {
    line: {
      stroke: brand[500],
      strokeWidth: 1,
      strokeOpacity: 0.75,
    },
  },
};

/**
 * Congen-specific color schemes for Nivo charts
 * Based on Congen's brand colors
 */
export const congenColorSchemes = {
  // Primary scheme using Congen's brand colors
  congen: [
    brand[500],    // Primary blue
    secondary[500], // Orange
    green[500],     // Success green
    warning[500],   // Warning yellow
    brand[300],     // Light blue
    secondary[300], // Light orange
    green[300],     // Light green
    warning[300],   // Light yellow
  ],
  
  // Strength-focused scheme
  strength: [
    brand[600],     // Dark blue
    brand[500],     // Blue
    brand[400],     // Medium blue
    brand[300],     // Light blue
    secondary[600], // Dark orange
    secondary[500], // Orange
    secondary[400], // Medium orange
    secondary[300], // Light orange
  ],
  
  // Performance scheme
  performance: [
    green[600],     // Dark green
    green[500],     // Green
    brand[500],     // Blue
    secondary[500], // Orange
    warning[500],   // Yellow
    brand[300],     // Light blue
    green[300],     // Light green
    secondary[300], // Light orange
  ],
};

/**
 * Congen gradient definitions for enhanced visual appeal
 * Based on Nivo gradients guide: https://nivo.rocks/guides/gradients/
 */
export const congenGradients = [
  // Primary gradient - blue to light blue
  {
    id: 'congenPrimary',
    type: 'linearGradient',
    colors: [
      { offset: 0, color: brand[600] },
      { offset: 50, color: brand[500] },
      { offset: 100, color: brand[300] },
    ],
  },
  
  // Secondary gradient - orange to light orange
  {
    id: 'congenSecondary',
    type: 'linearGradient',
    colors: [
      { offset: 0, color: secondary[600] },
      { offset: 50, color: secondary[500] },
      { offset: 100, color: secondary[300] },
    ],
  },
  
  // Success gradient - green to light green
  {
    id: 'congenSuccess',
    type: 'linearGradient',
    colors: [
      { offset: 0, color: green[600] },
      { offset: 50, color: green[500] },
      { offset: 100, color: green[300] },
    ],
  },
  
  // Warning gradient - yellow to light yellow
  {
    id: 'congenWarning',
    type: 'linearGradient',
    colors: [
      { offset: 0, color: warning[600] },
      { offset: 50, color: warning[500] },
      { offset: 100, color: warning[300] },
    ],
  },
  
  // Strength gradient - dark to light
  {
    id: 'congenStrength',
    type: 'linearGradient',
    colors: [
      { offset: 0, color: gray[800] },
      { offset: 50, color: gray[600] },
      { offset: 100, color: gray[400] },
    ],
  },
];

/**
 * Common legend configuration for Congen charts
 * Based on Nivo legends guide: https://nivo.rocks/guides/legends/
 */
export const congenLegendConfig = {
  // Standard legend configuration with clickable legends
  standard: {
    anchor: 'bottom-right' as const,
    direction: 'column' as const,
    justify: false,
    translateX: 100,
    translateY: 0,
    itemsSpacing: 0,
    itemDirection: 'left-to-right' as const,
    itemWidth: 80,
    itemHeight: 20,
    itemTextColor: gray[800],
    itemOpacity: 1,
    symbolSize: 12,
    symbolShape: 'circle' as const,
    symbolBorderColor: gray[400],
    symbolBorderWidth: 1,
    onClick: (data: any) => {
      // This will be overridden by individual chart components
      console.log('Legend clicked:', data);
    },
    effects: [
      {
        on: 'hover',
        style: {
          itemBackground: gray[100],
          itemOpacity: 1,
          itemTextColor: gray[900],
          cursor: 'pointer',
        },
      },
    ],
  },
  
  // Horizontal legend configuration with clickable legends
  horizontal: {
    anchor: 'bottom' as const,
    direction: 'row' as const,
    justify: false,
    translateX: 0,
    translateY: 56,
    itemsSpacing: 0,
    itemWidth: 100,
    itemHeight: 18,
    itemTextColor: gray[800],
    itemDirection: 'left-to-right' as const,
    itemOpacity: 1,
    symbolSize: 18,
    symbolShape: 'circle' as const,
    onClick: (data: any) => {
      // This will be overridden by individual chart components
      console.log('Legend clicked:', data);
    },
    effects: [
      {
        on: 'hover',
        style: {
          itemTextColor: gray[900],
          cursor: 'pointer',
        },
      },
    ],
  },
  
  // Compact legend configuration
  compact: {
    anchor: 'top-right' as const,
    direction: 'column' as const,
    justify: false,
    translateX: -20,
    translateY: 0,
    itemsSpacing: 2,
    itemDirection: 'left-to-right' as const,
    itemWidth: 60,
    itemHeight: 16,
    itemTextColor: gray[600],
    itemOpacity: 0.75,
    symbolSize: 10,
    symbolShape: 'circle' as const,
    symbolBorderColor: gray[300],
    symbolBorderWidth: 1,
    effects: [
      {
        on: 'hover',
        style: {
          itemBackground: gray[100],
          itemOpacity: 1,
          itemTextColor: gray[800],
        },
      },
    ],
  },
};

/**
 * Common axis configuration for Congen charts
 * Based on Nivo axes guide: https://nivo.rocks/guides/axes/
 */
export const congenAxisConfig = {
  // Standard bottom axis
  bottom: {
    tickSize: 5,
    tickPadding: 5,
    tickRotation: -45,
    legend: '',
    legendOffset: 40,
    legendPosition: 'middle' as const,
  },
  
  // Standard left axis
  left: {
    tickSize: 5,
    tickPadding: 5,
    tickRotation: 0,
    legend: '',
    legendOffset: -50,
    legendPosition: 'middle' as const,
  },
  
  // Standard top axis
  top: {
    tickSize: 5,
    tickPadding: 5,
    tickRotation: 0,
    legend: '',
    legendOffset: -40,
    legendPosition: 'middle' as const,
  },
  
  // Standard right axis
  right: {
    tickSize: 5,
    tickPadding: 5,
    tickRotation: 0,
    legend: '',
    legendOffset: 50,
    legendPosition: 'middle' as const,
  },
};
