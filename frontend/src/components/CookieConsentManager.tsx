import * as React from 'react';
import { useCookie } from '../contexts/CookieContext';
import { CookieConsentBanner } from './CookieConsentBanner';

export const CookieConsentManager: React.FC = () => {
  const { hasConsented } = useCookie();

  // Don't show the banner if user has already consented
  if (hasConsented) {
    return null;
  }

  return <CookieConsentBanner />;
};
