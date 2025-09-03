import { render, screen } from '@testing-library/react';
import React from 'react';
import { MemoryRouter } from 'react-router';

import { DashboardPage } from './DashboardPage';
import type { User } from '../api/types';

// Mock the auth context
const mockUser: User = {
  keycloak_id: 'test-user-id',
  name: 'Test User',
  created_at: '2024-01-01T00:00:00Z',
  updated_at: '2024-01-01T00:00:00Z',
  roles: ['user'],
};

const mockUseAuth = jest.fn();

jest.mock('../contexts/AuthContext', () => ({
  useAuth: () => mockUseAuth(),
}));

jest.mock('react-oidc-context', () => ({
  useAuth: () => ({
    user: {
      profile: {
        given_name: 'John',
        family_name: 'Doe',
        name: 'John Doe',
      },
    },
  }),
}));

// Mock the Dashboard component
jest.mock('../components/Dashboard', () => ({
  Dashboard: ({ user }: { user: User }) => (
    <div data-testid="dashboard">Dashboard for {user.name}</div>
  ),
}));

// Mock the LoadingSpinner component
jest.mock('../components/LoadingSpinner', () => ({
  LoadingSpinner: ({ message }: { message?: string }) => (
    <div data-testid="loading-spinner">{message || 'Loading...'}</div>
  ),
}));

const renderWithProviders = (component: React.ReactElement) => {
  return render(<MemoryRouter>{component}</MemoryRouter>);
};

describe('DashboardPage', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('should render loading spinner when loading', () => {
    mockUseAuth.mockReturnValue({
      user: null,
      isLoading: true,
    });

    renderWithProviders(<DashboardPage />);

    expect(screen.getByTestId('loading-spinner')).toBeInTheDocument();
  });

  it('should render loading message when user has no profile', () => {
    mockUseAuth.mockReturnValue({
      user: null,
      isLoading: false,
    });

    renderWithProviders(<DashboardPage />);

    expect(screen.getByText('Loading Dashboard')).toBeInTheDocument();
    expect(
      screen.getByText(/Please ensure you have a profile to access the dashboard/)
    ).toBeInTheDocument();
  });

  it('should render dashboard when user has a profile', () => {
    mockUseAuth.mockReturnValue({
      user: mockUser,
      isLoading: false,
    });

    renderWithProviders(<DashboardPage />);

    expect(screen.getByTestId('dashboard')).toBeInTheDocument();
    expect(screen.getByText('Dashboard for Test User')).toBeInTheDocument();
  });
});
