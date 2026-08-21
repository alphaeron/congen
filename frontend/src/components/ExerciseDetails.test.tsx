import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen, act } from '@testing-library/react';
import AxiosMockAdapter from 'axios-mock-adapter';
import { SnackbarProvider } from 'notistack';
import React from 'react';

import { ExerciseDetails } from './ExerciseDetails';
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

  const renderExerciseDetails = async () => {
    await act(async () => {
      render(
        <QueryClientProvider client={queryClient}>
          <SnackbarProvider>
            <ExerciseDetails exerciseName={EXERCISE.name} />
          </SnackbarProvider>
        </QueryClientProvider>
      );
    });
  };

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
      userExercisePreferences: [],
      userOneRepMaxes: [],
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
      refreshSpecificData: jest.fn(),
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
      loadUserExercisePreferences: jest.fn().mockResolvedValue([]),
      loadUserWeightUnitPreferences: jest.fn().mockResolvedValue([]),
    };

    mockUseData.mockReturnValue(defaultMockDataContext);

    // Set up mock auth context
    mockUseAuth.mockReturnValue({
      user: { keycloak_id: 'test-user-id', name: 'Test User' },
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
    await renderExerciseDetails();

    const equipmentRegex = new RegExp(`^${EXERCISE_EQUIPMENT.equipment_name}$`, 'i');
    expect(screen.getByText(equipmentRegex)).toBeInTheDocument();
  });

  it('Renders the muscle', async () => {
    await renderExerciseDetails();

    const muscleRegex = new RegExp(`^${EXERCISE_MUSCLE.muscle_name}$`, 'i');
    expect(screen.getByText(muscleRegex)).toBeInTheDocument();
  });

  it('Renders the exercise name', async () => {
    await renderExerciseDetails();

    const regex = new RegExp(`^${EXERCISE.name}$`, 'i');
    expect(screen.getByText(regex)).toBeInTheDocument();
  });

  it('Renders the exercise description', async () => {
    await renderExerciseDetails();

    expect(screen.getByText(EXERCISE.description)).toBeInTheDocument();
  });

  it('Renders the exercise movementType', async () => {
    await renderExerciseDetails();

    expect(screen.getByText('MovementType Exercise')).toBeInTheDocument();
  });

  it('Renders the exercise isUnilateral', async () => {
    await renderExerciseDetails();

    expect(
      screen.getByText(EXERCISE.is_unilateral ? 'Unilateral' : 'Bilateral')
    ).toBeInTheDocument();
  });

  it('Renders the exercise isUpper', async () => {
    await renderExerciseDetails();

    expect(screen.getByText(EXERCISE.is_upper ? 'Upper Body' : 'Lower Body')).toBeInTheDocument();
  });

  it('Renders the exercise isAccessory', async () => {
    await renderExerciseDetails();

    expect(
      screen.getByText(EXERCISE.is_accessory ? 'Accessory' : 'Primary Movement')
    ).toBeInTheDocument();
  });

  it('renders weight unit controls using existing preference state', async () => {
    mockUseData.mockReturnValue({
      userData: null,
      exerciseMuscleData: new Map(),
      weightUnitPreferences: [
        {
          user_id: 'test-user-id',
          exercise_name: EXERCISE.name,
          preferred_unit: 'LBS',
          created_at: new Date(),
          updated_at: new Date(),
        },
      ],
      userExercisePreferences: [],
      userOneRepMaxes: [],
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
      refreshSpecificData: jest.fn(),
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
      loadUserExercisePreferences: jest.fn().mockResolvedValue([]),
      loadUserWeightUnitPreferences: jest.fn().mockResolvedValue([]),
    });

    await renderExerciseDetails();

    expect(screen.getByLabelText(`Set ${EXERCISE.name} to pounds`)).toHaveAttribute(
      'aria-pressed',
      'true'
    );
    expect(screen.getByLabelText(`Set ${EXERCISE.name} to kilograms`)).toHaveAttribute(
      'aria-pressed',
      'false'
    );
  });
});
