// Type for OIDC user object
interface OidcUser {
  profile?: {
    sub?: string;
    preferred_username?: string;
    email?: string;
    name?: string;
    roles?: string[];
    realm_access?: {
      roles?: string[];
    };
  };
}

/**
 * Decode JWT token and extract user information
 */
export const decodeToken = (token: string): unknown => {
  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map(c => {
          return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
        })
        .join('')
    );
    return JSON.parse(jsonPayload);
  } catch {
    return null;
  }
};

/**
 * Checks if the user has any of the specified permissions (roles).
 *
 * @param user The OIDC user object
 * @param permissions Array of permission strings to check
 * @return True if user has any of the specified permissions
 */
export const hasAnyPermission = (user: OidcUser | null, permissions: string[]): boolean => {
  if (!user || !permissions.length) {
    return false;
  }

  // Get user's roles
  const userRoles = user.profile?.realm_access?.roles || user.profile?.roles || [];

  return permissions.some(permission => userRoles.includes(permission));
};

/**
 * Clears all authentication-related data from storage.
 * This includes OIDC user data, redirect paths, and any other auth-related items.
 */
export const clearAuthenticationState = (): void => {
  // Clear OIDC user data from storage
  localStorage.removeItem('oidc.user:congen:congen-frontend');
  sessionStorage.removeItem('oidc.user:congen:congen-frontend');
  
  // Clear any other auth-related storage
  sessionStorage.removeItem('congen_redirect_after_password_change');
  sessionStorage.removeItem('congen_redirect_after_profile_edit');
  
  // Clear any other potential auth-related items
  const keysToRemove: string[] = [];
  
  // Find and remove any other OIDC-related keys
  for (let i = 0; i < localStorage.length; i++) {
    const key = localStorage.key(i);
    if (key && key.startsWith('oidc.')) {
      keysToRemove.push(key);
    }
  }
  
  // Remove found keys
  keysToRemove.forEach(key => localStorage.removeItem(key));
};

/**
 * Checks if a JWT token is malformed (invalid format).
 * 
 * @param token The JWT token to check
 * @returns true if the token is malformed, false otherwise
 */
export const isTokenMalformed = (token: string): boolean => {
  try {
    // Check if token has the correct format (header.payload.signature)
    const parts = token.split('.');
    if (parts.length !== 3) {
      return true;
    }
    
    // Try to decode the payload
    JSON.parse(atob(parts[1]));
    return false;
  } catch {
    // If token decoding fails, consider it malformed
    return true;
  }
};

/**
 * Checks if a JWT token is expired.
 * 
 * @param token The JWT token to check
 * @returns true if the token is expired, false otherwise
 */
export const isTokenExpired = (token: string): boolean => {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    const exp = payload.exp * 1000; // Convert to milliseconds
    const now = Date.now();
    
    return exp <= now;
  } catch {
    // If token decoding fails, consider it expired
    return true;
  }
};

/**
 * Checks if a JWT token will expire within a specified time window.
 * 
 * @param token The JWT token to check
 * @param windowMs The time window in milliseconds (default: 5 minutes)
 * @returns true if the token expires within the window, false otherwise
 */
export const isTokenExpiringSoon = (token: string, windowMs: number = 5 * 60 * 1000): boolean => {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    const exp = payload.exp * 1000; // Convert to milliseconds
    const now = Date.now();
    
    return (exp - now) < windowMs;
  } catch {
    // If token decoding fails, consider it expired
    return true;
  }
};

/**
 * Redirects the user to the login page if they're not already there.
 */
export const redirectToLogin = (): void => {
  if (window.location.pathname !== '/login') {
    window.location.href = '/login';
  }
};

/**
 * Handles authentication failure by clearing state and redirecting to login.
 * This is a comprehensive cleanup function for when authentication fails.
 */
export const handleAuthenticationFailure = (reason: string = 'Unknown authentication failure'): void => {
  // Clear all authentication state
  clearAuthenticationState();
  
  // Redirect to login
  redirectToLogin();
};
