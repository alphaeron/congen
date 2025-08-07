import { render, screen } from '@testing-library/react';
import React from 'react';

import { LoadingSpinner } from './LoadingSpinner';

describe('LoadingSpinner', () => {
  it('renders with default props', () => {
    render(<LoadingSpinner />);

    expect(screen.getByRole('progressbar')).toBeInTheDocument();
    expect(screen.getByText('Loading...')).toBeInTheDocument();
  });

  it('renders with custom message', () => {
    render(<LoadingSpinner message="Custom loading message" />);

    expect(screen.getByRole('progressbar')).toBeInTheDocument();
    expect(screen.getByText('Custom loading message')).toBeInTheDocument();
  });

  it('renders without message when message is empty', () => {
    render(<LoadingSpinner message="" />);

    expect(screen.getByRole('progressbar')).toBeInTheDocument();
    expect(screen.queryByText('Loading...')).not.toBeInTheDocument();
  });

  it('applies fullHeight class when fullHeight is true', () => {
    render(<LoadingSpinner fullHeight />);

    const container = screen.getByRole('progressbar').closest('div');
    expect(container).toHaveStyle({ height: '100vh' });
  });

  it('applies default height when fullHeight is false', () => {
    render(<LoadingSpinner fullHeight={false} />);

    const container = screen.getByRole('progressbar').closest('div');
    expect(container).toHaveStyle({ height: 'auto' });
  });
});
