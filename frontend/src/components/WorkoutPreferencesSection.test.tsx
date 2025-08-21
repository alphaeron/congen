import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import MockAdapter from 'axios-mock-adapter';
import React from 'react';
import { BrowserRouter } from 'react-router';

import { WorkoutPreferencesSection } from './WorkoutPreferencesSection';
import { ENDPOINT } from '../api/endpoint';
import type { User } from '../api/types';

// Create axios mock adapter for the ENDPOINT instance
const mock = new MockAdapter(ENDPOINT);

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
  return render(<BrowserRouter>{component}</BrowserRouter>);
};

describe('WorkoutPreferencesSection', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mock.reset();

    // Mock exercises API
    mock.onGet('/exercise/').reply(200, [
      { name: 'Bench Press', description: 'Chest exercise', movement_type: 'push', is_unilateral: false, is_upper: true, is_accessory: false },
      { name: 'Deadlift', description: 'Back exercise', movement_type: 'pull', is_unilateral: false, is_upper: false, is_accessory: false },
      { name: 'Squat', description: 'Leg exercise', movement_type: 'push', is_unilateral: false, is_upper: false, is_accessory: false },
    ]);

    // Mock program preferences API - no existing preferences
    mock.onGet('/user_program_preferences/test-user-id').reply(404);

    // Mock weight unit preferences API - no existing preferences
    mock.onGet('/user_weight_unit_preference/test-user-id').reply(404);
  });

  afterAll(() => {
    mock.restore();
  });

  it('should render workout preferences section', async () => {
    renderWithProviders(<WorkoutPreferencesSection />);

    await waitFor(() => {
      expect(screen.getByText('Workout Preferences')).toBeInTheDocument();
      expect(screen.getByText('Program Settings')).toBeInTheDocument();
      expect(screen.getByText('Weight Unit Preferences')).toBeInTheDocument();
      expect(screen.getByText('Current Settings Summary')).toBeInTheDocument();
    }, { timeout: 10000 });
  });

  it('should show default program preferences', async () => {
    renderWithProviders(<WorkoutPreferencesSection />);

    await waitFor(() => {
      expect(screen.getByDisplayValue('3')).toBeInTheDocument(); // Default days per week
      expect(screen.getByDisplayValue('60')).toBeInTheDocument(); // Default session length
    }, { timeout: 10000 });
  });

  it('should show save button for program preferences', async () => {
    renderWithProviders(<WorkoutPreferencesSection />);

    await waitFor(() => {
      expect(screen.getByText('Save Program Preferences')).toBeInTheDocument();
    }, { timeout: 10000 });
  });

  it('should show add preference button for weight units', async () => {
    renderWithProviders(<WorkoutPreferencesSection />);

    await waitFor(() => {
      expect(screen.getByText('Add Preference')).toBeInTheDocument();
    }, { timeout: 10000 });
  });

  it('should open dialog when add preference button is clicked', async () => {
    renderWithProviders(<WorkoutPreferencesSection />);

    await waitFor(() => {
      const addButton = screen.getByText('Add Preference');
      fireEvent.click(addButton);
    }, { timeout: 10000 });

    expect(screen.getByText('Add Weight Unit Preference')).toBeInTheDocument();
    expect(screen.getAllByText('Exercise')[0]).toBeInTheDocument();
    expect(screen.getAllByText('Preferred Unit')[0]).toBeInTheDocument();
  });

  it('should show exercises in the dialog', async () => {
    renderWithProviders(<WorkoutPreferencesSection />);

    await waitFor(() => {
      const addButton = screen.getByText('Add Preference');
      fireEvent.click(addButton);
    }, { timeout: 10000 });

    await waitFor(() => {
      expect(screen.getByText('Bench Press')).toBeInTheDocument();
      expect(screen.getByText('Deadlift')).toBeInTheDocument();
      expect(screen.getByText('Squat')).toBeInTheDocument();
    }, { timeout: 10000 });
  });

  it('should show current settings summary', async () => {
    renderWithProviders(<WorkoutPreferencesSection />);

    await waitFor(() => {
      expect(screen.getByText('3 days/week')).toBeInTheDocument();
      expect(screen.getByText('60 minutes')).toBeInTheDocument();
      expect(screen.getByText('0 exercises')).toBeInTheDocument();
      expect(screen.getByText('Never')).toBeInTheDocument();
    }, { timeout: 10000 });
  });

  it('should save program preferences successfully', async () => {
    // Mock successful save
    mock.onPost('/user_program_preferences/').reply(200, {
      user_id: 'test-user-id',
      program_days_per_week: 4,
      session_time_length_in_minutes: 75,
      created_at: '2024-01-01T00:00:00Z',
      updated_at: '2024-01-01T00:00:00Z',
    });

    renderWithProviders(<WorkoutPreferencesSection />);

    await waitFor(() => {
      // Change days per week
      const daysSelect = screen.getByDisplayValue('3');
      fireEvent.mouseDown(daysSelect);
      const fourDaysOption = screen.getByText('4 days');
      fireEvent.click(fourDaysOption);

      // Change session length
      const sessionSelect = screen.getByDisplayValue('60');
      fireEvent.mouseDown(sessionSelect);
      const seventyFiveOption = screen.getByText('75 minutes');
      fireEvent.click(seventyFiveOption);

      // Save preferences
      const saveButton = screen.getByText('Save Program Preferences');
      fireEvent.click(saveButton);
    }, { timeout: 10000 });

    await waitFor(() => {
      expect(screen.getByText('Program preferences saved successfully')).toBeInTheDocument();
    }, { timeout: 10000 });
  });

  it('should add weight unit preference successfully', async () => {
    // Mock successful weight unit preference creation
    mock.onPut('/user_weight_unit_preference/').reply(200, {
      user_id: 'test-user-id',
      exercise_name: 'Bench Press',
      preferred_unit: 'LBS',
      created_at: '2024-01-01T00:00:00Z',
      updated_at: '2024-01-01T00:00:00Z',
    });

    // Mock weight unit preferences after adding
    mock.onGet('/user_weight_unit_preference/test-user-id').reply(200, [
      {
        user_id: 'test-user-id',
        exercise_name: 'Bench Press',
        preferred_unit: 'LBS',
        created_at: '2024-01-01T00:00:00Z',
        updated_at: '2024-01-01T00:00:00Z',
      },
    ]);

    renderWithProviders(<WorkoutPreferencesSection />);

    await waitFor(() => {
      // Open dialog
      const addButton = screen.getByText('Add Preference');
      fireEvent.click(addButton);
    }, { timeout: 10000 });

    await waitFor(() => {
      // Select exercise
      const exerciseSelect = screen.getByDisplayValue('');
      fireEvent.mouseDown(exerciseSelect);
      const benchPressOption = screen.getByText('Bench Press');
      fireEvent.click(benchPressOption);

      // Select unit
      const unitSelect = screen.getByDisplayValue('Pounds (LBS)');
      fireEvent.mouseDown(unitSelect);
      const lbsOption = screen.getByText('Pounds (LBS)');
      fireEvent.click(lbsOption);

      // Add preference
      const addPreferenceButton = screen.getByText('Add Preference');
      fireEvent.click(addPreferenceButton);
    }, { timeout: 10000 });

    await waitFor(() => {
      expect(screen.getByText('Weight unit preference added successfully')).toBeInTheDocument();
    }, { timeout: 10000 });
  });

  it('should handle loading state', async () => {
    // Mock slow API response
    mock.onGet('/exercise/').timeout();

    renderWithProviders(<WorkoutPreferencesSection />);

    expect(screen.getByRole('progressbar')).toBeInTheDocument();
  });

  it('should handle API errors gracefully', async () => {
    // Mock API error
    mock.onGet('/exercise/').reply(500, { message: 'Internal server error' });

    renderWithProviders(<WorkoutPreferencesSection />);

    await waitFor(() => {
      expect(screen.getByText('Failed to load preferences')).toBeInTheDocument();
    }, { timeout: 10000 });
  });

  it('should verify axios mock is working', async () => {
    // Test that the axios mock is working by making a simple request
    mock.onGet('/test').reply(200, { message: 'test' });

    const response = await ENDPOINT.get('/test');
    expect(response.data.message).toBe('test');
  });
});
