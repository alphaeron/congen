import { default as BarChartIcon } from '@mui/icons-material/BarChart';
import { Box, Card, CardContent, Typography, useTheme } from '@mui/material';
import { ResponsiveLine } from '@nivo/line';
import React, { useState, useMemo } from 'react';

import { createCongenNivoTheme, congenColorSchemes } from '../theme/nivoTheme';

interface LineData {
  id: string;
  data: Array<{
    x: string;
    y: number;
  }>;
}

interface LineChartProps {
  data: LineData[];
  title?: string;
  description?: string;
  xAxisLabel?: string;
  yAxisLabel?: string;
  height?: number;
  showLegend?: boolean;
  colors?: string[];
}

/**
 * Line Chart component for displaying progression data.
 *
 * @param data The chart data in line format
 * @param title Optional title for the chart
 * @param description Optional description for the chart
 * @param xAxisLabel Optional x-axis label
 * @param yAxisLabel Optional y-axis label
 * @param height Optional height for the chart container
 * @param showLegend Whether to show the legend
 * @param colors Optional color scheme
 * @return Line Chart component
 */
export const LineChart: React.FC<LineChartProps> = ({
  data,
  title = 'Volume Progression',
  description = 'Total weight lifted over time (including band resistance)',
  xAxisLabel = 'Workout Date',
  yAxisLabel = 'Volume (lbs)',
  height = 300,
  showLegend = true,
  colors,
}) => {
  const theme = useTheme();
  const nivoTheme = createCongenNivoTheme(theme.palette.mode);
  const [selectedItems, setSelectedItems] = useState<string[]>([]);

  // Filter data based on legend selection
  const filteredData = useMemo(() => {
    if (selectedItems.length === 0) {
      return data;
    }
    return data.filter(item => selectedItems.includes(item.id));
  }, [data, selectedItems]);

  const handleLegendClick = (data: { id?: string; label?: string }) => {
    const itemId = data.id || data.label;
    setSelectedItems(prev => {
      if (prev.includes(itemId)) {
        return prev.filter(id => id !== itemId);
      } else {
        return [...prev, itemId];
      }
    });
  };

  return (
    <Card variant="outlined">
      <CardContent>
        <Box display="flex" alignItems="center" gap={1} sx={{ mb: 2 }}>
          <BarChartIcon color="primary" />
          <Typography variant="h6">{title}</Typography>
        </Box>
        <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
          {description}
        </Typography>
        <Box sx={{ height }}>
          <ResponsiveLine
            data={filteredData}
            margin={{ top: 20, right: 20, bottom: 50, left: 60 }}
            xScale={{ type: 'point' }}
            yScale={{ type: 'linear', min: 'auto', max: 'auto' }}
            axisTop={null}
            axisRight={null}
            axisBottom={{
              tickSize: 5,
              tickPadding: 5,
              tickRotation: -45,
              legend: xAxisLabel,
              legendOffset: 40,
              legendPosition: 'middle',
            }}
            axisLeft={{
              tickSize: 5,
              tickPadding: 5,
              tickRotation: 0,
              legend: yAxisLabel,
              legendOffset: -50,
              legendPosition: 'middle',
            }}
            pointSize={8}
            pointColor={{ theme: 'background' }}
            pointBorderWidth={2}
            pointBorderColor={{ from: 'serieColor' }}
            pointLabelYOffset={-12}
            useMesh={true}
            colors={colors || congenColorSchemes.strength}
            theme={nivoTheme}
            legends={
              showLegend
                ? [
                    {
                      anchor: 'top',
                      direction: 'row',
                      justify: false,
                      translateX: 0,
                      translateY: -20,
                      itemsSpacing: 0,
                      itemDirection: 'left-to-right',
                      itemWidth: 80,
                      itemHeight: 20,
                      itemTextColor: '#333333',
                      itemOpacity: 1,
                      symbolSize: 12,
                      symbolShape: 'circle',
                      symbolBorderColor: 'rgba(0, 0, 0, .5)',
                      onClick: handleLegendClick,
                      effects: [
                        {
                          on: 'hover',
                          style: {
                            itemBackground: 'rgba(0, 0, 0, .03)',
                            itemOpacity: 1,
                            itemTextColor: '#000',
                          },
                        },
                      ],
                    },
                  ]
                : []
            }
          />
        </Box>
      </CardContent>
    </Card>
  );
};
