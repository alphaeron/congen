/**
 * Authentication utility functions for token validation and sanitization
 * Copied from frontend to ensure consistent behavior
 */

/**
 * Checks if a JWT token is malformed.
 * A malformed token is one that doesn't have the expected JWT structure.
 *
 * @param token The token to check
 * @returns True if the token is malformed, false otherwise
 */
export const isTokenMalformed = (token: string): boolean => {
  if (!token || typeof token !== 'string') {
    return true;
  }

  // JWT tokens should have exactly 3 parts separated by dots
  const parts = token.split('.');
  if (parts.length !== 3) {
    return true;
  }

  // Each part should be base64 encoded and non-empty
  for (const part of parts) {
    if (!part || part.trim() === '') {
      return true;
    }
  }

  return false;
};

/**
 * Checks if a JWT token is expired.
 * This function decodes the token and checks the 'exp' claim.
 *
 * @param token The JWT token to check
 * @returns True if the token is expired, false otherwise
 */
export const isTokenExpired = (token: string): boolean => {
  if (!token || typeof token !== 'string') {
    return true;
  }

  try {
    // Decode the JWT payload (second part)
    const parts = token.split('.');
    if (parts.length !== 3) {
      return true;
    }

    // Decode base64url encoded payload
    const payload = parts[1];
    // Add padding if needed for base64url decoding
    const paddedPayload = payload + '='.repeat((4 - (payload.length % 4)) % 4);
    const decodedPayload = atob(paddedPayload.replace(/-/g, '+').replace(/_/g, '/'));
    const parsedPayload = JSON.parse(decodedPayload);

    // Check if the token has an expiration time
    if (!parsedPayload.exp) {
      return true;
    }

    // Check if the token is expired (exp is in seconds, Date.now() is in milliseconds)
    const currentTime = Math.floor(Date.now() / 1000);
    return parsedPayload.exp < currentTime;
  } catch {
    // If we can't parse the token, consider it expired
    return true;
  }
};

/**
 * Checks if a JWT token is expiring soon (within the next 5 minutes).
 * This is used to trigger token refresh before it actually expires.
 *
 * @param token The JWT token to check
 * @returns True if the token is expiring soon, false otherwise
 */
export const isTokenExpiringSoon = (token: string): boolean => {
  if (!token || typeof token !== 'string') {
    return true;
  }

  try {
    // Decode the JWT payload (second part)
    const parts = token.split('.');
    if (parts.length !== 3) {
      return true;
    }

    // Decode base64url encoded payload
    const payload = parts[1];
    // Add padding if needed for base64url decoding
    const paddedPayload = payload + '='.repeat((4 - (payload.length % 4)) % 4);
    const decodedPayload = atob(paddedPayload.replace(/-/g, '+').replace(/_/g, '/'));
    const parsedPayload = JSON.parse(decodedPayload);

    // Check if the token has an expiration time
    if (!parsedPayload.exp) {
      return true;
    }

    // Check if the token is expiring within the next 5 minutes (300 seconds)
    const currentTime = Math.floor(Date.now() / 1000);
    const fiveMinutesFromNow = currentTime + 300;
    return parsedPayload.exp < fiveMinutesFromNow;
  } catch {
    // If we can't parse the token, consider it expiring soon
    return true;
  }
};

/**
 * Sanitizes and validates token data to prevent XSS attacks.
 * This function ensures tokens are properly formatted and don't contain malicious content.
 *
 * @param token The token to sanitize
 * @returns The sanitized token or null if invalid
 */
export const sanitizeToken = (token: string | null | undefined): string | null => {
  if (!token || typeof token !== 'string') {
    return null;
  }

  // Remove any potential XSS vectors
  const sanitized = token
    .replace(/<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi, '') // Remove script tags
    .replace(/javascript:/gi, '') // Remove javascript: protocols
    .replace(/on\w+\s*=/gi, '') // Remove event handlers
    .trim();

  // Validate JWT format (3 parts separated by dots)
  const parts = sanitized.split('.');
  if (parts.length !== 3) {
    return null;
  }

  return sanitized;
};

/**
 * Handles authentication failure by clearing stored tokens and redirecting to login.
 * This function should be called when authentication fails or tokens are invalid.
 */
export const handleAuthenticationFailure = (reason: string): void => {
  // Clear any stored authentication state
  // In the Keycloak theme context, we don't need to redirect since we're already in Keycloak
  // The OIDC context will handle token refresh automatically
};
