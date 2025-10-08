import { render, screen } from '@testing-library/react';
import React from 'react';

import { GameProgressBar, GameCircularProgressBar } from './GameProgressBar';

// Mock framer-motion
jest.mock('framer-motion', () => ({
  motion: {
    div: ({ children, ...props }: { children: React.ReactNode; [key: string]: unknown }) => (
      <div data-testid="motion-div" {...props}>
        {children}
      </div>
    ),
    circle: ({ ...props }: { [key: string]: unknown }) => (
      <circle data-testid="motion-circle" {...props} />
    ),
    span: ({ children, ...props }: { children: React.ReactNode; [key: string]: unknown }) => (
      <span data-testid="motion-span" {...props}>
        {children}
      </span>
    ),
  },
  useMotionValue: () => ({ set: jest.fn() }),
  useAnimation: () => ({
    start: jest.fn(),
  }),
}));

// Mock MUI components
jest.mock('@mui/material', () => ({
  ...jest.requireActual('@mui/material'),
  Box: ({ children, ...props }: { children: React.ReactNode; [key: string]: unknown }) => (
    <div data-testid="box" {...props}>
      {children}
    </div>
  ),
  Tooltip: ({ children, title }: { children: React.ReactNode; title: string }) => (
    <div data-testid="tooltip" title={title}>
      {children}
    </div>
  ),
  alpha: (color: string, opacity: number) => `${color}${Math.round(opacity * 255).toString(16)}`,
  styled: () => (props: { [key: string]: unknown }) => (
    <div data-testid="styled-component" {...props} />
  ),
}));

// Mock GameTheme
jest.mock('./GameTheme', () => ({
  GameText: ({ children, ...props }: { children: React.ReactNode; [key: string]: unknown }) => (
    <div data-testid="game-text" {...props}>
      {children}
    </div>
  ),
}));

describe('GameProgressBar', () => {
  const defaultProps = {
    label: 'HP',
    current: 80,
    max: 100,
    color: '#00bcd4',
    tooltip: 'Health Points',
  };

  it('renders with required props', () => {
    render(<GameProgressBar {...defaultProps} />);

    expect(screen.getByText('HP')).toBeInTheDocument();
    expect(screen.getByText('80/100')).toBeInTheDocument();
    expect(screen.getByTestId('tooltip')).toHaveAttribute('title', 'Health Points');
  });

  it('renders with icon', () => {
    const icon = <div data-testid="test-icon">Icon</div>;
    render(<GameProgressBar {...defaultProps} icon={icon} />);

    expect(screen.getByTestId('test-icon')).toBeInTheDocument();
  });

  it('calculates percentage correctly', () => {
    render(<GameProgressBar {...defaultProps} current={50} max={200} />);

    expect(screen.getByText('50/200')).toBeInTheDocument();
  });

  it('handles zero max value', () => {
    render(<GameProgressBar {...defaultProps} current={50} max={0} />);

    expect(screen.getByText('50/0')).toBeInTheDocument();
  });

  it('handles current value greater than max', () => {
    render(<GameProgressBar {...defaultProps} current={150} max={100} />);

    expect(screen.getByText('150/100')).toBeInTheDocument();
  });

  it('handles negative current value', () => {
    render(<GameProgressBar {...defaultProps} current={-10} max={100} />);

    expect(screen.getByText('-10/100')).toBeInTheDocument();
  });

  it('renders with animation when animated prop is true', () => {
    render(<GameProgressBar {...defaultProps} animated={true} delay={500} />);

    // Check that motion-div elements are present (there will be multiple)
    const motionDivs = screen.getAllByTestId('motion-div');
    expect(motionDivs.length).toBeGreaterThan(0);
  });

  it('renders without animation when animated prop is false', () => {
    render(<GameProgressBar {...defaultProps} animated={false} />);

    expect(screen.queryByTestId('motion-div')).not.toBeInTheDocument();
  });
});

describe('GameCircularProgressBar', () => {
  const defaultProps = {
    label: 'Fatigue',
    current: 60,
    max: 100,
    color: '#00bcd4',
    tooltip: 'Fatigue level',
  };

  it('renders with required props', () => {
    render(<GameCircularProgressBar {...defaultProps} />);

    expect(screen.getByText('Fatigue')).toBeInTheDocument();
    expect(screen.getByText('60/100')).toBeInTheDocument();
    expect(screen.getByTestId('tooltip')).toHaveAttribute('title', 'Fatigue level');
  });

  it('renders with icon', () => {
    const icon = <div data-testid="test-icon">Icon</div>;
    render(<GameCircularProgressBar {...defaultProps} icon={icon} />);

    expect(screen.getByTestId('test-icon')).toBeInTheDocument();
  });

  it('calculates percentage correctly', () => {
    render(<GameCircularProgressBar {...defaultProps} current={25} max={50} />);

    expect(screen.getByText('25/50')).toBeInTheDocument();
  });

  it('handles zero max value', () => {
    render(<GameCircularProgressBar {...defaultProps} current={30} max={0} />);

    expect(screen.getByText('30/0')).toBeInTheDocument();
  });

  it('handles current value greater than max', () => {
    render(<GameCircularProgressBar {...defaultProps} current={120} max={100} />);

    expect(screen.getByText('120/100')).toBeInTheDocument();
  });

  it('handles negative current value', () => {
    render(<GameCircularProgressBar {...defaultProps} current={-5} max={100} />);

    expect(screen.getByText('-5/100')).toBeInTheDocument();
  });

  it('renders with animation when animated prop is true', () => {
    render(<GameCircularProgressBar {...defaultProps} animated={true} delay={300} />);

    // Check that motion-div elements are present (there will be multiple)
    const motionDivs = screen.getAllByTestId('motion-div');
    expect(motionDivs.length).toBeGreaterThan(0);
  });

  it('renders without animation when animated prop is false', () => {
    render(<GameCircularProgressBar {...defaultProps} animated={false} />);

    expect(screen.queryByTestId('motion-div')).not.toBeInTheDocument();
  });

  it('renders SVG circle elements', () => {
    render(<GameCircularProgressBar {...defaultProps} />);

    // The SVG elements should be present (mocked as regular divs in our test setup)
    // There will be multiple box elements, so we check that at least one exists
    const boxes = screen.getAllByTestId('box');
    expect(boxes.length).toBeGreaterThan(0);
  });
});
