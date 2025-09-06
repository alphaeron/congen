import { createRoot } from 'react-dom/client';
import { StrictMode } from 'react';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import { CssBaseline } from '@mui/material';
import KcPage from './account/KcPage';
import { getTheme } from './theme';
import type { KcContext } from './account/KcContext';

// Declare global window interface for Keycloak context
declare global {
  interface Window {
    kcContext?: unknown;
  }
}

// Create the Congen theme
const theme = createTheme(getTheme('light'));

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <ThemeProvider theme={theme}>
      <CssBaseline />
      {!window.kcContext ? (
        <h1>No Keycloak Context</h1>
      ) : (
        <KcPage kcContext={window.kcContext as KcContext} />
      )}
    </ThemeProvider>
  </StrictMode>
);
