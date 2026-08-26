import CloseIcon from '@mui/icons-material/Close';
import { Box, Dialog, IconButton } from '@mui/material';
import { ResponsiveLine } from '@nivo/line';
import React, { useMemo } from 'react';

import { GameText, GAME_CLASSES } from './GameTheme';
import { VOLUME_SERIES_COLORS } from './volumeOverviewTheme';
import {
  getCongenNivoTooltipStyle,
  type CongenNivoTheme,
} from '../theme/nivoTheme';
import type {
  VolumeCategory,
  VolumeTrendPoint,
  WeekVolumeTotals,
} from '../utils/volumeOverviewUtils';
import { getWeeklyAcwrRatio } from '../utils/volumeOverviewUtils';

export interface VolumeTrendChartDialogProps {
  open: boolean;
  category: VolumeCategory;
  volumeData: VolumeTrendPoint[];
  acwrData: VolumeTrendPoint[];
  intensityData: VolumeTrendPoint[];
  weekVolumes: WeekVolumeTotals[];
  nivoTheme: CongenNivoTheme;
  onClose: () => void;
}

/**
 * Expanded weekly trend dialog with volume, ACWR, and intensity series.
 */
export const VolumeTrendChartDialog: React.FC<VolumeTrendChartDialogProps> = ({
  open,
  category,
  volumeData,
  acwrData,
  intensityData,
  weekVolumes,
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
      return VOLUME_SERIES_COLORS.acwr;
    }
    if (line.id === 'Intensity') {
      return VOLUME_SERIES_COLORS.intensity;
    }
    return VOLUME_SERIES_COLORS.volume;
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
    line.id === 'Intensity' ? VOLUME_SERIES_COLORS.intensity : VOLUME_SERIES_COLORS.acwr
  );

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
              colors={[VOLUME_SERIES_COLORS.volume]}
              theme={nivoTheme}
              animate={true}
              motionConfig="gentle"
              useMesh={true}
              legends={[]}
              tooltip={({ point }) => {
                const week = String(point.data.x);
                const volume = Number(point.data.y);
                const weekNumber = Number(week.replace(/^W/, ''));
                const acwr = getWeeklyAcwrRatio(weekVolumes, weekNumber, category);
                const intensity = intensityByWeek.get(week);
                return (
                  <div
                    data-testid="volume-trend-dialog-tooltip"
                    style={getCongenNivoTooltipStyle(nivoTheme)}
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
