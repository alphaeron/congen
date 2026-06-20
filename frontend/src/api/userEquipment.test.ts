import AxiosMockAdapter from 'axios-mock-adapter';

import { expectRequestError } from './apiRequestErrorTestUtils';
import { ENDPOINT } from './endpoint';
import type { UserEquipment } from './types';
import { getUserEquipment, addUserEquipment, removeUserEquipment } from './userEquipment';

const mockAdapter = new AxiosMockAdapter(ENDPOINT);

const mockUserEquipment: UserEquipment[] = [
  {
    user_id: 'test-user-id',
    equipment_name: 'Barbell',
  },
  {
    user_id: 'test-user-id',
    equipment_name: 'Dumbbells',
  },
];

const mockSingleUserEquipment: UserEquipment = {
  user_id: 'test-user-id',
  equipment_name: 'Kettlebell',
};

describe('userEquipment API', () => {
  beforeEach(() => {
    mockAdapter.reset();
  });

  describe('getUserEquipment', () => {
    it('should get user equipment successfully', async () => {
      mockAdapter.onGet('/user_equipment/test-user-id').reply(200, mockUserEquipment);

      const result = await getUserEquipment('test-user-id');

      expect(result).toEqual(mockUserEquipment);
      expect(mockAdapter.history.get[0].url).toBe('/user_equipment/test-user-id');
    });

    it('should handle 404 error', async () => {
      const errorResponse = { error: 'User not found' };
      mockAdapter.onGet('/user_equipment/test-user-id').reply(404, errorResponse);

      await expectRequestError(getUserEquipment('test-user-id'), errorResponse);
    });

    it('should handle 401 error', async () => {
      const errorResponse = { error: 'Unauthorized' };
      mockAdapter.onGet('/user_equipment/test-user-id').reply(401, errorResponse);

      await expectRequestError(getUserEquipment('test-user-id'), errorResponse);
    });

    it('should handle 500 error', async () => {
      const errorResponse = { error: 'Internal server error' };
      mockAdapter.onGet('/user_equipment/test-user-id').reply(500, errorResponse);

      await expectRequestError(getUserEquipment('test-user-id'), errorResponse);
    });

    it('should handle network error', async () => {
      mockAdapter.onGet('/user_equipment/test-user-id').networkError();

      await expect(getUserEquipment('test-user-id')).rejects.toThrow('Network Error');
    }, 10000);

    it('should handle timeout error', async () => {
      mockAdapter.onGet('/user_equipment/test-user-id').timeout();

      await expect(getUserEquipment('test-user-id')).rejects.toThrow('timeout of 10000ms exceeded');
    });

    it('should encode user ID in URL', async () => {
      mockAdapter.onGet('/user_equipment/user%20with%20spaces').reply(200, mockUserEquipment);

      await getUserEquipment('user with spaces');

      expect(mockAdapter.history.get[0].url).toBe('/user_equipment/user%20with%20spaces');
    });
  });

  describe('addUserEquipment', () => {
    it('should add user equipment successfully', async () => {
      mockAdapter.onPost('/user_equipment/').reply(200, mockSingleUserEquipment);

      const result = await addUserEquipment('test-user-id', 'Kettlebell');

      expect(result).toEqual(mockSingleUserEquipment);
      expect(mockAdapter.history.post[0].url).toBe('/user_equipment/');
      expect(mockAdapter.history.post[0].params).toEqual({
        user_id: 'test-user-id',
        equipment_name: 'Kettlebell',
      });
    });

    it('should handle 400 error', async () => {
      const errorResponse = { error: 'Bad request' };
      mockAdapter.onPost('/user_equipment/').reply(400, errorResponse);

      await expectRequestError(addUserEquipment('test-user-id', 'Kettlebell'), errorResponse);
    });

    it('should handle 422 error', async () => {
      const errorResponse = { error: 'Validation error' };
      mockAdapter.onPost('/user_equipment/').reply(422, errorResponse);

      await expectRequestError(addUserEquipment('test-user-id', 'Kettlebell'), errorResponse);
    });

    it('should handle 409 error', async () => {
      const errorResponse = { error: 'Conflict' };
      mockAdapter.onPost('/user_equipment/').reply(409, errorResponse);

      await expectRequestError(addUserEquipment('test-user-id', 'Kettlebell'), errorResponse);
    });

    it('should handle 500 error', async () => {
      const errorResponse = { error: 'Internal server error' };
      mockAdapter.onPost('/user_equipment/').reply(500, errorResponse);

      await expectRequestError(addUserEquipment('test-user-id', 'Kettlebell'), errorResponse);
    });

    it('should handle network error', async () => {
      mockAdapter.onPost('/user_equipment/').networkError();

      await expect(addUserEquipment('test-user-id', 'Kettlebell')).rejects.toThrow('Network Error');
    }, 10000);

    it('should handle timeout error', async () => {
      mockAdapter.onPost('/user_equipment/').timeout();

      await expect(addUserEquipment('test-user-id', 'Kettlebell')).rejects.toThrow(
        'timeout of 10000ms exceeded'
      );
    });
  });

  describe('removeUserEquipment', () => {
    it('should remove user equipment successfully', async () => {
      mockAdapter.onDelete('/user_equipment/').reply(200);

      await removeUserEquipment('test-user-id', 'Kettlebell');

      expect(mockAdapter.history.delete[0].url).toBe('/user_equipment/');
      expect(mockAdapter.history.delete[0].params).toEqual({
        user_id: 'test-user-id',
        equipment_name: 'Kettlebell',
      });
    });

    it('should handle 404 error', async () => {
      const errorResponse = { error: 'User equipment not found' };
      mockAdapter.onDelete('/user_equipment/').reply(404, errorResponse);

      await expectRequestError(removeUserEquipment('test-user-id', 'Kettlebell'), errorResponse);
    });

    it('should handle 401 error', async () => {
      const errorResponse = { error: 'Unauthorized' };
      mockAdapter.onDelete('/user_equipment/').reply(401, errorResponse);

      await expectRequestError(removeUserEquipment('test-user-id', 'Kettlebell'), errorResponse);
    });

    it('should handle 500 error', async () => {
      const errorResponse = { error: 'Internal server error' };
      mockAdapter.onDelete('/user_equipment/').reply(500, errorResponse);

      await expectRequestError(removeUserEquipment('test-user-id', 'Kettlebell'), errorResponse);
    });

    it('should handle network error', async () => {
      mockAdapter.onDelete('/user_equipment/').networkError();

      await expect(removeUserEquipment('test-user-id', 'Kettlebell')).rejects.toThrow(
        'Network Error'
      );
    }, 10000);

    it('should handle timeout error', async () => {
      mockAdapter.onDelete('/user_equipment/').timeout();

      await expect(removeUserEquipment('test-user-id', 'Kettlebell')).rejects.toThrow(
        'timeout of 10000ms exceeded'
      );
    });
  });
});
