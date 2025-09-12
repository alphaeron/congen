import * as React from 'react';
import ReactDOM from 'react-dom/client';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import { CssBaseline, useMediaQuery } from '@mui/material';
import { SnackbarProvider } from 'notistack';
import { AuthProvider as OidcAuthProvider } from 'react-oidc-context';
import { KcPage, type KcContext } from './keycloak-theme/kc.gen';
import { getTheme } from './theme';
import { AuthProvider } from './keycloak-theme/account/AuthContext';
import { getAuthProviderConfig } from './keycloak-theme/account/oidcConfig';
import { LoadingSpinner } from './components/LoadingSpinner';

// Import Roboto font to match frontend
import '@fontsource/roboto/300.css';
import '@fontsource/roboto/400.css';
import '@fontsource/roboto/500.css';
import '@fontsource/roboto/700.css';

// Import custom styles for consistent sizing and typography
import './main.css';

// Get the root element - Keycloakify generates a "root" div
const rootElement = document.getElementById('root') as HTMLElement;

if (rootElement) {
  const root = ReactDOM.createRoot(rootElement);

  // Create a component that can use hooks
  const App = () => {
    // Detect user's preferred color scheme - always call hooks at the top level
    const prefersDarkMode = useMediaQuery('(prefers-color-scheme: dark)');
    const mode = prefersDarkMode ? 'dark' : 'light';
    const theme = createTheme(getTheme(mode));

    return (
      <ThemeProvider theme={theme}>
        <CssBaseline />
        <SnackbarProvider>
          <OidcAuthProvider {...getAuthProviderConfig()}>
            <AuthWrapper />
          </OidcAuthProvider>
        </SnackbarProvider>
      </ThemeProvider>
    );
  };

  // Wrapper component for Keycloak account theme
  const AuthWrapper = () => {
    return (
      <AuthProvider>
        {window.kcContext ? (
          <KcPage kcContext={window.kcContext} />
        ) : (
          <LoadingSpinner message="Loading Keycloak Account..." fullHeight={true} size={60} />
        )}
      </AuthProvider>
    );
  };

  root.render(
    <React.StrictMode>
      <App />
    </React.StrictMode>
  );
}

declare global {
  interface Window {
    kcContext?: KcContext;
  }
}
