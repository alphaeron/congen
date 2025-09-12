import { REQUEST } from './endpoint';
import type { UserEquipment } from './types';

/**
 * Get all equipment for a specific user.
 *
 * @param userId The Keycloak identifier of the user
 * @returns Promise containing the user's equipment list
 */
export const getUserEquipment = async (userId: string): Promise<UserEquipment[]> => {
  return REQUEST({
    method: 'GET',
    url: `/user_equipment/${encodeURIComponent(userId)}`,
  });
};

/**
 * Add equipment for a user.
 *
 * @param userId The Keycloak identifier of the user
 * @param equipmentName The name of the equipment to add
 * @returns Promise containing the created user equipment
 */
export const addUserEquipment = async (userId: string, equipmentName: string): Promise<UserEquipment> => {
  return REQUEST({
    method: 'POST',
    url: '/user_equipment/',
    params: {
      user_id: userId,
      equipment_name: equipmentName,
    },
  });
};

/**
 * Remove equipment for a user.
 *
 * @param userId The Keycloak identifier of the user
 * @param equipmentName The name of the equipment to remove
 * @returns Promise containing the deletion result
 */
export const removeUserEquipment = async (userId: string, equipmentName: string): Promise<void> => {
  return REQUEST({
    method: 'DELETE',
    url: '/user_equipment/',
    params: {
      user_id: userId,
      equipment_name: equipmentName,
    },
  });
};
