import { useForm } from '@tanstack/react-form';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import React from 'react';

import {
  SetSchemeForm,
  buildPerformedBySetFilledFromSource,
  buildSetSchemeFormDefaultsFromExercise,
  performedValuesVary,
  resolvePerformedForSetIndex,
  type SetSchemeFormData,
} from './SetSchemeForm';
import type { ProgrammedExerciseWithSetSchemes, UserWeightUnitPreference } from '../api/types';

const mockWeightUnitPreferences: UserWeightUnitPreference[] = [
  {
    user_id: 'user1',
    exercise_name: 'Bench Press',
    preferred_unit: 'LBS',
    created_at: new Date('2023-01-01T00:00:00Z'),
    updated_at: new Date('2023-01-01T00:00:00Z'),
  },
  {
    user_id: 'user1',
    exercise_name: 'Squat',
    preferred_unit: 'KG',
    created_at: new Date('2023-01-01T00:00:00Z'),
    updated_at: new Date('2023-01-01T00:00:00Z'),
  },
];

const baseDefaults: SetSchemeFormData = {
  totalSets: 1,
  targetWeight: 0,
  targetReps: 1,
  restSeconds: 0,
  performedWeight: undefined,
  performedReps: undefined,
  performedRepsBySet: [],
  performedWeightBySet: [],
  customizePerSet: false,
  useTempo: false,
  eccentricTempo: '',
  isometricTempo: '',
  concentricTempo: '',
  isAmrap: false,
  isEmom: false,
};

const TestWrapper: React.FC<{
  defaultValues?: Partial<SetSchemeFormData>;
  exerciseName?: string;
  weightUnitPreferences?: UserWeightUnitPreference[];
  showPerformedFields?: boolean;
  showTempoFields?: boolean;
  showSetTypeFields?: boolean;
}> = ({
  defaultValues = {},
  exerciseName,
  weightUnitPreferences = [],
  showPerformedFields = true,
  showTempoFields = true,
  showSetTypeFields = true,
}) => {
  const form = useForm({
    defaultValues: {
      ...baseDefaults,
      ...defaultValues,
    },
  });

  return (
    <form>
      <SetSchemeForm
        form={form}
        exerciseName={exerciseName}
        weightUnitPreferences={weightUnitPreferences}
        showPerformedFields={showPerformedFields}
        showTempoFields={showTempoFields}
        showSetTypeFields={showSetTypeFields}
      />
    </form>
  );
};

const openAdvanced = async (user: ReturnType<typeof userEvent.setup>) => {
  await user.click(screen.getByRole('button', { name: /advanced/i }));
};

