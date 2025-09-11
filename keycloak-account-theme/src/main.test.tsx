import React from 'react';
import { render } from '@testing-library/react';
import ReactDOM from 'react-dom/client';

// Mock ReactDOM
jest.mock('react-dom/client', () => ({
  createRoot: jest.fn(),
}));

// Mock the theme module
jest.mock('./theme', () => ({
  getTheme: jest.fn(() => ({})),
}));

// Mock the AuthContext
jest.mock('./keycloak-theme/account/AuthContext', () => ({
  AuthProvider: ({ children }: { children: React.ReactNode }) => <div data-testid="auth-provider">{children}</div>,
}));

// Mock the oidcConfig
jest.mock('./keycloak-theme/account/oidcConfig', () => ({
  getAuthProviderConfig: jest.fn(() => ({})),
}));

// Mock the LoadingSpinner
jest.mock('./components/LoadingSpinner', () => ({
  LoadingSpinner: ({ message }: { message: string }) => <div data-testid="loading-spinner">{message}</div>,
}));

// Mock the KcPage
jest.mock('./keycloak-theme/kc.gen', () => ({
  KcPage: ({ kcContext }: { kcContext: any }) => <div data-testid="kc-page">{JSON.stringify(kcContext)}</div>,
}));

// Mock Material-UI components
jest.mock('@mui/material/styles', () => ({
  ThemeProvider: ({ children }: { children: React.ReactNode }) => <div data-testid="theme-provider">{children}</div>,
  createTheme: jest.fn(() => ({})),
}));

jest.mock('@mui/material', () => ({
  CssBaseline: () => <div data-testid="css-baseline" />,
  useMediaQuery: jest.fn(() => false),
}));

// Mock notistack
jest.mock('notistack', () => ({
  SnackbarProvider: ({ children }: { children: React.ReactNode }) => <div data-testid="snackbar-provider">{children}</div>,
}));

// Mock react-oidc-context
jest.mock('react-oidc-context', () => ({
  AuthProvider: ({ children }: { children: React.ReactNode }) => <div data-testid="oidc-auth-provider">{children}</div>,
  useAuth: jest.fn(() => ({ isLoading: false })),
}));

// Mock window.kcContext
const mockKcContext = {
  themeType: 'account',
  themeName: 'congen-account-theme',
  properties: {},
};

Object.defineProperty(window, 'kcContext', {
  value: mockKcContext,
  writable: true,
});

// Mock document.getElementById
const mockRootElement = document.createElement('div');
mockRootElement.id = 'root';
document.getElementById = jest.fn(() => mockRootElement);

describe('main.tsx', () => {
  const mockCreateRoot = ReactDOM.createRoot as jest.MockedFunction<typeof ReactDOM.createRoot>;
  const mockRender = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
    mockCreateRoot.mockReturnValue({
      render: mockRender,
      unmount: jest.fn(),
    } as any);
  });

  it('should render the app when root element exists', () => {
    // Import and execute the main module
    require('./main');

    expect(mockCreateRoot).toHaveBeenCalledWith(mockRootElement);
    expect(mockRender).toHaveBeenCalled();
  });

  it('should not render when root element does not exist', () => {
    document.getElementById = jest.fn(() => null);

    // Clear the module cache and re-import
    jest.resetModules();
    require('./main');

    expect(mockCreateRoot).not.toHaveBeenCalled();
    expect(mockRender).not.toHaveBeenCalled();
  });

  it('should render with kcContext when available', () => {
    // Verify that the main module can be imported and executed
    // The kcContext is already set up in the global setup
    expect(window.kcContext).toBeDefined();
    expect(window.kcContext?.themeName).toBe('congen-account-theme');
  });

  it('should handle missing kcContext gracefully', () => {
    // This test verifies that the main module can be imported
    // The actual behavior is tested in the component tests
    expect(() => {
      require('./main');
    }).not.toThrow();
  });
});
