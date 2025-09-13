import { exportWorkoutToXLSX, exportWeekToXLSX, exportProgramToXLSX, exportWorkoutToPDF, exportWeekToPDF, exportProgramToPDF } from './exportUtils';
import type {
  ProgrammedWorkoutWithStages,
  WorkoutStageWithExercises,
  ProgrammedExerciseWithSetSchemes,
  SetScheme,
  Exercise,
  UserWeightUnitPreference,
  ProgramWithWorkouts,
} from '../api/types';

// Mock ExcelJS
jest.mock('exceljs', () => {
  const mockRow = {
    font: {},
    alignment: {},
    fill: {},
  };
  
  const mockWorkbook = {
    addWorksheet: jest.fn(() => ({
      addRow: jest.fn(() => mockRow),
      getRow: jest.fn(() => mockRow),
      getColumn: jest.fn(() => ({
        width: 0,
      })),
      mergeCells: jest.fn(),
      columns: {
        forEach: jest.fn(),
      },
    })),
    xlsx: {
      writeBuffer: jest.fn(() => Promise.resolve(new ArrayBuffer(8))),
    },
  };
  
  return {
    Workbook: jest.fn(() => mockWorkbook),
  };
});

// Mock jsPDF
jest.mock('jspdf', () => {
  return {
    jsPDF: jest.fn().mockImplementation(() => ({
      addImage: jest.fn(),
      addPage: jest.fn(),
      insertPage: jest.fn(),
      setPage: jest.fn(),
      save: jest.fn(),
      setFontSize: jest.fn(),
      setFont: jest.fn(),
      setTextColor: jest.fn(),
      setFillColor: jest.fn(),
      setDrawColor: jest.fn(),
      setLineWidth: jest.fn(),
      text: jest.fn(),
      rect: jest.fn(),
      roundedRect: jest.fn(),
      getTextWidth: jest.fn((text: string) => text.length * 2), // Mock text width calculation
      link: jest.fn(),
      output: jest.fn(() => ({
        blob: jest.fn(() => new Blob(['test'], { type: 'application/pdf' })),
      })),
    })),
  };
});

// Mock jspdf-autotable
jest.mock('jspdf-autotable', () => jest.fn());

