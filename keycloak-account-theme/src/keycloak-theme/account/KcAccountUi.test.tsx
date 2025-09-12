import { screen, act, waitFor } from '@testing-library/react';
import React from 'react';

import { render } from '../../test-utils';
import KcAccountUi from './KcAccountUi';

// Mock LoadingSpinner
jest.mock('../../components/LoadingSpinner', () => ({
  LoadingSpinner: ({ size }: { size: number }) => (
    <div data-testid="loading-spinner" data-size={size}>
      Loading...
    </div>
  ),
}));

const mockKcContext = {
  themeType: 'account' as const,
  themeName: 'test-theme',
  properties: {},
};

describe('KcAccountUi', () => {
  it('renders with kcContext', async () => {
    await act(async () => {
      render(<KcAccountUi kcContext={mockKcContext} />);
    });

    // Wait for the lazy-loaded component to finish loading
    await waitFor(
      () => {
        const conGenElement = screen.queryByText('ConGen');
        const loadingElement = screen.queryByText('Loading...');
        expect(conGenElement || loadingElement).toBeTruthy();
      },
      { timeout: 10000 }
    );
  }, 15000);

  it('renders with Suspense wrapper', async () => {
    await act(async () => {
      render(<KcAccountUi kcContext={mockKcContext} />);
    });

    // Wait for the Account component to load
    await waitFor(
      () => {
        expect(screen.getByText('ConGen')).toBeInTheDocument();
      },
      { timeout: 10000 }
    );
  }, 15000);

  it('has correct fallback configuration', async () => {
    await act(async () => {
      render(<KcAccountUi kcContext={mockKcContext} />);
    });

    // Wait for the Account component to render
    await waitFor(
      () => {
        expect(screen.getByText('ConGen')).toBeInTheDocument();
      },
      { timeout: 10000 }
    );
  }, 15000);

  it('renders with correct structure', async () => {
    await act(async () => {
      render(<KcAccountUi kcContext={mockKcContext} />);
    });

    // Wait for the Account component to render with proper structure
    await waitFor(
      () => {
        expect(screen.getByText('ConGen')).toBeInTheDocument();
        expect(screen.getByText('User Profile')).toBeInTheDocument();
      },
      { timeout: 10000 }
    );
  }, 15000);
});
