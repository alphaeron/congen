import { render, screen, waitFor, act } from '@testing-library/react';
import { MemoryRouter } from 'react-router';
import { SnackbarProvider } from 'notistack';
import React from 'react';

import { PasswordChangeRedirect } from './PasswordChangeRedirect';

// Mock react-router
const mockNavigate = jest.fn();
jest.mock('react-router', () => ({
  ...jest.requireActual('react-router'),
  useNavigate: () => mockNavigate,
}));

describe('PasswordChangeRedirect', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    
    // Clear sessionStorage
    sessionStorage.clear();
    
    // Mock setTimeout and clearTimeout
    jest.useFakeTimers();
  });

  afterEach(() => {
    jest.useRealTimers();
  });

  it('should show loading state initially', () => {
    render(
      <MemoryRouter>
        <SnackbarProvider>
          <PasswordChangeRedirect />
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
          <PasswordChangeRedirect />
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

  it('should redirect to stored path when redirect path exists', async () => {
    const redirectPath = '/dashboard';
    sessionStorage.setItem('congen_redirect_after_password_change', redirectPath);

    render(
      <MemoryRouter>
        <SnackbarProvider>
          <PasswordChangeRedirect />
        </SnackbarProvider>
      </MemoryRouter>
    );

    // Fast-forward initial timer within act
    await act(async () => {
      jest.advanceTimersByTime(100);
    });

    // Fast-forward success message delay within act
    await act(async () => {
      jest.advanceTimersByTime(1500);
    });

    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith(redirectPath, { replace: true });
    });

    // Verify sessionStorage was cleared
    expect(sessionStorage.getItem('congen_redirect_after_password_change')).toBeNull();
  });

  it('should show loading spinner and success message when redirecting', async () => {
    const redirectPath = '/dashboard';
    sessionStorage.setItem('congen_redirect_after_password_change', redirectPath);

    render(
      <MemoryRouter>
        <SnackbarProvider>
          <PasswordChangeRedirect />
        </SnackbarProvider>
      </MemoryRouter>
    );

    // Fast-forward initial timer within act
    await act(async () => {
      jest.advanceTimersByTime(100);
    });

    await waitFor(() => {
      expect(screen.getByText('Password changed successfully!')).toBeInTheDocument();
    });
  });

  it('should handle errors gracefully', async () => {
    const redirectPath = '/dashboard';
    sessionStorage.setItem('congen_redirect_after_password_change', redirectPath);

    render(
      <MemoryRouter>
        <SnackbarProvider>
          <PasswordChangeRedirect />
        </SnackbarProvider>
      </MemoryRouter>
    );

    // Fast-forward initial timer within act
    await act(async () => {
      jest.advanceTimersByTime(100);
    });

    // Fast-forward success message delay within act
    await act(async () => {
      jest.advanceTimersByTime(1500);
    });

    // Component should navigate successfully
    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith(redirectPath, { replace: true });
    });

    // Verify sessionStorage was cleared
    expect(sessionStorage.getItem('congen_redirect_after_password_change')).toBeNull();
  });

  it('should handle special characters in redirect path', async () => {
    const redirectPath = '/dashboard?tab=settings&section=profile';
    sessionStorage.setItem('congen_redirect_after_password_change', redirectPath);

    render(
      <MemoryRouter>
        <SnackbarProvider>
          <PasswordChangeRedirect />
        </SnackbarProvider>
      </MemoryRouter>
    );

    // Fast-forward initial timer within act
    await act(async () => {
      jest.advanceTimersByTime(100);
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
        <PasswordChangeRedirect />
      </MemoryRouter>
    );

    unmount();

    expect(clearTimeoutSpy).toHaveBeenCalled();
  });
});
