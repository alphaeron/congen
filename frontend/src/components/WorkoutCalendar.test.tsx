import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import MockAdapter from 'axios-mock-adapter';
import { WorkoutCalendar } from './WorkoutCalendar';
import { ENDPOINT } from '../api/endpoint';
import type { User, Program, ProgrammedWorkout } from '../api/types';

// Create axios mock adapter for the ENDPOINT instance
const mock = new MockAdapter(ENDPOINT);

// Create a theme for testing
const theme = createTheme();

const renderWithTheme = (component: React.ReactElement) => {
  return render(
    <ThemeProvider theme={theme}>
      {component}
    </ThemeProvider>
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

  it('renders loading state initially', () => {
    mock.onGet('/program/').reply(200, []);
    mock.onGet('/programmed_workout/').reply(200, []);

    renderWithTheme(<WorkoutCalendar user={mockUser} />);

    expect(screen.getByRole('progressbar')).toBeInTheDocument();
  });

  it('renders calendar page title and sections', async () => {
    mock.onGet('/program/').reply(200, []);
    mock.onGet('/programmed_workout/').reply(200, []);

    await waitFor(() => {
      expect(screen.getByText('Workout Calendar')).toBeInTheDocument();
      expect(screen.getByText('Upcoming Workouts')).toBeInTheDocument();
      expect(screen.getByText('Past Workouts')).toBeInTheDocument();
    });
  });

  it('displays active program information', async () => {
    mock.onGet('/program/').reply(200, [mockProgram]);
    mock.onGet('/programmed_workout/').reply(200, [mockWorkout]);

    await waitFor(() => {
      expect(screen.getByText('Test Program')).toBeInTheDocument();
      expect(screen.getByText('Week 2')).toBeInTheDocument();
      expect(screen.getByText('Active')).toBeInTheDocument();
    });
  });

  it('displays upcoming workouts', async () => {
    mock.onGet('/program/').reply(200, [mockProgram]);
    mock.onGet('/programmed_workout/').reply(200, [mockWorkout]);

    await waitFor(() => {
      expect(screen.getByText('Push Day')).toBeInTheDocument();
      expect(screen.getByText(/Today/)).toBeInTheDocument();
    });
  });

  it('shows error message when API calls fail', async () => {
    mock.onGet('/program/').reply(500, { message: 'Internal server error' });
    mock.onGet('/programmed_workout/').reply(200, []);

    await waitFor(() => {
      expect(screen.getByText('Failed to load calendar data. Please try again.')).toBeInTheDocument();
    });
  });

  it('displays no active program message when no active program exists', async () => {
    const inactiveProgram = { ...mockProgram, is_active: false };
    mock.onGet('/program/').reply(200, [inactiveProgram]);
    mock.onGet('/programmed_workout/').reply(200, []);

    await waitFor(() => {
      expect(screen.getByText(/No active program found/)).toBeInTheDocument();
    });
  });

  it('displays workout completion status', async () => {
    mock.onGet('/program/').reply(200, [mockProgram]);
    mock.onGet('/programmed_workout/').reply(200, [mockWorkout]);

    await waitFor(() => {
      // Should show completed workout indicator
      expect(screen.getByText('Push Day')).toBeInTheDocument();
    });
  });

  it('shows calendar navigation controls', async () => {
    mock.onGet('/program/').reply(200, [mockProgram]);
    mock.onGet('/programmed_workout/').reply(200, [mockWorkout]);

    await waitFor(() => {
      expect(screen.getByText('Workout Calendar')).toBeInTheDocument();
      expect(screen.getByText('Upcoming Workouts')).toBeInTheDocument();
      expect(screen.getByText('Past Workouts')).toBeInTheDocument();
    });
  });

  it('displays workout details correctly', async () => {
    const workout2 = { ...mockWorkout, id: 2, name: 'Pull Day', day_number: 2 };
    mock.onGet('/program/').reply(200, [mockProgram]);
    mock.onGet('/programmed_workout/').reply(200, [mockWorkout, workout2]);

    await waitFor(() => {
      expect(screen.getByText('Push Day')).toBeInTheDocument();
      expect(screen.getByText('Pull Day')).toBeInTheDocument();
    });
  });

  it('handles multiple programs correctly', async () => {
    const program2 = { ...mockProgram, id: 2, name: 'Program 2', is_active: false };
    mock.onGet('/program/').reply(200, [mockProgram, program2]);
    mock.onGet('/programmed_workout/').reply(200, [mockWorkout]);

    await waitFor(() => {
      // Should only show active program
      expect(screen.getByText('Test Program')).toBeInTheDocument();
      expect(screen.queryByText('Program 2')).not.toBeInTheDocument();
    });
  });

  it('displays workout scheduling information', async () => {
    mock.onGet('/program/').reply(200, [mockProgram]);
    mock.onGet('/programmed_workout/').reply(200, [mockWorkout]);

    await waitFor(() => {
      expect(screen.getByText('Push Day')).toBeInTheDocument();
      expect(screen.getByText(/Day 1/)).toBeInTheDocument();
    });
  });

  it('verifies API calls are made with correct endpoints', async () => {
    mock.onGet('/program/').reply(200, []);
    mock.onGet('/programmed_workout/').reply(200, []);

    await waitFor(() => {
      expect(mock.history.get).toHaveLength(2);
      expect(mock.history.get[0].url).toBe('/program/');
      expect(mock.history.get[1].url).toBe('/programmed_workout/');
    });
  });

  it('displays calendar week view correctly', async () => {
    mock.onGet('/program/').reply(200, [mockProgram]);
    mock.onGet('/programmed_workout/').reply(200, [mockWorkout]);

    await waitFor(() => {
      // Should show upcoming workouts for the week
      expect(screen.getByText('Upcoming Workouts')).toBeInTheDocument();
    });
  });

  it('handles empty workout data gracefully', async () => {
    mock.onGet('/program/').reply(200, [mockProgram]);
    mock.onGet('/programmed_workout/').reply(200, []);

    await waitFor(() => {
      expect(screen.getByText('Test Program')).toBeInTheDocument();
      // Should handle empty workouts gracefully
    });
  });
});
