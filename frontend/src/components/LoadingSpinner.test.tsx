import { render, screen } from '@testing-library/react';
import React from 'react';

import { LoadingSpinner } from './LoadingSpinner';

describe('LoadingSpinner', () => {
  it('renders with default props', () => {
    render(<LoadingSpinner />);

    // The component renders a custom spinner, not a progressbar role
    expect(screen.getByText('Loading...')).toBeInTheDocument();
  });

  it('renders with custom message', () => {
    render(<LoadingSpinner message="Custom loading message" />);

    expect(screen.getByText('Custom loading message')).toBeInTheDocument();
  });

  it('renders without message when message is empty', () => {
    render(<LoadingSpinner message="" />);

    expect(screen.queryByText('Loading...')).not.toBeInTheDocument();
  });

  it('applies fullHeight class when fullHeight is true', () => {
    render(<LoadingSpinner fullHeight />);

    // Get the container div that has the height style
    const container = screen.getByText('Loading...').closest('div');
    expect(container).toHaveStyle({ height: '100vh' });
  });

  it('applies default height when fullHeight is false', () => {
    render(<LoadingSpinner fullHeight={false} />);

    // Get the container div that has the height style
    const container = screen.getByText('Loading...').closest('div');
    expect(container).toHaveStyle({ height: 'auto' });
  });
});
