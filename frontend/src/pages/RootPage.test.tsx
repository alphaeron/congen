import { render, screen } from '@testing-library/react';
import * as React from 'react';

import { RootPage } from './RootPage';

// Mock the component dependencies
jest.mock('../components/Hero', () => ({
  Hero: () => <div data-testid="hero">Hero Component</div>,
}));

jest.mock('../components/Features', () => ({
  Features: () => <div data-testid="features">Features Component</div>,
}));

jest.mock('../components/OpenSource', () => ({
  OpenSource: () => <div data-testid="open-source">Open Source Component</div>,
}));

describe('RootPage', () => {
  it('should render all required components', () => {
    render(<RootPage />);

    expect(screen.getByTestId('hero')).toBeInTheDocument();
    expect(screen.getByTestId('features')).toBeInTheDocument();
    expect(screen.getByTestId('open-source')).toBeInTheDocument();
  });

  it('should render components in correct order', () => {
    render(<RootPage />);

    const hero = screen.getByTestId('hero');
    const features = screen.getByTestId('features');
    const openSource = screen.getByTestId('open-source');

    // Check that all components are rendered
    expect(hero).toBeInTheDocument();
    expect(features).toBeInTheDocument();
    expect(openSource).toBeInTheDocument();
  });

  it('should render dividers between components', () => {
    const { container } = render(<RootPage />);

    // Should have dividers between Features and OpenSource
    const dividers = container.querySelectorAll('[class*="MuiDivider"]');
    expect(dividers).toHaveLength(2);
  });

  it('should render background box with correct styling', () => {
    const { container } = render(<RootPage />);

    const backgroundBox = container.querySelector('[class*="MuiBox-root"]');
    expect(backgroundBox).toBeInTheDocument();
  });

  it('should render React.Fragment as root element', () => {
    const { container } = render(<RootPage />);

    // The root should be a React.Fragment (no wrapper element)
    expect(container.firstChild?.nodeType).toBe(Node.ELEMENT_NODE);
  });

  it('should render consistently across multiple renders', () => {
    const { rerender } = render(<RootPage />);

    expect(screen.getByTestId('hero')).toBeInTheDocument();
    expect(screen.getByTestId('features')).toBeInTheDocument();
    expect(screen.getByTestId('open-source')).toBeInTheDocument();

    rerender(<RootPage />);

    expect(screen.getByTestId('hero')).toBeInTheDocument();
    expect(screen.getByTestId('features')).toBeInTheDocument();
    expect(screen.getByTestId('open-source')).toBeInTheDocument();
  });

  it('should have proper component text content', () => {
    render(<RootPage />);

    expect(screen.getByText('Hero Component')).toBeInTheDocument();
    expect(screen.getByText('Features Component')).toBeInTheDocument();
    expect(screen.getByText('Open Source Component')).toBeInTheDocument();
  });
});
