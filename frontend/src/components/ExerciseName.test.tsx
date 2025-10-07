import { render, screen, act, waitFor } from '@testing-library/react';
import AxiosMockAdapter from 'axios-mock-adapter';
import React from 'react';
import { MemoryRouter } from 'react-router';

import { ExerciseName } from './ExerciseName';
import { ENDPOINT } from '../api/endpoint';
import type { Exercise, ExerciseMuscle, Muscle } from '../api/types';

// Mock DataContext
const mockUseData = jest.fn();
jest.mock('../contexts/DataContext', () => ({
  useData: () => mockUseData(),
  DataProvider: ({ children }: { children: React.ReactNode }) => children,
}));

describe('ExerciseName', () => {
  let mockAdapter: AxiosMockAdapter;

  beforeEach(() => {
    mockAdapter = new AxiosMockAdapter(ENDPOINT);

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
      getExercise: jest.fn(),
      getExerciseMuscles: jest.fn(),
      getExerciseEquipmentData: jest.fn(),
      getMuscle: jest.fn(),
      getEquipment: jest.fn(),
      getProgram: jest.fn(),
      loadAllExercises: jest.fn(),
      loadAllMuscles: jest.fn(),
      loadAllEquipment: jest.fn(),
    };

    mockUseData.mockReturnValue(defaultMockDataContext);
  });

  afterEach(() => {
    mockAdapter.restore();
    jest.clearAllMocks();
  });

  const mockExercise: Exercise = {
    name: 'Bench Press',
    description: 'A compound upper body exercise',
    movement_type: 'strength',
    is_unilateral: false,
    is_upper: true,
    is_accessory: false,
  };

  const mockExerciseMuscles: ExerciseMuscle[] = [
    {
      exercise_name: 'Bench Press',
      muscle_name: 'Pectoralis Major',
    },
    {
      exercise_name: 'Bench Press',
      muscle_name: 'Triceps Brachii',
    },
  ];

  const mockMuscles: Muscle[] = [
    {
      name: 'Pectoralis Major',
      description: 'Chest muscle',
    },
    {
      name: 'Triceps Brachii',
      description: 'Arm muscle',
    },
  ];

  it('should render exercise name with tooltip when data is loaded', async () => {
    // Set up mock data context with exercise and muscle data
    const mockDataContext = {
      ...mockUseData(),
      getExercise: jest.fn().mockResolvedValue(mockExercise),
      getMuscle: jest.fn().mockResolvedValue(mockMuscles[0]),
      getExerciseMuscles: jest.fn().mockResolvedValue(mockExerciseMuscles),
    };
    mockUseData.mockReturnValue(mockDataContext);

    await act(async () => {
      render(
        <MemoryRouter>
          <ExerciseName exerciseName="Bench Press" />
        </MemoryRouter>
      );
    });

    expect(screen.getByText('Bench Press')).toBeInTheDocument();
  });

  it('should show loading tooltip when data is loading', async () => {
    // Set up mock data context with loading state
    const mockDataContext = {
      ...mockUseData(),
      getExercise: jest.fn().mockImplementation(() => new Promise(() => {})), // Never resolves
      getMuscle: jest.fn().mockImplementation(() => new Promise(() => {})), // Never resolves
      getExerciseMuscles: jest.fn().mockImplementation(() => new Promise(() => {})), // Never resolves
    };
    mockUseData.mockReturnValue(mockDataContext);
    await act(async () => {
      render(
        <MemoryRouter>
          <ExerciseName exerciseName="Bench Press" />
        </MemoryRouter>
      );
    });

    expect(screen.getByText('Bench Press')).toBeInTheDocument();
  });

  it('should show error tooltip when data fails to load', async () => {
    // Set up mock data context with error state
    const mockDataContext = {
      ...mockUseData(),
      getExercise: jest.fn().mockRejectedValue(new Error('Failed to load')),
      getMuscle: jest.fn().mockRejectedValue(new Error('Failed to load')),
      getExerciseMuscles: jest.fn().mockRejectedValue(new Error('Failed to load')),
    };
    mockUseData.mockReturnValue(mockDataContext);

    await act(async () => {
      render(
        <MemoryRouter>
          <ExerciseName exerciseName="Bench Press" />
        </MemoryRouter>
      );
    });

    // Wait for the async operations to complete and error state to be set
    await waitFor(() => {
      expect(screen.getByText('Bench Press')).toBeInTheDocument();
    });
  });

  it('should render with custom variant and sx props', async () => {
    // Set up mock data context with exercise and muscle data
    const mockDataContext = {
      ...mockUseData(),
      getExercise: jest.fn().mockResolvedValue(mockExercise),
      getMuscle: jest.fn().mockResolvedValue(mockMuscles[0]),
      getExerciseMuscles: jest.fn().mockResolvedValue(mockExerciseMuscles),
    };
    mockUseData.mockReturnValue(mockDataContext);

    await act(async () => {
      render(
        <MemoryRouter>
          <ExerciseName exerciseName="Bench Press" variant="h6" sx={{ color: 'red' }} />
        </MemoryRouter>
      );
    });

    await waitFor(() => {
      expect(screen.getByText('Bench Press')).toBeInTheDocument();
    });
  });

  it('should render custom children when provided', async () => {
    // Set up mock data context with exercise and muscle data
    const mockDataContext = {
      ...mockUseData(),
      getExercise: jest.fn().mockResolvedValue(mockExercise),
      getMuscle: jest.fn().mockResolvedValue(mockMuscles[0]),
      getExerciseMuscles: jest.fn().mockResolvedValue(mockExerciseMuscles),
    };
    mockUseData.mockReturnValue(mockDataContext);

    await act(async () => {
      render(
        <MemoryRouter>
          <ExerciseName exerciseName="Bench Press">Custom Exercise Name</ExerciseName>
        </MemoryRouter>
      );
    });

    await waitFor(() => {
      expect(screen.getByText('Custom Exercise Name')).toBeInTheDocument();
    });
  });

  it('should handle exercise without muscles', async () => {
    // Set up mock data context with exercise but no muscles
    const mockDataContext = {
      ...mockUseData(),
      getExercise: jest.fn().mockResolvedValue(mockExercise),
      getMuscle: jest.fn().mockResolvedValue(null),
      getExerciseMuscles: jest.fn().mockResolvedValue([]), // No exercise muscles
    };
    mockUseData.mockReturnValue(mockDataContext);

    await act(async () => {
      render(
        <MemoryRouter>
          <ExerciseName exerciseName="Bench Press" />
        </MemoryRouter>
      );
    });

    await waitFor(() => {
      expect(screen.getByText('Bench Press')).toBeInTheDocument();
    });
  });

  it('should create proper link to exercise details page', async () => {
    // Set up mock data context with exercise and muscle data
    const mockDataContext = {
      ...mockUseData(),
      getExercise: jest.fn().mockResolvedValue(mockExercise),
      getMuscle: jest.fn().mockResolvedValue(mockMuscles[0]),
      getExerciseMuscles: jest.fn().mockResolvedValue(mockExerciseMuscles),
    };
    mockUseData.mockReturnValue(mockDataContext);

    await act(async () => {
      render(
        <MemoryRouter>
          <ExerciseName exerciseName="Bench Press" />
        </MemoryRouter>
      );
    });

    await waitFor(() => {
      const link = screen.getByRole('link');
      expect(link).toHaveAttribute('href', '/exercises/Bench%20Press');
    });
  });

  it('should handle exercise names with special characters in URL', async () => {
    // Set up mock data context with exercise and muscle data
    const mockDataContext = {
      ...mockUseData(),
      getExercise: jest.fn().mockResolvedValue(mockExercise),
      getMuscle: jest.fn().mockResolvedValue(mockMuscles[0]),
      getExerciseMuscles: jest.fn().mockResolvedValue(mockExerciseMuscles),
    };
    mockUseData.mockReturnValue(mockDataContext);

    await act(async () => {
      render(
        <MemoryRouter>
          <ExerciseName exerciseName="Bench Press (Barbell)" />
        </MemoryRouter>
      );
    });

    await waitFor(() => {
      const link = screen.getByRole('link');
      expect(link).toHaveAttribute('href', '/exercises/Bench%20Press%20%28Barbell%29');
    }, { timeout: 15000 });
  }, 20000);
});
