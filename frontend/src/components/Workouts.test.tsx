import { ThemeProvider, createTheme } from '@mui/material/styles';
import { render, screen, fireEvent, waitFor, act } from '@testing-library/react';
import MockAdapter from 'axios-mock-adapter';
import { SnackbarProvider } from 'notistack';
import React from 'react';
import { MemoryRouter } from 'react-router';

import { Workouts } from './Workouts';
import { ENDPOINT } from '../api/endpoint';
import type { User, Program, ProgrammedWorkout, ProgramPreferences } from '../api/types';

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

  const mockProgramPreferences: ProgramPreferences = {
    program_id: 1,
    program_days_per_week: 3,
    session_time_length_in_minutes: 60,
    created_at: '2024-01-01T00:00:00Z',
    updated_at: '2024-01-01T00:00:00Z',
  };

  const mockProgram: Program & { program_preferences?: ProgramPreferences } = {
    id: 1,
    user_id: 'test-user-id',
    name: 'Test Program',
    current_week_number: 2,
    created_at: '2024-01-01T00:00:00Z',
    updated_at: '2024-01-01T00:00:00Z',
    is_active: true,
    program_preferences: mockProgramPreferences,
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
    // Create a fresh mock adapter for each test
    mock = new MockAdapter(ENDPOINT);
    jest.clearAllMocks();
    
    // Mock program preferences API calls
    mock.onGet('/program_preferences/1').reply(200, {
      program_id: 1,
      program_days_per_week: 4,
      session_time_length_in_minutes: 60,
      created_at: new Date('2024-01-01T00:00:00.000Z'),
      updated_at: new Date('2024-01-01T00:00:00.000Z'),
    });
  });

  afterEach(() => {
    // Properly clean up the mock adapter
    if (mock) {
      mock.restore();
    }
    jest.clearAllMocks();
  });

  it('renders component without errors', async () => {
    mock.onGet('/program/with-preferences').reply(200, []);
    mock.onGet('/programmed_workout/').reply(200, []);
    // Mock WorkoutAnalytics dependencies
    mock.onGet('/gdpr/export').reply(200, { training_programs: [], data_retention_policies: [] });
    mock.onGet('/user_weight_unit_preference/test-user-id').reply(200, []);
    mock.onGet(/\/exercise\/[^/]+$/).reply(200, {
      id: 1,
      exercise_name: 'Test Exercise',
      category: 'strength',
      primary_muscle: 'chest',
      secondary_muscles: ['triceps', 'shoulders'],
      instructions: 'Test instructions',
      equipment: 'barbell',
      difficulty: 'intermediate',
    });

    await act(async () => {
      renderWithProviders(<Workouts user={mockUser} />);
    });

    await waitFor(
      () => {
        expect(screen.getByText('Workouts')).toBeInTheDocument();
      },
      { timeout: 10000 }
    );
  });

  it('displays no active program message when no active program exists', async () => {
    const inactiveProgram = { ...mockProgram, is_active: false };
    mock.onGet('/program/with-preferences').reply(200, [inactiveProgram]);
    mock.onGet('/programmed_workout/').reply(200, []);
    // Mock WorkoutAnalytics dependencies
    mock.onGet('/gdpr/export').reply(200, { training_programs: [], data_retention_policies: [] });
    mock.onGet('/user_weight_unit_preference/test-user-id').reply(200, []);
    mock.onGet(/\/exercise\/[^/]+$/).reply(200, {
      id: 1,
      exercise_name: 'Test Exercise',
      category: 'strength',
      primary_muscle: 'chest',
      secondary_muscles: ['triceps', 'shoulders'],
      instructions: 'Test instructions',
      equipment: 'barbell',
      difficulty: 'intermediate',
    });

    await act(async () => {
      renderWithProviders(<Workouts user={mockUser} />);
    });

    await waitFor(
      () => {
        expect(screen.getByText(/No Active Program/)).toBeInTheDocument();
      },
      { timeout: 10000 }
    );
  });

  it('displays active program information', async () => {
    mock.onGet('/program/with-preferences').reply(200, [mockProgram]);
    mock.onGet('/programmed_workout/').reply(200, [mockWorkout]);
    // Mock WorkoutAnalytics dependencies
    mock.onGet('/gdpr/export').reply(200, { training_programs: [], data_retention_policies: [] });
    mock.onGet('/user_weight_unit_preference/test-user-id').reply(200, []);
    mock.onGet(/\/exercise\/[^/]+$/).reply(200, {
      id: 1,
      exercise_name: 'Test Exercise',
      category: 'strength',
      primary_muscle: 'chest',
      secondary_muscles: ['triceps', 'shoulders'],
      instructions: 'Test instructions',
      equipment: 'barbell',
      difficulty: 'intermediate',
    });

    await act(async () => {
      renderWithProviders(<Workouts user={mockUser} />);
    });

    await waitFor(
      () => {
        expect(screen.getByText('Test Program')).toBeInTheDocument();
        expect(screen.getByText(/Week.*2/)).toBeInTheDocument();
      },
      { timeout: 10000 }
    );
  });

  it('shows generate next week button', async () => {
    mock.onGet('/program/with-preferences').reply(200, [mockProgram]);
    mock.onGet('/programmed_workout/').reply(200, [mockWorkout]);
    // Mock WorkoutAnalytics dependencies
    mock.onGet('/gdpr/export').reply(200, { training_programs: [], data_retention_policies: [] });
    mock.onGet('/user_weight_unit_preference/test-user-id').reply(200, []);
    mock.onGet(/\/exercise\/[^/]+$/).reply(200, {
      id: 1,
      exercise_name: 'Test Exercise',
      category: 'strength',
      primary_muscle: 'chest',
      secondary_muscles: ['triceps', 'shoulders'],
      instructions: 'Test instructions',
      equipment: 'barbell',
      difficulty: 'intermediate',
    });

    await act(async () => {
      renderWithProviders(<Workouts user={mockUser} />);
    });

    await waitFor(
      () => {
        expect(screen.getByRole('button', { name: /generate next week/i })).toBeInTheDocument();
      },
      { timeout: 10000 }
    );
  });

  it('opens generate dialog when generate button is clicked', async () => {
    mock.onGet('/program/with-preferences').reply(200, [mockProgram]);
    mock.onGet('/programmed_workout/').reply(200, [mockWorkout]);
    // Mock WorkoutAnalytics dependencies
    mock.onGet('/gdpr/export').reply(200, { training_programs: [], data_retention_policies: [] });
    mock.onGet('/user_weight_unit_preference/test-user-id').reply(200, []);
    mock.onGet(/\/exercise\/[^/]+$/).reply(200, {
      id: 1,
      exercise_name: 'Test Exercise',
      category: 'strength',
      primary_muscle: 'chest',
      secondary_muscles: ['triceps', 'shoulders'],
      instructions: 'Test instructions',
      equipment: 'barbell',
      difficulty: 'intermediate',
    });

    await act(async () => {
      renderWithProviders(<Workouts user={mockUser} />);
    });

    await waitFor(
      () => {
        const generateButton = screen.getByRole('button', { name: /generate next week/i });
        fireEvent.click(generateButton);
      },
      { timeout: 10000 }
    );

    // Use getAllByText to handle multiple elements and check for dialog title specifically
    const dialogTitles = screen.getAllByText('Generate Workouts');
    expect(dialogTitles.some(title => title.tagName === 'H2')).toBe(true);
    expect(screen.getByText(/Generate next week's workouts for/)).toBeInTheDocument();
  });

  it('generates workouts successfully', async () => {
    mock.onGet('/program/with-preferences').reply(200, [mockProgram]);
    mock.onGet('/programmed_workout/').reply(200, [mockWorkout]);
    mock.onPost('/conjugate_workout_generator/1').reply(200, mockProgram);
    // Mock WorkoutAnalytics dependencies
    mock.onGet('/gdpr/export').reply(200, { training_programs: [], data_retention_policies: [] });
    mock.onGet('/user_weight_unit_preference/test-user-id').reply(200, []);
    mock.onGet(/\/exercise\/[^/]+$/).reply(200, {
      id: 1,
      exercise_name: 'Test Exercise',
      category: 'strength',
      primary_muscle: 'chest',
      secondary_muscles: ['triceps', 'shoulders'],
      instructions: 'Test instructions',
      equipment: 'barbell',
      difficulty: 'intermediate',
    });

    await act(async () => {
      renderWithProviders(<Workouts user={mockUser} />);
    });

    await waitFor(
      () => {
        const generateButton = screen.getByRole('button', { name: /generate next week/i });
        fireEvent.click(generateButton);
      },
      { timeout: 10000 }
    );

    // Check that the dialog opened
    await waitFor(
      () => {
        expect(screen.getByText('Generate Workouts')).toBeInTheDocument();
      },
      { timeout: 10000 }
    );

    // Click the generate button in the dialog
    await waitFor(
      () => {
        const generateWorkoutsButton = screen.getByRole('button', { name: /generate/i });
        fireEvent.click(generateWorkoutsButton);
      },
      { timeout: 10000 }
    );

    // Check that the API call was made
    await waitFor(
      () => {
        expect(mock.history.post).toHaveLength(1);
        expect(mock.history.post[0].url).toBe('/conjugate_workout_generator/1');
      },
      { timeout: 10000 }
    );
  }, 15000);

  it('displays training weeks when workouts exist', async () => {
    mock.onGet('/program/with-preferences').reply(200, [mockProgram]);
    mock.onGet('/programmed_workout/').reply(200, [mockWorkout]);
    // Mock WorkoutAnalytics dependencies
    mock.onGet('/gdpr/export').reply(200, { training_programs: [], data_retention_policies: [] });
    mock.onGet('/user_weight_unit_preference/test-user-id').reply(200, []);
    mock.onGet(/\/exercise\/[^/]+$/).reply(200, {
      id: 1,
      exercise_name: 'Test Exercise',
      category: 'strength',
      primary_muscle: 'chest',
      secondary_muscles: ['triceps', 'shoulders'],
      instructions: 'Test instructions',
      equipment: 'barbell',
      difficulty: 'intermediate',
    });

    await act(async () => {
      renderWithProviders(<Workouts user={mockUser} />);
    });

    await waitFor(
      () => {
        expect(screen.getByText('Week 1')).toBeInTheDocument();
        expect(screen.getByText(/Push Day/)).toBeInTheDocument();
      },
      { timeout: 10000 }
    );
  }, 15000);

  it('displays multiple weeks when workouts span multiple weeks', async () => {
    // Create workouts for week 1 and week 2
    const workout1 = { ...mockWorkout, id: 1, day_number: 1, name: 'Push Day' };
    const workout2 = { ...mockWorkout, id: 2, day_number: 2, name: 'Pull Day' };
    const workout3 = { ...mockWorkout, id: 3, day_number: 3, name: 'Leg Day' };
    const workout4 = { ...mockWorkout, id: 4, day_number: 4, name: 'Upper Body' };
    const workout5 = { ...mockWorkout, id: 5, day_number: 5, name: 'Lower Body' };
    const workout6 = { ...mockWorkout, id: 6, day_number: 6, name: 'Full Body' };

    mock.onGet('/program/with-preferences').reply(200, [mockProgram]);
    mock
      .onGet('/programmed_workout/')
      .reply(200, [workout1, workout2, workout3, workout4, workout5, workout6]);
    // Mock WorkoutAnalytics dependencies
    mock.onGet('/gdpr/export').reply(200, { training_programs: [], data_retention_policies: [] });
    mock.onGet('/user_weight_unit_preference/test-user-id').reply(200, []);
    mock.onGet(/\/exercise\/[^/]+$/).reply(200, {
      id: 1,
      exercise_name: 'Test Exercise',
      category: 'strength',
      primary_muscle: 'chest',
      secondary_muscles: ['triceps', 'shoulders'],
      instructions: 'Test instructions',
      equipment: 'barbell',
      difficulty: 'intermediate',
    });

    await act(async () => {
      renderWithProviders(<Workouts user={mockUser} />);
    });

    await waitFor(
      () => {
        expect(screen.getByText('Week 1')).toBeInTheDocument();
        expect(screen.getByText('Week 2')).toBeInTheDocument();
        expect(screen.getByText(/Push Day/)).toBeInTheDocument();
        expect(screen.getByText(/Pull Day/)).toBeInTheDocument();
        expect(screen.getByText(/Leg Day/)).toBeInTheDocument();
      },
      { timeout: 10000 }
    );
  }, 15000);

  it('verifies API calls are made with correct endpoints', async () => {
    mock.onGet('/program/with-preferences').reply(200, []);
    mock.onGet('/programmed_workout/').reply(200, []);
    // Mock WorkoutAnalytics dependencies
    mock.onGet('/gdpr/export').reply(200, { training_programs: [], data_retention_policies: [] });
    mock.onGet('/user_weight_unit_preference/test-user-id').reply(200, []);
    mock.onGet(/\/exercise\/[^/]+$/).reply(200, {
      id: 1,
      exercise_name: 'Test Exercise',
      category: 'strength',
      primary_muscle: 'chest',
      secondary_muscles: ['triceps', 'shoulders'],
      instructions: 'Test instructions',
      equipment: 'barbell',
      difficulty: 'intermediate',
    });

    await act(async () => {
      renderWithProviders(<Workouts user={mockUser} />);
    });

    await waitFor(
      () => {
        expect(screen.getByText('Workouts')).toBeInTheDocument();
      },
      { timeout: 10000 }
    );

    expect(mock.history.get.length).toBeGreaterThanOrEqual(2); // At least program and programmed_workout
    const urls = mock.history.get.map(req => req.url);
    expect(urls).toContain('/program/with-preferences');
    expect(urls).toContain('/programmed_workout/');
  });

  it('navigates to week details when week is clicked', async () => {
    mock.onGet('/program/with-preferences').reply(200, [mockProgram]);
    mock.onGet('/programmed_workout/').reply(200, [mockWorkout]);
    // Mock WorkoutAnalytics dependencies
    mock.onGet('/gdpr/export').reply(200, { training_programs: [], data_retention_policies: [] });
    mock.onGet('/user_weight_unit_preference/test-user-id').reply(200, []);
    mock.onGet(/\/exercise\/[^/]+$/).reply(200, {
      id: 1,
      exercise_name: 'Test Exercise',
      category: 'strength',
      primary_muscle: 'chest',
      secondary_muscles: ['triceps', 'shoulders'],
      instructions: 'Test instructions',
      equipment: 'barbell',
      difficulty: 'intermediate',
    });

    await act(async () => {
      renderWithProviders(<Workouts user={mockUser} />);
    });

    await waitFor(
      () => {
        const weekButton = screen.getByText('Week 1');
        fireEvent.click(weekButton);
      },
      { timeout: 10000 }
    );

    // Should navigate to week details
    await waitFor(
      () => {
        expect(screen.getByText('Week 1 Workouts')).toBeInTheDocument();
      },
      { timeout: 10000 }
    );
  });
});
