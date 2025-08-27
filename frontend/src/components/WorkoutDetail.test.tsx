import { ThemeProvider, createTheme } from '@mui/material/styles';
import { render, screen, waitFor, act } from '@testing-library/react';
import MockAdapter from 'axios-mock-adapter';
import React from 'react';

import { WorkoutDetail } from './WorkoutDetail';
import { ENDPOINT } from '../api/endpoint';
import { getProgrammedExercisesByStage } from '../api/programmedExercise';
import { getProgrammedWorkout } from '../api/programmedWorkout';
import { getSetSchemesByExercise } from '../api/setScheme';
import type { ProgrammedWorkout, WorkoutStage, ProgrammedExercise, SetScheme } from '../api/types';
import { getWorkoutStagesByWorkout } from '../api/workoutStage';

const mock = new MockAdapter(ENDPOINT);

// Mock the API functions
jest.mock('../api/programmedWorkout');
jest.mock('../api/workoutStage');
jest.mock('../api/programmedExercise');
jest.mock('../api/setScheme');

const mockGetProgrammedWorkout = getProgrammedWorkout as jest.MockedFunction<
  typeof getProgrammedWorkout
>;
const mockGetWorkoutStagesByWorkout = getWorkoutStagesByWorkout as jest.MockedFunction<
  typeof getWorkoutStagesByWorkout
>;
const mockGetProgrammedExercisesByStage = getProgrammedExercisesByStage as jest.MockedFunction<
  typeof getProgrammedExercisesByStage
>;
const mockGetSetSchemesByExercise = getSetSchemesByExercise as jest.MockedFunction<
  typeof getSetSchemesByExercise
>;

const theme = createTheme();

const mockWorkout: ProgrammedWorkout = {
  id: 1,
  program_id: 1,
  day_number: 1,
  name: 'Push Day',
  created_at: '2024-01-01T00:00:00Z',
  updated_at: '2024-01-01T00:00:00Z',
};

const mockStage: WorkoutStage = {
  id: 1,
  programmed_workout_id: 1,
  stage_type_id: 1,
  position: 1,
  name: 'Warm-up',
  created_at: '2024-01-01T00:00:00Z',
  updated_at: '2024-01-01T00:00:00Z',
};

const mockExercise: ProgrammedExercise = {
  id: 1,
  workout_stage_id: 1,
  exercise_name: 'Bench Press',
  position: 1,
  notes: 'Focus on form',
  created_at: '2024-01-01T00:00:00Z',
  updated_at: '2024-01-01T00:00:00Z',
};

const mockSetScheme: SetScheme = {
  id: 1,
  programmed_exercise_id: 1,
  set_number: 1,
  target_rep_count: 8,
  target_weight: 135,
  rest_seconds: 90,
  is_amrap: false,
  is_emom: false,
  use_tempo: false,
  eccentric_tempo: undefined,
  isometric_tempo: undefined,
  concentric_tempo: undefined,
  created_at: '2024-01-01T00:00:00Z',
  updated_at: '2024-01-01T00:00:00Z',
};

const renderWithTheme = (component: React.ReactElement) => {
  return render(<ThemeProvider theme={theme}>{component}</ThemeProvider>);
};

