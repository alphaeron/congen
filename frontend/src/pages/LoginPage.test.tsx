import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import React from 'react';

import { LoginPage } from './LoginPage';
import { useAuth } from '../contexts/AuthContext';

// Mock the useAuth hook
jest.mock('../contexts/AuthContext');
const mockUseAuth = useAuth as jest.MockedFunction<typeof useAuth>;

// Mock the LoadingSpinner component
jest.mock('../components/LoadingSpinner', () => ({
  LoadingSpinner: ({ fullHeight }: { fullHeight?: boolean }) => (
    <div data-testid="loading-spinner" data-full-height={fullHeight}>
      Loading...
    </div>
  ),
}));

describe('LoginPage', () => {
  const mockLogin = jest.fn();

  beforeEach(() => {
    mockUseAuth.mockReturnValue({
      isLoading: false,
      login: mockLogin,
      logout: jest.fn(),
      clearAuthState: jest.fn(),
      refreshUser: jest.fn(),
      user: null,
      isAuthenticated: false,
    });
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  it('renders the login page with sign in button', () => {
    render(<LoginPage />);

    expect(screen.getByRole('heading', { name: 'Sign In' })).toBeInTheDocument();
    expect(screen.getByText('Please sign in to access your account')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Sign In' })).toBeInTheDocument();
  });

  it('shows loading spinner when authentication is loading', () => {
    mockUseAuth.mockReturnValue({
      isLoading: true,
      login: mockLogin,
      logout: jest.fn(),
      clearAuthState: jest.fn(),
      refreshUser: jest.fn(),
      user: null,
      isAuthenticated: false,
    });

    render(<LoginPage />);

    expect(screen.getByTestId('loading-spinner')).toBeInTheDocument();
    expect(screen.queryByText('Sign In')).not.toBeInTheDocument();
  });

  it('calls login function when sign in button is clicked', async () => {
    render(<LoginPage />);

    const signInButton = screen.getByRole('button', { name: 'Sign In' });
    fireEvent.click(signInButton);

    await waitFor(() => {
      expect(mockLogin).toHaveBeenCalledTimes(1);
    });
  });

  it('handles login errors gracefully', async () => {
    mockLogin.mockRejectedValueOnce(new Error('Login failed'));

    render(<LoginPage />);

    const signInButton = screen.getByRole('button', { name: 'Sign In' });
    fireEvent.click(signInButton);

    await waitFor(() => {
      expect(mockLogin).toHaveBeenCalledTimes(1);
    });
  });
});
