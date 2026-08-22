import React from 'react';
import { render, screen, fireEvent, act } from '@testing-library/react';

import { TrainingTimeline } from './TrainingTimeline';

class ResizeObserverMock {
  observe() {}

  unobserve() {}

  disconnect() {}
}

global.ResizeObserver = ResizeObserverMock;

const createMockWorkout = (id: number, name: string) => ({
  id,
  program_id: 1,
  day_number: id,
  name,
  created_at: new Date('2024-01-01T00:00:00.000Z'),
  updated_at: new Date('2024-01-01T00:00:00.000Z'),
});

const createMockWeek = (
  weekNumber: number,
  workoutCount: number,
  completedWorkoutIndexes: number[] = weekNumber < 2
    ? Array.from({ length: workoutCount }, (_, i) => i)
    : []
) => ({
  weekNumber,
  workouts: Array.from({ length: workoutCount }, (_, index) => ({
    workout: createMockWorkout(weekNumber * 10 + index, `Workout ${weekNumber}-${index + 1}`),
    isCompleted: completedWorkoutIndexes.includes(index),
  })),
  isCompleted: weekNumber < 2,
  completedWorkouts: completedWorkoutIndexes.length,
});

const createMockWeekWorkout = (id: number, name: string, isCompleted: boolean) => ({
  workout: createMockWorkout(id, name),
  isCompleted,
});

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
    AnimatePresence: ({ children }: { children: React.ReactNode }) => (
      <React.Fragment>{children}</React.Fragment>
    ),
  };
});

