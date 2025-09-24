import React from 'react';
import {
  Box,
  useTheme,
  Tooltip,
} from '@mui/material';
import { ResponsiveRadar } from '@nivo/radar';
import { 
  Radar as RadarIcon,
} from '@mui/icons-material';
import type { UserPerformanceScores, UserPerformanceMetrics, UserWeeklyTest } from '../api/types';
import { createCongenNivoTheme } from '../theme/nivoTheme';
import { GameText } from './GameTheme';
import { CustomSvgIcon } from './CustomSvgIcon';

// Import custom SVG icons for attributes
import PowerIcon from '../resources/power-icon.svg';
import EnduranceIcon from '../resources/endurance-icon.svg';
import RecoveryIcon from '../resources/recovery-icon.svg';
import SpeedIcon from '../resources/speed-icon.svg';

interface PerformanceRadarChartProps {
  scores: UserPerformanceScores;
  metrics?: UserPerformanceMetrics | null;
  weeklyTest?: UserWeeklyTest | null;
  wilksScore?: number | null;
  title?: string;
  height?: number;
}

const getMetricData = (scores: UserPerformanceScores, metrics?: UserPerformanceMetrics | null, weeklyTest?: UserWeeklyTest | null, wilksScore?: number | null) => {
  // Calculate strength score from Wilks score (scale 0-100)
  const strengthScore = wilksScore ? Math.max(1, Math.min(100, (wilksScore / 5.0))) : null; // Rough scaling: 500 Wilks = 100 score

  const data = [
    {
      metric: 'Explosiveness',
      value: Math.max(1, Math.min(100, scores.explosiveness_score || 1)), // Use scaled score for radar chart
      description: 'Explosive power based on vertical jump height',
      rawValue: weeklyTest?.vertical_jump_result ? `${weeklyTest.vertical_jump_result.toFixed(1)} cm` : 'Vertical jump test required',
      color: '#4ECDC4',
    },
    {
      metric: 'Endurance',
      value: Math.max(1, Math.min(100, scores.aerobic_capacity_score || 1)), // Use scaled score for radar chart
      description: 'Aerobic capacity based on VO₂ max',
      rawValue: metrics?.vo2_max ? `${metrics.vo2_max.toFixed(1)} ml/kg/min` : 'VO₂ max test required',
      color: '#45B7D1',
    },
    {
      metric: 'Recovery',
      value: Math.max(1, Math.min(100, scores.recovery_score || 1)), // Use scaled score for radar chart
      description: 'Recovery ability based on HR recovery',
      rawValue: weeklyTest?.hr_recovery_result ? `${weeklyTest.hr_recovery_result} bpm drop` : 'HR recovery test required',
      color: '#96CEB4',
    },
    {
      metric: 'Reaction Time',
      value: Math.max(1, Math.min(100, scores.reaction_time_score || 1)), // Use scaled score for radar chart
      description: 'Reaction speed based on response time',
      rawValue: weeklyTest?.reflex_result ? `${weeklyTest.reflex_result} ms` : 'Reflex test required',
      color: '#DDA0DD',
    },
    {
      metric: 'Strength',
      value: strengthScore || 1, // Use calculated strength score for radar chart
      description: 'Relative strength based on Wilks score',
      rawValue: wilksScore ? `${wilksScore.toFixed(1)} Wilks` : '1RM data required',
      color: '#FF6B6B',
    },
  ];

  return data; // Show all metrics even if 0
};

export const PerformanceRadarChart: React.FC<PerformanceRadarChartProps> = ({
  scores,
  metrics,
  weeklyTest,
  wilksScore,
  title = 'Performance Metrics',
  height = 400,
}) => {
  const theme = useTheme();
  const nivoTheme = createCongenNivoTheme(theme.palette.mode);
  
  const metricData = getMetricData(scores, metrics, weeklyTest, wilksScore);
  
  // Ensure we have valid data to prevent NaN errors
  const validMetricData = metricData.map(item => ({
    ...item,
    value: (() => {
      const val = item.value;
      if (val === null || val === undefined || isNaN(val) || !isFinite(val)) {
        return 1; // Use 1 instead of 0 to prevent division by zero
      }
      return Math.max(1, Math.min(100, val)); // Clamp between 1-100
    })()
  }));

  // Custom tooltip component following Nivo example
    const MyCustomTooltip = ({ index, data, id, value, color }: any) => {
      // Use 'index' as the metric name since 'id' is undefined
      const metricName = index;
      
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
            <Box sx={{ fontSize: '0.9rem' }}>
              No metric found for: {metricName}
            </Box>
          </Box>
        );
      }

    const getIcon = (metricName: string) => {
      switch (metricName) {
        case 'Explosiveness': return <CustomSvgIcon src={PowerIcon} alt="Explosiveness" sx={{ fontSize: 16, color: '#00bcd4' }} />;
        case 'Endurance': return <CustomSvgIcon src={EnduranceIcon} alt="Endurance" sx={{ fontSize: 16, color: '#00bcd4' }} />;
        case 'Recovery': return <CustomSvgIcon src={RecoveryIcon} alt="Recovery" sx={{ fontSize: 16, color: '#00bcd4' }} />;
        case 'Reaction Time': return <CustomSvgIcon src={SpeedIcon} alt="Reaction Time" sx={{ fontSize: 16, color: '#00bcd4' }} />;
        case 'Strength': return <CustomSvgIcon src={PowerIcon} alt="Strength" sx={{ fontSize: 16, color: '#00bcd4' }} />;
        default: return null;
      }
    };

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
          {getIcon(metric.metric)}
          <Box sx={{ fontWeight: 'bold' }}>
            {metric.metric}:
          </Box>
          <Box sx={{ fontWeight: 'bold', color: '#00bcd4' }}>
            {metric.rawValue}
          </Box>
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
            <GameText variant="h6">
              {title}
            </GameText>
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
  const CustomGridLabel = ({ id, anchor, x, y }: any) => {
    // Safety checks to prevent NaN errors
    const safeX = typeof x === 'number' && !isNaN(x) ? x : 0;
    const safeY = typeof y === 'number' && !isNaN(y) ? y : 0;
    
    const getIconPath = (metric: string) => {
      switch (metric) {
        case 'Explosiveness': return PowerIcon;
        case 'Endurance': return EnduranceIcon;
        case 'Recovery': return RecoveryIcon;
        case 'Reaction Time': return SpeedIcon;
        case 'Strength': return PowerIcon;
        default: return null;
      }
    };

    const iconPath = getIconPath(id);
    if (!iconPath) return null;

    return (
      <g transform={`translate(${safeX}, ${safeY})`}>
        <image
          href={iconPath}
          width="20"
          height="20"
          x={-10}
          y={-10}
        />
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
      
      <Box sx={{ 
        height, 
        width: '100%', 
        minHeight: 300, 
        minWidth: 300,
        position: 'relative',
        overflow: 'visible',
        zIndex: 1
      }}>
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
