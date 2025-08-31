import { render, screen, fireEvent, waitFor, act } from '@testing-library/react';
import MockAdapter from 'axios-mock-adapter';
import React from 'react';
import { MemoryRouter } from 'react-router';

import { WorkoutPreferencesSection } from './WorkoutPreferencesSection';
import { ENDPOINT } from '../api/endpoint';
import type { User } from '../api/types';

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

  it('should render workout preferences section', async () => {
    // Mock API responses
    mock.onGet('/exercise/').reply(200, [
      {
        name: 'exerciseName',
        description: 'exerciseDescription',
        movement_type: 'movementType',
        is_unilateral: true,
        is_upper: true,
        is_accessory: false,
      },
    ]);
    mock.onGet('/user_weight_unit_preference/test-user-id').reply(404);

    await act(async () => {
      renderWithProviders(<WorkoutPreferencesSection />);
    });

    // Wait for loading to complete
    await waitFor(
      () => {
        expect(screen.queryByRole('progressbar')).not.toBeInTheDocument();
      },
      { timeout: 10000 }
    );

    // Then check for the content
    expect(screen.getAllByText('Weight Unit Preferences')).toHaveLength(2); // Main heading and card heading
    expect(
      screen.getByText('Set your preferred weight units for specific exercises.')
    ).toBeInTheDocument();
  });

  it('should show add preference button for weight units', async () => {
    // Mock API responses
    mock.onGet('/exercise/').reply(200, []);
    mock.onGet('/user_weight_unit_preference/test-user-id').reply(404);

    await act(async () => {
      renderWithProviders(<WorkoutPreferencesSection />);
    });

    // Wait for loading to complete first
    await waitFor(
      () => {
        expect(screen.queryByRole('progressbar')).not.toBeInTheDocument();
      },
      { timeout: 10000 }
    );

    expect(screen.getByText('Add Preference')).toBeInTheDocument();
  });

  it('should open dialog when add preference button is clicked', async () => {
    // Mock API responses
    mock.onGet('/exercise/').reply(200, []);
    mock.onGet('/user_weight_unit_preference/test-user-id').reply(404);

    await act(async () => {
      renderWithProviders(<WorkoutPreferencesSection />);
    });

    // Wait for loading to complete first
    await waitFor(
      () => {
        expect(screen.queryByRole('progressbar')).not.toBeInTheDocument();
      },
      { timeout: 10000 }
    );

    const addButton = screen.getByText('Add Preference');
    fireEvent.click(addButton);

    expect(screen.getByText('Add Weight Unit Preference')).toBeInTheDocument();
    expect(screen.getAllByText('Exercise')[0]).toBeInTheDocument();
    expect(screen.getAllByText('Preferred Unit')[0]).toBeInTheDocument();
  });

  it('should show no preferences message when no weight unit preferences exist', async () => {
    // Mock API responses
    mock.onGet('/exercise/').reply(200, []);
    mock.onGet('/user_weight_unit_preference/test-user-id').reply(404);

    await act(async () => {
      renderWithProviders(<WorkoutPreferencesSection />);
    });

    // Wait for loading to complete first
    await waitFor(
      () => {
        expect(screen.queryByRole('progressbar')).not.toBeInTheDocument();
      },
      { timeout: 10000 }
    );

    expect(screen.getByText('No weight unit preferences set yet.')).toBeInTheDocument();
  });

  it('should handle loading state', async () => {
    // Mock slow API response to ensure loading state is visible
    mock
      .onGet('/exercise/')
      .reply(() => new Promise(resolve => setTimeout(() => resolve([200, []]), 100)));
    mock.onGet('/user_weight_unit_preference/test-user-id').reply(404);

    await act(async () => {
      renderWithProviders(<WorkoutPreferencesSection />);
    });

    // Check for loading state immediately after rendering
    expect(screen.getByRole('progressbar')).toBeInTheDocument();
  });

  it('should verify axios mock is working', async () => {
    // Test that the axios mock is working by making a simple request
    mock.onGet('/test').reply(200, { message: 'test' });

    const response = await ENDPOINT.get('/test');
    expect(response.data.message).toBe('test');
  });
});
