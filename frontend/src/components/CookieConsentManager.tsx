import * as React from 'react';

import { CookieConsentBanner } from './CookieConsentBanner';
import { useCookie } from '../contexts/CookieContext';

export const CookieConsentManager: React.FC = () => {
  const { hasConsented } = useCookie();

  // Don't show the banner if user has already consented
  if (hasConsented) {
    return null;
  }

  return <CookieConsentBanner />;
};
