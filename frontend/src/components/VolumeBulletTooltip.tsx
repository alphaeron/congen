import React from 'react';

import { getCongenNivoTooltipStyle, type CongenNivoTheme } from '../theme/nivoTheme';
import type { VolumeCategoryMetrics } from '../utils/volumeOverviewUtils';
import { formatCompactVolume } from '../utils/volumeOverviewUtils';

export interface VolumeBulletTooltipProps {
  card: VolumeCategoryMetrics;
  doneLabel: string;
  planLabel: string;
  preferredUnit: 'KG' | 'LBS';
  nivoTheme: CongenNivoTheme;
}

const VolumeBulletTooltipRow: React.FC<{ label: string; value: React.ReactNode }> = ({
  label,
  value,
}) => (
  <div>
    {label}: <strong>{value}</strong>
  </div>
);

/**
 * Plain-language tooltip for the volume bullet chart.
 * Rows are always rendered in a fixed order so positions stay stable as data fills in.
 */
export const VolumeBulletTooltip: React.FC<VolumeBulletTooltipProps> = ({
  card,
  doneLabel,
  planLabel,
  preferredUnit,
  nivoTheme,
}) => {
  const percentOfPlan = card.hasTarget ? Math.round((card.current / card.target) * 100) : null;
  const weekAvgLabel =
    card.sameWeekSlotAverage != null
      ? formatCompactVolume(card.sameWeekSlotAverage, preferredUnit)
      : null;

  const doneValue = card.loggingIncomplete ? 'no data' : doneLabel;
  const planValue =
    card.hasTarget && percentOfPlan != null ? `${planLabel} (${percentOfPlan}%)` : planLabel;
  const weekAvgValue = weekAvgLabel
    ? `${weekAvgLabel}${
        card.sameWeekSlotSampleCount > 0 ? ` (n=${card.sameWeekSlotSampleCount})` : ''
      }`
    : 'no data';

  return (
    <div
      data-testid="volume-bullet-tooltip"
      style={getCongenNivoTooltipStyle(nivoTheme, {
        position: 'relative',
        zIndex: 2000,
        pointerEvents: 'none',
      })}
    >
      <VolumeBulletTooltipRow label="Done" value={doneValue} />
      <VolumeBulletTooltipRow label="Target" value={planValue} />
      <VolumeBulletTooltipRow label="Average Volume" value={weekAvgValue} />
    </div>
  );
};
