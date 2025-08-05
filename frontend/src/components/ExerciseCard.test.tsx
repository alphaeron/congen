import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router';
import * as React from 'react';

import { ExerciseCard } from './ExerciseCard';

import { EXERCISE, EXERCISE_MUSCLE, EXERCISE_EQUIPMENT } from '../__mocks__/data';

describe('ExerciseCard component', () => {
  beforeEach(() => {
    render(
      <MemoryRouter>
        <ExerciseCard
          exercise={EXERCISE}
          equipment={[EXERCISE_EQUIPMENT.equipment_name]}
          muscles={[EXERCISE_MUSCLE.muscle_name]}
        />
      </MemoryRouter>
    );
  });

  it('Renders the equipment', () => {
    const equipmentRegex = new RegExp(`^${EXERCISE_EQUIPMENT.equipment_name}$`, 'i');
    expect(screen.getByText(equipmentRegex)).toBeInTheDocument();
  });

  it('Renders the muscle', () => {
    const muscleRegex = new RegExp(`^${EXERCISE_MUSCLE.muscle_name}$`, 'i');
    expect(screen.getByText(muscleRegex)).toBeInTheDocument();
  });

  it('Renders the exercise name', () => {
    const regex = new RegExp(`^${EXERCISE.name}$`, 'i');
    expect(screen.getByText(regex)).toBeInTheDocument();
  });

  it('Renders the exercise movementType', () => {
    const regex = new RegExp(`${EXERCISE.movement_type}`, 'i');
    expect(screen.getByText(regex)).toBeInTheDocument();
  });

  it('Renders the exercise isUnilateral', () => {
    const text = EXERCISE.is_unilateral ? 'Unilateral' : 'Bilateral';
    const regex = new RegExp(`${text}`, 'i');
    expect(screen.getByText(regex)).toBeInTheDocument();
  });

  it('Renders the exercise isUpper', () => {
    const text = EXERCISE.is_upper ? 'Upper Body' : 'Lower Body';
    const regex = new RegExp(`${text}`, 'i');
    expect(screen.getByText(regex)).toBeInTheDocument();
  });

  it('Renders the exercise isAccessory', () => {
    const text = EXERCISE.is_accessory ? 'Accessory' : 'Primary Movement';
    const regex = new RegExp(`${text}`, 'i');
    expect(screen.getByText(regex)).toBeInTheDocument();
  });
});
