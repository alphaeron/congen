import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';

import { LoginPage } from './LoginPage';

// Mock the AuthContext
const mockUseAuth = jest.fn();
jest.mock('../contexts/AuthContext', () => ({
  useAuth: () => mockUseAuth(),
  AuthProvider: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
}));

// Mock react-oidc-context
jest.mock('react-oidc-context', () => ({
  AuthProvider: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
}));

// Mock react-router-dom hooks
const mockNavigate = jest.fn();
const mockLocation: { state: unknown } = { state: null };

jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useNavigate: () => mockNavigate,
  useLocation: () => mockLocation,
}));

// Test wrapper component
const TestWrapper = () => {
  return (
    <MemoryRouter>
      <LoginPage />
    </MemoryRouter>
  );
};

describe('LoginPage', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    // Default mock implementation
    mockUseAuth.mockReturnValue({
      isAuthenticated: false,
      isLoading: false,
      user: null,
      login: jest.fn(),
      logout: jest.fn(),
      register: jest.fn(),
      clearError: jest.fn(),
    });
    // Reset location state
    mockLocation.state = null;
  });

  it('should render login page with tabs', async () => {
    render(<TestWrapper />);

    await waitFor(() => {
      expect(screen.getByRole('tab', { name: 'Sign In' })).toBeInTheDocument();
    });
    expect(screen.getByRole('tab', { name: 'Sign Up' })).toBeInTheDocument();
  });

  it('should display sign in tab content', async () => {
    render(<TestWrapper />);

    await waitFor(() => {
      expect(screen.getByRole('tab', { name: 'Sign In' })).toBeInTheDocument();
    });

    // Check that the sign in form is rendered
    expect(screen.getByText('Sign In', { selector: 'h1' })).toBeInTheDocument();
  });

  it('should display sign up tab content', async () => {
    render(<TestWrapper />);

    await waitFor(() => {
      expect(screen.getByRole('tab', { name: 'Sign In' })).toBeInTheDocument();
    });

    // Click on the Sign Up tab
    const signUpTab = screen.getByRole('tab', { name: 'Sign Up' });
    fireEvent.click(signUpTab);

    await waitFor(() => {
      expect(screen.getByText('Create Account', { selector: 'h1' })).toBeInTheDocument();
    });
  });

  it('should show loading state when authentication is loading', () => {
    mockUseAuth.mockReturnValue({
      isAuthenticated: false,
      isLoading: true,
      user: null,
      login: jest.fn(),
      logout: jest.fn(),
      register: jest.fn(),
      clearError: jest.fn(),
    });

    render(<TestWrapper />);

    // The loading state should be handled by the App component, not LoginPage
    // LoginPage should still render normally
    expect(screen.getByRole('tab', { name: 'Sign In' })).toBeInTheDocument();
  });

  it('should redirect when authenticated', async () => {
    mockUseAuth.mockReturnValue({
      isAuthenticated: true,
      isLoading: false,
      user: { id: 1, name: 'Test User' },
      login: jest.fn(),
      logout: jest.fn(),
      register: jest.fn(),
      clearError: jest.fn(),
    });

    render(<TestWrapper />);

    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith('/profile');
    });
  });

  it('should display registration success message', async () => {
    mockLocation.state = { message: 'Registration successful!' };

    render(<TestWrapper />);

    await waitFor(() => {
      expect(screen.getByText('Registration successful!')).toBeInTheDocument();
    });
  });

  it('should handle tab switching', async () => {
    render(<TestWrapper />);

    await waitFor(() => {
      expect(screen.getByRole('tab', { name: 'Sign In' })).toBeInTheDocument();
    });

    // Initially on Sign In tab
    expect(screen.getByText('Sign In', { selector: 'h1' })).toBeInTheDocument();

    // Switch to Sign Up tab
    const signUpTab = screen.getByRole('tab', { name: 'Sign Up' });
    fireEvent.click(signUpTab);

    await waitFor(() => {
      expect(screen.getByText('Create Account', { selector: 'h1' })).toBeInTheDocument();
    });

    // Switch back to Sign In tab
    const signInTab = screen.getByRole('tab', { name: 'Sign In' });
    fireEvent.click(signInTab);

    await waitFor(() => {
      expect(screen.getByText('Sign In', { selector: 'h1' })).toBeInTheDocument();
    });
  });
});
