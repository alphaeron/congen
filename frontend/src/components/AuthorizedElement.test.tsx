import { render, screen } from '@testing-library/react';
import React from 'react';
import { MemoryRouter } from 'react-router';

import { AuthorizedElement } from './AuthorizedElement';

// Mock the AuthContext
const mockUseAuth = jest.fn();
jest.mock('../contexts/AuthContext', () => ({
  useAuth: () => mockUseAuth(),
}));

const TestComponent: React.FC = () => <div>Protected Content</div>;

const TestWrapper: React.FC<{ children: React.ReactNode }> = ({ children }) => (
  <MemoryRouter>{children}</MemoryRouter>
);

describe('AuthorizedElement', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('should show loading state when authentication is loading', () => {
    mockUseAuth.mockReturnValue({
      isAuthenticated: false,
      isLoading: true,
      user: null,
    });

    render(
      <TestWrapper>
        <AuthorizedElement>
          <TestComponent />
        </AuthorizedElement>
      </TestWrapper>
    );

    expect(screen.queryByText('Protected Content')).not.toBeInTheDocument();
  });

  it('should render children when user is authenticated and no roles required', () => {
    mockUseAuth.mockReturnValue({
      isAuthenticated: true,
      isLoading: false,
      user: { roles: [] },
    });

    render(
      <TestWrapper>
        <AuthorizedElement>
          <TestComponent />
        </AuthorizedElement>
      </TestWrapper>
    );

    expect(screen.getByText('Protected Content')).toBeInTheDocument();
  });

  it('should not render children when user is not authenticated', () => {
    mockUseAuth.mockReturnValue({
      isAuthenticated: false,
      isLoading: false,
      user: null,
    });

    render(
      <TestWrapper>
        <AuthorizedElement>
          <TestComponent />
        </AuthorizedElement>
      </TestWrapper>
    );

    expect(screen.queryByText('Protected Content')).not.toBeInTheDocument();
  });

  it('should render fallback when user is not authenticated', () => {
    mockUseAuth.mockReturnValue({
      isAuthenticated: false,
      isLoading: false,
      user: null,
    });

    render(
      <TestWrapper>
        <AuthorizedElement fallback={<div>Fallback Content</div>}>
          <TestComponent />
        </AuthorizedElement>
      </TestWrapper>
    );

    expect(screen.getByText('Fallback Content')).toBeInTheDocument();
    expect(screen.queryByText('Protected Content')).not.toBeInTheDocument();
  });

  it('should render children when authentication is not required', () => {
    mockUseAuth.mockReturnValue({
      isAuthenticated: false,
      isLoading: false,
      user: null,
    });

    render(
      <TestWrapper>
        <AuthorizedElement requireAuth={false}>
          <TestComponent />
        </AuthorizedElement>
      </TestWrapper>
    );

    expect(screen.getByText('Protected Content')).toBeInTheDocument();
  });

  it('should render children when user has required role', () => {
    mockUseAuth.mockReturnValue({
      isAuthenticated: true,
      isLoading: false,
      user: { roles: ['admin'] },
    });

    render(
      <TestWrapper>
        <AuthorizedElement roles={['admin']}>
          <TestComponent />
        </AuthorizedElement>
      </TestWrapper>
    );

    expect(screen.getByText('Protected Content')).toBeInTheDocument();
  });

  it('should render children when user has required role in roles array', () => {
    mockUseAuth.mockReturnValue({
      isAuthenticated: true,
      isLoading: false,
      user: { roles: ['admin'] },
    });

    render(
      <TestWrapper>
        <AuthorizedElement roles={['admin']}>
          <TestComponent />
        </AuthorizedElement>
      </TestWrapper>
    );

    expect(screen.getByText('Protected Content')).toBeInTheDocument();
  });

  it('should not render children when user does not have required role', () => {
    mockUseAuth.mockReturnValue({
      isAuthenticated: true,
      isLoading: false,
      user: { roles: ['user'] },
    });

    render(
      <TestWrapper>
        <AuthorizedElement roles={['admin']}>
          <TestComponent />
        </AuthorizedElement>
      </TestWrapper>
    );

    expect(screen.queryByText('Protected Content')).not.toBeInTheDocument();
  });
});
