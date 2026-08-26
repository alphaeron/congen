import RemoveIcon from '@mui/icons-material/Remove';
import TrendingDownIcon from '@mui/icons-material/TrendingDown';
import TrendingUpIcon from '@mui/icons-material/TrendingUp';
import { Box, CardContent, Chip, Divider, Grid, Tooltip, useTheme } from '@mui/material';
import React, { useMemo } from 'react';

import { GameCard, GameText, GameTextSecondary, GAME_CLASSES } from './GameTheme';
import { VOLUME_SERIES_COLORS } from './volumeOverviewTheme';
import { VolumeTrendSparkline } from './VolumeTrendSparkline';
import type { UserOneRepMax } from '../api/types';
import { replaceUnderscoresWithSpaces, formatWeightWithUnit } from '../common/utils';
import { createCongenNivoTheme } from '../theme/nivoTheme';
import type { MeTopSetResult, WeekKeyResults } from '../utils/performanceAnalyticsUtils';
import {
  computeMeSetIntensityPercent,
  formatCategoryVolume,
  formatVolumeDeltaLabel,
  formatVolumePriorComparison,
  getVolumeDeltaColor,
  getVolumeDeltaTone,
} from '../utils/performanceAnalyticsUtils';
import type { VolumeCategory, VolumeTrendPoint } from '../utils/volumeOverviewUtils';

export interface WeekKeyResultsSummaryProps {
  results: WeekKeyResults;
  preferredUnit: 'KG' | 'LBS';
  userOneRepMaxes?: UserOneRepMax[];
  totalVolumeTrend?: VolumeTrendPoint[];
  onWorkoutClick?: (workoutId: number) => void;
  onExerciseClick?: (exerciseName: string) => void;
}

const CATEGORY_VOLUME_ROWS: Array<{
  label: VolumeCategory;
  shortLabel: string;
  key: keyof WeekKeyResults['volume'];
  color: string;
}> = [
  { label: 'Max Effort', shortLabel: 'ME', key: 'maxEffort', color: VOLUME_SERIES_COLORS.volume },
  {
    label: 'Dynamic Effort',
    shortLabel: 'DE',
    key: 'dynamicEffort',
    color: VOLUME_SERIES_COLORS.acwr,
  },
  {
    label: 'Accessory',
    shortLabel: 'Acc',
    key: 'accessory',
    color: VOLUME_SERIES_COLORS.intensity,
  },
];

function DeltaIcon({
  tone,
}: {
  tone: ReturnType<typeof getVolumeDeltaTone>;
}): React.ReactElement | null {
  if (tone === 'positive') {
    return <TrendingUpIcon sx={{ fontSize: 16 }} />;
  }
  if (tone === 'negative') {
    return <TrendingDownIcon sx={{ fontSize: 16 }} />;
  }
  if (tone === 'flat') {
    return <RemoveIcon sx={{ fontSize: 16 }} />;
  }
  return null;
}

function VolumeComparisonBar({
  current,
  prior,
  color,
}: {
  current: number;
  prior: number;
  color: string;
}): React.ReactElement {
  const scaleMax = Math.max(current, prior, 1);
  const currentWidth = Math.round((current / scaleMax) * 100);
  const priorWidth = Math.round((prior / scaleMax) * 100);

  return (
    <Box
      sx={{
        position: 'relative',
        height: 6,
        borderRadius: 999,
        backgroundColor: 'rgba(255,255,255,0.08)',
      }}
      aria-hidden="true"
    >
      {prior > 0 ? (
        <Box
          sx={{
            position: 'absolute',
            inset: 0,
            width: `${priorWidth}%`,
            borderRadius: 999,
            border: '1px dashed rgba(255,255,255,0.25)',
          }}
        />
      ) : null}
      <Box
        sx={{
          position: 'absolute',
          top: 0,
          left: 0,
          height: '100%',
          width: `${currentWidth}%`,
          borderRadius: 999,
          backgroundColor: color,
          opacity: current > 0 ? 0.9 : 0,
        }}
      />
    </Box>
  );
}

