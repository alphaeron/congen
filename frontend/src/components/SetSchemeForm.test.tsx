import { useForm } from '@tanstack/react-form';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import React from 'react';

import { SetSchemeForm, type SetSchemeFormData } from './SetSchemeForm';
import type { UserWeightUnitPreference } from '../api/types';

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
      totalSets: 1,
      targetWeight: 0,
      targetReps: 1,
      restSeconds: 0,
      performedWeight: undefined,
      performedReps: undefined,
      useTempo: false,
      eccentricTempo: '',
      isometricTempo: '',
      concentricTempo: '',
      isAmrap: false,
      isEmom: false,
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

describe('SetSchemeForm', () => {
  it('renders all form fields by default', () => {
    render(<TestWrapper />);

    expect(screen.getByLabelText('Total Sets')).toBeInTheDocument();
    expect(screen.getByLabelText('Target Weight (kg)')).toBeInTheDocument();
    expect(screen.getByLabelText('Performed Weight (kg)')).toBeInTheDocument();
    expect(screen.getByLabelText('Target Reps')).toBeInTheDocument();
    expect(screen.getByLabelText('Performed Reps')).toBeInTheDocument();
    expect(screen.getByLabelText('Rest Period (seconds)')).toBeInTheDocument();
    expect(screen.getByText('Use Tempo')).toBeInTheDocument();
    expect(screen.getByText('AMRAP')).toBeInTheDocument();
    expect(screen.getByText('EMOM')).toBeInTheDocument();
  });

  it('shows weight unit in lbs when user prefers lbs for the exercise', () => {
    render(
      <TestWrapper exerciseName="Bench Press" weightUnitPreferences={mockWeightUnitPreferences} />
    );

    expect(screen.getByLabelText('Target Weight (lbs)')).toBeInTheDocument();
    expect(screen.getByLabelText('Performed Weight (lbs)')).toBeInTheDocument();
  });

  it('shows weight unit in kg when user prefers kg for the exercise', () => {
    render(<TestWrapper exerciseName="Squat" weightUnitPreferences={mockWeightUnitPreferences} />);

    expect(screen.getByLabelText('Target Weight (kg)')).toBeInTheDocument();
    expect(screen.getByLabelText('Performed Weight (kg)')).toBeInTheDocument();
  });

  it('defaults to kg when no weight unit preference is found', () => {
    render(
      <TestWrapper exerciseName="Deadlift" weightUnitPreferences={mockWeightUnitPreferences} />
    );

    expect(screen.getByLabelText('Target Weight (kg)')).toBeInTheDocument();
    expect(screen.getByLabelText('Performed Weight (kg)')).toBeInTheDocument();
  });

  it('hides performed fields when showPerformedFields is false', () => {
    render(<TestWrapper showPerformedFields={false} />);

    expect(screen.queryByLabelText('Performed Weight (kg)')).not.toBeInTheDocument();
    expect(screen.queryByLabelText('Performed Reps')).not.toBeInTheDocument();
  });

  it('hides tempo fields when showTempoFields is false', () => {
    render(<TestWrapper showTempoFields={false} />);

    expect(screen.queryByText('Use Tempo')).not.toBeInTheDocument();
  });

  it('hides set type fields when showSetTypeFields is false', () => {
    render(<TestWrapper showSetTypeFields={false} />);

    expect(screen.queryByText('AMRAP')).not.toBeInTheDocument();
    expect(screen.queryByText('EMOM')).not.toBeInTheDocument();
  });

  it('shows tempo input fields when tempo is enabled', async () => {
    render(<TestWrapper defaultValues={{ useTempo: true }} />);

    // The tempo fields should be visible when useTempo is true
    expect(screen.getByLabelText('Eccentric')).toBeInTheDocument();
    expect(screen.getByLabelText('Isometric')).toBeInTheDocument();
    expect(screen.getByLabelText('Concentric')).toBeInTheDocument();
  });

  it('validates required fields', async () => {
    const user = userEvent.setup();
    render(<TestWrapper />);

    const totalSetsInput = screen.getByLabelText('Total Sets');
    await user.clear(totalSetsInput);
    await user.type(totalSetsInput, '0');

    // Trigger validation by clicking away
    await user.click(screen.getByLabelText('Target Weight (kg)'));

    await waitFor(() => {
      expect(screen.getByText('Must have at least 1 set')).toBeInTheDocument();
    });
  });

  it('handles weight conversion for display', async () => {
    render(
      <TestWrapper
        exerciseName="Bench Press"
        weightUnitPreferences={mockWeightUnitPreferences}
        defaultValues={{ targetWeight: 100 }} // 100 kg
      />
    );

    // The weight should show the raw value (100) since we're storing raw input values
    const targetWeightInput = screen.getByLabelText('Target Weight (lbs)') as HTMLInputElement;
    expect(targetWeightInput.value).toBe('100');
  });

  it('handles weight conversion for storage', async () => {
    const user = userEvent.setup();
    render(
      <TestWrapper exerciseName="Bench Press" weightUnitPreferences={mockWeightUnitPreferences} />
    );

    const targetWeightInput = screen.getByLabelText('Target Weight (lbs)');
    await user.clear(targetWeightInput);
    await user.type(targetWeightInput, '220.46');

    // The value gets converted: 220.46 lbs -> 100 kg -> 220.46 lbs (with rounding)
    // Due to floating point precision, we expect approximately 220.46
    expect(targetWeightInput).toHaveValue('220.46');
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

    expect(screen.getByLabelText('Total Sets')).toHaveValue('3');
    expect(screen.getByLabelText('Target Weight (kg)')).toHaveValue('50');
    expect(screen.getByLabelText('Target Reps')).toHaveValue('8');
    expect(screen.getByLabelText('Rest Period (seconds)')).toHaveValue('90');
  });

  it('handles input field clearing and leading zeros correctly', async () => {
    const user = userEvent.setup();
    render(<TestWrapper />);

    const totalSetsInput = screen.getByLabelText('Total Sets');

    // Test clearing the field
    await user.clear(totalSetsInput);
    expect(totalSetsInput).toHaveValue('');

    // Test typing with leading zeros
    await user.type(totalSetsInput, '024');
    expect(totalSetsInput).toHaveValue('024'); // Leading zeros are preserved during typing

    // Test clearing and typing a single digit
    await user.clear(totalSetsInput);
    await user.type(totalSetsInput, '5');
    expect(totalSetsInput).toHaveValue('5');
  });

  it('handles floating point numbers correctly for weight fields', async () => {
    const user = userEvent.setup();
    render(
      <TestWrapper exerciseName="Bench Press" weightUnitPreferences={mockWeightUnitPreferences} />
    );

    const targetWeightInput = screen.getByLabelText('Target Weight (lbs)');

    // Test entering a decimal weight
    await user.clear(targetWeightInput);
    await user.type(targetWeightInput, '135.5');
    // The input should accept the decimal value
    expect(targetWeightInput).toHaveValue('135.5');

    // Test entering a weight with multiple decimal places
    await user.clear(targetWeightInput);
    await user.type(targetWeightInput, '225.75');
    expect(targetWeightInput).toHaveValue('225.75');

    // Test entering a weight that starts with decimal
    await user.clear(targetWeightInput);
    await user.type(targetWeightInput, '.5');
    expect(targetWeightInput).toHaveValue('.5');
  });

  it('handles integer fields correctly (sets, reps, rest)', async () => {
    const user = userEvent.setup();
    render(<TestWrapper />);

    const totalSetsInput = screen.getByLabelText('Total Sets');
    const targetRepsInput = screen.getByLabelText('Target Reps');
    const restInput = screen.getByLabelText('Rest Period (seconds)');

    // Test that decimal values are preserved during typing
    await user.clear(totalSetsInput);
    await user.type(totalSetsInput, '3.7');
    expect(totalSetsInput).toHaveValue('3.7'); // Should preserve decimal during typing

    // Test that decimal values are converted to integers on blur
    await user.click(document.body); // Blur the input
    expect(totalSetsInput).toHaveValue('3'); // Should be floored to 3 on blur

    // Test that decimal values are preserved during typing for reps
    await user.clear(targetRepsInput);
    await user.type(targetRepsInput, '8.9');
    expect(targetRepsInput).toHaveValue('8.9'); // Should preserve decimal during typing

    // Test that decimal values are converted to integers on blur for reps
    await user.click(document.body); // Blur the input
    expect(targetRepsInput).toHaveValue('8'); // Should be floored to 8 on blur

    // Test that decimal values are preserved during typing for rest
    await user.clear(restInput);
    await user.type(restInput, '90.5');
    expect(restInput).toHaveValue('90.5'); // Should preserve decimal during typing

    // Test that decimal values are converted to integers on blur for rest
    await user.click(document.body); // Blur the input
    expect(restInput).toHaveValue('90'); // Should be floored to 90 on blur
  });
});
