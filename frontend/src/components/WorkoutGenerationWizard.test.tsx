import { render, screen, fireEvent, waitFor, act } from '@testing-library/react';
import MockAdapter from 'axios-mock-adapter';
import React from 'react';
import { MemoryRouter } from 'react-router';

import { WorkoutGenerationWizard } from './WorkoutGenerationWizard';
import { ENDPOINT } from '../api/endpoint';
import type { Program } from '../api/types';

// Mock DataContext
const mockUseData = jest.fn();
jest.mock('../contexts/DataContext', () => ({
  useData: () => mockUseData(),
  DataProvider: ({ children }: { children: React.ReactNode }) => children,
}));

const mock = new MockAdapter(ENDPOINT);

const mockProgram: Program = {
  id: 1,
  user_id: 'test-user-id',
  name: 'Test Program',
  current_week_number: 2,
  created_at: new Date('2024-01-01T00:00:00.000Z'),
  updated_at: new Date('2024-01-01T00:00:00.000Z'),
  is_active: true,
};

const mockUserExercisePool = {
  user_id: 'test-user-id',
  total_exercises: 10,
  available_exercises: 8,
  primary_exercises: [
    {
      id: 1,
      exercise_name: 'Bench Press',
      category: 'strength',
      primary_muscle: 'chest',
      secondary_muscles: ['triceps', 'shoulders'],
      instructions: 'Test instructions',
      equipment: 'barbell',
      difficulty: 'intermediate',
    },
  ],
  accessory_exercises: [],
  user_equipment: [],
  user_preferences: [],
  previously_used_exercises: [],
};

const renderWithProviders = (component: React.ReactElement) => {
  return render(<MemoryRouter>{component}</MemoryRouter>);
};

