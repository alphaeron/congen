import { render, screen, waitFor } from '@testing-library/react';
import MockAdapter from 'axios-mock-adapter';
import React from 'react';
import { MemoryRouter } from 'react-router';

import { ExerciseAnalytics } from './ExerciseAnalytics';
import { ENDPOINT } from '../api/endpoint';
import type { User } from '../api/types';

// Mock Nivo charts to avoid rendering issues in tests
jest.mock('@nivo/radial-bar', () => ({
  ResponsiveRadialBar: ({ data }: any) => (
    <div data-testid="radial-bar-chart">
      {data.map((series: any) => (
        <div key={series.id} data-testid={`radial-series-${series.id}`}>
          {series.data.length} points
        </div>
      ))}
    </div>
  ),
}));

jest.mock('@nivo/sunburst', () => ({
  ResponsiveSunburst: ({ data }: any) => (
    <div data-testid="sunburst-chart">
      {data.children?.length || 0} children
    </div>
  ),
}));

jest.mock('@nivo/treemap', () => ({
  ResponsiveTreeMap: ({ data }: any) => (
    <div data-testid="treemap-chart">
      {data.children?.length || 0} items
    </div>
  ),
}));

jest.mock('@nivo/icicle', () => ({
  ResponsiveIcicle: ({ data }: any) => (
    <div data-testid="icicle-chart">
      {data.children?.length || 0} children
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

const mockStage = {
  id: 1,
  programmed_workout_id: 1,
  stage_type_id: 1,
  position: 1,
  name: 'Warm-up',
  created_at: '2024-01-01T00:00:00Z',
  updated_at: '2024-01-01T00:00:00Z',
};

const mockExercise = {
  id: 1,
  workout_stage_id: 1,
  exercise_name: 'Bench Press',
  position: 1,
  notes: 'Test notes',
  created_at: '2024-01-01T00:00:00Z',
  updated_at: '2024-01-01T00:00:00Z',
};

const mockSetScheme = {
  id: 1,
  programmed_exercise_id: 1,
  set_number: 1,
  is_amrap: false,
  is_emom: false,
  use_tempo: false,
  target_weight: 135,
  performed_weight: 135,
  target_rep_count: 5,
  performed_rep_count: 5,
  rest_seconds: 90,
  created_at: '2024-01-01T00:00:00Z',
  updated_at: '2024-01-01T00:00:00Z',
};

const mockOneRepMax = {
  user_id: 'test-user-id',
  exercise_name: 'Bench Press',
  one_rep_max: 185,
  updated_at: '2024-01-01T00:00:00Z',
};

describe('ExerciseAnalytics', () => {
  let mock: MockAdapter;

  beforeEach(() => {
    mock = new MockAdapter(ENDPOINT);
  });

  afterEach(() => {
    mock.reset();
  });

  it('should show loading state initially', () => {
    mock.onGet('/programmed_workout/').reply(200, []);
    mock.onGet('/user_one_rep_max/test-user-id').reply(200, []);

    renderWithProviders(<ExerciseAnalytics user={mockUser} />);

    expect(screen.getByRole('progressbar')).toBeInTheDocument();
  });

  it('should handle API failure gracefully', async () => {
    mock.onGet('/programmed_workout/').reply(500);

    renderWithProviders(<ExerciseAnalytics user={mockUser} />);

    // Component should render without crashing
    await waitFor(() => {
      expect(screen.getByText('Exercise Analytics')).toBeInTheDocument();
    });
  }, 10000);

  it('should display basic component structure', () => {
    // Mock all possible API calls with correct endpoints
    mock.onGet('/programmed_workout/').reply(200, []);
    mock.onGet('/user_one_rep_max/test-user-id').reply(200, []);
    mock.onGet(/\/workout_stage\/workout\/\d+/).reply(200, []);
    mock.onGet(/\/programmed_exercise\/stage\/\d+/).reply(200, []);
    mock.onGet(/\/set_scheme\/exercise\/\d+/).reply(200, []);

    renderWithProviders(<ExerciseAnalytics user={mockUser} />);

    // Check that the component shows loading state initially
    expect(screen.getByRole('progressbar')).toBeInTheDocument();
  });
});
