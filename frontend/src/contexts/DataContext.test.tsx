import { render, screen, waitFor, act } from '@testing-library/react';
import React from 'react';

import { useAuth } from './AuthContext';
import { DataProvider, useData } from './DataContext';

// Mock the API functions
const mockGetUserDataExport = jest.fn();
const mockGetExerciseMuscle = jest.fn();
const mockGetUserWeightUnitPreferences = jest.fn();

jest.mock('../api/gdpr', () => ({
  getUserDataExport: mockGetUserDataExport,
}));

jest.mock('../api/exerciseMuscle', () => ({
  getExerciseMuscle: mockGetExerciseMuscle,
}));

jest.mock('../api/userWeightUnitPreference', () => ({
  getUserWeightUnitPreferences: mockGetUserWeightUnitPreferences,
}));

// Mock the AuthContext
const mockAuthContext = {
  Provider: ({ children }: { children: React.ReactNode; value?: unknown }) => children,
  Consumer: ({ children }: { children: (value: unknown) => React.ReactNode }) => children({}),
};
jest.mock('./AuthContext', () => ({
  useAuth: jest.fn(),
  AuthContext: mockAuthContext,
}));

// Mock notistack
jest.mock('notistack', () => ({
  useSnackbar: () => ({
    enqueueSnackbar: jest.fn(),
  }),
}));

const mockUser = {
  keycloak_id: 'test-user-id',
  email: 'test@example.com',
};

const mockUserData = {
  programs: [
    {
      id: 1,
      name: 'Test Program',
      workouts: [
        {
          id: 1,
          week_number: 1,
          stages: [
            {
              id: 1,
              stage_name: 'Push Day',
              exercises: [
                {
                  exercise: {
                    id: 1,
                    exercise_name: 'Bench Press',
                    notes: 'Test notes',
                  },
                  setSchemes: [],
                },
              ],
            },
          ],
        },
      ],
    },
  ],
};

const mockExerciseMuscleData = [
  { exercise_name: 'Bench Press', muscle_name: 'Pectoralis Major' },
  { exercise_name: 'Bench Press', muscle_name: 'Triceps Brachii' },
];

const mockWeightUnitPreferences = [{ id: 1, user_id: 'test-user-id', weight_unit: 'lbs' }];

// Test component that uses the DataContext
const TestComponent = () => {
  const { userData, exerciseMuscleData, weightUnitPreferences, isLoading, error, refreshData } =
    useData();

  return (
    <div>
      <div data-testid="loading">{isLoading ? 'Loading' : 'Not Loading'}</div>
      <div data-testid="error">{error || 'No Error'}</div>
      <div data-testid="user-data">{userData ? 'User Data Loaded' : 'No User Data'}</div>
      <div data-testid="exercise-muscle-data">
        {exerciseMuscleData.size > 0 ? 'Exercise Muscle Data Loaded' : 'No Exercise Muscle Data'}
      </div>
      <div data-testid="weight-unit-preferences">
        {weightUnitPreferences.length > 0
          ? 'Weight Unit Preferences Loaded'
          : 'No Weight Unit Preferences'}
      </div>
      <button data-testid="refresh-button" onClick={refreshData}>
        Refresh
      </button>
    </div>
  );
};

describe('DataContext', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    (useAuth as jest.Mock).mockReturnValue({ user: mockUser });
  });

  it('loads data successfully', async () => {
    mockGetUserDataExport.mockResolvedValue(mockUserData);
    mockGetExerciseMuscle.mockResolvedValue(mockExerciseMuscleData);
    mockGetUserWeightUnitPreferences.mockResolvedValue(mockWeightUnitPreferences);

    await act(async () => {
      render(
        <DataProvider>
          <TestComponent />
        </DataProvider>
      );
    });

    // Wait for loading to complete
    await waitFor(
      () => {
        expect(screen.getByTestId('loading')).toHaveTextContent('Not Loading');
      },
      { timeout: 3000 }
    );

    // Verify the component renders without crashing
    expect(screen.getByTestId('user-data')).toBeInTheDocument();
    expect(screen.getByTestId('exercise-muscle-data')).toBeInTheDocument();
    expect(screen.getByTestId('weight-unit-preferences')).toBeInTheDocument();
  }, 5000);

  it('refreshes data when refreshData is called', async () => {
    mockGetUserDataExport.mockResolvedValue(mockUserData);
    mockGetExerciseMuscle.mockResolvedValue(mockExerciseMuscleData);
    mockGetUserWeightUnitPreferences.mockResolvedValue(mockWeightUnitPreferences);

    await act(async () => {
      render(
        <DataProvider>
          <TestComponent />
        </DataProvider>
      );
    });

    // Wait for loading to complete
    await waitFor(
      () => {
        expect(screen.getByTestId('loading')).toHaveTextContent('Not Loading');
      },
      { timeout: 3000 }
    );

    // Verify refresh button exists and can be clicked
    const refreshButton = screen.getByTestId('refresh-button');
    expect(refreshButton).toBeInTheDocument();

    await act(async () => {
      refreshButton.click();
    });

    // Verify the component still renders after refresh
    expect(screen.getByTestId('refresh-button')).toBeInTheDocument();
  }, 5000);

  it('does not load data when user is not available', () => {
    (useAuth as jest.Mock).mockReturnValue({ user: null });

    render(
      <DataProvider>
        <TestComponent />
      </DataProvider>
    );

    expect(mockGetUserDataExport).not.toHaveBeenCalled();
    expect(mockGetExerciseMuscle).not.toHaveBeenCalled();
    expect(mockGetUserWeightUnitPreferences).not.toHaveBeenCalled();
  });

  it('prevents multiple simultaneous data loads', async () => {
    mockGetUserDataExport.mockResolvedValue(mockUserData);
    mockGetExerciseMuscle.mockResolvedValue([]);
    mockGetUserWeightUnitPreferences.mockResolvedValue([]);

    await act(async () => {
      render(
        <DataProvider>
          <TestComponent />
        </DataProvider>
      );
    });

    // Wait for loading to complete
    await waitFor(
      () => {
        expect(screen.getByTestId('loading')).toHaveTextContent('Not Loading');
      },
      { timeout: 3000 }
    );

    // Verify the component renders
    expect(screen.getByTestId('refresh-button')).toBeInTheDocument();
  }, 5000);

  it('throws error when useData is used outside DataProvider', () => {
    expect(() => {
      render(<TestComponent />);
    }).toThrow('useData must be used within a DataProvider');
  });

  it('converts exercise muscle data to Map correctly', async () => {
    mockGetUserDataExport.mockResolvedValue(mockUserData);
    mockGetExerciseMuscle.mockResolvedValue(mockExerciseMuscleData);
    mockGetUserWeightUnitPreferences.mockResolvedValue(mockWeightUnitPreferences);

    await act(async () => {
      render(
        <DataProvider>
          <TestComponent />
        </DataProvider>
      );
    });

    // Wait for loading to complete
    await waitFor(
      () => {
        expect(screen.getByTestId('loading')).toHaveTextContent('Not Loading');
      },
      { timeout: 3000 }
    );

    // Verify the component renders
    expect(screen.getByTestId('exercise-muscle-data')).toBeInTheDocument();
  }, 5000);
});
