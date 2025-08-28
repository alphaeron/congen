import { render, screen, waitFor } from '@testing-library/react';
import MockAdapter from 'axios-mock-adapter';
import React from 'react';
import { MemoryRouter } from 'react-router';

import { WorkoutAnalytics } from './WorkoutAnalytics';
import { ENDPOINT } from '../api/endpoint';
import type { User } from '../api/types';

// Mock Nivo charts to avoid rendering issues in tests
jest.mock('@nivo/icicle', () => ({
  ResponsiveIcicle: ({ data }: any) => (
    <div data-testid="icicle-chart">
      {data?.children?.length || 0} children
    </div>
  ),
}));

jest.mock('@nivo/stream', () => ({
  ResponsiveStream: ({ data }: any) => (
    <div data-testid="stream-chart">
      {data?.length || 0} data points
    </div>
  ),
}));

jest.mock('@nivo/bump', () => ({
  ResponsiveBump: ({ data }: any) => (
    <div data-testid="bump-chart">
      {data?.length || 0} series
    </div>
  ),
}));

const renderWithProviders = (component: React.ReactElement) => {
  return render(<MemoryRouter>{component}</MemoryRouter>);
};

const mockUser: User = {
  keycloak_id: 'test-user-id',
  name: 'Test User',
  created_at: '2024-01-01T00:00:00Z',
  updated_at: '2024-01-01T00:00:00Z',
  roles: ['user'],
};

const mockWorkout = {
  id: 1,
  program_id: 1,
  day_number: 1,
  name: 'Test Workout',
  created_at: '2024-01-01T00:00:00Z',
  updated_at: '2024-01-01T00:00:00Z',
};

const mockProgram = {
  id: 1,
  name: 'Test Program',
  is_active: true,
  current_week_number: 1,
  created_at: '2024-01-01T00:00:00Z',
  updated_at: '2024-01-01T00:00:00Z',
};

describe('WorkoutAnalytics', () => {
  let mock: MockAdapter;

  beforeEach(() => {
    mock = new MockAdapter(ENDPOINT);
  });

  afterEach(() => {
    mock.reset();
  });

  it('should show loading state initially', () => {
    mock.onGet('/programmed_workout/').reply(200, []);
    mock.onGet('/program/').reply(200, []);

    renderWithProviders(<WorkoutAnalytics user={mockUser} />);

    expect(screen.getByRole('progressbar')).toBeInTheDocument();
  });

  it('should handle API failure gracefully', async () => {
    mock.onGet('/programmed_workout/').reply(500);

    renderWithProviders(<WorkoutAnalytics user={mockUser} />);

    // Component should render without crashing
    await waitFor(() => {
      expect(screen.getByText('Workout Analytics')).toBeInTheDocument();
    });
  }, 10000);

  it('should display basic component structure', () => {
    // Mock all possible API calls with correct endpoints
    mock.onGet('/programmed_workout/').reply(200, []);
    mock.onGet('/program/').reply(200, []);
    mock.onGet(/\/workout_stage\/workout\/\d+/).reply(200, []);
    mock.onGet(/\/programmed_exercise\/stage\/\d+/).reply(200, []);
    mock.onGet(/\/set_scheme\/exercise\/\d+/).reply(200, []);

    renderWithProviders(<WorkoutAnalytics user={mockUser} />);

    // Check that the component shows loading state initially
    expect(screen.getByRole('progressbar')).toBeInTheDocument();
  });
});
