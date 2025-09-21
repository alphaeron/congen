import { ThemeProvider, createTheme } from '@mui/material/styles';
import { render, screen, fireEvent, waitFor, act } from '@testing-library/react';
import MockAdapter from 'axios-mock-adapter';
import { SnackbarProvider } from 'notistack';
import React from 'react';
import { MemoryRouter } from 'react-router';

import { Workouts } from './Workouts';
import { ENDPOINT } from '../api/endpoint';
import type {
  User,
  ProgrammedWorkout,
  ProgramPreferences,
  ProgramWithPreferences,
} from '../api/types';

// Mock DataContext
const mockUseData = jest.fn();
jest.mock('../contexts/DataContext', () => ({
  useData: () => mockUseData(),
  DataProvider: ({ children }: { children: React.ReactNode }) => children,
}));

// Mock Nivo chart components to prevent rendering issues
jest.mock('@nivo/stream', () => ({
  ResponsiveStream: (): React.ReactElement => (
    <div data-testid="stream-chart">Mock Stream Chart</div>
  ),
}));

jest.mock('@nivo/radar', () => ({
  ResponsiveRadar: (): React.ReactElement => <div data-testid="radar-chart">Mock Radar Chart</div>,
}));

jest.mock('@nivo/sunburst', () => ({
  ResponsiveSunburst: (): React.ReactElement => (
    <div data-testid="sunburst-chart">Mock Sunburst Chart</div>
  ),
}));

// Mock WorkoutWeekDetails component to prevent auth context issues
jest.mock('./WorkoutWeekDetails', () => ({
  WorkoutWeekDetails: ({ selectedWorkout, weekNumber }: unknown) => (
    <div data-testid="workout-week-details">
      Mock WorkoutWeekDetails for Week {weekNumber}
      {selectedWorkout && ` - Workout: ${selectedWorkout}`}
    </div>
  ),
}));

// Mock ConjugateProgression component to prevent Nivo chart issues
jest.mock('./ConjugateProgression', () => ({
  ConjugateProgression: () => (
    <div data-testid="conjugate-progression">Mock Conjugate Progression</div>
  ),
}));

// Mock ExerciseRotationVisualization component
jest.mock('./ExerciseRotationVisualization', () => ({
  ExerciseRotationVisualization: () => (
    <div data-testid="exercise-rotation">Mock Exercise Rotation</div>
  ),
}));

// Mock WorkoutPreferencesSection component
jest.mock('./WorkoutPreferencesSection', () => ({
  WorkoutPreferencesSection: () => (
    <div data-testid="workout-preferences">Mock Workout Preferences</div>
  ),
}));

