import '@testing-library/jest-dom';

// Polyfill TextEncoder and TextDecoder for tests
import { TextEncoder, TextDecoder } from 'util';

global.TextEncoder = TextEncoder;
global.TextDecoder = TextDecoder as never;

// Configure React testing environment for concurrent features
import { configure } from '@testing-library/react';

configure({ asyncUtilTimeout: 5000 });

// Set up window.location for React Router tests
Object.defineProperty(window, 'location', {
  value: {
    href: 'http://localhost:3000/',
    origin: 'http://localhost:3000',
    pathname: '/',
    search: '',
    hash: '',
  },
  writable: true,
});

// Mock window.matchMedia for tests
Object.defineProperty(window, 'matchMedia', {
  writable: true,
  value: jest.fn().mockImplementation(query => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: jest.fn(),
    removeListener: jest.fn(),
    addEventListener: jest.fn(),
    removeEventListener: jest.fn(),
    dispatchEvent: jest.fn(),
  })),
});
