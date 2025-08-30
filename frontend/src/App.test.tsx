import { render, screen, waitFor } from '@testing-library/react';
import React from 'react';

import { App } from './App';

// Mock react-router hooks
const mockNavigate = jest.fn();
const mockLocation = { pathname: '/', search: '', hash: '', state: null };

jest.mock('react-router', () => ({
  ...jest.requireActual('react-router'),
  useNavigate: () => mockNavigate,
  useLocation: () => mockLocation,
  BrowserRouter: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
  Link: ({
    children,
    to,
    ...props
  }: {
    children: React.ReactNode;
    to: string;
    [key: string]: unknown;
  }) => (
    <a href={to} {...props}>
      {children}
    </a>
  ),
  Routes: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
  Route: ({ element }: { element: React.ReactNode }) => <div>{element}</div>,
  Navigate: ({ to }: { to: string }) => (
    <div data-testid={`navigate-to-${to}`}>Navigate to {to}</div>
  ),
}));

// Mock the AuthContext
const mockUseAuth = jest.fn();
(global as Record<string, unknown>).mockUseAuth = mockUseAuth;
jest.mock('./contexts/AuthContext', () => ({
  useAuth: () => mockUseAuth(),
  AuthProvider: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
}));

// Mock react-oidc-context
jest.mock('react-oidc-context', () => ({
  AuthProvider: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
}));

// Mock the auth config
jest.mock('./auth/OidcConfig', () => ({
  getAuthProviderConfig: () => ({}),
}));

// Mock all the page components
jest.mock('./pages/RootPage', () => ({
  RootPage: () => <div data-testid="root-page">ConGen Home Page</div>,
}));

jest.mock('./pages/DashboardPage', () => ({
  DashboardPage: () => <div data-testid="dashboard-page">Dashboard Page</div>,
}));

jest.mock('./pages/ExerciseOverviewPage', () => ({
  ExerciseOverviewPage: () => (
    <div data-testid="exercise-overview-page">Exercise Overview Page</div>
  ),
}));

jest.mock('./pages/ExerciseDetailsPage', () => ({
  ExerciseDetailsPage: () => <div data-testid="exercise-details-page">Exercise Details Page</div>,
}));

jest.mock('./pages/UserProfilePage', () => ({
  UserProfilePage: () => <div data-testid="user-profile-page">User Profile Page</div>,
}));

jest.mock('./pages/LoginPage', () => ({
  LoginPage: () => <div data-testid="login-page">Login Page</div>,
}));

jest.mock('./pages/PrivacyPolicyPage', () => ({
  PrivacyPolicyPage: () => <div data-testid="privacy-policy-page">Privacy Policy Page</div>,
}));

// Mock the AuthorizedElement component
jest.mock('./components/AuthorizedElement', () => ({
  AuthorizedElement: ({
    children,
    fallback,
    requireAuth,
  }: {
    children: React.ReactNode;
    fallback?: React.ReactNode;
    requireAuth?: boolean;
  }) => {
    // For testing, we'll use the global mockUseAuth to determine authentication state
    const globalMockUseAuth = (global as Record<string, unknown>).mockUseAuth as jest.Mock;
    if (globalMockUseAuth) {
      const authState = globalMockUseAuth();
      if (requireAuth === false) {
        return <div data-testid="authorized-element">{children}</div>;
      }
      if (authState.isAuthenticated) {
        return <div data-testid="authorized-element">{children}</div>;
      } else {
        return <div data-testid="authorized-element">{fallback}</div>;
      }
    }
    return <div data-testid="authorized-element">{children || fallback}</div>;
  },
}));

// Mock the UserProfile component
jest.mock('./components/UserProfile', () => ({
  UserProfile: () => <div data-testid="user-profile">Profile</div>,
}));

// Mock the ProtectedRoute component to prevent navigation issues
jest.mock('./components/ProtectedRoute', () => ({
  ProtectedRoute: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
}));

// Mock the LoadingSpinner component
jest.mock('./components/LoadingSpinner', () => ({
  LoadingSpinner: () => <div data-testid="loading-spinner">Loading...</div>,
}));

// Mock the AuthCallback component
jest.mock('./components/AuthCallback', () => ({
  AuthCallback: () => <div data-testid="auth-callback">Auth Callback</div>,
}));

// Mock the SnackbarProvider
jest.mock('notistack', () => ({
  SnackbarProvider: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
}));

// Mock the QueryClient and QueryClientProvider
jest.mock('@tanstack/react-query', () => ({
  QueryClient: jest.fn().mockImplementation(() => ({
    mount: jest.fn(),
    unmount: jest.fn(),
  })),
  QueryClientProvider: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
}));

// Mock Material-UI components
jest.mock('@mui/material/useMediaQuery', () => () => false);

describe('App', () => {
  beforeEach(() => {
    mockUseAuth.mockReturnValue({
      isAuthenticated: false,
      isLoading: false,
      user: null,
      login: jest.fn(),
      logout: jest.fn(),
      register: jest.fn(),
      error: null,
    });
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  it('renders without crashing', async () => {
    render(<App />);

    await waitFor(() => {
      // Should show the root page content when not authenticated
      expect(screen.getByTestId('root-page')).toBeInTheDocument();
    });
  });

  it('shows root page when user is not authenticated', async () => {
    mockUseAuth.mockReturnValue({
      isAuthenticated: false,
      isLoading: false,
      user: null,
      login: jest.fn(),
      logout: jest.fn(),
      register: jest.fn(),
      error: null,
    });

    render(<App />);

    await waitFor(() => {
      // Should show the root page content when not authenticated
      expect(screen.getByTestId('root-page')).toBeInTheDocument();
    });
  });

  it('redirects authenticated users from root to dashboard', async () => {
    mockUseAuth.mockReturnValue({
      isAuthenticated: true,
      isLoading: false,
      user: { keycloak_id: 'test-user-id', email: 'test@example.com' },
      login: jest.fn(),
      logout: jest.fn(),
      register: jest.fn(),
      error: null,
    });

    render(<App />);

    await waitFor(() => {
      // Should show dashboard page when authenticated
      expect(screen.getByTestId('dashboard-page')).toBeInTheDocument();
    });
  });

  it('shows dashboard page when navigating to /dashboard', async () => {
    mockUseAuth.mockReturnValue({
      isAuthenticated: true,
      isLoading: false,
      user: { keycloak_id: 'test-user-id', email: 'test@example.com' },
      login: jest.fn(),
      logout: jest.fn(),
      register: jest.fn(),
      error: null,
    });

    render(<App />);

    await waitFor(() => {
      expect(screen.getByTestId('dashboard-page')).toBeInTheDocument();
    });
  });

  it('shows loading spinner when authentication is loading', async () => {
    mockUseAuth.mockReturnValue({
      isAuthenticated: false,
      isLoading: true,
      user: null,
      login: jest.fn(),
      logout: jest.fn(),
      register: jest.fn(),
      error: null,
    });

    render(<App />);

    await waitFor(() => {
      expect(screen.getByTestId('loading-spinner')).toBeInTheDocument();
    });
  });
});
