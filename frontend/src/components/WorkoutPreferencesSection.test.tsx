import { render, screen, fireEvent, waitFor, act } from '@testing-library/react';
import MockAdapter from 'axios-mock-adapter';
import React from 'react';
import { MemoryRouter } from 'react-router';

import { WorkoutPreferencesSection } from './WorkoutPreferencesSection';
import { ENDPOINT } from '../api/endpoint';
import type { User } from '../api/types';
import { EXERCISE } from '../__mocks__/data';

// Mock the APIs to return mock data directly
jest.mock('../api/exercise', () => ({
  getExercises: jest.fn().mockResolvedValue([
    {
      name: 'exerciseName',
      description: 'exerciseDescription',
      movement_type: 'movementType',
      is_unilateral: true,
      is_upper: true,
      is_accessory: false,
    },
  ]),
}));

jest.mock('../api/userProgramPreferences', () => ({
  getUserProgramPreferences: jest.fn().mockRejectedValue(new Error('Not found')),
  updateUserProgramPreferences: jest.fn().mockResolvedValue({
    data: {
      user_id: 'test-user-id',
      program_days_per_week: 4,
      session_time_length_in_minutes: 75,
      created_at: '2024-01-01T00:00:00Z',
      updated_at: '2024-01-01T00:00:00Z',
    },
  }),
  createUserProgramPreferences: jest.fn().mockResolvedValue({
    data: {
      user_id: 'test-user-id',
      program_days_per_week: 3,
      session_time_length_in_minutes: 60,
      created_at: '2024-01-01T00:00:00Z',
      updated_at: '2024-01-01T00:00:00Z',
    },
  }),
}));

jest.mock('../api/userWeightUnitPreference', () => ({
  getUserWeightUnitPreferences: jest.fn().mockRejectedValue(new Error('Not found')),
  upsertUserWeightUnitPreference: jest.fn().mockResolvedValue({
    data: {
      user_id: 'test-user-id',
      exercise_name: 'exerciseName',
      preferred_unit: 'LBS',
      created_at: '2024-01-01T00:00:00Z',
      updated_at: '2024-01-01T00:00:00Z',
    },
  }),
  deleteUserWeightUnitPreference: jest.fn().mockResolvedValue({
    data: {
      user_id: 'test-user-id',
      exercise_name: 'exerciseName',
      preferred_unit: 'LBS',
      created_at: '2024-01-01T00:00:00Z',
      updated_at: '2024-01-01T00:00:00Z',
    },
  }),
  WeightUnit: {
    KG: 'KG',
    LBS: 'LBS',
  },
}));

// Create axios mock adapter for the ENDPOINT instance
const mockAdapter = new MockAdapter(ENDPOINT);

const mockUser: User = {
  keycloak_id: 'test-user-id',
  name: 'John Doe',
  created_at: '2024-01-01T00:00:00Z',
  updated_at: '2024-01-01T00:00:00Z',
  roles: ['user'],
};

// Mock the auth context
jest.mock('../contexts/AuthContext', () => ({
  useAuth: () => ({
    user: mockUser,
  }),
}));

const renderWithProviders = (component: React.ReactElement) => {
  return render(<MemoryRouter>{component}</MemoryRouter>);
};

describe('WorkoutPreferencesSection', () => {
  beforeEach(() => {
    mockAdapter.reset();
  });

  afterAll(() => {
    mockAdapter.restore();
  });

  it('should render workout preferences section', async () => {
    await act(async () => {
      renderWithProviders(<WorkoutPreferencesSection />);
    });

    // Wait for loading to complete
    await waitFor(() => {
      expect(screen.queryByRole('progressbar')).not.toBeInTheDocument();
    }, { timeout: 10000 });

    // Then check for the content
    expect(screen.getByText('Workout Preferences')).toBeInTheDocument();
    expect(screen.getByText('Program Settings')).toBeInTheDocument();
    expect(screen.getAllByText('Weight Unit Preferences')).toHaveLength(2); // Appears in summary and main section
    expect(screen.getByText('Current Settings Summary')).toBeInTheDocument();
  });

  it('should show default program preferences', async () => {
    await act(async () => {
      renderWithProviders(<WorkoutPreferencesSection />);
    });

    // Wait for loading to complete first
    await waitFor(() => {
      expect(screen.queryByRole('progressbar')).not.toBeInTheDocument();
    }, { timeout: 10000 });

    expect(screen.getByDisplayValue('3')).toBeInTheDocument(); // Default days per week
    expect(screen.getByDisplayValue('60')).toBeInTheDocument(); // Default session length
  });

  it('should show save button for program preferences', async () => {
    await act(async () => {
      renderWithProviders(<WorkoutPreferencesSection />);
    });

    // Wait for loading to complete first
    await waitFor(() => {
      expect(screen.queryByRole('progressbar')).not.toBeInTheDocument();
    }, { timeout: 10000 });

    expect(screen.getByText('Save Program Preferences')).toBeInTheDocument();
  });

  it('should show add preference button for weight units', async () => {
    await act(async () => {
      renderWithProviders(<WorkoutPreferencesSection />);
    });

    // Wait for loading to complete first
    await waitFor(() => {
      expect(screen.queryByRole('progressbar')).not.toBeInTheDocument();
    }, { timeout: 10000 });

    expect(screen.getByText('Add Preference')).toBeInTheDocument();
  });

  it('should open dialog when add preference button is clicked', async () => {
    await act(async () => {
      renderWithProviders(<WorkoutPreferencesSection />);
    });

    // Wait for loading to complete first
    await waitFor(() => {
      expect(screen.queryByRole('progressbar')).not.toBeInTheDocument();
    }, { timeout: 10000 });

    const addButton = screen.getByText('Add Preference');
    fireEvent.click(addButton);

    expect(screen.getByText('Add Weight Unit Preference')).toBeInTheDocument();
    expect(screen.getAllByText('Exercise')[0]).toBeInTheDocument();
    expect(screen.getAllByText('Preferred Unit')[0]).toBeInTheDocument();
  });

  it('should show current settings summary', async () => {
    await act(async () => {
      renderWithProviders(<WorkoutPreferencesSection />);
    });

    // Wait for loading to complete first
    await waitFor(() => {
      expect(screen.queryByRole('progressbar')).not.toBeInTheDocument();
    }, { timeout: 10000 });

    expect(screen.getByText('3 days/week')).toBeInTheDocument();
    expect(screen.getAllByText('60 minutes')).toHaveLength(2); // Appears in summary and select
    expect(screen.getByText('0 exercises')).toBeInTheDocument();
    expect(screen.getByText('Never')).toBeInTheDocument();
  });

  it('should handle loading state', async () => {
    // Mock slow API response
    mockAdapter.onGet('/exercise/').timeout();

    renderWithProviders(<WorkoutPreferencesSection />);

    expect(screen.getByRole('progressbar')).toBeInTheDocument();
  });

  it('should verify axios mock is working', async () => {
    // Test that the axios mock is working by making a simple request
    mockAdapter.onGet('/test').reply(200, { message: 'test' });

    const response = await ENDPOINT.get('/test');
    expect(response.data.message).toBe('test');
  });
});
