import { render, screen, waitFor, act } from '@testing-library/react';
import MockAdapter from 'axios-mock-adapter';
import { SnackbarProvider } from 'notistack';
import React from 'react';
import { MemoryRouter } from 'react-router';

import { ProfileEditRedirect } from './ProfileEditRedirect';
import { ENDPOINT } from '../api/endpoint';

// Mock react-router
const mockNavigate = jest.fn();
jest.mock('react-router', () => ({
  ...jest.requireActual('react-router'),
  useNavigate: () => mockNavigate,
}));

// Mock react-oidc-context
jest.mock('react-oidc-context', () => ({
  useAuth: () => ({
    user: {
      profile: {
        name: 'Test User',
      },
    },
  }),
}));

describe('ProfileEditRedirect', () => {
  let mock: MockAdapter;

  beforeEach(() => {
    jest.clearAllMocks();
    mock = new MockAdapter(ENDPOINT);

    // Clear sessionStorage
    sessionStorage.clear();

    // Mock setTimeout and clearTimeout
    jest.useFakeTimers();
  });

  afterEach(() => {
    mock.restore();
    jest.useRealTimers();
  });

  it('should show loading state initially', () => {
    render(
      <MemoryRouter>
        <SnackbarProvider>
          <ProfileEditRedirect />
        </SnackbarProvider>
      </MemoryRouter>
    );

    expect(screen.getByText('Redirecting...')).toBeInTheDocument();
    expect(screen.getByText('Please wait while we process your request.')).toBeInTheDocument();
  });

  it('should redirect to profile when no redirect path is stored', async () => {
    render(
      <MemoryRouter>
        <SnackbarProvider>
          <ProfileEditRedirect />
        </SnackbarProvider>
      </MemoryRouter>
    );

    // Fast-forward timers within act
    await act(async () => {
      jest.advanceTimersByTime(100);
    });

    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith('/profile', { replace: true });
    });
  });

  it('should sync profile and redirect when redirect path exists', async () => {
    const redirectPath = '/dashboard';
    sessionStorage.setItem('congen_redirect_after_profile_edit', redirectPath);

    mock.onPatch('/user/me').reply(200, {});

    render(
      <MemoryRouter>
        <SnackbarProvider>
          <ProfileEditRedirect />
        </SnackbarProvider>
      </MemoryRouter>
    );

    // Fast-forward initial timer within act
    await act(async () => {
      jest.advanceTimersByTime(100);
    });

    await waitFor(() => {
      expect(mock.history.patch).toHaveLength(1);
      expect(mock.history.patch[0].url).toBe('/user/me');
      expect(mock.history.patch[0].params).toEqual({ name: 'Test User' });
    });

    // Fast-forward success message delay within act
    await act(async () => {
      jest.advanceTimersByTime(1500);
    });

    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith(redirectPath, { replace: true });
    });

    // Verify sessionStorage was cleared
    expect(sessionStorage.getItem('congen_redirect_after_profile_edit')).toBeNull();
  });

  it('should show loading spinner and success message when syncing', async () => {
    const redirectPath = '/dashboard';
    sessionStorage.setItem('congen_redirect_after_profile_edit', redirectPath);

    mock.onPatch('/user/me').reply(200, {});

    render(
      <MemoryRouter>
        <SnackbarProvider>
          <ProfileEditRedirect />
        </SnackbarProvider>
      </MemoryRouter>
    );

    // Fast-forward initial timer within act
    await act(async () => {
      jest.advanceTimersByTime(100);
    });

    // Check that the API call was made
    await waitFor(() => {
      expect(mock.history.patch).toHaveLength(1);
    });

    // Check that success message appears in snackbar
    await waitFor(() => {
      expect(screen.getByText('Profile updated successfully!')).toBeInTheDocument();
    });
  });

  it('should handle API errors gracefully', async () => {
    const redirectPath = '/dashboard';
    sessionStorage.setItem('congen_redirect_after_profile_edit', redirectPath);

    mock.onPatch('/user/me').reply(500, { error: 'Server error' });

    render(
      <MemoryRouter>
        <SnackbarProvider>
          <ProfileEditRedirect />
        </SnackbarProvider>
      </MemoryRouter>
    );

    // Fast-forward initial timer within act
    await act(async () => {
      jest.advanceTimersByTime(100);
    });

    // Component should handle error gracefully
    await waitFor(() => {
      expect(mock.history.patch).toHaveLength(1);
      expect(mock.history.patch[0].url).toBe('/user/me');
    });

    // Check that error message appears in snackbar
    await waitFor(() => {
      expect(
        screen.getByText('Failed to sync profile changes. Please try again.')
      ).toBeInTheDocument();
    });
  });

  it('should handle special characters in redirect path', async () => {
    const redirectPath = '/dashboard?tab=settings&section=profile';
    sessionStorage.setItem('congen_redirect_after_profile_edit', redirectPath);

    mock.onPatch('/user/me').reply(200, {});

    render(
      <MemoryRouter>
        <SnackbarProvider>
          <ProfileEditRedirect />
        </SnackbarProvider>
      </MemoryRouter>
    );

    // Fast-forward initial timer within act
    await act(async () => {
      jest.advanceTimersByTime(100);
    });

    await waitFor(() => {
      expect(mock.history.patch).toHaveLength(1);
      expect(mock.history.patch[0].url).toBe('/user/me');
      expect(mock.history.patch[0].params).toEqual({ name: 'Test User' });
    });

    // Fast-forward success message delay within act
    await act(async () => {
      jest.advanceTimersByTime(1500);
    });

    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith(redirectPath, { replace: true });
    });
  });

  it('should clear timeout on unmount', () => {
    const clearTimeoutSpy = jest.spyOn(global, 'clearTimeout');

    const { unmount } = render(
      <MemoryRouter>
        <ProfileEditRedirect />
      </MemoryRouter>
    );

    unmount();

    expect(clearTimeoutSpy).toHaveBeenCalled();
  });
});
