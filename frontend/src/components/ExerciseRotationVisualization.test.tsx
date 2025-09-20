import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen, waitFor } from '@testing-library/react';
import MockAdapter from 'axios-mock-adapter';
import { SnackbarProvider } from 'notistack';
import React from 'react';
import { MemoryRouter } from 'react-router';

import { ExerciseRotationVisualization } from './ExerciseRotationVisualization';
import { ENDPOINT } from '../api/endpoint';
import type { Program, UserExercisePoolResponse } from '../api/types';

// Mock auth context
jest.mock('../contexts/AuthContext', () => ({
  useAuth: () => ({
    user: {
      keycloak_id: 'test-user-id',
      name: 'Test User',
    },
  }),
}));

// Mock react-router
const mockNavigate = jest.fn();
const mockSearchParams = new URLSearchParams();
jest.mock('react-router', () => ({
  ...jest.requireActual('react-router'),
  useNavigate: () => mockNavigate,
  useSearchParams: () => [mockSearchParams],
}));

// Mock chart components to avoid Nivo library issues in tests
jest.mock('./ExercisePoolPieChart', () => ({
  ExercisePoolPieChart: () => <div data-testid="exercise-pool-pie-chart">Mock Pie Chart</div>,
}));

jest.mock('./RadialBarChart', () => ({
  RadialBarChart: () => <div data-testid="radial-bar-chart">Mock Radial Bar Chart</div>,
}));

jest.mock('./ExercisePoolSunburstChart', () => ({
  ExercisePoolSunburstChart: () => (
    <div data-testid="exercise-pool-sunburst-chart">Mock Sunburst Chart</div>
  ),
}));

const mockPrograms: Program[] = [
  {
    id: 1,
    user_id: 'test-user-id',
    name: 'Test Program',
    current_week_number: 1,
    created_at: new Date('2023-01-01'),
    updated_at: new Date('2023-01-01'),
    is_active: true,
  },
];

const mockExercisePoolData: UserExercisePoolResponse = {
  user_id: 'test-user-id',
  total_exercises: 3,
  available_exercises: 3,
  primary_exercises: [
    {
      name: 'Bench Press',
      description: 'A compound upper body exercise',
      movement_type: 'strength',
      is_unilateral: false,
      is_upper: true,
      is_accessory: false,
    },
  ],
  accessory_exercises: [
    {
      name: 'Bicep Curls',
      description: 'An isolation exercise for biceps',
      movement_type: 'strength',
      is_unilateral: false,
      is_upper: true,
      is_accessory: true,
    },
  ],
  user_equipment: [],
  user_preferences: [],
  previously_used_exercises: ['Deadlift'],
};

