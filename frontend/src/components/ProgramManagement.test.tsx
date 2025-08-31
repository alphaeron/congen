import { ThemeProvider, createTheme } from '@mui/material/styles';
import { render, screen, fireEvent, waitFor, act } from '@testing-library/react';
import MockAdapter from 'axios-mock-adapter';
import { SnackbarProvider } from 'notistack';
import React from 'react';

import { ProgramManagement } from './ProgramManagement';
import { ENDPOINT } from '../api/endpoint';
import type { User, Program, ProgrammedWorkout } from '../api/types';

describe('ProgramManagement', () => {
  // Create a theme for testing
  const theme = createTheme();

  const renderWithProviders = (component: React.ReactElement) => {
    return render(
      <SnackbarProvider>
        <ThemeProvider theme={theme}>{component}</ThemeProvider>
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

  // Create a new mock adapter for each test to prevent interference
  let mock: MockAdapter;

  beforeEach(() => {
    // Create a fresh mock adapter for each test
    mock = new MockAdapter(ENDPOINT);
  });

  afterEach(() => {
    // Properly clean up the mock adapter
    if (mock) {
      mock.restore();
    }
  });

  it('renders loading state initially', async () => {
    // Use a delayed response to ensure loading state is visible
    mock
      .onGet('/program/')
      .reply(() => new Promise(resolve => setTimeout(() => resolve([200, []]), 100)));
    mock
      .onGet('/programmed_workout/')
      .reply(() => new Promise(resolve => setTimeout(() => resolve([200, []]), 100)));

    await act(async () => {
      renderWithProviders(<ProgramManagement user={mockUser} />);
    });

    expect(screen.getByRole('progressbar')).toBeInTheDocument();

    // Wait for loading to complete to avoid act warnings
    await waitFor(() => {
      expect(screen.queryByRole('progressbar')).not.toBeInTheDocument();
    });
  });

  it('renders program management title and create button', async () => {
    mock.onGet('/program/').reply(200, []);
    mock.onGet('/programmed_workout/').reply(200, []);

    await act(async () => {
      renderWithProviders(<ProgramManagement user={mockUser} />);
    });

    await waitFor(() => {
      expect(screen.getByText('Program Management')).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /create program/i })).toBeInTheDocument();
    });
  });

  it('displays programs when data loads successfully', async () => {
    // Mock user-related API calls from AuthContext
    mock.onGet('/user/me').reply(200, mockUser);
    mock.onPost('/user/').reply(200, mockUser);

    // Mock program and workout API calls
    mock.onGet('/program/').reply(200, [mockProgram]);
    mock.onGet('/programmed_workout/').reply(200, [mockWorkout]);

    // Mock additional API calls that might be made by chart components or other dependencies
    mock.onGet('/gdpr/export').reply(200, {
      training_programs: [],
      programmed_workouts: [],
      workout_stages: [],
      programmed_exercises: [],
      set_schemes: [],
      user_one_rep_max: [],
      user_weight_unit_preferences: [],
    });

    // Fix: getUserWeightUnitPreferences needs user ID in URL
    mock.onGet('/user_weight_unit_preference/test-user-id').reply(200, []);

    mock.onGet('/exercise_muscle/').reply(200, []);

    mock.onGet('/exercise_equipment/').reply(200, []);

    // Fix: getIndividualExercise needs exercise name in URL
    mock.onGet(/\/exercise\/[^/]+$/).reply(200, {
      name: 'Test Exercise',
      description: 'Test Description',
      movement_type: 'push',
      is_unilateral: false,
      is_upper: true,
      is_accessory: false,
    });

    // Fix: getExerciseMuscles needs exercise name in URL
    mock.onGet(/\/exercise\/[^/]+\/muscle$/).reply(200, []);

    // Fix: getExerciseEquipment needs exercise name in URL
    mock.onGet(/\/exercise\/[^/]+\/equipment$/).reply(200, []);

    await act(async () => {
      renderWithProviders(<ProgramManagement user={mockUser} />);
    });

    await waitFor(() => {
      expect(screen.getByText('Test Program')).toBeInTheDocument();
      expect(screen.getByText('Active')).toBeInTheDocument();
      expect(screen.getByText('Week 2')).toBeInTheDocument();
      expect(screen.getByText('1 workouts')).toBeInTheDocument();
    });
  });

  it('displays no programs state when no programs exist', async () => {
    mock.onGet('/program/').reply(200, []);
    mock.onGet('/programmed_workout/').reply(200, []);

    await act(async () => {
      renderWithProviders(<ProgramManagement user={mockUser} />);
    });

    await waitFor(() => {
      expect(screen.getByText('No Programs Yet')).toBeInTheDocument();
      expect(screen.getByText(/Create your first program to get started/)).toBeInTheDocument();
    });
  });

  it('shows error message when API calls fail', async () => {
    mock.onGet('/program/').reply(500, { message: 'Internal server error' });
    mock.onGet('/programmed_workout/').reply(200, []);

    await act(async () => {
      renderWithProviders(<ProgramManagement user={mockUser} />);
    });

    await waitFor(
      () => {
        expect(screen.getByText('Failed to load programs. Please try again.')).toBeInTheDocument();
      },
      { timeout: 5000 }
    );
  });

  it('opens create program dialog when create button is clicked', async () => {
    mock.onGet('/program/').reply(200, []);
    mock.onGet('/programmed_workout/').reply(200, []);

    await act(async () => {
      renderWithProviders(<ProgramManagement user={mockUser} />);
    });

    await waitFor(() => {
      const createButton = screen.getByRole('button', { name: /create program/i });
      fireEvent.click(createButton);
    });

    expect(screen.getByText('Create New Program')).toBeInTheDocument();
    expect(screen.getByLabelText(/program name/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/set as active program/i)).toBeInTheDocument();
  });

  it('creates a new program successfully', async () => {
    const newProgram = { ...mockProgram, id: 2, name: 'New Program' };

    // Mock user-related API calls from AuthContext
    mock.onGet('/user/me').reply(200, mockUser);
    mock.onPost('/user/').reply(200, mockUser);

    // Mock program and workout API calls
    mock.onGet('/program/').reply(200, []);
    mock.onGet('/programmed_workout/').reply(200, []);
    mock.onPost('/program/').reply(200, newProgram);

    // Mock additional API calls that might be made by chart components or other dependencies
    mock.onGet('/gdpr/export').reply(200, {
      training_programs: [],
      programmed_workouts: [],
      workout_stages: [],
      programmed_exercises: [],
      set_schemes: [],
      user_one_rep_max: [],
      user_weight_unit_preferences: [],
    });

    // Fix: getUserWeightUnitPreferences needs user ID in URL
    mock.onGet('/user_weight_unit_preference/test-user-id').reply(200, []);

    mock.onGet('/exercise_muscle/').reply(200, []);

    mock.onGet('/exercise_equipment/').reply(200, []);

    // Fix: getIndividualExercise needs exercise name in URL
    mock.onGet(/\/exercise\/[^/]+$/).reply(200, {
      name: 'Test Exercise',
      description: 'Test Description',
      movement_type: 'push',
      is_unilateral: false,
      is_upper: true,
      is_accessory: false,
    });

    // Fix: getExerciseMuscles needs exercise name in URL
    mock.onGet(/\/exercise\/[^/]+\/muscle$/).reply(200, []);

    // Fix: getExerciseEquipment needs exercise name in URL
    mock.onGet(/\/exercise\/[^/]+\/equipment$/).reply(200, []);

    await act(async () => {
      renderWithProviders(<ProgramManagement user={mockUser} />);
    });

    await waitFor(() => {
      const createButton = screen.getByRole('button', { name: /create program/i });
      fireEvent.click(createButton);
    });

    const nameInput = screen.getByLabelText(/program name/i);
    fireEvent.change(nameInput, { target: { value: 'New Program' } });

    const createDialogButton = screen.getByRole('button', { name: /create program/i });
    fireEvent.click(createDialogButton);

    await waitFor(() => {
      expect(mock.history.post).toHaveLength(1);
      expect(mock.history.post[0].params).toEqual({
        user_id: 'test-user-id',
        name: 'New Program',
        is_active: true,
      });
    });
  });

  it('opens edit dialog when edit button is clicked', async () => {
    // Mock user-related API calls from AuthContext
    mock.onGet('/user/me').reply(200, mockUser);
    mock.onPost('/user/').reply(200, mockUser);

    // Mock program and workout API calls
    mock.onGet('/program/').reply(200, [mockProgram]);
    mock.onGet('/programmed_workout/').reply(200, []);

    // Mock additional API calls that might be made by chart components or other dependencies
    mock.onGet('/gdpr/export').reply(200, {
      training_programs: [],
      programmed_workouts: [],
      workout_stages: [],
      programmed_exercises: [],
      set_schemes: [],
      user_one_rep_max: [],
      user_weight_unit_preferences: [],
    });

    // Fix: getUserWeightUnitPreferences needs user ID in URL
    mock.onGet('/user_weight_unit_preference/test-user-id').reply(200, []);

    mock.onGet('/exercise_muscle/').reply(200, []);

    mock.onGet('/exercise_equipment/').reply(200, []);

    // Fix: getIndividualExercise needs exercise name in URL
    mock.onGet(/\/exercise\/[^/]+$/).reply(200, {
      name: 'Test Exercise',
      description: 'Test Description',
      movement_type: 'push',
      is_unilateral: false,
      is_upper: true,
      is_accessory: false,
    });

    // Fix: getExerciseMuscles needs exercise name in URL
    mock.onGet(/\/exercise\/[^/]+\/muscle$/).reply(200, []);

    // Fix: getExerciseEquipment needs exercise name in URL
    mock.onGet(/\/exercise\/[^/]+\/equipment$/).reply(200, []);

    await act(async () => {
      renderWithProviders(<ProgramManagement user={mockUser} />);
    });

    await waitFor(() => {
      const editButton = screen.getByLabelText(/edit program/i);
      fireEvent.click(editButton);
    });

    expect(screen.getByText('Edit Program')).toBeInTheDocument();
    expect(screen.getByDisplayValue('Test Program')).toBeInTheDocument();
  });

  it('updates a program successfully', async () => {
    const updatedProgram = { ...mockProgram, name: 'Updated Program' };

    // Mock user-related API calls from AuthContext
    mock.onGet('/user/me').reply(200, mockUser);
    mock.onPost('/user/').reply(200, mockUser);

    // Mock program and workout API calls
    mock.onGet('/program/').reply(200, [mockProgram]);
    mock.onGet('/programmed_workout/').reply(200, []);
    mock.onPatch('/program/1').reply(200, updatedProgram);

    // Mock additional API calls that might be made by chart components or other dependencies
    mock.onGet('/gdpr/export').reply(200, {
      training_programs: [],
      programmed_workouts: [],
      workout_stages: [],
      programmed_exercises: [],
      set_schemes: [],
      user_one_rep_max: [],
      user_weight_unit_preferences: [],
    });

    // Fix: getUserWeightUnitPreferences needs user ID in URL
    mock.onGet('/user_weight_unit_preference/test-user-id').reply(200, []);

    mock.onGet('/exercise_muscle/').reply(200, []);

    mock.onGet('/exercise_equipment/').reply(200, []);

    // Fix: getIndividualExercise needs exercise name in URL
    mock.onGet(/\/exercise\/[^/]+$/).reply(200, {
      name: 'Test Exercise',
      description: 'Test Description',
      movement_type: 'push',
      is_unilateral: false,
      is_upper: true,
      is_accessory: false,
    });

    // Fix: getExerciseMuscles needs exercise name in URL
    mock.onGet(/\/exercise\/[^/]+\/muscle$/).reply(200, []);

    // Fix: getExerciseEquipment needs exercise name in URL
    mock.onGet(/\/exercise\/[^/]+\/equipment$/).reply(200, []);

    await act(async () => {
      renderWithProviders(<ProgramManagement user={mockUser} />);
    });

    await waitFor(() => {
      const editButton = screen.getByLabelText(/edit program/i);
      fireEvent.click(editButton);
    });

    const nameInput = screen.getByDisplayValue('Test Program');
    fireEvent.change(nameInput, { target: { value: 'Updated Program' } });

    const updateButton = screen.getByRole('button', { name: /update program/i });
    fireEvent.click(updateButton);

    await waitFor(() => {
      expect(mock.history.patch).toHaveLength(1);
      expect(mock.history.patch[0].params).toEqual({
        name: 'Updated Program',
        current_week_number: 2,
        is_active: true,
      });
    });
  });

  it('opens delete dialog when delete button is clicked', async () => {
    // Mock user-related API calls from AuthContext
    mock.onGet('/user/me').reply(200, mockUser);
    mock.onPost('/user/').reply(200, mockUser);

    // Mock program and workout API calls
    mock.onGet('/program/').reply(200, [mockProgram]);
    mock.onGet('/programmed_workout/').reply(200, []);

    // Mock additional API calls that might be made by chart components or other dependencies
    mock.onGet('/gdpr/export').reply(200, {
      training_programs: [],
      programmed_workouts: [],
      workout_stages: [],
      programmed_exercises: [],
      set_schemes: [],
      user_one_rep_max: [],
      user_weight_unit_preferences: [],
    });

    // Fix: getUserWeightUnitPreferences needs user ID in URL
    mock.onGet('/user_weight_unit_preference/test-user-id').reply(200, []);

    mock.onGet('/exercise_muscle/').reply(200, []);

    mock.onGet('/exercise_equipment/').reply(200, []);

    // Fix: getIndividualExercise needs exercise name in URL
    mock.onGet(/\/exercise\/[^/]+$/).reply(200, {
      name: 'Test Exercise',
      description: 'Test Description',
      movement_type: 'push',
      is_unilateral: false,
      is_upper: true,
      is_accessory: false,
    });

    // Fix: getExerciseMuscles needs exercise name in URL
    mock.onGet(/\/exercise\/[^/]+\/muscle$/).reply(200, []);

    // Fix: getExerciseEquipment needs exercise name in URL
    mock.onGet(/\/exercise\/[^/]+\/equipment$/).reply(200, []);

    await act(async () => {
      renderWithProviders(<ProgramManagement user={mockUser} />);
    });

    await waitFor(() => {
      const deleteButton = screen.getByLabelText(/delete program/i);
      fireEvent.click(deleteButton);
    });

    expect(screen.getByRole('heading', { name: 'Delete Program' })).toBeInTheDocument();
    expect(screen.getByText(/Are you sure you want to delete this program/)).toBeInTheDocument();
  });

  it('deletes a program successfully', async () => {
    // Mock user-related API calls from AuthContext
    mock.onGet('/user/me').reply(200, mockUser);
    mock.onPost('/user/').reply(200, mockUser);

    // Mock program and workout API calls
    mock.onGet('/program/').reply(200, [mockProgram]);
    mock.onGet('/programmed_workout/').reply(200, []);
    mock.onDelete('/program/1').reply(200, mockProgram);

    // Mock additional API calls that might be made by chart components or other dependencies
    mock.onGet('/gdpr/export').reply(200, {
      training_programs: [],
      programmed_workouts: [],
      workout_stages: [],
      programmed_exercises: [],
      set_schemes: [],
      user_one_rep_max: [],
      user_weight_unit_preferences: [],
    });

    // Fix: getUserWeightUnitPreferences needs user ID in URL
    mock.onGet('/user_weight_unit_preference/test-user-id').reply(200, []);

    mock.onGet('/exercise_muscle/').reply(200, []);

    mock.onGet('/exercise_equipment/').reply(200, []);

    // Fix: getIndividualExercise needs exercise name in URL
    mock.onGet(/\/exercise\/[^/]+$/).reply(200, {
      name: 'Test Exercise',
      description: 'Test Description',
      movement_type: 'push',
      is_unilateral: false,
      is_upper: true,
      is_accessory: false,
    });

    // Fix: getExerciseMuscles needs exercise name in URL
    mock.onGet(/\/exercise\/[^/]+\/muscle$/).reply(200, []);

    // Fix: getExerciseEquipment needs exercise name in URL
    mock.onGet(/\/exercise\/[^/]+\/equipment$/).reply(200, []);

    await act(async () => {
      renderWithProviders(<ProgramManagement user={mockUser} />);
    });

    await waitFor(() => {
      const deleteButton = screen.getByLabelText(/delete program/i);
      fireEvent.click(deleteButton);
    });

    const confirmDeleteButton = screen.getByRole('button', { name: /delete program/i });
    fireEvent.click(confirmDeleteButton);

    await waitFor(() => {
      expect(mock.history.delete).toHaveLength(1);
      expect(mock.history.delete[0].url).toBe('/program/1');
    });
  });

  it('displays program workouts correctly', async () => {
    // Mock user-related API calls from AuthContext
    mock.onGet('/user/me').reply(200, mockUser);
    mock.onPost('/user/').reply(200, mockUser);

    // Mock program and workout API calls
    mock.onGet('/program/').reply(200, [mockProgram]);
    mock.onGet('/programmed_workout/').reply(200, [mockWorkout]);

    // Mock additional API calls that might be made by chart components or other dependencies
    mock.onGet('/gdpr/export').reply(200, {
      training_programs: [],
      programmed_workouts: [],
      workout_stages: [],
      programmed_exercises: [],
      set_schemes: [],
      user_one_rep_max: [],
      user_weight_unit_preferences: [],
    });

    // Fix: getUserWeightUnitPreferences needs user ID in URL
    mock.onGet('/user_weight_unit_preference/test-user-id').reply(200, []);

    mock.onGet('/exercise_muscle/').reply(200, []);

    mock.onGet('/exercise_equipment/').reply(200, []);

    // Fix: getIndividualExercise needs exercise name in URL
    mock.onGet(/\/exercise\/[^/]+$/).reply(200, {
      name: 'Test Exercise',
      description: 'Test Description',
      movement_type: 'push',
      is_unilateral: false,
      is_upper: true,
      is_accessory: false,
    });

    // Fix: getExerciseMuscles needs exercise name in URL
    mock.onGet(/\/exercise\/[^/]+\/muscle$/).reply(200, []);

    // Fix: getExerciseEquipment needs exercise name in URL
    mock.onGet(/\/exercise\/[^/]+\/equipment$/).reply(200, []);

    await act(async () => {
      renderWithProviders(<ProgramManagement user={mockUser} />);
    });

    await waitFor(() => {
      expect(screen.getByText('Recent Workouts:')).toBeInTheDocument();
      expect(screen.getByText('Day 1: Push Day')).toBeInTheDocument();
    });
  });

  it('disables create button when program name is empty', async () => {
    mock.onGet('/program/').reply(200, []);
    mock.onGet('/programmed_workout/').reply(200, []);

    await act(async () => {
      renderWithProviders(<ProgramManagement user={mockUser} />);
    });

    await waitFor(() => {
      const createButton = screen.getByRole('button', { name: /create program/i });
      fireEvent.click(createButton);
    });

    const createDialogButton = screen.getByRole('button', { name: /create program/i });
    expect(createDialogButton).toBeDisabled();
  });

  it('closes dialogs when cancel is clicked', async () => {
    mock.onGet('/program/').reply(200, [mockProgram]);
    mock.onGet('/programmed_workout/').reply(200, []);

    await act(async () => {
      renderWithProviders(<ProgramManagement user={mockUser} />);
    });

    await waitFor(() => {
      const createButton = screen.getByRole('button', { name: /create program/i });
      fireEvent.click(createButton);
    });

    const cancelButton = screen.getByRole('button', { name: /cancel/i });
    fireEvent.click(cancelButton);

    await waitFor(() => {
      expect(screen.queryByText('Create New Program')).not.toBeInTheDocument();
    });
  });

  it('displays program creation date', async () => {
    // Mock user-related API calls from AuthContext
    mock.onGet('/user/me').reply(200, mockUser);
    mock.onPost('/user/').reply(200, mockUser);

    // Mock program and workout API calls
    mock.onGet('/program/').reply(200, [mockProgram]);
    mock.onGet('/programmed_workout/').reply(200, []);

    // Mock additional API calls that might be made by chart components or other dependencies
    mock.onGet('/gdpr/export').reply(200, {
      training_programs: [],
      programmed_workouts: [],
      workout_stages: [],
      programmed_exercises: [],
      set_schemes: [],
      user_one_rep_max: [],
      user_weight_unit_preferences: [],
    });

    // Fix: getUserWeightUnitPreferences needs user ID in URL
    mock.onGet('/user_weight_unit_preference/test-user-id').reply(200, []);

    mock.onGet('/exercise_muscle/').reply(200, []);

    mock.onGet('/exercise_equipment/').reply(200, []);

    // Fix: getIndividualExercise needs exercise name in URL
    mock.onGet(/\/exercise\/[^/]+$/).reply(200, {
      name: 'Test Exercise',
      description: 'Test Description',
      movement_type: 'push',
      is_unilateral: false,
      is_upper: true,
      is_accessory: false,
    });

    // Fix: getExerciseMuscles needs exercise name in URL
    mock.onGet(/\/exercise\/[^/]+\/muscle$/).reply(200, []);

    // Fix: getExerciseEquipment needs exercise name in URL
    mock.onGet(/\/exercise\/[^/]+\/equipment$/).reply(200, []);

    await act(async () => {
      renderWithProviders(<ProgramManagement user={mockUser} />);
    });

    await waitFor(
      () => {
        expect(screen.getByText(/Created:/)).toBeInTheDocument();
      },
      { timeout: 10000 }
    );
  });

  it('shows pause/activate button for programs', async () => {
    // Mock user-related API calls from AuthContext
    mock.onGet('/user/me').reply(200, mockUser);
    mock.onPost('/user/').reply(200, mockUser);

    // Mock program and workout API calls
    mock.onGet('/program/').reply(200, [mockProgram]);
    mock.onGet('/programmed_workout/').reply(200, []);

    // Mock additional API calls that might be made by chart components or other dependencies
    mock.onGet('/gdpr/export').reply(200, {
      training_programs: [],
      programmed_workouts: [],
      workout_stages: [],
      programmed_exercises: [],
      set_schemes: [],
      user_one_rep_max: [],
      user_weight_unit_preferences: [],
    });

    // Fix: getUserWeightUnitPreferences needs user ID in URL
    mock.onGet('/user_weight_unit_preference/test-user-id').reply(200, []);

    mock.onGet('/exercise_muscle/').reply(200, []);

    mock.onGet('/exercise_equipment/').reply(200, []);

    // Fix: getIndividualExercise needs exercise name in URL
    mock.onGet(/\/exercise\/[^/]+$/).reply(200, {
      name: 'Test Exercise',
      description: 'Test Description',
      movement_type: 'push',
      is_unilateral: false,
      is_upper: true,
      is_accessory: false,
    });

    // Fix: getExerciseMuscles needs exercise name in URL
    mock.onGet(/\/exercise\/[^/]+\/muscle$/).reply(200, []);

    // Fix: getExerciseEquipment needs exercise name in URL
    mock.onGet(/\/exercise\/[^/]+\/equipment$/).reply(200, []);

    await act(async () => {
      renderWithProviders(<ProgramManagement user={mockUser} />);
    });

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /pause/i })).toBeInTheDocument();
    });
  });

  it('verifies API calls are made with correct endpoints', async () => {
    // Mock user-related API calls from AuthContext
    mock.onGet('/user/me').reply(200, mockUser);
    mock.onPost('/user/').reply(200, mockUser);

    // Mock program and workout API calls
    mock.onGet('/program/').reply(200, []);
    mock.onGet('/programmed_workout/').reply(200, []);

    // Mock additional API calls that might be made by chart components or other dependencies
    mock.onGet('/gdpr/export').reply(200, {
      training_programs: [],
      programmed_workouts: [],
      workout_stages: [],
      programmed_exercises: [],
      set_schemes: [],
      user_one_rep_max: [],
      user_weight_unit_preferences: [],
    });

    // Fix: getUserWeightUnitPreferences needs user ID in URL
    mock.onGet('/user_weight_unit_preference/test-user-id').reply(200, []);

    mock.onGet('/exercise_muscle/').reply(200, []);

    mock.onGet('/exercise_equipment/').reply(200, []);

    // Fix: getIndividualExercise needs exercise name in URL
    mock.onGet(/\/exercise\/[^/]+$/).reply(200, {
      name: 'Test Exercise',
      description: 'Test Description',
      movement_type: 'push',
      is_unilateral: false,
      is_upper: true,
      is_accessory: false,
    });

    // Fix: getExerciseMuscles needs exercise name in URL
    mock.onGet(/\/exercise\/[^/]+\/muscle$/).reply(200, []);

    // Fix: getExerciseEquipment needs exercise name in URL
    mock.onGet(/\/exercise\/[^/]+\/equipment$/).reply(200, []);

    await act(async () => {
      renderWithProviders(<ProgramManagement user={mockUser} />);
    });

    await waitFor(() => {
      expect(mock.history.get).toHaveLength(2);
      expect(mock.history.get[0].url).toBe('/program/');
      expect(mock.history.get[1].url).toBe('/programmed_workout/');
    });
  });
});
