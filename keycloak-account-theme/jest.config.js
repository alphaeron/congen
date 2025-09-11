module.exports = {
  testEnvironment: 'jsdom',
  setupFilesAfterEnv: ['<rootDir>/src/setupTests.ts'],
  moduleNameMapper: {
    '^@fontsource/.*': '<rootDir>/src/__mocks__/fontsource.css',
    '\\.(css|less|scss|sass)$': 'identity-obj-proxy',
    '\\.(svg|png|jpg|jpeg|gif|ico)$': '<rootDir>/src/__mocks__/fileMock.ts',
    '^keycloakify/account$': '<rootDir>/src/__mocks__/keycloakify.ts',
    '^keycloakify/account/DefaultPage$':
      '<rootDir>/src/__mocks__/keycloakify-account-DefaultPage.tsx',
    '^keycloakify/account/Template$': '<rootDir>/src/__mocks__/keycloakify-account-Template.tsx',
  },
  transform: {
    '^.+\\.(ts|tsx)$': 'babel-jest',
  },
  transformIgnorePatterns: ['node_modules/(?!(keycloakify|@mui|@emotion)/)'],
  testMatch: ['<rootDir>/src/**/?(*.)(spec|test).(ts|tsx|js)'],
  collectCoverageFrom: [
    'src/**/*.{ts,tsx}',
    '!src/**/*.d.ts',
    '!src/**/*.test.{ts,tsx}',
    '!src/**/*.spec.{ts,tsx}',
    '!src/**/test-utils.tsx',
    '!src/**/setupTests.ts',
  ],
  coverageThreshold: {
    global: {
      branches: 80,
      functions: 80,
      lines: 80,
      statements: 80,
    },
  },
  passWithNoTests: true,
};
