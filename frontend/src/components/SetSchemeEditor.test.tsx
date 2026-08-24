import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import React from 'react';

import { SetSchemeEditor } from './SetSchemeEditor';
import { createSetScheme, deleteSetScheme, updateSetScheme } from '../api/setScheme';
import type { ProgrammedExerciseWithSetSchemes } from '../api/types';

jest.mock('../api/programmedExercise', () => ({
  deleteProgrammedExercise: jest.fn(),
  updateProgrammedExercise: jest.fn(),
}));

jest.mock('../api/setScheme', () => ({
  createSetScheme: jest.fn(),
  deleteSetScheme: jest.fn(),
  updateSetScheme: jest.fn(),
}));

const mockExercise: ProgrammedExerciseWithSetSchemes = {
  exercise: {
    id: 1,
    workout_stage_id: 1,
    exercise_name: 'Bench Press',
    position: 1,
    notes: 'Test notes',
    created_at: new Date('2024-01-01'),
    updated_at: new Date('2024-01-01'),
  },
  set_schemes: [
    {
      id: 1,
      programmed_exercise_id: 1,
      set_number: 1,
      target_rep_count: 8,
      target_weight: 135,
      performed_rep_count: 8,
      performed_weight: 135,
      rest_seconds: 90,
      use_tempo: false,
      eccentric_tempo: undefined,
      isometric_tempo: undefined,
      concentric_tempo: undefined,
      is_amrap: false,
      is_emom: false,
      created_at: new Date('2024-01-01'),
      updated_at: new Date('2024-01-01'),
    },
    {
      id: 2,
      programmed_exercise_id: 1,
      set_number: 2,
      target_rep_count: 6,
      target_weight: 145,
      performed_rep_count: 6,
      performed_weight: 145,
      rest_seconds: 90,
      use_tempo: false,
      eccentric_tempo: undefined,
      isometric_tempo: undefined,
      concentric_tempo: undefined,
      is_amrap: false,
      is_emom: false,
      created_at: new Date('2024-01-01'),
      updated_at: new Date('2024-01-01'),
    },
  ],
};

