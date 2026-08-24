import CloseIcon from '@mui/icons-material/Close';
import { Box, CardContent, Dialog, IconButton, Tooltip, useTheme } from '@mui/material';
import { ResponsiveLine } from '@nivo/line';
import { motion } from 'framer-motion';
import React, { useMemo, useState } from 'react';

import { CongenBullet } from './CongenBullet';
import { GameCard, GameText, GAME_CLASSES } from './GameTheme';
import type { UserDataExport, Exercise, UserOneRepMax } from '../api/types';
import { createCongenNivoTheme } from '../theme/nivoTheme';
import type {
  VolumeStatus,
  VolumeCategoryMetrics,
  WeekVolumeTotals,
  VolumeTrendPoint,
} from '../utils/volumeOverviewUtils';
import {
  buildVolumeOverviewModel,
  buildBulletAxisLabels,
  buildWeeklyAcwrSeries,
  buildWeeklyIntensitySeries,
  formatCompactVolume,
} from '../utils/volumeOverviewUtils';

interface VolumeOverviewCardsProps {
  userDataExport: UserDataExport | null;
  exerciseData: Map<string, Exercise>;
  workoutsPerWeek: number;
  currentWeek: number;
  preferredUnit?: 'KG' | 'LBS';
}

const BAND_COLORS = {
  poor: '#5c2b2b',
  ok: '#5c4a1f',
  good: '#1f4d3a',
  overload: '#6b2a2a',
};

const BULLET_MARGIN = { top: 10, right: 8, bottom: 40, left: 8 };

const MARKER_PLAN = 'var(--game-cyan)';
const MARKER_WEEK_AVG = '#f0c14a';

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

function formatCardVolume(volume: number, preferredUnit: 'KG' | 'LBS'): string {
  if (volume === 0) {
    return formatCompactVolume(0, preferredUnit);
  }
  return formatCompactVolume(volume, preferredUnit);
}

function getSparklineAriaLabel(
  category: string,
  priorPeriodDeltaPercent: number | null
): string {
  if (priorPeriodDeltaPercent == null) {
    return `${category} weekly volume trend. Click to expand.`;
  }
  const direction = priorPeriodDeltaPercent >= 0 ? 'up' : 'down';
  return `${category} weekly volume trend, ${direction} ${Math.abs(priorPeriodDeltaPercent)} percent versus last week. Click to expand.`;
}

const SERIES_COLORS = {
  volume: 'var(--game-cyan)',
  acwr: '#f0c14a',
  intensity: 'var(--game-success)',
};

/**
 * Compact sparkline for card header trend display.
 */
const VolumeTrendSparkline: React.FC<{
  data: VolumeTrendPoint[];
  nivoTheme: ReturnType<typeof createCongenNivoTheme>;
  ariaLabel: string;
  onClick: () => void;
}> = ({ data, nivoTheme, ariaLabel, onClick }) => (
  <Tooltip title="Click to expand" arrow>
    <Box
      sx={{
        width: 88,
        height: 28,
        flexShrink: 0,
        ml: 'auto',
        cursor: 'pointer',
        borderRadius: 1,
        '&:hover': {
          backgroundColor: 'rgba(0, 188, 212, 0.08)',
        },
      }}
      data-testid="volume-trend-sparkline"
      role="button"
      tabIndex={0}
      aria-label={ariaLabel}
      onClick={(event: React.MouseEvent) => {
        event.stopPropagation();
        onClick();
      }}
      onKeyDown={(event: React.KeyboardEvent) => {
        if (event.key === 'Enter' || event.key === ' ') {
          event.preventDefault();
          event.stopPropagation();
          onClick();
        }
      }}
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
        colors={[SERIES_COLORS.volume]}
        lineWidth={2}
        theme={nivoTheme}
        animate={true}
        motionConfig="gentle"
      />
    </Box>
  </Tooltip>
);

/**
 * Expanded weekly trend dialog with volume, ACWR, and intensity series.
 */