describe('exportUtils', () => {
  const mockExerciseData = new Map<string, Exercise>([
    ['Bench Press', { id: 1, name: 'Bench Press', exercise_name: 'Bench Press' } as Exercise],
    ['Squat', { id: 2, name: 'Squat', exercise_name: 'Squat' } as Exercise],
  ]);

  const mockWeightUnitPreferences: UserWeightUnitPreference[] = [
    { user_id: '1', exercise_name: 'Bench Press', preferred_unit: 'LBS', created_at: new Date(), updated_at: new Date() },
    { user_id: '1', exercise_name: 'Squat', preferred_unit: 'KG', created_at: new Date(), updated_at: new Date() },
  ];

  const mockSetScheme: SetScheme = {
    id: 1,
    programmed_exercise_id: 1,
    set_number: 3,
    is_amrap: false,
    is_emom: false,
    use_tempo: false,
    target_weight: 135,
    target_rep_count: 8,
    rest_seconds: 120,
    created_at: new Date(),
    updated_at: new Date(),
  };

  const mockExerciseWithSetSchemes: ProgrammedExerciseWithSetSchemes = {
    exercise: { exercise_name: 'Bench Press' },
    set_schemes: [mockSetScheme],
  };

  const mockStage: WorkoutStageWithExercises = {
    stage: { id: 1, name: 'Main Movement' },
    exercises: [mockExerciseWithSetSchemes],
  };

  const mockWorkoutData: ProgrammedWorkoutWithStages = {
    workout: {
      id: 1,
      name: 'Upper Body Day',
      day_number: 1,
      created_at: '2024-01-01T00:00:00Z',
    },
    stages: [mockStage],
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('exportWorkoutToXLSX', () => {
    it('should export workout data to XLSX format', async () => {
      const options = {
        title: 'Test Workout',
        filename: 'test-workout',
      };

      // Mock DOM methods
      const mockLink = {
        href: '',
        download: '',
        click: jest.fn(),
      };
      const mockURL = {
        createObjectURL: jest.fn(() => 'mock-url'),
        revokeObjectURL: jest.fn(),
      };
      
      Object.defineProperty(window, 'URL', {
        value: mockURL,
        writable: true,
      });
      Object.defineProperty(document, 'createElement', {
        value: jest.fn(() => mockLink),
        writable: true,
      });

      await exportWorkoutToXLSX(mockWorkoutData, mockWeightUnitPreferences, options);

      // Verify ExcelJS methods were called
      const ExcelJS = require('exceljs');
      expect(ExcelJS.Workbook).toHaveBeenCalled();
      expect(mockLink.download).toBe('test-workout.xlsx');
      expect(mockLink.click).toHaveBeenCalled();
    });

    it('should handle workout with multiple stages and exercises', async () => {
      const multiStageWorkout: ProgrammedWorkoutWithStages = {
        ...mockWorkoutData,
        stages: [
          mockStage,
          {
            stage: { id: 2, name: 'Accessory' },
            exercises: [{
              exercise: { exercise_name: 'Squat' },
              set_schemes: [{ ...mockSetScheme, target_weight: 225 }],
            }],
          },
        ],
      };

      const options = {
        title: 'Multi Stage Workout',
        filename: 'multi-stage-workout',
      };

      // Mock DOM methods
      const mockLink = {
        href: '',
        download: '',
        click: jest.fn(),
      };
      const mockURL = {
        createObjectURL: jest.fn(() => 'mock-url'),
        revokeObjectURL: jest.fn(),
      };
      
      Object.defineProperty(window, 'URL', {
        value: mockURL,
        writable: true,
      });
      Object.defineProperty(document, 'createElement', {
        value: jest.fn(() => mockLink),
        writable: true,
      });

      await exportWorkoutToXLSX(multiStageWorkout, mockWeightUnitPreferences, options);

      expect(mockLink.download).toBe('multi-stage-workout.xlsx');
      expect(mockLink.click).toHaveBeenCalled();
    });
  });

  describe('exportWeekToXLSX', () => {
    it('should export week workouts to XLSX format', async () => {
      const weekWorkouts = [mockWorkoutData];
      const options = {
        title: 'Week 1',
        filename: 'week-1-workouts',
      };

      // Mock DOM methods
      const mockLink = {
        href: '',
        download: '',
        click: jest.fn(),
      };
      const mockURL = {
        createObjectURL: jest.fn(() => 'mock-url'),
        revokeObjectURL: jest.fn(),
      };
      
      Object.defineProperty(window, 'URL', {
        value: mockURL,
        writable: true,
      });
      Object.defineProperty(document, 'createElement', {
        value: jest.fn(() => mockLink),
        writable: true,
      });

      await exportWeekToXLSX(weekWorkouts, mockWeightUnitPreferences, options);

      expect(mockLink.download).toBe('week-1-workouts.xlsx');
      expect(mockLink.click).toHaveBeenCalled();
    });

    it('should handle multiple workouts in a week', async () => {
      const weekWorkouts = [
        mockWorkoutData,
        {
          ...mockWorkoutData,
          workout: { ...mockWorkoutData.workout, id: 2, name: 'Lower Body Day' },
        },
      ];
      const options = {
        title: 'Week 1',
        filename: 'week-1-workouts',
      };

      // Mock DOM methods
      const mockLink = {
        href: '',
        download: '',
        click: jest.fn(),
      };
      const mockURL = {
        createObjectURL: jest.fn(() => 'mock-url'),
        revokeObjectURL: jest.fn(),
      };
      
      Object.defineProperty(window, 'URL', {
        value: mockURL,
        writable: true,
      });
      Object.defineProperty(document, 'createElement', {
        value: jest.fn(() => mockLink),
        writable: true,
      });

      await exportWeekToXLSX(weekWorkouts, mockWeightUnitPreferences, options);

      expect(mockLink.download).toBe('week-1-workouts.xlsx');
      expect(mockLink.click).toHaveBeenCalled();
    });
  });

  describe('exportProgramToXLSX', () => {
    it('should export program workouts to XLSX format', async () => {
      const programData: ProgramWithWorkouts = {
        program: {
          id: 1,
          user_id: '1',
          name: 'Test Program',
          is_active: true,
          current_week_number: 1,
          created_at: new Date(),
          updated_at: new Date(),
        },
        workouts: [mockWorkoutData],
      };

      const options = {
        title: 'Test Program',
        filename: 'test-program',
      };

      // Mock DOM methods
      const mockLink = {
        href: '',
        download: '',
        click: jest.fn(),
      };
      const mockURL = {
        createObjectURL: jest.fn(() => 'mock-url'),
        revokeObjectURL: jest.fn(),
      };
      
      Object.defineProperty(window, 'URL', {
        value: mockURL,
        writable: true,
      });
      Object.defineProperty(document, 'createElement', {
        value: jest.fn(() => mockLink),
        writable: true,
      });

      await exportProgramToXLSX(programData, mockWeightUnitPreferences, options);

      expect(mockLink.download).toBe('test-program.xlsx');
      expect(mockLink.click).toHaveBeenCalled();
    });

    it('should group workouts by week correctly', async () => {
      const programData: ProgramWithWorkouts = {
        program: {
          id: 1,
          user_id: '1',
          name: 'Test Program',
          is_active: true,
          current_week_number: 2,
          created_at: new Date(),
          updated_at: new Date(),
        },
        workouts: [
          { ...mockWorkoutData, workout: { ...mockWorkoutData.workout, day_number: 1 } },
          { ...mockWorkoutData, workout: { ...mockWorkoutData.workout, id: 2, day_number: 8 } },
        ],
      };

      const options = {
        title: 'Test Program',
        filename: 'test-program',
      };

      // Mock DOM methods
      const mockLink = {
        href: '',
        download: '',
        click: jest.fn(),
      };
      const mockURL = {
        createObjectURL: jest.fn(() => 'mock-url'),
        revokeObjectURL: jest.fn(),
      };
      
      Object.defineProperty(window, 'URL', {
        value: mockURL,
        writable: true,
      });
      Object.defineProperty(document, 'createElement', {
        value: jest.fn(() => mockLink),
        writable: true,
      });

      await exportProgramToXLSX(programData, mockWeightUnitPreferences, options);

      expect(mockLink.download).toBe('test-program.xlsx');
      expect(mockLink.click).toHaveBeenCalled();
    });
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
          is_active: true,
          current_week_number: 1,
          created_at: new Date(),
          updated_at: new Date(),
        },
        workouts: [mockWorkoutData],
      };

      const options = {
        title: 'Test Program',
        filename: 'test-program',
      };

      await exportProgramToPDF(programData, mockWeightUnitPreferences, options);

      const { jsPDF } = require('jspdf');
      expect(jsPDF).toHaveBeenCalled();
    });
  });
});
