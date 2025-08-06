import { decodeToken, hasAnyPermission } from './authUtils';

// Type for OIDC user object (matching the one in authUtils.ts)
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
  describe('decodeToken', () => {
    it('should decode valid JWT token', () => {
      // Create a mock JWT token with known payload
      const mockPayload = {
        sub: 'user123',
        name: 'Test User',
        groups: ['admin', 'user'],
        realm_access: { roles: ['admin'] },
      };

      const mockToken = `header.${btoa(JSON.stringify(mockPayload))}.signature`;

      const result = decodeToken(mockToken);

      expect(result).toEqual(mockPayload);
    });

    it('should handle token with special characters in payload', () => {
      const mockPayload = {
        sub: 'user123',
        name: 'Test User with special chars: !@#$%^&*()',
        groups: ['admin', 'user'],
        realm_access: { roles: ['admin'] },
      };

      const mockToken = `header.${btoa(JSON.stringify(mockPayload))}.signature`;

      const result = decodeToken(mockToken);

      expect(result).toEqual(mockPayload);
    });

    it('should return null for invalid token format', () => {
      const invalidToken = 'invalid-token-format';

      const result = decodeToken(invalidToken);

      expect(result).toBeNull();
    });

    it('should return null for token with invalid base64', () => {
      const invalidToken = 'header.invalid-base64.signature';

      const result = decodeToken(invalidToken);

      expect(result).toBeNull();
    });

    it('should return null for token with invalid JSON', () => {
      const invalidToken = `header.${btoa('invalid-json')}.signature`;

      const result = decodeToken(invalidToken);

      expect(result).toBeNull();
    });

    it('should handle empty token', () => {
      const result = decodeToken('');

      expect(result).toBeNull();
    });

    it('should handle null token', () => {
      const result = decodeToken(null as unknown as string);

      expect(result).toBeNull();
    });

    it('should handle undefined token', () => {
      const result = decodeToken(undefined as unknown as string);

      expect(result).toBeNull();
    });
  });

  describe('hasAnyPermission', () => {
    const createMockUser = (groups: string[] = [], roles: string[] = []) => ({
      profile: {
        groups,
        realm_access: { roles },
      },
    });

    it('should return false for null user', () => {
      const result = hasAnyPermission(null, ['admin']);

      expect(result).toBe(false);
    });

    it('should return false for empty permissions array', () => {
      const user = createMockUser(['admin'], ['admin']);

      const result = hasAnyPermission(user, []);

      expect(result).toBe(false);
    });

    it('should return true when user has matching group', () => {
      const user = createMockUser(['admin', 'user'], ['user']);

      const result = hasAnyPermission(user, ['admin']);

      expect(result).toBe(true);
    });

    it('should return true when user has matching role', () => {
      const user = createMockUser(['user'], ['admin', 'user']);

      const result = hasAnyPermission(user, ['admin']);

      expect(result).toBe(true);
    });

    it('should return true when user has matching permission in both groups and roles', () => {
      const user = createMockUser(['admin'], ['admin']);

      const result = hasAnyPermission(user, ['admin']);

      expect(result).toBe(true);
    });

    it('should return false when user has no matching permissions', () => {
      const user = createMockUser(['user'], ['user']);

      const result = hasAnyPermission(user, ['admin', 'moderator']);

      expect(result).toBe(false);
    });

    it('should return true when user has any of multiple required permissions', () => {
      const user = createMockUser(['user'], ['admin']);

      const result = hasAnyPermission(user, ['admin', 'moderator']);

      expect(result).toBe(true);
    });

    it('should handle user with no groups or roles', () => {
      const user = createMockUser([], []);

      const result = hasAnyPermission(user, ['admin']);

      expect(result).toBe(false);
    });

    it('should handle user with undefined groups and roles', () => {
      const user = {
        profile: {
          groups: undefined,
          realm_access: { roles: undefined },
        },
      };

      const result = hasAnyPermission(user as unknown as OidcUser, ['admin']);

      expect(result).toBe(false);
    });

    it('should handle user with null groups and roles', () => {
      const user = {
        profile: {
          groups: null,
          realm_access: { roles: null },
        },
      };

      const result = hasAnyPermission(user as unknown as OidcUser, ['admin']);

      expect(result).toBe(false);
    });

    it('should handle user with missing profile', () => {
      const user = {};

      const result = hasAnyPermission(user as unknown as OidcUser, ['admin']);

      expect(result).toBe(false);
    });

    it('should handle user with missing realm_access', () => {
      const user = {
        profile: {
          groups: ['admin'],
        },
      };

      const result = hasAnyPermission(user as unknown as OidcUser, ['admin']);

      expect(result).toBe(true);
    });

    it('should handle case-sensitive permission matching', () => {
      const user = createMockUser(['Admin'], ['User']);

      const result = hasAnyPermission(user, ['admin', 'user']);

      expect(result).toBe(false);
    });

    it('should handle empty strings in permissions', () => {
      const user = createMockUser(['admin'], ['user']);

      const result = hasAnyPermission(user, ['', 'admin']);

      expect(result).toBe(true);
    });

    it('should handle special characters in permissions', () => {
      const user = createMockUser(['admin-user'], ['user@domain']);

      const result = hasAnyPermission(user, ['admin-user', 'user@domain']);

      expect(result).toBe(true);
    });

    it('should handle whitespace in permissions', () => {
      const user = createMockUser([' admin '], [' user ']);

      const result = hasAnyPermission(user, ['admin', 'user']);

      expect(result).toBe(false); // Exact match required
    });
  });
});
