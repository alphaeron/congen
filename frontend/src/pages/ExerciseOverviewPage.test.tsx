import { render, screen } from '@testing-library/react';
import * as React from 'react';

import { ExerciseOverviewPage } from './ExerciseOverviewPage';

// Mock the ExerciseOverview component
jest.mock('../components/ExerciseOverview', () => ({
  ExerciseOverview: () => <div data-testid="exercise-overview">Exercise Overview Component</div>,
}));

describe('ExerciseOverviewPage', () => {
  it('should render ExerciseOverview component', () => {
    render(<ExerciseOverviewPage />);

    expect(screen.getByTestId('exercise-overview')).toBeInTheDocument();
    expect(screen.getByText('Exercise Overview Component')).toBeInTheDocument();
  });

  it('should render only the ExerciseOverview component', () => {
    const { container } = render(<ExerciseOverviewPage />);

    // Should only have one child element (the ExerciseOverview component)
    expect(container.firstChild).toBeInTheDocument();
    expect(container.firstChild?.childNodes).toHaveLength(1);
  });

  it('should render consistently across multiple renders', () => {
    const { rerender } = render(<ExerciseOverviewPage />);

    expect(screen.getByTestId('exercise-overview')).toBeInTheDocument();

    rerender(<ExerciseOverviewPage />);

    expect(screen.getByTestId('exercise-overview')).toBeInTheDocument();
    expect(screen.getByText('Exercise Overview Component')).toBeInTheDocument();
  });
});
