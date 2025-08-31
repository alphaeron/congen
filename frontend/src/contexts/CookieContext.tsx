import * as React from 'react';

interface CookieConsent {
  necessary: boolean;
  timestamp: number;
}

interface CookieContextType {
  consent: CookieConsent | null;
  hasConsented: boolean;
  setConsent: (consent: CookieConsent) => void;
  acceptAll: () => void;
  rejectAll: () => void;
}

const CookieContext = React.createContext<CookieContextType | undefined>(undefined);

const COOKIE_CONSENT_KEY = 'cookie-consent';

const getStoredConsent = (): CookieConsent | null => {
  try {
    const stored = localStorage.getItem(COOKIE_CONSENT_KEY);
    return stored ? JSON.parse(stored) : null;
  } catch {
    return null;
  }
};

const storeConsent = (consent: CookieConsent): void => {
  try {
    localStorage.setItem(COOKIE_CONSENT_KEY, JSON.stringify(consent));
  } catch {
    // Silently fail if localStorage is not available
  }
};

export const CookieProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [consent, setConsentState] = React.useState<CookieConsent | null>(getStoredConsent);

  const setConsent = React.useCallback((newConsent: CookieConsent) => {
    setConsentState(newConsent);
    storeConsent(newConsent);
  }, []);

  const acceptAll = React.useCallback(() => {
    const allConsent: CookieConsent = {
      necessary: true,
      timestamp: Date.now(),
    };
    setConsent(allConsent);
  }, [setConsent]);

  const rejectAll = React.useCallback(() => {
    const minimalConsent: CookieConsent = {
      necessary: true, // Necessary cookies cannot be rejected
      timestamp: Date.now(),
    };
    setConsent(minimalConsent);
  }, [setConsent]);

  const hasConsented = React.useMemo(() => {
    return consent !== null;
  }, [consent]);

  const value = React.useMemo(
    () => ({
      consent,
      hasConsented,
      setConsent,
      acceptAll,
      rejectAll,
    }),
    [consent, hasConsented, setConsent, acceptAll, rejectAll]
  );

  return <CookieContext.Provider value={value}>{children}</CookieContext.Provider>;
};

export const useCookie = (): CookieContextType => {
  const context = React.useContext(CookieContext);
  if (!context) {
    throw new Error('useCookie must be used within a CookieProvider');
  }
  return context;
};
