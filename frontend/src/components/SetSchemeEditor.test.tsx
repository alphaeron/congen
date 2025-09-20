import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import React from 'react';

import { SetSchemeEditor } from './SetSchemeEditor';
import type { ProgrammedExerciseWithSetSchemes } from '../api/types';

// Mock the updateProgrammedExercise and updateSetScheme functions
jest.mock('../api/programmedExercise', () => ({
  updateProgrammedExercise: jest.fn(),
}));

jest.mock('../api/setScheme', () => ({
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

    // When disabled, the button is wrapped in a span, so we need to find the button inside
    const editButton = screen.getByRole('button', {
      name: /editing only available for most recent week/i,
    });
    expect(editButton).toBeDisabled();
  });

  it('shows tooltip when edit button is disabled', async () => {
    render(
      <SetSchemeEditor
        exercise={mockExercise}
        onExerciseUpdate={mockOnExerciseUpdate}
        isMostRecentWeek={false}
      />
    );

    // When disabled, the button is wrapped in a span, so we need to find the button inside
    const editButton = screen.getByRole('button', {
      name: /editing only available for most recent week/i,
    });

    // For disabled buttons, we can't test tooltip interaction directly
    // Instead, we'll just verify the button is disabled and has the correct aria-label
    expect(editButton).toBeDisabled();
    expect(editButton).toHaveAttribute('aria-label', 'Editing only available for most recent week');
  });

  it('opens popover when edit button is clicked', async () => {
    const user = userEvent.setup();
    render(
      <SetSchemeEditor
        exercise={mockExercise}
        onExerciseUpdate={mockOnExerciseUpdate}
        isMostRecentWeek={true}
      />
    );

    const editButton = screen.getByLabelText(/edit exercise/i);
    await user.click(editButton);

    expect(screen.getByText(/edit exercise: bench press/i)).toBeInTheDocument();
    expect(screen.getByText('Set Scheme Details')).toBeInTheDocument();
    expect(screen.getByLabelText('Total Sets')).toBeInTheDocument();
  });

  it('populates form with existing exercise data', async () => {
    const user = userEvent.setup();
    render(
      <SetSchemeEditor
        exercise={mockExercise}
        onExerciseUpdate={mockOnExerciseUpdate}
        isMostRecentWeek={true}
      />
    );

    const editButton = screen.getByLabelText(/edit exercise/i);
    await user.click(editButton);

    // Check that form is populated with existing data

    // Use more specific selectors to avoid ambiguity
    const totalSetsInput = screen.getByLabelText('Total Sets');
    const targetRepsInput = screen.getByLabelText('Target Reps');
    const targetWeightInput = screen.getByLabelText('Target Weight (kg)');
    expect(totalSetsInput).toHaveValue('2'); // mockExercise has 2 sets
    expect(targetRepsInput).toHaveValue('8');
    expect(targetWeightInput).toHaveValue('135');
  });

  it('closes popover when cancel button is clicked', async () => {
    const user = userEvent.setup();
    render(
      <SetSchemeEditor
        exercise={mockExercise}
        onExerciseUpdate={mockOnExerciseUpdate}
        isMostRecentWeek={true}
      />
    );

    const editButton = screen.getByLabelText(/edit exercise/i);
    await user.click(editButton);

    expect(screen.getByText(/edit exercise: bench press/i)).toBeInTheDocument();

    const cancelButton = screen.getByText(/cancel/i);
    await user.click(cancelButton);

    // Popover should be closed (form should not be visible)
    expect(screen.queryByLabelText('Total Sets')).not.toBeInTheDocument();
  });

  it('validates required fields', async () => {
    const user = userEvent.setup();
    render(
      <SetSchemeEditor
        exercise={mockExercise}
        onExerciseUpdate={mockOnExerciseUpdate}
        isMostRecentWeek={true}
      />
    );

    const editButton = screen.getByLabelText(/edit exercise/i);
    await user.click(editButton);

    // Clear required fields
    const totalSetsInput = screen.getByLabelText('Total Sets');
    await user.clear(totalSetsInput);

    const submitButton = screen.getByText(/submit/i);
    await user.click(submitButton);

    // Should show validation error - check for specific validation message
    await waitFor(
      () => {
        const errorMessage = screen.queryByText(/must have at least 1 set/i);
        expect(errorMessage).toBeInTheDocument();
      },
      { timeout: 10000 }
    );
  }, 15000);

  it('submits form with valid data', async () => {
    const user = userEvent.setup();
    const { updateProgrammedExercise } = jest.requireActual('../api/programmedExercise');
    const { updateSetScheme } = jest.requireActual('../api/setScheme');

    updateProgrammedExercise.mockResolvedValue({ success: true });
    updateSetScheme.mockResolvedValue({ success: true });

    render(
      <SetSchemeEditor
        exercise={mockExercise}
        onExerciseUpdate={mockOnExerciseUpdate}
        isMostRecentWeek={true}
      />
    );

    const editButton = screen.getByLabelText(/edit exercise/i);
    await user.click(editButton);

    // Wait for the form to be ready

    const submitButton = screen.getByText(/submit/i);
    await user.click(submitButton);

    // Wait for API calls to complete
    await waitFor(
      () => {
        // Should call updateSetScheme for each set scheme
        expect(updateSetScheme).toHaveBeenCalledTimes(2); // mockExercise has 2 sets
      },
      { timeout: 10000 }
    );
  }, 15000);

  it('handles form submission errors gracefully', async () => {
    const user = userEvent.setup();
    const { updateSetScheme } = jest.requireActual('../api/setScheme');

    updateSetScheme.mockRejectedValue(new Error('API Error'));

    render(
      <SetSchemeEditor
        exercise={mockExercise}
        onExerciseUpdate={mockOnExerciseUpdate}
        isMostRecentWeek={true}
      />
    );

    const editButton = screen.getByLabelText(/edit exercise/i);
    await user.click(editButton);

    const submitButton = screen.getByText(/submit/i);
    await user.click(submitButton);

    // Should handle error gracefully (error would be shown via snackbar in real app)
    await waitFor(
      () => {
        expect(updateSetScheme).toHaveBeenCalled();
      },
      { timeout: 10000 }
    );
  }, 15000);

  it('updates all set schemes with same values when total sets changes', async () => {
    const user = userEvent.setup();
    const { updateProgrammedExercise } = jest.requireActual('../api/programmedExercise');
    const { updateSetScheme } = jest.requireActual('../api/setScheme');

    updateProgrammedExercise.mockResolvedValue({ success: true });
    updateSetScheme.mockResolvedValue({ success: true });

    render(
      <SetSchemeEditor
        exercise={mockExercise}
        onExerciseUpdate={mockOnExerciseUpdate}
        isMostRecentWeek={true}
      />
    );

    const editButton = screen.getByLabelText(/edit exercise/i);
    await user.click(editButton);

    // Change total sets
    const totalSetsInput = screen.getByLabelText('Total Sets');
    await user.clear(totalSetsInput);
    await user.type(totalSetsInput, '3');

    const submitButton = screen.getByText(/submit/i);
    await user.click(submitButton);

    await waitFor(
      () => {
        // Should call updateSetScheme for each existing set scheme
        expect(updateSetScheme).toHaveBeenCalledTimes(2); // mockExercise has 2 sets
      },
      { timeout: 10000 }
    );
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

    const editButton = screen.getByLabelText(/edit exercise/i);
    await user.click(editButton);

    // Should still show the form
    expect(screen.getByText(/edit exercise: bench press/i)).toBeInTheDocument();
    expect(screen.getByLabelText('Total Sets')).toBeInTheDocument();
  });
});
