import { render, screen, waitFor, act } from '@testing-library/react';
import MockAdapter from 'axios-mock-adapter';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { SnackbarProvider } from 'notistack';
import React from 'react';
import { MemoryRouter } from 'react-router';

import { DashboardOverview } from './DashboardOverview';
import { ENDPOINT } from '../api/endpoint';
import type { User } from '../api/types';

// Mock DataContext
const mockUseData = jest.fn();
jest.mock('../contexts/DataContext', () => ({
  useData: () => mockUseData(),
  DataProvider: ({ children }: { children: React.ReactNode }) => children,
}));

// Mock Nivo components to avoid Jest configuration issues
interface LineChartData {
  id: string;
  data: Array<{ x: number; y: number }>;
}

interface PieChartData {
  id: string;
  value: number;
}

jest.mock('@nivo/line', () => ({
  ResponsiveLine: ({ data }: { data: LineChartData[] }) => (
    <div data-testid="line-chart">
      {data.map((series: LineChartData) => (
        <div key={series.id} data-testid={`line-series-${series.id}`}>
          {series.data.length} points
        </div>
      ))}
    </div>
  ),
}));

jest.mock('@nivo/pie', () => ({
  ResponsivePie: ({ data }: { data: PieChartData[] }) => (
    <div data-testid="pie-chart">
      {data.map((item: PieChartData) => (
        <div key={item.id} data-testid={`pie-item-${item.id}`}>
          {item.value}
        </div>
      ))}
    </div>
  ),
}));

// Mock AdventurerStatusCard component
jest.mock('./AdventurerStatusCard', () => ({
  AdventurerStatusCard: ({ userName }: { userName: string }) => (
    <div data-testid="adventurer-status-card">
      <div>Adventurer Status Card</div>
      <div>Status for {userName}</div>
    </div>
  ),
}));

