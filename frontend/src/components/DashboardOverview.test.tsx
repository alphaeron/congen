import { render, screen, waitFor, act } from '@testing-library/react';
import MockAdapter from 'axios-mock-adapter';
import { SnackbarProvider } from 'notistack';
import React from 'react';
import { MemoryRouter } from 'react-router';

import { DashboardOverview } from './DashboardOverview';
import { ENDPOINT } from '../api/endpoint';
import type { User } from '../api/types';

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

// Mock UserStatusSystem component
jest.mock('./UserStatusSystem', () => ({
  UserStatusSystem: ({ user }: { user: User }) => (
    <div data-testid="user-status-system">
      <div>Overall Health & Fitness Status</div>
      <div>Status System for {user.name}</div>
    </div>
  ),
}));

describe('DashboardOverview', () => {
  // Create a new mock adapter for each test to prevent interference
  let mock: MockAdapter;

  const renderWithProviders = (component: React.ReactElement) => {
    return render(
      <SnackbarProvider>
        <MemoryRouter>{component}</MemoryRouter>
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
    id: 1,
    program_id: 1,
    day_number: 1,
    name: 'Test Workout',
    created_at: new Date('2024-01-01T00:00:00.000Z'),
    updated_at: new Date('2024-01-01T00:00:00.000Z'),
  };

  const mockOneRepMax = {
    user_id: 'test-user-id',
    exercise_name: 'Bench Press',
    one_rep_max: 225,
    unit: 'KG',
    created_at: new Date('2024-01-01T00:00:00Z'),
    updated_at: new Date('2024-01-01T00:00:00Z'),
  };

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
    mock
      .onGet('/gdpr/export')
      .reply(
        () => new Promise(resolve => setTimeout(() => resolve([200, mockUserDataExport]), 100))
      );

    await act(async () => {
      renderWithProviders(<DashboardOverview user={mockUser} />);
    });

    expect(screen.getByText('Loading dashboard...')).toBeInTheDocument();
  });

  it('should render dashboard overview when data loads successfully', async () => {
    mock.onGet('/program/').reply(200, [mockProgram]);
    mock.onGet('/programmed_workout/').reply(200, [mockWorkout]);
    mock.onGet('/user_one_rep_max/user/test-user-id').reply(200, [mockOneRepMax]);
    mock.onGet('/gdpr/export').reply(200, mockUserDataExport);
    mock.onGet(/\/exercise\/.*/).reply(200, mockExercise);

    await act(async () => {
      renderWithProviders(<DashboardOverview user={mockUser} />);
    });

    await waitFor(() => {
      expect(screen.getByText('Key Performance Indicators')).toBeInTheDocument();
    });

    expect(screen.getByText('Total Workouts')).toBeInTheDocument();
    expect(screen.getByText('1RM Records')).toBeInTheDocument();
    expect(screen.getByText('Total Volume (lbs)')).toBeInTheDocument();
    expect(screen.getByText('Latest Volume (lbs)')).toBeInTheDocument();
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
      expect(screen.getByText('Active Program')).toBeInTheDocument();
      expect(screen.getByText('Test Program')).toBeInTheDocument();
      expect(screen.getByText('Week 2')).toBeInTheDocument(); // current_week_number = 2
      expect(screen.getByText('Active')).toBeInTheDocument();
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
      expect(screen.getByText('Recent Achievements')).toBeInTheDocument();
      // Check for Bench Press in the Recent Achievements section specifically
      const recentAchievementsSection = screen
        .getByText('Recent Achievements')
        .closest('.MuiCard-root');
      expect(recentAchievementsSection).toHaveTextContent('Bench Press');
      expect(recentAchievementsSection).toHaveTextContent('225 KG');
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
      expect(screen.getByTestId('user-status-system')).toBeInTheDocument();
      expect(screen.getByText('Overall Health & Fitness Status')).toBeInTheDocument();
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

    mock.onGet('/program/').reply(200, multiplePrograms);
    mock.onGet('/programmed_workout/').reply(200, multipleWorkouts);
    mock.onGet('/user_one_rep_max/user/test-user-id').reply(200, multipleOneRepMaxes);
    mock.onGet('/gdpr/export').reply(200, multipleWorkoutsDataExport);
    mock.onGet(/\/exercise\/.*/).reply(200, mockExercise);

    // Mock individual exercise calls that the component makes for each unique exercise
    mock.onGet('/exercise/Bench%20Press').reply(200, mockExercise);
    mock.onGet('/exercise/Squat').reply(200, mockExercise);

    await act(async () => {
      renderWithProviders(<DashboardOverview user={mockUser} />);
    });

    await waitFor(() => {
      // Check Key Performance Indicators
      expect(screen.getByText('Key Performance Indicators')).toBeInTheDocument();

      // Check that the dashboard is rendering with the expected data
      expect(screen.getByText('Total Workouts')).toBeInTheDocument();
      expect(screen.getByText('Total Volume (lbs)')).toBeInTheDocument();
      expect(screen.getByText('1RM Records')).toBeInTheDocument();

      // Check that the values are displayed (using more specific assertions)
      const totalWorkoutsElement = screen.getByText('Total Workouts').closest('.MuiGrid-root');
      expect(totalWorkoutsElement).toHaveTextContent('2');

      const totalVolumeElement = screen.getByText('Total Volume (lbs)').closest('.MuiGrid-root');
      expect(totalVolumeElement).toHaveTextContent('2k');

      const oneRmRecordsElement = screen.getByText('1RM Records').closest('.MuiGrid-root');
      expect(oneRmRecordsElement).toHaveTextContent('2');
    });
  });

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
      // Check that we have at least the minimum expected API calls
      expect(mock.history.get.length).toBeGreaterThanOrEqual(5);

      // Verify the core API calls are made in the expected order
      expect(mock.history.get[0].url).toBe('/program/');
      expect(mock.history.get[1].url).toBe('/programmed_workout/');
      expect(mock.history.get[2].url).toBe('/user_one_rep_max/user/test-user-id');
      expect(mock.history.get[3].url).toBe('/gdpr/export');

      // Check that at least one exercise call is made
      const exerciseCalls = mock.history.get.filter(
        call => call.url && call.url.match(/\/exercise\/.*/)
      );
      expect(exerciseCalls.length).toBeGreaterThanOrEqual(1);
    });
  }, 10000);
});
