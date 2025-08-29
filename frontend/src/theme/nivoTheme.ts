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
        stroke: gray[600],
        strokeWidth: 2,
      },
    },
    ticks: {
      line: {
        stroke: gray[600],
        strokeWidth: 1,
      },
      text: {
        fontSize: 11,
        fill: gray[100],
        outlineWidth: 0,
        outlineColor: 'transparent',
        fontWeight: 600,
      },
    },
    legend: {
      text: {
        fontSize: 12,
        fill: gray[100],
        outlineWidth: 0,
        outlineColor: 'transparent',
        fontWeight: 700,
      },
    },
  },
  
  // Grid styling
  grid: {
    line: {
      stroke: gray[300],
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
      maxWidth: 200,
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
  // Strength-focused color scheme
  strength: [
    brand[500],    // Primary brand color
    secondary[500], // Secondary color
    green[500],    // Success green
    warning[500],  // Warning orange
    gray[600],     // Neutral gray
    brand[300],    // Light brand
    secondary[300], // Light secondary
    green[300],    // Light green
  ],
  
  // Hierarchical color scheme for nested structures
  hierarchical: [
    brand[600],    // Dark primary for root
    brand[500],    // Primary for level 1
    brand[400],    // Medium for level 2
    brand[300],    // Light for level 3
    secondary[600], // Dark secondary for variety
    secondary[500], // Secondary for level 1
    secondary[400], // Medium secondary for level 2
    secondary[300], // Light secondary for level 3
  ],
  
  // Exercise category colors
  exercise: {
    compound: brand[500],     // Compound lifts
    accessory: secondary[500], // Accessory work
    cardio: green[500],       // Cardio
    mobility: warning[500],   // Mobility/flexibility
    other: gray[500],         // Other exercises
  },
};

/**
 * Congen-specific gradients for Nivo charts
 */
export const congenGradients = [
  {
    id: 'brand',
    type: 'linearGradient',
    colors: [
      { offset: 0, color: brand[300] },
      { offset: 50, color: brand[500] },
      { offset: 100, color: brand[700] },
    ],
  },
  {
    id: 'secondary',
    type: 'linearGradient',
    colors: [
      { offset: 0, color: secondary[300] },
      { offset: 50, color: secondary[500] },
      { offset: 100, color: secondary[700] },
    ],
  },
  {
    id: 'success',
    type: 'linearGradient',
    colors: [
      { offset: 0, color: green[300] },
      { offset: 50, color: green[500] },
      { offset: 100, color: green[700] },
    ],
  },
  {
    id: 'neutral',
    type: 'linearGradient',
    colors: [
      { offset: 0, color: gray[200] },
      { offset: 50, color: gray[400] },
      { offset: 100, color: gray[600] },
    ],
  },
];

/**
 * Common legend configuration for Congen charts with multiselection support
 * Based on Nivo legends guide: https://nivo.rocks/guides/legends/
 */
export const congenLegendConfig = {
  // Standard legend configuration with clickable legends and multiselection
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
  
  // Horizontal legend configuration with clickable legends and multiselection
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
 * Common axis configuration for Congen charts with text wrapping
 * Based on Nivo axes guide: https://nivo.rocks/guides/axes/
 */
export const congenAxisConfig = {
  // Standard bottom axis with text wrapping
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

/**
 * Helper function to create hierarchical color schemes
 * @param depth The depth of the hierarchy
 * @param baseColor The base color to start with
 * @returns Array of colors for each level
 */
export const createHierarchicalColors = (depth: number, baseColor: string = brand[500]) => {
  const colors = [];
  for (let i = 0; i < depth; i++) {
    const opacity = 1 - (i * 0.2);
    colors.push(`${baseColor}${Math.round(opacity * 255).toString(16).padStart(2, '0')}`);
  }
  return colors;
};

/**
 * Helper function to truncate long text with ellipsis
 * @param text The text to truncate
 * @param maxLength The maximum length before truncation
 * @returns Truncated text
 */
export const truncateText = (text: string, maxLength: number = 20) => {
  if (text.length <= maxLength) return text;
  return text.substring(0, maxLength) + '...';
};
