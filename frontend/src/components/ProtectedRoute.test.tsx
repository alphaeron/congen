import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';

import { ProtectedRoute } from './ProtectedRoute';
import { AuthProvider, useAuth } from 'react-oidc-context';

// Mock Keycloak
jest.mock('keycloak-js', () => {
  return jest.fn().mockImplementation(() => ({
    authenticated: false,
    token: null,
    tokenParsed: null,
    login: jest.fn(),
    logout: jest.fn(),
    updateToken: jest.fn().mockResolvedValue(true),
    accountManagement: jest.fn(),
  }));
});

// Mock react-oidc-context
const mockUseAuth = jest.fn();
jest.mock('react-oidc-context', () => ({
  useAuth: mockUseAuth,
  AuthProvider: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
}));

const TestComponent: React.FC = () => <div>Protected Content</div>;

const TestWrapper: React.FC<{ children: React.ReactNode }> = ({ children }) => (
  <MemoryRouter>
    <AuthProvider>
      {children}
    </AuthProvider>
  </MemoryRouter>
);

describe('ProtectedRoute', () => {
  const mockUseAuth = useAuth as jest.MockedFunction<any>;

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('should show loading state when authentication is loading', () => {
    mockUseAuth.mockReturnValue({
      isAuthenticated: false,
      isLoading: true,
      user: null,
      signinRedirect: jest.fn(),
      signoutRedirect: jest.fn(),
    });

    render(
      <TestWrapper>
        <ProtectedRoute>
          <TestComponent />
        </ProtectedRoute>
      </TestWrapper>
    );

    expect(screen.getByText('Loading...')).toBeInTheDocument();
  });

  it('should render children when user is authenticated', () => {
    mockUseAuth.mockReturnValue({
      isAuthenticated: true,
      isLoading: false,
      user: { profile: { sub: 'test-user' } },
      signinRedirect: jest.fn(),
      signoutRedirect: jest.fn(),
    });

    render(
      <TestWrapper>
        <ProtectedRoute>
          <TestComponent />
        </ProtectedRoute>
      </TestWrapper>
    );

    expect(screen.getByText('Protected Content')).toBeInTheDocument();
  });

  it('should redirect to login when user is not authenticated', () => {
    mockUseAuth.mockReturnValue({
      isAuthenticated: false,
      isLoading: false,
      user: null,
      signinRedirect: jest.fn(),
      signoutRedirect: jest.fn(),
    });

    render(
      <TestWrapper>
        <ProtectedRoute>
          <TestComponent />
        </ProtectedRoute>
      </TestWrapper>
    );

    // Should redirect to login (Navigate component behavior)
    expect(screen.queryByText('Protected Content')).not.toBeInTheDocument();
  });

  it('should redirect authenticated users away from public pages', () => {
    mockUseAuth.mockReturnValue({
      isAuthenticated: true,
      isLoading: false,
      user: { profile: { sub: 'test-user' } },
      signinRedirect: jest.fn(),
      signoutRedirect: jest.fn(),
    });

    render(
      <TestWrapper>
        <ProtectedRoute requireAuth={false}>
          <TestComponent />
        </ProtectedRoute>
      </TestWrapper>
    );

    // Should redirect authenticated users away from public pages
    expect(screen.queryByText('Protected Content')).not.toBeInTheDocument();
  });

  it('should render public content for unauthenticated users', () => {
    mockUseAuth.mockReturnValue({
      isAuthenticated: false,
      isLoading: false,
      user: null,
      signinRedirect: jest.fn(),
      signoutRedirect: jest.fn(),
    });

    render(
      <TestWrapper>
        <ProtectedRoute requireAuth={false}>
          <TestComponent />
        </ProtectedRoute>
      </TestWrapper>
    );

    expect(screen.getByText('Protected Content')).toBeInTheDocument();
  });
}); 