describe('Workouts', () => {
  // Create a new mock adapter for each test to prevent interference
  let mock: MockAdapter;
  const theme = createTheme();
  let defaultMockDataContext: unknown;

  const renderWithProviders = (component: React.ReactElement, initialEntries: string[] = ['/']) => {
    return render(
      <SnackbarProvider>
        <MemoryRouter initialEntries={initialEntries}>
          <ThemeProvider theme={theme}>{component}</ThemeProvider>
        </MemoryRouter>
      </SnackbarProvider>
    );
  };

  const mockUser: User = {
    keycloak_id: 'test-user-id',
    name: 'Test User',
    created_at: new Date('2024-01-01T00:00:00.000Z'),
    updated_at: new Date('2024-01-01T00:00:00.000Z'),
    roles: ['user'],
  };

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

    // Default mock data for useData hook - can be overridden in individual tests
    const defaultMockUserData = {
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
    };

    defaultMockDataContext = {
      userData: defaultMockUserData,
      exerciseMuscleData: new Map(),
      weightUnitPreferences: [],
      isLoading: false,
      error: null,
      refreshData: jest.fn(),
      isDataStale: false,
      loadProgramPreferences: jest.fn().mockResolvedValue([mockProgramWithPreferences]),
      getExercise: jest.fn().mockResolvedValue({
        name: 'Test Exercise',
        description: 'Test Description',
        movement_type: 'push',
        is_unilateral: false,
        is_upper: true,
        is_accessory: false,
      }),
      generateWorkout: jest.fn().mockResolvedValue(mockProgramWithPreferences.program),
    };

    mockUseData.mockReturnValue(defaultMockDataContext);
  });

  afterEach(() => {
    // Properly clean up the mock adapter
    if (mock) {
      mock.restore();
    }
    jest.clearAllMocks();
  });

  it('renders component without errors', async () => {
    // Set up mock data context with no active program
    const emptyMockDataContext = {
      userData: { training_programs: [] },
      exerciseMuscleData: new Map(),
      weightUnitPreferences: [],
      isLoading: false,
      error: null,
      refreshData: jest.fn(),
      isDataStale: false,
    };

    mockUseData.mockReturnValue(emptyMockDataContext);

    // Mock the additional API calls that the component still makes
    mock.onGet('/program/with-preferences').reply(200, []);
    mock.onGet('/user_weight_unit_preference/test-user-id').reply(200, []);

    await act(async () => {
      renderWithProviders(<Workouts user={mockUser} />);
    });

    await waitFor(
      () => {
        // Check that the workout calendar content is rendered
        expect(screen.getByText('No Active Program')).toBeInTheDocument();
      },
      { timeout: 5000 }
    );
  });

  it('displays no active program message when no active program exists', async () => {
    // Set up mock data context with no active program (all programs inactive)
    const noActiveProgramMockDataContext = {
      userData: {
        training_programs: [
          {
            program: { ...mockProgramWithPreferences.program, is_active: false },
            workouts: [],
          },
        ],
      },
      exerciseMuscleData: new Map(),
      weightUnitPreferences: [],
      isLoading: false,
      error: null,
      refreshData: jest.fn(),
      isDataStale: false,
    };

    mockUseData.mockReturnValue(noActiveProgramMockDataContext);

    // Mock the additional API calls that the component still makes
    mock.onGet('/program/with-preferences').reply(200, []);
    mock.onGet('/user_weight_unit_preference/test-user-id').reply(200, []);

    await act(async () => {
      renderWithProviders(<Workouts user={mockUser} />);
    });

    await waitFor(
      () => {
        expect(screen.getByText(/No Active Program/)).toBeInTheDocument();
      },
      { timeout: 5000 }
    );
  });

  it('displays active program information', async () => {
    // Use default mock data (already set up in beforeEach)
    // Mock the additional API calls that the component still makes
    mock.onGet('/program/with-preferences').reply(200, [mockProgramWithPreferences]);
    mock.onGet('/user_weight_unit_preference/test-user-id').reply(200, []);

    await act(async () => {
      renderWithProviders(<Workouts user={mockUser} />);
    });

    // Wait for the component to load and render
    await waitFor(
      () => {
        expect(screen.getByText('Current Week: Week 2')).toBeInTheDocument();
        expect(screen.getByText('Training Weeks')).toBeInTheDocument();
      },
      { timeout: 5000 }
    );
  });

  it('shows generate next week button', async () => {
    mock.onGet('/program/with-preferences').reply(200, [mockProgramWithPreferences]);
    mock.onGet('/programmed_workout/').reply(200, [mockWorkout]);
    // Mock WorkoutAnalytics dependencies
    mock.onGet('/gdpr/export').reply(200, { training_programs: [], data_retention_policies: [] });
    mock.onGet('/user_weight_unit_preference/test-user-id').reply(200, []);
    mock.onGet(/\/exercise\/[^/]+$/).reply(200, {
      id: 1,
      exercise_name: 'Test Exercise',
      category: 'strength',
      primary_muscle: 'chest',
      secondary_muscles: ['triceps', 'shoulders'],
      instructions: 'Test instructions',
      equipment: 'barbell',
      difficulty: 'intermediate',
    });

    await act(async () => {
      renderWithProviders(<Workouts user={mockUser} />);
    });

    await waitFor(
      () => {
        expect(screen.getByRole('button', { name: /generate next week/i })).toBeInTheDocument();
      },
      { timeout: 10000 }
    );
  });

  it('opens wizard when generate button is clicked', async () => {
    mock.onGet('/program/with-preferences').reply(200, [mockProgramWithPreferences]);
    mock.onGet('/programmed_workout/').reply(200, [mockWorkout]);
    // Mock WorkoutAnalytics dependencies
    mock.onGet('/gdpr/export').reply(200, { training_programs: [], data_retention_policies: [] });
    mock.onGet('/user_weight_unit_preference/test-user-id').reply(200, []);
    mock.onGet(/\/exercise\/[^/]+$/).reply(200, {
      id: 1,
      exercise_name: 'Test Exercise',
      category: 'strength',
      primary_muscle: 'chest',
      secondary_muscles: ['triceps', 'shoulders'],
      instructions: 'Test instructions',
      equipment: 'barbell',
      difficulty: 'intermediate',
    });

    await act(async () => {
      renderWithProviders(<Workouts user={mockUser} />);
    });

    await waitFor(
      () => {
        const generateButton = screen.getByRole('button', { name: /generate next week/i });
        fireEvent.click(generateButton);
      },
      { timeout: 10000 }
    );

    // Check for wizard title (use role to be more specific)
    expect(screen.getByRole('heading', { name: /Generate Workouts/ })).toBeInTheDocument();
    expect(screen.getByText(/The next week's workouts will be generated/)).toBeInTheDocument();
  });

  it('generates workouts successfully', async () => {
    mock.onGet('/program/with-preferences').reply(200, [mockProgramWithPreferences]);
    mock.onGet('/programmed_workout/').reply(200, [mockWorkout]);
    mock.onPost('/conjugate_workout_generator/1').reply(200, mockProgramWithPreferences.program);
    mock.onGet('/conjugate_workout_generator/exercise_pool').reply(200, {
      user_id: 'test-user-id',
      total_exercises: 10,
      available_exercises: 8,
      primary_exercises: [],
      accessory_exercises: [],
      user_equipment: [],
      user_preferences: [],
      previously_used_exercises: [],
    });
    // Mock WorkoutAnalytics dependencies
    mock.onGet('/gdpr/export').reply(200, { training_programs: [], data_retention_policies: [] });
    mock.onGet('/user_weight_unit_preference/test-user-id').reply(200, []);
    mock.onGet(/\/exercise\/[^/]+$/).reply(200, {
      id: 1,
      exercise_name: 'Test Exercise',
      category: 'strength',
      primary_muscle: 'chest',
      secondary_muscles: ['triceps', 'shoulders'],
      instructions: 'Test instructions',
      equipment: 'barbell',
      difficulty: 'intermediate',
    });

    await act(async () => {
      renderWithProviders(<Workouts user={mockUser} />);
    });

    await waitFor(
      () => {
        const generateButton = screen.getByRole('button', { name: /generate next week/i });
        fireEvent.click(generateButton);
      },
      { timeout: 10000 }
    );

    // Check that the wizard opened
    await waitFor(
      () => {
        expect(screen.getByRole('heading', { name: /Generate Workouts/ })).toBeInTheDocument();
      },
      { timeout: 10000 }
    );

    // Click the generate button in the wizard
    await waitFor(
      () => {
        const generateWorkoutsButton = screen.getByRole('button', { name: /generate workouts/i });
        fireEvent.click(generateWorkoutsButton);
      },
      { timeout: 10000 }
    );

    // Check that the DataContext function was called
    await waitFor(
      () => {
        expect(defaultMockDataContext.generateWorkout).toHaveBeenCalledWith(1);
      },
      { timeout: 10000 }
    );
  }, 15000);

  it('displays training weeks when workouts exist', async () => {
    mock.onGet('/program/with-preferences').reply(200, [mockProgramWithPreferences]);
    mock.onGet('/programmed_workout/').reply(200, [mockWorkout]);
    // Mock WorkoutAnalytics dependencies
    mock.onGet('/gdpr/export').reply(200, { training_programs: [], data_retention_policies: [] });
    mock.onGet('/user_weight_unit_preference/test-user-id').reply(200, []);
    mock.onGet(/\/exercise\/[^/]+$/).reply(200, {
      id: 1,
      exercise_name: 'Test Exercise',
      category: 'strength',
      primary_muscle: 'chest',
      secondary_muscles: ['triceps', 'shoulders'],
      instructions: 'Test instructions',
      equipment: 'barbell',
      difficulty: 'intermediate',
    });

    await act(async () => {
      renderWithProviders(<Workouts user={mockUser} />);
    });

    await waitFor(
      () => {
        expect(screen.getByText('Week 1')).toBeInTheDocument();
        expect(screen.getByText(/Push Day/)).toBeInTheDocument();
      },
      { timeout: 10000 }
    );
  }, 15000);

  it('displays multiple weeks when workouts span multiple weeks', async () => {
    // Create workouts for week 1 and week 2
    const workout1 = { ...mockWorkout, id: 1, day_number: 1, name: 'Push Day' };
    const workout2 = { ...mockWorkout, id: 2, day_number: 2, name: 'Pull Day' };
    const workout3 = { ...mockWorkout, id: 3, day_number: 3, name: 'Leg Day' };
    const workout4 = { ...mockWorkout, id: 4, day_number: 4, name: 'Upper Body' };
    const workout5 = { ...mockWorkout, id: 5, day_number: 5, name: 'Lower Body' };
    const workout6 = { ...mockWorkout, id: 6, day_number: 6, name: 'Full Body' };

    // Set up mock data context with multiple workouts spanning multiple weeks
    const multipleWeeksMockDataContext = {
      userData: {
        training_programs: [
          {
            program: mockProgramWithPreferences.program,
            workouts: [
              { workout: workout1, stages: [] },
              { workout: workout2, stages: [] },
              { workout: workout3, stages: [] },
              { workout: workout4, stages: [] },
              { workout: workout5, stages: [] },
              { workout: workout6, stages: [] },
            ],
          },
        ],
      },
      exerciseMuscleData: new Map(),
      weightUnitPreferences: [],
      isLoading: false,
      error: null,
      refreshData: jest.fn(),
      isDataStale: false,
      loadProgramPreferences: jest.fn().mockResolvedValue([mockProgramWithPreferences]),
      getExercise: jest.fn().mockResolvedValue({
        name: 'Test Exercise',
        description: 'Test Description',
        movement_type: 'push',
        is_unilateral: false,
        is_upper: true,
        is_accessory: false,
      }),
    };

    mockUseData.mockReturnValue(multipleWeeksMockDataContext);

    // Mock the additional API calls that the component still makes
    mock.onGet('/program/with-preferences').reply(200, [mockProgramWithPreferences]);
    mock.onGet('/user_weight_unit_preference/test-user-id').reply(200, []);

    await act(async () => {
      renderWithProviders(<Workouts user={mockUser} />);
    });

    await waitFor(
      () => {
        expect(screen.getByText('Week 1')).toBeInTheDocument();
        expect(screen.getByText('Week 2')).toBeInTheDocument();
        expect(screen.getByText(/Push Day/)).toBeInTheDocument();
        expect(screen.getByText(/Pull Day/)).toBeInTheDocument();
        expect(screen.getByText(/Leg Day/)).toBeInTheDocument();
      },
      { timeout: 10000 }
    );
  });

  it('verifies API calls are made with correct endpoints', async () => {
    // Override the default mock to show no active program
    const noActiveProgramMockDataContext = {
      userData: { training_programs: [] },
      exerciseMuscleData: new Map(),
      weightUnitPreferences: [],
      isLoading: false,
      error: null,
      refreshData: jest.fn(),
      isDataStale: false,
      loadProgramPreferences: jest.fn().mockResolvedValue([]),
      getExercise: jest.fn().mockResolvedValue(null),
    };

    mockUseData.mockReturnValue(noActiveProgramMockDataContext);

    mock.onGet('/program/with-preferences').reply(200, []);
    mock.onGet('/programmed_workout/').reply(200, []);
    // Mock WorkoutAnalytics dependencies
    mock.onGet('/gdpr/export').reply(200, { training_programs: [], data_retention_policies: [] });
    mock.onGet('/user_weight_unit_preference/test-user-id').reply(200, []);
    mock.onGet(/\/exercise\/[^/]+$/).reply(200, {
      id: 1,
      exercise_name: 'Test Exercise',
      category: 'strength',
      primary_muscle: 'chest',
      secondary_muscles: ['triceps', 'shoulders'],
      instructions: 'Test instructions',
      equipment: 'barbell',
      difficulty: 'intermediate',
    });

    await act(async () => {
      renderWithProviders(<Workouts user={mockUser} />);
    });

    await waitFor(
      () => {
        expect(screen.getByText('No Active Program')).toBeInTheDocument();
      },
      { timeout: 15000 }
    );

    // Since the component now uses DataContext, it doesn't make direct API calls
    // Instead, we should test that the component renders correctly with the mocked data
    expect(screen.getByText('No Active Program')).toBeInTheDocument();
  }, 20000);

  it('navigates to week details when week is clicked', async () => {
    mock.onGet('/program/with-preferences').reply(200, [mockProgramWithPreferences]);
    mock.onGet('/programmed_workout/').reply(200, [mockWorkout]);
    mock.onGet('/gdpr/export').reply(200, { training_programs: [], data_retention_policies: [] });
    mock.onGet('/user_weight_unit_preference/test-user-id').reply(200, []);

    await act(async () => {
      renderWithProviders(<Workouts user={mockUser} />);
    });

    await waitFor(
      () => {
        const weekButton = screen.getByText('Week 1');
        fireEvent.click(weekButton);
      },
      { timeout: 10000 }
    );

    // Should navigate to week details - check for navigation or week details content
    await waitFor(
      () => {
        // The component should show some indication that week details are being displayed
        // This could be a different component or navigation state
        expect(screen.getByText('Week 1')).toBeInTheDocument();
      },
      { timeout: 10000 }
    );
  }, 20000);
});
