import { Box, CardContent } from '@mui/material';
import { motion } from 'framer-motion';
import React, { useMemo, useState } from 'react';

import { GameCard, GameText, GAME_CLASSES } from './GameTheme';
import { VolumeCategoryBullet } from './VolumeCategoryBullet';
import { VolumeTrendChartDialog } from './VolumeTrendChartDialog';
import { getVolumeSparklineAriaLabel, VolumeTrendSparkline } from './VolumeTrendSparkline';
import type { UserOneRepMax } from '../api/types';
import type { CongenNivoTheme } from '../theme/nivoTheme';
import type { VolumeCategoryMetrics, WeekVolumeTotals } from '../utils/volumeOverviewUtils';
import {
  buildWeeklyAcwrSeries,
  buildWeeklyIntensitySeries,
  formatCompactVolume,
  getCategoryValue,
} from '../utils/volumeOverviewUtils';

export interface VolumeCategoryCardProps {
  card: VolumeCategoryMetrics;
  index: number;
  preferredUnit: 'KG' | 'LBS';
  nivoTheme: CongenNivoTheme;
  weekVolumes: WeekVolumeTotals[];
  userOneRepMaxes: UserOneRepMax[] | undefined;
}

/**
 * Single volume category KPI card with bullet chart and expandable trend dialog.
 */
export const VolumeCategoryCard: React.FC<VolumeCategoryCardProps> = ({
  card,
  index,
  preferredUnit,
  nivoTheme,
  weekVolumes,
  userOneRepMaxes,
}) => {
  const [chartOpen, setChartOpen] = useState(false);
  const currentLabel = formatCompactVolume(card.current, preferredUnit);
  const planLabel = card.hasTarget ? formatCompactVolume(card.target, preferredUnit) : 'No plan';

  const sparklineData = useMemo(
    () =>
      weekVolumes.map(week => ({
        x: `W${week.weekNumber}`,
        y: getCategoryValue(week, card.type),
      })),
    [weekVolumes, card.type]
  );

  const acwrData = useMemo(
    () => buildWeeklyAcwrSeries(weekVolumes, card.type),
    [weekVolumes, card.type]
  );

  const intensityData = useMemo(
    () =>
      card.type === 'Max Effort' ? buildWeeklyIntensitySeries(weekVolumes, userOneRepMaxes) : [],
    [weekVolumes, userOneRepMaxes, card.type]
  );

  return (
    <Box
      sx={{
        flex: '1 1 280px',
        minWidth: 260,
        overflow: 'visible',
        position: 'relative',
        zIndex: 1,
        '&:hover': {
          zIndex: 30,
        },
      }}
    >
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.4, delay: index * 0.1 }}
        style={{
          overflow: 'visible',
          position: 'relative',
          height: '100%',
        }}
      >
        <GameCard
          className={GAME_CLASSES.overflowVisible}
          sx={{ height: '100%', overflow: 'visible !important', position: 'relative' }}
        >
          <CardContent
            sx={{
              p: 2,
              '&:last-child': { pb: 2 },
              overflow: 'visible',
              position: 'relative',
            }}
          >
            <Box
              sx={{
                display: 'flex',
                alignItems: 'center',
                mb: 1.5,
                gap: 1,
                minWidth: 0,
              }}
            >
              <GameText
                variant="subtitle1"
                className={GAME_CLASSES.textMedium}
                sx={{ flexShrink: 0 }}
              >
                {card.type}
              </GameText>
              <VolumeTrendSparkline
                data={sparklineData}
                nivoTheme={nivoTheme}
                ariaLabel={getVolumeSparklineAriaLabel(card.type, card.priorPeriodDeltaPercent)}
                onClick={() => setChartOpen(true)}
              />
            </Box>

            {card.isOvershoot ? (
              <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1, mb: 1 }}>
                <GameText variant="caption" sx={{ color: 'var(--game-error)' }}>
                  Over plan
                </GameText>
              </Box>
            ) : null}

            <VolumeCategoryBullet
              card={card}
              preferredUnit={preferredUnit}
              nivoTheme={nivoTheme}
              doneLabel={currentLabel}
              planLabel={planLabel}
            />
          </CardContent>
        </GameCard>

        <VolumeTrendChartDialog
          open={chartOpen}
          category={card.type}
          volumeData={sparklineData}
          acwrData={acwrData}
          intensityData={intensityData}
          weekVolumes={weekVolumes}
          nivoTheme={nivoTheme}
          preferredUnit={preferredUnit}
          onClose={() => setChartOpen(false)}
        />
      </motion.div>
    </Box>
  );
};
