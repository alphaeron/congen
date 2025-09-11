import { render, screen } from '@testing-library/react';
import React from 'react';

import { LoadingBackdrop } from './LoadingBackdrop';

describe('LoadingBackdrop', () => {
  const defaultProps = {
    open: true,
    message: 'Loading...',
  };

  it('renders with default props', () => {
    render(<LoadingBackdrop {...defaultProps} />);
    
    expect(screen.getByText('Loading...')).toBeInTheDocument();
  });

  it('renders with custom message', () => {
    render(<LoadingBackdrop {...defaultProps} message="Please wait..." />);
    
    expect(screen.getByText('Please wait...')).toBeInTheDocument();
  });

  it('renders with custom spinner size', () => {
    const { container } = render(<LoadingBackdrop {...defaultProps} spinnerSize={100} />);
    
    // The spinner is rendered via LoadingSpinner component - just check it renders
    const spinner = container.querySelector('.MuiBox-root');
    expect(spinner).toBeInTheDocument();
  });

  it('renders with sub message', () => {
    render(
      <LoadingBackdrop 
        {...defaultProps} 
        message="Loading..." 
        subMessage="This may take a moment" 
      />
    );
    
    expect(screen.getByText('Loading...')).toBeInTheDocument();
    expect(screen.getByText('This may take a moment')).toBeInTheDocument();
  });

  it('renders with all custom props', () => {
    const { container } = render(
      <LoadingBackdrop 
        open={true}
        message="Custom loading message" 
        subMessage="Please be patient"
        spinnerSize={80} 
      />
    );
    
    expect(screen.getByText('Custom loading message')).toBeInTheDocument();
    expect(screen.getByText('Please be patient')).toBeInTheDocument();
    
    // Check that the spinner renders
    const spinner = container.querySelector('.MuiBox-root');
    expect(spinner).toBeInTheDocument();
  });

  it('renders but is hidden when open is false', () => {
    const { container } = render(<LoadingBackdrop {...defaultProps} open={false} />);
    
    // MUI Backdrop still renders but with opacity: 0 and visibility: hidden
    const backdrop = container.querySelector('.MuiBackdrop-root');
    expect(backdrop).toBeInTheDocument();
  });

  it('includes CSS animation keyframes from LoadingSpinner', () => {
    const { container } = render(<LoadingBackdrop {...defaultProps} />);
    
    const styleElement = container.querySelector('style');
    expect(styleElement).toBeInTheDocument();
    expect(styleElement?.textContent).toContain('@keyframes spin');
  });
});