describe('TrainingTimeline', () => {
  const mockWeeks = [
    {
      weekNumber: 1,
      workouts: [
        createMockWeekWorkout(1, 'Workout 1', true),
        createMockWeekWorkout(2, 'Workout 2', true),
      ],
      isCompleted: true,
      completedWorkouts: 2,
    },
    {
      weekNumber: 2,
      workouts: [
        createMockWeekWorkout(3, 'Workout 3', true),
        createMockWeekWorkout(4, 'Workout 4', false),
        createMockWeekWorkout(5, 'Workout 5', false),
      ],
      isCompleted: false,
      completedWorkouts: 1,
    },
    {
      weekNumber: 3,
      workouts: [createMockWeekWorkout(6, 'Workout 6', false)],
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

    expect(screen.getByText('completed')).toBeInTheDocument();
    expect(screen.getByText('current')).toBeInTheDocument();
    expect(screen.getByText('future')).toBeInTheDocument();
  });

  it('marks week 1 as current when later weeks exist but none are completed', () => {
    const manyIncompleteWeeks = Array.from({ length: 5 }, (_, index) => ({
      weekNumber: index + 1,
      workouts: [createMockWeekWorkout(index + 1, `Workout ${index + 1}`, false)],
      isCompleted: false,
      completedWorkouts: 0,
    }));

    render(
      <TrainingTimeline weeks={manyIncompleteWeeks} onWeekClick={jest.fn()} currentWeek={1} />
    );

    expect(screen.getAllByText('current')).toHaveLength(1);
    expect(screen.getByText('Week 1').closest('[data-week-number="1"]')).toBeInTheDocument();
    expect(screen.getAllByText('future')).toHaveLength(4);
  });

  it('marks week 2 as current when week 1 is completed', () => {
    const weeks = [
      {
        weekNumber: 1,
        workouts: [createMockWeekWorkout(1, 'Workout 1', true)],
        isCompleted: true,
        completedWorkouts: 1,
      },
      {
        weekNumber: 2,
        workouts: [createMockWeekWorkout(2, 'Workout 2', false)],
        isCompleted: false,
        completedWorkouts: 0,
      },
    ];

    render(<TrainingTimeline weeks={weeks} onWeekClick={jest.fn()} currentWeek={2} />);

    expect(screen.getByText('completed')).toBeInTheDocument();
    expect(screen.getByText('current')).toBeInTheDocument();
    expect(screen.queryByText('future')).not.toBeInTheDocument();
  });

  it('marks completion dots based on individual workout completion', () => {
    const weeks = [
      {
        weekNumber: 2,
        workouts: [
          createMockWeekWorkout(1, 'ME Upper', false),
          createMockWeekWorkout(2, 'DE Lower', true),
          createMockWeekWorkout(3, 'ME Lower', false),
        ],
        isCompleted: false,
        completedWorkouts: 1,
      },
    ];

    render(<TrainingTimeline weeks={weeks} onWeekClick={jest.fn()} currentWeek={2} />);

    expect(screen.getByTestId('workout-completion-dot-1')).toHaveAttribute(
      'data-completed',
      'false'
    );
    expect(screen.getByTestId('workout-completion-dot-2')).toHaveAttribute(
      'data-completed',
      'true'
    );
    expect(screen.getByTestId('workout-completion-dot-3')).toHaveAttribute(
      'data-completed',
      'false'
    );
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

    expect(screen.getByText('Workout 1')).toBeInTheDocument();
    expect(screen.getByText('Workout 2')).toBeInTheDocument();
    expect(screen.getByText('Workout 3')).toBeInTheDocument();
    expect(screen.getByText('Workout 4')).toBeInTheDocument();
    expect(screen.getByText('Workout 5')).toBeInTheDocument();
    expect(screen.getByText('Workout 6')).toBeInTheDocument();
  });

  it('displays adherence density and status hints when provided', () => {
    render(
      <TrainingTimeline
        {...defaultProps}
        weeks={[
          {
            weekNumber: 1,
            workouts: [
              createMockWeekWorkout(1, 'ME Upper', true),
              createMockWeekWorkout(2, 'DE Lower', false),
            ],
            isCompleted: false,
            completedWorkouts: 1,
            plannedWorkouts: 2,
            totalVolume: 5000,
            statusHint: '1 of 2 sessions · No ME volume',
          },
        ]}
      />
    );

    expect(screen.getByTestId('week-adherence-1')).toBeInTheDocument();
    expect(screen.getByTestId('week-volume-1')).toBeInTheDocument();
    expect(screen.getByText('1 of 2 sessions · No ME volume')).toBeInTheDocument();
  });

  it('does not show scroll arrows when all weeks fit in the container', () => {
    render(<TrainingTimeline {...defaultProps} />);

    const scrollContainer = screen.getByTestId('timeline-scroll-container');
    Object.defineProperty(scrollContainer, 'clientWidth', {
      configurable: true,
      value: 2000,
    });
    Object.defineProperty(scrollContainer, 'scrollWidth', {
      configurable: true,
      value: 2000,
    });
    Object.defineProperty(scrollContainer, 'scrollLeft', {
      configurable: true,
      value: 0,
      writable: true,
    });

    act(() => {
      fireEvent.scroll(scrollContainer);
    });

    expect(screen.queryByTestId('timeline-scroll-left')).not.toBeInTheDocument();
    expect(screen.queryByTestId('timeline-scroll-right')).not.toBeInTheDocument();
  });

  it('shows scroll arrows when timeline content overflows the container', () => {
    const manyWeeks = Array.from({ length: 10 }, (_, index) => createMockWeek(index + 1, 1));

    render(<TrainingTimeline weeks={manyWeeks} onWeekClick={jest.fn()} currentWeek={5} />);

    const scrollContainer = screen.getByTestId('timeline-scroll-container');
    Object.defineProperty(scrollContainer, 'clientWidth', {
      configurable: true,
      value: 800,
    });
    Object.defineProperty(scrollContainer, 'scrollWidth', {
      configurable: true,
      value: 2000,
    });
    Object.defineProperty(scrollContainer, 'scrollLeft', {
      configurable: true,
      value: 0,
      writable: true,
    });

    act(() => {
      fireEvent.scroll(scrollContainer);
    });

    expect(screen.getByTestId('timeline-scroll-right')).toBeInTheDocument();
    expect(screen.queryByTestId('timeline-scroll-left')).not.toBeInTheDocument();
  });

  it('scrolls timeline content when arrow buttons are clicked', () => {
    const manyWeeks = Array.from({ length: 10 }, (_, index) => createMockWeek(index + 1, 1));

    render(<TrainingTimeline weeks={manyWeeks} onWeekClick={jest.fn()} currentWeek={5} />);

    const scrollContainer = screen.getByTestId('timeline-scroll-container');
    const scrollBy = jest.fn();
    Object.defineProperty(scrollContainer, 'clientWidth', {
      configurable: true,
      value: 800,
    });
    Object.defineProperty(scrollContainer, 'scrollWidth', {
      configurable: true,
      value: 2000,
    });
    Object.defineProperty(scrollContainer, 'scrollLeft', {
      configurable: true,
      value: 0,
      writable: true,
    });
    scrollContainer.scrollBy = scrollBy;

    act(() => {
      fireEvent.scroll(scrollContainer);
    });

    fireEvent.click(screen.getByTestId('timeline-scroll-right'));

    expect(scrollBy).toHaveBeenCalledWith({
      left: 600,
      behavior: 'smooth',
    });
  });
});
