import type { KcContextLike } from "@keycloakify/keycloak-account-ui";

export interface UserProfile {
    id?: string;
    username?: string;
    email?: string;
    firstName?: string;
    lastName?: string;
    emailVerified?: boolean;
    enabled?: boolean;
    createdTimestamp?: number;
    attributes?: Record<string, string[]>;
}

export type KcContext = KcContextLike & {
    themeType: "account";
    themeName: string;
    properties: Record<string, string>;
    user?: UserProfile;
    // Additional Keycloak context properties
    authUrl?: string;
    serverBaseUrl?: string;
    realm?: string;
    accessToken?: string;
    token?: string;
    // User profile data from Keycloak
    profile?: UserProfile;
    // Keycloak account context properties
    account?: {
        user?: UserProfile;
    };
    // Direct user data from Keycloak
    userProfile?: UserProfile;
};