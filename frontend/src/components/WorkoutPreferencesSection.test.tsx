import { render, screen, fireEvent, waitFor, act } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import MockAdapter from 'axios-mock-adapter';
import { SnackbarProvider } from 'notistack';
import React from 'react';
import { MemoryRouter } from 'react-router';

import { WorkoutPreferencesSection } from './WorkoutPreferencesSection';
import { ENDPOINT } from '../api/endpoint';
import type { User } from '../api/types';

const mockUser: User = {
  keycloak_id: 'test-user-id',
  name: 'John Doe',
  created_at: '2024-01-01T00:00:00Z',
  updated_at: '2024-01-01T00:00:00Z',
  roles: ['user'],
};

// Mock the auth context
jest.mock('../contexts/AuthContext', () => ({
  useAuth: () => ({
    user: mockUser,
  }),
}));

// Mock DataContext
const mockUseData = jest.fn();
jest.mock('../contexts/DataContext', () => ({
  useData: () => mockUseData(),
  DataProvider: ({ children }: { children: React.ReactNode }) => children,
}));

const renderWithProviders = (component: React.ReactElement) => {
  return render(
    <MemoryRouter>
      <SnackbarProvider>{component}</SnackbarProvider>
    </MemoryRouter>
  );
};

describe('WorkoutPreferencesSection', () => {
  // Create a new mock adapter for each test to prevent interference
  let mock: MockAdapter;

  beforeEach(() => {
    // Create a fresh mock adapter for each test
    mock = new MockAdapter(ENDPOINT);

    // Set up default mock data for DataContext
    const defaultMockDataContext = {
      userData: null,
      exerciseMuscleData: new Map(),
      weightUnitPreferences: [],
      userEquipment: [],
      userWeakMuscles: [],
      userExercisePreferences: [],
      isLoading: false,
      error: null,
      refreshData: jest.fn(),
      refreshSpecificData: jest.fn(),
      isDataStale: false,
      loadAllExercises: jest.fn().mockResolvedValue([]),
      loadAllMuscles: jest.fn().mockResolvedValue([]),
      loadAllEquipment: jest.fn().mockResolvedValue([]),
      loadUserEquipment: jest.fn().mockResolvedValue([]),
      loadUserWeakMuscles: jest.fn().mockResolvedValue([]),
      loadUserExercisePreferences: jest.fn().mockResolvedValue([]),
      loadUserWeightUnitPreferences: jest.fn().mockResolvedValue([]),
    };

    mockUseData.mockReturnValue(defaultMockDataContext);
  });

  afterEach(() => {
    // Properly clean up the mock adapter
    if (mock) {
      mock.restore();
    }
  });

  it('should render workout preferences section', async () => {
    // Mock API responses
    mock.onGet('/exercise/').reply(200, [
      {
        name: 'exerciseName',
        description: 'exerciseDescription',
        movement_type: 'movementType',
        is_unilateral: true,
        is_upper: true,
        is_accessory: false,
      },
    ]);
    mock.onGet('/user_weight_unit_preference/test-user-id').reply(404);

    await act(async () => {
      renderWithProviders(<WorkoutPreferencesSection />);
    });

    // Wait for loading to complete
    await waitFor(
      () => {
        expect(screen.queryByRole('progressbar')).not.toBeInTheDocument();
      },
      { timeout: 10000 }
    );

    // Then check for the content
    expect(screen.getAllByText('Weight Unit Preferences')).toHaveLength(2); // Main heading and card heading
    expect(
      screen.getByText(
        'Set your preferred weight units for specific exercises. Search and select multiple exercises at once.'
      )
    ).toBeInTheDocument();
  });

  it('should show add preference button for weight units', async () => {
    // Mock API responses
    mock.onGet('/exercise/').reply(200, []);
    mock.onGet('/user_weight_unit_preference/test-user-id').reply(404);

    await act(async () => {
      renderWithProviders(<WorkoutPreferencesSection />);
    });

    // Wait for loading to complete first
    await waitFor(
      () => {
        expect(screen.queryByRole('progressbar')).not.toBeInTheDocument();
      },
      { timeout: 10000 }
    );

    expect(screen.getAllByText('Add Preference')).toHaveLength(1); // Only the active tab (Weight units) is visible
  });

  it('should open dialog when add preference button is clicked', async () => {
    // Mock API responses
    mock.onGet('/exercise/').reply(200, []);
    mock.onGet('/user_weight_unit_preference/test-user-id').reply(404);

    await act(async () => {
      renderWithProviders(<WorkoutPreferencesSection />);
    });

    // Wait for loading to complete first
    await waitFor(
      () => {
        expect(screen.queryByRole('progressbar')).not.toBeInTheDocument();
      },
      { timeout: 10000 }
    );

    const addButtons = screen.getAllByText('Add Preference');
    // Click the first "Add Preference" button (weight units section)
    fireEvent.click(addButtons[0]);

    expect(screen.getByText('Add Weight Unit Preferences')).toBeInTheDocument();
    expect(screen.getAllByText('Exercises')[0]).toBeInTheDocument();
    expect(screen.getAllByText('Preferred Unit')[0]).toBeInTheDocument();
  });

  it('should show no preferences message when no weight unit preferences exist', async () => {
    // Mock API responses
    mock.onGet('/exercise/').reply(200, []);
    mock.onGet('/user_weight_unit_preference/test-user-id').reply(404);

    await act(async () => {
      renderWithProviders(<WorkoutPreferencesSection />);
    });

    // Wait for loading to complete first
    await waitFor(
      () => {
        expect(screen.queryByRole('progressbar')).not.toBeInTheDocument();
      },
      { timeout: 10000 }
    );

    expect(screen.getByText('No weight unit preferences set yet.')).toBeInTheDocument();
  });

  it('should filter and sort weight unit preferences', async () => {
    const preferences = [
      {
        user_id: 'test-user-id',
        exercise_name: 'Squat',
        preferred_unit: 'KG',
        created_at: new Date(),
        updated_at: new Date(),
      },
      {
        user_id: 'test-user-id',
        exercise_name: 'Bench Press',
        preferred_unit: 'LBS',
        created_at: new Date(),
        updated_at: new Date(),
      },
      {
        user_id: 'test-user-id',
        exercise_name: 'Deadlift',
        preferred_unit: 'LBS',
        created_at: new Date(),
        updated_at: new Date(),
      },
    ];

    mockUseData.mockReturnValue({
      userData: null,
      exerciseMuscleData: new Map(),
      weightUnitPreferences: preferences,
      userEquipment: [],
      userWeakMuscles: [],
      userExercisePreferences: [],
      isLoading: false,
      error: null,
      refreshData: jest.fn(),
      refreshSpecificData: jest.fn(),
      isDataStale: false,
      loadAllExercises: jest.fn().mockResolvedValue([
        {
          name: 'Bench Press',
          description: '',
          movement_type: 'press',
          is_unilateral: false,
          is_upper: true,
          is_accessory: false,
        },
        {
          name: 'Deadlift',
          description: '',
          movement_type: 'hinge',
          is_unilateral: false,
          is_upper: false,
          is_accessory: false,
        },
        {
          name: 'Squat',
          description: '',
          movement_type: 'squat',
          is_unilateral: false,
          is_upper: false,
          is_accessory: false,
        },
      ]),
      loadAllMuscles: jest.fn().mockResolvedValue([]),
      loadAllEquipment: jest.fn().mockResolvedValue([]),
      loadUserEquipment: jest.fn().mockResolvedValue([]),
      loadUserWeakMuscles: jest.fn().mockResolvedValue([]),
      loadUserExercisePreferences: jest.fn().mockResolvedValue([]),
      loadUserWeightUnitPreferences: jest.fn().mockResolvedValue([]),
    });

    await act(async () => {
      renderWithProviders(<WorkoutPreferencesSection />);
    });

    await waitFor(
      () => {
        expect(screen.queryByRole('progressbar')).not.toBeInTheDocument();
      },
      { timeout: 10000 }
    );

    expect(screen.getByText('Bench Press')).toBeInTheDocument();
    expect(screen.getByText('Deadlift')).toBeInTheDocument();
    expect(screen.getByText('Squat')).toBeInTheDocument();

    fireEvent.change(screen.getByLabelText('Filter exercises'), {
      target: { value: 'bench' },
    });

    expect(screen.getByText('Bench Press')).toBeInTheDocument();
    expect(screen.queryByText('Deadlift')).not.toBeInTheDocument();
    expect(screen.queryByText('Squat')).not.toBeInTheDocument();

    fireEvent.change(screen.getByLabelText('Filter exercises'), {
      target: { value: '' },
    });
    fireEvent.click(screen.getByLabelText('Show pounds only'));

    expect(screen.getByText('Bench Press')).toBeInTheDocument();
    expect(screen.getByText('Deadlift')).toBeInTheDocument();
    expect(screen.queryByText('Squat')).not.toBeInTheDocument();

    expect(screen.getByLabelText('Set Bench Press to pounds')).toHaveAttribute(
      'aria-pressed',
      'true'
    );
  });

  it('should allow selecting multiple exercises in the weight unit preference dialog', async () => {
    const user = userEvent.setup();
    mockUseData.mockReturnValue({
      userData: null,
      exerciseMuscleData: new Map(),
      weightUnitPreferences: [],
      userEquipment: [],
      userWeakMuscles: [],
      userExercisePreferences: [],
      isLoading: false,
      error: null,
      refreshData: jest.fn(),
      refreshSpecificData: jest.fn(),
      isDataStale: false,
      loadAllExercises: jest.fn().mockResolvedValue([
        {
          name: 'Bench Press',
          description: '',
          movement_type: 'press',
          is_unilateral: false,
          is_upper: true,
          is_accessory: false,
        },
        {
          name: 'Squat',
          description: '',
          movement_type: 'squat',
          is_unilateral: false,
          is_upper: false,
          is_accessory: false,
        },
      ]),
      loadAllMuscles: jest.fn().mockResolvedValue([]),
      loadAllEquipment: jest.fn().mockResolvedValue([]),
      loadUserEquipment: jest.fn().mockResolvedValue([]),
      loadUserWeakMuscles: jest.fn().mockResolvedValue([]),
      loadUserExercisePreferences: jest.fn().mockResolvedValue([]),
      loadUserWeightUnitPreferences: jest.fn().mockResolvedValue([]),
    });

    await act(async () => {
      renderWithProviders(<WorkoutPreferencesSection />);
    });

    await waitFor(
      () => {
        expect(screen.queryByRole('progressbar')).not.toBeInTheDocument();
      },
      { timeout: 10000 }
    );

    await user.click(screen.getByText('Add Preference'));
    expect(await screen.findByText('Add Weight Unit Preferences')).toBeInTheDocument();
    expect(
      await screen.findByPlaceholderText('Search and select exercises...')
    ).toBeInTheDocument();

    const exerciseInput = screen.getByPlaceholderText('Search and select exercises...');
    await user.click(exerciseInput);
    await user.click(await screen.findByRole('option', { name: /Bench Press/i }));
    await user.click(await screen.findByRole('option', { name: /Squat/i }));

    expect(await screen.findByRole('button', { name: /Bench Press/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Squat/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Add Preferences' })).toBeInTheDocument();
  }, 15000);

  it('should handle loading state', async () => {
    // Mock slow DataContext functions to ensure loading state is visible
    const slowLoadAllExercises = jest
      .fn()
      .mockImplementation(() => new Promise(resolve => setTimeout(() => resolve([]), 100)));
    const slowLoadAllMuscles = jest
      .fn()
      .mockImplementation(() => new Promise(resolve => setTimeout(() => resolve([]), 100)));
    const slowLoadAllEquipment = jest
      .fn()
      .mockImplementation(() => new Promise(resolve => setTimeout(() => resolve([]), 100)));
    const slowLoadUserEquipment = jest
      .fn()
      .mockImplementation(() => new Promise(resolve => setTimeout(() => resolve([]), 100)));
    const slowLoadUserWeakMuscles = jest
      .fn()
      .mockImplementation(() => new Promise(resolve => setTimeout(() => resolve([]), 100)));
    const slowLoadUserExercisePreferences = jest
      .fn()
      .mockImplementation(() => new Promise(resolve => setTimeout(() => resolve([]), 100)));

    mockUseData.mockReturnValue({
      userData: null,
      exerciseMuscleData: new Map(),
      weightUnitPreferences: [],
      userEquipment: [],
      userWeakMuscles: [],
      userExercisePreferences: [],
      isLoading: false,
      error: null,
      refreshData: jest.fn(),
      refreshSpecificData: jest.fn(),
      isDataStale: false,
      loadAllExercises: slowLoadAllExercises,
      loadAllMuscles: slowLoadAllMuscles,
      loadAllEquipment: slowLoadAllEquipment,
      loadUserEquipment: slowLoadUserEquipment,
      loadUserWeakMuscles: slowLoadUserWeakMuscles,
      loadUserExercisePreferences: slowLoadUserExercisePreferences,
      loadUserWeightUnitPreferences: jest.fn().mockResolvedValue([]),
    });

    await act(async () => {
      renderWithProviders(<WorkoutPreferencesSection />);
    });

    // Check for loading state immediately after rendering
    expect(screen.getByText('Loading workout preferences...')).toBeInTheDocument();
  });

  it('should verify axios mock is working', async () => {
    // Test that the axios mock is working by making a simple request
    mock.onGet('/test').reply(200, { message: 'test' });

    const response = await ENDPOINT.get('/test');
    expect(response.data.message).toBe('test');
  });
});
