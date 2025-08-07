import { render } from '@testing-library/react';
import React from 'react';
import { BrowserRouter } from 'react-router';

import { LoadingSpinner } from './LoadingSpinner';
import { ProtectedRoute } from './ProtectedRoute';
import type { User } from '../api/types';
import { useAuth } from '../contexts/AuthContext';


// Mock dependencies
jest.mock('../contexts/AuthContext');
jest.mock('./LoadingSpinner');

const mockUseAuth = useAuth as jest.MockedFunction<typeof useAuth>;
const mockLoadingSpinner = LoadingSpinner as jest.MockedFunction<typeof LoadingSpinner>;

describe('ProtectedRoute', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockLoadingSpinner.mockReturnValue(<div data-testid="loading-spinner">Loading...</div>);
  });

  it('should render children when authentication is required and user is authenticated', () => {
    const mockUser: User = {
      keycloak_id: 'test-user-id',
      name: 'Test User',
      age: 25,
      height: 180,
      weight: 75,
      created_at: '2024-01-01T00:00:00Z',
      updated_at: '2024-01-01T00:00:00Z',
      roles: [],
    };

    mockUseAuth.mockReturnValue({
      isAuthenticated: true,
      isLoading: false,
      user: mockUser,
      error: null,
      login: jest.fn(),
      logout: jest.fn(),
      createProfile: jest.fn(),
      clearError: jest.fn(),
    });

    const { getByText } = render(
      <BrowserRouter>
        <ProtectedRoute requireAuth={true}>
          <div>Protected Content</div>
        </ProtectedRoute>
      </BrowserRouter>
    );

    expect(getByText('Protected Content')).toBeInTheDocument();
  });

  it('should not render children when authentication is required and user is not authenticated', () => {
    mockUseAuth.mockReturnValue({
      isAuthenticated: false,
      isLoading: false,
      user: null,
      error: null,
      login: jest.fn(),
      logout: jest.fn(),
      createProfile: jest.fn(),
      clearError: jest.fn(),
    });

    const { queryByText } = render(
      <BrowserRouter>
        <ProtectedRoute requireAuth={true}>
          <div>Protected Content</div>
        </ProtectedRoute>
      </BrowserRouter>
    );

    expect(queryByText('Protected Content')).not.toBeInTheDocument();
  });

  it('should render children when authentication is not required and user is not authenticated', () => {
    mockUseAuth.mockReturnValue({
      isAuthenticated: false,
      isLoading: false,
      user: null,
      error: null,
      login: jest.fn(),
      logout: jest.fn(),
      createProfile: jest.fn(),
      clearError: jest.fn(),
    });

    const { getByText } = render(
      <BrowserRouter>
        <ProtectedRoute requireAuth={false}>
          <div>Public Content</div>
        </ProtectedRoute>
      </BrowserRouter>
    );

    expect(getByText('Public Content')).toBeInTheDocument();
  });

  it('should not render children when authentication is not required and user is authenticated', () => {
    const mockUser: User = {
      keycloak_id: 'test-user-id',
      name: 'Test User',
      age: 25,
      height: 180,
      weight: 75,
      created_at: '2024-01-01T00:00:00Z',
      updated_at: '2024-01-01T00:00:00Z',
      roles: [],
    };

    mockUseAuth.mockReturnValue({
      isAuthenticated: true,
      isLoading: false,
      user: mockUser,
      error: null,
      login: jest.fn(),
      logout: jest.fn(),
      createProfile: jest.fn(),
      clearError: jest.fn(),
    });

    const { queryByText } = render(
      <BrowserRouter>
        <ProtectedRoute requireAuth={false}>
          <div>Public Content</div>
        </ProtectedRoute>
      </BrowserRouter>
    );

    expect(queryByText('Public Content')).not.toBeInTheDocument();
  });

  it('should show loading spinner when authentication is loading', () => {
    mockUseAuth.mockReturnValue({
      isAuthenticated: false,
      isLoading: true,
      user: null,
      error: null,
      login: jest.fn(),
      logout: jest.fn(),
      createProfile: jest.fn(),
      clearError: jest.fn(),
    });

    const { getByTestId } = render(
      <BrowserRouter>
        <ProtectedRoute requireAuth={true}>
          <div>Protected Content</div>
        </ProtectedRoute>
      </BrowserRouter>
    );

    expect(getByTestId('loading-spinner')).toBeInTheDocument();
  });

  it('should default to requiring authentication', () => {
    mockUseAuth.mockReturnValue({
      isAuthenticated: false,
      isLoading: false,
      user: null,
      error: null,
      login: jest.fn(),
      logout: jest.fn(),
      createProfile: jest.fn(),
      clearError: jest.fn(),
    });

    const { queryByText } = render(
      <BrowserRouter>
        <ProtectedRoute>
          <div>Protected Content</div>
        </ProtectedRoute>
      </BrowserRouter>
    );

    expect(queryByText('Protected Content')).not.toBeInTheDocument();
  });
});
