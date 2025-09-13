import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router';
import React from 'react';

import { ExercisePreferenceControls } from './ExercisePreferenceControls';
import { useAuth } from '../contexts/AuthContext';
import { useSnackbar } from 'notistack';
import {
  getUserExercisePreferences,
  upsertUserExercisePreference,
  removeUserExercisePreference,
} from '../api/userExercisePreference';

// Mock the dependencies
jest.mock('../contexts/AuthContext');
jest.mock('notistack');
jest.mock('../api/userExercisePreference');

const mockUseAuth = useAuth as jest.MockedFunction<typeof useAuth>;
const mockUseSnackbar = useSnackbar as jest.MockedFunction<typeof useSnackbar>;
const mockGetUserExercisePreferences = getUserExercisePreferences as jest.MockedFunction<typeof getUserExercisePreferences>;
const mockUpsertUserExercisePreference = upsertUserExercisePreference as jest.MockedFunction<typeof upsertUserExercisePreference>;
const mockRemoveUserExercisePreference = removeUserExercisePreference as jest.MockedFunction<typeof removeUserExercisePreference>;

describe('ExercisePreferenceControls', () => {
  const mockUser = {
    keycloak_id: 'test-user-id',
    email: 'test@example.com',
    first_name: 'Test',
    last_name: 'User',
    name: 'Test User',
    created_at: new Date(),
    updated_at: new Date(),
  };

  const mockEnqueueSnackbar = jest.fn();

  beforeEach(() => {
    mockUseAuth.mockReturnValue({
      user: mockUser,
      login: jest.fn(),
      logout: jest.fn(),
      isAuthenticated: true,
      isLoading: false,
      clearAuthState: jest.fn(),
    });

    mockUseSnackbar.mockReturnValue({
      enqueueSnackbar: mockEnqueueSnackbar,
      closeSnackbar: jest.fn(),
    });

    mockGetUserExercisePreferences.mockResolvedValue([]);
    mockUpsertUserExercisePreference.mockResolvedValue({
      user_id: 'test-user-id',
      exercise_name: 'Bench Press',
      should_avoid: false,
      created_at: new Date(),
      updated_at: new Date(),
    });
    mockRemoveUserExercisePreference.mockResolvedValue();
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  it('renders segmented variant by default', async () => {
    render(
      <MemoryRouter>
        <ExercisePreferenceControls exerciseName="Bench Press" />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('Prefer')).toBeInTheDocument();
      expect(screen.getByText('Neutral')).toBeInTheDocument();
      expect(screen.getByText('Ignore')).toBeInTheDocument();
    });
  });

  it('renders chip variant when specified', async () => {
    render(
      <MemoryRouter>
        <ExercisePreferenceControls exerciseName="Bench Press" variant="chip" />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('Neutral')).toBeInTheDocument();
    });
  });

  it('renders icon variant when specified', async () => {
    render(
      <MemoryRouter>
        <ExercisePreferenceControls exerciseName="Bench Press" variant="icon" />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByRole('button')).toBeInTheDocument();
    });
  });

  it('shows preferred state when exercise is preferred', async () => {
    mockGetUserExercisePreferences.mockResolvedValue([
      {
        user_id: 'test-user-id',
        exercise_name: 'Bench Press',
        should_avoid: false,
        created_at: new Date(),
        updated_at: new Date(),
      },
    ]);

    render(
      <MemoryRouter>
        <ExercisePreferenceControls exerciseName="Bench Press" />
      </MemoryRouter>
    );

    await waitFor(() => {
      const preferButton = screen.getByRole('button', { name: /prefer exercise/i });
      expect(preferButton).toHaveAttribute('aria-pressed', 'true');
    });
  });

  it('shows ignored state when exercise is ignored', async () => {
    mockGetUserExercisePreferences.mockResolvedValue([
      {
        user_id: 'test-user-id',
        exercise_name: 'Bench Press',
        should_avoid: true,
        created_at: new Date(),
        updated_at: new Date(),
      },
    ]);

    render(
      <MemoryRouter>
        <ExercisePreferenceControls exerciseName="Bench Press" />
      </MemoryRouter>
    );

    await waitFor(() => {
      const ignoreButton = screen.getByRole('button', { name: /ignore exercise/i });
      expect(ignoreButton).toHaveAttribute('aria-pressed', 'true');
    });
  });

  it('allows direct selection of prefer option', async () => {
    render(
      <MemoryRouter>
        <ExercisePreferenceControls exerciseName="Bench Press" />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('Prefer')).toBeInTheDocument();
    });

    fireEvent.click(screen.getByText('Prefer'));

    await waitFor(() => {
      expect(mockUpsertUserExercisePreference).toHaveBeenCalledWith('test-user-id', 'Bench Press', false);
      expect(mockEnqueueSnackbar).toHaveBeenCalledWith('Exercise preferred successfully', { variant: 'success' });
    });
  });

  it('allows direct selection of ignore option', async () => {
    render(
      <MemoryRouter>
        <ExercisePreferenceControls exerciseName="Bench Press" />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('Ignore')).toBeInTheDocument();
    });

    fireEvent.click(screen.getByText('Ignore'));

    await waitFor(() => {
      expect(mockUpsertUserExercisePreference).toHaveBeenCalledWith('test-user-id', 'Bench Press', true);
      expect(mockEnqueueSnackbar).toHaveBeenCalledWith('Exercise ignored successfully', { variant: 'success' });
    });
  });

  it('allows selection of neutral option to remove preference', async () => {
    mockGetUserExercisePreferences.mockResolvedValue([
      {
        user_id: 'test-user-id',
        exercise_name: 'Bench Press',
        should_avoid: false,
        created_at: new Date(),
        updated_at: new Date(),
      },
    ]);

    render(
      <MemoryRouter>
        <ExercisePreferenceControls exerciseName="Bench Press" />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('No Preference')).toBeInTheDocument();
    });

    fireEvent.click(screen.getByText('No Preference'));

    await waitFor(() => {
      expect(mockRemoveUserExercisePreference).toHaveBeenCalledWith('test-user-id', 'Bench Press');
      expect(mockEnqueueSnackbar).toHaveBeenCalledWith('Exercise preference removed', { variant: 'success' });
    });
  });

  it('shows neutral state when no preference exists', async () => {
    render(
      <MemoryRouter>
        <ExercisePreferenceControls exerciseName="Bench Press" />
      </MemoryRouter>
    );

    await waitFor(() => {
      const neutralButton = screen.getByRole('button', { name: /neutral preference/i });
      expect(neutralButton).toHaveAttribute('aria-pressed', 'true');
    });
  });
});
