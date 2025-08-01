import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { AuthCallback } from './AuthCallback';

// Mock react-router-dom
const mockNavigate = jest.fn();
const mockLocation = { search: '' };

jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useNavigate: () => mockNavigate,
  useLocation: () => mockLocation,
}));

// Mock react-oidc-context
const mockOidcAuth: {
  isLoading: boolean;
  isAuthenticated: boolean;
  user: unknown;
  error: unknown;
} = {
  isLoading: false,
  isAuthenticated: false,
  user: null,
  error: null,
};

jest.mock('react-oidc-context', () => ({
  useAuth: () => mockOidcAuth,
}));

const TestWrapper: React.FC<{ children: React.ReactNode }> = ({ children }) => (
  <MemoryRouter>{children}</MemoryRouter>
);

describe('AuthCallback', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockOidcAuth.isLoading = false;
    mockOidcAuth.isAuthenticated = false;
    mockOidcAuth.user = null;
    mockOidcAuth.error = null;
    mockLocation.search = '';
  });

  describe('URL error handling', () => {
    it('should redirect to login with error when URL contains error parameter', async () => {
      mockLocation.search = '?error=access_denied';

      render(
        <TestWrapper>
          <AuthCallback />
        </TestWrapper>
      );

      await waitFor(() => {
        expect(mockNavigate).toHaveBeenCalledWith('/login', {
          state: { error: 'Authentication failed: access_denied' },
          replace: true,
        });
      });
    });

    it('should handle different error types', async () => {
      mockLocation.search = '?error=invalid_request';

      render(
        <TestWrapper>
          <AuthCallback />
        </TestWrapper>
      );

      await waitFor(() => {
        expect(mockNavigate).toHaveBeenCalledWith('/login', {
          state: { error: 'Authentication failed: invalid_request' },
          replace: true,
        });
      });
    });
  });

  describe('loading state handling', () => {
    it('should show loading spinner while OIDC is processing', () => {
      mockOidcAuth.isLoading = true;

      render(
        <TestWrapper>
          <AuthCallback />
        </TestWrapper>
      );

      expect(screen.getByText('Processing authentication...')).toBeInTheDocument();
      expect(screen.getByRole('progressbar')).toBeInTheDocument();
    });

    it('should not redirect while loading', async () => {
      mockOidcAuth.isLoading = true;

      render(
        <TestWrapper>
          <AuthCallback />
        </TestWrapper>
      );

      // Wait a bit to ensure no redirect happens
      await new Promise(resolve => setTimeout(resolve, 100));

      expect(mockNavigate).not.toHaveBeenCalled();
    });
  });

  describe('successful authentication', () => {
    it('should redirect to profile when authentication is successful', async () => {
      mockOidcAuth.isAuthenticated = true;
      mockOidcAuth.user = { access_token: 'test-token' };

      // Mock window.history.replaceState
      const mockReplaceState = jest.fn();
      Object.defineProperty(window, 'history', {
        value: { replaceState: mockReplaceState },
        writable: true,
      });

      render(
        <TestWrapper>
          <AuthCallback />
        </TestWrapper>
      );

      await waitFor(() => {
        expect(mockReplaceState).toHaveBeenCalledWith({}, document.title, '/profile');
        expect(mockNavigate).toHaveBeenCalledWith('/profile', { replace: true });
      });
    });

    it('should clean up URL before redirecting', async () => {
      mockOidcAuth.isAuthenticated = true;
      mockOidcAuth.user = { access_token: 'test-token' };

      const mockReplaceState = jest.fn();
      Object.defineProperty(window, 'history', {
        value: { replaceState: mockReplaceState },
        writable: true,
      });

      render(
        <TestWrapper>
          <AuthCallback />
        </TestWrapper>
      );

      await waitFor(() => {
        expect(mockReplaceState).toHaveBeenCalledWith({}, document.title, '/profile');
      });
    });
  });

  describe('authentication error handling', () => {
    it('should redirect to login when OIDC error occurs', async () => {
      mockOidcAuth.error = { message: 'Authentication failed' };

      render(
        <TestWrapper>
          <AuthCallback />
        </TestWrapper>
      );

      await waitFor(() => {
        expect(mockNavigate).toHaveBeenCalledWith('/login', {
          state: { error: 'Authentication failed' },
          replace: true,
        });
      });
    });

    it('should handle different error message formats', async () => {
      mockOidcAuth.error = { message: 'Network error occurred' };

      render(
        <TestWrapper>
          <AuthCallback />
        </TestWrapper>
      );

      await waitFor(() => {
        expect(mockNavigate).toHaveBeenCalledWith('/login', {
          state: { error: 'Network error occurred' },
          replace: true,
        });
      });
    });
  });

  describe('fallback handling', () => {
    it('should redirect to login with generic error for unexpected states', async () => {
      // No URL error, not loading, not authenticated, no OIDC error
      // This represents an unexpected state

      render(
        <TestWrapper>
          <AuthCallback />
        </TestWrapper>
      );

      await waitFor(() => {
        expect(mockNavigate).toHaveBeenCalledWith('/login', {
          state: { error: 'Authentication failed. Please try again.' },
          replace: true,
        });
      });
    });

    it('should handle case where user is authenticated but no user object', async () => {
      mockOidcAuth.isAuthenticated = true;
      mockOidcAuth.user = null;

      render(
        <TestWrapper>
          <AuthCallback />
        </TestWrapper>
      );

      await waitFor(() => {
        expect(mockNavigate).toHaveBeenCalledWith('/login', {
          state: { error: 'Authentication failed. Please try again.' },
          replace: true,
        });
      });
    });
  });

  describe('component rendering', () => {
    it('should always render loading spinner initially', () => {
      render(
        <TestWrapper>
          <AuthCallback />
        </TestWrapper>
      );

      expect(screen.getByText('Processing authentication...')).toBeInTheDocument();
      expect(screen.getByRole('progressbar')).toBeInTheDocument();
    });

    it('should use full height loading spinner', () => {
      render(
        <TestWrapper>
          <AuthCallback />
        </TestWrapper>
      );

      const loadingSpinner = screen.getByRole('progressbar').closest('div');
      expect(loadingSpinner).toHaveStyle({ height: '100vh' });
    });
  });

  describe('timing and async behavior', () => {
    it('should wait for OIDC processing before making decisions', async () => {
      // Start with loading, then become authenticated
      mockOidcAuth.isLoading = true;

      render(
        <TestWrapper>
          <AuthCallback />
        </TestWrapper>
      );

      // Should not redirect while loading
      await new Promise(resolve => setTimeout(resolve, 50));
      expect(mockNavigate).not.toHaveBeenCalled();

      // Simulate OIDC finishing
      mockOidcAuth.isLoading = false;
      mockOidcAuth.isAuthenticated = true;
      mockOidcAuth.user = { access_token: 'test-token' };

      const mockReplaceState = jest.fn();
      Object.defineProperty(window, 'history', {
        value: { replaceState: mockReplaceState },
        writable: true,
      });

      // Wait for the effect to run again
      await waitFor(() => {
        expect(mockReplaceState).toHaveBeenCalled();
      });
    });

    it('should handle rapid state changes gracefully', async () => {
      // Simulate rapid state changes
      mockOidcAuth.isLoading = true;
      mockOidcAuth.isAuthenticated = false;
      mockOidcAuth.user = null;

      render(
        <TestWrapper>
          <AuthCallback />
        </TestWrapper>
      );

      // Change state rapidly
      mockOidcAuth.isLoading = false;
      mockOidcAuth.isAuthenticated = true;
      mockOidcAuth.user = { access_token: 'test-token' };

      const mockReplaceState = jest.fn();
      Object.defineProperty(window, 'history', {
        value: { replaceState: mockReplaceState },
        writable: true,
      });

      await waitFor(() => {
        expect(mockReplaceState).toHaveBeenCalled();
      });
    });
  });

  describe('edge cases', () => {
    it('should handle empty search string', async () => {
      mockLocation.search = '';

      render(
        <TestWrapper>
          <AuthCallback />
        </TestWrapper>
      );

      // Should not redirect due to URL error
      await new Promise(resolve => setTimeout(resolve, 100));
      expect(mockNavigate).toHaveBeenCalledWith('/login', {
        state: { error: 'Authentication failed. Please try again.' },
        replace: true,
      });
    });

    it('should handle search string with no error parameter', async () => {
      mockLocation.search = '?code=some-auth-code&state=some-state';

      render(
        <TestWrapper>
          <AuthCallback />
        </TestWrapper>
      );

      // Should not redirect due to URL error
      await new Promise(resolve => setTimeout(resolve, 100));
      expect(mockNavigate).toHaveBeenCalledWith('/login', {
        state: { error: 'Authentication failed. Please try again.' },
        replace: true,
      });
    });

    it('should handle multiple error parameters', async () => {
      mockLocation.search = '?error=access_denied&error_description=User+cancelled';

      render(
        <TestWrapper>
          <AuthCallback />
        </TestWrapper>
      );

      await waitFor(() => {
        expect(mockNavigate).toHaveBeenCalledWith('/login', {
          state: { error: 'Authentication failed: access_denied' },
          replace: true,
        });
      });
    });
  });
});
