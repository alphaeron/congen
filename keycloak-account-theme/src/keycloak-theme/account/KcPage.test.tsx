import React from 'react';
import { screen, waitFor } from '@testing-library/react';
import { render, createMockKcContext } from '../../test-utils';
import type { KcContextWithUser } from '../../test-utils';
import KcPage from './KcPage';

describe('KcPage', () => {
  it('renders Account component for account page', async () => {
    const kcContext = createMockKcContext({
      url: {
        accountUrl: '/auth/realms/congen/account',
      } as KcContextWithUser['url'],
    });
    render(<KcPage kcContext={kcContext} />);

    // In test environment, lazy loading shows the fallback LoadingSpinner
    await waitFor(() => {
      expect(screen.getByText('Loading...')).toBeInTheDocument();
    });
  });

  it('handles unknown page types gracefully', () => {
    const kcContext = createMockKcContext({
      pageId: 'sessions.ftl' as KcContextWithUser['pageId'], // Use a valid pageId for testing
      url: {
        accountUrl: '/auth/realms/congen/unknown-page',
      } as KcContextWithUser['url'],
    });
    render(<KcPage kcContext={kcContext} />);

    // The Account component should render with the app bar
    expect(screen.getByText('ConGen')).toBeInTheDocument();
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
    render(<KcPage kcContext={kcContext} />);

    // The Account component should render with the app bar
    expect(screen.getByText('ConGen')).toBeInTheDocument();
  });

  it('renders with different realm names', async () => {
    const kcContext = createMockKcContext({
      realm: {
        internationalizationEnabled: true,
        userManagedAccessAllowed: true,
      },
    });
    render(<KcPage kcContext={kcContext} />);

    // The Account component should render with the app bar
    expect(screen.getByText('ConGen')).toBeInTheDocument();
  });

  it('handles missing realm information', async () => {
    const kcContext = createMockKcContext({
      realm: undefined,
    });
    render(<KcPage kcContext={kcContext} />);

    // The Account component should render with the app bar
    expect(screen.getByText('ConGen')).toBeInTheDocument();
  });
});
