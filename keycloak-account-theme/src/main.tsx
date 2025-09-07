import { createRoot } from "react-dom/client";
import { StrictMode } from "react";
import { ThemeProvider, createTheme } from '@mui/material/styles';
import { CssBaseline } from '@mui/material';
import { KcPage, type KcContext } from "./keycloak-theme/kc.gen";
import { getTheme } from './theme';
import './account.css';

// Create the Congen theme
const theme = createTheme(getTheme('light'));

createRoot(document.getElementById("root")!).render(
    <StrictMode>
        <ThemeProvider theme={theme}>
            <CssBaseline />
            {window.kcContext ? (
                <KcPage kcContext={window.kcContext} />
            ) : (
                <h1>No Keycloak Context</h1>
            )}
        </ThemeProvider>
    </StrictMode>
);

declare global {
    interface Window {
        kcContext?: KcContext;
    }
}