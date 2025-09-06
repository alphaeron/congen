import { createRoot } from "react-dom/client";
import { StrictMode } from "react";
import { ThemeProvider, createTheme } from "@mui/material/styles";
import { CssBaseline } from "@mui/material";
import { KcPage } from "./kc.gen";
import { getTheme } from "./theme";

// The following block can be uncommented to test a specific page with `npm start`
// Don't forget to comment back or your bundle size will increase
/*
import { getKcContextMock } from "./login/KcPageStory";

if (process.env.NODE_ENV === 'development') {
    window.kcContext = getKcContextMock({
        pageId: "register.ftl",
        overrides: {}
    });
}
*/

// Create the Congen theme
const theme = createTheme(getTheme('light'));

createRoot(document.getElementById("root")!).render(
    <StrictMode>
        <ThemeProvider theme={theme}>
            <CssBaseline />
            {!window.kcContext ? (
                <h1>No Keycloak Context</h1>
            ) : (
                <KcPage kcContext={window.kcContext} />
            )}
        </ThemeProvider>
    </StrictMode>
);
