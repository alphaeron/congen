import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router';
import AxiosMockAdapter from 'axios-mock-adapter';
import React from 'react';

import { ExerciseName } from './ExerciseName';
import { ENDPOINT } from '../api/endpoint';
import type { Exercise, ExerciseMuscle, Muscle } from '../api/types';

// Mock the useApiGet hook
jest.mock('../api/hooks', () => ({
  useApiGet: jest.fn(),
}));

const mockUseApiGet = require('../api/hooks').useApiGet;

describe('ExerciseName', () => {
  let mockAdapter: AxiosMockAdapter;

  beforeEach(() => {
    mockAdapter = new AxiosMockAdapter(ENDPOINT);
  });

  afterEach(() => {
    mockAdapter.restore();
    jest.clearAllMocks();
  });

  const mockExercise: Exercise = {
    name: 'Bench Press',
    description: 'A compound upper body exercise',
    movement_type: 'strength',
    is_unilateral: false,
    is_upper: true,
    is_accessory: false,
  };

  const mockExerciseMuscles: ExerciseMuscle[] = [
    {
      exercise_name: 'Bench Press',
      muscle_name: 'Pectoralis Major',
    },
    {
      exercise_name: 'Bench Press',
      muscle_name: 'Triceps Brachii',
    },
  ];

  const mockMuscles: Muscle[] = [
    {
      name: 'Pectoralis Major',
      description: 'Chest muscle',
    },
    {
      name: 'Triceps Brachii',
      description: 'Arm muscle',
    },
  ];

  it('should render exercise name with tooltip when data is loaded', async () => {
    mockUseApiGet
      .mockReturnValueOnce({
        data: mockExercise,
        isLoading: false,
        error: null,
      })
      .mockReturnValueOnce({
        data: mockExerciseMuscles,
        isLoading: false,
        error: null,
      })
      .mockReturnValueOnce({
        data: mockMuscles,
        isLoading: false,
        error: null,
      });

    render(
      <MemoryRouter>
        <ExerciseName exerciseName="Bench Press" />
      </MemoryRouter>
    );

    expect(screen.getByText('Bench Press')).toBeInTheDocument();
    
    // Check that the element has cursor pointer style and is a link
    const exerciseNameElement = screen.getByText('Bench Press');
    expect(exerciseNameElement).toHaveStyle('cursor: pointer');
    
    // Check that it's wrapped in a link
    const linkElement = exerciseNameElement.closest('a');
    expect(linkElement).toHaveAttribute('href', '/exercises/Bench%20Press');
  });

  it('should show loading tooltip when data is loading', async () => {
    mockUseApiGet
      .mockReturnValueOnce({
        data: null,
        isLoading: true,
        error: null,
      })
      .mockReturnValueOnce({
        data: null,
        isLoading: true,
        error: null,
      })
      .mockReturnValueOnce({
        data: null,
        isLoading: true,
        error: null,
      });

    render(
      <MemoryRouter>
        <ExerciseName exerciseName="Bench Press" />
      </MemoryRouter>
    );

    expect(screen.getByText('Bench Press')).toBeInTheDocument();
    
    // Check that it's wrapped in a link even when loading
    const exerciseNameElement = screen.getByText('Bench Press');
    const linkElement = exerciseNameElement.closest('a');
    expect(linkElement).toHaveAttribute('href', '/exercises/Bench%20Press');
  });

  it('should show error tooltip when data fails to load', async () => {
    mockUseApiGet
      .mockReturnValueOnce({
        data: null,
        isLoading: false,
        error: new Error('Failed to load'),
      })
      .mockReturnValueOnce({
        data: null,
        isLoading: false,
        error: new Error('Failed to load'),
      })
      .mockReturnValueOnce({
        data: null,
        isLoading: false,
        error: new Error('Failed to load'),
      });

    render(
      <MemoryRouter>
        <ExerciseName exerciseName="Bench Press" />
      </MemoryRouter>
    );

    expect(screen.getByText('Bench Press')).toBeInTheDocument();
    
    // Check that it's wrapped in a link even when there's an error
    const exerciseNameElement = screen.getByText('Bench Press');
    const linkElement = exerciseNameElement.closest('a');
    expect(linkElement).toHaveAttribute('href', '/exercises/Bench%20Press');
  });

  it('should render with custom variant and sx props', async () => {
    mockUseApiGet
      .mockReturnValueOnce({
        data: mockExercise,
        isLoading: false,
        error: null,
      })
      .mockReturnValueOnce({
        data: mockExerciseMuscles,
        isLoading: false,
        error: null,
      })
      .mockReturnValueOnce({
        data: mockMuscles,
        isLoading: false,
        error: null,
      });

    render(
      <MemoryRouter>
        <ExerciseName 
          exerciseName="Bench Press" 
          variant="h6"
          sx={{ fontWeight: 'bold' }}
        />
      </MemoryRouter>
    );

    const exerciseNameElement = screen.getByText('Bench Press');
    expect(exerciseNameElement).toHaveStyle('font-weight: 700');
  });

  it('should render custom children when provided', async () => {
    mockUseApiGet
      .mockReturnValueOnce({
        data: mockExercise,
        isLoading: false,
        error: null,
      })
      .mockReturnValueOnce({
        data: mockExerciseMuscles,
        isLoading: false,
        error: null,
      })
      .mockReturnValueOnce({
        data: mockMuscles,
        isLoading: false,
        error: null,
      });

    render(
      <MemoryRouter>
        <ExerciseName exerciseName="Bench Press">
          Custom Exercise Name
        </ExerciseName>
      </MemoryRouter>
    );

    expect(screen.getByText('Custom Exercise Name')).toBeInTheDocument();
    expect(screen.queryByText('Bench Press')).not.toBeInTheDocument();
  });

  it('should handle exercise without muscles', async () => {
    mockUseApiGet
      .mockReturnValueOnce({
        data: mockExercise,
        isLoading: false,
        error: null,
      })
      .mockReturnValueOnce({
        data: [],
        isLoading: false,
        error: null,
      })
      .mockReturnValueOnce({
        data: [],
        isLoading: false,
        error: null,
      });

    render(
      <MemoryRouter>
        <ExerciseName exerciseName="Bench Press" />
      </MemoryRouter>
    );

    expect(screen.getByText('Bench Press')).toBeInTheDocument();
  });

  it('should create proper link to exercise details page', async () => {
    mockUseApiGet
      .mockReturnValueOnce({
        data: mockExercise,
        isLoading: false,
        error: null,
      })
      .mockReturnValueOnce({
        data: mockExerciseMuscles,
        isLoading: false,
        error: null,
      })
      .mockReturnValueOnce({
        data: mockMuscles,
        isLoading: false,
        error: null,
      });

    render(
      <MemoryRouter>
        <ExerciseName exerciseName="Bench Press" />
      </MemoryRouter>
    );

    const linkElement = screen.getByRole('link');
    expect(linkElement).toHaveAttribute('href', '/exercises/Bench%20Press');
    expect(linkElement).toHaveStyle('text-decoration: none');
    expect(linkElement).toHaveStyle('color: inherit');
  });

  it('should handle exercise names with special characters in URL', async () => {
    mockUseApiGet
      .mockReturnValueOnce({
        data: mockExercise,
        isLoading: false,
        error: null,
      })
      .mockReturnValueOnce({
        data: mockExerciseMuscles,
        isLoading: false,
        error: null,
      })
      .mockReturnValueOnce({
        data: mockMuscles,
        isLoading: false,
        error: null,
      });

    render(
      <MemoryRouter>
        <ExerciseName exerciseName="Bench Press (Incline)" />
      </MemoryRouter>
    );

    const linkElement = screen.getByRole('link');
    expect(linkElement).toHaveAttribute('href', '/exercises/Bench%20Press%20(Incline)');
  });
});
