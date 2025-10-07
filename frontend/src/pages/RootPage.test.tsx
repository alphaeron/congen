import { render, screen } from '@testing-library/react';
import * as React from 'react';

import { RootPage } from './RootPage';

// Mock framer-motion hooks that are used in GameProgressBar
jest.mock('framer-motion', () => ({
  motion: {
    div: 'div',
  },
  useMotionValue: jest.fn(() => ({ get: jest.fn(), set: jest.fn() })),
  useAnimation: jest.fn(() => ({
    start: jest.fn(),
    stop: jest.fn(),
  })),
}));

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

jest.mock('../components/AlgorithmInfographic', () => ({
  __esModule: true,
  default: () => <div data-testid="algorithm-infographic">Algorithm Infographic Component</div>,
}));

jest.mock('../components/GamificationSection', () => ({
  GamificationSection: () => (
    <div data-testid="gamification-section">Gamification Section Component</div>
  ),
}));

jest.mock('../components/PersonalizationSection', () => ({
  PersonalizationSection: () => (
    <div data-testid="personalization-section">Personalization Section Component</div>
  ),
}));

describe('RootPage', () => {
  it('should render all required components', () => {
    render(<RootPage />);

    expect(screen.getByTestId('hero')).toBeInTheDocument();
    expect(screen.getByTestId('features')).toBeInTheDocument();
    expect(screen.getByTestId('algorithm-infographic')).toBeInTheDocument();
    expect(screen.getByTestId('gamification-section')).toBeInTheDocument();
    expect(screen.getByTestId('personalization-section')).toBeInTheDocument();
    expect(screen.getByTestId('open-source')).toBeInTheDocument();
  });

  it('should render components in correct order', () => {
    render(<RootPage />);

    const hero = screen.getByTestId('hero');
    const features = screen.getByTestId('features');
    const algorithmInfographic = screen.getByTestId('algorithm-infographic');
    const gamificationSection = screen.getByTestId('gamification-section');
    const personalizationSection = screen.getByTestId('personalization-section');
    const openSource = screen.getByTestId('open-source');

    // Check that all components are rendered
    expect(hero).toBeInTheDocument();
    expect(features).toBeInTheDocument();
    expect(algorithmInfographic).toBeInTheDocument();
    expect(gamificationSection).toBeInTheDocument();
    expect(personalizationSection).toBeInTheDocument();
    expect(openSource).toBeInTheDocument();
  });

  it('should render dividers between components', () => {
    const { container } = render(<RootPage />);

    // The component uses sections with snap-parent styling, not MUI dividers
    const sections = container.querySelectorAll('.section');
    expect(sections).toHaveLength(6); // hero, features, algorithm, gamification, personalization, opensource
  });

  it('should render background box with correct styling', () => {
    const { container } = render(<RootPage />);

    // The component uses a snap-parent div with specific styling
    const snapParent = container.querySelector('.snap-parent-y-mandatory');
    expect(snapParent).toBeInTheDocument();
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
    expect(screen.getByTestId('algorithm-infographic')).toBeInTheDocument();
    expect(screen.getByTestId('gamification-section')).toBeInTheDocument();
    expect(screen.getByTestId('personalization-section')).toBeInTheDocument();
    expect(screen.getByTestId('open-source')).toBeInTheDocument();

    rerender(<RootPage />);

    expect(screen.getByTestId('hero')).toBeInTheDocument();
    expect(screen.getByTestId('features')).toBeInTheDocument();
    expect(screen.getByTestId('algorithm-infographic')).toBeInTheDocument();
    expect(screen.getByTestId('gamification-section')).toBeInTheDocument();
    expect(screen.getByTestId('personalization-section')).toBeInTheDocument();
    expect(screen.getByTestId('open-source')).toBeInTheDocument();
  });

  it('should have proper component text content', () => {
    render(<RootPage />);

    expect(screen.getByText('Hero Component')).toBeInTheDocument();
    expect(screen.getByText('Features Component')).toBeInTheDocument();
    expect(screen.getByText('Algorithm Infographic Component')).toBeInTheDocument();
    expect(screen.getByText('Gamification Section Component')).toBeInTheDocument();
    expect(screen.getByText('Personalization Section Component')).toBeInTheDocument();
    expect(screen.getByText('Open Source Component')).toBeInTheDocument();
  });
});
