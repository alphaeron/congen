import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { useSnackbar } from 'notistack';
import React from 'react';
import { MemoryRouter } from 'react-router';

import { ExercisePreferenceControls } from './ExercisePreferenceControls';
import {
  getUserExercisePreferences,
  upsertUserExercisePreference,
  removeUserExercisePreference,
} from '../api/userExercisePreference';
import { useAuth } from '../contexts/AuthContext';

// Mock the dependencies
jest.mock('../contexts/AuthContext');
jest.mock('notistack');
jest.mock('../api/userExercisePreference');

// Mock DataContext
let mockUserExercisePreferences: unknown[] = [];
const mockLoadUserExercisePreferences = jest.fn();
const mockRefreshSpecificData = jest.fn();

jest.mock('../contexts/DataContext', () => ({
  useData: () => ({
    userExercisePreferences: mockUserExercisePreferences,
    loadUserExercisePreferences: mockLoadUserExercisePreferences,
    refreshSpecificData: mockRefreshSpecificData,
  }),
}));

const mockUseAuth = useAuth as jest.MockedFunction<typeof useAuth>;
const mockUseSnackbar = useSnackbar as jest.MockedFunction<typeof useSnackbar>;
const mockGetUserExercisePreferences = getUserExercisePreferences as jest.MockedFunction<
  typeof getUserExercisePreferences
>;
const mockUpsertUserExercisePreference = upsertUserExercisePreference as jest.MockedFunction<
  typeof upsertUserExercisePreference
>;
const mockRemoveUserExercisePreference = removeUserExercisePreference as jest.MockedFunction<
  typeof removeUserExercisePreference
>;

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

  const renderWithProviders = (component: React.ReactElement) => {
    return render(<MemoryRouter>{component}</MemoryRouter>);
  };

  beforeEach(() => {
    // Reset mock data
    mockUserExercisePreferences = [];
    mockLoadUserExercisePreferences.mockResolvedValue([]);
    mockRefreshSpecificData.mockResolvedValue(undefined);

    mockUseAuth.mockReturnValue({
      user: mockUser,
      login: jest.fn(),
      logout: jest.fn(),
      isAuthenticated: true,
      isLoading: false,
      clearAuthState: jest.fn(),
      refreshUser: jest.fn(),
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
    renderWithProviders(<ExercisePreferenceControls exerciseName="Bench Press" />);

    await waitFor(() => {
      expect(screen.getByText('Prefer')).toBeInTheDocument();
      expect(screen.getByText('Neutral')).toBeInTheDocument();
      expect(screen.getByText('Ignore')).toBeInTheDocument();
    });
  });

  it('renders chip variant when specified', async () => {
    renderWithProviders(<ExercisePreferenceControls exerciseName="Bench Press" variant="chip" />);

    await waitFor(() => {
      expect(screen.getByText('Neutral')).toBeInTheDocument();
    });
  });

  it('renders icon variant when specified', async () => {
    renderWithProviders(<ExercisePreferenceControls exerciseName="Bench Press" variant="icon" />);

    await waitFor(() => {
      expect(screen.getByRole('button')).toBeInTheDocument();
    });
  });

  it('shows preferred state when exercise is preferred', async () => {
    const preferredExercise = {
      user_id: 'test-user-id',
      exercise_name: 'Bench Press',
      should_avoid: false,
      created_at: new Date(),
      updated_at: new Date(),
    };

    mockUserExercisePreferences = [preferredExercise];
    mockGetUserExercisePreferences.mockResolvedValue([preferredExercise]);

    renderWithProviders(<ExercisePreferenceControls exerciseName="Bench Press" />);

    await waitFor(() => {
      const preferButton = screen.getByRole('button', { name: /prefer exercise/i });
      expect(preferButton).toHaveAttribute('aria-pressed', 'true');
    });
  });

  it('shows ignored state when exercise is ignored', async () => {
    const ignoredExercise = {
      user_id: 'test-user-id',
      exercise_name: 'Bench Press',
      should_avoid: true,
      created_at: new Date(),
      updated_at: new Date(),
    };

    mockUserExercisePreferences = [ignoredExercise];
    mockGetUserExercisePreferences.mockResolvedValue([ignoredExercise]);

    renderWithProviders(<ExercisePreferenceControls exerciseName="Bench Press" />);

    await waitFor(() => {
      const ignoreButton = screen.getByRole('button', { name: /ignore exercise/i });
      expect(ignoreButton).toHaveAttribute('aria-pressed', 'true');
    });
  });

  it('allows direct selection of prefer option', async () => {
    renderWithProviders(<ExercisePreferenceControls exerciseName="Bench Press" />);

    await waitFor(() => {
      expect(screen.getByText('Prefer')).toBeInTheDocument();
    });

    fireEvent.click(screen.getByText('Prefer'));

    await waitFor(() => {
      expect(mockUpsertUserExercisePreference).toHaveBeenCalledWith(
        'test-user-id',
        'Bench Press',
        false
      );
      expect(mockEnqueueSnackbar).toHaveBeenCalledWith('Exercise preferred successfully', {
        variant: 'success',
      });
    });
  });

  it('allows direct selection of ignore option', async () => {
    renderWithProviders(<ExercisePreferenceControls exerciseName="Bench Press" />);

    await waitFor(() => {
      expect(screen.getByText('Ignore')).toBeInTheDocument();
    });

    fireEvent.click(screen.getByText('Ignore'));

    await waitFor(() => {
      expect(mockUpsertUserExercisePreference).toHaveBeenCalledWith(
        'test-user-id',
        'Bench Press',
        true
      );
      expect(mockEnqueueSnackbar).toHaveBeenCalledWith('Exercise ignored successfully', {
        variant: 'success',
      });
    });
  });

  it('allows selection of neutral option to remove preference', async () => {
    const preferredExercise = {
      user_id: 'test-user-id',
      exercise_name: 'Bench Press',
      should_avoid: false,
      created_at: new Date(),
      updated_at: new Date(),
    };

    mockUserExercisePreferences = [preferredExercise];
    mockGetUserExercisePreferences.mockResolvedValue([preferredExercise]);

    renderWithProviders(<ExercisePreferenceControls exerciseName="Bench Press" />);

    await waitFor(
      () => {
        expect(screen.getByText('Neutral')).toBeInTheDocument();
      },
      { timeout: 10000 }
    );

    // Click on the Neutral button to remove the preference
    const neutralButton = screen.getByRole('button', { name: /neutral preference/i });
    fireEvent.click(neutralButton);

    await waitFor(
      () => {
        expect(mockRemoveUserExercisePreference).toHaveBeenCalledWith(
          'test-user-id',
          'Bench Press'
        );
        expect(mockEnqueueSnackbar).toHaveBeenCalledWith('Exercise preference removed', {
          variant: 'success',
        });
      },
      { timeout: 10000 }
    );
  }, 15000);

  it('shows neutral state when no preference exists', async () => {
    renderWithProviders(<ExercisePreferenceControls exerciseName="Bench Press" />);

    await waitFor(() => {
      const neutralButton = screen.getByRole('button', { name: /neutral preference/i });
      expect(neutralButton).toHaveAttribute('aria-pressed', 'true');
    });
  });
});
