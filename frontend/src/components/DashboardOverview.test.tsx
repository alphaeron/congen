import { render, screen, waitFor, act } from '@testing-library/react';
import MockAdapter from 'axios-mock-adapter';
import React from 'react';
import { MemoryRouter } from 'react-router';

import { DashboardOverview } from './DashboardOverview';
import { ENDPOINT } from '../api/endpoint';
import type { User } from '../api/types';

// Mock Nivo components to avoid Jest configuration issues
jest.mock('@nivo/line', () => ({
  ResponsiveLine: ({ data }: any) => (
    <div data-testid="line-chart">
      {data.map((series: any) => (
        <div key={series.id} data-testid={`line-series-${series.id}`}>
          {series.data.length} points
        </div>
      ))}
    </div>
  ),
}));

jest.mock('@nivo/pie', () => ({
  ResponsivePie: ({ data }: any) => (
    <div data-testid="pie-chart">
      {data.map((item: any) => (
        <div key={item.id} data-testid={`pie-item-${item.id}`}>
          {item.value}
        </div>
      ))}
    </div>
  ),
}));

describe('DashboardOverview', () => {
  // Create a new mock adapter for each test to prevent interference
  let mock: MockAdapter;
  
  const renderWithProviders = (component: React.ReactElement) => {
    return render(<MemoryRouter>{component}</MemoryRouter>);
  };

  const mockUser: User = {
    keycloak_id: 'test-user-id',
    name: 'Test User',
    created_at: '2024-01-01T00:00:00Z',
    updated_at: '2024-01-01T00:00:00Z',
    roles: ['user'],
  };
  
  const mockProgram = {
    id: 1,
    user_id: 'test-user-id',
    name: 'Test Program',
    current_week_number: 2,
    created_at: '2024-01-01T00:00:00Z',
    updated_at: '2024-01-01T00:00:00Z',
    is_active: true,
  };
  
  const mockWorkout = {
    id: 1,
    program_id: 1,
    day_number: 1,
    name: 'Test Workout',
    created_at: '2024-01-01T00:00:00Z',
    updated_at: '2024-01-01T00:00:00Z',
  };
  
  const mockOneRepMax = {
    user_id: 'test-user-id',
    exercise_name: 'Bench Press',
    one_rep_max: 225,
    unit: 'KG',
    created_at: '2024-01-01T00:00:00Z',
    updated_at: '2024-01-01T00:00:00Z',
  };

  beforeEach(() => {
    // Create a fresh mock adapter for each test
    mock = new MockAdapter(ENDPOINT);
    jest.clearAllMocks();
  });

  afterEach(() => {
    // Properly clean up the mock adapter
    if (mock) {
      mock.restore();
    }
  });

  it('should render loading state initially', async () => {
    // Use a delayed response to ensure loading state is visible
    mock
      .onGet('/program/')
      .reply(() => new Promise(resolve => setTimeout(() => resolve([200, []]), 100)));
    mock
      .onGet('/programmed_workout/')
      .reply(() => new Promise(resolve => setTimeout(() => resolve([200, []]), 100)));
    mock
      .onGet('/user_one_rep_max/user/test-user-id')
      .reply(() => new Promise(resolve => setTimeout(() => resolve([200, []]), 100)));

    await act(async () => {
      renderWithProviders(<DashboardOverview user={mockUser} />);
    });

    expect(screen.getByRole('progressbar')).toBeInTheDocument();
  });

  it('should render dashboard overview when data loads successfully', async () => {
    mock.onGet('/program/').reply(200, [mockProgram]);
    mock.onGet('/programmed_workout/').reply(200, [mockWorkout]);
    mock.onGet('/user_one_rep_max/user/test-user-id').reply(200, [mockOneRepMax]);

    await act(async () => {
      renderWithProviders(<DashboardOverview user={mockUser} />);
    });

    await waitFor(() => {
      expect(screen.getByText('Dashboard Overview')).toBeInTheDocument();
    });

    expect(screen.getByText('Total Workouts')).toBeInTheDocument();
    expect(screen.getByText('1RM Records')).toBeInTheDocument();
    expect(screen.getByText('Unique Exercises')).toBeInTheDocument();
    expect(screen.getByText('Current Week')).toBeInTheDocument();
  });

  it('should display active program when available', async () => {
    mock.onGet('/program/').reply(200, [mockProgram]);
    mock.onGet('/programmed_workout/').reply(200, [mockWorkout]);
    mock.onGet('/user_one_rep_max/user/test-user-id').reply(200, []);

    await act(async () => {
      renderWithProviders(<DashboardOverview user={mockUser} />);
    });

    await waitFor(() => {
      expect(screen.getByText('Active Program')).toBeInTheDocument();
      expect(screen.getByText('Test Program')).toBeInTheDocument();
      expect(screen.getByText('Week 1')).toBeInTheDocument(); // 1 workout = Week 1
      expect(screen.getByText('Active')).toBeInTheDocument();
    });
  });

  it('should display recent 1RM records when available', async () => {
    mock.onGet('/program/').reply(200, []);
    mock.onGet('/programmed_workout/').reply(200, []);
    mock.onGet('/user_one_rep_max/user/test-user-id').reply(200, [mockOneRepMax]);

    await act(async () => {
      renderWithProviders(<DashboardOverview user={mockUser} />);
    });

    await waitFor(() => {
      expect(screen.getByText('Recent 1RM Records')).toBeInTheDocument();
      // Check for Bench Press in the Recent 1RM Records section specifically
      const recentOneRepMaxSection = screen
        .getByText('Recent 1RM Records')
        .closest('.MuiCard-root');
      expect(recentOneRepMaxSection).toHaveTextContent('Bench Press');
      expect(recentOneRepMaxSection).toHaveTextContent('225 KG');
    });
  });

  it('should display welcome message when no data is available', async () => {
    mock.onGet('/program/').reply(200, []);
    mock.onGet('/programmed_workout/').reply(200, []);
    mock.onGet('/user_one_rep_max/user/test-user-id').reply(200, []);

    await act(async () => {
      renderWithProviders(<DashboardOverview user={mockUser} />);
    });

    await waitFor(() => {
      expect(screen.getByText('Welcome to Your Dashboard!')).toBeInTheDocument();
      expect(screen.getByText(/Start by creating your first program/)).toBeInTheDocument();
    });
  });

  it('should calculate and display correct statistics', async () => {
    const multiplePrograms = [
      { ...mockProgram, current_week_number: 3 },
      { ...mockProgram, id: 2, current_week_number: 2, is_active: false },
    ];
    const multipleWorkouts = [
      { ...mockWorkout, id: 1, program_id: 1 },
      { ...mockWorkout, id: 2, program_id: 1 },
      { ...mockWorkout, id: 3, program_id: 1 },
      { ...mockWorkout, id: 4, program_id: 2 },
    ];
    const multipleOneRepMaxes = [
      mockOneRepMax,
      { ...mockOneRepMax, exercise_name: 'Squat', one_rep_max: 315 },
    ];

    mock.onGet('/program/').reply(200, multiplePrograms);
    mock.onGet('/programmed_workout/').reply(200, multipleWorkouts);
    mock.onGet('/user_one_rep_max/user/test-user-id').reply(200, multipleOneRepMaxes);

    await act(async () => {
      renderWithProviders(<DashboardOverview user={mockUser} />);
    });

    await waitFor(
      () => {
        // Total workouts should be actual workout count (4 workouts)
        expect(screen.getByText('4')).toBeInTheDocument();
        // 1RM records count - check the card with ShowChartIcon
        const showChartIcons = screen.getAllByTestId('ShowChartIcon');
        const oneRepMaxCard = showChartIcons[0].closest('.MuiCard-root');
        expect(oneRepMaxCard).toHaveTextContent('2');
        // Unique exercises count - check the card with TrendingUpIcon (first one in dashboard)
        const trendingUpIcons = screen.getAllByTestId('TrendingUpIcon');
        const uniqueExercisesCard = trendingUpIcons[0].closest('.MuiCard-root');
        expect(uniqueExercisesCard).toHaveTextContent('2');
        // Current week from active program (3 workouts = Week 1)
        expect(screen.getByText('1')).toBeInTheDocument();
      },
      { timeout: 10000 }
    );
  });

  it('should verify API calls are made with correct endpoints', async () => {
    mock.onGet('/program/').reply(200, []);
    mock.onGet('/programmed_workout/').reply(200, []);
    mock.onGet('/user_one_rep_max/user/test-user-id').reply(200, []);
    // Mock ConjugateProgression dependencies
    mock.onGet('/gdpr/export').reply(200, { 
      training_programs: [{
        program: { id: 1, name: 'Test Program' },
        workouts: [{
          workout: { id: 1, name: 'Test Workout' },
          stages: [{
            stage: { name: 'Test Stage' },
            exercises: [{
              exercise: { exercise_name: 'Bench Press' },
              set_schemes: [{
                performed_weight: 100,
                performed_rep_count: 10,
                band_weight_lbs: null
              }]
            }]
          }]
        }]
      }], 
      data_retention_policies: [] 
    });
    mock.onGet(/\/exercise\/.*/).reply(200, {
      id: 1,
      exercise_name: 'Test Exercise',
      category: 'strength',
      primary_muscle: 'chest',
      secondary_muscles: ['triceps', 'shoulders'],
      instructions: 'Test instructions',
      equipment: 'barbell',
      difficulty: 'intermediate'
    });

    await act(async () => {
      renderWithProviders(<DashboardOverview user={mockUser} />);
    });

    await waitFor(() => {
      expect(mock.history.get).toHaveLength(5); // 3 from DashboardOverview + 2 from ConjugateProgression
      expect(mock.history.get[0].url).toBe('/program/');
      expect(mock.history.get[1].url).toBe('/programmed_workout/');
      expect(mock.history.get[2].url).toBe('/user_one_rep_max/user/test-user-id');
      // Additional calls from ConjugateProgression component
      expect(mock.history.get[3].url).toBe('/gdpr/export');
      expect(mock.history.get[4].url).toMatch(/\/exercise\/.*/);
    });
  });
});
