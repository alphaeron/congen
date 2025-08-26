import { render, screen, waitFor, act } from '@testing-library/react';
import React from 'react';
import { useAuth as useOidcAuth } from 'react-oidc-context';

import { AuthProvider, useAuth } from './AuthContext';
import type { User } from '../api/types';
import { createUserProfile, getCurrentUser } from '../api/user';

// Mock the API functions
jest.mock('../api/user');
const mockCreateUserProfile = createUserProfile as jest.MockedFunction<typeof createUserProfile>;
const mockGetCurrentUser = getCurrentUser as jest.MockedFunction<typeof getCurrentUser>;

// Mock react-oidc-context
jest.mock('react-oidc-context');
const mockUseOidcAuth = useOidcAuth as jest.MockedFunction<typeof useOidcAuth>;

const mockUser: User = {
  keycloak_id: 'test-id',
  name: 'Test User',
  created_at: '2023-01-01T00:00:00Z',
  updated_at: '2023-01-01T00:00:00Z',
};

const TestComponent: React.FC = () => {
  const { user, isAuthenticated, isLoading, error, login, logout, clearError } = useAuth();
  return (
    <div>
      <div data-testid="user">{user ? user.name : 'No user'}</div>
      <div data-testid="isAuthenticated">{isAuthenticated.toString()}</div>
      <div data-testid="isLoading">{isLoading.toString()}</div>
      <div data-testid="error">{error || 'No error'}</div>
      <button onClick={login}>Login</button>
      <button onClick={logout}>Logout</button>
      <button onClick={clearError}>Clear Error</button>
    </div>
  );
};

