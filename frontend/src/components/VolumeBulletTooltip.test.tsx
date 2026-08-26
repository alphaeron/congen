import { render, screen } from '@testing-library/react';
import React from 'react';

import { VolumeBulletTooltip } from './VolumeBulletTooltip';
import { createCongenNivoTheme } from '../theme/nivoTheme';
import type { VolumeCategoryMetrics } from '../utils/volumeOverviewUtils';

describe('VolumeBulletTooltip', () => {
  const card: VolumeCategoryMetrics = {
    type: 'Max Effort',
    current: 1000,
    target: 2000,
    hasTarget: true,
    sameWeekSlotAverage: 1800,
    sameWeekSlotSampleCount: 2,
    status: 'under',
    priorPeriodDeltaPercent: null,
    onTrackFloor: 0.9,
    poorEnd: 1800,
    okEnd: 2000,
    goodEnd: 2300,
    scaleMax: 2700,
    loggingIncomplete: false,
    isOvershoot: false,
  };

  it('renders Done, Target, and Average Volume rows', () => {
    render(
      <VolumeBulletTooltip
        card={card}
        doneLabel="1.0k lbs"
        planLabel="2.0k lbs"
        preferredUnit="LBS"
        nivoTheme={createCongenNivoTheme('dark')}
      />
    );

    const tooltip = screen.getByTestId('volume-bullet-tooltip');
    expect(tooltip).toHaveTextContent(/Done:/);
    expect(tooltip).toHaveTextContent(/Target:/);
    expect(tooltip).toHaveTextContent(/Average Volume:/);
    expect(tooltip).not.toHaveTextContent(/Overshoot/);
  });
});
