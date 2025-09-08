import * as React from 'react';
import ReactDOM from 'react-dom/client';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import { CssBaseline } from '@mui/material';
import { KcPage, type KcContext } from "./keycloak-theme/kc.gen";
import { getTheme } from './theme';
import './account.css';

// Create the Congen theme
const theme = createTheme(getTheme('light'));

// Get the root element - Keycloakify generates a "root" div
const rootElement = document.getElementById("root") as HTMLElement;

if (rootElement) {
    const root = ReactDOM.createRoot(rootElement);
    
    root.render(
        <React.StrictMode>
            <ThemeProvider theme={theme}>
                <CssBaseline />
                {window.kcContext ? (
                    <KcPage kcContext={window.kcContext} />
                ) : (
                    <div style={{ padding: '20px', textAlign: 'center' }}>
                        <h1>Loading Keycloak Account...</h1>
                        <p>Please wait while we initialize your account session.</p>
                    </div>
                )}
            </ThemeProvider>
        </React.StrictMode>
    );
} else {
    console.error('Could not find root element');
}

declare global {
    interface Window {
        kcContext?: KcContext;
    }
}