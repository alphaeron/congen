import React from 'react';
import { render } from '@testing-library/react';
import { useAuth as useOidcAuth } from 'react-oidc-context';

import { AuthCallback } from './AuthCallback';
import { LoadingSpinner } from './LoadingSpinner';

// Mock dependencies
jest.mock('react-oidc-context');
jest.mock('./LoadingSpinner');

const mockUseOidcAuth = useOidcAuth as jest.MockedFunction<typeof useOidcAuth>;
const mockLoadingSpinner = LoadingSpinner as jest.MockedFunction<typeof LoadingSpinner>;

describe('AuthCallback', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockLoadingSpinner.mockReturnValue(<div data-testid="loading-spinner">Loading...</div>);
  });

  it('should show loading spinner when OIDC is loading', () => {
    mockUseOidcAuth.mockReturnValue({
      isLoading: true,
      isAuthenticated: false,
      user: null,
      error: null,
      signinRedirect: jest.fn(),
      removeUser: jest.fn(),
    } as unknown as ReturnType<typeof useOidcAuth>);

    const { getByTestId } = render(<AuthCallback />);

    expect(getByTestId('loading-spinner')).toBeInTheDocument();
  });

  it('should log successful authentication', () => {
    const consoleSpy = jest.spyOn(console, 'log').mockImplementation();

    mockUseOidcAuth.mockReturnValue({
      isLoading: false,
      isAuthenticated: true,
      user: { sub: 'test-user' },
      error: null,
      signinRedirect: jest.fn(),
      removeUser: jest.fn(),
    } as unknown as ReturnType<typeof useOidcAuth>);

    render(<AuthCallback />);

    expect(consoleSpy).toHaveBeenCalledWith('🔐 AuthCallback: Authentication successful');
    consoleSpy.mockRestore();
  });

  it('should log authentication error', () => {
    const consoleSpy = jest.spyOn(console, 'log').mockImplementation();
    const mockError = new Error('Authentication failed');

    mockUseOidcAuth.mockReturnValue({
      isLoading: false,
      isAuthenticated: false,
      user: null,
      error: mockError,
      signinRedirect: jest.fn(),
      removeUser: jest.fn(),
    } as unknown as ReturnType<typeof useOidcAuth>);

    render(<AuthCallback />);

    expect(consoleSpy).toHaveBeenCalledWith('🔐 AuthCallback: Authentication failed', mockError);
    consoleSpy.mockRestore();
  });

  it('should log unclear authentication state', () => {
    const consoleSpy = jest.spyOn(console, 'log').mockImplementation();

    mockUseOidcAuth.mockReturnValue({
      isLoading: false,
      isAuthenticated: false,
      user: null,
      error: null,
      signinRedirect: jest.fn(),
      removeUser: jest.fn(),
    } as unknown as ReturnType<typeof useOidcAuth>);

    render(<AuthCallback />);

    expect(consoleSpy).toHaveBeenCalledWith('🔐 AuthCallback: Authentication state unclear');
    consoleSpy.mockRestore();
  });

  it('should return null when not loading', () => {
    mockUseOidcAuth.mockReturnValue({
      isLoading: false,
      isAuthenticated: true,
      user: { sub: 'test-user' },
      error: null,
      signinRedirect: jest.fn(),
      removeUser: jest.fn(),
    } as unknown as ReturnType<typeof useOidcAuth>);

    const { container } = render(<AuthCallback />);

    expect(container.firstChild).toBeNull();
  });
});
