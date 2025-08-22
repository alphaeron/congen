import React from 'react';
import { render, screen, fireEvent, waitFor, act } from '@testing-library/react';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import MockAdapter from 'axios-mock-adapter';
import { Workouts } from './Workouts';
import { ENDPOINT } from '../api/endpoint';
import type { User, Program, ProgrammedWorkout } from '../api/types';

const mock = new MockAdapter(ENDPOINT);
const theme = createTheme();

const renderWithTheme = (component: React.ReactElement) => {
  return render(
    <ThemeProvider theme={theme}>
      {component}
    </ThemeProvider>
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

  it('renders loading state initially', () => {
    mock.onGet('/program/').reply(200, []);
    mock.onGet('/programmed_workout/').reply(200, []);

    renderWithTheme(<Workouts user={mockUser} />);

    expect(screen.getByRole('progressbar')).toBeInTheDocument();
  });

  it('renders workouts page title', async () => {
    mock.onGet('/program/').reply(200, []);
    mock.onGet('/programmed_workout/').reply(200, []);

    await act(async () => {
      renderWithTheme(<Workouts user={mockUser} />);
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
      renderWithTheme(<Workouts user={mockUser} />);
    });

    await waitFor(() => {
      expect(screen.getByText(/No Active Program/)).toBeInTheDocument();
    });
  });

  it('displays active program information', async () => {
    mock.onGet('/program/').reply(200, [mockProgram]);
    mock.onGet('/programmed_workout/').reply(200, [mockWorkout]);

    await act(async () => {
      renderWithTheme(<Workouts user={mockUser} />);
    });

    await waitFor(() => {
      expect(screen.getByText('Test Program')).toBeInTheDocument();
      expect(screen.getByText('Week 2')).toBeInTheDocument();
    });
  });

  it('shows generate workouts button', async () => {
    mock.onGet('/program/').reply(200, [mockProgram]);
    mock.onGet('/programmed_workout/').reply(200, [mockWorkout]);

    await act(async () => {
      renderWithTheme(<Workouts user={mockUser} />);
    });

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /generate workouts/i })).toBeInTheDocument();
    });
  });

  it('opens generate dialog when generate button is clicked', async () => {
    mock.onGet('/program/').reply(200, [mockProgram]);
    mock.onGet('/programmed_workout/').reply(200, [mockWorkout]);

    await act(async () => {
      renderWithTheme(<Workouts user={mockUser} />);
    });

    await waitFor(() => {
      const generateButton = screen.getByRole('button', { name: /generate workouts/i });
      fireEvent.click(generateButton);
    });

    expect(screen.getByText('Generate Workouts')).toBeInTheDocument();
    expect(screen.getByText(/This will generate the next week of workouts/)).toBeInTheDocument();
  });

  it('generates workouts successfully', async () => {
    mock.onGet('/program/').reply(200, [mockProgram]);
    mock.onGet('/programmed_workout/').reply(200, [mockWorkout]);
    mock.onPost('/conjugate_workout_generator/1').reply(200, mockProgram);

    await act(async () => {
      renderWithTheme(<Workouts user={mockUser} />);
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
      renderWithTheme(<Workouts user={mockUser} />);
    });

    await waitFor(() => {
      expect(screen.getByText('Push Day')).toBeInTheDocument();
      expect(screen.getByText('Day 1')).toBeInTheDocument();
    });
  });

  it('shows start workout button when workout is available', async () => {
    mock.onGet('/program/').reply(200, [mockProgram]);
    mock.onGet('/programmed_workout/').reply(200, [mockWorkout]);

    await act(async () => {
      renderWithTheme(<Workouts user={mockUser} />);
    });

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /start workout/i })).toBeInTheDocument();
    });
  });

  it('starts workout when start button is clicked', async () => {
    mock.onGet('/program/').reply(200, [mockProgram]);
    mock.onGet('/programmed_workout/').reply(200, [mockWorkout]);

    await act(async () => {
      renderWithTheme(<Workouts user={mockUser} />);
    });

    await waitFor(() => {
      const startButton = screen.getByRole('button', { name: /start workout/i });
      fireEvent.click(startButton);
    });

    expect(screen.getByRole('button', { name: /pause/i })).toBeInTheDocument();
  });

  it('shows error message when API calls fail', async () => {
    mock.onGet('/program/').reply(500, { error: 'Internal server error' });
    mock.onGet('/programmed_workout/').reply(200, []);

    await act(async () => {
      renderWithTheme(<Workouts user={mockUser} />);
    });

    await waitFor(() => {
      expect(screen.getByText('Failed to load workout data. Please try again.')).toBeInTheDocument();
    });
  });

  it('displays this week\'s workouts', async () => {
    const workout2 = { ...mockWorkout, id: 2, name: 'Pull Day', day_number: 2 };
    mock.onGet('/program/').reply(200, [mockProgram]);
    mock.onGet('/programmed_workout/').reply(200, [mockWorkout, workout2]);

    await act(async () => {
      renderWithTheme(<Workouts user={mockUser} />);
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
      renderWithTheme(<Workouts user={mockUser} />);
    });

    await waitFor(() => {
      expect(mock.history.get).toHaveLength(2);
      expect(mock.history.get[0].url).toBe('/program/');
      expect(mock.history.get[1].url).toBe('/programmed_workout/');
    });
  });

  it('displays quick stats correctly', async () => {
    mock.onGet('/program/').reply(200, [mockProgram]);
    mock.onGet('/programmed_workout/').reply(200, [mockWorkout]);

    await act(async () => {
      renderWithTheme(<Workouts user={mockUser} />);
    });

    await waitFor(() => {
      expect(screen.getByText('Quick Stats')).toBeInTheDocument();
      expect(screen.getByText('Workouts This Week')).toBeInTheDocument();
      expect(screen.getByText('Current Week')).toBeInTheDocument();
      expect(screen.getByText('Program')).toBeInTheDocument();
    });
  });
});
