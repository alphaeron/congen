import { ThemeProvider, createTheme } from '@mui/material/styles';
import { render, screen, fireEvent, waitFor, act } from '@testing-library/react';
import { SnackbarProvider } from 'notistack';
import MockAdapter from 'axios-mock-adapter';
import React from 'react';
import { MemoryRouter } from 'react-router';

import { Workouts } from './Workouts';
import { ENDPOINT } from '../api/endpoint';
import type { User, Program, ProgrammedWorkout } from '../api/types';

const mock = new MockAdapter(ENDPOINT);
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

describe('Workouts', () => {
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

  it('renders component without errors', async () => {
    mock.onGet('/program/').reply(200, []);
    mock.onGet('/programmed_workout/').reply(200, []);

    renderWithProviders(<Workouts user={mockUser} />);

    // Should render the component without errors
    await waitFor(() => {
      expect(screen.getByText('Workouts')).toBeInTheDocument();
    });
  });

  it('renders workouts page title', async () => {
    mock.onGet('/program/').reply(200, []);
    mock.onGet('/programmed_workout/').reply(200, []);

    await act(async () => {
      renderWithProviders(<Workouts user={mockUser} />);
    });

    await waitFor(() => {
      expect(screen.getByText('Workouts')).toBeInTheDocument();
    });
  });

  it('displays no active program message when no active program exists', async () => {
    const inactiveProgram = { ...mockProgram, is_active: false };
    mock.onGet('/program/').reply(200, [inactiveProgram]);
    mock.onGet('/programmed_workout/').reply(200, []);

    await act(async () => {
      renderWithProviders(<Workouts user={mockUser} />);
    });

    await waitFor(() => {
      expect(screen.getByText(/No Active Program/)).toBeInTheDocument();
    });
  });

  it('displays active program information', async () => {
    mock.onGet('/program/').reply(200, [mockProgram]);
    mock.onGet('/programmed_workout/').reply(200, [mockWorkout]);

    await act(async () => {
      renderWithProviders(<Workouts user={mockUser} />);
    });

    await waitFor(() => {
      expect(screen.getByText('Test Program')).toBeInTheDocument();
      expect(screen.getByText(/Week.*2/)).toBeInTheDocument();
    });
  });

  it('shows generate workouts button', async () => {
    mock.onGet('/program/').reply(200, [mockProgram]);
    mock.onGet('/programmed_workout/').reply(200, [mockWorkout]);

    await act(async () => {
      renderWithProviders(<Workouts user={mockUser} />);
    });

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /generate workouts/i })).toBeInTheDocument();
    });
  });

  it('opens generate dialog when generate button is clicked', async () => {
    mock.onGet('/program/').reply(200, [mockProgram]);
    mock.onGet('/programmed_workout/').reply(200, [mockWorkout]);

    await act(async () => {
      renderWithProviders(<Workouts user={mockUser} />);
    });

    await waitFor(() => {
      const generateButton = screen.getByRole('button', { name: /generate workouts/i });
      fireEvent.click(generateButton);
    });

    // Use getAllByText to handle multiple elements and check for dialog title specifically
    const dialogTitles = screen.getAllByText('Generate Workouts');
    expect(dialogTitles.some(title => title.tagName === 'H2')).toBe(true);
    expect(screen.getByText(/Generate next week's workouts for/)).toBeInTheDocument();
  });

  it('generates workouts successfully', async () => {
    mock.onGet('/program/').reply(200, [mockProgram]);
    mock.onGet('/programmed_workout/').reply(200, [mockWorkout]);
    mock.onPost('/conjugate_workout_generator/1').reply(200, mockProgram);

    await act(async () => {
      renderWithProviders(<Workouts user={mockUser} />);
    });

    await waitFor(() => {
      const generateButton = screen.getByRole('button', { name: /generate workouts/i });
      fireEvent.click(generateButton);
    });

    await waitFor(() => {
      const confirmButton = screen.getByRole('button', { name: /generate/i });
      fireEvent.click(confirmButton);
    });

    await waitFor(() => {
      expect(mock.history.post).toHaveLength(1);
      expect(mock.history.post[0].url).toBe('/conjugate_workout_generator/1');
    });
  });

  it('displays current workout when available', async () => {
    mock.onGet('/program/').reply(200, [mockProgram]);
    mock.onGet('/programmed_workout/').reply(200, [mockWorkout]);

    await act(async () => {
      renderWithProviders(<Workouts user={mockUser} />);
    });

    await waitFor(() => {
      expect(screen.getByText('Push Day')).toBeInTheDocument();
      expect(screen.getByText('Day 1')).toBeInTheDocument();
    });
  });

  it('shows error message when API calls fail', async () => {
    mock.onGet('/program/').reply(500, { error: 'Internal server error' });
    mock.onGet('/programmed_workout/').reply(200, []);

    await act(async () => {
      renderWithProviders(<Workouts user={mockUser} />);
    });

    await waitFor(() => {
      expect(
        screen.getByText('Failed to load workout data. Please try again.')
      ).toBeInTheDocument();
    });
  });

  it("displays this week's workouts", async () => {
    const workout2 = { ...mockWorkout, id: 2, name: 'Pull Day', day_number: 2 };
    mock.onGet('/program/').reply(200, [mockProgram]);
    mock.onGet('/programmed_workout/').reply(200, [mockWorkout, workout2]);

    await act(async () => {
      renderWithProviders(<Workouts user={mockUser} />);
    });

    await waitFor(() => {
      expect(screen.getByText('Push Day')).toBeInTheDocument();
      expect(screen.getByText('Pull Day')).toBeInTheDocument();
    });
  });

  it('verifies API calls are made with correct endpoints', async () => {
    mock.onGet('/program/').reply(200, []);
    mock.onGet('/programmed_workout/').reply(200, []);

    await act(async () => {
      renderWithProviders(<Workouts user={mockUser} />);
    });

    await waitFor(() => {
      expect(mock.history.get).toHaveLength(2);
      expect(mock.history.get[0].url).toBe('/program/');
      expect(mock.history.get[1].url).toBe('/programmed_workout/');
    });
  });
});