function MeTopSetCard({
  set,
  preferredUnit,
  userOneRepMaxes,
  onWorkoutClick,
  onExerciseClick,
}: {
  set: MeTopSetResult;
  preferredUnit: 'KG' | 'LBS';
  userOneRepMaxes?: UserOneRepMax[];
  onWorkoutClick?: (workoutId: number) => void;
  onExerciseClick?: (exerciseName: string) => void;
}): React.ReactElement {
  const intensityPercent = computeMeSetIntensityPercent(
    set.weightKg,
    set.exerciseName,
    userOneRepMaxes
  );
  const workoutLabel = replaceUnderscoresWithSpaces(set.workoutName);

  return (
    <Box
      data-testid={`week-me-top-set-${set.workoutId}-${set.exerciseName}`}
      sx={{
        p: 1.5,
        borderRadius: 1,
        border: 1,
        borderColor: 'var(--game-cyan-border)',
        backgroundColor: 'rgba(0, 188, 212, 0.06)',
        cursor: onWorkoutClick || onExerciseClick ? 'pointer' : 'default',
        transition: 'var(--game-transition)',
        '&:hover':
          onWorkoutClick || onExerciseClick
            ? {
                borderColor: 'var(--game-cyan)',
                boxShadow: 'var(--game-cyan-shadow)',
              }
            : undefined,
      }}
      onClick={() => onWorkoutClick?.(set.workoutId)}
      onKeyDown={
        onWorkoutClick
          ? (event: React.KeyboardEvent) => {
              if (event.key === 'Enter' || event.key === ' ') {
                event.preventDefault();
                onWorkoutClick(set.workoutId);
              }
            }
          : undefined
      }
      role={onWorkoutClick ? 'button' : undefined}
      tabIndex={onWorkoutClick ? 0 : undefined}
    >
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 0.75 }}>
        <Chip
          label={`Day ${set.dayInWeek}`}
          size="small"
          sx={{
            height: 22,
            backgroundColor: 'var(--game-cyan-light)',
            color: 'var(--game-cyan)',
            border: '1px solid var(--game-cyan-border)',
          }}
        />
        <GameTextSecondary variant="caption">{workoutLabel}</GameTextSecondary>
      </Box>
      <GameText
        variant="h6"
        className={GAME_CLASSES.textMedium}
        sx={{ color: 'var(--game-cyan)', lineHeight: 1.2 }}
      >
        {formatWeightWithUnit(set.weightKg, preferredUnit)} × {set.reps}
      </GameText>
      <Box
        sx={{ display: 'flex', alignItems: 'center', gap: 1, flexWrap: 'wrap', mt: 0.5 }}
        onClick={
          onExerciseClick
            ? (event: React.MouseEvent) => {
                event.stopPropagation();
                onExerciseClick(set.exerciseName);
              }
            : undefined
        }
      >
        <GameText variant="body2">{set.exerciseName}</GameText>
        {intensityPercent != null ? (
          <Chip
            label={`${intensityPercent}% 1RM`}
            size="small"
            sx={{
              height: 22,
              backgroundColor: 'rgba(34, 197, 94, 0.15)',
              color: 'var(--game-success)',
            }}
          />
        ) : null}
      </Box>
    </Box>
  );
}

/**
 * Summarizes logged ME top sets and week-over-week volume for a program week.
 */
