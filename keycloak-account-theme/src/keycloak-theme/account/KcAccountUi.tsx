import { Suspense, lazy } from "react";
import { ThemeProvider, createTheme } from "@mui/material/styles";
import { CssBaseline, CircularProgress, Box } from "@mui/material";
import type { KcContext } from "./KcContext";
import { getTheme } from "../../theme";

// Lazy load the account components
const Account = lazy(() => import("./Account"));

const theme = createTheme(getTheme('light'));

export default function KcAccountUi(props: { kcContext: KcContext }) {
    const { kcContext } = props;
    
    // Debug logging
    console.log('KcAccountUi - kcContext:', kcContext);

    return (
        <ThemeProvider theme={theme}>
            <CssBaseline />
            <Suspense
                fallback={
                    <Box
                        sx={{
                            display: 'flex',
                            justifyContent: 'center',
                            alignItems: 'center',
                            minHeight: '100vh',
                        }}
                    >
                        <CircularProgress size={60} />
                    </Box>
                }
            >
                <Account kcContext={kcContext} />
            </Suspense>
        </ThemeProvider>
    );
}
