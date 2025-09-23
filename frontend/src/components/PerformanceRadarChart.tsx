import React from 'react';
import {
  Box,
  CardContent,
  useTheme,
  Tooltip,
} from '@mui/material';
import { ResponsiveRadar } from '@nivo/radar';
import { Radar as RadarIcon } from '@mui/icons-material';
import type { UserPerformanceScores, UserPerformanceMetrics } from '../api/types';
import { createCongenNivoTheme } from '../theme/nivoTheme';
import { GameCard, GameText } from './GameTheme';
import { GameChartContainer } from './GameChartContainer';

interface PerformanceRadarChartProps {
  scores: UserPerformanceScores;
  metrics?: UserPerformanceMetrics | null;
  title?: string;
  height?: number;
}

const getMetricData = (scores: UserPerformanceScores, metrics?: UserPerformanceMetrics | null) => {
  const data = [
    {
      metric: 'Power',
      value: scores.explosiveness_score || 0,
      description: 'Explosive power based on vertical jump height',
      rawValue: metrics?.vo2_max ? `${metrics.vo2_max.toFixed(1)} ml/kg/min` : 'N/A',
      color: '#4ECDC4',
    },
    {
      metric: 'Endurance',
      value: scores.aerobic_capacity_score || 0,
      description: 'Aerobic capacity based on VO₂ max',
      rawValue: metrics?.vo2_max ? `${metrics.vo2_max.toFixed(1)} ml/kg/min` : 'N/A',
      color: '#45B7D1',
    },
    {
      metric: 'Recovery',
      value: scores.recovery_score || 0,
      description: 'Recovery ability based on HR recovery',
      rawValue: metrics?.recovery ? `${metrics.recovery.toFixed(0)}%` : 'N/A',
      color: '#96CEB4',
    },
    {
      metric: 'Speed',
      value: scores.reaction_time_score || 0,
      description: 'Reaction speed based on response time',
      rawValue: metrics?.hrv ? `${metrics.hrv.toFixed(0)} ms` : 'N/A',
      color: '#DDA0DD',
    },
  ];

  return data; // Show all metrics even if 0
};

export const PerformanceRadarChart: React.FC<PerformanceRadarChartProps> = ({
  scores,
  metrics,
  title = 'Performance Metrics',
  height = 400,
}) => {
  const theme = useTheme();
  const nivoTheme = createCongenNivoTheme(theme.palette.mode);
  
  const metricData = getMetricData(scores, metrics);

  // Custom tooltip component
  const CustomTooltip = ({ data }: any) => {
    const metric = metricData.find(m => m.metric === data.indexBy);
    if (!metric) return null;

    return (
      <Box
        sx={{
          background: 'rgba(0, 0, 0, 0.9)',
          color: 'white',
          borderRadius: 2,
          padding: 2,
          border: '1px solid rgba(255, 255, 255, 0.2)',
          minWidth: 200,
        }}
      >
        <Box sx={{ fontWeight: 'bold', fontSize: '0.9rem', mb: 1 }}>
          {metric.metric}
        </Box>
        <Box sx={{ fontSize: '0.8rem', mb: 0.5 }}>
          <strong>Score:</strong> {data.value.toFixed(1)}/100
        </Box>
        <Box sx={{ fontSize: '0.8rem', mb: 0.5 }}>
          <strong>Raw Value:</strong> {metric.rawValue}
        </Box>
        <Box sx={{ fontSize: '0.75rem', opacity: 0.8, fontStyle: 'italic' }}>
          {metric.description}
        </Box>
      </Box>
    );
  };

  // Don't render if no data
  if (metricData.length === 0) {
    return (
      <GameCard>
        <CardContent>
          <Box display="flex" alignItems="center" gap={1} sx={{ mb: 2 }}>
            <RadarIcon sx={{ color: 'white' }} />
            <GameText variant="h6">
              {title}
            </GameText>
          </Box>
          <Box
            sx={{
              height,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              backgroundColor: 'rgba(255, 255, 255, 0.1)',
              borderRadius: 2,
            }}
          >
            <GameText variant="body1" sx={{ opacity: 0.7 }}>
              No performance data available. Complete some tests to see your metrics!
            </GameText>
          </Box>
        </CardContent>
      </GameCard>
    );
  }

  return (
    <GameCard>
      <CardContent>
        <Box display="flex" alignItems="center" gap={1} sx={{ mb: 2 }}>
          <RadarIcon sx={{ color: 'white' }} />
          <Tooltip title="Your performance across different fitness domains" arrow>
            <GameText variant="h6" sx={{ fontWeight: 'bold' }}>
              {title}
            </GameText>
          </Tooltip>
        </Box>
        
        <GameChartContainer height={height}>
          <ResponsiveRadar
            data={metricData}
            keys={['value']}
            indexBy="metric"
            valueFormat=".1f"
            margin={{ top: 80, right: 80, bottom: 80, left: 80 }}
            borderColor={{ from: 'color' }}
            gridLabelOffset={36}
            dotSize={12}
            dotColor={{ theme: 'background' }}
            dotBorderWidth={3}
            colors={metricData.map(item => item.color)}
            blendMode="normal"
            motionConfig="gentle"
            sliceTooltip={CustomTooltip}
            theme={{
              ...nivoTheme,
              background: 'transparent',
              text: {
                fill: 'white',
                fontSize: 12,
                fontWeight: 'bold',
              },
              grid: {
                line: {
                  stroke: 'rgba(255, 255, 255, 0.3)',
                  strokeWidth: 1,
                },
              },
              axis: {
                domain: {
                  line: {
                    stroke: 'rgba(255, 255, 255, 0.5)',
                    strokeWidth: 1,
                  },
                },
                ticks: {
                  line: {
                    stroke: 'rgba(255, 255, 255, 0.5)',
                    strokeWidth: 1,
                  },
                  text: {
                    fill: 'white',
                    fontSize: 11,
                    fontWeight: 'bold',
                  },
                },
              },
              dots: {
                text: {
                  fill: 'white',
                  fontSize: 10,
                  fontWeight: 'bold',
                },
              },
              tooltip: {
                container: {
                  background: 'rgba(0, 0, 0, 0.8)',
                  color: 'white',
                  borderRadius: 8,
                  padding: 12,
                  fontSize: 12,
                },
              },
            }}
            legends={[
              {
                anchor: 'top-left',
                direction: 'column',
                translateX: -50,
                translateY: -40,
                itemWidth: 80,
                itemHeight: 20,
                itemTextColor: 'white',
                symbolSize: 12,
                symbolShape: 'circle',
                effects: [
                  {
                    on: 'hover',
                    style: {
                      itemTextColor: '#FFD700',
                    },
                  },
                ],
              },
            ]}
            maxValue={100}
            curve="linearClosed"
            fillOpacity={0.1}
            animate={true}
          />
        </GameChartContainer>
        
      </CardContent>
    </GameCard>
  );
};
