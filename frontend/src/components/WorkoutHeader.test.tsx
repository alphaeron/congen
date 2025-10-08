import { render, screen, fireEvent } from '@testing-library/react';
import React from 'react';

import { WorkoutHeader } from './WorkoutHeader';

// Mock framer-motion
jest.mock('framer-motion', () => ({
  motion: {
    div: ({ children, initial, animate, transition, whileHover, whileTap, ...props }: any) => (
      <div data-testid="motion-div" {...props}>{children}</div>
    ),
  },
}));

// Mock MUI components
jest.mock('@mui/material', () => ({
  ...jest.requireActual('@mui/material'),
  Box: ({ children, ...props }: any) => <div data-testid="box" {...props}>{children}</div>,
  IconButton: ({ children, onClick, ...props }: any) => (
    <button data-testid="icon-button" onClick={onClick} {...props}>
      {children}
    </button>
  ),
  Tooltip: ({ children, title }: any) => (
    <div data-testid="tooltip" title={title}>
      {children}
    </div>
  ),
  Button: ({ children, onClick, disabled, ...props }: any) => (
    <button data-testid="button" onClick={onClick} disabled={disabled} {...props}>
      {children}
    </button>
  ),
}));

// Mock MUI icons
jest.mock('@mui/icons-material', () => ({
  Settings: () => <div data-testid="settings-icon">Settings</div>,
  ArrowBack: () => <div data-testid="arrow-back-icon">Back</div>,
}));

// Mock components
jest.mock('./ExportButtons', () => ({
  ExportButtons: ({ onExportPDF, disabled }: any) => (
    <button data-testid="export-buttons" onClick={onExportPDF} disabled={disabled}>
      Export
    </button>
  ),
}));

jest.mock('./ProgressBar', () => ({
  ProgressBar: ({ value, max, showFraction, smooth, animationDuration, ...props }: any) => (
    <div data-testid="progress-bar" data-value={value} data-max={max} {...props}>
      Progress: {value}/{max}
    </div>
  ),
}));

// Mock GameTheme components
jest.mock('./GameTheme', () => ({
  GameCard: ({ children, ...props }: any) => <div data-testid="game-card" {...props}>{children}</div>,
  GameText: ({ children, ...props }: any) => <div data-testid="game-text" {...props}>{children}</div>,
  GAME_CLASSES: {
    subCard: 'sub-card',
    textBold: 'text-bold',
    textMuted: 'text-muted',
    textMedium: 'text-medium',
    button: 'button',
  },
}));

