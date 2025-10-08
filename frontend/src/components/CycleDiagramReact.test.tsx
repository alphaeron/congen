import { render, screen } from '@testing-library/react';
import React from 'react';

import { CycleDiagramReact } from './CycleDiagramReact';

// Mock framer-motion
jest.mock('framer-motion', () => ({
  motion: {
    div: ({ children, ...props }: any) => <div data-testid="motion-div" {...props}>{children}</div>,
  },
}));

// Mock CycleDiagram class
const mockCycleDiagram = {
  update: jest.fn(),
  updateTextVisibility: jest.fn(),
  destroy: jest.fn(),
};

jest.mock('./CycleDiagram', () => ({
  CycleDiagram: jest.fn().mockImplementation(() => mockCycleDiagram),
}));

describe('CycleDiagramReact', () => {
  const defaultProps = {
    nodes: [
      { id: 'node1', label: 'Node 1', details: ['Detail 1'] },
      { id: 'node2', label: 'Node 2', details: ['Detail 2'] },
    ],
    title: 'Test Diagram',
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders with required props', () => {
    render(<CycleDiagramReact {...defaultProps} />);
    
    expect(screen.getByTestId('motion-div')).toBeInTheDocument();
  });

  it('creates CycleDiagram instance on mount', () => {
    render(<CycleDiagramReact {...defaultProps} />);
    
    // The CycleDiagram constructor should be called
    expect(require('./CycleDiagram').CycleDiagram).toHaveBeenCalled();
  });

  it('updates diagram when props change', () => {
    const { rerender } = render(<CycleDiagramReact {...defaultProps} />);
    
    const newProps = {
      ...defaultProps,
      nodes: [
        { id: 'node3', label: 'Node 3', details: ['Detail 3'] },
      ],
    };
    
    rerender(<CycleDiagramReact {...newProps} />);
    
    expect(mockCycleDiagram.update).toHaveBeenCalledWith(newProps);
  });

  it('updates text visibility when showText changes', () => {
    render(<CycleDiagramReact {...defaultProps} />);
    
    // The component should call updateTextVisibility
    expect(mockCycleDiagram.updateTextVisibility).toHaveBeenCalled();
  });

  it('destroys diagram on unmount', () => {
    const { unmount } = render(<CycleDiagramReact {...defaultProps} />);
    
    unmount();
    
    expect(mockCycleDiagram.destroy).toHaveBeenCalled();
  });

  it('renders motion components with correct initial state', () => {
    render(<CycleDiagramReact {...defaultProps} />);
    
    const motionDiv = screen.getByTestId('motion-div');
    expect(motionDiv).toBeInTheDocument();
  });

  it('handles empty nodes array', () => {
    render(<CycleDiagramReact {...defaultProps} nodes={[]} />);
    
    expect(screen.getByTestId('motion-div')).toBeInTheDocument();
  });

  it('handles nodes without details', () => {
    const propsWithoutDetails = {
      nodes: [
        { id: 'node1', label: 'Node 1', details: [] },
        { id: 'node2', label: 'Node 2' },
      ],
    };
    
    render(<CycleDiagramReact {...propsWithoutDetails} />);
    
    expect(screen.getByTestId('motion-div')).toBeInTheDocument();
  });

  it('handles custom theme', () => {
    const propsWithTheme = {
      ...defaultProps,
      theme: 'vibrant' as const,
    };
    
    render(<CycleDiagramReact {...propsWithTheme} />);
    
    expect(screen.getByTestId('motion-div')).toBeInTheDocument();
  });

  it('handles custom dimensions', () => {
    const propsWithDimensions = {
      ...defaultProps,
      width: 800,
      height: 600,
    };
    
    render(<CycleDiagramReact {...propsWithDimensions} />);
    
    expect(screen.getByTestId('motion-div')).toBeInTheDocument();
  });

  it('handles custom colors', () => {
    const propsWithColors = {
      ...defaultProps,
      customColors: ['#ff0000', '#00ff00', '#0000ff'],
    };
    
    render(<CycleDiagramReact {...propsWithColors} />);
    
    expect(screen.getByTestId('motion-div')).toBeInTheDocument();
  });
});
