import { Box, CardContent, Collapse, useTheme } from '@mui/material';
import { ResponsiveBullet } from '@nivo/bullet';
import { ResponsiveLine } from '@nivo/line';
import { motion } from 'framer-motion';
import React, { useMemo, useState } from 'react';

import { GameCard, GameText, GAME_CLASSES } from './GameTheme';
import type { UserDataExport, Exercise } from '../api/types';
import { createCongenNivoTheme } from '../theme/nivoTheme';
import type { VolumeStatus, VolumeCategoryMetrics } from '../utils/volumeOverviewUtils';
import { buildVolumeOverviewModel, formatCompactVolume } from '../utils/volumeOverviewUtils';

interface VolumeOverviewCardsProps {
  userDataExport: UserDataExport | null;
  exerciseData: Map<string, Exercise>;
  workoutsPerWeek: number;
  currentWeek: number;
  preferredUnit?: 'KG' | 'LBS';
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
  }
}

function getTrendColor(deltaPercent: number | null): string {
  if (deltaPercent == null || deltaPercent === 0) {
    return 'var(--game-white-muted)';
  }
  return deltaPercent > 0 ? 'var(--game-cyan)' : 'var(--game-error)';
}

function formatCardVolume(volume: number, preferredUnit: 'KG' | 'LBS'): string {
  if (volume === 0) {
    return formatCompactVolume(0, preferredUnit);
  }
  return formatCompactVolume(volume, preferredUnit);
}

/**
 * Compact sparkline for card header trend display.
 */
const VolumeTrendSparkline: React.FC<{
  data: Array<{ x: string; y: number }>;
  nivoTheme: ReturnType<typeof createCongenNivoTheme>;
}> = ({ data, nivoTheme }) => (
  <Box sx={{ width: 72, height: 28, flexShrink: 0 }} data-testid="volume-trend-sparkline">
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
      colors={['var(--game-cyan)']}
      lineWidth={2}
      theme={nivoTheme}
      animate={true}
      motionConfig="gentle"
    />
  </Box>
);

/**
 * Volume overview KPI cards with shared-scale bullets and vs-target deltas.
 *
 * @param userDataExport Raw user export containing workouts and optional 1RM records
 * @param exerciseData Exercise metadata used for ME/DE/Accessory categorization
 * @param workoutsPerWeek Program days per week for week bucketing
 * @param currentWeek Active program week number
 * @param preferredUnit Weight unit for volume display
 * @return Volume overview section
 */
export const VolumeOverviewCards: React.FC<VolumeOverviewCardsProps> = ({
  userDataExport,
  exerciseData,
  workoutsPerWeek,
  currentWeek,
  preferredUnit = 'LBS',
}) => {
  const theme = useTheme();
  const nivoTheme = createCongenNivoTheme(theme.palette.mode);
  const [expandedCategory, setExpandedCategory] = useState<string | null>(null);

  const model = useMemo(
    () =>
      buildVolumeOverviewModel(
        userDataExport,
        exerciseData,
        workoutsPerWeek,
        currentWeek,
        'this_week',
        preferredUnit
      ),
    [userDataExport, exerciseData, workoutsPerWeek, currentWeek, preferredUnit]
  );

  if (!model) {
    return null;
  }

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.5, ease: 'easeOut' }}
      style={{ display: 'flex', flexDirection: 'column', gap: 12 }}
    >
      <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
        {model.categories.map((card, index) => (
          <VolumeCategoryCard
            key={card.type}
            card={card}
            index={index}
            sharedScaleMax={model.sharedScaleMax}
            preferredUnit={preferredUnit}
            nivoTheme={nivoTheme}
            expanded={expandedCategory === card.type}
            onToggle={() => setExpandedCategory(prev => (prev === card.type ? null : card.type))}
            sparklineData={model.weekVolumes.map(week => ({
              x: `W${week.weekNumber}`,
              y:
                card.type === 'Max Effort'
                  ? week.maxEffortVolume
                  : card.type === 'Dynamic Effort'
                    ? week.dynamicEffortVolume
                    : week.accessoryVolume,
            }))}
          />
        ))}
      </Box>
    </motion.div>
  );
};

interface VolumeCategoryCardProps {
  card: VolumeCategoryMetrics;
  index: number;
  sharedScaleMax: number;
  preferredUnit: 'KG' | 'LBS';
  nivoTheme: ReturnType<typeof createCongenNivoTheme>;
  expanded: boolean;
  onToggle: () => void;
  sparklineData: Array<{ x: string; y: number }>;
}

/**
 * Single volume category KPI card with bullet chart and progressive disclosure.
 */
