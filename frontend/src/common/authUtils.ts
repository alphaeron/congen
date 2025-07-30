// Type for OIDC user object
interface OidcUser {
  profile?: {
    sub?: string;
    preferred_username?: string;
    email?: string;
    name?: string;
    roles?: string[];
    groups?: string[];
    realm_access?: {
      roles?: string[];
    };
  };
}

/**
 * Gets the user's roles from the OIDC user profile.
 *
 * @param user The OIDC user object
 * @return Array of role strings, or empty array if no roles found
 */
export const getUserRoles = (user: OidcUser | null): string[] => {
  if (!user?.profile) {
    return [];
  }
  
  // Check for roles in realm_access.roles (standard Keycloak format)
  const realmAccess = user.profile.realm_access as { roles?: string[] } | undefined;
  if (realmAccess?.roles) {
    return realmAccess.roles;
  }
  
  // Fallback to direct roles property
  return (user.profile.roles as string[]) || [];
};

/**
 * Gets the user's groups from the OIDC user profile.
 *
 * @param user The OIDC user object
 * @return Array of group strings, or empty array if no groups found
 */
export const getUserGroups = (user: OidcUser | null): string[] => {
  if (!user?.profile) {
    return [];
  }
  
  return (user.profile.groups as string[]) || [];
};

/**
 * Gets all user permissions (roles and groups combined).
 *
 * @param user The OIDC user object
 * @return Array of permission strings (roles and groups)
 */
export const getUserPermissions = (user: OidcUser | null): string[] => {
  const roles = getUserRoles(user);
  const groups = getUserGroups(user);
  return [...roles, ...groups];
};

/**
 * Checks if the user has any of the specified permissions (roles or groups).
 *
 * @param user The OIDC user object
 * @param permissions Array of permission strings to check
 * @return True if user has any of the specified permissions
 */
export const hasAnyPermission = (user: OidcUser | null, permissions: string[]): boolean => {
  if (!user || !permissions.length) {
    return false;
  }
  
  const userPermissions = getUserPermissions(user);
  return permissions.some(permission => userPermissions.includes(permission));
};

/**
 * Checks if the user has all of the specified permissions (roles or groups).
 *
 * @param user The OIDC user object
 * @param permissions Array of permission strings to check
 * @return True if user has all of the specified permissions
 */
export const hasAllPermissions = (user: OidcUser | null, permissions: string[]): boolean => {
  if (!user || !permissions.length) {
    return false;
  }
  
  const userPermissions = getUserPermissions(user);
  return permissions.every(permission => userPermissions.includes(permission));
}; 