import { render, screen, waitFor, act } from '@testing-library/react';
import MockAdapter from 'axios-mock-adapter';
import React from 'react';
import { MemoryRouter } from 'react-router';

import { ExerciseAnalytics } from './ExerciseAnalytics';
import { ENDPOINT } from '../api/endpoint';
import type { User } from '../api/types';

// Mock Nivo charts to avoid rendering issues in tests
interface RadialBarData {
  id: string;
  data: Array<{ x: number; y: number }>;
}

interface ChartData {
  children?: Array<{ id: string }>;
}

jest.mock('@nivo/radial-bar', () => ({
  ResponsiveRadialBar: ({ data }: { data: RadialBarData[] }) => (
    <div data-testid="radial-bar-chart">
      {data.map((series: RadialBarData) => (
        <div key={series.id} data-testid={`radial-series-${series.id}`}>
          {series.data.length} points
        </div>
      ))}
    </div>
  ),
}));

jest.mock('@nivo/sunburst', () => ({
  ResponsiveSunburst: ({ data }: { data: ChartData }) => (
    <div data-testid="sunburst-chart">{data.children?.length || 0} children</div>
  ),
}));

jest.mock('@nivo/treemap', () => ({
  ResponsiveTreeMap: ({ data }: { data: ChartData }) => (
    <div data-testid="treemap-chart">{data.children?.length || 0} items</div>
  ),
}));

jest.mock('@nivo/icicle', () => ({
  ResponsiveIcicle: ({ data }: { data: ChartData }) => (
    <div data-testid="icicle-chart">{data.children?.length || 0} children</div>
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

describe('ExerciseAnalytics', () => {
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
      renderWithProviders(<ExerciseAnalytics user={mockUser} />);
    });

    // Component should render without crashing
    await waitFor(() => {
      expect(screen.getByText('Exercise Analytics')).toBeInTheDocument();
    });
  }, 10000);

  it('should display basic component structure', async () => {
    // Mock all possible API calls with correct endpoints
    mock.onGet('/programmed_workout/').reply(200, []);
    mock.onGet('/user_one_rep_max/test-user-id').reply(200, []);
    mock.onGet('/user_weight_unit_preference/test-user-id').reply(200, []);
    mock.onGet('/gdpr/export').reply(200, { training_programs: [], data_retention_policies: [] });
    mock.onGet('/exercise_muscle/').reply(200, []);
    mock.onGet('/exercise_equipment/').reply(200, []);

    await act(async () => {
      renderWithProviders(<ExerciseAnalytics user={mockUser} />);
    });

    // Check that the component shows the empty state message
    await waitFor(() => {
      expect(screen.getByText('Exercise Analytics')).toBeInTheDocument();
      expect(
        screen.getByText(/Complete your first workout to see exercise analytics and insights/)
      ).toBeInTheDocument();
    });
  });
});
