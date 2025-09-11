import { renderHook, act } from '@testing-library/react';
import { useApiClient, useUserProfile, usePasswordChange } from './hooks';
import { createApiClient } from './client';

// Mock the client module
jest.mock('./client', () => ({
  createApiClient: jest.fn(),
}));

const mockCreateApiClient = createApiClient as jest.MockedFunction<typeof createApiClient>;

describe('API hooks', () => {
  const mockKcContext = {
    authUrl: 'http://localhost:8080',
    realm: { name: 'test-realm' },
  };

  const mockApiClient = {
    getUserProfile: jest.fn(),
    updateUserProfile: jest.fn(),
    changePassword: jest.fn(),
  };

  beforeEach(() => {
    jest.clearAllMocks();
    mockCreateApiClient.mockReturnValue(mockApiClient as any);
  });

  describe('useApiClient', () => {
    it('should initialize API client successfully', async () => {
      const { result } = renderHook(() => useApiClient(mockKcContext));

      // Wait for the effect to run
      await act(async () => {
        await new Promise(resolve => setTimeout(resolve, 0));
      });

      expect(result.current.loading).toBe(false);
      expect(result.current.apiClient).toBe(mockApiClient);
      expect(result.current.error).toBe(null);
      expect(mockCreateApiClient).toHaveBeenCalledWith(mockKcContext);
    });

    it('should handle API client initialization error', async () => {
      const errorMessage = 'Failed to create client';
      mockCreateApiClient.mockImplementation(() => {
        throw new Error(errorMessage);
      });

      const { result } = renderHook(() => useApiClient(mockKcContext));

      await act(async () => {
        // Wait for useEffect to complete
      });

      expect(result.current.loading).toBe(false);
      expect(result.current.apiClient).toBe(null);
      expect(result.current.error).toBe(errorMessage);
    });

    it('should handle non-Error exceptions', async () => {
      mockCreateApiClient.mockImplementation(() => {
        throw 'String error';
      });

      const { result } = renderHook(() => useApiClient(mockKcContext));

      await act(async () => {
        // Wait for useEffect to complete
      });

      expect(result.current.loading).toBe(false);
      expect(result.current.apiClient).toBe(null);
      expect(result.current.error).toBe('Failed to initialize API client');
    });
  });

  describe('useUserProfile', () => {
    it('should return profile management functions', () => {
      const { result } = renderHook(() => useUserProfile(mockKcContext));

      expect(result.current.loading).toBe(false);
      expect(result.current.error).toBe(null);
      expect(typeof result.current.updateProfile).toBe('function');
    });

    it('should handle successful profile update', async () => {
      mockApiClient.updateUserProfile.mockResolvedValue({ success: true });

      const { result } = renderHook(() => useUserProfile(mockKcContext));

      const updateData = {
        firstName: 'NewFirst',
        lastName: 'NewLast',
      };

      let updateResult: boolean;
      await act(async () => {
        updateResult = await result.current.updateProfile(updateData);
      });

      expect(updateResult!).toBe(true);
      expect(mockApiClient.updateUserProfile).toHaveBeenCalledWith(updateData);
      expect(result.current.error).toBe(null);
    });

    it('should handle profile update error', async () => {
      const errorMessage = 'Update failed';
      mockApiClient.updateUserProfile.mockResolvedValue({ 
        success: false, 
        error: errorMessage 
      });

      const { result } = renderHook(() => useUserProfile(mockKcContext));

      const updateData = {
        firstName: 'NewFirst',
      };

      let updateResult: boolean;
      await act(async () => {
        updateResult = await result.current.updateProfile(updateData);
      });

      expect(updateResult!).toBe(false);
      expect(result.current.error).toBe(errorMessage);
    });

    it('should handle API client not available', async () => {
      mockCreateApiClient.mockReturnValue(null as any);

      const { result } = renderHook(() => useUserProfile(mockKcContext));

      const updateData = {
        firstName: 'NewFirst',
      };

      let updateResult: boolean;
      await act(async () => {
        updateResult = await result.current.updateProfile(updateData);
      });

      expect(updateResult!).toBe(false);
      expect(result.current.error).toBe('API client not available');
    });

    it('should handle exceptions during profile update', async () => {
      const errorMessage = 'Network error';
      mockApiClient.updateUserProfile.mockRejectedValue(new Error(errorMessage));

      const { result } = renderHook(() => useUserProfile(mockKcContext));

      const updateData = {
        firstName: 'NewFirst',
      };

      let updateResult: boolean;
      await act(async () => {
        updateResult = await result.current.updateProfile(updateData);
      });

      expect(updateResult!).toBe(false);
      expect(result.current.error).toBe(errorMessage);
    });
  });

  describe('usePasswordChange', () => {
    it('should return password change functions', () => {
      const { result } = renderHook(() => usePasswordChange(mockKcContext));

      expect(result.current.loading).toBe(false);
      expect(result.current.error).toBe(null);
      expect(typeof result.current.changePassword).toBe('function');
    });

    it('should handle successful password change', async () => {
      mockApiClient.changePassword.mockResolvedValue({ success: true });

      const { result } = renderHook(() => usePasswordChange(mockKcContext));

      const passwordData = {
        currentPassword: 'oldpassword',
        newPassword: 'newpassword',
        confirmPassword: 'newpassword',
      };

      let changeResult: boolean;
      await act(async () => {
        changeResult = await result.current.changePassword(passwordData);
      });

      expect(changeResult!).toBe(true);
      expect(mockApiClient.changePassword).toHaveBeenCalledWith(passwordData);
      expect(result.current.error).toBe(null);
    });

    it('should handle password change error', async () => {
      const errorMessage = 'Password change failed';
      mockApiClient.changePassword.mockResolvedValue({ 
        success: false, 
        error: errorMessage 
      });

      const { result } = renderHook(() => usePasswordChange(mockKcContext));

      const passwordData = {
        currentPassword: 'oldpassword',
        newPassword: 'newpassword',
        confirmPassword: 'newpassword',
      };

      let changeResult: boolean;
      await act(async () => {
        changeResult = await result.current.changePassword(passwordData);
      });

      expect(changeResult!).toBe(false);
      expect(result.current.error).toBe(errorMessage);
    });

    it('should handle API client not available', async () => {
      mockCreateApiClient.mockReturnValue(null as any);

      const { result } = renderHook(() => usePasswordChange(mockKcContext));

      const passwordData = {
        currentPassword: 'oldpassword',
        newPassword: 'newpassword',
        confirmPassword: 'newpassword',
      };

      let changeResult: boolean;
      await act(async () => {
        changeResult = await result.current.changePassword(passwordData);
      });

      expect(changeResult!).toBe(false);
      expect(result.current.error).toBe('API client not available');
    });

    it('should handle exceptions during password change', async () => {
      const errorMessage = 'Network error';
      mockApiClient.changePassword.mockRejectedValue(new Error(errorMessage));

      const { result } = renderHook(() => usePasswordChange(mockKcContext));

      const passwordData = {
        currentPassword: 'oldpassword',
        newPassword: 'newpassword',
        confirmPassword: 'newpassword',
      };

      let changeResult: boolean;
      await act(async () => {
        changeResult = await result.current.changePassword(passwordData);
      });

      expect(changeResult!).toBe(false);
      expect(result.current.error).toBe(errorMessage);
    });
  });
});
