import { render } from '@testing-library/react';
import React from 'react';
import { useAuth as useOidcAuth } from 'react-oidc-context';
import { BrowserRouter } from 'react-router';

import { AuthCallback } from './AuthCallback';
import { LoadingSpinner } from './LoadingSpinner';
import { useAuth } from '../contexts/AuthContext';

// Mock dependencies
jest.mock('react-oidc-context');
jest.mock('./LoadingSpinner');
jest.mock('../contexts/AuthContext', () => ({
  useAuth: jest.fn(),
}));

const mockUseOidcAuth = useOidcAuth as jest.MockedFunction<typeof useOidcAuth>;
const mockLoadingSpinner = LoadingSpinner as jest.MockedFunction<typeof LoadingSpinner>;

// Import the mocked useAuth
const mockUseAuth = useAuth as jest.MockedFunction<typeof useAuth>;

describe('AuthCallback', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('should show loading spinner when OIDC is loading', () => {
    mockUseOidcAuth.mockReturnValue({
      isAuthenticated: false,
      isLoading: true,
      user: null,
      error: undefined,
    } as ReturnType<typeof useOidcAuth>);

    mockUseAuth.mockReturnValue({
      user: null,
      isAuthenticated: false,
      isLoading: true,
      login: jest.fn(),
      logout: jest.fn(),
      clearAuthState: jest.fn(),
      refreshUser: jest.fn(),
    });

    render(
      <BrowserRouter>
        <AuthCallback />
      </BrowserRouter>
    );
    expect(mockLoadingSpinner).toHaveBeenCalled();
  });

  it('should show loading spinner when auth context is loading', () => {
    mockUseOidcAuth.mockReturnValue({
      isAuthenticated: true,
      isLoading: false,
      user: { access_token: 'test-token' },
      error: undefined,
    } as ReturnType<typeof useOidcAuth>);

    mockUseAuth.mockReturnValue({
      user: null,
      isAuthenticated: false,
      isLoading: true,
      login: jest.fn(),
      logout: jest.fn(),
      clearAuthState: jest.fn(),
      refreshUser: jest.fn(),
    });

    render(
      <BrowserRouter>
        <AuthCallback />
      </BrowserRouter>
    );
    expect(mockLoadingSpinner).toHaveBeenCalled();
  });
});
