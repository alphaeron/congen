// Mock API functions first
const mockRegisterUser = jest.fn();

jest.mock('../api/user', () => ({
  registerUser: jest.fn(),
  getCurrentUser: jest.fn(),
}));

jest.mock('../api/endpoint', () => ({
  setTokenGetter: jest.fn(),
}));

// Mock authUtils
const mockDecodeToken = jest.fn();
jest.mock('../common/authUtils', () => ({
  decodeToken: jest.fn(),
}));

// Mock react-router-dom
const mockNavigate = jest.fn();
jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useNavigate: () => mockNavigate,
}));

// Mock react-oidc-context
const mockOidcAuth: {
  user: unknown;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: unknown;
  signinRedirect: jest.Mock;
  removeUser: jest.Mock;
} = {
  user: null,
  isAuthenticated: false,
  isLoading: false,
  error: null,
  signinRedirect: jest.fn(),
  removeUser: jest.fn(),
};

jest.mock('react-oidc-context', () => ({
  useAuth: () => mockOidcAuth,
}));

import React from 'react';
import { render, screen, waitFor, act } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';

// Import after mocks are set up
import { AuthProvider, useAuth } from './AuthContext';

// Get the mocked functions
// eslint-disable-next-line @typescript-eslint/no-require-imports
const endpointModule = require('../api/endpoint') as jest.Mocked<typeof import('../api/endpoint')>;
// eslint-disable-next-line @typescript-eslint/no-require-imports
const userModule = require('../api/user') as jest.Mocked<typeof import('../api/user')>;
// eslint-disable-next-line @typescript-eslint/no-require-imports
const authUtilsModule = require('../common/authUtils') as jest.Mocked<
  typeof import('../common/authUtils')
>;

// Test component to access auth context
const TestComponent: React.FC = () => {
  const auth = useAuth();
  return (
    <div>
      <div data-testid="isAuthenticated">{auth.isAuthenticated.toString()}</div>
      <div data-testid="isLoading">{auth.isLoading.toString()}</div>
      <div data-testid="error">{auth.error || 'no-error'}</div>
      <div data-testid="user">{auth.user ? JSON.stringify(auth.user) : 'no-user'}</div>
      <button onClick={() => auth.login()}>Login</button>
      <button onClick={() => auth.logout()}>Logout</button>
      <button
        onClick={() =>
          auth.register({
            name: 'Test User',
            age: 25,
            height: 180,
            weight: 80,
            email: 'test@example.com',
            password: 'password123',
            unit: 'metric',
          })
        }
      >
        Register
      </button>
      <button onClick={() => auth.clearError()}>Clear Error</button>
    </div>
  );
};

const TestWrapper: React.FC<{ children: React.ReactNode }> = ({ children }) => (
  <MemoryRouter>
    <AuthProvider>{children}</AuthProvider>
  </MemoryRouter>
);

