import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import React from 'react';

import { VolumeOverviewCards } from './VolumeOverviewCards';

beforeAll(() => {
  class ResizeObserverMock {
    callback: ResizeObserverCallback;

    constructor(callback: ResizeObserverCallback) {
      this.callback = callback;
    }

    observe(target: Element) {
      this.callback(
        [
          {
            target,
            contentRect: {
              width: 320,
              height: 88,
              top: 0,
              left: 0,
              bottom: 88,
              right: 320,
              x: 0,
              y: 0,
              toJSON: () => ({}),
            },
            borderBoxSize: [],
            contentBoxSize: [],
            devicePixelContentBoxSize: [],
          } as ResizeObserverEntry,
        ],
        this
      );
    }

    unobserve() {}

    disconnect() {}
  }

  Object.defineProperty(window, 'ResizeObserver', {
    writable: true,
    configurable: true,
    value: ResizeObserverMock,
  });
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
  const mockUserDataExport = {
    training_programs: [
      {
        program: {
          id: 1,
          is_active: true,
          current_week_number: 1,
          name: 'Test Program',
          user_id: 'u1',
          created_at: '2023-01-01T00:00:00Z',
          updated_at: '2023-01-01T00:00:00Z',
        },
        workouts: [
          {
            workout: {
              id: 1,
              program_id: 1,
              day_number: 1,
              name: 'ME Upper',
              created_at: '2023-01-01T00:00:00Z',
              updated_at: '2023-01-01T00:00:00Z',
            },
            stages: [
              {
                stage: {
                  id: 1,
                  programmed_workout_id: 1,
                  name: 'Max Effort',
                  created_at: '2023-01-01T00:00:00Z',
                  updated_at: '2023-01-01T00:00:00Z',
                },
                exercises: [
                  {
                    exercise: {
                      id: 1,
                      workout_stage_id: 1,
                      exercise_name: 'Bench Press',
                      created_at: '2023-01-01T00:00:00Z',
                      updated_at: '2023-01-01T00:00:00Z',
                    },
                    set_schemes: [
                      {
                        id: 1,
                        programmed_exercise_id: 1,
                        set_number: 1,
                        performed_weight: undefined,
                        performed_rep_count: undefined,
                        target_weight: 100,
                        target_rep_count: 5,
                        created_at: '2023-01-01T00:00:00Z',
                        updated_at: '2023-01-01T00:00:00Z',
                      },
                    ],
                  },
                ],
              },
            ],
          },
        ],
      },
    ],
    user_one_rep_max: [],
  };

  const mockExerciseData = new Map([
    [
      'Bench Press',
      {
        name: 'Bench Press',
        description: 'Press',
        movement_type: 'horizontal_press',
        is_unilateral: false,
        is_upper: true,
        is_accessory: false,
      },
    ],
  ]);

  const defaultProps = {
    userDataExport: mockUserDataExport as never,
    exerciseData: mockExerciseData as never,
    workoutsPerWeek: 4,
    currentWeek: 1,
    preferredUnit: 'LBS' as const,
  };

  it('renders category cards without period controls or summary strip', () => {
    render(<VolumeOverviewCards {...defaultProps} />);

    expect(screen.getByText('Max Effort')).toBeInTheDocument();
    expect(screen.getByText('Dynamic Effort')).toBeInTheDocument();
    expect(screen.getByText('Accessory')).toBeInTheDocument();
    expect(screen.queryByText('This week')).not.toBeInTheDocument();
    expect(screen.queryByText('Last 4 weeks')).not.toBeInTheDocument();
    expect(screen.queryByText('Program')).not.toBeInTheDocument();
    expect(screen.queryByText('Sessions')).not.toBeInTheDocument();
    expect(screen.queryByText('Total volume')).not.toBeInTheDocument();
  });

  it('renders congen bullets with four range bands and axis ticks', async () => {
    render(<VolumeOverviewCards {...defaultProps} />);

    expect(screen.getAllByTestId('congen-bullet').length).toBe(3);
    await waitFor(() => {
      expect(screen.getAllByTestId(/congen-bullet-range-Max Effort-/).length).toBe(4);
    });
    expect(screen.queryByTestId('congen-bullet-range-Max Effort-4')).not.toBeInTheDocument();
    expect(screen.getAllByTestId('congen-bullet-axis').length).toBe(3);
    expect(screen.getAllByTestId('volume-trend-sparkline').length).toBe(3);
  });

  it('shows numeric band-bound axis labels under the bullet', async () => {
    render(<VolumeOverviewCards {...defaultProps} />);

    await waitFor(() => {
      expect(screen.getAllByTestId(/congen-bullet-tick-/).length).toBeGreaterThanOrEqual(4);
    });
    expect(screen.queryByText(/^Plan /)).not.toBeInTheDocument();
  });

  it('shows a fixed-order bullet tooltip without logging, ACWR, or intensity text', async () => {
    render(<VolumeOverviewCards {...defaultProps} />);

    await waitFor(() => {
      expect(screen.getByTestId('congen-bullet-range-Max Effort-0')).toBeInTheDocument();
    });
    fireEvent.mouseEnter(screen.getByTestId('congen-bullet-range-Max Effort-0'));

    const tooltip = await screen.findByTestId('volume-bullet-tooltip');
    expect(tooltip).toHaveTextContent(/Done:/);
    expect(tooltip).toHaveTextContent(/Target:/);
    expect(tooltip).toHaveTextContent(/Average Volume:/);
    expect(tooltip).not.toHaveTextContent(/Overshoot/);
    expect(tooltip).not.toHaveTextContent(/Logging/);
    expect(tooltip).not.toHaveTextContent(/ACWR/);
    expect(tooltip).not.toHaveTextContent(/Intensity/);
  });

  it('exposes click-to-expand on the sparkline', () => {
    render(<VolumeOverviewCards {...defaultProps} />);

    expect(screen.getAllByLabelText(/Click to expand/i).length).toBeGreaterThan(0);
  });

  it('opens a trend dialog with volume legend and tooltip when the sparkline is clicked', () => {
    render(<VolumeOverviewCards {...defaultProps} />);

    fireEvent.click(screen.getAllByTestId('volume-trend-sparkline')[0]);
    expect(screen.getByTestId('volume-trend-dialog-Max Effort')).toBeInTheDocument();
    expect(screen.getByTestId('volume-trend-dialog-chart-Max Effort')).toBeInTheDocument();
    expect(screen.getByTestId('volume-trend-dialog-legend-Max Effort')).toBeInTheDocument();
    expect(screen.getByTestId('volume-trend-dialog-legend-Max Effort')).toHaveTextContent(
      'Volume'
    );
    expect(screen.getByTestId('volume-trend-dialog-tooltip')).toBeInTheDocument();
    expect(screen.getByTestId('volume-trend-dialog-tooltip')).toHaveTextContent(/Week:/);
    expect(screen.getByTestId('volume-trend-dialog-tooltip')).toHaveTextContent(/Volume:/);
    expect(screen.queryByText(/Volume by program week/)).not.toBeInTheDocument();
  });

  it('closes the trend dialog when the close button is clicked', async () => {
    render(<VolumeOverviewCards {...defaultProps} />);

    fireEvent.click(screen.getAllByTestId('volume-trend-sparkline')[0]);
    fireEvent.click(screen.getByTestId('volume-trend-dialog-close-Max Effort'));

    await waitFor(() => {
      expect(screen.queryByTestId('volume-trend-dialog-Max Effort')).not.toBeInTheDocument();
    });
    expect(screen.getAllByTestId('responsive-line').length).toBe(3);
  });
});
