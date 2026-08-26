import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import React from 'react';

import { VolumeOverviewCards } from './VolumeOverviewCards';
import { setupResizeObserverMock } from '../testUtils/setupResizeObserverMock';
import {
  createVolumeExerciseData,
  createVolumeUserDataExport,
  createVolumeWorkout,
} from '../testUtils/volumeOverviewFixtures';

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
      Mock Line
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
        return React.createElement(
          'div',
          { 'data-testid': 'motion-div', ...filteredProps },
          children
        );
      },
    },
  };
});

describe('VolumeOverviewCards', () => {
  const defaultProps = {
    userDataExport: createVolumeUserDataExport(1, [
      createVolumeWorkout(1, 1, 'ME Upper', 100, { performed: false }),
    ]),
    exerciseData: createVolumeExerciseData(),
    workoutsPerWeek: 4,
    currentWeek: 1,
    preferredUnit: 'LBS' as const,
  };

  it('renders three category cards without period controls or summary strip', () => {
    render(<VolumeOverviewCards {...defaultProps} />);

    expect(screen.getByText('Max Effort')).toBeInTheDocument();
    expect(screen.getByText('Dynamic Effort')).toBeInTheDocument();
    expect(screen.getByText('Accessory')).toBeInTheDocument();
    expect(screen.queryByText('This week')).not.toBeInTheDocument();
    expect(screen.queryByText('Sessions')).not.toBeInTheDocument();
  });

  it('renders bullets and exposes sparkline expand affordance', async () => {
    render(<VolumeOverviewCards {...defaultProps} />);

    expect(screen.getAllByTestId('congen-bullet').length).toBe(3);
    expect(screen.getAllByTestId('volume-trend-sparkline').length).toBe(3);
    expect(screen.getAllByLabelText(/Click to expand/i).length).toBeGreaterThan(0);
    await waitFor(() => {
      expect(screen.getAllByTestId(/congen-bullet-range-Max Effort-/).length).toBe(4);
    });
  });

  it('shows Done, Target, and Average Volume in the bullet tooltip', async () => {
    render(<VolumeOverviewCards {...defaultProps} />);

    await waitFor(() => {
      expect(screen.getByTestId('congen-bullet-range-Max Effort-0')).toBeInTheDocument();
    });
    fireEvent.mouseEnter(screen.getByTestId('congen-bullet-range-Max Effort-0'));

    const tooltip = await screen.findByTestId('volume-bullet-tooltip');
    expect(tooltip).toHaveTextContent(/Done:/);
    expect(tooltip).toHaveTextContent(/Target:/);
    expect(tooltip).toHaveTextContent(/Average Volume:/);
  });
});
