import { render, screen } from '@testing-library/react';
import * as React from 'react';
import { MemoryRouter } from 'react-router';

import { CookieConsentManager } from './CookieConsentManager';
import { CookieProvider } from '../contexts/CookieContext';

const renderWithProvider = (component: React.ReactElement) => {
  return render(
    <MemoryRouter>
      <CookieProvider>{component}</CookieProvider>
    </MemoryRouter>
  );
};

describe('CookieConsentManager', () => {
  beforeEach(() => {
    localStorage.clear();
  });

  afterEach(() => {
    localStorage.clear();
  });

  it('should show the banner when user has not consented', () => {
    renderWithProvider(<CookieConsentManager />);

    expect(screen.getByText('Cookie Notice')).toBeInTheDocument();
  });

  it('should not show the banner when user has already consented', () => {
    // Set existing consent
    const existingConsent = {
      necessary: true,
      functional: true,
      analytics: false,
      marketing: false,
      timestamp: Date.now(),
    };
    localStorage.setItem('cookie-consent', JSON.stringify(existingConsent));

    renderWithProvider(<CookieConsentManager />);

    expect(screen.queryByText('Cookie Preferences')).not.toBeInTheDocument();
  });

  it('should not show the banner when localStorage has any consent data', () => {
    // Set minimal consent
    const minimalConsent = {
      necessary: true,
      functional: false,
      analytics: false,
      marketing: false,
      timestamp: Date.now(),
    };
    localStorage.setItem('cookie-consent', JSON.stringify(minimalConsent));

    renderWithProvider(<CookieConsentManager />);

    expect(screen.queryByText('Cookie Preferences')).not.toBeInTheDocument();
  });

  it('should handle invalid localStorage data gracefully', () => {
    // Set invalid consent data
    localStorage.setItem('cookie-consent', 'invalid-json');

    renderWithProvider(<CookieConsentManager />);

    // Should show banner when localStorage data is invalid
    expect(screen.getByText('Cookie Notice')).toBeInTheDocument();
  });
});
