import { Suspense, lazy } from "react";
import { Box } from "@mui/material";
import { LoadingSpinner } from "../../components/LoadingSpinner";
import type { KcContext } from "./KcContext";
import { AuthProvider } from "./AuthProvider";

// Lazy load the account components
const Account = lazy(() => import("./Account"));

export default function KcAccountUi(props: { kcContext: KcContext }) {
    const { kcContext } = props;
    
    // Debug logging
    console.log('KcAccountUi - kcContext:', kcContext);

    return (
        <AuthProvider>
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
                        <LoadingSpinner size={60} />
                    </Box>
                }
            >
                <Account kcContext={kcContext} />
            </Suspense>
        </AuthProvider>
    );
}