const VolumeCategoryCard: React.FC<VolumeCategoryCardProps> = ({
  card,
  index,
  sharedScaleMax,
  preferredUnit,
  nivoTheme,
  expanded,
  onToggle,
  sparklineData,
}) => {
  const statusColor = getStatusColor(card.status);
  const deltaPrefix = card.deltaAbsolute >= 0 ? '+' : '';
  const trendColor = getTrendColor(card.priorPeriodDeltaPercent);
  const currentLabel = formatCardVolume(card.current, preferredUnit);
  const targetLabel = card.hasBaseline
    ? formatCompactVolume(card.target, preferredUnit)
    : 'No baseline';
  const bulletData = [
    {
      id: card.type,
      title: '',
      ranges: card.hasBaseline
        ? [Math.round(card.target * 0.85), card.target, sharedScaleMax]
        : [Math.round(sharedScaleMax * 0.5), sharedScaleMax],
      measures: [card.current],
      markers: card.hasBaseline ? [card.target] : [],
    },
  ];

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.4, delay: index * 0.1 }}
      whileHover={{ y: -4 }}
      style={{
        flex: '1 1 280px',
        minWidth: 260,
      }}
    >
      <GameCard
        sx={{ height: '100%', cursor: 'pointer' }}
        onClick={onToggle}
        role="button"
        tabIndex={0}
        aria-expanded={expanded}
        onKeyDown={(event: React.KeyboardEvent) => {
          if (event.key === 'Enter' || event.key === ' ') {
            event.preventDefault();
            onToggle();
          }
        }}
      >
        <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
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
            <VolumeTrendSparkline data={sparklineData} nivoTheme={nivoTheme} />
            <GameText
              variant="caption"
              sx={{ color: trendColor, flexShrink: 0, ml: 'auto' }}
              aria-label={
                card.priorPeriodDeltaPercent == null
                  ? `${card.type} trend unavailable`
                  : `${card.type} trend ${card.priorPeriodDeltaPercent >= 0 ? 'up' : 'down'} ${Math.abs(card.priorPeriodDeltaPercent)} percent`
              }
            >
              {card.priorPeriodDeltaPercent == null
                ? '—'
                : `${card.priorPeriodDeltaPercent >= 0 ? '↗' : '↘'} ${Math.abs(card.priorPeriodDeltaPercent)}%`}
            </GameText>
          </Box>

          <Box
            sx={{ display: 'flex', alignItems: 'baseline', flexWrap: 'wrap', gap: 0.5, mb: 0.5 }}
          >
            <GameText variant="h3" className={GAME_CLASSES.textBold} sx={{ lineHeight: 1 }}>
              {currentLabel}
            </GameText>
            <GameText variant="body2" className={GAME_CLASSES.textMuted} sx={{ lineHeight: 1 }}>
              / {targetLabel}
            </GameText>
          </Box>

          {card.hasBaseline && card.status !== 'no_volume' ? (
            <GameText
              variant="caption"
              className={GAME_CLASSES.textMuted}
              sx={{ display: 'block', mb: 1 }}
            >
              {deltaPrefix}
              {formatCompactVolume(Math.abs(card.deltaAbsolute), preferredUnit)} ({deltaPrefix}
              {card.deltaPercent}%) vs target
            </GameText>
          ) : null}

          {card.emptyMessage ? (
            <GameText
              variant="caption"
              className={GAME_CLASSES.textMuted}
              sx={{ display: 'block', mb: 1 }}
            >
              {card.emptyMessage}
            </GameText>
          ) : null}

          <Box sx={{ height: 40, width: '100%' }} data-testid={`volume-bullet-${card.type}`}>
            <ResponsiveBullet
              data={bulletData}
              margin={{ top: 4, right: 4, bottom: 4, left: 4 }}
              spacing={12}
              titleAlign="start"
              titleOffsetX={0}
              titleOffsetY={0}
              measureSize={0.45}
              markerSize={0.7}
              rangeColors={['#1a3333', '#244444', '#2d5555']}
              measureColors={[statusColor]}
              markerColors={['var(--game-cyan)']}
              theme={nivoTheme}
              animate={true}
              motionConfig="gentle"
            />
          </Box>

          <Collapse in={expanded}>
            {expanded ? (
              <Box>
                <Box
                  sx={{ height: 90, width: '100%', mt: 1.5 }}
                  data-testid={`volume-sparkline-${card.type}`}
                >
                  <ResponsiveLine
                    data={[
                      {
                        id: card.type,
                        data: sparklineData,
                      },
                    ]}
                    margin={{ top: 8, right: 8, bottom: 24, left: 28 }}
                    xScale={{ type: 'point' }}
                    yScale={{ type: 'linear', min: 0, max: 'auto' }}
                    curve="monotoneX"
                    axisTop={null}
                    axisRight={null}
                    axisBottom={{
                      tickSize: 0,
                      tickPadding: 4,
                    }}
                    axisLeft={{
                      tickSize: 0,
                      tickPadding: 4,
                      tickValues: 3,
                    }}
                    enableGridX={false}
                    enablePoints={true}
                    pointSize={5}
                    enableArea={true}
                    areaOpacity={0.15}
                    colors={['var(--game-cyan)']}
                    theme={nivoTheme}
                    animate={true}
                    motionConfig="gentle"
                  />
                </Box>
                <GameText variant="caption" className={GAME_CLASSES.textMuted}>
                  Weekly trend
                  {card.hasBaseline
                    ? ` · target ${formatCompactVolume(card.target, preferredUnit)}`
                    : ' · no baseline yet'}
                </GameText>
              </Box>
            ) : null}
          </Collapse>
        </CardContent>
      </GameCard>
    </motion.div>
  );
};
