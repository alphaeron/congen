import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen, waitFor } from '@testing-library/react';
import AxiosMockAdapter from 'axios-mock-adapter';
import * as React from 'react';
import { MemoryRouter } from 'react-router';

// Mock TanStack Virtual to render all items in tests
jest.mock('@tanstack/react-virtual', () => ({
  useVirtualizer: () => ({
    getVirtualItems: () => [
      { index: 0, key: '0', start: 0, size: 300 },
      { index: 1, key: '1', start: 300, size: 300 },
    ],
    getTotalSize: () => 600,
  }),
}));

// Mock the auth context
jest.mock('../contexts/AuthContext', () => ({
  useAuth: () => ({
    user: {
      keycloak_id: 'test-user-id',
      name: 'Test User',
    },
  }),
}));

import { ExerciseOverview } from './ExerciseOverview';
import {
  EXERCISE,
  EXERCISE_MUSCLE,
  EXERCISE_EQUIPMENT,
  MUSCLE,
  EQUIPMENT,
} from '../__mocks__/data';
import { ENDPOINT } from '../api/endpoint';

describe('ExerciseOverview component', () => {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: {
        retry: false,
      },
    },
  });

  const mockAdapter = new AxiosMockAdapter(ENDPOINT);

  beforeEach(() => {
    mockAdapter.onGet('/equipment/').reply(200, [EQUIPMENT]);
    mockAdapter.onGet('/exercise/').reply(200, [EXERCISE]);
    mockAdapter.onGet('/exercise_equipment/').reply(200, [EXERCISE_EQUIPMENT]);
    mockAdapter.onGet('/exercise_muscle/').reply(200, [EXERCISE_MUSCLE]);
    mockAdapter.onGet('/muscle/').reply(200, [MUSCLE]);

    render(
      <MemoryRouter>
        <QueryClientProvider client={queryClient}>
          <ExerciseOverview />
        </QueryClientProvider>
      </MemoryRouter>
    );
  });

  afterEach(() => {
    mockAdapter.reset();
  });

  it('Renders the exercise header', async () => {
    // Wait for all API calls to be made
    await waitFor(
      () => {
        expect(mockAdapter.history.get.length).toBe(5);
      },
      { timeout: 10000 }
    );

    // Wait for the header to be rendered
    await waitFor(
      () => {
        const exerciseElement = screen.getByTestId('exerciseHeader');
        expect(exerciseElement).toBeInTheDocument();
        expect(exerciseElement).toHaveTextContent('Exercise Library');
      },
      { timeout: 10000 }
    );
  }, 15000);
});
