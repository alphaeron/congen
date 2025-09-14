import { exportWorkoutToPDF, exportWeekToPDF, exportProgramToPDF } from './exportUtils';
import type {
  ProgrammedWorkoutWithStages,
  WorkoutStageWithExercises,
  ProgrammedExerciseWithSetSchemes,
  SetScheme,
  Exercise,
  UserWeightUnitPreference,
  ProgramWithWorkouts,
} from '../api/types';

// Mock jsPDF
jest.mock('jspdf', () => {
  return {
    jsPDF: jest.fn().mockImplementation(() => ({
      addImage: jest.fn(),
      addPage: jest.fn(),
      insertPage: jest.fn(),
      setPage: jest.fn(),
      save: jest.fn(),
      text: jest.fn(),
      setFontSize: jest.fn(),
      setFont: jest.fn(),
      setTextColor: jest.fn(),
      getTextWidth: jest.fn((text: string) => text.length * 2),
      link: jest.fn(),
      setFillColor: jest.fn(),
      setDrawColor: jest.fn(),
      setLineWidth: jest.fn(),
      rect: jest.fn(),
      roundedRect: jest.fn(),
      internal: {
        getCurrentPageInfo: jest.fn(() => ({ pageNumber: 1 })),
      },
    })),
  };
});

// Mock jspdf-autotable
jest.mock('jspdf-autotable', () => {
  return jest.fn();
});

describe('exportUtils', () => {
  const mockExerciseData = new Map<string, Exercise>([
    ['Bench Press', { id: 1, name: 'Bench Press', exercise_name: 'Bench Press' } as Exercise],
    ['Squat', { id: 2, name: 'Squat', exercise_name: 'Squat' } as Exercise],
  ]);

  const mockSetScheme: SetScheme = {
    id: 1,
    programmed_exercise_id: 1,
    set_number: 1,
    target_reps: 5,
    target_weight: 225,
    rest_seconds: 180,
    notes: 'Test notes',
  };

  const mockExercise: ProgrammedExerciseWithSetSchemes = {
    exercise: { exercise_name: 'Bench Press' },
    set_schemes: [mockSetScheme],
  };

  const mockStage: WorkoutStageWithExercises = {
    stage: { id: 1, name: 'Main Movement' },
    exercises: [mockExercise],
  };

  const mockWorkoutData: ProgrammedWorkoutWithStages = {
    workout: {
      id: 1,
      program_id: 1,
      day_number: 1,
      name: 'Day 1',
      created_at: new Date('2023-01-01'),
      updated_at: new Date('2023-01-01'),
    },
    stages: [mockStage],
  };

  const mockWeightUnitPreferences: UserWeightUnitPreference[] = [
    {
      id: 1,
      user_id: '1',
      weight_unit: 'lbs',
      created_at: new Date('2023-01-01'),
      updated_at: new Date('2023-01-01'),
    },
  ];

  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('exportWorkoutToPDF', () => {
    it('should export workout data to PDF format', async () => {
      const options = {
        title: 'Test Workout',
        filename: 'test-workout',
      };

      await exportWorkoutToPDF(mockWorkoutData, mockWeightUnitPreferences, options);

      // Verify jsPDF methods were called
      const { jsPDF } = require('jspdf');
      expect(jsPDF).toHaveBeenCalled();
    });
  });

  describe('exportWeekToPDF', () => {
    it('should export week workouts to PDF format', async () => {
      const weekWorkouts = [mockWorkoutData];
      const options = {
        title: 'Week 1',
        filename: 'week-1-workouts',
      };

      await exportWeekToPDF(weekWorkouts, mockWeightUnitPreferences, options);

      // Verify jsPDF methods were called
      const { jsPDF } = require('jspdf');
      expect(jsPDF).toHaveBeenCalled();
    });
  });

  describe('exportProgramToPDF', () => {
    it('should export program workouts to PDF format', async () => {
      const programData: ProgramWithWorkouts = {
        program: {
          id: 1,
          user_id: '1',
          name: 'Test Program',
          created_at: new Date('2023-01-01'),
          updated_at: new Date('2023-01-01'),
          current_week_number: 1,
          is_active: true,
        },
        workouts: [mockWorkoutData],
      };
      const options = {
        title: 'Test Program',
        filename: 'test-program',
      };

      await exportProgramToPDF(programData, mockWeightUnitPreferences, options);

      // Verify jsPDF methods were called
      const { jsPDF } = require('jspdf');
      expect(jsPDF).toHaveBeenCalled();
    });
  });
});