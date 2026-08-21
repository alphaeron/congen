import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { useSnackbar } from 'notistack';
import React from 'react';
import { MemoryRouter } from 'react-router';

import { WeightUnitPreferenceControls } from './WeightUnitPreferenceControls';
import { WeightUnit } from '../api/types';
import { upsertUserWeightUnitPreference } from '../api/userWeightUnitPreference';
import { useAuth } from '../contexts/AuthContext';

jest.mock('../contexts/AuthContext');
jest.mock('notistack');
jest.mock('../api/userWeightUnitPreference');

let mockWeightUnitPreferences: unknown[] = [];
const mockRefreshSpecificData = jest.fn();

jest.mock('../contexts/DataContext', () => ({
  useData: () => ({
    weightUnitPreferences: mockWeightUnitPreferences,
    refreshSpecificData: mockRefreshSpecificData,
  }),
}));

const mockUseAuth = useAuth as jest.MockedFunction<typeof useAuth>;
const mockUseSnackbar = useSnackbar as jest.MockedFunction<typeof useSnackbar>;
const mockUpsertUserWeightUnitPreference = upsertUserWeightUnitPreference as jest.MockedFunction<
  typeof upsertUserWeightUnitPreference
>;

describe('WeightUnitPreferenceControls', () => {
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
    mockWeightUnitPreferences = [];
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

    mockUpsertUserWeightUnitPreference.mockResolvedValue({
      user_id: 'test-user-id',
      exercise_name: 'Bench Press',
      preferred_unit: WeightUnit.LBS,
      created_at: new Date(),
      updated_at: new Date(),
    });
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  it('defaults to KG when no preference exists', () => {
    renderWithProviders(<WeightUnitPreferenceControls exerciseName="Bench Press" />);

    expect(screen.getByLabelText('Set Bench Press to kilograms')).toHaveAttribute(
      'aria-pressed',
      'true'
    );
    expect(screen.getByLabelText('Set Bench Press to pounds')).toHaveAttribute(
      'aria-pressed',
      'false'
    );
  });

  it('uses existing preference as the starting state', () => {
    mockWeightUnitPreferences = [
      {
        user_id: 'test-user-id',
        exercise_name: 'Bench Press',
        preferred_unit: WeightUnit.LBS,
        created_at: new Date(),
        updated_at: new Date(),
      },
    ];

    renderWithProviders(<WeightUnitPreferenceControls exerciseName="Bench Press" />);

    expect(screen.getByLabelText('Set Bench Press to pounds')).toHaveAttribute(
      'aria-pressed',
      'true'
    );
    expect(screen.getByLabelText('Set Bench Press to kilograms')).toHaveAttribute(
      'aria-pressed',
      'false'
    );
  });

  it('upserts preference and refreshes DataContext on change', async () => {
    renderWithProviders(<WeightUnitPreferenceControls exerciseName="Bench Press" />);

    fireEvent.click(screen.getByLabelText('Set Bench Press to pounds'));

    await waitFor(() => {
      expect(mockUpsertUserWeightUnitPreference).toHaveBeenCalledWith(
        'test-user-id',
        'Bench Press',
        WeightUnit.LBS
      );
    });

    expect(mockRefreshSpecificData).toHaveBeenCalledWith('weightUnitPreferences');
    expect(mockEnqueueSnackbar).toHaveBeenCalledWith('Weight unit set to LBS', {
      variant: 'success',
    });
  });

  it('does not call the API when selecting the already active unit', async () => {
    mockWeightUnitPreferences = [
      {
        user_id: 'test-user-id',
        exercise_name: 'Bench Press',
        preferred_unit: WeightUnit.KG,
        created_at: new Date(),
        updated_at: new Date(),
      },
    ];

    renderWithProviders(<WeightUnitPreferenceControls exerciseName="Bench Press" />);

    fireEvent.click(screen.getByLabelText('Set Bench Press to kilograms'));

    await waitFor(() => {
      expect(mockUpsertUserWeightUnitPreference).not.toHaveBeenCalled();
    });
  });

  it('shows an error snackbar when the API request fails', async () => {
    mockUpsertUserWeightUnitPreference.mockRejectedValue(new Error('Network Error'));

    renderWithProviders(<WeightUnitPreferenceControls exerciseName="Bench Press" />);

    fireEvent.click(screen.getByLabelText('Set Bench Press to pounds'));

    await waitFor(() => {
      expect(mockEnqueueSnackbar).toHaveBeenCalledWith('Failed to update weight unit preference', {
        variant: 'error',
      });
    });
  });
});
