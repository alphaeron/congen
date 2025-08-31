import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import * as React from 'react';
import { MemoryRouter } from 'react-router';

import { CookieConsentBanner } from './CookieConsentBanner';
import { CookieProvider } from '../contexts/CookieContext';

const renderWithProvider = (component: React.ReactElement) => {
  return render(
    <MemoryRouter>
      <CookieProvider>{component}</CookieProvider>
    </MemoryRouter>
  );
};

describe('CookieConsentBanner', () => {
  beforeEach(() => {
    localStorage.clear();
  });

  afterEach(() => {
    localStorage.clear();
  });

  it('should render the banner with correct title and description', () => {
    renderWithProvider(<CookieConsentBanner />);

    expect(screen.getByText('Cookie Notice')).toBeInTheDocument();
    expect(
      screen.getByText(/We use essential cookies to ensure our website functions properly/)
    ).toBeInTheDocument();
  });

  it('should show accept button', () => {
    renderWithProvider(<CookieConsentBanner />);

    expect(screen.getByText('Accept')).toBeInTheDocument();
  });

  it('should show authentication cookies information', () => {
    renderWithProvider(<CookieConsentBanner />);

    expect(screen.getByText('Authentication Cookies')).toBeInTheDocument();
    expect(screen.getByText('Essential')).toBeInTheDocument();
    // Should show expand button
    expect(screen.getByTestId('ExpandMoreIcon')).toBeInTheDocument();
  });

  it('should have expand/collapse functionality', () => {
    renderWithProvider(<CookieConsentBanner />);

    // Should have expand button
    const expandButton = screen.getByTestId('ExpandMoreIcon').closest('button');
    expect(expandButton).toBeInTheDocument();

    // Click expand button (should not throw)
    expect(() => {
      fireEvent.click(expandButton!);
    }).not.toThrow();
  });

  it('should show cookie notice title', () => {
    renderWithProvider(<CookieConsentBanner />);

    expect(screen.getByText('Cookie Notice')).toBeInTheDocument();
  });

  it('should call acceptAll when Accept is clicked', async () => {
    const mockOnClose = jest.fn();
    renderWithProvider(<CookieConsentBanner onClose={mockOnClose} />);

    fireEvent.click(screen.getByText('Accept'));

    await waitFor(() => {
      expect(mockOnClose).toHaveBeenCalled();
    });

    // Check that consent was stored
    const stored = localStorage.getItem('cookie-consent');
    expect(stored).toBeTruthy();

    const consent = JSON.parse(stored || '{}');
    expect(consent.necessary).toBe(true);
  });

  it('should include privacy policy link', () => {
    renderWithProvider(<CookieConsentBanner />);

    const privacyLink = screen.getByText('Privacy Policy');
    expect(privacyLink).toBeInTheDocument();
    expect(privacyLink).toHaveAttribute('href', '/privacy_policy');
  });

  it('should have proper styling classes and structure', () => {
    renderWithProvider(<CookieConsentBanner />);

    // Check that the banner has the expected structure
    const banner = screen.getByText('Cookie Notice').closest('h2')?.parentElement?.parentElement;
    expect(banner).toBeInTheDocument();

    // Check for info icon
    expect(screen.getByTestId('InfoIcon')).toBeInTheDocument();
  });

  it('should not call onClose if not provided', async () => {
    renderWithProvider(<CookieConsentBanner />);

    // Should not throw when onClose is not provided
    expect(() => {
      fireEvent.click(screen.getByText('Accept'));
    }).not.toThrow();
  });
});
