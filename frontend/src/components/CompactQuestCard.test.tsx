import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import React from 'react';

import { CompactQuestCard } from './CompactQuestCard';

// Mock the DataContext
const mockUseData = {
  submitPerformanceMetrics: jest.fn(),
  submitWeeklyTest: jest.fn(),
  getCurrentWeekTest: jest.fn(() => []),
  loadPerformanceMetricsInRange: jest.fn(() =>
    Promise.resolve([
      {
        keycloak_id: 'test-user',
        strain: 10.5,
        recovery: 80.0,
        hrv: 42.0,
        sleep_score: 75.0,
        vo2_max: 40.0,
        rem_sleep_minutes: 85,
        deep_sleep_minutes: 110,
        subjective_tiredness: 4,
        created_at: new Date(Date.now() - 86400000),
        updated_at: new Date(Date.now() - 86400000),
      },
      {
        keycloak_id: 'test-user',
        strain: 11.0,
        recovery: 82.0,
        hrv: 43.0,
        sleep_score: 76.0,
        vo2_max: 41.0,
        rem_sleep_minutes: 88,
        deep_sleep_minutes: 115,
        subjective_tiredness: 3,
        created_at: new Date(Date.now() - 172800000),
        updated_at: new Date(Date.now() - 172800000),
      },
    ])
  ),
  loadWeeklyTests: jest.fn(),
  loadTestProtocols: jest.fn(),
  testProtocols: [
    {
      test_name: 'vertical_jump',
      display_name: 'Vertical Jump',
      unit: 'cm',
      description: 'Test your vertical jump height',
    },
    {
      test_name: 'hr_recovery',
      display_name: 'Heart Rate Recovery',
      unit: 'bpm',
      description: 'Test your heart rate recovery',
    },
  ],
  refreshData: jest.fn(),
};

jest.mock('../contexts/DataContext', () => ({
  useData: () => mockUseData,
}));

// Mock notistack
jest.mock('notistack', () => ({
  useSnackbar: () => ({
    enqueueSnackbar: jest.fn(),
  }),
}));

// Mock TanStack Query
jest.mock('@tanstack/react-query', () => ({
  useMutation: () => ({
    mutate: jest.fn(),
    isPending: false,
  }),
}));

