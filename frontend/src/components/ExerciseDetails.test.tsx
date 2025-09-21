import { render, screen, waitFor, act } from '@testing-library/react';
import AxiosMockAdapter from 'axios-mock-adapter';
import React from 'react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';

import { ExerciseDetails } from './ExerciseDetails';
import { getExerciseMuscles } from '../api/exercise';
import {
  EQUIPMENT,
  EXERCISE,
  EXERCISE_MUSCLE,
  EXERCISE_EQUIPMENT,
  MUSCLE,
} from '../__mocks__/data';
import { ENDPOINT } from '../api/endpoint';

// Mock DataContext
const mockUseData = jest.fn();
jest.mock('../contexts/DataContext', () => ({
  useData: () => mockUseData(),
  DataProvider: ({ children }: { children: React.ReactNode }) => children,
}));

// Mock AuthContext
const mockUseAuth = jest.fn();
jest.mock('../contexts/AuthContext', () => ({
  useAuth: () => mockUseAuth(),
  AuthProvider: ({ children }: { children: React.ReactNode }) => children,
}));

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
    mockAdapter.onGet('/user_exercise_preference/test-user-id').reply(200, []);

    // Set up default mock data for DataContext
    const defaultMockDataContext = {
      userData: null,
      exerciseMuscleData: new Map(),
      weightUnitPreferences: [],
      exerciseData: new Map(),
      exerciseEquipmentData: new Map(),
      muscleData: new Map(),
      equipmentData: new Map(),
      programData: new Map(),
      allExercises: [],
      allMuscles: [],
      allEquipment: [],
      isLoading: false,
      error: null,
      refreshData: jest.fn(),
      isDataStale: false,
      getExercise: jest.fn().mockResolvedValue(EXERCISE),
      getExerciseMuscles: jest.fn().mockResolvedValue([EXERCISE_MUSCLE]),
      getExerciseEquipmentData: jest.fn().mockResolvedValue([EXERCISE_EQUIPMENT]),
      getMuscle: jest.fn().mockResolvedValue(MUSCLE),
      getEquipment: jest.fn().mockResolvedValue(EQUIPMENT),
      getProgram: jest.fn(),
      loadAllExercises: jest.fn(),
      loadAllMuscles: jest.fn(),
      loadAllEquipment: jest.fn(),
    };
    
    mockUseData.mockReturnValue(defaultMockDataContext);
    
    // Set up mock auth context
    mockUseAuth.mockReturnValue({
      user: { id: 'test-user-id' },
      login: jest.fn(),
      logout: jest.fn(),
      isLoading: false,
    });
  });

  afterEach(() => {
    mockAdapter.reset();
    jest.clearAllMocks();
  });

  it('Renders the equipment', async () => {
    await act(async () => {
      render(
        <QueryClientProvider client={queryClient}>
          <ExerciseDetails exerciseName={EXERCISE.name} />
        </QueryClientProvider>
      );
    });

    const equipmentRegex = new RegExp(`^${EXERCISE_EQUIPMENT.equipment_name}$`, 'i');
    expect(screen.getByText(equipmentRegex)).toBeInTheDocument();
  });

  it('Renders the muscle', async () => {
    await act(async () => {
      render(
        <QueryClientProvider client={queryClient}>
          <ExerciseDetails exerciseName={EXERCISE.name} />
        </QueryClientProvider>
      );
    });

    const muscleRegex = new RegExp(`^${EXERCISE_MUSCLE.muscle_name}$`, 'i');
    expect(screen.getByText(muscleRegex)).toBeInTheDocument();
  });

  it('Renders the exercise name', async () => {
    await act(async () => {
      render(
        <QueryClientProvider client={queryClient}>
          <ExerciseDetails exerciseName={EXERCISE.name} />
        </QueryClientProvider>
      );
    });

    const regex = new RegExp(`^${EXERCISE.name}$`, 'i');
    expect(screen.getByText(regex)).toBeInTheDocument();
  });

  it('Renders the exercise description', async () => {
    await act(async () => {
      render(
        <QueryClientProvider client={queryClient}>
          <ExerciseDetails exerciseName={EXERCISE.name} />
        </QueryClientProvider>
      );
    });

    expect(screen.getByText(EXERCISE.description)).toBeInTheDocument();
  });

  it('Renders the exercise movementType', async () => {
    await act(async () => {
      render(
        <QueryClientProvider client={queryClient}>
          <ExerciseDetails exerciseName={EXERCISE.name} />
        </QueryClientProvider>
      );
    });

    expect(screen.getByText('MovementType Exercise')).toBeInTheDocument();
  });

  it('Renders the exercise isUnilateral', async () => {
    await act(async () => {
      render(
        <QueryClientProvider client={queryClient}>
          <ExerciseDetails exerciseName={EXERCISE.name} />
        </QueryClientProvider>
      );
    });

    expect(screen.getByText(EXERCISE.is_unilateral ? 'Unilateral' : 'Bilateral')).toBeInTheDocument();
  });

  it('Renders the exercise isUpper', async () => {
    await act(async () => {
      render(
        <QueryClientProvider client={queryClient}>
          <ExerciseDetails exerciseName={EXERCISE.name} />
        </QueryClientProvider>
      );
    });

    expect(screen.getByText(EXERCISE.is_upper ? 'Upper Body' : 'Lower Body')).toBeInTheDocument();
  });

  it('Renders the exercise isAccessory', async () => {
    await act(async () => {
      render(
        <QueryClientProvider client={queryClient}>
          <ExerciseDetails exerciseName={EXERCISE.name} />
        </QueryClientProvider>
      );
    });

    expect(screen.getByText(EXERCISE.is_accessory ? 'Accessory' : 'Primary Movement')).toBeInTheDocument();
  });
});