export const WeekKeyResultsSummary: React.FC<WeekKeyResultsSummaryProps> = ({
  results,
  preferredUnit,
  userOneRepMaxes,
  totalVolumeTrend = [],
  onWorkoutClick,
  onExerciseClick,
}) => {
  const theme = useTheme();
  const nivoTheme = useMemo(() => createCongenNivoTheme(theme.palette.mode), [theme.palette.mode]);

  const totalDeltaPercent = results.volumeDeltaPercent?.total ?? null;
  const totalPriorVolume = results.priorWeekVolume?.total ?? null;
  const totalTone = getVolumeDeltaTone(results.volume.total, totalPriorVolume, totalDeltaPercent);
  const totalDeltaLabel = formatVolumeDeltaLabel(totalTone, totalDeltaPercent);
  const totalPriorCaption = formatVolumePriorComparison(totalPriorVolume, preferredUnit);

  return (
    <GameCard className="glassmorphism-card" data-testid="week-key-results-summary">
      <CardContent sx={{ p: 2 }}>
        <Box
          sx={{
            display: 'flex',
            alignItems: 'flex-start',
            justifyContent: 'space-between',
            gap: 2,
            mb: 2,
          }}
        >
          <Box sx={{ minWidth: 0 }}>
            <GameText
              variant="h6"
              className={GAME_CLASSES.textMedium}
              sx={{ color: 'var(--game-cyan)', textShadow: '0 0 10px var(--game-cyan)', mb: 0.5 }}
            >
              Week at a Glance
            </GameText>
            <GameTextSecondary variant="caption">
              Logged volume and max effort peaks for week {results.weekNumber}
            </GameTextSecondary>
          </Box>
          {totalVolumeTrend.length > 1 ? (
            <VolumeTrendSparkline
              data={totalVolumeTrend}
              nivoTheme={nivoTheme}
              ariaLabel={`Total weekly volume trend across ${totalVolumeTrend.length} weeks`}
              interactive={false}
            />
          ) : null}
        </Box>

        <Box
          sx={{
            p: 2,
            mb: 2,
            borderRadius: 1,
            border: 1,
            borderColor: 'var(--game-cyan-border)',
            backgroundColor: 'rgba(0, 188, 212, 0.05)',
          }}
          data-testid="week-key-results-total-volume"
        >
          <GameTextSecondary variant="caption" sx={{ display: 'block', mb: 0.5 }}>
            Total Logged Volume
          </GameTextSecondary>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, flexWrap: 'wrap' }}>
            <GameText
              variant="h4"
              className={GAME_CLASSES.textBold}
              sx={{ color: 'var(--game-cyan)', lineHeight: 1.1 }}
            >
              {formatCategoryVolume(results.volume.total, preferredUnit)}
            </GameText>
            <Chip
              icon={<DeltaIcon tone={totalTone} />}
              label={totalDeltaLabel}
              size="small"
              sx={{
                color: getVolumeDeltaColor(totalTone),
                borderColor: getVolumeDeltaColor(totalTone),
                backgroundColor: 'rgba(255,255,255,0.04)',
              }}
              variant="outlined"
            />
          </Box>
          {totalPriorCaption ? (
            <GameTextSecondary variant="caption" sx={{ display: 'block', mt: 0.5 }}>
              {totalPriorCaption}
            </GameTextSecondary>
          ) : null}
        </Box>

        <Grid container spacing={2}>
          <Grid size={{ xs: 12, md: 6 }}>
            <GameText variant="subtitle2" className={GAME_CLASSES.marginBottom1}>
              Max Effort Peaks
            </GameText>
            {results.meTopSets.length === 0 ? (
              <GameTextSecondary variant="body2">
                No ME top sets logged yet. Complete a max effort day to see your peak lifts here.
              </GameTextSecondary>
            ) : (
              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.5 }}>
                {results.meTopSets.map(set => (
                  <MeTopSetCard
                    key={`${set.workoutId}-${set.exerciseName}`}
                    set={set}
                    preferredUnit={preferredUnit}
                    userOneRepMaxes={userOneRepMaxes}
                    onWorkoutClick={onWorkoutClick}
                    onExerciseClick={onExerciseClick}
                  />
                ))}
              </Box>
            )}
          </Grid>

          <Grid size={{ xs: 12, md: 6 }}>
            <Tooltip
              title="Compares logged volume to the previous calendar week. Rotation may affect DE and accessory totals."
              arrow
            >
              <GameText
                variant="subtitle2"
                className={GAME_CLASSES.marginBottom1}
                sx={{ width: 'fit-content' }}
              >
                Logged Volume · vs Last Week
              </GameText>
            </Tooltip>
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.25 }}>
              {CATEGORY_VOLUME_ROWS.map(row => {
                const current = results.volume[row.key];
                const prior = results.priorWeekVolume?.[row.key] ?? null;
                const delta =
                  row.key === 'maxEffort'
                    ? results.volumeDeltaPercent?.maxEffort
                    : row.key === 'dynamicEffort'
                      ? results.volumeDeltaPercent?.dynamicEffort
                      : results.volumeDeltaPercent?.accessory;
                const tone = getVolumeDeltaTone(current, prior, delta ?? null);
                const deltaLabel = formatVolumeDeltaLabel(tone, delta ?? null);
                const priorCaption = formatVolumePriorComparison(prior, preferredUnit);

                return (
                  <Box
                    key={row.label}
                    data-testid={`week-volume-row-${row.label.replace(/\s+/g, '-').toLowerCase()}`}
                  >
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 0.5 }}>
                      <Box
                        sx={{
                          width: 8,
                          height: 8,
                          borderRadius: '50%',
                          backgroundColor: row.color,
                          flexShrink: 0,
                        }}
                      />
                      <GameText variant="body2" sx={{ flex: 1 }}>
                        {row.shortLabel}
                      </GameText>
                      <GameText variant="body2" className={GAME_CLASSES.textMedium}>
                        {formatCategoryVolume(current, preferredUnit)}
                      </GameText>
                    </Box>
                    <VolumeComparisonBar current={current} prior={prior ?? 0} color={row.color} />
                    <Box
                      sx={{
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'space-between',
                        gap: 1,
                        mt: 0.5,
                      }}
                    >
                      {priorCaption ? (
                        <GameTextSecondary variant="caption">{priorCaption}</GameTextSecondary>
                      ) : (
                        <Box />
                      )}
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                        <DeltaIcon tone={tone} />
                        <GameTextSecondary
                          variant="caption"
                          sx={{ color: getVolumeDeltaColor(tone) }}
                        >
                          {deltaLabel}
                        </GameTextSecondary>
                      </Box>
                    </Box>
                  </Box>
                );
              })}
            </Box>
          </Grid>
        </Grid>

        <Divider sx={{ my: 2, borderColor: 'var(--game-cyan-border)' }} />

        <GameTextSecondary variant="caption">
          ME peaks show your heaviest logged set per max effort workout. Volume compares calendar
          weeks only.
        </GameTextSecondary>
      </CardContent>
    </GameCard>
  );
};
