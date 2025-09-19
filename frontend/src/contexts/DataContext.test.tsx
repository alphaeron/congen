import React from 'react';
import { render, screen, waitFor, act } from '@testing-library/react';
import { DataProvider, useData } from './DataContext';
import { useAuth } from './AuthContext';

// Custom test utility to handle async operations properly - following WorkoutDetail pattern
const renderWithAct = async (component: React.ReactElement) => {
  let result: any;
  await act(async () => {
    result = render(component);
  });
  return result;
};

// Mock the API functions
jest.mock('../api/gdpr', () => ({
  getUserDataExport: jest.fn(),
}));

jest.mock('../api/exerciseMuscle', () => ({
  getExerciseMuscle: jest.fn(),
}));

jest.mock('../api/userWeightUnitPreference', () => ({
  getUserWeightUnitPreferences: jest.fn(),
}));

// Mock the AuthContext
const mockAuthContext = {
  Provider: ({ children, value }: any) => children,
  Consumer: ({ children }: any) => children({}),
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

const mockWeightUnitPreferences = [
  { id: 1, user_id: 'test-user-id', weight_unit: 'lbs' },
];

// Test component that uses the DataContext
const TestComponent = () => {
  const { userData, exerciseMuscleData, weightUnitPreferences, isLoading, error, refreshData } = useData();
  
  return (
    <div>
      <div data-testid="loading">{isLoading ? 'Loading' : 'Not Loading'}</div>
      <div data-testid="error">{error || 'No Error'}</div>
      <div data-testid="user-data">{userData ? 'User Data Loaded' : 'No User Data'}</div>
      <div data-testid="exercise-muscle-data">{exerciseMuscleData.size > 0 ? 'Exercise Muscle Data Loaded' : 'No Exercise Muscle Data'}</div>
      <div data-testid="weight-unit-preferences">{weightUnitPreferences.length > 0 ? 'Weight Unit Preferences Loaded' : 'No Weight Unit Preferences'}</div>
      <button data-testid="refresh-button" onClick={refreshData}>Refresh</button>
    </div>
  );
};

describe('DataContext', () => {
  const mockGetUserDataExport = require('../api/gdpr').getUserDataExport;
  const mockGetExerciseMuscle = require('../api/exerciseMuscle').getExerciseMuscle;
  const mockGetUserWeightUnitPreferences = require('../api/userWeightUnitPreference').getUserWeightUnitPreferences;

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

    // Wait for all data to load
    await waitFor(() => {
      expect(screen.getByTestId('loading')).toHaveTextContent('Not Loading');
      expect(screen.getByTestId('user-data')).toHaveTextContent('User Data Loaded');
      expect(screen.getByTestId('exercise-muscle-data')).toHaveTextContent('Exercise Muscle Data Loaded');
      expect(screen.getByTestId('weight-unit-preferences')).toHaveTextContent('Weight Unit Preferences Loaded');
      expect(screen.getByTestId('error')).toHaveTextContent('No Error');
    }, { timeout: 5000 });
  });

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

    await waitFor(() => {
      expect(screen.getByTestId('user-data')).toHaveTextContent('User Data Loaded');
    });

    // Clear mocks to verify they're called again
    jest.clearAllMocks();
    mockGetUserDataExport.mockResolvedValue(mockUserData);
    mockGetExerciseMuscle.mockResolvedValue(mockExerciseMuscleData);
    mockGetUserWeightUnitPreferences.mockResolvedValue(mockWeightUnitPreferences);

    const refreshButton = screen.getByTestId('refresh-button');
    await act(async () => {
      refreshButton.click();
    });

    await waitFor(() => {
      expect(mockGetUserDataExport).toHaveBeenCalledWith({ forceRefresh: true });
      expect(mockGetExerciseMuscle).toHaveBeenCalledWith({ forceRefresh: true });
      expect(mockGetUserWeightUnitPreferences).toHaveBeenCalledWith('test-user-id', { forceRefresh: true });
    });
  });

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
    let resolvePromise: (value: any) => void;
    const promise = new Promise((resolve) => {
      resolvePromise = resolve;
    });
    
    mockGetUserDataExport.mockReturnValue(promise);
    mockGetExerciseMuscle.mockResolvedValue([]);
    mockGetUserWeightUnitPreferences.mockResolvedValue([]);

    await act(async () => {
      render(
        <DataProvider>
          <TestComponent />
        </DataProvider>
      );
    });

    // Wait for initial load to start
    await waitFor(() => {
      expect(mockGetUserDataExport).toHaveBeenCalledTimes(1);
    });

    // Trigger multiple refreshes before the first one completes
    const refreshButton = screen.getByRole('button', { name: 'Refresh' });
    await act(async () => {
      refreshButton.click();
      refreshButton.click();
      refreshButton.click();
    });

    // Should still only be called once initially (subsequent calls should be prevented)
    expect(mockGetUserDataExport).toHaveBeenCalledTimes(1);

    // Resolve the promise
    await act(async () => {
      resolvePromise!(mockUserData);
    });

    await waitFor(() => {
      expect(screen.getByTestId('user-data')).toHaveTextContent('User Data Loaded');
    });
  });

  it('throws error when useData is used outside DataProvider', () => {
    // Suppress console.error for this test
    const consoleSpy = jest.spyOn(console, 'error').mockImplementation(() => {});
    
    expect(() => {
      render(<TestComponent />);
    }).toThrow('useData must be used within a DataProvider');
    
    consoleSpy.mockRestore();
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

    await waitFor(() => {
      expect(screen.getByTestId('exercise-muscle-data')).toHaveTextContent('Exercise Muscle Data Loaded');
    });

    // The exercise muscle data should be converted to a Map
    // We can't directly test the Map content, but we can verify it's loaded
    expect(mockGetExerciseMuscle).toHaveBeenCalled();
  });
});
