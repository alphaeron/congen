import { ThemeProvider, createTheme } from '@mui/material/styles';
import { render, screen, waitFor, act } from '@testing-library/react';
import MockAdapter from 'axios-mock-adapter';
import { SnackbarProvider } from 'notistack';
import React from 'react';
import { MemoryRouter } from 'react-router';

import { WorkoutWeekDetails } from './WorkoutWeekDetails';
import { ENDPOINT } from '../api/endpoint';
import type { ProgrammedWorkout, ProgramPreferences, ProgramWithPreferences } from '../api/types';

// Mock chart components to prevent rendering issues
jest.mock('./RadarChart', () => ({
  RadarChart: (): React.ReactElement => <div data-testid="radar-chart">Mock Radar Chart</div>,
}));

jest.mock('./SunburstChart', () => ({
  SunburstChart: (): React.ReactElement => (
    <div data-testid="sunburst-chart">Mock Sunburst Chart</div>
  ),
}));

jest.mock('./WeekKeyResultsSummary', () => ({
  WeekKeyResultsSummary: (): React.ReactElement => (
    <div data-testid="week-key-results-summary">Mock Week Key Results</div>
  ),
}));

// Mock DataContext
const mockUseData = jest.fn();
jest.mock('../contexts/DataContext', () => ({
  useData: () => mockUseData(),
  DataProvider: ({ children }: { children: React.ReactNode }) => children,
}));

// Mock the useAuth hook to prevent auth context issues
jest.mock('../contexts/AuthContext', () => ({
  useAuth: () => ({
    user: {
      keycloak_id: 'test-user-id',
      name: 'Test User',
      created_at: '2024-01-01T00:00:00.000Z',
      updated_at: '2024-01-01T00:00:00.000Z',
      roles: ['user'],
    },
  }),
}));

