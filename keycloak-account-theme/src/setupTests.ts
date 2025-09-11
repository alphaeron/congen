import '@testing-library/jest-dom';

// Mock window.kcContext for tests
Object.defineProperty(window, 'kcContext', {
  value: {
    url: {
      loginAction: '/auth/realms/congen/account',
    },
    realm: {
      displayName: 'Congen',
    },
    user: {
      username: 'testuser',
      email: 'test@example.com',
      firstName: 'Test',
      lastName: 'User',
    },
  },
  writable: true,
});
