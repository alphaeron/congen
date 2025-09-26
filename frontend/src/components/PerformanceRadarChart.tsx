import { Radar as RadarIcon } from '@mui/icons-material';
import { Box, useTheme, Tooltip } from '@mui/material';
import { ResponsiveRadar } from '@nivo/radar';
import React from 'react';

import { CustomSvgIcon } from './CustomSvgIcon';
import { GameText } from './GameTheme';
import type { UserPerformanceScores, UserPerformanceMetrics, UserTestResult } from '../api/types';
import RecoveryIcon from '../resources/recovery-icon.svg';
import DexterityIcon from '../resources/dexterity-icon.svg';
import ExplosivenessIcon from '../resources/explosiveness-icon.svg';
import StrengthIcon from '../resources/strength-icon.svg';
import HealthIcon from '../resources/health-icon.svg';
import ReflexesIcon from '../resources/reflexes-icon.svg';
import { createCongenNivoTheme } from '../theme/nivoTheme';

interface PerformanceRadarChartProps {
  scores: UserPerformanceScores;
  metrics?: UserPerformanceMetrics | null;
  weeklyTests?: UserTestResult[] | null;
  wilksScore?: number | null;
  title?: string;
  height?: number;
}

const getMetricData = (
  scores: UserPerformanceScores,
  metrics?: UserPerformanceMetrics | null,
  weeklyTests?: UserTestResult[] | null,
  wilksScore?: number | null
) => {
  // Calculate strength score from Wilks score (scale 0-100)
  const strengthScore = wilksScore ? Math.max(1, Math.min(100, wilksScore / 5.0)) : null; // Rough scaling: 500 Wilks = 100 score

  const data = [
    {
      metric: 'Explosiveness',
      value: Math.max(1, Math.min(100, scores.explosiveness_score || 1)), // Use scaled score for radar chart
      description: 'Explosive power based on vertical jump height',
      rawValue: weeklyTests?.find(test => test.test_name === 'vertical_jump')?.result_value
        ? `${weeklyTests.find(test => test.test_name === 'vertical_jump')?.result_value?.toFixed(1)} cm`
        : 'Vertical jump test required',
      color: '#4ECDC4',
      icon: (
        <CustomSvgIcon
          src={ExplosivenessIcon}
          alt="Explosiveness"
          sx={{ fontSize: 32, color: '#00bcd4' }}
        />
      ),
    },
    {
      metric: 'Stamina',
      value: Math.max(1, Math.min(100, scores.aerobic_capacity_score || 1)), // Use scaled score for radar chart
      description: 'Aerobic capacity based on VO₂ max',
      rawValue: metrics?.vo2_max
        ? `${metrics.vo2_max.toFixed(1)} ml/kg/min`
        : 'VO₂ max test required',
      color: '#45B7D1',
      icon: <CustomSvgIcon src={HealthIcon} alt="Stamina" sx={{ fontSize: 32, color: '#00bcd4' }} />,
    },
    {
      metric: 'Recovery',
      value: Math.max(1, Math.min(100, scores.recovery_score || 1)), // Use scaled score for radar chart
      description: 'Recovery ability based on HR recovery',
      rawValue: weeklyTests?.find(test => test.test_name === 'hr_recovery')?.result_value
        ? `${weeklyTests.find(test => test.test_name === 'hr_recovery')?.result_value} bpm drop`
        : 'HR recovery test required',
      color: '#96CEB4',
      icon: (
        <CustomSvgIcon src={RecoveryIcon} alt="Recovery" sx={{ fontSize: 32, color: '#00bcd4' }} />
      ),
    },
    {
      metric: 'Reflexes',
      value: Math.max(1, Math.min(100, scores.reaction_time_score || 1)), // Use scaled score for radar chart
      description: 'Reaction speed based on response time',
      rawValue: weeklyTests?.find(test => test.test_name === 'reflex')?.result_value
        ? `${weeklyTests.find(test => test.test_name === 'reflex')?.result_value} ms`
        : 'Reflex test required',
      color: '#DDA0DD',
      icon: (
        <CustomSvgIcon
          src={ReflexesIcon}
          alt="Reflexes"
          sx={{ fontSize: 32, color: '#00bcd4' }}
        />
      ),
    },
    {
      metric: 'Strength',
      value: strengthScore || 1, // Use calculated strength score for radar chart
      description: 'Relative strength based on Wilks score',
      rawValue: wilksScore ? `${wilksScore.toFixed(1)} Wilks` : '1RM data required',
      color: '#FF6B6B',
      icon: <CustomSvgIcon src={StrengthIcon} alt="Strength" sx={{ fontSize: 32, color: '#00bcd4' }} />,
    },
    {
      metric: 'Dexterity',
      value: Math.max(1, Math.min(100, scores.mobility_score || 1)), // Use scaled score for radar chart
      description: 'Joint mobility and flexibility',
      rawValue: weeklyTests?.find(test => test.test_name === 'mobility')?.result_value
        ? `${weeklyTests.find(test => test.test_name === 'mobility')?.result_value?.toFixed(1)}%`
        : 'Mobility test required',
      color: '#9C27B0',
      icon: (
        <CustomSvgIcon src={DexterityIcon} alt="Dexterity" sx={{ fontSize: 32, color: '#00bcd4' }} />
      ),
    },
  ];

  return data; // Show all metrics even if 0
};

