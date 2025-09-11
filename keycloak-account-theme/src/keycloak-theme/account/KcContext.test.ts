import type { KcContext, UserProfile } from './KcContext';

describe('KcContext', () => {
  describe('UserProfile interface', () => {
    it('should allow all optional fields', () => {
      const userProfile: UserProfile = {
        id: '123',
        username: 'testuser',
        email: 'test@example.com',
        firstName: 'Test',
        lastName: 'User',
        emailVerified: true,
        enabled: true,
        createdTimestamp: 1234567890,
        attributes: { 'custom.attr': ['value1', 'value2'] },
      };

      expect(userProfile.id).toBe('123');
      expect(userProfile.username).toBe('testuser');
      expect(userProfile.email).toBe('test@example.com');
      expect(userProfile.firstName).toBe('Test');
      expect(userProfile.lastName).toBe('User');
      expect(userProfile.emailVerified).toBe(true);
      expect(userProfile.enabled).toBe(true);
      expect(userProfile.createdTimestamp).toBe(1234567890);
      expect(userProfile.attributes).toEqual({ 'custom.attr': ['value1', 'value2'] });
    });

    it('should allow empty object', () => {
      const userProfile: UserProfile = {};
      expect(userProfile).toEqual({});
    });
  });

  describe('KcContext interface', () => {
    it('should allow minimal context', () => {
      const kcContext: KcContext = {
        themeType: 'account',
        themeName: 'congen-account-theme',
        properties: {},
      };

      expect(kcContext.themeType).toBe('account');
      expect(kcContext.themeName).toBe('congen-account-theme');
      expect(kcContext.properties).toEqual({});
    });

    it('should allow full context with all optional fields', () => {
      const userProfile: UserProfile = {
        id: '123',
        username: 'testuser',
        email: 'test@example.com',
        firstName: 'Test',
        lastName: 'User',
      };

      const kcContext: KcContext = {
        themeType: 'account',
        themeName: 'congen-account-theme',
        properties: { 'custom.prop': 'value' },
        user: userProfile,
        authUrl: 'http://localhost:8080',
        serverBaseUrl: 'http://localhost:8080',
        realm: 'congen',
        accessToken: 'access-token',
        token: 'token',
        profile: userProfile,
        account: {
          user: userProfile,
        },
        userProfile: userProfile,
      };

      expect(kcContext.themeType).toBe('account');
      expect(kcContext.themeName).toBe('congen-account-theme');
      expect(kcContext.properties).toEqual({ 'custom.prop': 'value' });
      expect(kcContext.user).toEqual(userProfile);
      expect(kcContext.authUrl).toBe('http://localhost:8080');
      expect(kcContext.serverBaseUrl).toBe('http://localhost:8080');
      expect(kcContext.realm).toBe('congen');
      expect(kcContext.accessToken).toBe('access-token');
      expect(kcContext.token).toBe('token');
      expect(kcContext.profile).toEqual(userProfile);
      expect(kcContext.account?.user).toEqual(userProfile);
      expect(kcContext.userProfile).toEqual(userProfile);
    });

    it('should allow context with only required fields', () => {
      const kcContext: KcContext = {
        themeType: 'account',
        themeName: 'test-theme',
        properties: {},
      };

      expect(kcContext.themeType).toBe('account');
      expect(kcContext.themeName).toBe('test-theme');
      expect(kcContext.properties).toEqual({});
    });

    it('should allow context with user data in different locations', () => {
      const userProfile: UserProfile = {
        id: '123',
        username: 'testuser',
        email: 'test@example.com',
      };

      const kcContext: KcContext = {
        themeType: 'account',
        themeName: 'test-theme',
        properties: {},
        user: userProfile,
        profile: userProfile,
        userProfile: userProfile,
        account: {
          user: userProfile,
        },
      };

      expect(kcContext.user).toEqual(userProfile);
      expect(kcContext.profile).toEqual(userProfile);
      expect(kcContext.userProfile).toEqual(userProfile);
      expect(kcContext.account?.user).toEqual(userProfile);
    });
  });
});
