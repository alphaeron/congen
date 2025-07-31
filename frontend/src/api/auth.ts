import { KEYCLOAK_URL } from '../globals';

export interface AuthCodeResponse {
  code: string;
  state: string;
}

export interface TokenResponse {
  access_token: string;
  refresh_token: string;
  expires_in: number;
  refresh_expires_in: number;
  token_type: string;
}

export interface LogoutRequest {
  refresh_token: string;
}

/**
 * Generate a random string for PKCE code verifier
 */
export const generateCodeVerifier = (): string => {
  const array = new Uint8Array(32);
  crypto.getRandomValues(array);
  return btoa(String.fromCharCode(...Array.from(array)))
    .replace(/\+/g, '-')
    .replace(/\//g, '_')
    .replace(/=/g, '');
};

/**
 * Generate PKCE code challenge from verifier
 */
export const generateCodeChallenge = async (verifier: string): Promise<string> => {
  const encoder = new TextEncoder();
  const data = encoder.encode(verifier);
  const digest = await crypto.subtle.digest('SHA-256', data);
  return btoa(String.fromCharCode(...Array.from(new Uint8Array(digest))))
    .replace(/\+/g, '-')
    .replace(/\//g, '_')
    .replace(/=/g, '');
};

/**
 * Generate a random state parameter for CSRF protection
 */
export const generateState = (): string => {
  const array = new Uint8Array(16);
  crypto.getRandomValues(array);
  return btoa(String.fromCharCode(...Array.from(array)))
    .replace(/\+/g, '-')
    .replace(/\//g, '_')
    .replace(/=/g, '');
};

/**
 * Initiate the authorization code flow with PKCE
 */
export const initiateAuth = async (): Promise<void> => {
  const codeVerifier = generateCodeVerifier();
  const codeChallenge = await generateCodeChallenge(codeVerifier);
  const state = generateState();
  
  // Store PKCE values for later use
  sessionStorage.setItem('code_verifier', codeVerifier);
  sessionStorage.setItem('auth_state', state);
  
  // Build authorization URL
  const params = new URLSearchParams({
    response_type: 'code',
    client_id: 'congen-frontend',
    redirect_uri: `${window.location.origin}/auth/callback`,
    code_challenge: codeChallenge,
    code_challenge_method: 'S256',
    state: state,
    scope: 'openid profile email',
  });
  
  // Redirect to Keycloak authorization endpoint
  window.location.href = `${KEYCLOAK_URL}/realms/congen/protocol/openid-connect/auth?${params.toString()}`;
};

/**
 * Exchange authorization code for tokens
 */
export const exchangeCodeForTokens = async (code: string, state: string): Promise<TokenResponse> => {
  const storedState = sessionStorage.getItem('auth_state');
  const codeVerifier = sessionStorage.getItem('code_verifier');
  
  if (state !== storedState) {
    throw new Error('State mismatch - possible CSRF attack');
  }
  
  if (!codeVerifier) {
    throw new Error('Code verifier not found');
  }
  
  const params = new URLSearchParams({
    grant_type: 'authorization_code',
    client_id: 'congen-frontend',
    code_verifier: codeVerifier,
    code: code,
    redirect_uri: `${window.location.origin}/auth/callback`,
  });
  
  const tokens = await makeTokenRequest(params, 'Token exchange failed');
  
  // Clear PKCE values
  sessionStorage.removeItem('code_verifier');
  sessionStorage.removeItem('auth_state');
  
  return tokens;
};

/**
 * Common function to make token requests to Keycloak
 */
const makeTokenRequest = async (params: URLSearchParams, errorMessage: string): Promise<TokenResponse> => {
  const response = await fetch(`${KEYCLOAK_URL}/realms/congen/protocol/openid-connect/token`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
    },
    body: params.toString(),
  });
  
  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.error_description || errorMessage);
  }
  
  return response.json();
};

/**
 * Refresh access token using refresh token
 */
export const refreshToken = async (refreshTokenValue: string): Promise<TokenResponse> => {
  const params = new URLSearchParams({
    grant_type: 'refresh_token',
    client_id: 'congen-frontend',
    refresh_token: refreshTokenValue,
  });
  
  return makeTokenRequest(params, 'Token refresh failed');
};

/**
 * Logout user from Keycloak
 */
export const logoutUser = async (refreshTokenValue: string): Promise<void> => {
  const params = new URLSearchParams({
    client_id: 'congen-frontend',
    refresh_token: refreshTokenValue,
  });
  
  await fetch(`${KEYCLOAK_URL}/realms/congen/protocol/openid-connect/logout`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
    },
    body: params.toString(),
  });
}; 