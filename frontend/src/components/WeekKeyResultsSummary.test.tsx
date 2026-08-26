import { ThemeProvider, createTheme } from '@mui/material/styles';
import { fireEvent, render, screen } from '@testing-library/react';
import React from 'react';

import { WeekKeyResultsSummary } from './WeekKeyResultsSummary';
import type { WeekKeyResults } from '../utils/performanceAnalyticsUtils';

jest.mock('./VolumeTrendSparkline', () => ({
  VolumeTrendSparkline: (): React.ReactElement => (
    <div data-testid="volume-trend-sparkline">Sparkline</div>
  ),
}));

describe('WeekKeyResultsSummary', () => {
  const theme = createTheme();
  const results: WeekKeyResults = {
    weekNumber: 2,
    meTopSets: [
      {
        weekNumber: 2,
        dayInWeek: 1,
        workoutName: 'ME Upper Day',
        workoutId: 10,
        exerciseName: 'Bench Press',
        weightKg: 110,
        reps: 3,
      },
    ],
    volume: {
      maxEffort: 1200,
      dynamicEffort: 800,
      accessory: 500,
      total: 2500,
    },
    priorWeekVolume: {
      maxEffort: 1000,
      dynamicEffort: 700,
      accessory: 400,
      total: 2100,
    },
    volumeDeltaPercent: {
      maxEffort: 20,
      dynamicEffort: 14,
      accessory: 25,
      total: 19,
    },
  };

  const renderSummary = (props: Partial<React.ComponentProps<typeof WeekKeyResultsSummary>> = {}) =>
    render(
      <ThemeProvider theme={theme}>
        <WeekKeyResultsSummary
          results={results}
          preferredUnit="LBS"
          userOneRepMaxes={[
            {
              exercise_name: 'Bench Press',
              one_rep_max: 535,
              unit: 'LBS',
              created_at: new Date(),
              updated_at: new Date(),
            },
          ]}
          totalVolumeTrend={[
            { x: 'W1', y: 2100 },
            { x: 'W2', y: 2500 },
          ]}
          {...props}
        />
      </ThemeProvider>
    );

  it('renders hero total volume, ME peaks, and category volume rows', () => {
    renderSummary();

    expect(screen.getByTestId('week-key-results-summary')).toBeInTheDocument();
    expect(screen.getByText('Week at a Glance')).toBeInTheDocument();
    expect(screen.getByTestId('week-key-results-total-volume')).toHaveTextContent('+19%');
    expect(screen.getByTestId('week-me-top-set-10-Bench Press')).toBeInTheDocument();
    expect(screen.getByText(/Bench Press/)).toBeInTheDocument();
    expect(screen.getByText(/45% 1RM/)).toBeInTheDocument();
    expect(screen.getByTestId('week-volume-row-max-effort')).toHaveTextContent('+20%');
    expect(screen.getByTestId('week-volume-row-max-effort')).toHaveTextContent('vs 1.0k lbs prior week');
    expect(screen.getByTestId('volume-trend-sparkline')).toBeInTheDocument();
  });

  it('shows empty state when no ME top sets were logged', () => {
    renderSummary({
      results: {
        ...results,
        meTopSets: [],
      },
    });

    expect(
      screen.getByText('No ME top sets logged yet. Complete a max effort day to see your peak lifts here.')
    ).toBeInTheDocument();
  });

  it('shows not logged copy for zero-volume categories', () => {
    renderSummary({
      results: {
        ...results,
        volume: {
          ...results.volume,
          dynamicEffort: 0,
        },
        volumeDeltaPercent: {
          ...results.volumeDeltaPercent!,
          dynamicEffort: -100,
        },
      },
    });

    expect(screen.getByTestId('week-volume-row-dynamic-effort')).toHaveTextContent('Not logged');
  });

  it('invokes navigation callbacks from ME peak cards', () => {
    const onWorkoutClick = jest.fn();
    const onExerciseClick = jest.fn();
    renderSummary({ onWorkoutClick, onExerciseClick });

    fireEvent.click(screen.getByTestId('week-me-top-set-10-Bench Press'));
    expect(onWorkoutClick).toHaveBeenCalledWith(10);

    fireEvent.click(screen.getByText('Bench Press'));
    expect(onExerciseClick).toHaveBeenCalledWith('Bench Press');
  });
});
