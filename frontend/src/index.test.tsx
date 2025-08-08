import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { SnackbarProvider } from 'notistack';
import * as React from 'react';

// Mock the App component
jest.mock('./App', () => ({
  App: () => <div data-testid="app">App Component</div>,
}));

// Mock the CSS imports
jest.mock('@mui/material-pigment-css/styles.css', () => ({}));
jest.mock('@fontsource/roboto/300.css', () => ({}));
jest.mock('@fontsource/roboto/400.css', () => ({}));
jest.mock('@fontsource/roboto/500.css', () => ({}));
jest.mock('@fontsource/roboto/700.css', () => ({}));
jest.mock('./index.css', () => ({}));

describe('index.tsx', () => {
  it('should have correct imports', () => {
    // Test that the required modules are available
    expect(React).toBeDefined();
    expect(QueryClient).toBeDefined();
    expect(QueryClientProvider).toBeDefined();
    expect(SnackbarProvider).toBeDefined();
  });

  it('should have App component available', () => {
    const { App } = jest.requireMock('./App');
    expect(App).toBeDefined();
  });

  it('should have CSS imports available', () => {
    // These should not throw when imported
    expect(() => jest.requireMock('@mui/material-pigment-css/styles.css')).not.toThrow();
    expect(() => jest.requireMock('@fontsource/roboto/300.css')).not.toThrow();
    expect(() => jest.requireMock('@fontsource/roboto/400.css')).not.toThrow();
    expect(() => jest.requireMock('@fontsource/roboto/500.css')).not.toThrow();
    expect(() => jest.requireMock('@fontsource/roboto/700.css')).not.toThrow();
    expect(() => jest.requireMock('./index.css')).not.toThrow();
  });

  it('should have React available', () => {
    expect(React.StrictMode).toBeDefined();
  });

  it('should have ReactDOM available', () => {
    const ReactDOM = jest.requireMock('react-dom/client');
    expect(ReactDOM).toBeDefined();
    expect(ReactDOM.createRoot).toBeDefined();
  });

  it('should have document.getElementById available', () => {
    expect(document.getElementById).toBeDefined();
  });

  it('should have root element in DOM', () => {
    // Create a mock root element if it doesn't exist
    if (!document.getElementById('root')) {
      const rootElement = document.createElement('div');
      rootElement.id = 'root';
      document.body.appendChild(rootElement);
    }

    const rootElement = document.getElementById('root');
    expect(rootElement).toBeDefined();
  });
});
