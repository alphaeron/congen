import {
  getUserRoles,
  getUserGroups,
  getUserPermissions,
  hasAnyPermission,
  hasAllPermissions,
} from './authUtils';

// Type for OIDC user object (matching the interface in authUtils.ts)
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

describe('authUtils', () => {
  describe('getUserRoles', () => {
    it('should return empty array for null user', () => {
      const result = getUserRoles(null);
      expect(result).toEqual([]);
    });

    it('should return empty array for user without profile', () => {
      const user = {} as OidcUser;
      const result = getUserRoles(user);
      expect(result).toEqual([]);
    });

    it('should return roles from realm_access.roles', () => {
      const user: OidcUser = {
        profile: {
          realm_access: {
            roles: ['admin', 'user'],
          },
        },
      };
      const result = getUserRoles(user);
      expect(result).toEqual(['admin', 'user']);
    });

    it('should return roles from direct roles property when realm_access not available', () => {
      const user: OidcUser = {
        profile: {
          roles: ['service', 'user'],
        },
      };
      const result = getUserRoles(user);
      expect(result).toEqual(['service', 'user']);
    });

    it('should return empty array when no roles are present', () => {
      const user: OidcUser = {
        profile: {
          email: 'test@example.com',
        },
      };
      const result = getUserRoles(user);
      expect(result).toEqual([]);
    });
  });

  describe('getUserGroups', () => {
    it('should return empty array for null user', () => {
      const result = getUserGroups(null);
      expect(result).toEqual([]);
    });

    it('should return empty array for user without profile', () => {
      const user = {} as OidcUser;
      const result = getUserGroups(user);
      expect(result).toEqual([]);
    });

    it('should return groups from profile.groups', () => {
      const user: OidcUser = {
        profile: {
          groups: ['premium-users', 'beta-testers'],
        },
      };
      const result = getUserGroups(user);
      expect(result).toEqual(['premium-users', 'beta-testers']);
    });

    it('should return empty array when no groups are present', () => {
      const user: OidcUser = {
        profile: {
          email: 'test@example.com',
        },
      };
      const result = getUserGroups(user);
      expect(result).toEqual([]);
    });
  });

  describe('getUserPermissions', () => {
    it('should return empty array for null user', () => {
      const result = getUserPermissions(null);
      expect(result).toEqual([]);
    });

    it('should combine roles and groups', () => {
      const user: OidcUser = {
        profile: {
          realm_access: {
            roles: ['admin', 'user'],
          },
          groups: ['premium-users', 'beta-testers'],
        },
      };
      const result = getUserPermissions(user);
      expect(result).toEqual(['admin', 'user', 'premium-users', 'beta-testers']);
    });

    it('should handle user with only roles', () => {
      const user: OidcUser = {
        profile: {
          realm_access: {
            roles: ['service'],
          },
        },
      };
      const result = getUserPermissions(user);
      expect(result).toEqual(['service']);
    });

    it('should handle user with only groups', () => {
      const user: OidcUser = {
        profile: {
          groups: ['premium-users'],
        },
      };
      const result = getUserPermissions(user);
      expect(result).toEqual(['premium-users']);
    });

    it('should return empty array when no permissions are present', () => {
      const user: OidcUser = {
        profile: {
          email: 'test@example.com',
        },
      };
      const result = getUserPermissions(user);
      expect(result).toEqual([]);
    });
  });

  describe('hasAnyPermission', () => {
    it('should return false for null user', () => {
      const result = hasAnyPermission(null, ['admin']);
      expect(result).toBe(false);
    });

    it('should return false for empty permissions array', () => {
      const user: OidcUser = {
        profile: {
          realm_access: {
            roles: ['admin'],
          },
        },
      };
      const result = hasAnyPermission(user, []);
      expect(result).toBe(false);
    });

    it('should return true when user has any of the required permissions (roles)', () => {
      const user: OidcUser = {
        profile: {
          realm_access: {
            roles: ['admin', 'user'],
          },
        },
      };
      const result = hasAnyPermission(user, ['admin', 'service']);
      expect(result).toBe(true);
    });

    it('should return true when user has any of the required permissions (groups)', () => {
      const user: OidcUser = {
        profile: {
          groups: ['premium-users', 'beta-testers'],
        },
      };
      const result = hasAnyPermission(user, ['premium-users', 'vip-users']);
      expect(result).toBe(true);
    });

    it('should return true when user has permission from either roles or groups', () => {
      const user: OidcUser = {
        profile: {
          realm_access: {
            roles: ['user'],
          },
          groups: ['premium-users'],
        },
      };
      const result = hasAnyPermission(user, ['admin', 'premium-users']);
      expect(result).toBe(true);
    });

    it('should return false when user has none of the required permissions', () => {
      const user: OidcUser = {
        profile: {
          realm_access: {
            roles: ['user'],
          },
          groups: ['basic-users'],
        },
      };
      const result = hasAnyPermission(user, ['admin', 'service']);
      expect(result).toBe(false);
    });
  });

  describe('hasAllPermissions', () => {
    it('should return false for null user', () => {
      const result = hasAllPermissions(null, ['admin']);
      expect(result).toBe(false);
    });

    it('should return false for empty permissions array', () => {
      const user: OidcUser = {
        profile: {
          realm_access: {
            roles: ['admin'],
          },
        },
      };
      const result = hasAllPermissions(user, []);
      expect(result).toBe(false);
    });

    it('should return true when user has all required permissions (roles only)', () => {
      const user: OidcUser = {
        profile: {
          realm_access: {
            roles: ['admin', 'user', 'service'],
          },
        },
      };
      const result = hasAllPermissions(user, ['admin', 'user']);
      expect(result).toBe(true);
    });

    it('should return true when user has all required permissions (groups only)', () => {
      const user: OidcUser = {
        profile: {
          groups: ['premium-users', 'beta-testers', 'vip-users'],
        },
      };
      const result = hasAllPermissions(user, ['premium-users', 'beta-testers']);
      expect(result).toBe(true);
    });

    it('should return true when user has all permissions from roles and groups combined', () => {
      const user: OidcUser = {
        profile: {
          realm_access: {
            roles: ['admin'],
          },
          groups: ['premium-users'],
        },
      };
      const result = hasAllPermissions(user, ['admin', 'premium-users']);
      expect(result).toBe(true);
    });

    it('should return false when user is missing any required permission', () => {
      const user: OidcUser = {
        profile: {
          realm_access: {
            roles: ['admin'],
          },
          groups: ['premium-users'],
        },
      };
      const result = hasAllPermissions(user, ['admin', 'service']);
      expect(result).toBe(false);
    });

    it('should return false when user has none of the required permissions', () => {
      const user: OidcUser = {
        profile: {
          realm_access: {
            roles: ['user'],
          },
        },
      };
      const result = hasAllPermissions(user, ['admin', 'service']);
      expect(result).toBe(false);
    });
  });
}); 