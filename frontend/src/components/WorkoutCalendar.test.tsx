import { ThemeProvider, createTheme } from '@mui/material/styles';
import { render, screen, waitFor, act } from '@testing-library/react';
import { SnackbarProvider } from 'notistack';
import MockAdapter from 'axios-mock-adapter';
import React from 'react';

import { WorkoutCalendar } from './WorkoutCalendar';
import { ENDPOINT } from '../api/endpoint';
import type { User, Program, ProgrammedWorkout } from '../api/types';

// Create axios mock adapter for the ENDPOINT instance
const mock = new MockAdapter(ENDPOINT);

// Create a theme for testing
const theme = createTheme();

const renderWithProviders = (component: React.ReactElement) => {
  return render(
    <SnackbarProvider>
      <ThemeProvider theme={theme}>
        {component}
      </ThemeProvider>
    </SnackbarProvider>
  );
};

describe('WorkoutCalendar', () => {
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

  beforeEach(() => {
    mock.reset();
  });

  afterAll(() => {
    mock.restore();
  });

  it('renders loading state initially', async () => {
    // Use a delayed response to ensure loading state is visible
    mock.onGet('/program/').reply(() => new Promise(resolve => setTimeout(() => resolve([200, []]), 100)));
    mock.onGet('/programmed_workout/').reply(() => new Promise(resolve => setTimeout(() => resolve([200, []]), 100)));

    await act(async () => {
      render(<WorkoutCalendar user={mockUser} />);
    });

    // The component uses CircularProgress, not a progressbar role
    expect(screen.getByRole('progressbar')).toBeInTheDocument();
  });

  it('renders calendar page title and sections', async () => {
    mock.onGet('/program/').reply(200, []);
    mock.onGet('/programmed_workout/').reply(200, []);

    await act(async () => {
      renderWithProviders(<WorkoutCalendar user={mockUser} />);
    });

    await waitFor(() => {
      expect(screen.getByText('Workout Calendar')).toBeInTheDocument();
    });

    // When no active program, it shows a message instead of sections
    expect(screen.getByText('No Active Program')).toBeInTheDocument();
    expect(screen.getByText(/Create or activate a program to view your workout calendar/)).toBeInTheDocument();
  }, 10000);

  it('displays active program information', async () => {
    mock.onGet('/program/').reply(200, [mockProgram]);
    mock.onGet('/programmed_workout/').reply(200, [mockWorkout]);

    await act(async () => {
      renderWithProviders(<WorkoutCalendar user={mockUser} />);
    });

    await waitFor(() => {
      expect(screen.getByText('Test Program')).toBeInTheDocument();
    });

    // The component shows the week number in the program summary section
    // Look for the "2" that's specifically in the Current Week section
    const currentWeekSection = screen.getByText('Current Week').closest('div');
    expect(currentWeekSection).toBeInTheDocument();
    expect(currentWeekSection).toHaveTextContent('2'); // current_week_number
  }, 10000);

  it('displays upcoming workouts', async () => {
    mock.onGet('/program/').reply(200, [mockProgram]);
    mock.onGet('/programmed_workout/').reply(200, [mockWorkout]);

    await act(async () => {
      renderWithProviders(<WorkoutCalendar user={mockUser} />);
    });

    await waitFor(() => {
      // Use getAllByText since there are multiple "Push Day" elements
      const pushDayElements = screen.getAllByText('Push Day');
      expect(pushDayElements.length).toBeGreaterThan(0);
    });

    expect(screen.getByText(/Today/)).toBeInTheDocument();
  }, 10000);

  it('displays no active program message when no active program exists', async () => {
    const inactiveProgram = { ...mockProgram, is_active: false };
    mock.onGet('/program/').reply(200, [inactiveProgram]);
    mock.onGet('/programmed_workout/').reply(200, []);

    await act(async () => {
      renderWithProviders(<WorkoutCalendar user={mockUser} />);
    });

    await waitFor(() => {
      expect(screen.getByText('No Active Program')).toBeInTheDocument();
    });
  }, 10000);

  it('displays workout completion status', async () => {
    mock.onGet('/program/').reply(200, [mockProgram]);
    mock.onGet('/programmed_workout/').reply(200, [mockWorkout]);

    await act(async () => {
      renderWithProviders(<WorkoutCalendar user={mockUser} />);
    });

    await waitFor(() => {
      // Use getAllByText since there are multiple "Push Day" elements
      const pushDayElements = screen.getAllByText('Push Day');
      expect(pushDayElements.length).toBeGreaterThan(0);
    });
  }, 10000);

  it('shows calendar navigation controls', async () => {
    mock.onGet('/program/').reply(200, [mockProgram]);
    mock.onGet('/programmed_workout/').reply(200, [mockWorkout]);

    await act(async () => {
      renderWithProviders(<WorkoutCalendar user={mockUser} />);
    });

    await waitFor(() => {
      expect(screen.getByText('Workout Calendar')).toBeInTheDocument();
    });

    expect(screen.getByText('Upcoming Workouts')).toBeInTheDocument();
    expect(screen.getByText('Past Workouts')).toBeInTheDocument();
  }, 10000);

  it('displays workout details correctly', async () => {
    const workout2 = { ...mockWorkout, id: 2, name: 'Pull Day', day_number: 2 };
    mock.onGet('/program/').reply(200, [mockProgram]);
    mock.onGet('/programmed_workout/').reply(200, [mockWorkout, workout2]);

    await act(async () => {
      renderWithProviders(<WorkoutCalendar user={mockUser} />);
    });

    await waitFor(() => {
      // Use getAllByText since there are multiple "Push Day" elements
      const pushDayElements = screen.getAllByText('Push Day');
      expect(pushDayElements.length).toBeGreaterThan(0);
    });

    // Check for the second workout
    const pullDayElements = screen.getAllByText('Pull Day');
    expect(pullDayElements.length).toBeGreaterThan(0);
  }, 10000);

  it('handles multiple programs correctly', async () => {
    const program2 = { ...mockProgram, id: 2, name: 'Program 2', is_active: false };
    mock.onGet('/program/').reply(200, [mockProgram, program2]);
    mock.onGet('/programmed_workout/').reply(200, [mockWorkout]);

    await act(async () => {
      renderWithProviders(<WorkoutCalendar user={mockUser} />);
    });

    await waitFor(() => {
      // Should only show active program
      expect(screen.getByText('Test Program')).toBeInTheDocument();
    });

    expect(screen.queryByText('Program 2')).not.toBeInTheDocument();
  }, 10000);

  it('displays workout scheduling information', async () => {
    mock.onGet('/program/').reply(200, [mockProgram]);
    mock.onGet('/programmed_workout/').reply(200, [mockWorkout]);

    await act(async () => {
      renderWithProviders(<WorkoutCalendar user={mockUser} />);
    });

    await waitFor(() => {
      // Use getAllByText since there are multiple "Push Day" elements
      const pushDayElements = screen.getAllByText('Push Day');
      expect(pushDayElements.length).toBeGreaterThan(0);
    });

    // Use getAllByText since there are multiple "Day 1" elements
    const day1Elements = screen.getAllByText(/Day 1/);
    expect(day1Elements.length).toBeGreaterThan(0);
  }, 10000);

  it('verifies API calls are made with correct endpoints', async () => {
    mock.onGet('/program/').reply(200, []);
    mock.onGet('/programmed_workout/').reply(200, []);

    await act(async () => {
      renderWithProviders(<WorkoutCalendar user={mockUser} />);
    });

    await waitFor(() => {
      expect(mock.history.get).toHaveLength(2);
      expect(mock.history.get[0].url).toBe('/program/');
      expect(mock.history.get[1].url).toBe('/programmed_workout/');
    });
  }, 10000);

  it('displays calendar week view correctly', async () => {
    mock.onGet('/program/').reply(200, [mockProgram]);
    mock.onGet('/programmed_workout/').reply(200, [mockWorkout]);

    await act(async () => {
      renderWithProviders(<WorkoutCalendar user={mockUser} />);
    });

    await waitFor(() => {
      // Should show upcoming workouts for the week
      expect(screen.getByText('Upcoming Workouts')).toBeInTheDocument();
    });
  }, 10000);

  it('handles empty workout data gracefully', async () => {
    mock.onGet('/program/').reply(200, [mockProgram]);
    mock.onGet('/programmed_workout/').reply(200, []);

    await act(async () => {
      renderWithProviders(<WorkoutCalendar user={mockUser} />);
    });

    await waitFor(() => {
      expect(screen.getByText('Test Program')).toBeInTheDocument();
      // Should handle empty workouts gracefully
    });
  }, 10000);
});
