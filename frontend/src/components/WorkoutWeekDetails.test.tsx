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

  const renderWithProviders = (component: React.ReactElement) => {
    return render(
      <SnackbarProvider>
        <MemoryRouter>
          <ThemeProvider theme={theme}>{component}</ThemeProvider>
        </MemoryRouter>
      </SnackbarProvider>
    );
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
      renderWithProviders(<WorkoutWeekDetails weekNumber={1} />);
    });

    // Should render the component without errors
    await waitFor(() => {
      expect(screen.getByText('Workouts')).toBeInTheDocument();
    });
  });

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
      renderWithProviders(<WorkoutWeekDetails weekNumber={1} />);
    });

    await waitFor(
      () => {
        expect(screen.getByText(/No Active Program/)).toBeInTheDocument();
      },
      { timeout: 10000 }
    );
  });

  it('displays week information when active program exists', async () => {
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
      renderWithProviders(<WorkoutWeekDetails weekNumber={1} />);
    });

    await waitFor(
      () => {
        expect(screen.getByText('Test Program')).toBeInTheDocument();
        expect(screen.getByText('Week 1')).toBeInTheDocument();
      },
      { timeout: 10000 }
    );
  }, 15000);

  it('displays week workouts when workouts exist for the week', async () => {
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
      renderWithProviders(<WorkoutWeekDetails weekNumber={1} />);
    });

    await waitFor(
      () => {
        expect(screen.getByText('Test Program')).toBeInTheDocument();
        expect(screen.getByText('Week 1')).toBeInTheDocument();
        expect(screen.getByText('Day 1')).toBeInTheDocument();
        expect(screen.getByText('Push Day')).toBeInTheDocument();
      },
      { timeout: 10000 }
    );
  }, 15000);

  it('shows no workouts message when no workouts exist for the week', async () => {
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
      renderWithProviders(<WorkoutWeekDetails weekNumber={1} />);
    });

    await waitFor(
      () => {
        expect(screen.getByText(/No workouts found for Week 1/)).toBeInTheDocument();
      },
      { timeout: 10000 }
    );
  });

  it('shows error message when API calls fail', async () => {
    mock.onGet('/program/with-preferences').reply(500, { error: 'Internal server error' });
    mock.onGet('/programmed_workout/').reply(200, []);

    await act(async () => {
      renderWithProviders(<WorkoutWeekDetails weekNumber={1} />);
    });

    await waitFor(
      () => {
        expect(
          screen.getByText('Failed to load workout data. Please try again.')
        ).toBeInTheDocument();
      },
      { timeout: 10000 }
    );
  });

  it('displays multiple workouts for the week', async () => {
    const workout1 = { ...mockWorkout, id: 1, day_number: 1, name: 'Push Day' };
    const workout2 = { ...mockWorkout, id: 2, day_number: 2, name: 'Pull Day' };
    const workout3 = { ...mockWorkout, id: 3, day_number: 3, name: 'Leg Day' };
    // Note: With current_week_number: 2, workouts with day_number 1-2 go to week 1, day_number 3+ go to week 2

    mock.onGet('/program/with-preferences').reply(200, [mockProgramWithPreferences]);
    mock.onGet('/programmed_workout/').reply(200, [workout1, workout2, workout3]);
    // Mock WorkoutDetail dependencies
    mock.onGet('/gdpr/export').reply(200, {
      training_programs: [
        {
          program: mockProgramWithPreferences.program,
          program_preferences: mockProgramWithPreferences.program_preferences,
          workouts: [
            { workout: workout1, stages: [] },
            { workout: workout2, stages: [] },
            { workout: workout3, stages: [] },
          ],
        },
      ],
      data_retention_policies: [],
    });

    await act(async () => {
      renderWithProviders(<WorkoutWeekDetails weekNumber={1} />);
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
  });

  it('verifies API calls are made with correct endpoints', async () => {
    mock.onGet('/program/with-preferences').reply(200, []);
    mock.onGet('/programmed_workout/').reply(200, []);
    // Mock WorkoutDetail dependencies
    mock.onGet('/gdpr/export').reply(200, { training_programs: [], data_retention_policies: [] });

    await act(async () => {
      renderWithProviders(<WorkoutWeekDetails weekNumber={1} />);
    });

    await waitFor(
      () => {
        expect(screen.getByText('Workouts')).toBeInTheDocument();
      },
      { timeout: 10000 }
    );

    // Verify API calls were made
    expect(mock.history.get).toHaveLength(5); // program/with-preferences, exercise, gdpr/export, exercise_muscle, user_weight_unit_preference
    expect(mock.history.get[0].url).toBe('/program/with-preferences');
    expect(mock.history.get[1].url).toBe('/exercise/');
  });

  it('shows breadcrumb navigation with week number', async () => {
    mock.onGet('/program/with-preferences').reply(200, [mockProgramWithPreferences]);
    mock.onGet('/programmed_workout/').reply(200, [mockWorkout]);
    // Mock WorkoutDetail dependencies
    mock.onGet('/gdpr/export').reply(200, { training_programs: [], data_retention_policies: [] });

    await act(async () => {
      renderWithProviders(<WorkoutWeekDetails weekNumber={1} />);
    });

    await waitFor(
      () => {
        expect(screen.getByText('Workouts')).toBeInTheDocument();
        expect(screen.getByText('Week 1')).toBeInTheDocument();
      },
      { timeout: 10000 }
    );
  });
});
