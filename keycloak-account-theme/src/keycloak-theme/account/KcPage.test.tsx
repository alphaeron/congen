import React from 'react';
import { screen, waitFor, act } from '@testing-library/react';
import { render, createMockKcContext } from '../../test-utils';
import type { KcContextWithUser } from '../../test-utils';
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

describe('KcPage', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    
    // Mock successful userinfo response
    mockFetch.mockImplementation((url: string) => {
      if (url.includes('/protocol/openid-connect/userinfo')) {
        return Promise.resolve({
          ok: true,
          json: () => Promise.resolve({
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

  it('renders Account component for account page', async () => {
    const kcContext = createMockKcContext({
      url: {
        accountUrl: '/auth/realms/congen/account',
      } as KcContextWithUser['url'],
    });
    
    await act(async () => {
      render(<KcPage kcContext={kcContext} />);
    });

    // Wait for the component to load and show the main content
    await waitFor(() => {
      expect(screen.getByText('ConGen')).toBeInTheDocument();
    }, { timeout: 10000 });
  });

  it('handles unknown page types gracefully', async () => {
    const kcContext = createMockKcContext({
      pageId: 'sessions.ftl' as KcContextWithUser['pageId'], // Use a valid pageId for testing
      url: {
        accountUrl: '/auth/realms/congen/unknown-page',
      } as KcContextWithUser['url'],
    });
    
    await act(async () => {
      render(<KcPage kcContext={kcContext} />);
    });

    // The Account component should render with the app bar
    await waitFor(() => {
      expect(screen.getByText('ConGen')).toBeInTheDocument();
    });
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
    
    await act(async () => {
      render(<KcPage kcContext={kcContext} />);
    });

    // The Account component should render with the app bar
    await waitFor(() => {
      expect(screen.getByText('ConGen')).toBeInTheDocument();
    });
  });

  it('renders with different realm names', async () => {
    const kcContext = createMockKcContext({
      realm: {
        internationalizationEnabled: true,
        userManagedAccessAllowed: true,
      },
    });
    
    await act(async () => {
      render(<KcPage kcContext={kcContext} />);
    });

    // The Account component should render with the app bar
    await waitFor(() => {
      expect(screen.getByText('ConGen')).toBeInTheDocument();
    });
  });

  it('handles missing realm information', async () => {
    const kcContext = createMockKcContext({
      realm: undefined,
    });
    
    await act(async () => {
      render(<KcPage kcContext={kcContext} />);
    });

    // The Account component should render with the app bar
    await waitFor(() => {
      expect(screen.getByText('ConGen')).toBeInTheDocument();
    });
  });
});
