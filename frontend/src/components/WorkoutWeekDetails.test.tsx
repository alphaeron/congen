import { ThemeProvider, createTheme } from '@mui/material/styles';
import { render, screen, fireEvent, waitFor, act } from '@testing-library/react';
import MockAdapter from 'axios-mock-adapter';
import { SnackbarProvider } from 'notistack';
import React from 'react';
import { MemoryRouter } from 'react-router';

import { WorkoutWeekDetails } from './WorkoutWeekDetails';
import { ENDPOINT } from '../api/endpoint';
import type { User, Program, ProgrammedWorkout } from '../api/types';

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
    
  const mockUser: User = {
    keycloak_id: 'test-user-id',
    name: 'Test User',
    created_at: '2024-01-01T00:00:00Z',
    updated_at: '2024-01-01T00:00:00Z',
    roles: ['user'],
  };

  const mockProgram: Program = {
    id: 1,
    user_id: 'test-user-id',
    name: 'Test Program',
    current_week_number: 2,
    created_at: '2024-01-01T00:00:00Z',
    updated_at: '2024-01-01T00:00:00Z',
    is_active: true,
  };

  const mockWorkout: ProgrammedWorkout = {
    id: 1,
    program_id: 1,
    day_number: 1,
    name: 'Push Day',
    created_at: '2024-01-01T00:00:00Z',
    updated_at: '2024-01-01T00:00:00Z',
  };

  // Mock GDPR export data
  const mockGdprExport = {
    training_programs: [
      {
        program: mockProgram,
        workouts: [
          {
            workout: mockWorkout,
            stages: []
          }
        ]
      }
    ],
    data_retention_policies: []
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

  it('renders component without errors', async () => {
    mock.onGet('/program/').reply(200, []);
    mock.onGet('/programmed_workout/').reply(200, []);
    // Mock WorkoutDetail dependencies
    mock.onGet('/gdpr/export').reply(200, { training_programs: [], data_retention_policies: [] });

    await act(async () => {
      renderWithProviders(<WorkoutWeekDetails weekNumber={1} />);
    });

    // Should render the component without errors
    await waitFor(() => {
      expect(screen.getByText('Workouts')).toBeInTheDocument();
    });
  });

  it('displays no active program message when no active program exists', async () => {
    const inactiveProgram = { ...mockProgram, is_active: false };
    mock.onGet('/program/').reply(200, [inactiveProgram]);
    mock.onGet('/programmed_workout/').reply(200, []);
    // Mock WorkoutDetail dependencies
    mock.onGet('/gdpr/export').reply(200, { training_programs: [], data_retention_policies: [] });

    await act(async () => {
      renderWithProviders(<WorkoutWeekDetails weekNumber={1} />);
    });

    await waitFor(() => {
      expect(screen.getByText(/No Active Program/)).toBeInTheDocument();
    });
  });

  it('displays week information when active program exists', async () => {
    mock.onGet('/program/').reply(200, [mockProgram]);
    mock.onGet('/programmed_workout/').reply(200, [mockWorkout]);
    // Mock WorkoutDetail dependencies
    mock.onGet('/gdpr/export').reply(200, { training_programs: [], data_retention_policies: [] });

    await act(async () => {
      renderWithProviders(<WorkoutWeekDetails weekNumber={1} />);
    });

    await waitFor(() => {
      expect(screen.getByText('Test Program - Week 1')).toBeInTheDocument();
      expect(screen.getByText(/Week 1 of 2/)).toBeInTheDocument();
    });
  });

  it('displays week workouts when workouts exist for the week', async () => {
    mock.onGet('/program/').reply(200, [mockProgram]);
    mock.onGet('/programmed_workout/').reply(200, [mockWorkout]);
    // Mock WorkoutDetail dependencies
    mock.onGet('/gdpr/export').reply(200, { training_programs: [], data_retention_policies: [] });

    await act(async () => {
      renderWithProviders(<WorkoutWeekDetails user={mockUser} weekNumber={1} />);
    });

    await waitFor(() => {
      expect(screen.getByText('Week 1 Workouts')).toBeInTheDocument();
      expect(screen.getByText('Day 1')).toBeInTheDocument();
      expect(screen.getByText('Push Day')).toBeInTheDocument();
    });
  });

  it('shows no workouts message when no workouts exist for the week', async () => {
    mock.onGet('/program/').reply(200, [mockProgram]);
    mock.onGet('/programmed_workout/').reply(200, []);
    // Mock WorkoutDetail dependencies
    mock.onGet('/gdpr/export').reply(200, { training_programs: [], data_retention_policies: [] });

    await act(async () => {
      renderWithProviders(<WorkoutWeekDetails user={mockUser} weekNumber={1} />);
    });

    await waitFor(() => {
      expect(screen.getByText(/No workouts found for Week 1/)).toBeInTheDocument();
    });
  });

  it('shows error message when API calls fail', async () => {
    mock.onGet('/program/').reply(500, { error: 'Internal server error' });
    mock.onGet('/programmed_workout/').reply(200, []);

    await act(async () => {
      renderWithProviders(<WorkoutWeekDetails user={mockUser} weekNumber={1} />);
    });

    await waitFor(() => {
      expect(
        screen.getByText('Failed to load workout data. Please try again.')
      ).toBeInTheDocument();
    });
  });

  it('displays multiple workouts for the week', async () => {
    const workout1 = { ...mockWorkout, id: 1, day_number: 1, name: 'Push Day' };
    const workout2 = { ...mockWorkout, id: 2, day_number: 2, name: 'Pull Day' };
    const workout3 = { ...mockWorkout, id: 3, day_number: 3, name: 'Leg Day' };
    // Note: With current_week_number: 2, workouts with day_number 1-2 go to week 1, day_number 3+ go to week 2
    
    mock.onGet('/program/').reply(200, [mockProgram]);
    mock.onGet('/programmed_workout/').reply(200, [workout1, workout2, workout3]);
    // Mock WorkoutDetail dependencies
    mock.onGet('/gdpr/export').reply(200, { training_programs: [], data_retention_policies: [] });

    await act(async () => {
      renderWithProviders(<WorkoutWeekDetails user={mockUser} weekNumber={1} />);
    });

    await waitFor(() => {
      expect(screen.getByText('Day 1')).toBeInTheDocument();
      expect(screen.getByText('Day 2')).toBeInTheDocument();
      expect(screen.getByText('Push Day')).toBeInTheDocument();
      expect(screen.getByText('Pull Day')).toBeInTheDocument();
      // workout3 (day_number: 3) goes to week 2, not week 1
    });
  });

  it('verifies API calls are made with correct endpoints', async () => {
    mock.onGet('/program/').reply(200, []);
    mock.onGet('/programmed_workout/').reply(200, []);
    // Mock WorkoutDetail dependencies
    mock.onGet('/gdpr/export').reply(200, { training_programs: [], data_retention_policies: [] });

    await act(async () => {
      renderWithProviders(<WorkoutWeekDetails user={mockUser} weekNumber={1} />);
    });

    await waitFor(() => {
      expect(screen.getByText('Workouts')).toBeInTheDocument();
    });

    // Verify API calls were made
    expect(mock.history.get).toHaveLength(2); // program, programmed_workout
    expect(mock.history.get[0].url).toBe('/program/');
    expect(mock.history.get[1].url).toBe('/programmed_workout/');
  });

  it('shows breadcrumb navigation with week number', async () => {
    mock.onGet('/program/').reply(200, [mockProgram]);
    mock.onGet('/programmed_workout/').reply(200, [mockWorkout]);

    await act(async () => {
      renderWithProviders(<WorkoutWeekDetails user={mockUser} weekNumber={1} />);
    });

    await waitFor(() => {
      expect(screen.getByText('Workouts')).toBeInTheDocument();
      expect(screen.getByText('Week 1')).toBeInTheDocument();
    });
  });
});
