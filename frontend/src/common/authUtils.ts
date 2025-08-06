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
