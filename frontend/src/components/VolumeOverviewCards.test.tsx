import { render, screen, fireEvent } from '@testing-library/react';
import React from 'react';

import { VolumeOverviewCards } from './VolumeOverviewCards';

jest.mock('@nivo/bullet', () => ({
  ResponsiveBullet: () => <div data-testid="responsive-bullet">Mock Bullet</div>,
}));

jest.mock('@nivo/line', () => ({
  ResponsiveLine: () => <div data-testid="responsive-line">Mock Line</div>,
}));

jest.mock('framer-motion', () => {
  // eslint-disable-next-line @typescript-eslint/no-require-imports
  const React = require('react');
  return {
    motion: {
      div: ({ children, ...props }: { children?: React.ReactNode; [key: string]: unknown }) => {
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
        program: {
          id: 1,
          is_active: true,
          current_week_number: 1,
          name: 'Test Program',
          user_id: 'u1',
          created_at: '2023-01-01T00:00:00Z',
          updated_at: '2023-01-01T00:00:00Z',
        },
        workouts: [
          {
            workout: {
              id: 1,
              program_id: 1,
              day_number: 1,
              name: 'ME Upper',
              created_at: '2023-01-01T00:00:00Z',
              updated_at: '2023-01-01T00:00:00Z',
            },
            stages: [
              {
                stage: {
                  id: 1,
                  programmed_workout_id: 1,
                  name: 'Max Effort',
                  created_at: '2023-01-01T00:00:00Z',
                  updated_at: '2023-01-01T00:00:00Z',
                },
                exercises: [
                  {
                    exercise: {
                      id: 1,
                      workout_stage_id: 1,
                      exercise_name: 'Bench Press',
                      created_at: '2023-01-01T00:00:00Z',
                      updated_at: '2023-01-01T00:00:00Z',
                    },
                    set_schemes: [
                      {
                        id: 1,
                        programmed_exercise_id: 1,
                        set_number: 1,
                        performed_weight: 100,
                        performed_rep_count: 5,
                        target_weight: 100,
                        target_rep_count: 5,
                        created_at: '2023-01-01T00:00:00Z',
                        updated_at: '2023-01-01T00:00:00Z',
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
    user_one_rep_max: [],
  };

  const mockExerciseData = new Map([
    [
      'Bench Press',
      {
        name: 'Bench Press',
        description: 'Press',
        movement_type: 'horizontal_press',
        is_unilateral: false,
        is_upper: true,
        is_accessory: false,
      },
    ],
  ]);

  const defaultProps = {
    userDataExport: mockUserDataExport as never,
    exerciseData: mockExerciseData as never,
    workoutsPerWeek: 4,
    currentWeek: 1,
    preferredUnit: 'LBS' as const,
  };

  it('renders category cards without period controls or summary strip', () => {
    render(<VolumeOverviewCards {...defaultProps} />);

    expect(screen.getByText('Max Effort')).toBeInTheDocument();
    expect(screen.getByText('Dynamic Effort')).toBeInTheDocument();
    expect(screen.getByText('Accessory')).toBeInTheDocument();
    expect(screen.queryByText('This week')).not.toBeInTheDocument();
    expect(screen.queryByText('Last 4 weeks')).not.toBeInTheDocument();
    expect(screen.queryByText('Program')).not.toBeInTheDocument();
    expect(screen.queryByText('Sessions')).not.toBeInTheDocument();
    expect(screen.queryByText('Total volume')).not.toBeInTheDocument();
    expect(screen.queryByText(/Target:/)).not.toBeInTheDocument();
  });

  it('renders bullet charts and header trend sparklines', () => {
    render(<VolumeOverviewCards {...defaultProps} />);

    expect(screen.getAllByTestId('responsive-bullet').length).toBe(3);
    expect(screen.getAllByTestId('volume-trend-sparkline').length).toBe(3);
    expect(screen.getAllByTestId('responsive-line').length).toBe(3);
  });

  it('expands a card to show detailed sparkline trend', () => {
    render(<VolumeOverviewCards {...defaultProps} />);

    fireEvent.click(screen.getByText('Max Effort'));
    expect(screen.getByTestId('volume-sparkline-Max Effort')).toBeInTheDocument();
    expect(screen.getAllByTestId('responsive-line').length).toBe(4);
  });

  it('shows no baseline instead of target delta on week one', () => {
    render(<VolumeOverviewCards {...defaultProps} />);

    expect(screen.getAllByText(/No baseline/).length).toBeGreaterThan(0);
    expect(screen.queryByText(/vs target/)).not.toBeInTheDocument();
    expect(screen.queryByText(/\+100%/)).not.toBeInTheDocument();
  });

  it('shows trend percentage in card header', () => {
    render(<VolumeOverviewCards {...defaultProps} />);

    expect(screen.getAllByText(/↗|↘|—/).length).toBeGreaterThan(0);
    expect(screen.queryByText('Exceeded')).not.toBeInTheDocument();
    expect(screen.queryByText('On track')).not.toBeInTheDocument();
  });
});
