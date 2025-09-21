import { render, screen, fireEvent } from '@testing-library/react';
import React from 'react';

import { UserProfileDrawer } from './UserProfileDrawer';
import { navigateToFrontend } from './utils';

// Mock the utils module
jest.mock('./utils', () => ({
  navigateToFrontend: jest.fn(),
}));

const mockNavigateToFrontend = navigateToFrontend as jest.MockedFunction<typeof navigateToFrontend>;

const mockKcContext = {
  themeType: 'account' as const,
  themeName: 'test-theme',
  properties: {},
};

describe('UserProfileDrawer', () => {
  const defaultProps = {
    kcContext: mockKcContext,
    currentSection: 'privacy',
    onSectionChange: jest.fn(),
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders drawer with menu items', () => {
    render(<UserProfileDrawer {...defaultProps} />);

    expect(screen.getByText('Privacy & Data')).toBeInTheDocument();
    expect(screen.getByText('Manage Profile')).toBeInTheDocument();
  });

  it('renders with correct icons', () => {
    render(<UserProfileDrawer {...defaultProps} />);

    // Check that icons are rendered (they should be present as SVG elements)
    const privacyIcon =
      screen.getByTestId('PrivacyTipIcon') ||
      screen.getByText('Privacy & Data').closest('[data-testid*="Icon"]');
    const personIcon =
      screen.getByTestId('PersonIcon') ||
      screen.getByText('Manage Profile').closest('[data-testid*="Icon"]');

    expect(privacyIcon || screen.getByText('Privacy & Data')).toBeInTheDocument();
    expect(personIcon || screen.getByText('Manage Profile')).toBeInTheDocument();
  });

  it('handles external navigation for privacy section', () => {
    render(<UserProfileDrawer {...defaultProps} />);

    const privacyItem = screen.getByText('Privacy & Data');
    fireEvent.click(privacyItem);

    expect(mockNavigateToFrontend).toHaveBeenCalledWith('/profile?section=privacy');
  });

  it('handles internal navigation for personal info section', () => {
    render(<UserProfileDrawer {...defaultProps} />);

    const personalInfoItem = screen.getByText('Manage Profile');
    fireEvent.click(personalInfoItem);

    expect(defaultProps.onSectionChange).toHaveBeenCalledWith('personal-info');
  });

  it('highlights current section', () => {
    render(<UserProfileDrawer {...defaultProps} currentSection="privacy" />);

    const privacyItem = screen.getByText('Privacy & Data');
    const personalInfoItem = screen.getByText('Manage Profile');

    // The current section should have different styling
    expect(privacyItem.closest('[role="button"]')).toHaveClass('Mui-selected');
    expect(personalInfoItem.closest('[role="button"]')).not.toHaveClass('Mui-selected');
  });

  it('highlights personal-info section when it is current', () => {
    render(<UserProfileDrawer {...defaultProps} currentSection="personal-info" />);

    const privacyItem = screen.getByText('Privacy & Data');
    const personalInfoItem = screen.getByText('Manage Profile');

    expect(privacyItem.closest('[role="button"]')).not.toHaveClass('Mui-selected');
    expect(personalInfoItem.closest('[role="button"]')).toHaveClass('Mui-selected');
  });

  it('renders with different current sections', () => {
    const { rerender } = render(<UserProfileDrawer {...defaultProps} currentSection="privacy" />);

    expect(screen.getByText('Privacy & Data').closest('[role="button"]')).toHaveClass(
      'Mui-selected'
    );

    rerender(<UserProfileDrawer {...defaultProps} currentSection="personal-info" />);

    expect(screen.getByText('Manage Profile').closest('[role="button"]')).toHaveClass(
      'Mui-selected'
    );
  });

  it('calls onSectionChange with correct section id', () => {
    render(<UserProfileDrawer {...defaultProps} />);

    const personalInfoItem = screen.getByText('Manage Profile');
    fireEvent.click(personalInfoItem);

    expect(defaultProps.onSectionChange).toHaveBeenCalledWith('personal-info');
  });

  it('renders all menu items with correct structure', () => {
    render(<UserProfileDrawer {...defaultProps} />);

    const listItems = screen.getAllByRole('listitem');
    expect(listItems).toHaveLength(3);

    // Check that each item has the correct structure
    expect(screen.getByText('Physical Attributes')).toBeInTheDocument();
    expect(screen.getByText('Privacy & Data')).toBeInTheDocument();
    expect(screen.getByText('Manage Profile')).toBeInTheDocument();
  });
});
