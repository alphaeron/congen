import { render, screen } from '@testing-library/react';
import React from 'react';

import { PersonalizationSection } from './PersonalizationSection';

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
    useInView: () => true,
  };
});

// Mock CycleDiagramReact
jest.mock('./CycleDiagramReact', () => ({
  CycleDiagramReact: ({ nodes }: { nodes: { id: string; label: string }[] }) => (
    <div data-testid="cycle-diagram">
      {nodes.map((node: { id: string; label: string }) => (
        <div key={node.id} data-testid={`cycle-node-${node.id}`}>
          {node.label}
        </div>
      ))}
    </div>
  ),
}));

describe('PersonalizationSection', () => {
  it('renders the personalization section', () => {
    render(<PersonalizationSection />);

    // Check that motion-div elements are present (there will be multiple)
    const motionDivs = screen.getAllByTestId('motion-div');
    expect(motionDivs.length).toBeGreaterThan(0);
  });

  it('renders the cycle diagram with personalization nodes', () => {
    render(<PersonalizationSection />);

    expect(screen.getByTestId('cycle-diagram')).toBeInTheDocument();
    expect(screen.getByTestId('cycle-node-generation')).toBeInTheDocument();
    expect(screen.getByTestId('cycle-node-personalization')).toBeInTheDocument();
    expect(screen.getByTestId('cycle-node-tracking')).toBeInTheDocument();
    expect(screen.getByTestId('cycle-node-performance')).toBeInTheDocument();
  });

  it('displays correct node labels', () => {
    render(<PersonalizationSection />);

    expect(screen.getByText('Generate Workouts')).toBeInTheDocument();
    expect(screen.getByText('Workout Personalization')).toBeInTheDocument();
    expect(screen.getByText('Workout Tracking')).toBeInTheDocument();
    expect(screen.getByText('Performance Tracking')).toBeInTheDocument();
  });

  it('renders motion components', () => {
    render(<PersonalizationSection />);

    const motionDivs = screen.getAllByTestId('motion-div');
    expect(motionDivs.length).toBeGreaterThan(0);
  });

  it('renders box containers', () => {
    render(<PersonalizationSection />);

    // Check that the main content is rendered (the component structure)
    expect(screen.getByText('Smart Personalization')).toBeInTheDocument();
    expect(screen.getByText('Equipment Matching')).toBeInTheDocument();
    expect(screen.getByText('Time Optimization')).toBeInTheDocument();
  });
});