describe('SetSchemeForm', () => {
  it('hides rep fields when AMRAP is enabled', () => {
    render(<TestWrapper defaultValues={{ isAmrap: true }} showSetTypeFields={true} />);

    expect(screen.queryByLabelText('Target reps')).not.toBeInTheDocument();
    expect(screen.getByLabelText('Target weight')).toBeInTheDocument();
    expect(screen.queryByLabelText('Performed reps')).not.toBeInTheDocument();
    expect(screen.getByLabelText('Performed weight')).toBeInTheDocument();
  });

  it('renders core form fields by default', () => {
    render(<TestWrapper />);

    expect(screen.getByLabelText('Sets')).toBeInTheDocument();
    expect(screen.getByLabelText('Target weight')).toBeInTheDocument();
    expect(screen.getByLabelText('Performed weight')).toBeInTheDocument();
    expect(screen.getByLabelText('Target reps')).toBeInTheDocument();
    expect(screen.getByLabelText('Performed reps')).toBeInTheDocument();
    expect(screen.getByLabelText('Rest')).toBeInTheDocument();
    expect(screen.getByLabelText('Expand per set')).toBeInTheDocument();
    expect(screen.getByLabelText('Match target')).toBeInTheDocument();
  });

  it('shows weight unit suffix for exercise preference', () => {
    render(
      <TestWrapper exerciseName="Bench Press" weightUnitPreferences={mockWeightUnitPreferences} />
    );

    expect(screen.getByText(/target weight \(lbs\)/i)).toBeInTheDocument();
    expect(screen.getByText(/performed weight \(lbs\)/i)).toBeInTheDocument();
  });

  it('shows kg suffix when no weight unit preference is found', () => {
    render(
      <TestWrapper exerciseName="Deadlift" weightUnitPreferences={mockWeightUnitPreferences} />
    );

    expect(screen.getByText(/target weight \(kg\)/i)).toBeInTheDocument();
  });

  it('hides performed fields when showPerformedFields is false', () => {
    render(<TestWrapper showPerformedFields={false} />);

    expect(screen.queryByLabelText('Performed weight')).not.toBeInTheDocument();
    expect(screen.queryByLabelText('Performed reps')).not.toBeInTheDocument();
    expect(screen.queryByLabelText('Expand per set')).not.toBeInTheDocument();
  });

  it('hides tempo fields until advanced is expanded', async () => {
    const user = userEvent.setup();
    render(<TestWrapper showTempoFields={true} />);

    expect(screen.getByText('Use Tempo')).not.toBeVisible();

    await openAdvanced(user);

    expect(screen.getByText('Use Tempo')).toBeVisible();
  });

  it('hides set type fields until advanced is expanded', async () => {
    const user = userEvent.setup();
    render(<TestWrapper showSetTypeFields={true} />);

    expect(screen.getByText('AMRAP')).not.toBeVisible();

    await openAdvanced(user);

    expect(screen.getByText('AMRAP')).toBeVisible();
    expect(screen.getByText('EMOM')).toBeVisible();
  });

  it('shows tempo input fields when tempo is enabled', async () => {
    const user = userEvent.setup();
    render(<TestWrapper defaultValues={{ useTempo: true }} />);

    await openAdvanced(user);

    expect(screen.getByLabelText('Eccentric')).toBeInTheDocument();
    expect(screen.getByLabelText('Isometric')).toBeInTheDocument();
    expect(screen.getByLabelText('Concentric')).toBeInTheDocument();
  });

  it('expands per-set customization', async () => {
    const user = userEvent.setup();
    render(
      <TestWrapper
        defaultValues={{
          totalSets: 2,
          targetWeight: 135,
          targetReps: 8,
          performedWeight: 135,
          performedReps: 8,
        }}
      />
    );

    await user.click(screen.getByLabelText('Expand per set'));

    expect(screen.getByLabelText('Set 1 weight')).toBeInTheDocument();
    expect(screen.getByLabelText('Set 1 reps')).toBeInTheDocument();
    expect(screen.getByLabelText('Set 2 weight')).toBeInTheDocument();
    expect(screen.getByLabelText('Collapse to all sets')).toBeInTheDocument();
  });

  it('collapses per-set view back to all sets', async () => {
    const user = userEvent.setup();
    render(
      <TestWrapper
        defaultValues={{
          totalSets: 2,
          targetWeight: 135,
          targetReps: 8,
          performedWeight: 135,
          performedReps: 8,
          customizePerSet: true,
          performedRepsBySet: [8, 6],
          performedWeightBySet: [135, 125],
        }}
      />
    );

    await user.click(screen.getByLabelText('Collapse to all sets'));

    await waitFor(() => {
      expect(screen.getByLabelText('Performed weight')).toBeInTheDocument();
      expect(screen.queryByLabelText('Set 1 weight')).not.toBeInTheDocument();
    });
  });

  it('applies set weight to all sets without changing reps', async () => {
    const user = userEvent.setup();
    render(
      <TestWrapper
        defaultValues={{
          totalSets: 3,
          targetWeight: 135,
          targetReps: 8,
          performedWeight: 135,
          performedReps: 8,
          customizePerSet: true,
          performedRepsBySet: [8, 6, 8],
          performedWeightBySet: [135, 125, 135],
        }}
      />
    );

    await user.click(screen.getByLabelText('Apply set 1 weight to all sets'));

    expect(screen.getByLabelText('Set 1 weight')).toHaveValue('135');
    expect(screen.getByLabelText('Set 2 weight')).toHaveValue('135');
    expect(screen.getByLabelText('Set 3 weight')).toHaveValue('135');
    expect(screen.getByLabelText('Set 2 reps')).toHaveValue('6');
  });

  it('applies set reps to all sets without changing weight', async () => {
    const user = userEvent.setup();
    render(
      <TestWrapper
        defaultValues={{
          totalSets: 2,
          targetWeight: 135,
          targetReps: 8,
          customizePerSet: true,
          performedRepsBySet: [8, 5],
          performedWeightBySet: [135, 115],
        }}
      />
    );

    await user.click(screen.getByLabelText('Apply set 2 reps to all sets'));

    expect(screen.getByLabelText('Set 1 reps')).toHaveValue('5');
    expect(screen.getByLabelText('Set 2 reps')).toHaveValue('5');
    expect(screen.getByLabelText('Set 1 weight')).toHaveValue('135');
    expect(screen.getByLabelText('Set 2 weight')).toHaveValue('115');
  });

  it('syncs performed values to target', async () => {
    const user = userEvent.setup();
    render(
      <TestWrapper
        defaultValues={{
          targetWeight: 200,
          targetReps: 5,
          performedWeight: 100,
          performedReps: 3,
        }}
      />
    );

    await user.click(screen.getByLabelText('Match target'));

    expect(screen.getByLabelText('Performed weight')).toHaveValue('200');
    expect(screen.getByLabelText('Performed reps')).toHaveValue('5');
  });

  it('syncs performed values to target while staying expanded per set', async () => {
    const user = userEvent.setup();
    render(
      <TestWrapper
        defaultValues={{
          totalSets: 2,
          targetWeight: 200,
          targetReps: 5,
          customizePerSet: true,
          performedRepsBySet: [3, 4],
          performedWeightBySet: [100, 110],
        }}
      />
    );

    await user.click(screen.getByLabelText('Match target'));

    expect(screen.getByLabelText('Collapse to all sets')).toBeInTheDocument();
    expect(screen.getByLabelText('Set 1 weight')).toHaveValue('200');
    expect(screen.getByLabelText('Set 1 reps')).toHaveValue('5');
    expect(screen.getByLabelText('Set 2 weight')).toHaveValue('200');
    expect(screen.getByLabelText('Set 2 reps')).toHaveValue('5');
  });

  it('validates minimum sets via stepper', async () => {
    const user = userEvent.setup();
    render(<TestWrapper defaultValues={{ totalSets: 1 }} />);

    await user.click(screen.getByLabelText('Decrease Sets'));

    await waitFor(() => {
      expect(screen.getByLabelText('Sets')).toHaveValue('1');
    });
  });

  it('populates form with default values', () => {
    render(
      <TestWrapper
        defaultValues={{
          totalSets: 3,
          targetWeight: 50,
          targetReps: 8,
          restSeconds: 90,
        }}
      />
    );

    expect(screen.getByLabelText('Sets')).toHaveValue('3');
    expect(screen.getByLabelText('Target weight')).toHaveValue('50');
    expect(screen.getByLabelText('Target reps')).toHaveValue('8');
    expect(screen.getByLabelText('Rest')).toHaveValue('90');
  });

  it('handles floating point numbers for weight fields', async () => {
    const user = userEvent.setup();
    render(
      <TestWrapper exerciseName="Bench Press" weightUnitPreferences={mockWeightUnitPreferences} />
    );

    const targetWeightInput = screen.getByLabelText('Target weight');
    await user.clear(targetWeightInput);
    await user.type(targetWeightInput, '135.5');
    expect(targetWeightInput).toHaveValue('135.5');
  });

  it('allows typing a leading decimal in target weight', async () => {
    const user = userEvent.setup();
    render(
      <TestWrapper exerciseName="Bench Press" weightUnitPreferences={mockWeightUnitPreferences} />
    );

    const targetWeightInput = screen.getByLabelText('Target weight');
    await user.clear(targetWeightInput);
    await user.type(targetWeightInput, '.5');
    expect(targetWeightInput).toHaveValue('.5');
  });

  it('handles integer fields for sets and reps', async () => {
    const user = userEvent.setup();
    render(<TestWrapper />);

    const setsInput = screen.getByLabelText('Sets');
    const targetRepsInput = screen.getByLabelText('Target reps');

    await user.clear(setsInput);
    await user.type(setsInput, '3');
    expect(setsInput).toHaveValue('3');

    await user.clear(targetRepsInput);
    await user.type(targetRepsInput, '8');
    expect(targetRepsInput).toHaveValue('8');
  });
});

