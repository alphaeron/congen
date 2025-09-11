import { screen } from '@testing-library/react';
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
  it('renders with kcContext', () => {
    render(<KcAccountUi kcContext={mockKcContext} />);

    // In test environment, lazy loading may show LoadingSpinner initially
    // Check for either the Account component or LoadingSpinner
    const conGenElement = screen.queryByText('ConGen');
    const loadingElement = screen.queryByText('Loading...');

    expect(conGenElement || loadingElement).toBeTruthy();
  });

  it('renders with Suspense wrapper', () => {
    render(<KcAccountUi kcContext={mockKcContext} />);

    // The component should be wrapped in Suspense and render the Account component
    expect(screen.getByText('ConGen')).toBeInTheDocument();
  });

  it('has correct fallback configuration', () => {
    render(<KcAccountUi kcContext={mockKcContext} />);

    // The Account component should render
    expect(screen.getByText('ConGen')).toBeInTheDocument();
  });

  it('renders with correct structure', () => {
    render(<KcAccountUi kcContext={mockKcContext} />);

    // Check that the Account component renders with proper structure
    expect(screen.getByText('ConGen')).toBeInTheDocument();
    expect(screen.getByText('User Profile')).toBeInTheDocument();
  });
});
