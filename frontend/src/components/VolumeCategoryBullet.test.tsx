import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import React from 'react';

import { VolumeCategoryBullet } from './VolumeCategoryBullet';
import { createCongenNivoTheme } from '../theme/nivoTheme';
import { setupResizeObserverMock } from '../testUtils/setupResizeObserverMock';
import type { VolumeCategoryMetrics } from '../utils/volumeOverviewUtils';

beforeAll(() => {
  setupResizeObserverMock();
});

describe('VolumeCategoryBullet', () => {
  const card: VolumeCategoryMetrics = {
    type: 'Max Effort',
    current: 1200,
    target: 3500,
    hasTarget: true,
    sameWeekSlotAverage: 3100,
    sameWeekSlotSampleCount: 1,
    status: 'under',
    priorPeriodDeltaPercent: null,
    onTrackFloor: 0.9,
    poorEnd: 3150,
    okEnd: 3500,
    goodEnd: 4025,
    scaleMax: 4700,
    loggingIncomplete: false,
    isOvershoot: false,
  };

  it('renders four range bands and axis ticks from card ends', async () => {
    render(
      <VolumeCategoryBullet
        card={card}
        preferredUnit="LBS"
        nivoTheme={createCongenNivoTheme('dark')}
        doneLabel="1.2k lbs"
        planLabel="3.5k lbs"
      />
    );

    expect(screen.getByTestId('volume-bullet-Max Effort')).toBeInTheDocument();
    await waitFor(() => {
      expect(screen.getAllByTestId(/congen-bullet-range-Max Effort-/).length).toBe(4);
    });
    expect(screen.getByTestId('congen-bullet-axis')).toBeInTheDocument();
  });

  it('shows the volume bullet tooltip on range hover', async () => {
    render(
      <VolumeCategoryBullet
        card={card}
        preferredUnit="LBS"
        nivoTheme={createCongenNivoTheme('dark')}
        doneLabel="1.2k lbs"
        planLabel="3.5k lbs"
      />
    );

    await waitFor(() => {
      expect(screen.getByTestId('congen-bullet-range-Max Effort-0')).toBeInTheDocument();
    });
    fireEvent.mouseEnter(screen.getByTestId('congen-bullet-range-Max Effort-0'));
    expect(await screen.findByTestId('volume-bullet-tooltip')).toBeInTheDocument();
  });
});
