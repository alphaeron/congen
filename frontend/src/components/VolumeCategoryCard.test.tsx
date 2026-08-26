import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import React from 'react';

import { VolumeCategoryCard } from './VolumeCategoryCard';
import { createCongenNivoTheme } from '../theme/nivoTheme';
import { setupResizeObserverMock } from '../testUtils/setupResizeObserverMock';
import type { VolumeCategoryMetrics, WeekVolumeTotals } from '../utils/volumeOverviewUtils';

beforeAll(() => {
  setupResizeObserverMock();
});

jest.mock('@nivo/line', () => ({
  ResponsiveLine: ({
    tooltip,
    data,
  }: {
    tooltip?: (props: {
      point: { data: { x: string | number; y: number }; seriesColor: string };
    }) => React.ReactNode;
    data?: Array<{ id: string; data: Array<{ x: string; y: number }> }>;
  }) => (
    <div data-testid="responsive-line">
      {tooltip && data?.[0]?.data?.[0] ? (
        <div data-testid="line-tooltip-slot">
          {tooltip({
            point: {
              data: data[0].data[0],
              seriesColor: 'var(--game-cyan)',
            },
          })}
        </div>
      ) : null}
    </div>
  ),
}));

jest.mock('framer-motion', () => {
  // eslint-disable-next-line @typescript-eslint/no-require-imports
  const React = require('react');
  return {
    motion: {
      div: ({ children, ...props }: { children?: React.ReactNode; [key: string]: unknown }) => {
        const framerMotionProps = new Set([
          'whileHover',
          'whileTap',
          'initial',
          'animate',
          'transition',
          'variants',
        ]);
        const filteredProps = Object.fromEntries(
          Object.entries(props).filter(([key]) => !framerMotionProps.has(key))
        );
        return React.createElement('div', filteredProps, children);
      },
    },
  };
});

describe('VolumeCategoryCard', () => {
  const card: VolumeCategoryMetrics = {
    type: 'Max Effort',
    current: 1200,
    target: 3500,
    hasTarget: true,
    sameWeekSlotAverage: 3100,
    sameWeekSlotSampleCount: 1,
    status: 'under',
    priorPeriodDeltaPercent: 5,
    onTrackFloor: 0.9,
    poorEnd: 3150,
    okEnd: 3500,
    goodEnd: 4025,
    scaleMax: 4700,
    loggingIncomplete: false,
    isOvershoot: false,
  };

  const weekVolumes: WeekVolumeTotals[] = [
    {
      weekNumber: 1,
      maxEffortVolume: 1200,
      dynamicEffortVolume: 0,
      accessoryVolume: 0,
      totalVolume: 1200,
      maxEffortProgrammedVolume: 3500,
      dynamicEffortProgrammedVolume: 0,
      accessoryProgrammedVolume: 0,
      totalProgrammedVolume: 3500,
      performedSets: 1,
      targetSets: 1,
      completedWorkouts: 1,
      plannedWorkouts: 1,
      maxEffortPeakWeightLbs: 300,
      maxEffortPeakReps: 3,
      maxEffortPeakExerciseName: 'Bench Press',
    },
  ];

  it('opens and closes the trend dialog from the sparkline', async () => {
    render(
      <VolumeCategoryCard
        card={card}
        index={0}
        preferredUnit="LBS"
        nivoTheme={createCongenNivoTheme('dark')}
        weekVolumes={weekVolumes}
        userOneRepMaxes={[]}
      />
    );

    expect(screen.getByText('Max Effort')).toBeInTheDocument();
    fireEvent.click(screen.getByTestId('volume-trend-sparkline'));
    expect(screen.getByTestId('volume-trend-dialog-Max Effort')).toBeInTheDocument();

    fireEvent.click(screen.getByTestId('volume-trend-dialog-close-Max Effort'));
    await waitFor(() => {
      expect(screen.queryByTestId('volume-trend-dialog-Max Effort')).not.toBeInTheDocument();
    });
  });

  it('shows Over plan when the card is in overshoot', () => {
    render(
      <VolumeCategoryCard
        card={{ ...card, isOvershoot: true, status: 'overshoot' }}
        index={0}
        preferredUnit="LBS"
        nivoTheme={createCongenNivoTheme('dark')}
        weekVolumes={weekVolumes}
        userOneRepMaxes={[]}
      />
    );

    expect(screen.getByText('Over plan')).toBeInTheDocument();
  });
});