describe('WorkoutGenerationWizard', () => {
  beforeEach(() => {
    mock.reset();
    
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
      isLoading: false,
      error: null,
      refreshData: jest.fn(),
      isDataStale: false,
      getExercise: jest.fn(),
      getExerciseEquipmentData: jest.fn(),
      getMuscle: jest.fn(),
      getEquipment: jest.fn(),
      getProgram: jest.fn(),
      generateWorkout: jest.fn().mockResolvedValue(mockProgram),
      updateWorkoutWithOneRepMax: jest.fn().mockResolvedValue(mockProgram),
      loadUserExercisePool: jest.fn().mockResolvedValue(mockUserExercisePool),
    };
    
    mockUseData.mockReturnValue(defaultMockDataContext);
  });

  afterAll(() => {
    mock.restore();
  });

  it('renders the wizard when open', () => {
    renderWithProviders(
      <WorkoutGenerationWizard
        open={true}
        onClose={jest.fn()}
        onComplete={jest.fn()}
        program={mockProgram}
      />
    );

    expect(screen.getByRole('heading')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Generate Workouts' })).toBeInTheDocument();
  });

  it('does not render when closed', () => {
    renderWithProviders(
      <WorkoutGenerationWizard
        open={false}
        onClose={jest.fn()}
        onComplete={jest.fn()}
        program={mockProgram}
      />
    );

    expect(screen.queryByText('Generate Workouts')).not.toBeInTheDocument();
  });

  it('shows workout generation step initially', () => {
    renderWithProviders(
      <WorkoutGenerationWizard
        open={true}
        onClose={jest.fn()}
        onComplete={jest.fn()}
        program={mockProgram}
      />
    );

    expect(screen.getByRole('button', { name: 'Generate Workouts' })).toBeInTheDocument();
    expect(
      screen.getByText(
        `The next week's workouts will be generated for ${mockProgram.name}. This will create a new week of workouts based on your program preferences and current progress.`
      )
    ).toBeInTheDocument();
  });

  it('calls onClose when cancel button is clicked', () => {
    const onClose = jest.fn();
    renderWithProviders(
      <WorkoutGenerationWizard
        open={true}
        onClose={onClose}
        onComplete={jest.fn()}
        program={mockProgram}
      />
    );

    const cancelButton = screen.getByText('Cancel');
    fireEvent.click(cancelButton);

    expect(onClose).toHaveBeenCalled();
  });

  it('generates workouts when generate button is clicked', async () => {
    const onComplete = jest.fn();
    const updatedProgram = { ...mockProgram, current_week_number: 3 };

    // Mock the DataContext functions to resolve immediately
    const mockGenerateWorkout = jest.fn().mockResolvedValue(updatedProgram);
    const mockLoadUserExercisePool = jest.fn().mockImplementation(() => {
      // Return a resolved promise immediately to avoid async timing issues
      return Promise.resolve(mockUserExercisePool);
    });
    
    const testMockDataContext = {
      userData: null,
      exerciseMuscleData: new Map(),
      weightUnitPreferences: [],
      exerciseData: new Map(),
      exerciseEquipmentData: new Map(),
      muscleData: new Map(),
      equipmentData: new Map(),
      programData: new Map(),
      isLoading: false,
      error: null,
      refreshData: jest.fn(),
      isDataStale: false,
      getExercise: jest.fn(),
      getExerciseEquipmentData: jest.fn(),
      getMuscle: jest.fn(),
      getEquipment: jest.fn(),
      getProgram: jest.fn(),
      generateWorkout: mockGenerateWorkout,
      updateWorkoutWithOneRepMax: jest.fn().mockResolvedValue(mockProgram),
      loadUserExercisePool: mockLoadUserExercisePool,
    };
    
    mockUseData.mockReturnValue(testMockDataContext);

    await act(async () => {
      renderWithProviders(
        <WorkoutGenerationWizard
          open={true}
          onClose={jest.fn()}
          onComplete={onComplete}
          program={mockProgram}
        />
      );
    });

    // Wait for the exercise pool to be loaded
    await act(async () => {
      await waitFor(() => {
        expect(mockLoadUserExercisePool).toHaveBeenCalled();
      });
    });

    // Wait for the component to be ready
    await act(async () => {
      await waitFor(() => {
        expect(screen.getByRole('button', { name: 'Generate Workouts' })).toBeInTheDocument();
      });
    });

    const generateButton = screen.getByRole('button', { name: 'Generate Workouts' });
    
    await act(async () => {
      fireEvent.click(generateButton);
    });

    // Check that the DataContext function was called
    await waitFor(() => {
      expect(mockGenerateWorkout).toHaveBeenCalledWith(1);
    });
  }, 10000);

  it('handles generation errors gracefully', async () => {
    const onClose = jest.fn();
    const errorMockDataContext = {
      userData: null,
      exerciseMuscleData: new Map(),
      weightUnitPreferences: [],
      exerciseData: new Map(),
      exerciseEquipmentData: new Map(),
      muscleData: new Map(),
      equipmentData: new Map(),
      programData: new Map(),
      isLoading: false,
      error: null,
      refreshData: jest.fn(),
      isDataStale: false,
      getExercise: jest.fn(),
      getExerciseEquipmentData: jest.fn(),
      getMuscle: jest.fn(),
      getEquipment: jest.fn(),
      getProgram: jest.fn(),
      generateWorkout: jest.fn().mockRejectedValue(new Error('Generation failed')),
      updateWorkoutWithOneRepMax: jest.fn().mockResolvedValue(mockProgram),
      loadUserExercisePool: jest.fn().mockImplementation(() => {
        return Promise.resolve(mockUserExercisePool);
      }),
    };

    mockUseData.mockReturnValue(errorMockDataContext);

    await act(async () => {
      renderWithProviders(
        <WorkoutGenerationWizard
          open={true}
          onClose={onClose}
          onComplete={jest.fn()}
          program={mockProgram}
        />
      );
    });

    // Wait for the exercise pool to be loaded
    await act(async () => {
      await waitFor(() => {
        expect(errorMockDataContext.loadUserExercisePool).toHaveBeenCalled();
      });
    });

    // Wait for the component to be ready
    await act(async () => {
      await waitFor(() => {
        expect(screen.getByRole('button', { name: 'Generate Workouts' })).toBeInTheDocument();
      });
    });

    const generateButton = screen.getByRole('button', { name: 'Generate Workouts' });
    
    await act(async () => {
      fireEvent.click(generateButton);
    });

    // Check that the DataContext function was called and failed
    await waitFor(() => {
      expect(errorMockDataContext.generateWorkout).toHaveBeenCalledWith(1);
    });
  });
});
