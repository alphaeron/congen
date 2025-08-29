import { useCookie } from '../contexts/CookieContext';

/**
 * Utility functions for managing cookies based on user consent.
 */

export interface CookieConsent {
  necessary: boolean;
  timestamp: number;
}

/**
 * Check if necessary cookies are allowed based on user consent.
 * 
 * @param consent - The user's cookie consent object
 * @returns True if necessary cookies are allowed, false otherwise
 */
export const isCookieAllowed = (consent: CookieConsent | null): boolean => {
  if (!consent) {
    return false;
  }
  
  return consent.necessary;
};

/**
 * Hook to check if necessary cookies are currently allowed.
 * 
 * @returns True if necessary cookies are allowed, false otherwise
 */
export const useCookieAllowed = (): boolean => {
  const { consent } = useCookie();
  return isCookieAllowed(consent);
};

/**
 * Set a cookie with proper consent checking.
 * 
 * @param name - Cookie name
 * @param value - Cookie value
 * @param options - Additional cookie options
 * @returns True if cookie was set, false if consent was denied
 */
export const setCookieWithConsent = (
  name: string,
  value: string,
  options: {
    days?: number;
    path?: string;
    domain?: string;
    secure?: boolean;
    sameSite?: 'Strict' | 'Lax' | 'None';
  } = {}
): boolean => {
  // Get current consent from localStorage
  let consent: CookieConsent | null = null;
  try {
    const stored = localStorage.getItem('cookie-consent');
    consent = stored ? JSON.parse(stored) : null;
  } catch {
    return false;
  }

  // Check if consent is given for necessary cookies
  if (!isCookieAllowed(consent)) {
    return false;
  }

  // Set the cookie
  const { days = 365, path = '/', domain, secure, sameSite = 'Lax' } = options;
  
  let cookieString = `${name}=${encodeURIComponent(value)}`;
  
  if (days > 0) {
    const date = new Date();
    date.setTime(date.getTime() + (days * 24 * 60 * 60 * 1000));
    cookieString += `; expires=${date.toUTCString()}`;
  }
  
  cookieString += `; path=${path}`;
  
  if (domain) {
    cookieString += `; domain=${domain}`;
  }
  
  if (secure) {
    cookieString += '; secure';
  }
  
  cookieString += `; samesite=${sameSite}`;
  
  document.cookie = cookieString;
  return true;
};

/**
 * Get a cookie value.
 * 
 * @param name - Cookie name
 * @returns Cookie value or null if not found
 */
export const getCookie = (name: string): string | null => {
  const nameEQ = name + '=';
  const ca = document.cookie.split(';');
  
  for (let i = 0; i < ca.length; i++) {
    let c = ca[i];
    while (c.charAt(0) === ' ') {
      c = c.substring(1, c.length);
    }
    if (c.indexOf(nameEQ) === 0) {
      return decodeURIComponent(c.substring(nameEQ.length, c.length));
    }
  }
  
  return null;
};

/**
 * Delete a cookie.
 * 
 * @param name - Cookie name
 * @param path - Cookie path (must match the path used when setting)
 * @param domain - Cookie domain (must match the domain used when setting)
 */
export const deleteCookie = (name: string, path = '/', domain?: string): void => {
  let cookieString = `${name}=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=${path}`;
  
  if (domain) {
    cookieString += `; domain=${domain}`;
  }
  
  document.cookie = cookieString;
};

/**
 * Clear all cookies except necessary ones (authentication cookies).
 * This should be called when user revokes consent.
 */
export const clearOptionalCookies = (): void => {
  // Get all cookies
  const cookies = document.cookie.split(';');
  
  cookies.forEach(cookie => {
    const name = cookie.split('=')[0].trim();
    
    // Skip authentication and security cookies
    if (name.startsWith('kc_') || name.startsWith('auth_') || name === 'JSESSIONID') {
      return;
    }
    
    // Delete the cookie
    deleteCookie(name);
  });
};
