/**
 * Shared utilities for Keycloak account theme components
 */

import type { KcContext } from './KcContext';

/**
 * Get the frontend URL for the current environment
 */
export const getFrontendUrl = (): string => {
  return process.env.REACT_APP_FRONTEND_URL || 'http://localhost:3000';
};

/**
 * Generate logout URL for Keycloak
 */
export const getLogoutUrl = (kcContext: KcContext): string => {
  return `${kcContext.authUrl}/realms/${kcContext.realm?.name}/protocol/openid-connect/logout`;
};

/**
 * Handle logout by redirecting to Keycloak logout URL
 */
export const handleLogout = (kcContext: KcContext): void => {
  window.location.href = getLogoutUrl(kcContext);
};

/**
 * Navigate to frontend URL
 */
export const navigateToFrontend = (path: string = ''): void => {
  const frontendUrl = getFrontendUrl();
  window.location.href = `${frontendUrl}${path}`;
};
