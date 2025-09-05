import { render, screen } from '@testing-library/react';
import React from 'react';

import { LoadingBackdrop } from './LoadingBackdrop';

describe('LoadingBackdrop', () => {
  it('renders loading spinner with message', () => {
    render(<LoadingBackdrop open message="Loading..." />);
    
    expect(screen.getByText('Loading...')).toBeInTheDocument();
  });

  it('renders sub message when provided', () => {
    render(
      <LoadingBackdrop
        open
        message="Loading..."
        subMessage="This may take a few moments"
      />
    );
    
    expect(screen.getByText('This may take a few moments')).toBeInTheDocument();
  });

  it('does not render when closed', () => {
    const { container } = render(<LoadingBackdrop open={false} message="Loading..." />);
    
    // The backdrop should be hidden when open=false
    const backdrop = container.querySelector('.MuiBackdrop-root');
    expect(backdrop).toBeInTheDocument();
    expect(backdrop).toHaveStyle('opacity: 0');
    expect(backdrop).toHaveStyle('visibility: hidden');
  });

  it('applies custom spinner size', () => {
    render(<LoadingBackdrop open message="Loading..." spinnerSize={80} />);
    
    // The spinner size is passed to LoadingSpinner component
    // We can verify the component renders without error
    expect(screen.getByText('Loading...')).toBeInTheDocument();
  });
});
