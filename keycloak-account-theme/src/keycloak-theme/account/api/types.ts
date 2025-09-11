/**
 * API types for Keycloak account management
 * Based on Keycloak account UI API patterns
 */

export interface UserProfile {
  // Keycloak account API response fields
  id?: string;
  username?: string;
  email?: string;
  firstName?: string;
  lastName?: string;
  emailVerified?: boolean;
  enabled?: boolean;
  createdTimestamp?: number;
  attributes?: Record<string, string[]>;
  // OpenID Connect fields (for compatibility)
  sub?: string;
  preferred_username?: string;
  email_verified?: boolean;
  given_name?: string;
  family_name?: string;
  name?: string;
}

export interface UpdateUserProfileRequest {
  email?: string;
  firstName?: string;
  lastName?: string;
  attributes?: Record<string, string[]>;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
}

export interface ApiResponse<T = any> {
  success: boolean;
  data?: T;
  error?: string;
  message?: string;
}

export interface ValidationError {
  field: string;
  message: string;
}

export interface ApiError {
  success: false;
  error: string;
  validationErrors?: ValidationError[];
  statusCode?: number;
}

// Keycloak API endpoints
export const API_ENDPOINTS = {
  USER_PROFILE: '/realms/{realm}/account/?userProfileMetadata=true',
  UPDATE_PROFILE: '/realms/{realm}/account/',
  CHANGE_PASSWORD: '/realms/{realm}/account/credentials/password',
  DELETE_ACCOUNT: '/realms/{realm}/account/',
  SESSIONS: '/realms/{realm}/account/sessions',
  APPLICATIONS: '/realms/{realm}/account/applications',
  CREDENTIALS: '/realms/{realm}/account/credentials',
} as const;

export type ApiEndpoint = typeof API_ENDPOINTS[keyof typeof API_ENDPOINTS];
