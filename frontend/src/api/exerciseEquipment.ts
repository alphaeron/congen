import { REQUEST } from "./endpoint";
import { ExerciseEquipment } from "./types";

/**
 * Get a list of all available exercises and the equipment they use.
 *
 * @return A list of all available exercises and the equipment they use.
 */
export const getExerciseEquipment = (): Promise<ExerciseEquipment[]> =>
  REQUEST({
    url: "/exercise_equipment/",
    method: "GET",
  });