describe('ExerciseRotationVisualization', () => {
  let mock: MockAdapter;

  const renderWithProviders = (component: React.ReactElement) => {
    const queryClient = new QueryClient({
      defaultOptions: {
        queries: {
          retry: false,
        },
      },
    });

    return render(
      <QueryClientProvider client={queryClient}>
        <MemoryRouter>
          <SnackbarProvider>{component}</SnackbarProvider>
        </MemoryRouter>
      </QueryClientProvider>
    );
  };

  beforeEach(() => {
    jest.clearAllMocks();
    mock = new MockAdapter(ENDPOINT);

    // Mock the exercise API calls that ExerciseName component makes
    mock.onGet('/exercise/').reply(200, [
      { id: 1, name: 'Bench Press' },
      { id: 2, name: 'Squat' },
    ]);

    // Reset search params
    mockSearchParams.delete('category');
  });

  afterEach(() => {
    mock.restore();
  });

  it('should display exercise rotation overview when data is loaded', async () => {
    mock.onGet('/program/').reply(200, mockPrograms);
    mock.onGet('/conjugate_workout_generator/exercise_pool').reply(200, mockExercisePoolData);

    renderWithProviders(<ExerciseRotationVisualization />);

    await waitFor(
      () => {
        expect(screen.getByText('Available Exercises')).toBeInTheDocument();
      },
      { timeout: 10000 }
    );

    expect(screen.getAllByText('Primary Exercises')).toHaveLength(1);
    expect(screen.getAllByText('Accessory Exercises')).toHaveLength(1);
    expect(screen.getByText('Recent Exercises')).toBeInTheDocument();
  }, 15000);

  it('should show category details when category is selected', async () => {
    mockSearchParams.set('category', 'primary');

    mock.onGet('/program/').reply(200, mockPrograms);
    mock.onGet('/conjugate_workout_generator/exercise_pool').reply(200, mockExercisePoolData);

    renderWithProviders(<ExerciseRotationVisualization />);

    await waitFor(
      () => {
        expect(screen.getByTestId('ArrowBackIcon')).toBeInTheDocument();
      },
      { timeout: 10000 }
    );
  }, 15000);

  it('should display exercise pool statistics', async () => {
    mock.onGet('/program/').reply(200, mockPrograms);
    mock.onGet('/conjugate_workout_generator/exercise_pool').reply(200, mockExercisePoolData);

    renderWithProviders(<ExerciseRotationVisualization />);

    await waitFor(
      () => {
        expect(screen.getByText('Available Exercises')).toBeInTheDocument();
      },
      { timeout: 10000 }
    );

    // Should show the exercise counts - there are multiple "1" elements, so we check they exist
    expect(screen.getAllByText('1')).toHaveLength(2); // Multiple "1" elements in the stats
  }, 15000);

  it('should handle empty programs data', async () => {
    mock.onGet('/program/').reply(200, []);
    mock.onGet('/conjugate_workout_generator/exercise_pool').reply(200, mockExercisePoolData);

    renderWithProviders(<ExerciseRotationVisualization />);

    await waitFor(
      () => {
        expect(screen.getByText('Available Exercises')).toBeInTheDocument();
      },
      { timeout: 10000 }
    );
  }, 15000);

  it('should handle empty exercise pool data', async () => {
    const emptyExercisePoolData: UserExercisePoolResponse = {
      user_id: 'test-user-id',
      total_exercises: 0,
      available_exercises: 0,
      primary_exercises: [],
      accessory_exercises: [],
      user_equipment: [],
      user_preferences: [],
      previously_used_exercises: [],
    };

    mock.onGet('/program/').reply(200, mockPrograms);
    mock.onGet('/conjugate_workout_generator/exercise_pool').reply(200, emptyExercisePoolData);

    renderWithProviders(<ExerciseRotationVisualization />);

    await waitFor(
      () => {
        expect(screen.getByText('Available Exercises')).toBeInTheDocument();
      },
      { timeout: 10000 }
    );
  }, 15000);

  it('should show back button when in category view', async () => {
    mockSearchParams.set('category', 'primary');

    mock.onGet('/program/').reply(200, mockPrograms);
    mock.onGet('/conjugate_workout_generator/exercise_pool').reply(200, mockExercisePoolData);

    renderWithProviders(<ExerciseRotationVisualization />);

    await waitFor(
      () => {
        expect(screen.getByTestId('ArrowBackIcon')).toBeInTheDocument();
      },
      { timeout: 10000 }
    );
  }, 15000);

  it('navigates to category details when category card is clicked', async () => {
    mock.onGet('/conjugate_workout_generator/exercise_pool').reply(200, mockExercisePoolData);

    renderWithProviders(<ExerciseRotationVisualization />);

    await waitFor(() => {
      expect(screen.getByText('Available Exercises')).toBeInTheDocument();
    });

    // Click on Primary Exercises card
    const primaryCard = screen.getByText('Primary Exercises');
    primaryCard.click();

    expect(mockNavigate).toHaveBeenCalledWith(
      expect.stringContaining('section=workouts&subsection=rotation&category=primary')
    );
  });

  it('navigates to workout preferences when preferences card is clicked', async () => {
    mock.onGet('/conjugate_workout_generator/exercise_pool').reply(200, mockExercisePoolData);

    renderWithProviders(<ExerciseRotationVisualization />);

    await waitFor(() => {
      expect(screen.getByText('Available Exercises')).toBeInTheDocument();
    });

    // Click on Available Equipment card (which navigates to preferences)
    const equipmentCard = screen.getByText('Available Equipment');
    equipmentCard.click();

    expect(mockNavigate).toHaveBeenCalledWith(
      expect.stringContaining('section=workouts&subsection=preferences')
    );
  });

  it('shows back button when category is selected', async () => {
    // Set up URL params to show a selected category
    mockSearchParams.set('category', 'primary');
    mock.onGet('/conjugate_workout_generator/exercise_pool').reply(200, mockExercisePoolData);

    renderWithProviders(<ExerciseRotationVisualization />);

    await waitFor(
      () => {
        expect(screen.getByTestId('ArrowBackIcon')).toBeInTheDocument();
      },
      { timeout: 10000 }
    );

    // Should show back button (using data-testid since it doesn't have accessible name)
    expect(screen.getByTestId('ArrowBackIcon')).toBeInTheDocument();
  }, 15000);
});