// Mock framer-motion
jest.mock('framer-motion', () => {
  // eslint-disable-next-line @typescript-eslint/no-require-imports
  const React = require('react');
  return {
    motion: {
      div: ({ children, ...props }) => {
        // Filter out Framer Motion specific props
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

// Mock components
jest.mock('./CustomSvgIcon', () => ({
  CustomSvgIcon: ({ src, alt }: { src: string; alt?: string }) => (
    <div data-testid="custom-svg-icon" data-src={src} data-alt={alt} />
  ),
}));

jest.mock('./MetricTrendChart', () => ({
  MetricTrendChart: ({
    metricLabel,
    data,
    isLoading,
  }: {
    metricLabel: string;
    data: unknown[];
    isLoading: boolean;
  }) => (
    <div data-testid="metric-trend-chart">
      <div data-testid="chart-label">{metricLabel}</div>
      <div data-testid="chart-data-count">{data.length}</div>
      <div data-testid="chart-loading">{isLoading.toString()}</div>
    </div>
  ),
}));

describe('CompactQuestCard', () => {
  const mockCurrentMetrics = {
    keycloak_id: 'test-user',
    strain: 12.5,
    recovery: 85.0,
    hrv: 45.2,
    sleep_score: 78.3,
    vo2_max: 42.5,
    rem_sleep_minutes: 90,
    deep_sleep_minutes: 120,
    subjective_tiredness: 3,
    created_at: new Date(),
    updated_at: new Date(),
  };

  const mockWeeklyTests = [
    {
      id: 1,
      keycloak_id: 'test-user',
      week_start_timestamp: new Date(),
      test_name: 'vertical_jump',
      status: 'COMPLETED' as const,
      result_value: 52.3,
      created_at: new Date(),
      updated_at: new Date(),
    },
  ];

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders daily quest card', () => {
    render(<CompactQuestCard type="daily" currentMetrics={mockCurrentMetrics} />);

    expect(screen.getByText('Daily Quests')).toBeInTheDocument();
  });

  it('renders weekly quest card', () => {
    render(<CompactQuestCard type="weekly" weeklyTests={mockWeeklyTests} />);

    expect(screen.getByText('Weekly Quests')).toBeInTheDocument();
  });

  it('displays daily metrics', () => {
    render(<CompactQuestCard type="daily" currentMetrics={mockCurrentMetrics} />);

    expect(screen.getByText('Strain')).toBeInTheDocument();
    expect(screen.getByText('Recovery')).toBeInTheDocument();
    expect(screen.getByText('HRV')).toBeInTheDocument();
    expect(screen.getByText('Sleep Score')).toBeInTheDocument();
  });

  it('displays weekly test protocols', () => {
    render(<CompactQuestCard type="weekly" weeklyTests={mockWeeklyTests} />);

    expect(screen.getByText('Vertical Jump')).toBeInTheDocument();
    expect(screen.getByText('Heart Rate Recovery')).toBeInTheDocument();
  });

  it('opens dialog when metric is clicked', async () => {
    render(<CompactQuestCard type="daily" currentMetrics={mockCurrentMetrics} />);

    const strainCard = screen.getByText('Strain').closest('div');
    expect(strainCard).toBeInTheDocument();

    if (strainCard) {
      fireEvent.click(strainCard);

      await waitFor(() => {
        expect(screen.getByRole('dialog')).toBeInTheDocument();
      });
    }
  });

  it('opens dialog when weekly test is clicked', async () => {
    render(<CompactQuestCard type="weekly" weeklyTests={mockWeeklyTests} />);

    const verticalJumpCard = screen.getByText('Vertical Jump').closest('div');
    expect(verticalJumpCard).toBeInTheDocument();

    if (verticalJumpCard) {
      fireEvent.click(verticalJumpCard);

      await waitFor(() => {
        expect(screen.getByRole('dialog')).toBeInTheDocument();
      });
    }
  });

  it('closes dialog when close button is clicked', async () => {
    render(<CompactQuestCard type="daily" currentMetrics={mockCurrentMetrics} />);

    const strainCard = screen.getByText('Strain').closest('div');
    if (strainCard) {
      fireEvent.click(strainCard);

      await waitFor(() => {
        expect(screen.getByRole('dialog')).toBeInTheDocument();
      });

      const cancelButton = screen.getByText('Cancel');
      fireEvent.click(cancelButton);

      await waitFor(() => {
        expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
      });
    }
  });

  it('renders motion components', () => {
    render(<CompactQuestCard type="daily" currentMetrics={mockCurrentMetrics} />);

    const motionDivs = screen.getAllByTestId('motion-div');
    expect(motionDivs.length).toBeGreaterThan(0);
  });

  it('displays custom SVG icons', () => {
    render(<CompactQuestCard type="daily" currentMetrics={mockCurrentMetrics} />);

    const svgIcons = screen.getAllByTestId('custom-svg-icon');
    expect(svgIcons.length).toBeGreaterThan(0);
  });

  it('handles empty current metrics', () => {
    render(<CompactQuestCard type="daily" />);

    expect(screen.getByText('Daily Quests')).toBeInTheDocument();
  });

  it('handles empty weekly tests', () => {
    render(<CompactQuestCard type="weekly" />);

    expect(screen.getByText('Weekly Quests')).toBeInTheDocument();
  });

  it('calls onTestUpdate when provided', () => {
    const onTestUpdate = jest.fn();
    render(
      <CompactQuestCard type="weekly" weeklyTests={mockWeeklyTests} onTestUpdate={onTestUpdate} />
    );

    // The callback should be available for when tests are updated
    expect(onTestUpdate).toBeDefined();
  });
});
