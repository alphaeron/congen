import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router';
import MockAdapter from 'axios-mock-adapter';

import { WorkoutGenerationWizard } from './WorkoutGenerationWizard';
import { ENDPOINT } from '../api/endpoint';
import type { Program } from '../api/types';

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
  return render(
    <MemoryRouter>
      {component}
    </MemoryRouter>
  );
};

describe('WorkoutGenerationWizard', () => {
  beforeEach(() => {
    mock.reset();
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
    expect(screen.getByText(`The next week's workouts will be generated for ${mockProgram.name}. This will create a new week of workouts based on your program preferences and current progress.`)).toBeInTheDocument();
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

    mock.onPost('/conjugate_workout_generator/1').reply(200, updatedProgram);
    mock.onGet('/conjugate_workout_generator/exercise_pool').reply(200, mockUserExercisePool);

    renderWithProviders(
      <WorkoutGenerationWizard
        open={true}
        onClose={jest.fn()}
        onComplete={onComplete}
        program={mockProgram}
      />
    );

    const generateButton = screen.getByRole('button', { name: 'Generate Workouts' });
    fireEvent.click(generateButton);

    // Check that the API call was made
    await waitFor(() => {
      expect(mock.history.post).toHaveLength(1);
      expect(mock.history.post[0].url).toBe('/conjugate_workout_generator/1');
    });
  });

  it('handles generation errors gracefully', async () => {
    const onClose = jest.fn();
    
    mock.onPost('/conjugate_workout_generator/1').reply(500, { error: 'Generation failed' });
    mock.onGet('/conjugate_workout_generator/exercise_pool').reply(200, mockUserExercisePool);

    renderWithProviders(
      <WorkoutGenerationWizard
        open={true}
        onClose={onClose}
        onComplete={jest.fn()}
        program={mockProgram}
      />
    );

    const generateButton = screen.getByRole('button', { name: 'Generate Workouts' });
    fireEvent.click(generateButton);

    // Check that the API call was made and failed
    await waitFor(() => {
      expect(mock.history.post).toHaveLength(1);
      expect(mock.history.post[0].url).toBe('/conjugate_workout_generator/1');
    });
  });
});
