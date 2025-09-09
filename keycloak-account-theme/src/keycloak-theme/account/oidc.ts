import { createOidc } from "oidc-spa";

let oidcInstance: any = null;

// Create OIDC instance following keycloakify-starter pattern
const createOidcInstance = () => {
    if (!oidcInstance) {
        oidcInstance = createOidc({
            issuerUri: `${window.location.origin}/realms/congen`,
            clientId: "account-console",
            homeUrl: "/realms/congen/account/",
            autoLogin: true, // Enable auto-login for proper authentication
            silentLoginTimeoutInSeconds: 10, // Reasonable timeout
            // Add additional configuration for better compatibility
            extraQueryParams: {
                // Ensure we get the right scopes
                scope: "openid profile email"
            }
        });
    }
    return oidcInstance;
};

// Export the OIDC instance getter
export const getOidc = async () => {
    const oidc = createOidcInstance();
    // Return the OIDC client instance
    return oidc;
};