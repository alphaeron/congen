import { Box, useTheme } from '@mui/material';
import { ResponsiveLine } from '@nivo/line';
import React, { useMemo } from 'react';

import { getDateInBrowserTimezone } from '../common/utils';
import { createCongenNivoTheme, congenColorSchemes } from '../theme/nivoTheme';

interface DataPoint {
  date: Date;
  value: number;
}

interface MetricTrendChartProps {
  metricLabel: string;
  metricUnit: string;
  data?: DataPoint[];
  isLoading?: boolean;
  height?: number;
}

/**
 * Metric Trend Chart component for displaying trend data.
 *
 * This component renders a simple line chart from the provided data points.
 *
 * @param metricLabel The display label for the metric
 * @param metricUnit The unit of measurement for the metric
 * @param data Array of data points with date and value
 * @param isLoading Whether the data is currently loading
 * @param height The height of the chart
 */
export const MetricTrendChart: React.FC<MetricTrendChartProps> = ({
  metricLabel,
  metricUnit,
  data,
  isLoading = false,
  height = 200,
}) => {
  const theme = useTheme();
  const nivoTheme = createCongenNivoTheme(theme.palette.mode);

  // Process chart data
  const chartData = useMemo(() => {
    if (isLoading || !data || data.length === 0) {
      return [
        {
          id: metricLabel,
          data: [],
        },
      ];
    }

    const processedData = data
      .map(point => ({
        x: getDateInBrowserTimezone(point.date),
        y: Math.round(point.value * 10) / 10,
      }))
      .sort((a, b) => new Date(a.x).getTime() - new Date(b.x).getTime());

    return [
      {
        id: metricLabel,
        data: processedData,
      },
    ];
  }, [data, metricLabel, isLoading]);

  // Format tooltip value
  const formatTooltipValue = (value: number): string => {
    return value.toFixed(1);
  };

  // Handle loading state
  if (isLoading) {
    return (
      <Box
        sx={{
          height,
          width: '100%',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
        }}
      >
        <Box sx={{ color: '#00bcd4', fontStyle: 'italic' }}>Loading trend data...</Box>
      </Box>
    );
  }

  // Handle empty data state
  if (chartData[0].data.length === 0) {
    return (
      <Box
        sx={{
          height,
          width: '100%',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
        }}
      >
        <Box sx={{ color: '#666', fontStyle: 'italic' }}>
          No {metricLabel.toLowerCase()} data available
        </Box>
      </Box>
    );
  }

  return (
    <Box sx={{ height, width: '100%', overflow: 'visible' }}>
      <ResponsiveLine
        data={chartData}
        margin={{ top: 20, right: 40, bottom: 50, left: 60 }}
        xScale={{ type: 'time', format: '%Y-%m-%d', useUTC: false }}
        xFormat="time:%Y-%m-%d"
        yScale={{
          type: 'linear',
          min: 'auto',
          max: 'auto',
          stacked: false,
          reverse: false,
        }}
        axisTop={null}
        axisRight={null}
        axisBottom={{
          tickSize: 5,
          tickPadding: 5,
          tickRotation: -45,
          format: '%m/%d',
          legend: 'Date',
          legendOffset: 40,
          legendPosition: 'middle',
        }}
        axisLeft={{
          tickSize: 5,
          tickPadding: 5,
          tickRotation: 0,
          legend: metricUnit,
          legendOffset: -50,
          legendPosition: 'middle',
        }}
        pointSize={6}
        pointColor={{ theme: 'background' }}
        pointBorderWidth={2}
        pointBorderColor={{ from: 'serieColor' }}
        pointLabelYOffset={-12}
        useMesh={true}
        colors={[congenColorSchemes.strength[0]]} // Use primary brand color
        theme={nivoTheme}
        tooltip={({ point }) => (
          <div
            style={{
              padding: '8px 12px',
              color: nivoTheme.tooltip.container.color,
              background: nivoTheme.tooltip.container.background,
              borderRadius: nivoTheme.tooltip.container.borderRadius,
              boxShadow: nivoTheme.tooltip.container.boxShadow,
              border: nivoTheme.tooltip.container.border,
              whiteSpace: 'nowrap',
              fontSize: nivoTheme.tooltip.container.fontSize,
              fontFamily: '"Inter", "Roboto", "Helvetica", "Arial", sans-serif',
              lineHeight: '1.4',
              display: 'flex',
              alignItems: 'center',
              gap: '8px',
            }}
          >
            <div
              style={{
                width: '12px',
                height: '12px',
                backgroundColor: point.seriesColor,
                borderRadius: '2px',
                flexShrink: 0,
              }}
            />
            <div>
              <div>
                Date: <span style={{ fontWeight: 'bold' }}>{point.data.xFormatted}</span>
              </div>
              <div>
                {metricLabel}:{' '}
                <span style={{ fontWeight: 'bold' }}>
                  {formatTooltipValue(point.data.y as number)} {metricUnit}
                </span>
              </div>
            </div>
          </div>
        )}
        enableGridX={true}
        enableGridY={true}
        gridXValues={chartData[0].data.length > 7 ? 7 : undefined} // Show grid lines for each week if enough data
        gridYValues={5} // Show 5 horizontal grid lines
        animate={true}
        motionConfig="gentle"
      />
    </Box>
  );
};
