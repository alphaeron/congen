import { render, screen } from '@testing-library/react';
import * as React from 'react';

import { ExerciseDetailsPage } from './ExerciseDetailsPage';

// Mock the ExerciseDetails component
jest.mock('../components/ExerciseDetails', () => ({
  ExerciseDetails: ({ exerciseName }: { exerciseName: string }) => (
    <div data-testid="exercise-details">Exercise Details for: {exerciseName}</div>
  ),
}));

// Mock react-router
const mockUseParams = jest.fn();
jest.mock('react-router', () => ({
  ...jest.requireActual('react-router'),
  useParams: () => mockUseParams(),
}));

describe('ExerciseDetailsPage', () => {
  beforeEach(() => {
    mockUseParams.mockClear();
  });

  it('should render ExerciseDetails component when exercise name is provided', () => {
    mockUseParams.mockReturnValue({ exerciseName: 'Bench Press' });

    render(<ExerciseDetailsPage />);

    expect(screen.getByTestId('exercise-details')).toBeInTheDocument();
    expect(screen.getByText('Exercise Details for: Bench Press')).toBeInTheDocument();
  });

  it('should render error alert when no exercise name is provided', () => {
    mockUseParams.mockReturnValue({ exerciseName: undefined });

    render(<ExerciseDetailsPage />);

    expect(screen.getByText('Exercise Not Found')).toBeInTheDocument();
    expect(screen.getByText('No exercise name provided in the URL.')).toBeInTheDocument();
  });

  it('should render error alert when exercise name is empty string', () => {
    mockUseParams.mockReturnValue({ exerciseName: '' });

    render(<ExerciseDetailsPage />);

    expect(screen.getByText('Exercise Not Found')).toBeInTheDocument();
    expect(screen.getByText('No exercise name provided in the URL.')).toBeInTheDocument();
  });

  it('should handle exercise names with special characters', () => {
    mockUseParams.mockReturnValue({ exerciseName: 'Smith Machine Press' });

    render(<ExerciseDetailsPage />);

    expect(screen.getByTestId('exercise-details')).toBeInTheDocument();
    expect(screen.getByText('Exercise Details for: Smith Machine Press')).toBeInTheDocument();
  });

  it('should handle exercise names with numbers', () => {
    mockUseParams.mockReturnValue({ exerciseName: '45 Degree Press' });

    render(<ExerciseDetailsPage />);

    expect(screen.getByTestId('exercise-details')).toBeInTheDocument();
    expect(screen.getByText('Exercise Details for: 45 Degree Press')).toBeInTheDocument();
  });

  it('should handle exercise names with hyphens', () => {
    mockUseParams.mockReturnValue({ exerciseName: 'Dumbbell-Curl' });

    render(<ExerciseDetailsPage />);

    expect(screen.getByTestId('exercise-details')).toBeInTheDocument();
    expect(screen.getByText('Exercise Details for: Dumbbell-Curl')).toBeInTheDocument();
  });

  it('should handle exercise names with underscores', () => {
    mockUseParams.mockReturnValue({ exerciseName: 'Bench_Press' });

    render(<ExerciseDetailsPage />);

    expect(screen.getByTestId('exercise-details')).toBeInTheDocument();
    expect(screen.getByText('Exercise Details for: Bench_Press')).toBeInTheDocument();
  });

  it('should render error alert with correct severity', () => {
    mockUseParams.mockReturnValue({ exerciseName: undefined });

    render(<ExerciseDetailsPage />);

    const alert = screen.getByRole('alert');
    expect(alert).toBeInTheDocument();
    expect(alert).toHaveClass('MuiAlert-standardError');
  });

  it('should render error alert with correct title and message', () => {
    mockUseParams.mockReturnValue({ exerciseName: undefined });

    render(<ExerciseDetailsPage />);

    expect(screen.getByText('Exercise Not Found')).toBeInTheDocument();
    expect(screen.getByText('No exercise name provided in the URL.')).toBeInTheDocument();
  });
});
