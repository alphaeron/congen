import { Box, useTheme } from '@mui/material';
import { ResponsiveLine } from '@nivo/line';
import React, { useMemo } from 'react';

import type { UserPerformanceMetrics } from '../api/types';
import { createCongenNivoTheme, congenColorSchemes } from '../theme/nivoTheme';

interface MetricTrendChartProps {
  metricKey: string;
  metricLabel: string;
  metricUnit: string;
  historicalData: UserPerformanceMetrics[];
  isLoading?: boolean;
  height?: number;
}

/**
 * Metric Trend Chart component for displaying 30-day trend data.
 *
 * This component shows historical data for a specific metric over the past 30 days.
 *
 * @param metricKey The key identifier for the metric
 * @param metricLabel The display label for the metric
 * @param metricUnit The unit of measurement for the metric
 * @param historicalData Array of historical performance metrics data
 * @param isLoading Whether the data is currently loading
 * @param height The height of the chart
 */
export const MetricTrendChart: React.FC<MetricTrendChartProps> = ({
  metricKey,
  metricLabel,
  metricUnit,
  historicalData,
  isLoading = false,
  height = 200,
}) => {
  const theme = useTheme();
  const nivoTheme = createCongenNivoTheme(theme.palette.mode);

  // Get metric value from UserPerformanceMetrics object
  const getMetricValue = (metrics: UserPerformanceMetrics, key: string): number | null => {
    switch (key) {
      case 'strain':
        return metrics.strain || null;
      case 'recovery':
        return metrics.recovery || null;
      case 'hrv':
        return metrics.hrv || null;
      case 'sleep_score':
        return metrics.sleep_score || null;
      case 'rem_sleep_minutes':
        return metrics.rem_sleep_minutes || null;
      case 'deep_sleep_minutes':
        return metrics.deep_sleep_minutes || null;
      case 'subjective_tiredness':
        return metrics.subjective_tiredness || null;
      default:
        return null;
    }
  };

  // Process chart data from real metrics
  const chartData = useMemo(() => {
    if (isLoading || historicalData.length === 0) {
      // Show empty state or loading
      return [
        {
          id: metricLabel,
          data: [],
        },
      ];
    }

    const data = historicalData
      .map((metrics: UserPerformanceMetrics) => {
        const value = getMetricValue(metrics, metricKey);
        if (value === null || value === undefined) return null;

        return {
          x: new Date(metrics.created_at).toISOString().split('T')[0], // YYYY-MM-DD format
          y: Math.round(value * 10) / 10, // Round to 1 decimal place
        };
      })
      .filter((point): point is { x: string; y: number } => point !== null)
      .sort((a, b) => new Date(a.x).getTime() - new Date(b.x).getTime());

    return [
      {
        id: metricLabel,
        data: data,
      },
    ];
  }, [historicalData, metricKey, metricLabel, isLoading, getMetricValue]);

  // Format tooltip value based on metric type
  const formatTooltipValue = (value: number): string => {
    if (metricKey === 'subjective_tiredness') {
      return value.toFixed(1);
    }
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
        <Box sx={{ color: '#00bcd4', fontStyle: 'italic' }}>Loading 30-day trend data...</Box>
      </Box>
    );
  }

  return (
    <Box sx={{ height, width: '100%' }}>
      <ResponsiveLine
        data={chartData}
        margin={{ top: 20, right: 20, bottom: 50, left: 60 }}
        xScale={{ type: 'time', format: '%Y-%m-%d', useUTC: false }}
        xFormat="time:%Y-%m-%d"
        yScale={{ type: 'linear', min: 'auto', max: 'auto' }}
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
        gridXValues={7} // Show grid lines for each week
        gridYValues={5} // Show 5 horizontal grid lines
        animate={true}
        motionConfig="gentle"
      />
    </Box>
  );
};
