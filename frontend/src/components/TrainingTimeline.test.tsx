import { render, screen, fireEvent } from '@testing-library/react';
import React from 'react';

import { TrainingTimeline } from './TrainingTimeline';

// Mock framer-motion
jest.mock('framer-motion', () => {
  // eslint-disable-next-line @typescript-eslint/no-require-imports
  const React = require('react');
  return {
    motion: {
      div: ({ children, ...props }) => {
        // Filter out Framer Motion specific props
        const framerMotionProps = new Set([
          'initial',
          'animate',
          'transition',
          'whileHover',
          'whileTap',
          'whileFocus',
          'whileInView',
          'exit',
          'variants',
          'custom',
          'inherit',
          'layout',
          'layoutId',
          'layoutDependency',
          'layoutScroll',
          'layoutRoot',
          'drag',
          'dragConstraints',
          'dragElastic',
          'dragMomentum',
          'dragPropagation',
          'dragSnapToOrigin',
          'dragTransition',
          'dragControls',
          'onDrag',
          'onDragStart',
          'onDragEnd',
          'onAnimationStart',
          'onAnimationComplete',
          'onUpdate',
          'onTap',
          'onTapStart',
          'onTapCancel',
          'onHoverStart',
          'onHoverEnd',
          'onFocus',
          'onBlur',
          'onPan',
          'onPanStart',
          'onPanEnd',
          'onViewportEnter',
          'onViewportLeave',
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
      span: ({ children, ...props }) => {
        // Filter out Framer Motion specific props
        const framerMotionProps = new Set([
          'initial',
          'animate',
          'transition',
          'whileHover',
          'whileTap',
          'whileFocus',
          'whileInView',
          'exit',
          'variants',
          'custom',
          'inherit',
          'layout',
          'layoutId',
          'layoutDependency',
          'layoutScroll',
          'layoutRoot',
          'drag',
          'dragConstraints',
          'dragElastic',
          'dragMomentum',
          'dragPropagation',
          'dragSnapToOrigin',
          'dragTransition',
          'dragControls',
          'onDrag',
          'onDragStart',
          'onDragEnd',
          'onAnimationStart',
          'onAnimationComplete',
          'onUpdate',
          'onTap',
          'onTapStart',
          'onTapCancel',
          'onHoverStart',
          'onHoverEnd',
          'onFocus',
          'onBlur',
          'onPan',
          'onPanStart',
          'onPanEnd',
          'onViewportEnter',
          'onViewportLeave',
        ]);

        const filteredProps = Object.fromEntries(
          Object.entries(props).filter(([key]) => !framerMotionProps.has(key))
        );

        return React.createElement(
          'span',
          { 'data-testid': 'motion-span', ...filteredProps },
          children
        );
      },
    },
    AnimatePresence: () => 'div',
  };
});

describe('TrainingTimeline', () => {
  const mockWeeks = [
    {
      weekNumber: 1,
      workouts: [
        { id: 1, name: 'Workout 1' },
        { id: 2, name: 'Workout 2' },
      ],
      isCompleted: true,
      completedWorkouts: 2,
    },
    {
      weekNumber: 2,
      workouts: [
        { id: 3, name: 'Workout 3' },
        { id: 4, name: 'Workout 4' },
        { id: 5, name: 'Workout 5' },
      ],
      isCompleted: false,
      completedWorkouts: 1,
    },
    {
      weekNumber: 3,
      workouts: [{ id: 6, name: 'Workout 6' }],
      isCompleted: false,
      completedWorkouts: 0,
    },
  ];

  const defaultProps = {
    weeks: mockWeeks,
    onWeekClick: jest.fn(),
    currentWeek: 2,
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders with required props', () => {
    render(<TrainingTimeline {...defaultProps} />);

    expect(screen.getByText('Week 1')).toBeInTheDocument();
    expect(screen.getByText('Week 2')).toBeInTheDocument();
    expect(screen.getByText('Week 3')).toBeInTheDocument();
  });

  it('calls onWeekClick when a week is clicked', () => {
    const onWeekClick = jest.fn();
    render(<TrainingTimeline {...defaultProps} onWeekClick={onWeekClick} />);

    const week1 = screen.getByText('Week 1');
    fireEvent.click(week1);

    expect(onWeekClick).toHaveBeenCalledWith(1);
  });

  it('displays correct week status indicators', () => {
    render(<TrainingTimeline {...defaultProps} />);

    // Week 1 should be completed (green)
    // Week 2 should be current (blue)
    // Week 3 should be future (amber)
    expect(screen.getByText('completed')).toBeInTheDocument();
    expect(screen.getByText('current')).toBeInTheDocument();
    expect(screen.getByText('future')).toBeInTheDocument();
  });

  it('displays completion dots for each week', () => {
    render(<TrainingTimeline {...defaultProps} />);

    // Each week should have dots representing workouts
    const motionDivs = screen.getAllByTestId('motion-div');
    expect(motionDivs.length).toBeGreaterThan(0);
  });

  it('sorts weeks in descending order', () => {
    render(<TrainingTimeline {...defaultProps} />);

    // Weeks should be displayed in descending order (3, 2, 1)
    const weekElements = screen.getAllByText(/Week \d+/);
    expect(weekElements[0]).toHaveTextContent('Week 3');
    expect(weekElements[1]).toHaveTextContent('Week 2');
    expect(weekElements[2]).toHaveTextContent('Week 1');
  });

  it('renders motion components', () => {
    render(<TrainingTimeline {...defaultProps} />);

    const motionDivs = screen.getAllByTestId('motion-div');
    expect(motionDivs.length).toBeGreaterThan(0);
  });

  it('displays workout count for each week', () => {
    render(<TrainingTimeline {...defaultProps} />);

    // Should show workout names for each week
    expect(screen.getByText('Workout 1')).toBeInTheDocument(); // Week 1 has 2 workouts
    expect(screen.getByText('Workout 2')).toBeInTheDocument(); // Week 1 has 2 workouts
    expect(screen.getByText('Workout 3')).toBeInTheDocument(); // Week 2 has 3 workouts
    expect(screen.getByText('Workout 4')).toBeInTheDocument(); // Week 2 has 3 workouts
    expect(screen.getByText('Workout 5')).toBeInTheDocument(); // Week 2 has 3 workouts
    expect(screen.getByText('Workout 6')).toBeInTheDocument(); // Week 3 has 1 workout
  });
});
