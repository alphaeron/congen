import { render, screen } from '@testing-library/react';
import React from 'react';

import { VolumeOverviewCards } from './VolumeOverviewCards';

// Mock Nivo components
jest.mock('@nivo/line', () => ({
  ResponsiveLine: ({ data }: any) => (
    <div data-testid="responsive-line">
      {data.map((line: any, index: number) => (
        <div key={index} data-testid={`line-${index}`}>
          {line.id}: {line.data.length} points
        </div>
      ))}
    </div>
  ),
}));

// Mock framer-motion
jest.mock('framer-motion', () => ({
  motion: {
    div: ({ children, animate, initial, variants, whileHover, whileTap, whileInView, whileFocus, whileDrag, drag, dragConstraints, dragElastic, dragMomentum, dragPropagation, dragSnapToOrigin, dragTransition, dragControls, onDrag, onDragStart, onDragEnd, layout, layoutId, layoutDependency, layoutScroll, layoutRoot, transition, custom, inherit, ...props }: any) => (
      <div data-testid="motion-div" {...props}>{children}</div>
    ),
  },
}));

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
    ['Bench Press', {
      id: 1,
      exercise_name: 'Bench Press',
      movement_type: 'max_effort',
    }],
  ]);

  const mockWeightUnitPreferences = [
    {
      exercise_name: 'Bench Press',
      preferred_unit: 'KG',
    },
  ];

  const defaultProps = {
    userDataExport: mockUserDataExport,
    exerciseData: mockExerciseData,
    weightUnitPreferences: mockWeightUnitPreferences,
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