const VolumeTrendChartDialog: React.FC<{
  open: boolean;
  category: string;
  volumeData: VolumeTrendPoint[];
  acwrData: VolumeTrendPoint[];
  intensityData: VolumeTrendPoint[];
  nivoTheme: ReturnType<typeof createCongenNivoTheme>;
  onClose: () => void;
}> = ({
  open,
  category,
  volumeData,
  acwrData,
  intensityData,
  nivoTheme,
  onClose,
}) => {
  const series = useMemo(() => {
    const lines: Array<{ id: string; data: VolumeTrendPoint[] }> = [
      { id: 'Volume', data: volumeData },
    ];
    if (acwrData.length > 0) {
      lines.push({ id: 'ACWR', data: acwrData });
    }
    if (intensityData.length > 0) {
      lines.push({ id: 'Intensity', data: intensityData });
    }
    return lines;
  }, [volumeData, acwrData, intensityData]);

  const seriesColors = series.map(line => {
    if (line.id === 'ACWR') {
      return SERIES_COLORS.acwr;
    }
    if (line.id === 'Intensity') {
      return SERIES_COLORS.intensity;
    }
    return SERIES_COLORS.volume;
  });

  const secondarySeries = useMemo(() => {
    const lines: Array<{ id: string; data: VolumeTrendPoint[] }> = [];
    const scaleAcwrToPercent = intensityData.length > 0;
    if (acwrData.length > 0) {
      lines.push({
        id: 'ACWR',
        data: scaleAcwrToPercent
          ? acwrData.map(point => ({
              x: point.x,
              y: Math.round(point.y * 10000) / 100,
            }))
          : acwrData,
      });
    }
    if (intensityData.length > 0) {
      lines.push({ id: 'Intensity', data: intensityData });
    }
    return lines;
  }, [acwrData, intensityData]);

  const secondaryColors = secondarySeries.map(line =>
    line.id === 'Intensity' ? SERIES_COLORS.intensity : SERIES_COLORS.acwr
  );

  const acwrByWeek = useMemo(() => {
    const map = new Map<string, number>();
    acwrData.forEach(point => map.set(point.x, point.y));
    return map;
  }, [acwrData]);

  const intensityByWeek = useMemo(() => {
    const map = new Map<string, number>();
    intensityData.forEach(point => map.set(point.x, point.y));
    return map;
  }, [intensityData]);

  return (
    <Dialog
      open={open}
      onClose={onClose}
      maxWidth="sm"
      fullWidth
      aria-labelledby="volume-trend-dialog-title"
      data-testid={`volume-trend-dialog-${category}`}
    >
      <Box sx={{ position: 'relative', p: 2, pt: 2.5 }}>
        <IconButton
          aria-label="Close volume trend chart"
          onClick={onClose}
          sx={{
            position: 'absolute',
            top: 8,
            right: 8,
            color: 'text.secondary',
          }}
          data-testid={`volume-trend-dialog-close-${category}`}
        >
          <CloseIcon />
        </IconButton>
        <GameText
          id="volume-trend-dialog-title"
          variant="h6"
          className={GAME_CLASSES.textMedium}
          sx={{ pr: 5, mb: 2 }}
        >
          {category} weekly trend
        </GameText>
        <Box
          sx={{ height: 260, width: '100%', position: 'relative' }}
          data-testid={`volume-trend-dialog-chart-${category}`}
        >
          <Box sx={{ position: 'absolute', inset: 0 }}>
            <ResponsiveLine
              data={[{ id: 'Volume', data: volumeData }]}
              margin={{ top: 28, right: 48, bottom: 40, left: 48 }}
              xScale={{ type: 'point' }}
              yScale={{ type: 'linear', min: 0, max: 'auto' }}
              curve="monotoneX"
              axisTop={null}
              axisRight={null}
              axisBottom={{
                tickSize: 0,
                tickPadding: 8,
              }}
              axisLeft={{
                tickSize: 0,
                tickPadding: 8,
                tickValues: 4,
              }}
              enableGridX={false}
              enablePoints={true}
              pointSize={6}
              enableArea={true}
              areaOpacity={0.15}
              colors={[SERIES_COLORS.volume]}
              theme={nivoTheme}
              animate={true}
              motionConfig="gentle"
              useMesh={true}
              legends={[]}
              tooltip={({ point }) => {
                const week = String(point.data.x);
                const volume = Number(point.data.y);
                const acwr = acwrByWeek.get(week);
                const intensity = intensityByWeek.get(week);
                return (
                  <div
                    data-testid="volume-trend-dialog-tooltip"
                    style={{
                      padding: '8px 12px',
                      color: nivoTheme.tooltip.container.color,
                      background: nivoTheme.tooltip.container.background,
                      borderRadius: nivoTheme.tooltip.container.borderRadius,
                      boxShadow: nivoTheme.tooltip.container.boxShadow,
                      border: nivoTheme.tooltip.container.border,
                      whiteSpace: 'nowrap',
                      fontSize: nivoTheme.tooltip.container.fontSize,
                      fontFamily: '"Inter", "Roboto", "Helvetica", "Arial", sans-serif',
                      lineHeight: '1.45',
                    }}
                  >
                    <div>
                      Week: <strong>{week}</strong>
                    </div>
                    <div>
                      Volume: <strong>{Math.round(volume).toLocaleString()}</strong>
                    </div>
                    <div>
                      ACWR:{' '}
                      <strong>{acwr != null ? acwr.toFixed(2) : 'no data'}</strong>
                    </div>
                    <div>
                      Intensity:{' '}
                      <strong>{intensity != null ? `${intensity}%` : 'no data'}</strong>
                    </div>
                  </div>
                );
              }}
            />
          </Box>
          {secondarySeries.length > 0 && (
            <Box
              sx={{ position: 'absolute', inset: 0, pointerEvents: 'none' }}
              data-testid={`volume-trend-dialog-secondary-${category}`}
            >
              <ResponsiveLine
                data={secondarySeries}
                margin={{ top: 28, right: 48, bottom: 40, left: 48 }}
                xScale={{ type: 'point' }}
                yScale={{ type: 'linear', min: 0, max: 'auto' }}
                curve="monotoneX"
                axisTop={null}
                axisLeft={null}
                axisBottom={null}
                axisRight={{
                  tickSize: 0,
                  tickPadding: 8,
                  tickValues: 4,
                }}
                enableGridX={false}
                enableGridY={false}
                enablePoints={true}
                pointSize={5}
                enableArea={false}
                colors={secondaryColors}
                theme={nivoTheme}
                animate={true}
                motionConfig="gentle"
                isInteractive={false}
                legends={[]}
              />
            </Box>
          )}
          <Box
            sx={{
              position: 'absolute',
              top: 0,
              left: 48,
              right: 48,
              display: 'flex',
              justifyContent: 'center',
              gap: 2,
              flexWrap: 'wrap',
            }}
            data-testid={`volume-trend-dialog-legend-${category}`}
          >
            {series.map((line, index) => (
              <GameText
                key={line.id}
                variant="caption"
                sx={{
                  color: seriesColors[index],
                  display: 'flex',
                  alignItems: 'center',
                  gap: 0.5,
                }}
              >
                <Box
                  component="span"
                  sx={{
                    width: 10,
                    height: 10,
                    borderRadius: '50%',
                    backgroundColor: seriesColors[index],
                    display: 'inline-block',
                  }}
                />
                {line.id}
                {line.id === 'ACWR' && intensityData.length > 0 ? ' %' : ''}
              </GameText>
            ))}
          </Box>
        </Box>
      </Box>
    </Dialog>
  );
};