describe('AuthContext', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockOidcAuth.user = null;
    mockOidcAuth.isAuthenticated = false;
    mockOidcAuth.isLoading = false;
    mockOidcAuth.error = null;
    mockDecodeToken.mockReturnValue({ groups: [], roles: [] });
  });

  describe('useAuth hook', () => {
    it('should throw error when used outside AuthProvider', () => {
      // Suppress console.error for this test
      const consoleSpy = jest.spyOn(console, 'error').mockImplementation(() => {});

      expect(() => {
        render(<TestComponent />);
      }).toThrow('useAuth must be used within an AuthProvider');

      consoleSpy.mockRestore();
    });

    it('should provide auth context when used within AuthProvider', () => {
      render(
        <TestWrapper>
          <TestComponent />
        </TestWrapper>
      );

      expect(screen.getByTestId('isAuthenticated')).toBeInTheDocument();
      expect(screen.getByTestId('isLoading')).toBeInTheDocument();
      expect(screen.getByTestId('error')).toBeInTheDocument();
      expect(screen.getByTestId('user')).toBeInTheDocument();
    });
  });

  describe('token getter registration', () => {
    it('should register token getter when OIDC user changes', () => {
      mockOidcAuth.user = {
        access_token: 'test-token',
        profile: { name: 'Test User' },
      };

      render(
        <TestWrapper>
          <TestComponent />
        </TestWrapper>
      );

      expect(endpointModule.setTokenGetter).toHaveBeenCalledWith(expect.any(Function));
    });

    it('should register null token getter when no OIDC user', () => {
      render(
        <TestWrapper>
          <TestComponent />
        </TestWrapper>
      );

      expect(endpointModule.setTokenGetter).toHaveBeenCalledWith(expect.any(Function));
    });
  });

  describe('user profile synchronization', () => {
    it('should sync user profile when OIDC user is authenticated', async () => {
      const mockUser = {
        id: 1,
        name: 'Test User',
        age: 25,
        height: 180,
        weight: 80,
        groups: ['user'],
        roles: ['user'],
        created_at: '2024-01-01T00:00:00Z',
        updated_at: '2024-01-01T00:00:00Z',
      };

      mockOidcAuth.user = {
        access_token: 'test-token',
        profile: { name: 'Test User' },
      };
      mockOidcAuth.isAuthenticated = true;
      userModule.getCurrentUser.mockResolvedValue(mockUser);
      authUtilsModule.decodeToken.mockReturnValue({
        groups: ['admin'],
        realm_access: { roles: ['admin'] },
      });

      render(
        <TestWrapper>
          <TestComponent />
        </TestWrapper>
      );

      await waitFor(() => {
        expect(userModule.getCurrentUser).toHaveBeenCalled();
      });

      await waitFor(() => {
        const userElement = screen.getByTestId('user');
        const userData = JSON.parse(userElement.textContent || '{}');
        expect(userData.groups).toEqual(['admin']);
        expect(userData.roles).toEqual(['admin']);
      });
    });

    it('should handle backend profile fetch failure gracefully', async () => {
      mockOidcAuth.user = { access_token: 'test-token', profile: { name: 'Test User' } };
      mockOidcAuth.isAuthenticated = true;
      userModule.getCurrentUser.mockRejectedValue(new Error('Backend error'));
      authUtilsModule.decodeToken.mockReturnValue({
        groups: ['user'],
        realm_access: { roles: ['user'] },
      });

      render(
        <TestWrapper>
          <TestComponent />
        </TestWrapper>
      );

      await waitFor(() => {
        expect(userModule.getCurrentUser).toHaveBeenCalled();
      });

      await waitFor(() => {
        const userElement = screen.getByTestId('user');
        const userData = JSON.parse(userElement.textContent || '{}');
        expect(userData.name).toBe('Test User');
        expect(userData.groups).toEqual(['user']);
        expect(userData.roles).toEqual(['user']);
      });
    });

    it('should clear user when OIDC user is not authenticated', () => {
      render(
        <TestWrapper>
          <TestComponent />
        </TestWrapper>
      );

      expect(screen.getByTestId('user')).toHaveTextContent('no-user');
    });
  });

  describe('error handling', () => {
    it('should set error when OIDC error occurs', () => {
      mockOidcAuth.error = { message: 'OIDC error' };

      render(
        <TestWrapper>
          <TestComponent />
        </TestWrapper>
      );

      expect(screen.getByTestId('error')).toHaveTextContent('OIDC error');
    });

    it('should clear error when clearError is called', () => {
      mockOidcAuth.error = { message: 'OIDC error' };

      render(
        <TestWrapper>
          <TestComponent />
        </TestWrapper>
      );

      const clearErrorButton = screen.getByText('Clear Error');
      act(() => {
        clearErrorButton.click();
      });

      expect(screen.getByTestId('error')).toHaveTextContent('no-error');
    });
  });

  describe('login functionality', () => {
    it('should call OIDC signinRedirect when login is called', async () => {
      mockOidcAuth.signinRedirect.mockResolvedValue(undefined);

      render(
        <TestWrapper>
          <TestComponent />
        </TestWrapper>
      );

      const loginButton = screen.getByText('Login');
      await act(async () => {
        loginButton.click();
      });

      expect(mockOidcAuth.signinRedirect).toHaveBeenCalled();
    });

    it('should handle login errors', async () => {
      const loginError = new Error('Login failed');
      mockOidcAuth.signinRedirect.mockRejectedValue(loginError);

      render(
        <TestWrapper>
          <TestComponent />
        </TestWrapper>
      );

      const loginButton = screen.getByText('Login');
      await act(async () => {
        loginButton.click();
      });

      await waitFor(() => {
        expect(screen.getByTestId('error')).toHaveTextContent('Login failed');
      });
    });
  });

  describe('logout functionality', () => {
    it('should call OIDC removeUser when logout is called', async () => {
      mockOidcAuth.removeUser.mockResolvedValue(undefined);

      render(
        <TestWrapper>
          <TestComponent />
        </TestWrapper>
      );

      const logoutButton = screen.getByText('Logout');
      await act(async () => {
        logoutButton.click();
      });

      expect(mockOidcAuth.removeUser).toHaveBeenCalled();
    });

    it('should handle logout errors gracefully', async () => {
      const logoutError = new Error('Logout failed');
      mockOidcAuth.removeUser.mockRejectedValue(logoutError);

      render(
        <TestWrapper>
          <TestComponent />
        </TestWrapper>
      );

      const logoutButton = screen.getByText('Logout');
      await act(async () => {
        logoutButton.click();
      });

      // Should not throw error, just log it
      expect(mockOidcAuth.removeUser).toHaveBeenCalled();
    });
  });

  describe('registration functionality', () => {
    it('should register user and attempt automatic login', async () => {
      const mockNewUser = {
        id: 1,
        name: 'Test User',
        age: 25,
        height: 180,
        weight: 80,
        created_at: '2023-01-01T00:00:00Z',
        updated_at: '2023-01-01T00:00:00Z',
      };
      mockRegisterUser.mockResolvedValue(mockNewUser);
      mockOidcAuth.signinRedirect.mockResolvedValue(undefined);

      render(
        <TestWrapper>
          <TestComponent />
        </TestWrapper>
      );

      const registerButton = screen.getByText('Register');
      await act(async () => {
        registerButton.click();
      });

      expect(userModule.registerUser).toHaveBeenCalledWith(
        'Test User',
        25,
        180,
        80,
        'test@example.com',
        'password123',
        'metric'
      );

      expect(mockOidcAuth.signinRedirect).toHaveBeenCalledWith({
        extraQueryParams: {
          login_hint: 'test@example.com',
        },
      });
    });

    it('should handle registration errors', async () => {
      const registrationError = { response: { data: { message: 'Registration failed' } } };
      userModule.registerUser.mockRejectedValue(registrationError);

      render(
        <TestWrapper>
          <TestComponent />
        </TestWrapper>
      );

      const registerButton = screen.getByText('Register');
      await act(async () => {
        registerButton.click();
      });

      await waitFor(() => {
        expect(screen.getByTestId('error')).toHaveTextContent('Registration failed');
      });
    });

    it('should handle automatic login failure after registration', async () => {
      const mockNewUser = {
        id: 1,
        name: 'Test User',
        age: 25,
        height: 180,
        weight: 80,
        created_at: '2023-01-01T00:00:00Z',
        updated_at: '2023-01-01T00:00:00Z',
      };
      userModule.registerUser.mockResolvedValue(mockNewUser);
      const loginError = new Error('Auto-login failed');
      mockOidcAuth.signinRedirect.mockRejectedValue(loginError);

      render(
        <TestWrapper>
          <TestComponent />
        </TestWrapper>
      );

      const registerButton = screen.getByText('Register');
      await act(async () => {
        registerButton.click();
      });

      expect(userModule.registerUser).toHaveBeenCalled();
      expect(mockOidcAuth.signinRedirect).toHaveBeenCalled();
      expect(mockNavigate).toHaveBeenCalledWith('/login', {
        state: {
          message: 'Registration successful! Please sign in with your new account.',
          email: 'test@example.com',
        },
        replace: true,
      });
    });
  });

  describe('loading states', () => {
    it('should reflect OIDC loading state', () => {
      mockOidcAuth.isLoading = true;

      render(
        <TestWrapper>
          <TestComponent />
        </TestWrapper>
      );

      expect(screen.getByTestId('isLoading')).toHaveTextContent('true');
    });

    it('should reflect OIDC authentication state', () => {
      mockOidcAuth.isAuthenticated = true;

      render(
        <TestWrapper>
          <TestComponent />
        </TestWrapper>
      );

      expect(screen.getByTestId('isAuthenticated')).toHaveTextContent('true');
    });
  });
});
