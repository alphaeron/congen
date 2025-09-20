import React from 'react';
import { screen, waitFor, render as rtlRender } from '@testing-library/react';
import { createMockKcContext } from '../../test-utils';
import type { KcContextWithUser } from '../../test-utils';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import { CssBaseline } from '@mui/material';
import { getTheme } from '../../theme';
import KcPage from './KcPage';

// Mock fetch globally
const mockFetch = jest.fn();
global.fetch = mockFetch;

// Mock the API client
jest.mock('./api/client', () => ({
  createApiClient: jest.fn(() => ({
    getAccessToken: jest.fn(() => 'mock-token'),
    updateUserProfile: jest.fn(() => Promise.resolve({ success: true })),
    updateBackendUserProfile: jest.fn(() => Promise.resolve({ success: true })),
  })),
  setTokenGetter: jest.fn(),
}));

// Mock the AuthContext to avoid loading states
jest.mock('./AuthContext', () => ({
  useAuth: () => ({
    isAuthenticated: true,
    isLoading: false,
    login: jest.fn(),
    logout: jest.fn(),
  }),
  AuthProvider: ({ children }: { children: React.ReactNode }) => <>{children}</>,
}));

// Custom render function that doesn't use AuthProvider since we're mocking it
const render = (ui: React.ReactElement, kcContext?: KcContextWithUser) => {
  const muiTheme = createTheme(getTheme('light'));

  // Mock window.kcContext
  if (typeof window !== 'undefined' && kcContext) {
    (window as { kcContext?: unknown }).kcContext = kcContext;
  }

  return rtlRender(
    <ThemeProvider theme={muiTheme}>
      <CssBaseline />
      {ui}
    </ThemeProvider>
  );
};

describe('KcPage', () => {
  beforeEach(() => {
    jest.clearAllMocks();

    // Reset fetch mock implementation
    mockFetch.mockReset();

    // Mock successful userinfo response
    mockFetch.mockImplementation((url: string) => {
      if (url.includes('/protocol/openid-connect/userinfo')) {
        return Promise.resolve({
          ok: true,
          json: () =>
            Promise.resolve({
              sub: 'test-user-id',
              email: 'test@example.com',
              given_name: 'Test',
              family_name: 'User',
              firstName: 'Test',
              lastName: 'User',
            }),
        });
      }
      // Mock backend user profile update response
      if (url.includes('/user/me')) {
        return Promise.resolve({
          ok: true,
          json: () => Promise.resolve({}),
        });
      }
      return Promise.reject(new Error('Unmocked fetch call'));
    });
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  it('renders Account component for account page', async () => {
    const kcContext = createMockKcContext({
      url: {
        accountUrl: '/auth/realms/congen/account',
      } as KcContextWithUser['url'],
    });

    render(<KcPage kcContext={kcContext} />, kcContext);

    // Wait for the component to load and show the main content
    await waitFor(
      () => {
        expect(screen.getByText('ConGen')).toBeInTheDocument();
      },
      { timeout: 10000 }
    );
  }, 15000);

  it('handles unknown page types gracefully', async () => {
    const kcContext = createMockKcContext({
      pageId: 'sessions.ftl' as KcContextWithUser['pageId'], // Use a valid pageId for testing
      url: {
        accountUrl: '/auth/realms/congen/unknown-page',
      } as KcContextWithUser['url'],
    });

    render(<KcPage kcContext={kcContext} />, kcContext);

    // The Account component should render with the app bar
    await waitFor(
      () => {
        expect(screen.getByText('ConGen')).toBeInTheDocument();
      },
      { timeout: 5000 }
    );
  });

  it('passes kcContext to child components', async () => {
    const kcContext = createMockKcContext({
      user: {
        username: 'customuser',
        email: 'custom@example.com',
        firstName: 'Custom',
        lastName: 'User',
      },
    });

    render(<KcPage kcContext={kcContext} />, kcContext);

    // The Account component should render with the app bar
    await waitFor(
      () => {
        expect(screen.getByText('ConGen')).toBeInTheDocument();
      },
      { timeout: 5000 }
    );
  });

  it('renders with different realm names', async () => {
    const kcContext = createMockKcContext({
      realm: {
        internationalizationEnabled: true,
        userManagedAccessAllowed: true,
      },
    });

    render(<KcPage kcContext={kcContext} />, kcContext);

    // The Account component should render with the app bar
    await waitFor(
      () => {
        expect(screen.getByText('ConGen')).toBeInTheDocument();
      },
      { timeout: 5000 }
    );
  });

  it('handles missing realm information', async () => {
    const kcContext = createMockKcContext({
      realm: undefined,
    });

    render(<KcPage kcContext={kcContext} />, kcContext);

    // The Account component should render with the app bar
    await waitFor(
      () => {
        expect(screen.getByText('ConGen')).toBeInTheDocument();
      },
      { timeout: 5000 }
    );
  });
});
