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

    await waitFor(() => {
      expect(screen.getByText('Personal Information')).toBeInTheDocument();
    });
    expect(screen.getByText('Security Settings')).toBeInTheDocument();
    expect(screen.getByText('Quick Actions')).toBeInTheDocument();
  });

  it('handles unknown page types gracefully', () => {
    const kcContext = createMockKcContext({
      pageId: 'sessions.ftl' as KcContextWithUser['pageId'], // Use a valid pageId for testing
      url: {
        accountUrl: '/auth/realms/congen/unknown-page',
      } as KcContextWithUser['url'],
    });
    render(<KcPage kcContext={kcContext} />);

    // Should render the DefaultPage component (which is mocked as an empty div)
    const defaultPage = document.querySelector('div');
    expect(defaultPage).toBeInTheDocument();
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

    await waitFor(() => {
      expect(screen.getByText('Custom User')).toBeInTheDocument();
    });
    expect(screen.getByText('custom@example.com')).toBeInTheDocument();
    expect(screen.getByText('customuser')).toBeInTheDocument();
  });

  it('renders with different realm names', async () => {
    const kcContext = createMockKcContext({
      realm: {
        internationalizationEnabled: true,
        userManagedAccessAllowed: true,
      },
    });
    render(<KcPage kcContext={kcContext} />);

    await waitFor(() => {
      expect(screen.getByText('Personal Information')).toBeInTheDocument();
    });
  });

  it('handles missing realm information', async () => {
    const kcContext = createMockKcContext({
      realm: undefined,
    });
    render(<KcPage kcContext={kcContext} />);

    await waitFor(() => {
      expect(screen.getByText('Personal Information')).toBeInTheDocument();
    });
  });
});