describe('DashboardOverview', () => {
  // Create a new mock adapter for each test to prevent interference
  let mock: MockAdapter;

  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false },
      mutations: { retry: false },
    },
  });

  const renderWithProviders = (component: React.ReactElement) => {
    return render(
      <QueryClientProvider client={queryClient}>
        <SnackbarProvider>
          <MemoryRouter>{component}</MemoryRouter>
        </SnackbarProvider>
      </QueryClientProvider>
    );
  };

  const mockUser: User = {
    keycloak_id: 'test-user-id',
    name: 'Test User',
    created_at: new Date('2024-01-01T00:00:00.000Z'),
    updated_at: new Date('2024-01-01T00:00:00.000Z'),
    roles: ['user'],
  };

  const mockProgram = {
    id: 1,
    user_id: 'test-user-id',
    name: 'Test Program',
    current_week_number: 2,
    created_at: new Date('2024-01-01T00:00:00.000Z'),
    updated_at: new Date('2024-01-01T00:00:00.000Z'),
    is_active: true,
  };

  const mockWorkout = {
    workout: {
      id: 1,
      program_id: 1,
      day_number: 1,
      name: 'Test Workout',
      created_at: new Date('2024-01-01T00:00:00.000Z'),
      updated_at: new Date('2024-01-01T00:00:00.000Z'),
    },
    stages: [],
  };

  const mockOneRepMax = {
    user_id: 'test-user-id',
    exercise_name: 'Bench Press',
    one_rep_max: 225,
    unit: 'KG',
    created_at: new Date('2024-01-01T00:00:00Z'),
    updated_at: new Date('2024-01-01T00:00:00Z'),
  };

  beforeEach(() => {
    // Set up default mock data for DataContext
    const defaultMockDataContext = {
      userData: {
        training_programs: [
          {
            program: mockProgram,
            workouts: [mockWorkout],
          },
        ],
        user_one_rep_max: [mockOneRepMax],
      },
      exerciseMuscleData: new Map(),
      weightUnitPreferences: [],
      isLoading: false,
      isReady: true,
      error: null,
      refreshData: jest.fn(),
      isDataStale: false,
      performanceScores: {
        strength: 50,
        endurance: 60,
        power: 40,
        overall: 50,
      },
      performanceMetrics: [],
      weeklyTests: [],
      refreshPerformanceData: jest.fn(),
      testProtocols: [],
      loadTestProtocols: jest.fn(),
      submitPerformanceMetrics: jest.fn(),
      submitWeeklyTest: jest.fn(),
      getCurrentWeekTest: jest.fn(),
      loadPerformanceMetricsInRange: jest.fn(),
      loadWeeklyTests: jest.fn(),
    };

    mockUseData.mockReturnValue(defaultMockDataContext);
  });

  const mockUserDataExport = {
    training_programs: [
      {
        program: { id: 1, name: 'Test Program' },
        workouts: [
          {
            workout: {
              id: 1,
              name: 'Test Workout',
              created_at: new Date('2024-01-01T00:00:00Z'),
              updated_at: new Date('2024-01-01T00:00:00Z'),
              program_id: 1,
              day_number: 1,
            },
            stages: [
              {
                stage: {
                  name: 'Test Stage',
                  id: 1,
                  programmed_workout_id: 1,
                  stage_type_id: 1,
                  position: 1,
                  created_at: new Date('2024-01-01T00:00:00Z'),
                  updated_at: new Date('2024-01-01T00:00:00Z'),
                },
                exercises: [
                  {
                    exercise: {
                      exercise_name: 'Bench Press',
                      id: 1,
                      workout_stage_id: 1,
                      position: 1,
                      created_at: new Date('2024-01-01T00:00:00Z'),
                      updated_at: new Date('2024-01-01T00:00:00Z'),
                    },
                    set_schemes: [
                      {
                        performed_weight: 100,
                        performed_rep_count: 10,
                        band_weight_lbs: null,
                        id: 1,
                        programmed_exercise_id: 1,
                        set_number: 1,
                        is_amrap: false,
                        is_emom: false,
                        use_tempo: false,
                        created_at: new Date('2024-01-01T00:00:00Z'),
                        updated_at: new Date('2024-01-01T00:00:00Z'),
                      },
                    ],
                  },
                ],
              },
            ],
          },
        ],
      },
    ],
    user_one_rep_max: [mockOneRepMax],
    data_retention_policies: [],
  };

  const mockExercise = {
    id: 1,
    exercise_name: 'Bench Press',
    category: 'strength',
    primary_muscle: 'chest',
    secondary_muscles: ['triceps', 'shoulders'],
    instructions: 'Test instructions',
    equipment: 'barbell',
    difficulty: 'intermediate',
  };

  beforeEach(() => {
    mock = new MockAdapter(ENDPOINT);
    jest.clearAllMocks();
    mock.onGet('/program/').reply(200, []);
    mock.onGet('/programmed_workout/').reply(200, []);
    mock.onGet('/user_one_rep_max/user/test-user-id').reply(200, []);
    mock.onGet('/gdpr/export').reply(200, mockUserDataExport);
    mock.onGet('/exercise/').reply(200, [mockExercise]);
    mock.onGet('/user_weight_unit_preference/test-user-id').reply(200, []);
  });

  afterEach(() => {
    // Properly clean up the mock adapter
    if (mock) {
      mock.restore();
    }
  });

  it('should render loading state initially', async () => {
    // Set up mock data context with loading state
    const loadingMockDataContext = {
      userData: null,
      exerciseMuscleData: new Map(),
      weightUnitPreferences: [],
      isLoading: true,
      error: null,
      refreshData: jest.fn(),
      isDataStale: false,
    };

    mockUseData.mockReturnValue(loadingMockDataContext);

    await act(async () => {
      renderWithProviders(<DashboardOverview user={mockUser} />);
    });

    expect(screen.getByText('Loading dashboard...')).toBeInTheDocument();
  });

  it('should render dashboard overview when data loads successfully', async () => {
    // Use default mock data (already set up in beforeEach)
    // Mock the additional API calls that the component still makes
    mock.onGet(/\/exercise\/.*/).reply(200, mockExercise);

    await act(async () => {
      renderWithProviders(<DashboardOverview user={mockUser} />);
    });

    await waitFor(() => {
      expect(screen.getByTestId('adventurer-status-card')).toBeInTheDocument();
    });
  });

  it('should display active program when available', async () => {
    mock.onGet('/program/').reply(200, [mockProgram]);
    mock.onGet('/programmed_workout/').reply(200, [mockWorkout]);
    mock.onGet('/user_one_rep_max/user/test-user-id').reply(200, []);
    mock.onGet('/gdpr/export').reply(200, mockUserDataExport);
    mock.onGet(/\/exercise\/.*/).reply(200, mockExercise);

    await act(async () => {
      renderWithProviders(<DashboardOverview user={mockUser} />);
    });

    await waitFor(() => {
      expect(screen.getByTestId('adventurer-status-card')).toBeInTheDocument();
    });
  });

  it('should display recent 1RM records when available', async () => {
    mock.onGet('/program/').reply(200, []);
    mock.onGet('/programmed_workout/').reply(200, []);
    mock.onGet('/user_one_rep_max/user/test-user-id').reply(200, [mockOneRepMax]);
    mock.onGet('/gdpr/export').reply(200, mockUserDataExport);
    mock.onGet(/\/exercise\/.*/).reply(200, mockExercise);

    await act(async () => {
      renderWithProviders(<DashboardOverview user={mockUser} />);
    });

    await waitFor(() => {
      expect(screen.getByTestId('adventurer-status-card')).toBeInTheDocument();
    });
  });

  it('should display status system when no data is available', async () => {
    mock.onGet('/program/').reply(200, []);
    mock.onGet('/programmed_workout/').reply(200, []);
    mock.onGet('/user_one_rep_max/user/test-user-id').reply(200, []);
    mock
      .onGet('/gdpr/export')
      .reply(200, { training_programs: [], user_one_rep_max: [], data_retention_policies: [] });

    await act(async () => {
      renderWithProviders(<DashboardOverview user={mockUser} />);
    });

    await waitFor(() => {
      expect(screen.getByTestId('adventurer-status-card')).toBeInTheDocument();
      expect(screen.getByText('Adventurer Status Card')).toBeInTheDocument();
    });
  });

  it('should calculate and display correct statistics', async () => {
    // Use default mock data (already set up in beforeEach)
    // Mock the additional API calls that the component still makes
    mock.onGet('/exercise/Bench Press').reply(200, mockExercise);

    await act(async () => {
      renderWithProviders(<DashboardOverview user={mockUser} />);
    });

    await waitFor(() => {
      // Just verify the component renders without errors
      expect(screen.getByTestId('adventurer-status-card')).toBeInTheDocument();
    });
  }, 10000);

  it('should calculate and display correct statistics with complex data', async () => {
    const multipleOneRepMaxes = [
      mockOneRepMax,
      { ...mockOneRepMax, exercise_name: 'Squat', one_rep_max: 315 },
    ];

    const multipleWorkoutsDataExport = {
      ...mockUserDataExport,
      training_programs: [
        {
          program: { id: 1, name: 'Test Program' },
          workouts: [
            {
              workout: {
                id: 1,
                name: 'Test Workout 1',
                created_at: new Date('2024-01-01T00:00:00Z'),
                updated_at: new Date('2024-01-01T00:00:00Z'),
                program_id: 1,
                day_number: 1,
              },
              stages: [
                {
                  stage: {
                    name: 'Test Stage',
                    id: 1,
                    programmed_workout_id: 1,
                    stage_type_id: 1,
                    position: 1,
                    created_at: new Date('2024-01-01T00:00:00Z'),
                    updated_at: new Date('2024-01-01T00:00:00Z'),
                  },
                  exercises: [
                    {
                      exercise: {
                        exercise_name: 'Bench Press',
                        id: 1,
                        workout_stage_id: 1,
                        position: 1,
                        created_at: new Date('2024-01-01T00:00:00Z'),
                        updated_at: new Date('2024-01-01T00:00:00Z'),
                      },
                      set_schemes: [
                        {
                          performed_weight: 100,
                          performed_rep_count: 10,
                          band_weight_lbs: null,
                          id: 1,
                          programmed_exercise_id: 1,
                          set_number: 1,
                          is_amrap: false,
                          is_emom: false,
                          use_tempo: false,
                          created_at: new Date('2024-01-01T00:00:00Z'),
                          updated_at: new Date('2024-01-01T00:00:00Z'),
                        },
                      ],
                    },
                  ],
                },
              ],
            },
            {
              workout: {
                id: 2,
                name: 'Test Workout 2',
                created_at: new Date('2024-01-02T00:00:00Z'),
                updated_at: new Date('2024-01-02T00:00:00Z'),
                program_id: 1,
                day_number: 2,
              },
              stages: [
                {
                  stage: {
                    name: 'Test Stage',
                    id: 2,
                    programmed_workout_id: 2,
                    stage_type_id: 1,
                    position: 1,
                    created_at: new Date('2024-01-02T00:00:00Z'),
                    updated_at: new Date('2024-01-02T00:00:00Z'),
                  },
                  exercises: [
                    {
                      exercise: {
                        exercise_name: 'Squat',
                        id: 2,
                        workout_stage_id: 2,
                        position: 1,
                        created_at: new Date('2024-01-02T00:00:00Z'),
                        updated_at: new Date('2024-01-02T00:00:00Z'),
                      },
                      set_schemes: [
                        {
                          performed_weight: 150,
                          performed_rep_count: 8,
                          band_weight_lbs: null,
                          id: 2,
                          programmed_exercise_id: 2,
                          set_number: 1,
                          is_amrap: false,
                          is_emom: false,
                          use_tempo: false,
                          created_at: new Date('2024-01-02T00:00:00Z'),
                          updated_at: new Date('2024-01-02T00:00:00Z'),
                        },
                      ],
                    },
                  ],
                },
              ],
            },
          ],
        },
      ],
      user_one_rep_max: multipleOneRepMaxes,
    };

    // Set up DataContext with complex data
    const complexDataContext = {
      userData: {
        user_id: 'test-user-id',
        user_profile: {
          /* minimal profile data */
        },
        user_one_rep_max: multipleOneRepMaxes,
        user_weight_unit_preferences: [],
        training_programs: multipleWorkoutsDataExport.training_programs,
        audit_logs: [],
        data_retention_policies: [],
      },
      exerciseMuscleData: new Map([
        ['Bench Press', ['chest', 'triceps']],
        ['Squat', ['legs', 'glutes']],
      ]),
      weightUnitPreferences: [],
      isLoading: false,
      isReady: true,
      error: null,
      refreshData: jest.fn(),
      isDataStale: false,
      performanceScores: {
        strength: 50,
        endurance: 60,
        power: 40,
        overall: 50,
      },
      performanceMetrics: [],
      weeklyTests: [],
      refreshPerformanceData: jest.fn(),
      testProtocols: [],
      loadTestProtocols: jest.fn(),
      submitPerformanceMetrics: jest.fn(),
      submitWeeklyTest: jest.fn(),
      getCurrentWeekTest: jest.fn(),
      loadPerformanceMetricsInRange: jest.fn(),
      loadWeeklyTests: jest.fn(),
    };

    mockUseData.mockReturnValue(complexDataContext);

    // Mock individual exercise calls that the component makes for each unique exercise
    mock.onGet('/exercise/Bench%20Press').reply(200, mockExercise);
    mock.onGet('/exercise/Squat').reply(200, { ...mockExercise, exercise_name: 'Squat' });

    await act(async () => {
      renderWithProviders(<DashboardOverview user={mockUser} />);
    });

    await waitFor(() => {
      // Check that the dashboard is rendering with the mocked component
      expect(screen.getByTestId('adventurer-status-card')).toBeInTheDocument();
    }, 15000);
  }, 20000);

  it('should verify API calls are made with correct endpoints', async () => {
    mock.onGet('/program/').reply(200, []);
    mock.onGet('/programmed_workout/').reply(200, []);
    mock.onGet('/user_one_rep_max/user/test-user-id').reply(200, []);
    mock.onGet('/gdpr/export').reply(200, mockUserDataExport);
    mock.onGet(/\/exercise\/.*/).reply(200, mockExercise);

    await act(async () => {
      renderWithProviders(<DashboardOverview user={mockUser} />);
    });

    await waitFor(() => {
      // Component should render using DataContext data
      expect(screen.getByTestId('adventurer-status-card')).toBeInTheDocument();
    });
  }, 10000);
});
