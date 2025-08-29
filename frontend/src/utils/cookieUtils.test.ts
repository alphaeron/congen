import { isCookieAllowed } from './cookieUtils';
import type { CookieConsent } from './cookieUtils';

describe('cookieUtils', () => {
  describe('isCookieAllowed', () => {
    it('should return false when consent is null', () => {
      expect(isCookieAllowed(null)).toBe(false);
    });

    it('should return true for necessary cookies when consent exists', () => {
      const consent: CookieConsent = {
        necessary: true,
        timestamp: Date.now(),
      };
      
      expect(isCookieAllowed(consent)).toBe(true);
    });

    it('should return false when necessary cookies are not allowed', () => {
      const consent: CookieConsent = {
        necessary: false,
        timestamp: Date.now(),
      };
      
      expect(isCookieAllowed(consent)).toBe(false);
    });
  });
});
