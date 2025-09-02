import { render, screen, waitFor, act } from '@testing-library/react';
import MockAdapter from 'axios-mock-adapter';
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
      expect(screen.getByText('Dashboard Overview')).toBeInTheDocument();
    });

    expect(screen.getByText('Key Performance Indicators')).toBeInTheDocument();
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
      expect(screen.getByText('Week 1')).toBeInTheDocument(); // 1 workout = Week 1
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
    mock
      .onGet('/gdpr/export')
      .reply(200, { training_programs: [], user_one_rep_max: [], data_retention_policies: [] });

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

    await act(async () => {
      renderWithProviders(<DashboardOverview user={mockUser} />);
    });

    await waitFor(
      () => {
        // Check Key Performance Indicators
        expect(screen.getByText('Key Performance Indicators')).toBeInTheDocument();
        // Total workouts should be from the data export (2 workouts)
        expect(screen.getByText('2')).toBeInTheDocument();
        // 1RM records count
        expect(screen.getByText('2')).toBeInTheDocument();
      },
      { timeout: 10000 }
    );
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