describe('WorkoutHeader', () => {
  const defaultProps = {
    context: 'program' as const,
    onExportPDF: jest.fn(),
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders with program context', () => {
    render(<WorkoutHeader {...defaultProps} currentWeek={1} />);
    
    expect(screen.getByText('Week 1')).toBeInTheDocument();
    expect(screen.getByTestId('progress-bar')).toBeInTheDocument();
  });

  it('renders with week context', () => {
    render(
      <WorkoutHeader
        {...defaultProps}
        context="week"
        weekNumber={2}
        totalWorkouts={5}
        completedWeekWorkouts={3}
      />
    );
    
    expect(screen.getByText('Week 2')).toBeInTheDocument();
    expect(screen.getByText('5 workouts in week')).toBeInTheDocument();
    expect(screen.getByText('3/5 completed')).toBeInTheDocument();
  });

  it('renders with day context', () => {
    render(
      <WorkoutHeader
        {...defaultProps}
        context="day"
        dayNumber={3}
        workoutName="Upper Body"
        totalExercises={8}
        completedExercises={5}
      />
    );
    
    expect(screen.getByText('Day 3')).toBeInTheDocument();
    expect(screen.getByText('Upper Body')).toBeInTheDocument();
    expect(screen.getByText('5/8 exercises')).toBeInTheDocument();
  });

  it('calls onExportPDF when export button is clicked', () => {
    const onExportPDF = jest.fn();
    render(<WorkoutHeader {...defaultProps} onExportPDF={onExportPDF} />);
    
    const exportButton = screen.getByTestId('export-buttons');
    fireEvent.click(exportButton);
    
    expect(onExportPDF).toHaveBeenCalledTimes(1);
  });

  it('calls onSettings when settings button is clicked', () => {
    const onSettings = jest.fn();
    render(<WorkoutHeader {...defaultProps} onSettings={onSettings} />);
    
    const settingsButton = screen.getByTestId('icon-button');
    fireEvent.click(settingsButton);
    
    expect(onSettings).toHaveBeenCalledTimes(1);
  });

  it('calls onBack when back button is clicked in week context', () => {
    const onBack = jest.fn();
    render(
      <WorkoutHeader
        {...defaultProps}
        context="week"
        weekNumber={2}
        onBack={onBack}
      />
    );
    
    const backButton = screen.getByTestId('arrow-back-icon').closest('button');
    expect(backButton).toBeInTheDocument();
    
    if (backButton) {
      fireEvent.click(backButton);
      expect(onBack).toHaveBeenCalledTimes(1);
    }
  });

  it('calls onAddExercise when add exercise button is clicked in day context', () => {
    const onAddExercise = jest.fn();
    render(
      <WorkoutHeader
        {...defaultProps}
        context="day"
        dayNumber={1}
        onAddExercise={onAddExercise}
      />
    );
    
    const addExerciseButton = screen.getByText('Add Exercise');
    fireEvent.click(addExerciseButton);
    
    expect(onAddExercise).toHaveBeenCalledTimes(1);
  });

  it('disables export button when disabled prop is true', () => {
    render(<WorkoutHeader {...defaultProps} disabled={true} />);
    
    const exportButton = screen.getByTestId('export-buttons');
    expect(exportButton).toBeDisabled();
  });

  it('disables add exercise button when saving prop is true', () => {
    render(
      <WorkoutHeader
        {...defaultProps}
        context="day"
        dayNumber={1}
        onAddExercise={jest.fn()}
        saving={true}
      />
    );
    
    const addExerciseButton = screen.getByText('Add Exercise');
    expect(addExerciseButton).toBeDisabled();
  });

  it('calculates progress correctly for program context', () => {
    render(
      <WorkoutHeader
        {...defaultProps}
        context="program"
        currentWeek={2}
        progressValue={1}
        progressMax={4}
      />
    );
    
    const progressBar = screen.getByTestId('progress-bar');
    expect(progressBar).toHaveAttribute('data-value', '1');
    expect(progressBar).toHaveAttribute('data-max', '4');
  });

  it('calculates progress correctly for week context', () => {
    render(
      <WorkoutHeader
        {...defaultProps}
        context="week"
        weekNumber={1}
        totalWorkouts={6}
        completedWeekWorkouts={4}
      />
    );
    
    const progressBar = screen.getByTestId('progress-bar');
    expect(progressBar).toHaveAttribute('data-value', '4');
    expect(progressBar).toHaveAttribute('data-max', '6');
  });

  it('calculates progress correctly for day context', () => {
    render(
      <WorkoutHeader
        {...defaultProps}
        context="day"
        dayNumber={1}
        totalExercises={10}
        completedExercises={7}
      />
    );
    
    const progressBar = screen.getByTestId('progress-bar');
    expect(progressBar).toHaveAttribute('data-value', '7');
    expect(progressBar).toHaveAttribute('data-max', '10');
  });

  it('renders motion components', () => {
    render(<WorkoutHeader {...defaultProps} />);
    
    const motionDivs = screen.getAllByTestId('motion-div');
    expect(motionDivs.length).toBeGreaterThan(0);
  });

  it('shows back button only for week and day contexts', () => {
    const onBack = jest.fn();
    
    // Week context should show back button
    const { rerender } = render(
      <WorkoutHeader
        {...defaultProps}
        context="week"
        weekNumber={1}
        onBack={onBack}
      />
    );
    
    expect(screen.getByTestId('arrow-back-icon')).toBeInTheDocument();
    
    // Day context should show back button
    rerender(
      <WorkoutHeader
        {...defaultProps}
        context="day"
        dayNumber={1}
        onBack={onBack}
      />
    );
    
    expect(screen.getByTestId('arrow-back-icon')).toBeInTheDocument();
    
    // Program context should not show back button
    rerender(<WorkoutHeader {...defaultProps} context="program" onBack={onBack} />);
    
    expect(screen.queryByTestId('arrow-back-icon')).not.toBeInTheDocument();
  });

  it('shows add exercise button only for day context', () => {
    const onAddExercise = jest.fn();
    
    // Day context should show add exercise button
    const { rerender } = render(
      <WorkoutHeader
        {...defaultProps}
        context="day"
        dayNumber={1}
        onAddExercise={onAddExercise}
      />
    );
    
    expect(screen.getByText('Add Exercise')).toBeInTheDocument();
    
    // Week context should not show add exercise button
    rerender(
      <WorkoutHeader
        {...defaultProps}
        context="week"
        weekNumber={1}
        onAddExercise={onAddExercise}
      />
    );
    
    expect(screen.queryByText('Add Exercise')).not.toBeInTheDocument();
  });
});