interface VolumeBulletTooltipProps {
  card: VolumeCategoryMetrics;
  doneLabel: string;
  planLabel: string;
  preferredUnit: 'KG' | 'LBS';
  nivoTheme: ReturnType<typeof createCongenNivoTheme>;
}

/**
 * Single labeled tooltip row with a fixed layout for stable positioning.
 */
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
const VolumeBulletTooltip: React.FC<VolumeBulletTooltipProps> = ({
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
      style={{
        padding: '8px 12px',
        color: nivoTheme.tooltip.container.color,
        background: nivoTheme.tooltip.container.background,
        borderRadius: nivoTheme.tooltip.container.borderRadius,
        boxShadow: nivoTheme.tooltip.container.boxShadow,
        border: nivoTheme.tooltip.container.border,
        whiteSpace: 'nowrap',
        fontSize: nivoTheme.tooltip.container.fontSize,
        fontFamily: '"Inter", "Roboto", "Helvetica", "Arial", sans-serif',
        lineHeight: '1.45',
        position: 'relative',
        zIndex: 2000,
        pointerEvents: 'none',
      }}
    >
      <VolumeBulletTooltipRow label="Done" value={doneValue} />
      <VolumeBulletTooltipRow label="Target" value={planValue} />
      <VolumeBulletTooltipRow label="Average Volume" value={weekAvgValue} />
    </div>
  );
};

/**
 * Volume overview KPI cards with plan-relative bullets, W{{n}} history, and load insights.
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
      style={{ display: 'flex', flexDirection: 'column', gap: 12, overflow: 'visible' }}
    >
      <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap', overflow: 'visible' }}>
        {model.categories.map((card, index) => (
          <VolumeCategoryCard
            key={card.type}
            card={card}
            index={index}
            preferredUnit={preferredUnit}
            nivoTheme={nivoTheme}
            weekVolumes={model.weekVolumes}
            userOneRepMaxes={userDataExport?.user_one_rep_max as UserOneRepMax[] | undefined}
          />
        ))}
      </Box>
    </motion.div>
  );
};

interface VolumeCategoryCardProps {
  card: VolumeCategoryMetrics;
  index: number;
  preferredUnit: 'KG' | 'LBS';
  nivoTheme: ReturnType<typeof createCongenNivoTheme>;
  weekVolumes: WeekVolumeTotals[];
  userOneRepMaxes: UserOneRepMax[] | undefined;
}

/**
 * Single volume category KPI card with bullet chart and expandable trend dialog.
 */
