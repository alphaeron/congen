import { render, screen } from '@testing-library/react';
import React from 'react';

import { VolumeOverviewCards } from './VolumeOverviewCards';

// Mock Nivo components
jest.mock('@nivo/line', () => ({
  ResponsiveLine: ({ data }: { data: { id: string; data: unknown[] }[] }) => (
    <div data-testid="responsive-line">
      {data.map((line: { id: string; data: unknown[] }, index: number) => (
        <div key={index} data-testid={`line-${index}`}>
          {line.id}: {line.data.length} points
        </div>
      ))}
    </div>
  ),
}));

// Mock framer-motion
jest.mock('framer-motion', () => {
  // eslint-disable-next-line @typescript-eslint/no-require-imports
  const React = require('react');
  return {
    motion: {
      div: ({ children, ...props }) => {
        // Filter out Framer Motion specific props
        const framerMotionProps = new Set([
          'whileHover',
          'whileTap',
          'initial',
          'animate',
          'transition',
          'variants',
        ]);
        const filteredProps = Object.fromEntries(
          Object.entries(props).filter(([key]) => !framerMotionProps.has(key))
        );
        return React.createElement(
          'div',
          { 'data-testid': 'motion-div', ...filteredProps },
          children
        );
      },
    },
  };
});

describe('VolumeOverviewCards', () => {
  const mockUserDataExport = {
    training_programs: [
      {
        workouts: [
          {
            workout: {
              id: 1,
              name: 'Test Workout',
              created_at: '2023-01-01T00:00:00Z',
            },
            stages: [
              {
                stage: { id: 1, name: 'Max Effort' },
                exercises: [
                  {
                    exercise: {
                      id: 1,
                      exercise_name: 'Bench Press',
                      movement_type: 'max_effort',
                    },
                    set_schemes: [
                      {
                        performed_weight: 100,
                        performed_rep_count: 5,
                        target_weight: 100,
                        target_rep_count: 5,
                      },
                    ],
                  },
                ],
              },
            ],
          },
        ],
      },
    ],
  };

  const mockExerciseData = new Map([
    [
      'Bench Press',
      {
        id: 1,
        exercise_name: 'Bench Press',
        movement_type: 'max_effort',
      },
    ],
  ]);

  const defaultProps = {
    userDataExport: mockUserDataExport,
    exerciseData: mockExerciseData,
  };

  it('renders with required props', () => {
    render(<VolumeOverviewCards {...defaultProps} />);

    expect(screen.getByText('Max Effort')).toBeInTheDocument();
    expect(screen.getByText('Dynamic Effort')).toBeInTheDocument();
    expect(screen.getByText('Accessory')).toBeInTheDocument();
  });

  it('renders line charts', () => {
    render(<VolumeOverviewCards {...defaultProps} />);

    // There will be multiple responsive-line elements, so we check that at least one exists
    const responsiveLines = screen.getAllByTestId('responsive-line');
    expect(responsiveLines.length).toBeGreaterThan(0);
  });

  it('renders motion components', () => {
    render(<VolumeOverviewCards {...defaultProps} />);

    const motionDivs = screen.getAllByTestId('motion-div');
    expect(motionDivs.length).toBeGreaterThan(0);
  });

  it('renders with custom height', () => {
    render(<VolumeOverviewCards {...defaultProps} height={400} />);

    expect(screen.getByText('Max Effort')).toBeInTheDocument();
  });

  it('processes workout data correctly', () => {
    render(<VolumeOverviewCards {...defaultProps} />);

    // The component should process the workout data and display it
    // There will be multiple responsive-line elements, so we check that at least one exists
    const responsiveLines = screen.getAllByTestId('responsive-line');
    expect(responsiveLines.length).toBeGreaterThan(0);
  });
});
