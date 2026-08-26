import { Box, Tooltip } from '@mui/material';
import { ResponsiveLine } from '@nivo/line';
import React from 'react';

import { VOLUME_SERIES_COLORS } from './volumeOverviewTheme';
import type { CongenNivoTheme } from '../theme/nivoTheme';
import type { VolumeTrendPoint } from '../utils/volumeOverviewUtils';

export interface VolumeTrendSparklineProps {
  data: VolumeTrendPoint[];
  nivoTheme: CongenNivoTheme;
  ariaLabel: string;
  onClick?: () => void;
  interactive?: boolean;
}

/**
 * Builds the accessible label for the volume trend sparkline.
 *
 * @param category Volume category name
 * @param priorPeriodDeltaPercent Week-over-week delta or null
 * @returns Aria label including click-to-expand guidance
 */
export function getVolumeSparklineAriaLabel(
  category: string,
  priorPeriodDeltaPercent: number | null
): string {
  if (priorPeriodDeltaPercent == null) {
    return `${category} weekly volume trend. Click to expand.`;
  }
  const direction = priorPeriodDeltaPercent >= 0 ? 'up' : 'down';
  return `${category} weekly volume trend, ${direction} ${Math.abs(priorPeriodDeltaPercent)} percent versus last week. Click to expand.`;
}

/**
 * Compact sparkline for card header trend display.
 */
export const VolumeTrendSparkline: React.FC<VolumeTrendSparklineProps> = ({
  data,
  nivoTheme,
  ariaLabel,
  onClick,
  interactive = true,
}) => {
  const sparkline = (
    <Box
      sx={{
        width: 88,
        height: 28,
        flexShrink: 0,
        ml: interactive ? 'auto' : 0,
        cursor: interactive ? 'pointer' : 'default',
        borderRadius: 1,
        '&:hover': interactive
          ? {
              backgroundColor: 'rgba(0, 188, 212, 0.08)',
            }
          : undefined,
      }}
      data-testid="volume-trend-sparkline"
      role={interactive ? 'button' : undefined}
      tabIndex={interactive ? 0 : undefined}
      aria-label={ariaLabel}
      onClick={
        interactive
          ? (event: React.MouseEvent) => {
              event.stopPropagation();
              onClick?.();
            }
          : undefined
      }
      onKeyDown={
        interactive
          ? (event: React.KeyboardEvent) => {
              if (event.key === 'Enter' || event.key === ' ') {
                event.preventDefault();
                event.stopPropagation();
                onClick?.();
              }
            }
          : undefined
      }
    >
      <ResponsiveLine
        data={[{ id: 'trend', data }]}
        margin={{ top: 2, right: 2, bottom: 2, left: 2 }}
        xScale={{ type: 'point' }}
        yScale={{ type: 'linear', min: 0, max: 'auto' }}
        curve="monotoneX"
        axisTop={null}
        axisRight={null}
        axisBottom={null}
        axisLeft={null}
        enableGridX={false}
        enableGridY={false}
        enablePoints={false}
        enableArea={true}
        areaOpacity={0.3}
        colors={[VOLUME_SERIES_COLORS.volume]}
        lineWidth={2}
        theme={nivoTheme}
        animate={true}
        motionConfig="gentle"
      />
    </Box>
  );

  if (!interactive) {
    return sparkline;
  }

  return (
    <Tooltip title="Click to expand" arrow>
      {sparkline}
    </Tooltip>
  );
};