const VolumeCategoryCard: React.FC<VolumeCategoryCardProps> = ({
  card,
  index,
  preferredUnit,
  nivoTheme,
  weekVolumes,
  userOneRepMaxes,
}) => {
  const [chartOpen, setChartOpen] = useState(false);
  const statusColor = getStatusColor(card.status);
  const currentLabel = formatCardVolume(card.current, preferredUnit);
  const planLabel = card.hasTarget ? formatCompactVolume(card.target, preferredUnit) : 'No plan';
  const scaleMax = card.scaleMax;

  const sparklineData = useMemo(
    () =>
      weekVolumes.map(week => ({
        x: `W${week.weekNumber}`,
        y:
          card.type === 'Max Effort'
            ? week.maxEffortVolume
            : card.type === 'Dynamic Effort'
              ? week.dynamicEffortVolume
              : week.accessoryVolume,
      })),
    [weekVolumes, card.type]
  );

  const acwrData = useMemo(
    () => buildWeeklyAcwrSeries(weekVolumes, card.type),
    [weekVolumes, card.type]
  );

  const intensityData = useMemo(
    () =>
      card.type === 'Max Effort'
        ? buildWeeklyIntensitySeries(weekVolumes, userOneRepMaxes)
        : [],
    [weekVolumes, userOneRepMaxes, card.type]
  );

  const markers: number[] = [];
  const markerColors: string[] = [];
  if (card.hasTarget) {
    markers.push(Math.min(card.target, scaleMax));
    markerColors.push(MARKER_PLAN);
  }
  if (card.sameWeekSlotAverage != null && card.sameWeekSlotAverage > 0) {
    markers.push(Math.min(card.sameWeekSlotAverage, scaleMax));
    markerColors.push(MARKER_WEEK_AVG);
  }

  const axisLabels = buildBulletAxisLabels({
    scaleMax,
    poorEnd: card.poorEnd,
    okEnd: card.okEnd,
    goodEnd: card.goodEnd,
    hasTarget: card.hasTarget,
    preferredUnit,
  });

  const measureValue = Math.min(
    scaleMax,
    card.loggingIncomplete ? 0 : card.current
  );

  const bulletRanges = card.hasTarget
    ? [card.poorEnd, card.okEnd, card.goodEnd, scaleMax]
    : [
        Math.round(scaleMax * card.onTrackFloor),
        Math.round(scaleMax * 0.5),
        Math.round(scaleMax * 0.75),
        scaleMax,
      ];

  const bulletData = [
    {
      id: card.type,
      title: '',
      ranges: bulletRanges,
      measures: [measureValue],
      markers,
    },
  ];

  const measureColor = card.loggingIncomplete ? 'var(--game-white-muted)' : statusColor;
  const axisTickValues = axisLabels.map(label => label.value);
  const axisFormat = (value: number): string => {
    const match = axisLabels.find(label => label.value === value);
    return match?.text ?? formatCompactVolume(value, preferredUnit).replace(/ (lbs|kg)$/, '');
  };

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
              ariaLabel={getSparklineAriaLabel(card.type, card.priorPeriodDeltaPercent)}
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

          <Box
            sx={{ width: '100%', overflow: 'visible', position: 'relative', zIndex: 2 }}
            data-testid={`volume-bullet-${card.type}`}
            aria-label={`${card.type}: done ${currentLabel}, plan ${planLabel}`}
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
                data={bulletData}
                margin={BULLET_MARGIN}
                minValue={0}
                maxValue={scaleMax}
                measureSize={0.45}
                markerSize={0.75}
                rangeColors={[
                  BAND_COLORS.poor,
                  BAND_COLORS.ok,
                  BAND_COLORS.good,
                  BAND_COLORS.overload,
                ]}
                measureColors={[measureColor]}
                markerColors={markerColors.length > 0 ? markerColors : [MARKER_PLAN]}
                axisBottom={{
                  tickValues: axisTickValues,
                  format: axisFormat,
                  tickSize: 14,
                  tickPadding: 6,
                }}
                tooltip={() => (
                  <VolumeBulletTooltip
                    card={card}
                    doneLabel={currentLabel}
                    planLabel={planLabel}
                    preferredUnit={preferredUnit}
                    nivoTheme={nivoTheme}
                  />
                )}
              />
            </Box>
          </Box>
        </CardContent>
      </GameCard>

      <VolumeTrendChartDialog
        open={chartOpen}
        category={card.type}
        volumeData={sparklineData}
        acwrData={acwrData}
        intensityData={intensityData}
        nivoTheme={nivoTheme}
        onClose={() => setChartOpen(false)}
      />
    </motion.div>
    </Box>
  );
};
