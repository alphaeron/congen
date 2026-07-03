import { render, screen, fireEvent } from '@testing-library/react';
import React from 'react';

import { OneRepMaxInputStep } from './OneRepMaxInputStep';
import type { Exercise } from '../api/types';

// Mock the theme provider
const renderWithProviders = (ui: React.ReactElement) => {
  return render(ui);
};

const mockExercises: Exercise[] = [
  {
    id: 1,
    name: 'Bench Press',
    category: 'strength',
    primary_muscle: 'chest',
    secondary_muscles: ['triceps', 'shoulders'],
    instructions: 'Test instructions',
    equipment: 'barbell',
    difficulty: 'intermediate',
    set_schemes: [
      {
        id: 1,
        sets: 3,
        reps: 8,
        weight: 185,
        weight_unit: 'KG',
        rest_seconds: 120,
        rpe: 8,
        notes: null,
      },
    ],
  },
  {
    id: 2,
    name: 'Squat',
    category: 'strength',
    primary_muscle: 'quadriceps',
    secondary_muscles: ['glutes', 'hamstrings'],
    instructions: 'Test instructions',
    equipment: 'barbell',
    difficulty: 'intermediate',
    set_schemes: [
      {
        id: 2,
        sets: 3,
        reps: 5,
        weight: 225,
        weight_unit: 'KG',
        rest_seconds: 180,
        rpe: 9,
        notes: null,
      },
    ],
  },
];

describe('OneRepMaxInputStep', () => {
  it('renders the first exercise and progress indicator', () => {
    const onInputsChange = jest.fn();
    const onDeclineAll = jest.fn();
    const onComplete = jest.fn();

    renderWithProviders(
      <OneRepMaxInputStep
        exercises={mockExercises}
        onInputsChange={onInputsChange}
        onDeclineAll={onDeclineAll}
        onComplete={onComplete}
      />
    );

    expect(screen.getByText('Record Your 1RM Data (1/2)')).toBeInTheDocument();
    expect(screen.getByText('Bench Press')).toBeInTheDocument();
    expect(screen.queryByText('Squat')).not.toBeInTheDocument(); // Only one exercise visible at a time
  });

  it('shows reps and weight input fields directly', () => {
    const onInputsChange = jest.fn();
    const onDeclineAll = jest.fn();
    const onComplete = jest.fn();

    renderWithProviders(
      <OneRepMaxInputStep
        exercises={mockExercises}
        onInputsChange={onInputsChange}
        onDeclineAll={onDeclineAll}
        onComplete={onComplete}
      />
    );

    expect(screen.getByLabelText('Reps')).toBeInTheDocument();
    expect(screen.getByLabelText('Weight')).toBeInTheDocument();
    expect(screen.getByText('Unit')).toBeInTheDocument();
    expect(screen.getByText('KG')).toBeInTheDocument();
    expect(screen.getByText('LBS')).toBeInTheDocument();
  });

  it('allows inputting reps and weight values', () => {
    const onInputsChange = jest.fn();
    const onDeclineAll = jest.fn();
    const onComplete = jest.fn();

    renderWithProviders(
      <OneRepMaxInputStep
        exercises={mockExercises}
        onInputsChange={onInputsChange}
        onDeclineAll={onDeclineAll}
        onComplete={onComplete}
      />
    );

    const repsInput = screen.getByLabelText('Reps');
    const weightInput = screen.getByLabelText('Weight');

    // Change values to trigger onInputsChange
    fireEvent.change(repsInput, { target: { value: '10' } });
    fireEvent.change(weightInput, { target: { value: '200' } });

    expect(onInputsChange).toHaveBeenCalled();
  });

  it('exposes navigation functions via callback', () => {
    const onInputsChange = jest.fn();
    const onDeclineAll = jest.fn();
    const onComplete = jest.fn();
    const onNavigationChange = jest.fn();

    renderWithProviders(
      <OneRepMaxInputStep
        exercises={mockExercises}
        onInputsChange={onInputsChange}
        onDeclineAll={onDeclineAll}
        onComplete={onComplete}
        onNavigationChange={onNavigationChange}
      />
    );

    expect(onNavigationChange).toHaveBeenCalledWith(
      expect.objectContaining({
        onSkipExercise: expect.any(Function),
        onSkipRemaining: expect.any(Function),
        onNext: expect.any(Function),
        isLastExercise: false,
      })
    );
  });

  it('uses default values from exercise set schemes for reps/weight', () => {
    const onInputsChange = jest.fn();
    const onDeclineAll = jest.fn();
    const onComplete = jest.fn();

    renderWithProviders(
      <OneRepMaxInputStep
        exercises={mockExercises}
        onInputsChange={onInputsChange}
        onDeclineAll={onDeclineAll}
        onComplete={onComplete}
      />
    );

    // Check that default values from set scheme are used
    const repsInput = screen.getByLabelText('Reps');
    const weightInput = screen.getByLabelText('Weight');

    expect(repsInput).toHaveValue('8');
    expect(weightInput).toHaveValue('185');
  });

  it('shows calculated 1RM when reps and weight are provided', () => {
    const onInputsChange = jest.fn();
    const onDeclineAll = jest.fn();
    const onComplete = jest.fn();

    renderWithProviders(
      <OneRepMaxInputStep
        exercises={mockExercises}
        onInputsChange={onInputsChange}
        onDeclineAll={onDeclineAll}
        onComplete={onComplete}
      />
    );

    // The calculated 1RM should be shown with default values
    expect(screen.getByText(/Calculated 1RM:/)).toBeInTheDocument();
  });
});
