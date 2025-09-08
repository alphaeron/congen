import type { KcContextLike } from "@keycloakify/keycloak-account-ui";

export type KcContext = KcContextLike & {
    themeType: "account";
    themeName: string;
    properties: Record<string, string>;
    user?: {
        username?: string;
        email?: string;
        firstName?: string;
        lastName?: string;
    };
};