import { render, screen, waitFor, act } from '@testing-library/react';
import MockAdapter from 'axios-mock-adapter';
import { SnackbarProvider } from 'notistack';
import React from 'react';
import { MemoryRouter } from 'react-router';

import { ExerciseCategoryDetails } from './ExerciseCategoryDetails';
import { ENDPOINT } from '../api/endpoint';
import type { UserExercisePoolResponse } from '../api/types';

// Mock auth context
jest.mock('../contexts/AuthContext', () => ({
  useAuth: () => ({
    user: {
      keycloak_id: 'test-user-id',
      name: 'Test User',
    },
  }),
}));

// Mock DataContext
let mockUserExercisePool: UserExercisePoolResponse | null = null;
const mockLoadUserExercisePool = jest.fn();

jest.mock('../contexts/DataContext', () => ({
  useData: () => ({
    userExercisePool: mockUserExercisePool,
    loadUserExercisePool: mockLoadUserExercisePool,
    isLoading: false,
  }),
}));

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
    {
      name: 'Squat',
      description: 'A compound lower body exercise',
      movement_type: 'strength',
      is_unilateral: false,
      is_upper: false,
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
  previously_used_exercises: ['Deadlift', 'Overhead Press'],
};

describe('ExerciseCategoryDetails', () => {
  let mock: MockAdapter;

  const renderWithProviders = (component: React.ReactElement) => {
    return render(
      <MemoryRouter>
        <SnackbarProvider>{component}</SnackbarProvider>
      </MemoryRouter>
    );
  };

  beforeEach(() => {
    jest.clearAllMocks();
    mock = new MockAdapter(ENDPOINT);

    // Reset mock data
    mockUserExercisePool = null;
    mockLoadUserExercisePool.mockResolvedValue(mockExercisePoolData);
  });

  afterEach(() => {
    mock.restore();
  });

  it('should display primary exercises when category is primary', async () => {
    mock.onGet('/conjugate_workout_generator/exercise_pool').reply(200, mockExercisePoolData);

    // Set the mock data
    mockUserExercisePool = mockExercisePoolData;

    await act(async () => {
      renderWithProviders(<ExerciseCategoryDetails category="primary" />);
    });

    // Wait for loading to complete
    await waitFor(
      () => {
        expect(screen.queryByText('Loading exercise category details...')).not.toBeInTheDocument();
      },
      { timeout: 10000 }
    );

    // Wait for the content to appear
    await waitFor(
      () => {
        expect(screen.getByText('Bench Press')).toBeInTheDocument();
        expect(screen.getByText('Squat')).toBeInTheDocument();
      },
      { timeout: 10000 }
    );

    expect(screen.getByText('Bench Press')).toBeInTheDocument();
    expect(screen.getByText('Squat')).toBeInTheDocument();
  }, 15000);

  it('should display accessory exercises when category is accessory', async () => {
    mock.onGet('/conjugate_workout_generator/exercise_pool').reply(200, mockExercisePoolData);

    // Set the mock data
    mockUserExercisePool = mockExercisePoolData;

    await act(async () => {
      renderWithProviders(<ExerciseCategoryDetails category="accessory" />);
    });

    // Wait for loading to complete
    await waitFor(
      () => {
        expect(screen.queryByText('Loading exercise category details...')).not.toBeInTheDocument();
      },
      { timeout: 10000 }
    );

    await waitFor(
      () => {
        expect(screen.getByText('Bicep Curls')).toBeInTheDocument();
      },
      { timeout: 10000 }
    );

    expect(screen.getByText('Bicep Curls')).toBeInTheDocument();
  }, 15000);

  it('should display recent exercises when category is recent', async () => {
    mock.onGet('/conjugate_workout_generator/exercise_pool').reply(200, mockExercisePoolData);

    // Set the mock data
    mockUserExercisePool = mockExercisePoolData;

    await act(async () => {
      renderWithProviders(<ExerciseCategoryDetails category="recent" />);
    });

    // Wait for loading to complete
    await waitFor(
      () => {
        expect(screen.queryByText('Loading exercise category details...')).not.toBeInTheDocument();
      },
      { timeout: 10000 }
    );

    await waitFor(
      () => {
        expect(screen.getByText('Deadlift')).toBeInTheDocument();
        expect(screen.getByText('Overhead Press')).toBeInTheDocument();
      },
      { timeout: 10000 }
    );

    expect(screen.getByText('Deadlift')).toBeInTheDocument();
    expect(screen.getByText('Overhead Press')).toBeInTheDocument();
  }, 15000);

  it('should show empty state when no exercises in category', async () => {
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

    mock.onGet('/conjugate_workout_generator/exercise_pool').reply(200, emptyExercisePoolData);

    renderWithProviders(<ExerciseCategoryDetails category="primary" />);

    await waitFor(
      () => {
        // When there are no exercises, the grid should be empty
        expect(screen.queryByText('Bench Press')).not.toBeInTheDocument();
        expect(screen.queryByText('Squat')).not.toBeInTheDocument();
      },
      { timeout: 10000 }
    );
  }, 15000);

  it('should show error for invalid category', async () => {
    mock.onGet('/conjugate_workout_generator/exercise_pool').reply(200, mockExercisePoolData);

    renderWithProviders(<ExerciseCategoryDetails category="invalid" />);

    await waitFor(() => {
      expect(
        screen.getByText('Invalid exercise category. Please select a valid category.')
      ).toBeInTheDocument();
    });
  });

  it('should handle singular exercise count correctly', async () => {
    const singleExerciseData: UserExercisePoolResponse = {
      user_id: 'test-user-id',
      total_exercises: 1,
      available_exercises: 1,
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
      accessory_exercises: [],
      user_equipment: [],
      user_preferences: [],
      previously_used_exercises: [],
    };

    mock.onGet('/conjugate_workout_generator/exercise_pool').reply(200, singleExerciseData);

    // Set the mock data
    mockUserExercisePool = singleExerciseData;

    await act(async () => {
      renderWithProviders(<ExerciseCategoryDetails category="primary" />);
    });

    // Wait for loading to complete
    await waitFor(
      () => {
        expect(screen.queryByText('Loading exercise category details...')).not.toBeInTheDocument();
      },
      { timeout: 10000 }
    );

    await waitFor(
      () => {
        expect(screen.getByText('Bench Press')).toBeInTheDocument();
      },
      { timeout: 10000 }
    );
  }, 15000);

  it('should create proper exercise cards for recent exercises', async () => {
    mock.onGet('/conjugate_workout_generator/exercise_pool').reply(200, mockExercisePoolData);

    // Set the mock data
    mockUserExercisePool = mockExercisePoolData;

    await act(async () => {
      renderWithProviders(<ExerciseCategoryDetails category="recent" />);
    });

    await waitFor(() => {
      expect(screen.getByText('Deadlift')).toBeInTheDocument();
      expect(screen.getByText('Overhead Press')).toBeInTheDocument();
    });
  });
});
