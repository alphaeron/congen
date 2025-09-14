import React from 'react';
import { render, RenderOptions } from '@testing-library/react';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import { CssBaseline } from '@mui/material';
import { getTheme } from './theme';
import type { KcContext } from './keycloak-theme/account/KcContext';
import { AuthProvider } from './keycloak-theme/account/AuthContext';

// Mock react-oidc-context
jest.mock('react-oidc-context', () => ({
  useAuth: () => ({
    isAuthenticated: true,
    isLoading: false,
    user: {
      access_token: 'mock-token',
      profile: {
        sub: 'test-user-id',
        email: 'test@example.com',
        given_name: 'Test',
        family_name: 'User',
      },
    },
    signinRedirect: jest.fn(),
    signoutRedirect: jest.fn(),
  }),
}));

// Extended KcContext with user information for testing
export type KcContextWithUser = KcContext & {
  pageId?: string;
  url?: {
    accountUrl?: string;
    passwordUrl?: string;
    totpUrl?: string;
    socialUrl?: string;
    sessionsUrl?: string;
    applicationsUrl?: string;
    logUrl?: string;
    logoutUrl?: string;
    resourceUrl?: string;
    resourcesCommonPath?: string;
    resourcesPath?: string;
    getLogoutUrl?: () => string;
  };
  realm?: {
    name?: string;
    internationalizationEnabled?: boolean;
    userManagedAccessAllowed?: boolean;
    registrationEmailAsUsername?: boolean;
    editUsernameAllowed?: boolean;
    isInternationalizationEnabled?: boolean;
    identityFederationEnabled?: boolean;
  };
  user?: {
    username?: string;
    email?: string;
    firstName?: string;
    lastName?: string;
  };
};

interface CustomRenderOptions extends Omit<RenderOptions, 'wrapper'> {
  kcContext?: KcContextWithUser;
  theme?: 'light' | 'dark';
}

// Default test KcContext
const defaultKcContext: KcContextWithUser = {
  pageId: 'account.ftl',
  url: {
    accountUrl: '/auth/realms/congen/account',
    passwordUrl: '/auth/realms/congen/account/password',
    totpUrl: '/auth/realms/congen/account/totp',
    socialUrl: '/auth/realms/congen/account/social',
    sessionsUrl: '/auth/realms/congen/account/sessions',
    applicationsUrl: '/auth/realms/congen/account/applications',
    logUrl: '/auth/realms/congen/account/log',
    logoutUrl: '/auth/realms/congen/account/logout',
    resourceUrl: '/auth/realms/congen/account/resource',
    resourcesCommonPath: '/auth/realms/congen/account/resources',
    resourcesPath: '/auth/realms/congen/account/resources',
    getLogoutUrl: () => '/auth/realms/congen/account/logout',
  },
  realm: {
    internationalizationEnabled: true,
    userManagedAccessAllowed: false,
  },
  user: {
    username: 'testuser',
    email: 'test@example.com',
    firstName: 'Test',
    lastName: 'User',
  },
} as KcContextWithUser;

// Test wrapper component
const TestWrapper: React.FC<{
  children: React.ReactNode;
  kcContext?: KcContextWithUser;
  theme?: 'light' | 'dark';
}> = ({ children, kcContext = defaultKcContext, theme = 'light' }) => {
  const muiTheme = createTheme(getTheme(theme));

  // Mock window.kcContext
  if (typeof window !== 'undefined') {
    (window as { kcContext?: unknown }).kcContext = kcContext;
  }

  return (
    <ThemeProvider theme={muiTheme}>
      <CssBaseline />
      <AuthProvider>{children}</AuthProvider>
    </ThemeProvider>
  );
};

// Custom render function
const customRender = (ui: React.ReactElement, options: CustomRenderOptions = {}) => {
  const { kcContext, theme, ...renderOptions } = options;

  return render(ui, {
    wrapper: ({ children }) => (
      <TestWrapper kcContext={kcContext} theme={theme}>
        {children}
      </TestWrapper>
    ),
    ...renderOptions,
  });
};

// Re-export everything
export * from '@testing-library/react';
export { customRender as render };

// Test utilities
export const createMockKcContext = (
  overrides: Partial<KcContextWithUser> = {}
): KcContextWithUser =>
  ({
    ...defaultKcContext,
    ...overrides,
  }) as KcContextWithUser;

export const createMockUser = (overrides: Partial<KcContextWithUser['user']> = {}) => ({
  username: 'testuser',
  email: 'test@example.com',
  firstName: 'Test',
  lastName: 'User',
  ...overrides,
});
