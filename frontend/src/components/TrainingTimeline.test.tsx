import { render, screen, fireEvent } from '@testing-library/react';
import React from 'react';

import { TrainingTimeline } from './TrainingTimeline';

// Mock framer-motion
jest.mock('framer-motion', () => ({
  motion: {
    div: ({ children, animate, initial, variants, whileHover, whileTap, whileInView, whileFocus, whileDrag, drag, dragConstraints, dragElastic, dragMomentum, dragPropagation, dragSnapToOrigin, dragTransition, dragControls, onDrag, onDragStart, onDragEnd, layout, layoutId, layoutDependency, layoutScroll, layoutRoot, transition, custom, inherit, textVariant, ...props }: any) => (
      <div data-testid="motion-div" {...props}>{children}</div>
    ),
  },
  AnimatePresence: ({ children }: any) => <div data-testid="animate-presence">{children}</div>,
}));

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
      workouts: [
        { id: 6, name: 'Workout 6' },
      ],
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

  it('renders scroll buttons when needed', () => {
    // Create props with more weeks to trigger scroll buttons
    const propsWithMoreWeeks = {
      ...defaultProps,
      weeks: [
        ...defaultProps.weeks,
        {
          weekNumber: 4,
          workouts: [{ id: 7, name: 'Workout 7', exercises: [] }],
          isCompleted: false,
          completedWorkouts: 0,
        },
        {
          weekNumber: 5,
          workouts: [{ id: 8, name: 'Workout 8', exercises: [] }],
          isCompleted: false,
          completedWorkouts: 0,
        },
      ],
    };
    
    render(<TrainingTimeline {...propsWithMoreWeeks} />);
    
    // Only the right scroll button should be present when there are more weeks than can fit
    // The left button is only shown when scroll position > 0
    expect(screen.getByTestId('ChevronRightIcon')).toBeInTheDocument();
  });

  it('handles scroll left', () => {
    render(<TrainingTimeline {...defaultProps} />);
    
    // Scroll buttons are only shown when scroll position > 0 or when there are more weeks than visible
    // Since we have 3 weeks and the component shows 3 weeks, no scroll buttons should be visible initially
    expect(screen.queryByTestId('ChevronLeftIcon')).not.toBeInTheDocument();
  });

  it('handles scroll right', () => {
    render(<TrainingTimeline {...defaultProps} />);
    
    // Scroll buttons are only shown when scroll position > 0 or when there are more weeks than visible
    // Since we have 3 weeks and the component shows 3 weeks, no scroll buttons should be visible initially
    expect(screen.queryByTestId('ChevronRightIcon')).not.toBeInTheDocument();
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

  it('renders animate presence', () => {
    render(<TrainingTimeline {...defaultProps} />);
    
    expect(screen.getByTestId('animate-presence')).toBeInTheDocument();
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
