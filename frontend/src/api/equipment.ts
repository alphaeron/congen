import { REQUEST } from "./endpoint";
import { Equipment } from "./types";

/**
 * Get a list of all available equipment.
 *
 * @return A list of all available equipment.
 */
export const getEquipment = (): Promise<Equipment[]> =>
  REQUEST({
    url: "/equipment/",
    method: "GET",
  });

/**
 * Get an individual equipment.
 *
 * @param equipmentName The name of the equipment to get.
 *
 * @return The equipment obtained.
 */
export const getIndividualEquipment = (
  equipmentName: string,
): Promise<Equipment> =>
  REQUEST({
    url: `/equipment/${equipmentName}`,
    method: "GET",
  });
