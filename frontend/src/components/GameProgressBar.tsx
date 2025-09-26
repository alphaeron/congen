import { Box, Typography, Tooltip } from '@mui/material';
import { styled, alpha } from '@mui/material/styles';
import React from 'react';

import { StatusBarContainer } from './GameTheme';

/**
 * Shared progress bar components for consistent styling across the game UI.
 */
const ProgressBarContainer = styled(Box)(() => ({
  position: 'relative',
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
  height: 12,
  minWidth: 200,
  borderRadius: 6,
  backgroundColor: alpha('#ffffff', 0.3),
  overflow: 'hidden',
}));

const ProgressBarFill = styled(Box)<{ percentage: number; color: string }>(
  ({ percentage, color }) => ({
    position: 'absolute',
    left: 0,
    top: 0,
    height: '100%',
    width: `${Math.max(2, percentage)}%`, // Show available portion in blue
    backgroundColor: color,
    borderRadius: 4,
    transition: 'width 0.3s cubic-bezier(0.4, 0, 0.2, 1)', // Matches Congen's transition
  })
);

const ProgressBarText = styled(Typography)(() => ({
  position: 'relative',
  zIndex: 1,
  fontSize: '0.75rem',
  fontWeight: 600, // Matches Congen's font weight
  color: 'white',
  textShadow: '1px 1px 2px rgba(0, 0, 0, 0.7)',
  textAlign: 'center',
  lineHeight: 1,
  fontFamily: '"Inter", "system-ui", "sans-serif"', // Matches Congen's font
}));

const LabelText = styled(Typography)(() => ({
  fontSize: '0.75rem',
  color: '#ffffff',
  textShadow: '0 0 3px #00bcd4',
  textAlign: 'center',
  marginTop: 4,
  fontWeight: 600, // Matches Congen's font weight
  fontFamily: '"Inter", "system-ui", "sans-serif"', // Matches Congen's font
  textTransform: 'uppercase',
}));

interface GameProgressBarProps {
  icon?: React.ReactNode;
  label: string;
  current: number;
  max: number;
  color: string;
  tooltip: string;
}

/**
 * Linear progress bar with centered text overlay
 */
export const GameProgressBar: React.FC<GameProgressBarProps> = ({
  icon,
  label,
  current,
  max,
  color,
  tooltip,
}) => {
  const percentage = max === 0 ? 0 : Math.max(0, Math.min(100, (current / max) * 100));

  return (
    <Tooltip title={tooltip}>
      <StatusBarContainer>
        <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 0.25 }}>
          {icon}
          <LabelText>{label}</LabelText>
        </Box>
        <Box sx={{ flexGrow: 1 }}>
          <ProgressBarContainer>
            <ProgressBarFill percentage={percentage} color={color} />
            <ProgressBarText>
              {current.toFixed(0)}/{max.toFixed(0)}
            </ProgressBarText>
          </ProgressBarContainer>
        </Box>
      </StatusBarContainer>
    </Tooltip>
  );
};

const CircularProgressContainer = styled(Box)(() => ({
  position: 'relative',
  display: 'flex',
  flexDirection: 'column',
  alignItems: 'center',
  justifyContent: 'center',
  width: 90,
  height: 90,
}));

const CircularProgressWrapper = styled(Box)(() => ({
  position: 'relative',
  display: 'inline-flex',
  alignItems: 'center',
  justifyContent: 'center',
  width: 75,
  height: 75,
}));

const CircularProgressText = styled(Typography)(() => ({
  position: 'absolute',
  fontSize: '0.6rem',
  fontWeight: 600, // Matches Congen's font weight
  color: 'white',
  textAlign: 'center',
  lineHeight: 1,
  fontFamily: '"Inter", "system-ui", "sans-serif"', // Matches Congen's font
}));

/**
 * Circular progress bar with centered values and label
 */
export const GameCircularProgressBar: React.FC<GameProgressBarProps> = ({
  icon,
  label,
  current,
  max,
  color,
  tooltip,
}) => {
  const percentage = max === 0 ? 0 : Math.max(0, Math.min(100, (current / max) * 100));

  return (
    <Tooltip title={tooltip}>
      <StatusBarContainer>
        <CircularProgressContainer>
          <CircularProgressWrapper>
            <Box
              sx={{
                position: 'relative',
                display: 'inline-flex',
                alignItems: 'center',
                justifyContent: 'center',
                width: 75,
                height: 75,
              }}
            >
              {/* Custom circular progress using SVG - based on Smashing Things Together tutorial */}
              <svg
                width={75}
                height={75}
                viewBox="0 0 75 75"
                style={{ transform: 'rotate(90deg)' }}
              >
                {/* Background dashed circle */}
                <circle
                  cx="37.5"
                  cy="37.5"
                  r="30"
                  fill="none"
                  stroke={alpha('#ffffff', 0.2)}
                  strokeWidth="5"
                  strokeDasharray="1.5,1.5"
                />
                {/* Progress circle - counterclockwise from bottom */}
                <circle
                  cx="37.5"
                  cy="37.5"
                  r="30"
                  fill="none"
                  stroke={color}
                  strokeWidth="5"
                  strokeLinecap="round"
                  strokeDasharray={`${2 * Math.PI * 30}`}
                  strokeDashoffset={`-${2 * Math.PI * 30 * (1 - percentage / 100)}`}
                  style={{
                    transition: 'stroke-dashoffset 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
                  }}
                />
              </svg>
              <CircularProgressText>
                {current.toFixed(0)}/{max.toFixed(0)}
              </CircularProgressText>
            </Box>
          </CircularProgressWrapper>
          <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 0.5 }}>
            {icon && icon}
            <LabelText>{label}</LabelText>
          </Box>
        </CircularProgressContainer>
      </StatusBarContainer>
    </Tooltip>
  );
};