describe('AuthContext', () => {
  let originalConsoleError: typeof console.error;

  beforeEach(() => {
    jest.clearAllMocks();
    // Suppress console.error during tests
    originalConsoleError = console.error;
    console.error = jest.fn();

    mockUseOidcAuth.mockReturnValue({
      isAuthenticated: false,
      isLoading: false,
      user: null,
      error: null,
      signinRedirect: jest.fn(),
      signoutRedirect: jest.fn(),
      removeUser: jest.fn(),
    } as unknown as ReturnType<typeof useOidcAuth>);
  });

  afterEach(() => {
    // Restore console.error
    console.error = originalConsoleError;
  });

  it('should provide authentication context', () => {
    render(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>
    );

    expect(screen.getByTestId('user')).toHaveTextContent('No user');
    expect(screen.getByTestId('isAuthenticated')).toHaveTextContent('false');
    expect(screen.getByTestId('isLoading')).toHaveTextContent('false');
    expect(screen.getByTestId('error')).toHaveTextContent('No error');
  });

  it('should handle login', async () => {
    const mockSigninRedirect = jest.fn();
    mockUseOidcAuth.mockReturnValue({
      isAuthenticated: false,
      isLoading: false,
      user: null,
      error: null,
      signinRedirect: mockSigninRedirect,
      signoutRedirect: jest.fn(),
      removeUser: jest.fn(),
    } as unknown as ReturnType<typeof useOidcAuth>);

    render(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>
    );

    await act(async () => {
      screen.getByText('Login').click();
    });
    expect(mockSigninRedirect).toHaveBeenCalled();
  });

  it('should handle login error', async () => {
    const mockSigninRedirect = jest.fn().mockRejectedValue(new Error('Login failed'));
    mockUseOidcAuth.mockReturnValue({
      isAuthenticated: false,
      isLoading: false,
      user: null,
      error: null,
      signinRedirect: mockSigninRedirect,
      signoutRedirect: jest.fn(),
      removeUser: jest.fn(),
    } as unknown as ReturnType<typeof useOidcAuth>);

    render(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>
    );

    await act(async () => {
      screen.getByText('Login').click();
    });

    await waitFor(() => {
      expect(screen.getByTestId('error')).toHaveTextContent('Login failed. Please try again.');
    });
  });

  it('should handle logout', async () => {
    const mockSignoutRedirect = jest.fn();
    mockUseOidcAuth.mockReturnValue({
      isAuthenticated: true,
      isLoading: false,
      user: {} as Record<string, unknown>,
      error: null,
      signinRedirect: jest.fn(),
      signoutRedirect: mockSignoutRedirect,
      removeUser: jest.fn(),
    } as unknown as ReturnType<typeof useOidcAuth>);

    render(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>
    );

    await act(async () => {
      screen.getByText('Logout').click();
    });
    expect(mockSignoutRedirect).toHaveBeenCalled();
  });

  it('should handle logout error', async () => {
    const mockSignoutRedirect = jest.fn().mockRejectedValue(new Error('Logout failed'));
    mockUseOidcAuth.mockReturnValue({
      isAuthenticated: true,
      isLoading: false,
      user: {} as Record<string, unknown>,
      error: null,
      signinRedirect: jest.fn(),
      signoutRedirect: mockSignoutRedirect,
      removeUser: jest.fn(),
    } as unknown as ReturnType<typeof useOidcAuth>);

    render(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>
    );

    await act(async () => {
      screen.getByText('Logout').click();
    });

    await waitFor(() => {
      expect(screen.getByTestId('error')).toHaveTextContent('Logout failed. Please try again.');
    });
  });

  it('should clear error', async () => {
    mockUseOidcAuth.mockReturnValue({
      isAuthenticated: false,
      isLoading: false,
      user: null,
      error: new Error('Test error'),
      signinRedirect: jest.fn(),
      signoutRedirect: jest.fn(),
      removeUser: jest.fn(),
    } as unknown as ReturnType<typeof useOidcAuth>);

    render(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>
    );

    await act(async () => {
      screen.getByText('Clear Error').click();
    });

    await waitFor(() => {
      expect(screen.getByTestId('error')).toHaveTextContent('No error');
    });
  });

  it('should sync user profile when authenticated', async () => {
    mockUseOidcAuth.mockReturnValue({
      isAuthenticated: true,
      isLoading: false,
      user: {} as Record<string, unknown>,
      error: null,
      signinRedirect: jest.fn(),
      signoutRedirect: jest.fn(),
      removeUser: jest.fn(),
    } as unknown as ReturnType<typeof useOidcAuth>);
    mockGetCurrentUser.mockResolvedValue(mockUser);

    await act(async () => {
      render(
        <AuthProvider>
          <TestComponent />
        </AuthProvider>
      );
    });

    await waitFor(() => {
      expect(screen.getByTestId('user')).toHaveTextContent('Test User');
    });
  });

  it('should automatically create profile when user not found', async () => {
    mockUseOidcAuth.mockReturnValue({
      isAuthenticated: true,
      isLoading: false,
      user: {} as Record<string, unknown>,
      error: null,
      signinRedirect: jest.fn(),
      signoutRedirect: jest.fn(),
      removeUser: jest.fn(),
    } as unknown as ReturnType<typeof useOidcAuth>);

    // First call returns 404, second call creates profile
    mockGetCurrentUser
      .mockRejectedValueOnce({ response: { status: 404 } })
      .mockResolvedValueOnce(mockUser);
    mockCreateUserProfile.mockResolvedValue(mockUser);

    await act(async () => {
      render(
        <AuthProvider>
          <TestComponent />
        </AuthProvider>
      );
    });

    await waitFor(() => {
      expect(screen.getByTestId('user')).toHaveTextContent('Test User');
    });
    expect(mockCreateUserProfile).toHaveBeenCalled();
  });

  it('should handle OIDC errors', () => {
    const oidcError = new Error('OIDC error');
    mockUseOidcAuth.mockReturnValue({
      isAuthenticated: false,
      isLoading: false,
      user: null,
      error: oidcError,
      signinRedirect: jest.fn(),
      signoutRedirect: jest.fn(),
      removeUser: jest.fn(),
    } as unknown as ReturnType<typeof useOidcAuth>);

    render(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>
    );

    expect(screen.getByTestId('error')).toHaveTextContent('OIDC error');
  });
});