describe('set scheme form helpers', () => {
  const mockExercise: ProgrammedExerciseWithSetSchemes = {
    exercise: {
      id: 1,
      workout_stage_id: 1,
      exercise_name: 'Bench Press',
      position: 1,
      notes: '',
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
        created_at: new Date('2024-01-01'),
        updated_at: new Date('2024-01-01'),
      },
      {
        id: 2,
        programmed_exercise_id: 1,
        set_number: 2,
        target_rep_count: 8,
        target_weight: 135,
        performed_rep_count: 6,
        performed_weight: 135,
        rest_seconds: 90,
        use_tempo: false,
        created_at: new Date('2024-01-01'),
        updated_at: new Date('2024-01-01'),
      },
    ],
  };

  it('detects when performed values vary by set', () => {
    expect(performedValuesVary([8, 6], [135, 135], 8, 135)).toBe(true);
    expect(performedValuesVary([8, 8], [135, 135], 8, 135)).toBe(false);
  });

  it('builds defaults with customizePerSet when values differ', () => {
    const defaults = buildSetSchemeFormDefaultsFromExercise(mockExercise, 'KG');
    expect(defaults.totalSets).toBe(2);
    expect(defaults.customizePerSet).toBe(true);
    expect(defaults.performedRepsBySet).toEqual([8, 6]);
  });

  it('prefills performed from target when not recorded', () => {
    const exerciseWithoutPerformed = {
      ...mockExercise,
      set_schemes: [
        {
          ...mockExercise.set_schemes[0],
          performed_rep_count: undefined,
          performed_weight: undefined,
        },
      ],
    };
    const defaults = buildSetSchemeFormDefaultsFromExercise(exerciseWithoutPerformed, 'KG');
    expect(defaults.performedReps).toBe(8);
    expect(defaults.performedWeight).toBe(135);
    expect(defaults.customizePerSet).toBe(false);
  });

  it('resolves performed values for submit', () => {
    const shared = {
      customizePerSet: false,
      performedReps: 8,
      performedWeight: 135,
      performedRepsBySet: [],
      performedWeightBySet: [],
    };
    expect(resolvePerformedForSetIndex(0, shared)).toEqual({ reps: 8, weight: 135 });

    const customized = {
      customizePerSet: true,
      performedReps: 8,
      performedWeight: 135,
      performedRepsBySet: [8, 6],
      performedWeightBySet: [135, 130],
    };
    expect(resolvePerformedForSetIndex(1, customized)).toEqual({ reps: 6, weight: 130 });
  });

  it('builds filled performed arrays from a source set', () => {
    const values: SetSchemeFormData = {
      ...baseDefaults,
      totalSets: 3,
      targetWeight: 100,
      targetReps: 10,
      customizePerSet: true,
      performedRepsBySet: [10, 8, 10],
      performedWeightBySet: [100, 90, 100],
    };

    const filled = buildPerformedBySetFilledFromSource(values, 1);
    expect(filled.performedRepsBySet).toEqual([8, 8, 8]);
    expect(filled.performedWeightBySet).toEqual([90, 90, 90]);
  });
});
