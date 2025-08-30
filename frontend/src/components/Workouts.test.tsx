import { ThemeProvider, createTheme } from '@mui/material/styles';
import { render, screen, fireEvent, waitFor, act } from '@testing-library/react';
import MockAdapter from 'axios-mock-adapter';
import { SnackbarProvider } from 'notistack';
import React from 'react';
import { MemoryRouter } from 'react-router';

import { Workouts } from './Workouts';
import { ENDPOINT } from '../api/endpoint';
import type { User, Program, ProgrammedWorkout } from '../api/types';

describe('Workouts', () => {
  // Create a new mock adapter for each test to prevent interference
  let mock: MockAdapter;
  const theme = createTheme();
  
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
  });

  afterEach(() => {
    // Properly clean up the mock adapter
    if (mock) {
      mock.restore();
    }
    jest.clearAllMocks();
  });

  it('renders component without errors', async () => {
    mock.onGet('/program/').reply(200, []);
    mock.onGet('/programmed_workout/').reply(200, []);
    // Mock WorkoutAnalytics dependencies
    mock.onGet('/gdpr/export').reply(200, { training_programs: [], data_retention_policies: [] });
    mock.onGet('/user_weight_unit_preference/test-user-id').reply(200, []);
    mock.onGet(/\/exercise\/[^\/]+$/).reply(200, {
      id: 1,
      exercise_name: 'Test Exercise',
      category: 'strength',
      primary_muscle: 'chest',
      secondary_muscles: ['triceps', 'shoulders'],
      instructions: 'Test instructions',
      equipment: 'barbell',
      difficulty: 'intermediate'
    });

    await act(async () => {
      renderWithProviders(<Workouts user={mockUser} />);
    });

    // Should render the component without errors
    await waitFor(() => {
      expect(screen.getByText('Workouts')).toBeInTheDocument();
    });
  });

  it('renders workouts page title', async () => {
    mock.onGet('/program/').reply(200, []);
    mock.onGet('/programmed_workout/').reply(200, []);
    // Mock WorkoutAnalytics dependencies
    mock.onGet('/gdpr/export').reply(200, { training_programs: [], data_retention_policies: [] });
    mock.onGet('/user_weight_unit_preference/test-user-id').reply(200, []);
    mock.onGet(/\/exercise\/[^\/]+$/).reply(200, {
      id: 1,
      exercise_name: 'Test Exercise',
      category: 'strength',
      primary_muscle: 'chest',
      secondary_muscles: ['triceps', 'shoulders'],
      instructions: 'Test instructions',
      equipment: 'barbell',
      difficulty: 'intermediate'
    });

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
    // Mock WorkoutAnalytics dependencies
    mock.onGet('/gdpr/export').reply(200, { training_programs: [], data_retention_policies: [] });
    mock.onGet('/user_weight_unit_preference/test-user-id').reply(200, []);
    mock.onGet(/\/exercise\/[^\/]+$/).reply(200, {
      id: 1,
      exercise_name: 'Test Exercise',
      category: 'strength',
      primary_muscle: 'chest',
      secondary_muscles: ['triceps', 'shoulders'],
      instructions: 'Test instructions',
      equipment: 'barbell',
      difficulty: 'intermediate'
    });

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
    // Mock WorkoutAnalytics dependencies
    mock.onGet('/gdpr/export').reply(200, { training_programs: [], data_retention_policies: [] });
    mock.onGet('/user_weight_unit_preference/test-user-id').reply(200, []);
    mock.onGet(/\/exercise\/[^\/]+$/).reply(200, {
      id: 1,
      exercise_name: 'Test Exercise',
      category: 'strength',
      primary_muscle: 'chest',
      secondary_muscles: ['triceps', 'shoulders'],
      instructions: 'Test instructions',
      equipment: 'barbell',
      difficulty: 'intermediate'
    });

    await act(async () => {
      renderWithProviders(<Workouts user={mockUser} />);
    });

    await waitFor(() => {
      expect(screen.getByText('Test Program')).toBeInTheDocument();
      expect(screen.getByText(/Week.*2/)).toBeInTheDocument();
    });
  });

  it('shows generate next week button', async () => {
    mock.onGet('/program/').reply(200, [mockProgram]);
    mock.onGet('/programmed_workout/').reply(200, [mockWorkout]);
    // Mock WorkoutAnalytics dependencies
    mock.onGet('/gdpr/export').reply(200, { training_programs: [], data_retention_policies: [] });
    mock.onGet('/user_weight_unit_preference/test-user-id').reply(200, []);
    mock.onGet(/\/exercise\/[^\/]+$/).reply(200, {
      id: 1,
      exercise_name: 'Test Exercise',
      category: 'strength',
      primary_muscle: 'chest',
      secondary_muscles: ['triceps', 'shoulders'],
      instructions: 'Test instructions',
      equipment: 'barbell',
      difficulty: 'intermediate'
    });

    await act(async () => {
      renderWithProviders(<Workouts user={mockUser} />);
    });

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /generate next week/i })).toBeInTheDocument();
    });
  });

  it('opens generate dialog when generate button is clicked', async () => {
    mock.onGet('/program/').reply(200, [mockProgram]);
    mock.onGet('/programmed_workout/').reply(200, [mockWorkout]);
    // Mock WorkoutAnalytics dependencies
    mock.onGet('/gdpr/export').reply(200, { training_programs: [], data_retention_policies: [] });
    mock.onGet('/user_weight_unit_preference/test-user-id').reply(200, []);
    mock.onGet(/\/exercise\/[^\/]+$/).reply(200, {
      id: 1,
      exercise_name: 'Test Exercise',
      category: 'strength',
      primary_muscle: 'chest',
      secondary_muscles: ['triceps', 'shoulders'],
      instructions: 'Test instructions',
      equipment: 'barbell',
      difficulty: 'intermediate'
    });

    await act(async () => {
      renderWithProviders(<Workouts user={mockUser} />);
    });

    await waitFor(() => {
      const generateButton = screen.getByRole('button', { name: /generate next week/i });
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
    // Mock WorkoutAnalytics dependencies
    mock.onGet('/gdpr/export').reply(200, { training_programs: [], data_retention_policies: [] });
    mock.onGet('/user_weight_unit_preference/test-user-id').reply(200, []);
    mock.onGet(/\/exercise\/[^\/]+$/).reply(200, {
      id: 1,
      exercise_name: 'Test Exercise',
      category: 'strength',
      primary_muscle: 'chest',
      secondary_muscles: ['triceps', 'shoulders'],
      instructions: 'Test instructions',
      equipment: 'barbell',
      difficulty: 'intermediate'
    });

    await act(async () => {
      renderWithProviders(<Workouts user={mockUser} />);
    });

    await waitFor(() => {
      const generateButton = screen.getByRole('button', { name: /generate next week/i });
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

  it('displays training weeks when workouts exist', async () => {
    mock.onGet('/program/').reply(200, [mockProgram]);
    mock.onGet('/programmed_workout/').reply(200, [mockWorkout]);
    // Mock WorkoutAnalytics dependencies
    mock.onGet('/gdpr/export').reply(200, { training_programs: [], data_retention_policies: [] });
    mock.onGet('/user_weight_unit_preference/test-user-id').reply(200, []);
    mock.onGet(/\/exercise\/[^\/]+$/).reply(200, {
      id: 1,
      exercise_name: 'Test Exercise',
      category: 'strength',
      primary_muscle: 'chest',
      secondary_muscles: ['triceps', 'shoulders'],
      instructions: 'Test instructions',
      equipment: 'barbell',
      difficulty: 'intermediate'
    });

    await act(async () => {
      renderWithProviders(<Workouts user={mockUser} />);
    });

    await waitFor(() => {
      expect(screen.getByText('Training Weeks')).toBeInTheDocument();
      expect(screen.getByText('Week 1')).toBeInTheDocument();
    });
  });

  it('shows error message when API calls fail', async () => {
    mock.onGet('/program/').reply(500, { error: 'Internal server error' });
    mock.onGet('/programmed_workout/').reply(200, []);
    // Mock WorkoutAnalytics dependencies
    mock.onGet('/gdpr/export').reply(200, { training_programs: [], data_retention_policies: [] });
    mock.onGet('/user_weight_unit_preference/test-user-id').reply(200, []);
    // Mock individual exercise endpoint for WorkoutAnalytics component
    mock.onGet(/\/exercise\/[^\/]+$/).reply(200, {
      id: 1,
      exercise_name: 'Test Exercise',
      category: 'strength',
      primary_muscle: 'chest',
      secondary_muscles: ['triceps', 'shoulders'],
      instructions: 'Test instructions',
      equipment: 'barbell',
      difficulty: 'intermediate'
    });

    await act(async () => {
      renderWithProviders(<Workouts user={mockUser} />);
    });

    await waitFor(() => {
      expect(
        screen.getByText('Failed to load workout data. Please try again.')
      ).toBeInTheDocument();
    });
  });

  it("displays multiple weeks when workouts span multiple weeks", async () => {
    // Create workouts for week 1 and week 2
    const workout1 = { ...mockWorkout, id: 1, day_number: 1, name: 'Push Day' };
    const workout2 = { ...mockWorkout, id: 2, day_number: 2, name: 'Pull Day' };
    const workout3 = { ...mockWorkout, id: 3, day_number: 5, name: 'Leg Day' }; // Week 2
    const workout4 = { ...mockWorkout, id: 4, day_number: 6, name: 'Upper Day' }; // Week 2
    
    mock.onGet('/program/').reply(200, [mockProgram]);
    mock.onGet('/programmed_workout/').reply(200, [workout1, workout2, workout3, workout4]);
    // Mock WorkoutAnalytics dependencies
    mock.onGet('/gdpr/export').reply(200, { training_programs: [], data_retention_policies: [] });
    mock.onGet('/user_weight_unit_preference/test-user-id').reply(200, []);
    mock.onGet(/\/exercise\/[^\/]+$/).reply(200, {
      id: 1,
      exercise_name: 'Test Exercise',
      category: 'strength',
      primary_muscle: 'chest',
      secondary_muscles: ['triceps', 'shoulders'],
      instructions: 'Test instructions',
      equipment: 'barbell',
      difficulty: 'intermediate'
    });

    await act(async () => {
      renderWithProviders(<Workouts user={mockUser} />);
    });

    await waitFor(() => {
      expect(screen.getByText('Training Weeks')).toBeInTheDocument();
    });

    // Check that both weeks are displayed
    expect(screen.getByText('Week 1')).toBeInTheDocument();
    expect(screen.getByText('Week 2')).toBeInTheDocument();
  });

  it('verifies API calls are made with correct endpoints', async () => {
    mock.onGet('/program/').reply(200, []);
    mock.onGet('/programmed_workout/').reply(200, []);
    // Mock WorkoutAnalytics dependencies
    mock.onGet('/gdpr/export').reply(200, { training_programs: [], data_retention_policies: [] });
    mock.onGet('/user_weight_unit_preference/test-user-id').reply(200, []);
    mock.onGet(/\/exercise\/[^\/]+$/).reply(200, {
      id: 1,
      exercise_name: 'Test Exercise',
      category: 'strength',
      primary_muscle: 'chest',
      secondary_muscles: ['triceps', 'shoulders'],
      instructions: 'Test instructions',
      equipment: 'barbell',
      difficulty: 'intermediate'
    });

    await act(async () => {
      renderWithProviders(<Workouts user={mockUser} />);
    });

    await waitFor(() => {
      expect(screen.getByText('Workouts')).toBeInTheDocument();
    });

    // Verify API calls were made (WorkoutAnalytics also makes API calls)
    expect(mock.history.get.length).toBeGreaterThanOrEqual(2); // At least program and programmed_workout
    const urls = mock.history.get.map(req => req.url);
    expect(urls).toContain('/program/');
    expect(urls).toContain('/programmed_workout/');
  });

  it('navigates to week details when week is clicked', async () => {
    mock.onGet('/program/').reply(200, [mockProgram]);
    mock.onGet('/programmed_workout/').reply(200, [mockWorkout]);
    // Mock WorkoutAnalytics dependencies
    mock.onGet('/gdpr/export').reply(200, { training_programs: [], data_retention_policies: [] });
    mock.onGet('/user_weight_unit_preference/test-user-id').reply(200, []);
    mock.onGet(/\/exercise\/[^\/]+$/).reply(200, {
      id: 1,
      exercise_name: 'Test Exercise',
      category: 'strength',
      primary_muscle: 'chest',
      secondary_muscles: ['triceps', 'shoulders'],
      instructions: 'Test instructions',
      equipment: 'barbell',
      difficulty: 'intermediate'
    });

    await act(async () => {
      renderWithProviders(<Workouts user={mockUser} />);
    });

    await waitFor(() => {
      const weekButton = screen.getByText('Week 1');
      fireEvent.click(weekButton);
    });

    // Should show the WorkoutWeekDetails component
    await waitFor(() => {
      expect(screen.getByText('Test Program - Week 1')).toBeInTheDocument();
    });
  });
});
