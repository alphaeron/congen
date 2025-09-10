import * as React from 'react';
import ReactDOM from 'react-dom/client';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import { CssBaseline, useMediaQuery } from '@mui/material';
import { KcPage, type KcContext } from "./keycloak-theme/kc.gen";
import { getTheme } from './theme';

// Get the root element - Keycloakify generates a "root" div
const rootElement = document.getElementById("root") as HTMLElement;

if (rootElement) {
    const root = ReactDOM.createRoot(rootElement);
    
    // Create a component that can use hooks
    const App = () => {
        // Detect user's preferred color scheme
        const prefersDarkMode = useMediaQuery('(prefers-color-scheme: dark)');
        const mode = prefersDarkMode ? 'dark' : 'light';
        const theme = createTheme(getTheme(mode));
        
        return (
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
        );
    };
    
    root.render(
        <React.StrictMode>
            <App />
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