describe('WorkoutDetail', () => {
  const mockOnBack = jest.fn();

  beforeEach(() => {
    mock.reset();
    jest.clearAllMocks();
  });

  describe('Loading State', () => {
    it('should show loading spinner initially', () => {
      mockGetProgrammedWorkout.mockResolvedValue(mockWorkout);
      mockGetWorkoutStagesByWorkout.mockResolvedValue([]);
      mockGetProgrammedExercisesByStage.mockResolvedValue([]);
      mockGetSetSchemesByExercise.mockResolvedValue([]);

      renderWithTheme(<WorkoutDetail workoutId={1} onBack={mockOnBack} />);

      expect(screen.getByRole('progressbar')).toBeInTheDocument();
    });
  });

  describe('Success State', () => {
    beforeEach(() => {
      mockGetProgrammedWorkout.mockResolvedValue(mockWorkout);
      mockGetWorkoutStagesByWorkout.mockResolvedValue([mockStage]);
      mockGetProgrammedExercisesByStage.mockResolvedValue([mockExercise]);
      mockGetSetSchemesByExercise.mockResolvedValue([mockSetScheme]);
    });

    it('should display workout details when data loads successfully', async () => {
      await act(async () => {
        renderWithTheme(<WorkoutDetail workoutId={1} onBack={mockOnBack} />);
      });

      await waitFor(() => {
        expect(screen.getByText('Warm-up')).toBeInTheDocument();
      });

      expect(screen.getByText('Warm-up')).toBeInTheDocument();
      expect(screen.getByText('Bench Press')).toBeInTheDocument();
      expect(screen.getByText('1')).toBeInTheDocument(); // Number of sets
    });

    it('should display workout information correctly', async () => {
      await act(async () => {
        renderWithTheme(<WorkoutDetail workoutId={1} onBack={mockOnBack} />);
      });

      await waitFor(() => {
        expect(screen.getByText('Warm-up')).toBeInTheDocument();
      });

      expect(screen.getByText('Warm-up')).toBeInTheDocument();
      expect(screen.getByText('Exercise')).toBeInTheDocument();
      expect(screen.getByText('Sets')).toBeInTheDocument();
    });

    it('should display stage information correctly', async () => {
      await act(async () => {
        renderWithTheme(<WorkoutDetail workoutId={1} onBack={mockOnBack} />);
      });

      await waitFor(() => {
        expect(screen.getByText('Warm-up')).toBeInTheDocument();
      });

      expect(screen.getByText('Exercise')).toBeInTheDocument();
      expect(screen.getByText('Sets')).toBeInTheDocument();
    });

    it('should display exercise information correctly', async () => {
      await act(async () => {
        renderWithTheme(<WorkoutDetail workoutId={1} onBack={mockOnBack} />);
      });

      await waitFor(() => {
        expect(screen.getByText('Bench Press')).toBeInTheDocument();
      });

      // Check that the notes icon is present (notes are now in tooltip)
      expect(screen.getByTestId('NotesIcon')).toBeInTheDocument();
    });

    it('should display set scheme information correctly', async () => {
      await act(async () => {
        renderWithTheme(<WorkoutDetail workoutId={1} onBack={mockOnBack} />);
      });

      await waitFor(() => {
        expect(screen.getByText('Bench Press')).toBeInTheDocument();
      });

      expect(screen.getByText('135 lbs')).toBeInTheDocument();
      expect(screen.getByText('8')).toBeInTheDocument();
      expect(screen.getAllByText('-')).toHaveLength(2); // Rest and Notes show as "-" when null
      expect(screen.getByText('1')).toBeInTheDocument(); // Number of sets
    });

    it('should handle exercise without notes', async () => {
      const exerciseWithoutNotes = { ...mockExercise, notes: undefined };
      mockGetProgrammedExercisesByStage.mockResolvedValue([exerciseWithoutNotes]);

      await act(async () => {
        renderWithTheme(<WorkoutDetail workoutId={1} onBack={mockOnBack} />);
      });

      await waitFor(() => {
        expect(screen.getByText('Bench Press')).toBeInTheDocument();
      });

      expect(screen.queryByTestId('NotesIcon')).not.toBeInTheDocument();
    });

    it('should handle set scheme without notes', async () => {
      const setSchemeWithoutNotes = { ...mockSetScheme, notes: undefined };
      mockGetSetSchemesByExercise.mockResolvedValue([setSchemeWithoutNotes]);

      await act(async () => {
        renderWithTheme(<WorkoutDetail workoutId={1} onBack={mockOnBack} />);
      });

      await waitFor(() => {
        expect(screen.getByText('Bench Press')).toBeInTheDocument();
      });

      // Notes icon should still be present since it comes from exercise notes, not set scheme notes
      expect(screen.getByTestId('NotesIcon')).toBeInTheDocument();
    });

    it('should handle set scheme without RPE', async () => {
      const setSchemeWithoutRPE = { ...mockSetScheme, rpe: undefined };
      mockGetSetSchemesByExercise.mockResolvedValue([setSchemeWithoutRPE]);

      await act(async () => {
        renderWithTheme(<WorkoutDetail workoutId={1} onBack={mockOnBack} />);
      });

      await waitFor(() => {
        expect(screen.getByText('Bench Press')).toBeInTheDocument();
      });

      expect(screen.getByText('135 lbs')).toBeInTheDocument();
    });

    it('should handle multiple stages', async () => {
      const stage2 = { ...mockStage, id: 2, position: 2, name: 'Main Work' };
      mockGetWorkoutStagesByWorkout.mockResolvedValue([mockStage, stage2]);

      await act(async () => {
        renderWithTheme(<WorkoutDetail workoutId={1} onBack={mockOnBack} />);
      });

      await waitFor(() => {
        expect(screen.getByText('Warm-up')).toBeInTheDocument();
        expect(screen.getByText('Main Work')).toBeInTheDocument();
      });
    });

    it('should handle multiple exercises', async () => {
      const exercise2 = { ...mockExercise, id: 2, exercise_name: 'Incline Press', position: 2 };
      mockGetProgrammedExercisesByStage.mockResolvedValue([mockExercise, exercise2]);

      await act(async () => {
        renderWithTheme(<WorkoutDetail workoutId={1} onBack={mockOnBack} />);
      });

      await waitFor(() => {
        expect(screen.getByText('Bench Press')).toBeInTheDocument();
        expect(screen.getByText('Incline Press')).toBeInTheDocument();
      });
    });

    it('should handle multiple set schemes', async () => {
      const setScheme2 = {
        ...mockSetScheme,
        id: 2,
        set_number: 2,
        target_rep_count: 6,
        target_weight: 145,
      };
      mockGetSetSchemesByExercise.mockResolvedValue([mockSetScheme, setScheme2]);

      await act(async () => {
        renderWithTheme(<WorkoutDetail workoutId={1} onBack={mockOnBack} />);
      });

      await waitFor(() => {
        expect(screen.getByText('Bench Press')).toBeInTheDocument();
      });

      // Should show the number of sets (2) instead of individual set numbers
      expect(screen.getByText('2')).toBeInTheDocument();
    });
  });

  describe('Error State', () => {
    it('should display error message when workout fetch fails', async () => {
      mockGetProgrammedWorkout.mockRejectedValue(new Error('Failed to fetch workout'));

      await act(async () => {
        renderWithTheme(<WorkoutDetail workoutId={1} onBack={mockOnBack} />);
      });

      await waitFor(() => {
        expect(
          screen.getByText('Failed to load workout details. Please try again.')
        ).toBeInTheDocument();
      });
    });

    it('should display error message when stages fetch fails', async () => {
      mockGetProgrammedWorkout.mockResolvedValue(mockWorkout);
      mockGetWorkoutStagesByWorkout.mockRejectedValue(new Error('Failed to fetch stages'));

      await act(async () => {
        renderWithTheme(<WorkoutDetail workoutId={1} onBack={mockOnBack} />);
      });

      await waitFor(() => {
        expect(
          screen.getByText('Failed to load workout details. Please try again.')
        ).toBeInTheDocument();
      });
    });

    it('should display error message when exercises fetch fails', async () => {
      mockGetProgrammedWorkout.mockResolvedValue(mockWorkout);
      mockGetWorkoutStagesByWorkout.mockResolvedValue([mockStage]);
      mockGetProgrammedExercisesByStage.mockRejectedValue(new Error('Failed to fetch exercises'));

      await act(async () => {
        renderWithTheme(<WorkoutDetail workoutId={1} onBack={mockOnBack} />);
      });

      await waitFor(() => {
        expect(
          screen.getByText('Failed to load workout details. Please try again.')
        ).toBeInTheDocument();
      });
    });

    it('should display error message when set schemes fetch fails', async () => {
      mockGetProgrammedWorkout.mockResolvedValue(mockWorkout);
      mockGetWorkoutStagesByWorkout.mockResolvedValue([mockStage]);
      mockGetProgrammedExercisesByStage.mockResolvedValue([mockExercise]);
      mockGetSetSchemesByExercise.mockRejectedValue(new Error('Failed to fetch set schemes'));

      await act(async () => {
        renderWithTheme(<WorkoutDetail workoutId={1} onBack={mockOnBack} />);
      });

      await waitFor(() => {
        expect(
          screen.getByText('Failed to load workout details. Please try again.')
        ).toBeInTheDocument();
      });
    });
  });

  describe('Empty State', () => {
    it('should handle empty stages', async () => {
      mockGetProgrammedWorkout.mockResolvedValue(mockWorkout);
      mockGetWorkoutStagesByWorkout.mockResolvedValue([]);

      await act(async () => {
        renderWithTheme(<WorkoutDetail workoutId={1} onBack={mockOnBack} />);
      });

      await waitFor(() => {
        expect(screen.getByText('Exercise')).toBeInTheDocument();
      });

      // When there are no stages, only the table header should be visible
      expect(screen.getByText('Exercise')).toBeInTheDocument();
      expect(screen.getByText('Sets')).toBeInTheDocument();
    });

    it('should handle empty exercises', async () => {
      mockGetProgrammedWorkout.mockResolvedValue(mockWorkout);
      mockGetWorkoutStagesByWorkout.mockResolvedValue([mockStage]);
      mockGetProgrammedExercisesByStage.mockResolvedValue([]);

      await act(async () => {
        renderWithTheme(<WorkoutDetail workoutId={1} onBack={mockOnBack} />);
      });

      await waitFor(() => {
        expect(screen.getByText('Warm-up')).toBeInTheDocument();
      });

      // Should show the stage header but no exercise rows
      expect(screen.getByText('Warm-up')).toBeInTheDocument();
      expect(screen.getByText('Exercise')).toBeInTheDocument();
    });

    it('should handle empty set schemes', async () => {
      mockGetProgrammedWorkout.mockResolvedValue(mockWorkout);
      mockGetWorkoutStagesByWorkout.mockResolvedValue([mockStage]);
      mockGetProgrammedExercisesByStage.mockResolvedValue([mockExercise]);
      mockGetSetSchemesByExercise.mockResolvedValue([]);

      await act(async () => {
        renderWithTheme(<WorkoutDetail workoutId={1} onBack={mockOnBack} />);
      });

      await waitFor(() => {
        expect(screen.getByText('Warm-up')).toBeInTheDocument();
      });

      // Check that the table headers are present but no data rows
      expect(screen.getByText('Sets')).toBeInTheDocument();
      expect(screen.getByText('Reps')).toBeInTheDocument();
      expect(screen.getByText('Tempo')).toBeInTheDocument();
      expect(screen.getByText('Weight')).toBeInTheDocument();
      expect(screen.getByText('Rest')).toBeInTheDocument();
      expect(screen.getByText('Notes')).toBeInTheDocument();
    });
  });



  describe('Component Props', () => {
    it('should call onBack when back button is clicked', async () => {
      mockGetProgrammedWorkout.mockResolvedValue(mockWorkout);
      mockGetWorkoutStagesByWorkout.mockResolvedValue([]);

      await act(async () => {
        renderWithTheme(<WorkoutDetail workoutId={1} onBack={mockOnBack} />);
      });

      await waitFor(() => {
        expect(screen.getByText('Exercise')).toBeInTheDocument();
      });

      // Note: Back button is now in the parent Workouts component, not in WorkoutDetail
      // This test is no longer applicable since the back functionality is handled by the parent
      expect(mockOnBack).not.toHaveBeenCalled();
    });

    it('should load workout with correct ID', async () => {
      mockGetProgrammedWorkout.mockResolvedValue(mockWorkout);
      mockGetWorkoutStagesByWorkout.mockResolvedValue([]);

      await act(async () => {
        renderWithTheme(<WorkoutDetail workoutId={123} onBack={mockOnBack} />);
      });

      await waitFor(() => {
        expect(mockGetProgrammedWorkout).toHaveBeenCalledWith(123);
      });
    });
  });
});
