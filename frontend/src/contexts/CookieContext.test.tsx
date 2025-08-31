import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import * as React from 'react';
import { MemoryRouter } from 'react-router';

import { CookieProvider, useCookie } from './CookieContext';

// Test component to access the context
const TestComponent: React.FC = () => {
  const { consent, hasConsented, setConsent, acceptAll, rejectAll } = useCookie();

  return (
    <div>
      <div data-testid="has-consented">{hasConsented.toString()}</div>
      <div data-testid="consent">{JSON.stringify(consent)}</div>
      <button data-testid="accept-all" onClick={acceptAll}>
        Accept All
      </button>
      <button data-testid="reject-all" onClick={rejectAll}>
        Reject All
      </button>
      <button
        data-testid="set-custom"
        onClick={() =>
          setConsent({
            necessary: true,
            timestamp: 1234567890,
          })
        }
      >
        Set Custom
      </button>
    </div>
  );
};

const renderWithProvider = (component: React.ReactElement) => {
  return render(
    <MemoryRouter>
      <CookieProvider>{component}</CookieProvider>
    </MemoryRouter>
  );
};

describe('CookieContext', () => {
  beforeEach(() => {
    // Clear localStorage before each test
    localStorage.clear();
  });

  afterEach(() => {
    // Clear localStorage after each test
    localStorage.clear();
  });

  it('should initialize with no consent when localStorage is empty', () => {
    renderWithProvider(<TestComponent />);

    expect(screen.getByTestId('has-consented')).toHaveTextContent('false');
    expect(screen.getByTestId('consent')).toHaveTextContent('null');
  });

  it('should load existing consent from localStorage', () => {
    const existingConsent = {
      necessary: true,
      timestamp: 1234567890,
    };
    localStorage.setItem('cookie-consent', JSON.stringify(existingConsent));

    renderWithProvider(<TestComponent />);

    expect(screen.getByTestId('has-consented')).toHaveTextContent('true');
    expect(screen.getByTestId('consent')).toHaveTextContent(JSON.stringify(existingConsent));
  });

  it('should accept necessary cookies when acceptAll is called', async () => {
    renderWithProvider(<TestComponent />);

    fireEvent.click(screen.getByTestId('accept-all'));

    await waitFor(() => {
      expect(screen.getByTestId('has-consented')).toHaveTextContent('true');
    });

    const consent = JSON.parse(screen.getByTestId('consent').textContent || '{}');
    expect(consent.necessary).toBe(true);
    expect(consent.timestamp).toBeDefined();
  });

  it('should accept necessary cookies when rejectAll is called', async () => {
    renderWithProvider(<TestComponent />);

    fireEvent.click(screen.getByTestId('reject-all'));

    await waitFor(() => {
      expect(screen.getByTestId('has-consented')).toHaveTextContent('true');
    });

    const consent = JSON.parse(screen.getByTestId('consent').textContent || '{}');
    expect(consent.necessary).toBe(true);
    expect(consent.timestamp).toBeDefined();
  });

  it('should set custom consent when setConsent is called', async () => {
    renderWithProvider(<TestComponent />);

    fireEvent.click(screen.getByTestId('set-custom'));

    await waitFor(() => {
      expect(screen.getByTestId('has-consented')).toHaveTextContent('true');
    });

    const consent = JSON.parse(screen.getByTestId('consent').textContent || '{}');
    expect(consent.necessary).toBe(true);
    expect(consent.timestamp).toBe(1234567890);
  });

  it('should persist consent to localStorage', async () => {
    renderWithProvider(<TestComponent />);

    fireEvent.click(screen.getByTestId('accept-all'));

    await waitFor(() => {
      const stored = localStorage.getItem('cookie-consent');
      expect(stored).toBeTruthy();

      const parsed = JSON.parse(stored || '{}');
      expect(parsed.necessary).toBe(true);
    });
  });

  it('should handle localStorage errors gracefully', () => {
    // Mock localStorage to throw an error
    const originalGetItem = localStorage.getItem;
    const originalSetItem = localStorage.setItem;

    localStorage.getItem = jest.fn().mockImplementation(() => {
      throw new Error('localStorage error');
    });
    localStorage.setItem = jest.fn().mockImplementation(() => {
      throw new Error('localStorage error');
    });

    // Should not throw an error
    expect(() => {
      renderWithProvider(<TestComponent />);
    }).not.toThrow();

    // Should initialize with no consent
    expect(screen.getByTestId('has-consented')).toHaveTextContent('false');
    expect(screen.getByTestId('consent')).toHaveTextContent('null');

    // Restore original methods
    localStorage.getItem = originalGetItem;
    localStorage.setItem = originalSetItem;
  });
});
