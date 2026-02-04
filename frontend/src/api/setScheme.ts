import { REQUEST } from './endpoint';
import type { SetScheme } from './types';

/**
 * Get all set schemes for a specific programmed exercise.
 *
 * @param programmedExerciseId The ID of the programmed exercise
 * @returns Promise containing a list of set schemes
 */
export const getSetSchemesByExercise = (programmedExerciseId: number): Promise<SetScheme[]> => {
  return REQUEST({
    method: 'GET',
    url: `/set_scheme/exercise/${programmedExerciseId}`,
  });
};

/**
 * Get a specific set scheme by ID.
 *
 * @param id The set scheme ID
 * @returns Promise containing the set scheme details
 */
export const getSetScheme = (id: number): Promise<SetScheme> => {
  return REQUEST({
    method: 'GET',
    url: `/set_scheme/${id}`,
  });
};

/**
 * Update a set scheme.
 *
 * @param id The set scheme ID
 * @param programmedExerciseId The programmed exercise ID
 * @param setNumber The set number
 * @param isAmrap Whether this is an AMRAP set
 * @param isEmom Whether this is an EMOM set
 * @param useTempo Whether to use tempo timing
 * @param eccentricTempo Eccentric tempo
 * @param isometricTempo Isometric tempo
 * @param concentricTempo Concentric tempo
 * @param targetWeight Target weight
 * @param performedWeight Performed weight
 * @param targetRepCount Target rep count
 * @param performedRepCount Performed rep count
 * @param restSeconds Rest period in seconds
 * @param unit Weight unit (KG or LBS)
 * @returns Promise containing the updated set scheme
 */
export const updateSetScheme = (
  id: number,
  programmedExerciseId: number,
  setNumber: number,
  isAmrap: boolean,
  isEmom: boolean,
  useTempo: boolean,
  eccentricTempo?: string,
  isometricTempo?: string,
  concentricTempo?: string,
  targetWeight?: number,
  performedWeight?: number,
  targetRepCount?: number,
  performedRepCount?: number,
  restSeconds?: number,
  unit: string = 'KG'
): Promise<SetScheme> => {
  const params = new URLSearchParams({
    programmed_exercise_id: programmedExerciseId.toString(),
    set_number: setNumber.toString(),
    is_amrap: isAmrap.toString(),
    is_emom: isEmom.toString(),
    use_tempo: useTempo.toString(),
    unit,
  });

  if (eccentricTempo !== undefined) params.append('eccentric_tempo', eccentricTempo);
  if (isometricTempo !== undefined) params.append('isometric_tempo', isometricTempo);
  if (concentricTempo !== undefined) params.append('concentric_tempo', concentricTempo);
  if (targetWeight !== undefined) params.append('target_weight', targetWeight.toString());
  if (performedWeight !== undefined) params.append('performed_weight', performedWeight.toString());
  if (targetRepCount !== undefined) params.append('target_rep_count', targetRepCount.toString());
  if (performedRepCount !== undefined)
    params.append('performed_rep_count', performedRepCount.toString());
  if (restSeconds !== undefined) params.append('rest_seconds', restSeconds.toString());

  return REQUEST({
    method: 'PATCH',
    url: `/set_scheme/${id}?${params.toString()}`,
  });
};

/**
 * Create a new set scheme for a programmed exercise.
 *
 * @param programmedExerciseId The ID of the programmed exercise
 * @param setNumber The set number within the exercise
 * @param isAmrap Whether this is an AMRAP set
 * @param isEmom Whether this is an EMOM set
 * @param useTempo Whether to use tempo timing
 * @param eccentricTempo Eccentric tempo
 * @param isometricTempo Isometric tempo
 * @param concentricTempo Concentric tempo
 * @param targetWeight Target weight in kg
 * @param performedWeight Performed weight in kg
 * @param targetRepCount Target rep count
 * @param performedRepCount Performed rep count
 * @param restSeconds Rest period in seconds
 * @param unit Weight unit (KG or LBS)
 * @returns Promise containing the created set scheme
 */
export const createSetScheme = (
  programmedExerciseId: number,
  setNumber: number,
  isAmrap: boolean,
  isEmom: boolean,
  useTempo: boolean,
  eccentricTempo?: string,
  isometricTempo?: string,
  concentricTempo?: string,
  targetWeight?: number,
  performedWeight?: number,
  targetRepCount?: number,
  performedRepCount?: number,
  restSeconds?: number,
  unit: string = 'KG'
): Promise<SetScheme> => {
  const params = new URLSearchParams({
    programmed_exercise_id: programmedExerciseId.toString(),
    set_number: setNumber.toString(),
    is_amrap: isAmrap.toString(),
    is_emom: isEmom.toString(),
    use_tempo: useTempo.toString(),
    unit,
  });
  if (eccentricTempo !== undefined) params.append('eccentric_tempo', eccentricTempo);
  if (isometricTempo !== undefined) params.append('isometric_tempo', isometricTempo);
  if (concentricTempo !== undefined) params.append('concentric_tempo', concentricTempo);
  params.append('target_weight', (targetWeight ?? 1).toString());
  if (performedWeight !== undefined) params.append('performed_weight', performedWeight.toString());
  params.append('target_rep_count', (targetRepCount ?? 1).toString());
  if (performedRepCount !== undefined)
    params.append('performed_rep_count', performedRepCount.toString());
  params.append('rest_seconds', (restSeconds ?? 60).toString());

  return REQUEST({
    method: 'POST',
    url: `/set_scheme/?${params.toString()}`,
  });
};

/**
 * Delete a set scheme by ID.
 *
 * @param id The set scheme ID
 * @returns Promise containing the deleted set scheme
 */
export const deleteSetScheme = (id: number): Promise<SetScheme> => {
  return REQUEST({
    method: 'DELETE',
    url: `/set_scheme/${id}`,
  });
};