describe('WorkoutWeekDetails', () => {
  // Create a new mock adapter for each test to prevent interference
  let mock: MockAdapter;
  const theme = createTheme();
  let defaultMockDataContext: unknown;

  const mockUser = {
    keycloak_id: 'test-user-id',
    name: 'Test User',
    created_at: '2024-01-01T00:00:00.000Z',
    updated_at: '2024-01-01T00:00:00.000Z',
    roles: ['user'],
  };

  const renderWithProviders = (component: React.ReactElement) => {
    return render(
      <SnackbarProvider>
        <MemoryRouter>
          <ThemeProvider theme={theme}>{component}</ThemeProvider>
        </MemoryRouter>
      </SnackbarProvider>
    );
  };

  beforeEach(() => {
    defaultMockDataContext = {
      userData: {
        training_programs: [
          {
            program: { id: 1, name: 'Test Program', is_active: true },
            workouts: [],
          },
        ],
      },
      exerciseMuscleData: new Map(),
      weightUnitPreferences: [],
      exerciseData: new Map(),
      exerciseEquipmentData: new Map(),
      muscleData: new Map(),
      equipmentData: new Map(),
      programData: new Map(),
      allExercises: [],
      allMuscles: [],
      allEquipment: [],
      userEquipment: [],
      userWeakMuscles: [],
      userExercisePreferences: [],
      programPreferences: [],
      programmedWorkouts: [],
      userOneRepMaxes: [],
      userConsent: null,
      userExercisePool: null,
      dashboardStats: null,
      isLoading: false,
      error: null,
      refreshData: jest.fn(),
      isDataStale: false,
      getExercise: jest.fn(),
      getExerciseMuscles: jest.fn(),
      getExerciseEquipmentData: jest.fn(),
      getMuscle: jest.fn(),
      getEquipment: jest.fn(),
      getProgram: jest.fn(),
      loadAllExercises: jest.fn(),
      loadAllMuscles: jest.fn(),
      loadAllEquipment: jest.fn(),
      loadUserEquipment: jest.fn(),
      loadUserWeakMuscles: jest.fn(),
      loadUserExercisePreferences: jest.fn(),
      loadProgramPreferences: jest.fn(),
      loadProgrammedWorkouts: jest.fn(),
      loadUserOneRepMaxes: jest.fn(),
      loadUserConsent: jest.fn(),
      loadUserExercisePool: jest.fn(),
      loadDashboardStats: jest.fn(),
      updateUserConsent: jest.fn(),
      getProgramPreferencesById: jest.fn(),
      loadAllExercisesForComponents: jest.fn(),
      invalidateCache: jest.fn(),
      refreshSpecificData: jest.fn(),
      isLoadingSpecific: jest.fn(),
      getErrorForDataType: jest.fn(),
      clearError: jest.fn(),
      prefetchData: jest.fn(),
      prefetchRelatedData: jest.fn(),
      isOnline: true,
      syncPendingChanges: jest.fn(),
      getOfflineData: jest.fn(),
      getRelatedData: jest.fn(),
      updateDataRelationships: jest.fn(),
      predictivePrefetch: jest.fn(),
      compressData: jest.fn(),
      decompressData: jest.fn(),
      resolveDataConflicts: jest.fn(),
      syncWithServer: jest.fn(),
    };

    mockUseData.mockReturnValue(defaultMockDataContext);
  });

  const mockProgramPreferences: ProgramPreferences = {
    program_id: 1,
    program_days_per_week: 3,
    session_time_length_in_minutes: 60,
    created_at: new Date('2024-01-01T00:00:00.000Z'),
    updated_at: new Date('2024-01-01T00:00:00.000Z'),
  };

  const mockProgramWithPreferences: ProgramWithPreferences = {
    program: {
      id: 1,
      user_id: 'test-user-id',
      name: 'Test Program',
      current_week_number: 2,
      created_at: new Date('2024-01-01T00:00:00.000Z'),
      updated_at: new Date('2024-01-01T00:00:00.000Z'),
      is_active: true,
    },
    program_preferences: mockProgramPreferences,
  };

  const mockWorkout: ProgrammedWorkout = {
    id: 1,
    program_id: 1,
    day_number: 1,
    name: 'Push Day',
    created_at: new Date('2024-01-01T00:00:00.000Z'),
    updated_at: new Date('2024-01-01T00:00:00.000Z'),
  };

  beforeEach(() => {
    // Create a fresh mock adapter for each test
    mock = new MockAdapter(ENDPOINT);
    jest.clearAllMocks();

    // Mock OIDC auth

    // Mock all the API calls that the component makes
    mock.onGet('/exercise/').reply(200, []);
    mock.onGet('/gdpr/export').reply(200, { training_programs: [], data_retention_policies: [] });
    mock.onGet('/exercise_muscle/').reply(200, []);
    mock.onGet('/user_weight_unit_preference/test-user-id').reply(200, []);
    mock.onGet('/user/me').reply(200, {
      keycloak_id: 'test-user-id',
      name: 'Test User',
      created_at: new Date('2024-01-01T00:00:00.000Z'),
      updated_at: new Date('2024-01-01T00:00:00.000Z'),
      roles: ['user'],
    });
  });

  afterEach(() => {
    // Properly clean up the mock adapter
    if (mock) {
      mock.restore();
    }
  });

  it('renders component without errors', async () => {
    mock.onGet('/program/with-preferences').reply(200, []);
    mock.onGet('/programmed_workout/').reply(200, []);
    // Mock WorkoutDetail dependencies
    mock.onGet('/gdpr/export').reply(200, {
      training_programs: [],
      data_retention_policies: [],
    });

    await act(async () => {
      renderWithProviders(<WorkoutWeekDetails user={mockUser} weekNumber={1} />);
    });

    // Should render the component without errors
    await waitFor(
      () => {
        expect(screen.getByText('No Active Program')).toBeInTheDocument();
      },
      { timeout: 15000 }
    );
  }, 20000);

  it('displays no active program message when no active program exists', async () => {
    const inactiveProgram = {
      ...mockProgramWithPreferences,
      program: { ...mockProgramWithPreferences.program, is_active: false },
    };
    mock.onGet('/program/with-preferences').reply(200, [inactiveProgram]);
    mock.onGet('/programmed_workout/').reply(200, []);
    // Mock WorkoutDetail dependencies
    mock.onGet('/gdpr/export').reply(200, {
      training_programs: [],
      data_retention_policies: [],
    });

    await act(async () => {
      renderWithProviders(<WorkoutWeekDetails user={mockUser} weekNumber={1} />);
    });

    await waitFor(
      () => {
        expect(screen.getByText(/No Active Program/)).toBeInTheDocument();
      },
      { timeout: 10000 }
    );
  });

  it('displays week information when active program exists', async () => {
    // Override the mock to provide active program data
    const activeProgramMockDataContext = {
      ...defaultMockDataContext,
      userData: {
        training_programs: [
          {
            program: mockProgramWithPreferences.program,
            workouts: [
              {
                workout: mockWorkout,
                stages: [],
              },
            ],
          },
        ],
      },
      programPreferences: [mockProgramWithPreferences],
      programmedWorkouts: [mockWorkout],
      loadProgramPreferences: jest.fn().mockResolvedValue([mockProgramWithPreferences]),
    };

    mockUseData.mockReturnValue(activeProgramMockDataContext);

    mock.onGet('/program/with-preferences').reply(200, [mockProgramWithPreferences]);
    mock.onGet('/programmed_workout/').reply(200, [mockWorkout]);
    // Mock WorkoutDetail dependencies
    mock.onGet('/gdpr/export').reply(200, {
      training_programs: [
        {
          program: mockProgramWithPreferences.program,
          program_preferences: mockProgramWithPreferences.program_preferences,
          workouts: [
            {
              workout: mockWorkout,
              stages: [],
            },
          ],
        },
      ],
      data_retention_policies: [],
    });

    await act(async () => {
      renderWithProviders(<WorkoutWeekDetails user={mockUser} weekNumber={1} />);
    });

    await waitFor(
      () => {
        expect(screen.getByText(/Week 1 of 2/)).toBeInTheDocument();
        expect(screen.getByText('Workouts')).toBeInTheDocument();
      },
      { timeout: 10000 }
    );
  }, 15000);

  it('displays week one of one when current_week_number is zero for new program', async () => {
    const newProgramWithPreferences: ProgramWithPreferences = {
      ...mockProgramWithPreferences,
      program: {
        ...mockProgramWithPreferences.program,
        current_week_number: 0,
      },
    };

    const newProgramMockDataContext = {
      ...defaultMockDataContext,
      userData: {
        training_programs: [
          {
            program: newProgramWithPreferences.program,
            workouts: [
              {
                workout: mockWorkout,
                stages: [],
              },
            ],
          },
        ],
      },
      programPreferences: [newProgramWithPreferences],
      programmedWorkouts: [mockWorkout],
      loadProgramPreferences: jest.fn().mockResolvedValue([newProgramWithPreferences]),
    };

    mockUseData.mockReturnValue(newProgramMockDataContext);
    mock.onGet('/program/with-preferences').reply(200, [newProgramWithPreferences]);

    await act(async () => {
      renderWithProviders(<WorkoutWeekDetails user={mockUser} weekNumber={1} />);
    });

    await waitFor(
      () => {
        expect(screen.getByText(/Week 1 of 1/)).toBeInTheDocument();
      },
      { timeout: 10000 }
    );
  }, 15000);

  it('displays week workouts when workouts exist for the week', async () => {
    const workoutWithStages = {
      workout: {
        id: 1,
        program_id: 1,
        day_number: 1,
        name: 'Test Workout',
        created_at: new Date('2024-01-01T00:00:00.000Z'),
        updated_at: new Date('2024-01-01T00:00:00.000Z'),
        is_completed: false,
      },
      stages: [],
    };

    const workoutDataContext = {
      ...mockUseData(),
      userData: {
        user_id: 'test-user-id',
        user_profile: {
          user_id: 'test-user-id',
          name: 'Test User',
          age: 30,
          weight: 180,
          height: 72,
          gender: 'male',
          created_at: new Date('2024-01-01T00:00:00Z'),
          updated_at: new Date('2024-01-01T00:00:00Z'),
        },
        user_one_rep_max: [],
        user_weight_unit_preferences: [],
        training_programs: [
          {
            program: mockProgramWithPreferences.program,
            workouts: [workoutWithStages],
          },
        ],
        audit_logs: [],
        data_retention_policies: [],
      },
      programPreferences: [mockProgramWithPreferences],
      loadProgramPreferences: jest.fn().mockResolvedValue([mockProgramWithPreferences]),
    };
    mockUseData.mockReturnValue(workoutDataContext);

    // Mock the API call that the component still makes
    mock.onGet('/program/with-preferences').reply(200, [mockProgramWithPreferences]);

    await act(async () => {
      renderWithProviders(<WorkoutWeekDetails user={mockUser} weekNumber={1} />);
    });

    await waitFor(
      () => {
        expect(screen.getByText(/Week 1 of 2/)).toBeInTheDocument();
        expect(screen.getByText('Day 1')).toBeInTheDocument();
        expect(screen.getByText('Test Workout')).toBeInTheDocument();
      },
      { timeout: 10000 }
    );
  }, 15000);

  it('shows no workouts message when no workouts exist for the week', async () => {
    const noWorkoutsDataContext = {
      ...defaultMockDataContext,
      userData: {
        user_id: 'test-user-id',
        user_profile: {
          user_id: 'test-user-id',
          name: 'Test User',
          age: 30,
          weight: 180,
          height: 72,
          gender: 'male',
          created_at: new Date('2024-01-01T00:00:00Z'),
          updated_at: new Date('2024-01-01T00:00:00Z'),
        },
        user_one_rep_max: [],
        user_weight_unit_preferences: [],
        training_programs: [
          {
            program: mockProgramWithPreferences.program,
            workouts: [],
          },
        ],
        audit_logs: [],
        data_retention_policies: [],
      },
      programPreferences: [mockProgramWithPreferences],
      loadProgramPreferences: jest.fn().mockResolvedValue([mockProgramWithPreferences]),
    };

    mockUseData.mockReturnValue(noWorkoutsDataContext);

    mock.onGet('/program/with-preferences').reply(200, [mockProgramWithPreferences]);
    mock.onGet('/programmed_workout/').reply(200, []);
    // Mock WorkoutDetail dependencies
    mock.onGet('/gdpr/export').reply(200, {
      training_programs: [
        {
          program: mockProgramWithPreferences.program,
          program_preferences: mockProgramWithPreferences.program_preferences,
          workouts: [],
        },
      ],
      data_retention_policies: [],
    });

    await act(async () => {
      renderWithProviders(<WorkoutWeekDetails user={mockUser} weekNumber={1} />);
    });

    await waitFor(
      () => {
        expect(screen.getByText(/No workouts found for Week 1/)).toBeInTheDocument();
      },
      { timeout: 10000 }
    );
  });

  it('shows error message when API calls fail', async () => {
    const errorDataContext = {
      ...defaultMockDataContext,
      loadProgramPreferences: jest
        .fn()
        .mockRejectedValue(new Error('Failed to load program preferences')),
    };

    mockUseData.mockReturnValue(errorDataContext);

    mock.onGet('/program/with-preferences').reply(500, { error: 'Internal server error' });
    mock.onGet('/programmed_workout/').reply(200, []);

    await act(async () => {
      renderWithProviders(<WorkoutWeekDetails user={mockUser} weekNumber={1} />);
    });

    await waitFor(
      () => {
        expect(
          screen.getByText('Failed to load additional week data. Please try again.')
        ).toBeInTheDocument();
      },
      { timeout: 15000 }
    );
  }, 20000);

  it('displays multiple workouts for the week', async () => {
    const workout1 = {
      workout: {
        id: 1,
        program_id: 1,
        day_number: 1,
        name: 'Push Day',
        created_at: new Date('2024-01-01T00:00:00.000Z'),
        updated_at: new Date('2024-01-01T00:00:00.000Z'),
        is_completed: false,
      },
      stages: [],
    };
    const workout2 = {
      workout: {
        id: 2,
        program_id: 1,
        day_number: 2,
        name: 'Pull Day',
        created_at: new Date('2024-01-01T00:00:00.000Z'),
        updated_at: new Date('2024-01-01T00:00:00.000Z'),
        is_completed: false,
      },
      stages: [],
    };
    const workout3 = {
      workout: {
        id: 3,
        program_id: 1,
        day_number: 3,
        name: 'Leg Day',
        created_at: new Date('2024-01-01T00:00:00.000Z'),
        updated_at: new Date('2024-01-01T00:00:00.000Z'),
        is_completed: false,
      },
      stages: [],
    };
    // Note: With current_week_number: 2, workouts with day_number 1-2 go to week 1, day_number 3+ go to week 2

    const multipleWorkoutsDataContext = {
      ...mockUseData(),
      userData: {
        user_id: 'test-user-id',
        user_profile: {
          user_id: 'test-user-id',
          name: 'Test User',
          age: 30,
          weight: 180,
          height: 72,
          gender: 'male',
          created_at: new Date('2024-01-01T00:00:00Z'),
          updated_at: new Date('2024-01-01T00:00:00Z'),
        },
        user_one_rep_max: [],
        user_weight_unit_preferences: [],
        training_programs: [
          {
            program: mockProgramWithPreferences.program,
            workouts: [workout1, workout2, workout3],
          },
        ],
        audit_logs: [],
        data_retention_policies: [],
      },
      programPreferences: [mockProgramWithPreferences],
      loadProgramPreferences: jest.fn().mockResolvedValue([mockProgramWithPreferences]),
    };
    mockUseData.mockReturnValue(multipleWorkoutsDataContext);

    // Mock the API call that the component still makes
    mock.onGet('/program/with-preferences').reply(200, [mockProgramWithPreferences]);

    await act(async () => {
      renderWithProviders(<WorkoutWeekDetails user={mockUser} weekNumber={1} />);
    });

    await waitFor(
      () => {
        expect(screen.getByText('Day 1')).toBeInTheDocument();
        expect(screen.getByText('Day 2')).toBeInTheDocument();
        expect(screen.getByText('Push Day')).toBeInTheDocument();
        expect(screen.getByText('Pull Day')).toBeInTheDocument();
        // workout3 (day_number: 3) goes to week 2, not week 1
      },
      { timeout: 10000 }
    );
  }, 20000);

  it('verifies component uses DataContext instead of direct API calls', async () => {
    // Component now uses DataContext, so no direct API calls should be made
    await act(async () => {
      renderWithProviders(<WorkoutWeekDetails user={mockUser} weekNumber={1} />);
    });

    await waitFor(
      () => {
        expect(screen.getByText('No Active Program')).toBeInTheDocument();
      },
      { timeout: 15000 }
    );

    // Verify no direct API calls were made since component uses DataContext
    expect(mock.history.get).toHaveLength(0);
  }, 20000);
});
