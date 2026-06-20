import { cleanup, render, screen, waitFor, act } from '@testing-library/react';
import { SnackbarProvider } from 'notistack';
import React from 'react';
import { useAuth as useOidcAuth } from 'react-oidc-context';

import { AuthProvider, useAuth } from './AuthContext';
import { ApiRequestError } from '../api/endpoint';
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

const renderWithProviders = (component: React.ReactElement) => {
  return render(<SnackbarProvider>{component}</SnackbarProvider>);
};

const TestComponent: React.FC = () => {
  const { user, isAuthenticated, isLoading, login, logout } = useAuth();
  return (
    <div>
      <div data-testid="user">{user ? user.name : 'No user'}</div>
      <div data-testid="isAuthenticated">{isAuthenticated.toString()}</div>
      <div data-testid="isLoading">{isLoading.toString()}</div>
      <button onClick={login}>Login</button>
      <button onClick={logout}>Logout</button>
    </div>
  );
};

describe('AuthContext', () => {
  beforeEach(() => {
    mockGetCurrentUser.mockReset();
    mockCreateUserProfile.mockReset();
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
    cleanup();
  });

  it('should provide authentication context', () => {
    renderWithProviders(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>
    );

    expect(screen.getByTestId('user')).toHaveTextContent('No user');
    expect(screen.getByTestId('isAuthenticated')).toHaveTextContent('false');
    expect(screen.getByTestId('isLoading')).toHaveTextContent('false');
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

    renderWithProviders(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>
    );

    await act(async () => {
      screen.getByText('Login').click();
    });
    expect(mockSigninRedirect).toHaveBeenCalled();
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

    renderWithProviders(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>
    );

    await act(async () => {
      screen.getByText('Logout').click();
    });
    expect(mockSignoutRedirect).toHaveBeenCalled();
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
      renderWithProviders(
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
      renderWithProviders(
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

  it('should automatically create profile when getCurrentUser throws ApiRequestError 404', async () => {
    mockUseOidcAuth.mockReturnValue({
      isAuthenticated: true,
      isLoading: false,
      user: {} as Record<string, unknown>,
      error: null,
      signinRedirect: jest.fn(),
      signoutRedirect: jest.fn(),
      removeUser: jest.fn(),
    } as unknown as ReturnType<typeof useOidcAuth>);

    mockGetCurrentUser.mockImplementation(() =>
      Promise.reject(
        new ApiRequestError('Resource not found', { status: 404, url: '/user/me', method: 'GET' })
      )
    );
    mockCreateUserProfile.mockResolvedValue(mockUser);

    await act(async () => {
      renderWithProviders(
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
});
