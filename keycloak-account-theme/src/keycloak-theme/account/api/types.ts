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

export interface UseUserProfileResult {
  user: UserProfile | null;
  loading: boolean;
  error: string | null;
  refetch: () => Promise<void>;
  updateProfile: (data: UpdateUserProfileRequest) => Promise<boolean>;
}

export interface UsePasswordChangeResult {
  changePassword: (data: ChangePasswordRequest) => Promise<boolean>;
  loading: boolean;
  error: string | null;
}
