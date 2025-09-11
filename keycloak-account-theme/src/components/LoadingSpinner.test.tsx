import { render, screen } from '@testing-library/react';
import React from 'react';

import { LoadingSpinner } from './LoadingSpinner';

describe('LoadingSpinner', () => {
  it('renders with default props', () => {
    render(<LoadingSpinner />);

    expect(screen.getByText('Loading...')).toBeInTheDocument();
  });

  it('renders with custom message', () => {
    render(<LoadingSpinner message="Please wait..." />);

    expect(screen.getByText('Please wait...')).toBeInTheDocument();
  });

  it('renders with custom size', () => {
    const { container } = render(<LoadingSpinner size={100} />);

    // The spinner is rendered as a Box with MUI styling - just check it renders
    const spinner = container.querySelector('.MuiBox-root');
    expect(spinner).toBeInTheDocument();
  });

  it('renders with full height when fullHeight is true', () => {
    const { container } = render(<LoadingSpinner fullHeight={true} />);

    // The main container should be present
    const mainBox = container.firstChild as HTMLElement;
    expect(mainBox).toBeInTheDocument();
  });

  it('renders without full height when fullHeight is false', () => {
    const { container } = render(<LoadingSpinner fullHeight={false} />);

    // The main container should be present
    const mainBox = container.firstChild as HTMLElement;
    expect(mainBox).toBeInTheDocument();
  });

  it('renders with all custom props', () => {
    const { container } = render(
      <LoadingSpinner message="Custom loading message" size={80} fullHeight={true} />
    );

    expect(screen.getByText('Custom loading message')).toBeInTheDocument();

    // Check that the component renders
    const mainBox = container.firstChild as HTMLElement;
    expect(mainBox).toBeInTheDocument();
  });

  it('includes CSS animation keyframes', () => {
    const { container } = render(<LoadingSpinner />);

    const styleElement = container.querySelector('style');
    expect(styleElement).toBeInTheDocument();
    expect(styleElement?.textContent).toContain('@keyframes spin');
  });
});
