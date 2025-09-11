import type {
  UserProfile,
  UpdateUserProfileRequest,
  ChangePasswordRequest,
  ApiResponse,
  ValidationError,
  ApiError,
  UseUserProfileResult,
  UsePasswordChangeResult,
} from './types';

describe('API types', () => {
  describe('UserProfile', () => {
    it('should allow all optional fields', () => {
      const userProfile: UserProfile = {
        id: '123',
        username: 'testuser',
        email: 'test@example.com',
        firstName: 'Test',
        lastName: 'User',
        emailVerified: true,
        enabled: true,
        createdTimestamp: 1234567890,
        attributes: { 'custom.attr': ['value1', 'value2'] },
        sub: '123',
        preferred_username: 'testuser',
        email_verified: true,
        given_name: 'Test',
        family_name: 'User',
        name: 'Test User',
      };

      expect(userProfile.id).toBe('123');
      expect(userProfile.username).toBe('testuser');
      expect(userProfile.email).toBe('test@example.com');
      expect(userProfile.firstName).toBe('Test');
      expect(userProfile.lastName).toBe('User');
      expect(userProfile.emailVerified).toBe(true);
      expect(userProfile.enabled).toBe(true);
      expect(userProfile.createdTimestamp).toBe(1234567890);
      expect(userProfile.attributes).toEqual({ 'custom.attr': ['value1', 'value2'] });
      expect(userProfile.sub).toBe('123');
      expect(userProfile.preferred_username).toBe('testuser');
      expect(userProfile.email_verified).toBe(true);
      expect(userProfile.given_name).toBe('Test');
      expect(userProfile.family_name).toBe('User');
      expect(userProfile.name).toBe('Test User');
    });

    it('should allow empty object', () => {
      const userProfile: UserProfile = {};
      expect(userProfile).toEqual({});
    });
  });

  describe('UpdateUserProfileRequest', () => {
    it('should allow all optional fields', () => {
      const updateRequest: UpdateUserProfileRequest = {
        email: 'newemail@example.com',
        firstName: 'NewFirst',
        lastName: 'NewLast',
        attributes: { 'custom.attr': ['newvalue'] },
      };

      expect(updateRequest.email).toBe('newemail@example.com');
      expect(updateRequest.firstName).toBe('NewFirst');
      expect(updateRequest.lastName).toBe('NewLast');
      expect(updateRequest.attributes).toEqual({ 'custom.attr': ['newvalue'] });
    });

    it('should allow empty object', () => {
      const updateRequest: UpdateUserProfileRequest = {};
      expect(updateRequest).toEqual({});
    });
  });

  describe('ChangePasswordRequest', () => {
    it('should require all fields', () => {
      const changePasswordRequest: ChangePasswordRequest = {
        currentPassword: 'oldpassword',
        newPassword: 'newpassword',
        confirmPassword: 'newpassword',
      };

      expect(changePasswordRequest.currentPassword).toBe('oldpassword');
      expect(changePasswordRequest.newPassword).toBe('newpassword');
      expect(changePasswordRequest.confirmPassword).toBe('newpassword');
    });
  });

  describe('ApiResponse', () => {
    it('should allow success response with data', () => {
      const successResponse: ApiResponse<{ id: string }> = {
        success: true,
        data: { id: '123' },
      };

      expect(successResponse.success).toBe(true);
      expect(successResponse.data).toEqual({ id: '123' });
    });

    it('should allow error response', () => {
      const errorResponse: ApiResponse = {
        success: false,
        error: 'Something went wrong',
        message: 'Error message',
      };

      expect(errorResponse.success).toBe(false);
      expect(errorResponse.error).toBe('Something went wrong');
      expect(errorResponse.message).toBe('Error message');
    });
  });

  describe('ValidationError', () => {
    it('should require field and message', () => {
      const validationError: ValidationError = {
        field: 'email',
        message: 'Email is required',
      };

      expect(validationError.field).toBe('email');
      expect(validationError.message).toBe('Email is required');
    });
  });

  describe('ApiError', () => {
    it('should allow all fields', () => {
      const apiError: ApiError = {
        success: false,
        error: 'API Error',
        validationErrors: [
          { field: 'email', message: 'Email is required' },
        ],
        statusCode: 400,
      };

      expect(apiError.success).toBe(false);
      expect(apiError.error).toBe('API Error');
      expect(apiError.validationErrors).toEqual([
        { field: 'email', message: 'Email is required' },
      ]);
      expect(apiError.statusCode).toBe(400);
    });

    it('should allow minimal error', () => {
      const apiError: ApiError = {
        success: false,
        error: 'Simple error',
      };

      expect(apiError.success).toBe(false);
      expect(apiError.error).toBe('Simple error');
    });
  });

  describe('UseUserProfileResult', () => {
    it('should allow all fields', () => {
      const result: UseUserProfileResult = {
        user: { id: '123', username: 'testuser' },
        loading: false,
        error: null,
        refetch: jest.fn(),
        updateProfile: jest.fn(),
      };

      expect(result.user).toEqual({ id: '123', username: 'testuser' });
      expect(result.loading).toBe(false);
      expect(result.error).toBe(null);
      expect(typeof result.refetch).toBe('function');
      expect(typeof result.updateProfile).toBe('function');
    });
  });

  describe('UsePasswordChangeResult', () => {
    it('should allow all fields', () => {
      const result: UsePasswordChangeResult = {
        changePassword: jest.fn(),
        loading: false,
        error: null,
      };

      expect(typeof result.changePassword).toBe('function');
      expect(result.loading).toBe(false);
      expect(result.error).toBe(null);
    });
  });
});
