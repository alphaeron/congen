import { useI18n, type I18n } from './i18n';

// Mock keycloakify/account
jest.mock('keycloakify/account', () => ({
  i18nBuilder: {
    withThemeName: jest.fn().mockReturnThis(),
    build: jest.fn().mockReturnValue({
      useI18n: jest.fn(),
      ofTypeI18n: {},
    }),
  },
}));

describe('i18n', () => {
  describe('exports', () => {
    it('should export useI18n function', () => {
      expect(typeof useI18n).toBe('function');
    });

    it('should export I18n type', () => {
      // Type check - this will fail at compile time if the type is not exported correctly
      const i18nType: I18n = {};
      expect(i18nType).toBeDefined();
    });
  });

  describe('useI18n', () => {
    it('should be a function', () => {
      expect(typeof useI18n).toBe('function');
    });
  });
});
