import { Box } from '@mui/material';
import React from 'react';

import { CongenBullet } from './CongenBullet';
import { VolumeBulletTooltip } from './VolumeBulletTooltip';
import {
  VOLUME_BAND_COLORS,
  VOLUME_BULLET_MARGIN,
  VOLUME_MARKER_COLORS,
} from './volumeOverviewTheme';
import type { CongenNivoTheme } from '../theme/nivoTheme';
import type { VolumeCategoryMetrics, VolumeStatus } from '../utils/volumeOverviewUtils';
import { buildBulletAxisLabels, formatCompactVolume } from '../utils/volumeOverviewUtils';

export interface VolumeCategoryBulletProps {
  card: VolumeCategoryMetrics;
  preferredUnit: 'KG' | 'LBS';
  nivoTheme: CongenNivoTheme;
  doneLabel: string;
  planLabel: string;
}

function getStatusColor(status: VolumeStatus): string {
  switch (status) {
    case 'exceeded':
      return 'var(--game-success)';
    case 'on_track':
      return 'var(--game-cyan)';
    case 'under':
      return 'var(--game-warning)';
    case 'no_volume':
      return 'var(--game-white-muted)';
    case 'overshoot':
      return 'var(--game-error)';
  }
}

/**
 * Maps volume category metrics onto a CongenBullet with plan bands and markers.
 */
export const VolumeCategoryBullet: React.FC<VolumeCategoryBulletProps> = ({
  card,
  preferredUnit,
  nivoTheme,
  doneLabel,
  planLabel,
}) => {
  const scaleMax = card.scaleMax;
  const markers: number[] = [];
  const markerColors: string[] = [];
  if (card.sameWeekSlotAverage != null && card.sameWeekSlotAverage > 0) {
    markers.push(Math.min(card.sameWeekSlotAverage, scaleMax));
    markerColors.push(VOLUME_MARKER_COLORS.weekAvg);
  }

  const axisLabels = buildBulletAxisLabels({
    scaleMax,
    poorEnd: card.poorEnd,
    okEnd: card.okEnd,
    goodEnd: card.goodEnd,
    preferredUnit,
  });

  const measureValue = Math.min(scaleMax, card.loggingIncomplete ? 0 : card.current);
  const bulletRanges = [card.poorEnd, card.okEnd, card.goodEnd, scaleMax];
  const measureColor = card.loggingIncomplete
    ? 'var(--game-white-muted)'
    : getStatusColor(card.status);
  const axisTickValues = axisLabels.map(label => label.value);
  const axisFormat = (value: number): string => {
    const match = axisLabels.find(label => label.value === value);
    return match?.text ?? formatCompactVolume(value, preferredUnit).replace(/ (lbs|kg)$/, '');
  };

  return (
    <Box
      sx={{ width: '100%', overflow: 'visible', position: 'relative', zIndex: 2 }}
      data-testid={`volume-bullet-${card.type}`}
      aria-label={`${card.type}: done ${doneLabel}, plan ${planLabel}`}
    >
      <Box
        sx={{
          height: 88,
          width: '100%',
          overflow: 'visible',
          position: 'relative',
        }}
      >
        <CongenBullet
          data={[
            {
              id: card.type,
              title: '',
              ranges: bulletRanges,
              measures: [measureValue],
              markers,
            },
          ]}
          margin={VOLUME_BULLET_MARGIN}
          minValue={0}
          maxValue={scaleMax}
          measureSize={0.45}
          markerSize={0.75}
          rangeColors={[
            VOLUME_BAND_COLORS.poor,
            VOLUME_BAND_COLORS.ok,
            VOLUME_BAND_COLORS.good,
            VOLUME_BAND_COLORS.overload,
          ]}
          measureColors={[measureColor]}
          markerColors={markerColors}
          axisBottom={{
            tickValues: axisTickValues,
            format: axisFormat,
            tickSize: 14,
            tickPadding: 6,
          }}
          tooltip={() => (
            <VolumeBulletTooltip
              card={card}
              doneLabel={doneLabel}
              planLabel={planLabel}
              preferredUnit={preferredUnit}
              nivoTheme={nivoTheme}
            />
          )}
        />
      </Box>
    </Box>
  );
};
