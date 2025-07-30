import React from 'react';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';

import { AuthorizedElement } from './AuthorizedElement';
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
    hasRealmRole: jest.fn(),
    hasResourceRole: jest.fn(),
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

describe('AuthorizedElement', () => {
  const mockUseAuth = useAuth as jest.MockedFunction<any>;

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('should show loading state when authentication is loading', () => {
    mockUseAuth.mockReturnValue({
      authenticated: false,
      loading: true,
      userId: null,
      keycloak: null,
      login: jest.fn(),
      logout: jest.fn(),
      updateToken: jest.fn(),
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
      authenticated: true,
      loading: false,
      userId: 'test-user',
      keycloak: {
        hasRealmRole: jest.fn(),
        hasResourceRole: jest.fn(),
      } as any,
      login: jest.fn(),
      logout: jest.fn(),
      updateToken: jest.fn(),
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
      authenticated: false,
      loading: false,
      userId: null,
      keycloak: null,
      login: jest.fn(),
      logout: jest.fn(),
      updateToken: jest.fn(),
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
      authenticated: false,
      loading: false,
      userId: null,
      keycloak: null,
      login: jest.fn(),
      logout: jest.fn(),
      updateToken: jest.fn(),
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
      authenticated: false,
      loading: false,
      userId: null,
      keycloak: null,
      login: jest.fn(),
      logout: jest.fn(),
      updateToken: jest.fn(),
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
    const mockKeycloak = {
      hasRealmRole: jest.fn().mockReturnValue(true),
      hasResourceRole: jest.fn().mockReturnValue(false),
    };

    mockUseAuth.mockReturnValue({
      authenticated: true,
      loading: false,
      userId: 'test-user',
      keycloak: mockKeycloak as any,
      login: jest.fn(),
      logout: jest.fn(),
      updateToken: jest.fn(),
    });

    render(
      <TestWrapper>
        <AuthorizedElement roles={['admin']}>
          <TestComponent />
        </AuthorizedElement>
      </TestWrapper>
    );

    expect(screen.getByText('Protected Content')).toBeInTheDocument();
    expect(mockKeycloak.hasRealmRole).toHaveBeenCalledWith('admin');
  });

  it('should not render children when user does not have required role', () => {
    const mockKeycloak = {
      hasRealmRole: jest.fn().mockReturnValue(false),
      hasResourceRole: jest.fn().mockReturnValue(false),
    };

    mockUseAuth.mockReturnValue({
      authenticated: true,
      loading: false,
      userId: 'test-user',
      keycloak: mockKeycloak as any,
      login: jest.fn(),
      logout: jest.fn(),
      updateToken: jest.fn(),
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