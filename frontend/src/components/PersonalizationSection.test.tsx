import { render, screen } from '@testing-library/react';
import React from 'react';

import { PersonalizationSection } from './PersonalizationSection';

// Mock framer-motion
jest.mock('framer-motion', () => ({
  motion: {
    div: ({ children, animate, initial, variants, whileHover, whileTap, whileInView, whileFocus, whileDrag, drag, dragConstraints, dragElastic, dragMomentum, dragPropagation, dragSnapToOrigin, dragTransition, dragControls, onDrag, onDragStart, onDragEnd, layout, layoutId, layoutDependency, layoutScroll, layoutRoot, transition, custom, inherit, textVariant, ...props }: any) => (
      <div data-testid="motion-div" {...props}>{children}</div>
    ),
  },
  useInView: () => true,
}));

// Mock CycleDiagramReact
jest.mock('./CycleDiagramReact', () => ({
  CycleDiagramReact: ({ nodes }: any) => (
    <div data-testid="cycle-diagram">
      {nodes.map((node: any) => (
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
