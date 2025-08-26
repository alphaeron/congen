import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen, waitFor } from '@testing-library/react';
import AxiosMockAdapter from 'axios-mock-adapter';
import * as React from 'react';
import { MemoryRouter } from 'react-router';

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
    await waitFor(() => {
      expect(mockAdapter.history.get.length).toBe(5);
    });

    // Wait for the header to be rendered
    await waitFor(() => {
      const exerciseElement = screen.getByTestId('exerciseHeader');
      expect(exerciseElement).toBeInTheDocument();
      expect(exerciseElement).toHaveTextContent('Exercise Library');
    });
  });
});
