import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen, waitFor, act } from '@testing-library/react';
import AxiosMockAdapter from 'axios-mock-adapter';
import * as React from 'react';

// Mock the auth context
jest.mock('../contexts/AuthContext', () => ({
  useAuth: () => ({
    user: {
      keycloak_id: 'test-user-id',
      name: 'Test User',
    },
  }),
}));

import { ExerciseDetails } from './ExerciseDetails';
import {
  EQUIPMENT,
  EXERCISE,
  EXERCISE_MUSCLE,
  EXERCISE_EQUIPMENT,
  MUSCLE,
} from '../__mocks__/data';
import { ENDPOINT } from '../api/endpoint';

describe('ExerciseDetails component', () => {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: {
        retry: false,
      },
    },
  });

  const mockAdapter = new AxiosMockAdapter(ENDPOINT);

  beforeEach(async () => {
    mockAdapter.onGet(`/exercise/${EXERCISE.name}`).reply(200, EXERCISE);
    mockAdapter.onGet(`/exercise/${EXERCISE.name}/equipment`).reply(200, [EXERCISE_EQUIPMENT]);
    mockAdapter.onGet(`/exercise/${EXERCISE.name}/muscle`).reply(200, [EXERCISE_MUSCLE]);
    mockAdapter.onGet(`/equipment/${EXERCISE_EQUIPMENT.equipment_name}`).reply(200, EQUIPMENT);
    mockAdapter.onGet(`/muscle/${EXERCISE_MUSCLE.muscle_name}`).reply(200, MUSCLE);
    mockAdapter.onGet('/user_exercise_preference/test-user-id').reply(200, []);

    await act(async () => {
      render(
        <QueryClientProvider client={queryClient}>
          <ExerciseDetails exerciseName={EXERCISE.name} />
        </QueryClientProvider>
      );
    });

    // Wait for all async operations to complete
    await waitFor(
      () => {
        expect(mockAdapter.history.get.length).toBe(6);
      },
      { timeout: 10000 }
    );
  });

  afterEach(() => {
    mockAdapter.reset();
  });

  it('Renders the equipment', () => {
    const equipmentRegex = new RegExp(`^${EXERCISE_EQUIPMENT.equipment_name}$`, 'i');
    expect(screen.getByText(equipmentRegex)).toBeInTheDocument();
  });

  it('Renders the muscle', () => {
    const muscleRegex = new RegExp(`^${EXERCISE_MUSCLE.muscle_name}$`, 'i');
    expect(screen.getByText(muscleRegex)).toBeInTheDocument();
  });

  it('Renders the exercise name', () => {
    const regex = new RegExp(`^${EXERCISE.name}$`, 'i');
    expect(screen.getByText(regex)).toBeInTheDocument();
  });

  it('Renders the exercise description', () => {
    const regex = new RegExp(`^${EXERCISE.description}$`, 'i');
    expect(screen.getByText(regex)).toBeInTheDocument();
  });

  it('Renders the exercise movementType', () => {
    const regex = new RegExp(`${EXERCISE.movement_type}`, 'i');
    expect(screen.getByText(regex)).toBeInTheDocument();
  });

  it('Renders the exercise isUnilateral', () => {
    const text = EXERCISE.is_unilateral ? 'Unilateral' : 'Bilateral';
    const regex = new RegExp(`${text}`, 'i');
    expect(screen.getByText(regex)).toBeInTheDocument();
  });

  it('Renders the exercise isUpper', () => {
    const text = EXERCISE.is_upper ? 'Upper Body' : 'Lower Body';
    const regex = new RegExp(`${text}`, 'i');
    expect(screen.getByText(regex)).toBeInTheDocument();
  });

  it('Renders the exercise isAccessory', () => {
    const text = EXERCISE.is_accessory ? 'Accessory' : 'Primary Movement';
    const regex = new RegExp(`${text}`, 'i');
    expect(screen.getByText(regex)).toBeInTheDocument();
  });
});
