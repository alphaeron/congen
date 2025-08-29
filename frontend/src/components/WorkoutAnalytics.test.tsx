import { render, screen, waitFor, act } from '@testing-library/react';
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

describe('WorkoutAnalytics', () => {
  let mock: MockAdapter;

  beforeEach(() => {
    mock = new MockAdapter(ENDPOINT);
    jest.clearAllMocks();
  });

  afterEach(() => {
    if (mock) {
      mock.restore();
    }
  });

  it('should handle API failure gracefully', async () => {
    mock.onGet('/programmed_workout/').reply(500);
    mock.onGet('/user_weight_unit_preference/test-user-id').reply(200, []);
    mock.onGet('/gdpr/export').reply(200, { training_programs: [], data_retention_policies: [] });

    await act(async () => {
      renderWithProviders(<WorkoutAnalytics user={mockUser} />);
    });

    // Component should render without crashing
    await waitFor(() => {
      expect(screen.getByText('Workout Analytics')).toBeInTheDocument();
    });
  }, 10000);

  it('should display basic component structure', async () => {
    // Mock all possible API calls with correct endpoints
    mock.onGet('/programmed_workout/').reply(200, []);
    mock.onGet('/program/').reply(200, []);
    mock.onGet('/user_weight_unit_preference/test-user-id').reply(200, []);
    mock.onGet('/gdpr/export').reply(200, { training_programs: [], data_retention_policies: [] });
    mock.onGet(/\/exercise\/[^\/]+$/).reply(200, {});

    await act(async () => {
      renderWithProviders(<WorkoutAnalytics user={mockUser} />);
    });

    // Check that the component shows the empty state message
    await waitFor(() => {
      expect(screen.getByText('Workout Analytics')).toBeInTheDocument();
      expect(screen.getByText(/Complete your first workout to see workout analytics and insights/)).toBeInTheDocument();
    });
  });
});
