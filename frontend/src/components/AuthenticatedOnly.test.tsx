import React from 'react';
import { render, screen } from '@testing-library/react';

import { AuthenticatedOnly } from './AuthenticatedOnly';

// Mock AuthContext
jest.mock('../auth/AuthContext', () => ({
  useAuth: jest.fn(),
}));

describe('AuthenticatedOnly', () => {
  const mockUseAuth = require('../auth/AuthContext').useAuth;

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('should render children when user is authenticated', () => {
    mockUseAuth.mockReturnValue({
      authenticated: true,
    });

    render(
      <AuthenticatedOnly>
        <div data-testid="protected-content">Protected Content</div>
      </AuthenticatedOnly>
    );

    expect(screen.getByTestId('protected-content')).toBeInTheDocument();
    expect(screen.getByText('Protected Content')).toBeInTheDocument();
  });

  it('should render React.Fragment when user is not authenticated', () => {
    mockUseAuth.mockReturnValue({
      authenticated: false,
    });

    const { container } = render(
      <AuthenticatedOnly>
        <div data-testid="protected-content">Protected Content</div>
      </AuthenticatedOnly>
    );

    // Should not render the protected content
    expect(screen.queryByTestId('protected-content')).not.toBeInTheDocument();
    expect(screen.queryByText('Protected Content')).not.toBeInTheDocument();

    // Should render an empty fragment (no additional DOM elements)
    expect(container.firstChild).toBeNull();
  });

  it('should render custom fallback when user is not authenticated', () => {
    mockUseAuth.mockReturnValue({
      authenticated: false,
    });

    render(
      <AuthenticatedOnly fallback={<div data-testid="fallback">Please sign in</div>}>
        <div data-testid="protected-content">Protected Content</div>
      </AuthenticatedOnly>
    );

    // Should not render the protected content
    expect(screen.queryByTestId('protected-content')).not.toBeInTheDocument();

    // Should render the custom fallback
    expect(screen.getByTestId('fallback')).toBeInTheDocument();
    expect(screen.getByText('Please sign in')).toBeInTheDocument();
  });

  it('should render multiple children when authenticated', () => {
    mockUseAuth.mockReturnValue({
      authenticated: true,
    });

    render(
      <AuthenticatedOnly>
        <div data-testid="content-1">Content 1</div>
        <div data-testid="content-2">Content 2</div>
        <div data-testid="content-3">Content 3</div>
      </AuthenticatedOnly>
    );

    expect(screen.getByTestId('content-1')).toBeInTheDocument();
    expect(screen.getByTestId('content-2')).toBeInTheDocument();
    expect(screen.getByTestId('content-3')).toBeInTheDocument();
    expect(screen.getByText('Content 1')).toBeInTheDocument();
    expect(screen.getByText('Content 2')).toBeInTheDocument();
    expect(screen.getByText('Content 3')).toBeInTheDocument();
  });

  it('should handle complex nested components when authenticated', () => {
    mockUseAuth.mockReturnValue({
      authenticated: true,
    });

    const NestedComponent = () => (
      <div data-testid="nested">
        <span>Nested content</span>
        <button>Click me</button>
      </div>
    );

    render(
      <AuthenticatedOnly>
        <NestedComponent />
      </AuthenticatedOnly>
    );

    expect(screen.getByTestId('nested')).toBeInTheDocument();
    expect(screen.getByText('Nested content')).toBeInTheDocument();
    expect(screen.getByRole('button')).toBeInTheDocument();
  });

  it('should handle empty children when authenticated', () => {
    mockUseAuth.mockReturnValue({
      authenticated: true,
    });

    const { container } = render(<AuthenticatedOnly>{null}</AuthenticatedOnly>);

    // Should render an empty fragment
    expect(container.firstChild).toBeNull();
  });

  it('should handle undefined children when authenticated', () => {
    mockUseAuth.mockReturnValue({
      authenticated: true,
    });

    const { container } = render(<AuthenticatedOnly>{undefined}</AuthenticatedOnly>);

    // Should render an empty fragment
    expect(container.firstChild).toBeNull();
  });

  it('should handle null fallback when not authenticated', () => {
    mockUseAuth.mockReturnValue({
      authenticated: false,
    });

    const { container } = render(
      <AuthenticatedOnly fallback={null}>
        <div data-testid="protected-content">Protected Content</div>
      </AuthenticatedOnly>
    );

    // Should not render the protected content
    expect(screen.queryByTestId('protected-content')).not.toBeInTheDocument();

    // Should render an empty fragment
    expect(container.firstChild).toBeNull();
  });

  it('should handle undefined fallback when not authenticated', () => {
    mockUseAuth.mockReturnValue({
      authenticated: false,
    });

    const { container } = render(
      <AuthenticatedOnly fallback={undefined}>
        <div data-testid="protected-content">Protected Content</div>
      </AuthenticatedOnly>
    );

    // Should not render the protected content
    expect(screen.queryByTestId('protected-content')).not.toBeInTheDocument();

    // Should render an empty fragment
    expect(container.firstChild).toBeNull();
  });
});