describe('SetSchemeEditor', () => {
  const mockOnExerciseUpdate = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
    (updateSetScheme as jest.Mock).mockImplementation(async (_id, ...args) => ({
      id: _id,
      programmed_exercise_id: args[0],
      set_number: args[1],
      target_rep_count: args[10],
      target_weight: args[8],
      performed_rep_count: args[11],
      performed_weight: args[9],
      rest_seconds: args[12],
      use_tempo: args[4],
      is_amrap: args[2],
      is_emom: args[3],
      created_at: new Date('2024-01-01'),
      updated_at: new Date('2024-01-01'),
    }));
    (createSetScheme as jest.Mock).mockImplementation(async (...args) => ({
      id: 99,
      programmed_exercise_id: args[0],
      set_number: args[1],
      target_rep_count: args[10],
      target_weight: args[8],
      performed_rep_count: args[11],
      performed_weight: args[9],
      rest_seconds: args[12],
      use_tempo: args[4],
      is_amrap: args[2],
      is_emom: args[3],
      created_at: new Date('2024-01-01'),
      updated_at: new Date('2024-01-01'),
    }));
    (deleteSetScheme as jest.Mock).mockResolvedValue(undefined);
  });

  it('renders edit button when isMostRecentWeek is true', () => {
    render(
      <SetSchemeEditor
        exercise={mockExercise}
        onExerciseUpdate={mockOnExerciseUpdate}
        isMostRecentWeek={true}
      />
    );

    expect(screen.getByLabelText(/edit exercise/i)).toBeInTheDocument();
  });

  it('disables edit button when isMostRecentWeek is false', () => {
    render(
      <SetSchemeEditor
        exercise={mockExercise}
        onExerciseUpdate={mockOnExerciseUpdate}
        isMostRecentWeek={false}
      />
    );

    const editButton = screen.getByRole('button', {
      name: /editing only available for the current week/i,
    });
    expect(editButton).toBeDisabled();
  });

  it('opens editor dialog when edit button is clicked', async () => {
    const user = userEvent.setup();
    render(
      <SetSchemeEditor
        exercise={mockExercise}
        onExerciseUpdate={mockOnExerciseUpdate}
        isMostRecentWeek={true}
      />
    );

    await user.click(screen.getByLabelText(/edit exercise/i));

    expect(screen.getByText('Bench Press')).toBeInTheDocument();
    expect(screen.getByText('Set Scheme Details')).toBeInTheDocument();
    expect(screen.getByLabelText('Sets')).toBeInTheDocument();
  });

  it('populates form with existing exercise data and per-set mode when values vary', async () => {
    const user = userEvent.setup();
    render(
      <SetSchemeEditor
        exercise={mockExercise}
        onExerciseUpdate={mockOnExerciseUpdate}
        isMostRecentWeek={true}
      />
    );

    await user.click(screen.getByLabelText(/edit exercise/i));

    expect(screen.getByLabelText('Sets')).toHaveValue('2');
    expect(screen.getByLabelText('Target reps')).toHaveValue('8');
    expect(screen.getByLabelText('Target weight')).toHaveValue('135');
    expect(screen.getByLabelText('Set 1 reps')).toBeInTheDocument();
    expect(screen.getByLabelText('Set 2 reps')).toBeInTheDocument();
  });

  it('closes editor dialog when cancel button is clicked', async () => {
    const user = userEvent.setup();
    render(
      <SetSchemeEditor
        exercise={mockExercise}
        onExerciseUpdate={mockOnExerciseUpdate}
        isMostRecentWeek={true}
      />
    );

    await user.click(screen.getByLabelText(/edit exercise/i));
    expect(screen.getByRole('dialog')).toBeInTheDocument();

    await user.click(screen.getByText(/cancel/i));

    await waitFor(() => {
      expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
    });
  });

  it('submits form with valid data', async () => {
    const user = userEvent.setup();

    render(
      <SetSchemeEditor
        exercise={mockExercise}
        onExerciseUpdate={mockOnExerciseUpdate}
        isMostRecentWeek={true}
      />
    );

    await user.click(screen.getByLabelText(/edit exercise/i));
    await user.click(screen.getByText(/submit/i));

    await waitFor(
      () => {
        expect(updateSetScheme).toHaveBeenCalledTimes(2);
      },
      { timeout: 10000 }
    );
  }, 15000);

  it('handles form submission errors gracefully', async () => {
    const user = userEvent.setup();

    (updateSetScheme as jest.Mock).mockRejectedValue(new Error('API Error'));

    render(
      <SetSchemeEditor
        exercise={mockExercise}
        onExerciseUpdate={mockOnExerciseUpdate}
        isMostRecentWeek={true}
      />
    );

    await user.click(screen.getByLabelText(/edit exercise/i));
    await user.click(screen.getByText(/submit/i));

    await waitFor(
      () => {
        expect(updateSetScheme).toHaveBeenCalled();
      },
      { timeout: 10000 }
    );
  }, 15000);

  it('creates additional set schemes when total sets increases', async () => {
    const user = userEvent.setup();

    render(
      <SetSchemeEditor
        exercise={mockExercise}
        onExerciseUpdate={mockOnExerciseUpdate}
        isMostRecentWeek={true}
      />
    );

    await user.click(screen.getByLabelText(/edit exercise/i));
    await user.click(screen.getByLabelText('Increase Sets'));
    await user.click(screen.getByText(/submit/i));

    await waitFor(
      () => {
        expect(updateSetScheme).toHaveBeenCalledTimes(2);
        expect(createSetScheme).toHaveBeenCalledTimes(1);
      },
      { timeout: 10000 }
    );
  }, 15000);

  it('passes band weight when updating DE exercise tracking', async () => {
    const user = userEvent.setup();
    const deExercise: ProgrammedExerciseWithSetSchemes = {
      ...mockExercise,
      set_schemes: mockExercise.set_schemes.map(scheme => ({
        ...scheme,
        band_weight_lbs: 60,
      })),
    };

    render(
      <SetSchemeEditor
        exercise={deExercise}
        onExerciseUpdate={mockOnExerciseUpdate}
        isMostRecentWeek={true}
      />
    );

    await user.click(screen.getByLabelText(/edit exercise/i));
    await user.click(screen.getByText(/submit/i));

    await waitFor(
      () => {
        expect(updateSetScheme).toHaveBeenCalled();
        expect((updateSetScheme as jest.Mock).mock.calls[0][15]).toBe(60);
        expect((updateSetScheme as jest.Mock).mock.calls[1][15]).toBe(60);
      },
      { timeout: 10000 }
    );
  }, 15000);

  it('submits exercises without band weight', async () => {
    const user = userEvent.setup();
    const exerciseWithoutBand: ProgrammedExerciseWithSetSchemes = {
      ...mockExercise,
      set_schemes: mockExercise.set_schemes.map(scheme => ({
        ...scheme,
        band_weight_lbs: null,
      })),
    };

    render(
      <SetSchemeEditor
        exercise={exerciseWithoutBand}
        onExerciseUpdate={mockOnExerciseUpdate}
        isMostRecentWeek={true}
      />
    );

    await user.click(screen.getByLabelText(/edit exercise/i));
    await user.click(screen.getByText(/submit/i));

    await waitFor(
      () => {
        expect(updateSetScheme).toHaveBeenCalledTimes(2);
        expect((updateSetScheme as jest.Mock).mock.calls[0][15]).toBeNull();
      },
      { timeout: 10000 }
    );
    expect(mockOnExerciseUpdate).toHaveBeenCalled();
  }, 15000);

  it('handles exercises with no set schemes', async () => {
    const user = userEvent.setup();
    const exerciseWithoutSets = {
      ...mockExercise,
      set_schemes: [],
    };

    render(
      <SetSchemeEditor
        exercise={exerciseWithoutSets}
        onExerciseUpdate={mockOnExerciseUpdate}
        isMostRecentWeek={true}
      />
    );

    await user.click(screen.getByLabelText(/edit exercise/i));

    expect(screen.getByText('Bench Press')).toBeInTheDocument();
    expect(screen.getByLabelText('Sets')).toBeInTheDocument();
  });
});
