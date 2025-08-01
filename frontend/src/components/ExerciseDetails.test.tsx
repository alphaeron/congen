import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen, waitFor } from '@testing-library/react';
import AxiosMockAdapter from 'axios-mock-adapter';
import * as React from 'react';

import { ENDPOINT } from '../api/endpoint';
import { ExerciseDetails } from './ExerciseDetails';

import {
  EQUIPMENT,
  EXERCISE,
  EXERCISE_MUSCLE,
  EXERCISE_EQUIPMENT,
  MUSCLE,
} from '../__mocks__/data';

describe('ExerciseDetails component', () => {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: {
        retry: false,
      },
    },
  });

  const mockAdapter = new AxiosMockAdapter(ENDPOINT);

  beforeEach(() => {
    mockAdapter.onGet(`/exercise/${EXERCISE.name}`).reply(200, EXERCISE);
    mockAdapter.onGet(`/exercise/${EXERCISE.name}/equipment`).reply(200, [EXERCISE_EQUIPMENT]);
    mockAdapter.onGet(`/exercise/${EXERCISE.name}/muscle`).reply(200, [EXERCISE_MUSCLE]);
    mockAdapter.onGet(`/equipment/${EXERCISE_EQUIPMENT.equipment_name}`).reply(200, EQUIPMENT);
    mockAdapter.onGet(`/muscle/${EXERCISE_MUSCLE.muscle_name}`).reply(200, MUSCLE);

    render(
      <QueryClientProvider client={queryClient}>
        <ExerciseDetails exerciseName={EXERCISE.name} />
      </QueryClientProvider>
    );
  });

  afterEach(() => {
    mockAdapter.reset();
  });

  it('Renders the equipment', async () => {
    const equipmentRegex = new RegExp(`^${EXERCISE_EQUIPMENT.equipment_name}$`, 'i');
    await waitFor(() => {
      // All 5 mocks should have been called.
      expect(mockAdapter.history.get.length).toBe(5);

      expect(screen.getByText(equipmentRegex)).toBeInTheDocument();
    });
  });

  it('Renders the muscle', async () => {
    const muscleRegex = new RegExp(`^${EXERCISE_MUSCLE.muscle_name}$`, 'i');
    expect(screen.getByText(muscleRegex)).toBeInTheDocument();
    await waitFor(() => {
      // All 5 mocks should have been called.
      expect(mockAdapter.history.get.length).toBe(5);

      expect(screen.getByText(muscleRegex)).toBeInTheDocument();
    });
  });

  it('Renders the exercise name', async () => {
    const regex = new RegExp(`^${EXERCISE.name}$`, 'i');
    expect(screen.getByText(regex)).toBeInTheDocument();
    await waitFor(() => {
      // All 5 mocks should have been called.
      expect(mockAdapter.history.get.length).toBe(5);

      expect(screen.getByText(regex)).toBeInTheDocument();
    });
  });

  it('Renders the exercise description', async () => {
    const regex = new RegExp(`^${EXERCISE.description}$`, 'i');
    expect(screen.getByText(regex)).toBeInTheDocument();
    await waitFor(() => {
      // All 5 mocks should have been called.
      expect(mockAdapter.history.get.length).toBe(5);

      expect(screen.getByText(regex)).toBeInTheDocument();
    });
  });

  it('Renders the exercise movementType', async () => {
    const regex = new RegExp(`${EXERCISE.movement_type}`, 'i');
    await waitFor(() => {
      // All 5 mocks should have been called.
      expect(mockAdapter.history.get.length).toBe(5);

      expect(screen.getByText(regex)).toBeInTheDocument();
    });
  });

  it('Renders the exercise isUnilateral', async () => {
    const text = EXERCISE.is_unilateral ? 'Unilateral' : 'Bilateral';
    const regex = new RegExp(`${text}`, 'i');
    await waitFor(() => {
      // All 5 mocks should have been called.
      expect(mockAdapter.history.get.length).toBe(5);

      expect(screen.getByText(regex)).toBeInTheDocument();
    });
  });

  it('Renders the exercise isUpper', async () => {
    const text = EXERCISE.is_upper ? 'Upper Body' : 'Lower Body';
    const regex = new RegExp(`${text}`, 'i');
    await waitFor(() => {
      // All 5 mocks should have been called.
      expect(mockAdapter.history.get.length).toBe(5);

      expect(screen.getByText(regex)).toBeInTheDocument();
    });
  });

  it('Renders the exercise isAccessory', async () => {
    const text = EXERCISE.is_accessory ? 'Accessory' : 'Primary Movement';
    const regex = new RegExp(`${text}`, 'i');
    await waitFor(() => {
      // All 5 mocks should have been called.
      expect(mockAdapter.history.get.length).toBe(5);

      expect(screen.getByText(regex)).toBeInTheDocument();
    });
  });
});