export const PerformanceRadarChart: React.FC<PerformanceRadarChartProps> = ({
  scores,
  metrics,
  weeklyTests,
  wilksScore,
  title = 'Performance Metrics',
  height = 400,
}) => {
  const theme = useTheme();

  const metricData = getMetricData(scores, metrics, weeklyTests, wilksScore);

  // Ensure we have valid data to prevent NaN errors
  const validMetricData = metricData.map(item => ({
    ...item,
    value: (() => {
      const val = item.value;
      if (val === null || val === undefined || isNaN(val) || !isFinite(val)) {
        return 1; // Use 1 instead of 0 to prevent division by zero
      }
      return Math.max(1, Math.min(100, val)); // Clamp between 1-100
    })(),
  }));

  // Custom tooltip component following Nivo example
  const MyCustomTooltip = ({ index }: { index: string | number }) => {
    // Use 'index' as the metric name since 'id' is undefined
    const metricName = String(index);

    // Find the metric data using the metric name
    const metric = validMetricData.find(m => m.metric === metricName);

    if (!metric) {
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
          <Box sx={{ fontSize: '0.9rem' }}>No metric found for: {metricName}</Box>
        </Box>
      );
    }

    return (
      <Box
        sx={{
          background: 'rgba(0, 0, 0, 0.9)',
          color: 'white',
          borderRadius: 2,
          padding: 2,
          border: '1px solid rgba(255, 255, 255, 0.2)',
          minWidth: 200,
          boxShadow: '0 4px 12px rgba(0, 0, 0, 0.5)',
          zIndex: 99999,
          position: 'relative',
        }}
      >
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, fontSize: '0.9rem' }}>
          {metric.icon}
          <Box sx={{ fontWeight: 'bold' }}>{metric.metric}:</Box>
          <Box sx={{ fontWeight: 'bold', color: '#00bcd4' }}>{metric.rawValue}</Box>
        </Box>
        <Box sx={{ fontSize: '0.75rem', opacity: 0.8, fontStyle: 'italic', mt: 1 }}>
          {metric.description}
        </Box>
      </Box>
    );
  };

  // Don't render if no data
  if (metricData.length === 0) {
    return (
      <Box>
        {title && (
          <Box display="flex" alignItems="center" gap={1} sx={{ mb: 2 }}>
            <RadarIcon sx={{ color: 'white' }} />
            <GameText variant="h6">{title}</GameText>
          </Box>
        )}
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
      </Box>
    );
  }

  // Custom grid label component that renders SVG icons instead of text
  const CustomGridLabel = ({ id, x, y }: { id: string; x: number; y: number }) => {
    // Safety checks to prevent NaN errors
    const safeX = typeof x === 'number' && !isNaN(x) ? x : 0;
    const safeY = typeof y === 'number' && !isNaN(y) ? y : 0;

    // Find the metric data to get the icon
    const metricData = validMetricData.find(m => m.metric === id);
    if (!metricData?.icon) return null;

    return (
      <g transform={`translate(${safeX}, ${safeY})`}>
        <foreignObject width="20" height="20" x={-10} y={-10}>
          <div
            style={{
              width: '20px',
              height: '20px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            }}
          >
            {metricData.icon}
          </div>
        </foreignObject>
      </g>
    );
  };

  return (
    <Box sx={{ position: 'relative', overflow: 'visible' }}>
      {title && (
        <Box display="flex" alignItems="center" gap={1} sx={{ mb: 2 }}>
          <RadarIcon sx={{ color: 'white' }} />
          <Tooltip title="Your performance across different fitness domains" arrow>
            <GameText variant="h6" sx={{ fontWeight: 'bold' }}>
              {title}
            </GameText>
          </Tooltip>
        </Box>
      )}
      <Box
        sx={{
          height,
          width: '100%',
          minHeight: 300,
          minWidth: 300,
          position: 'relative',
          overflow: 'visible',
          zIndex: 1,
        }}
      >
        <ResponsiveRadar
          data={validMetricData}
          keys={['value']}
          indexBy="metric"
          margin={{ top: 60, right: 60, bottom: 60, left: 60 }}
          colors={['#00bcd4']}
          maxValue={100}
          curve="linearClosed"
          fillOpacity={0.1}
          animate={true}
          gridLevels={5}
          gridShape="circular"
          gridLabelOffset={36}
          gridLabel={CustomGridLabel}
          sliceTooltip={MyCustomTooltip}
          isInteractive={true}
          enableDots={true}
          dotSize={8}
          dotBorderWidth={2}
          theme={createCongenNivoTheme(theme.palette.mode)}
        />
      </Box>
    </Box>
  );
};
