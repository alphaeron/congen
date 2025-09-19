import { REQUEST } from './endpoint';
import type { ProgrammedExercise } from './types';

/**
 * Get all programmed exercises for a specific workout stage.
 *
 * @param workoutStageId The ID of the workout stage
 * @returns Promise containing a list of programmed exercises
 */
export const getProgrammedExercisesByStage = (
  workoutStageId: number
): Promise<ProgrammedExercise[]> => {
  return REQUEST({
    method: 'GET',
    url: `/programmed_exercise/stage/${workoutStageId}`,
  });
};

/**
 * Get a specific programmed exercise by ID.
 *
 * @param id The programmed exercise ID
 * @returns Promise containing the programmed exercise details
 */
export const getProgrammedExercise = (id: number): Promise<ProgrammedExercise> => {
  return REQUEST({
    method: 'GET',
    url: `/programmed_exercise/${id}`,
  });
};

/**
 * Update a programmed exercise.
 *
 * @param id The programmed exercise ID
 * @param workoutStageId The workout stage ID
 * @param exerciseName The exercise name
 * @param position The position within the stage
 * @param notes Optional notes for the exercise
 * @returns Promise containing the updated programmed exercise
 */
export const updateProgrammedExercise = (
  id: number,
  workoutStageId: number,
  exerciseName: string,
  position: number,
  notes?: string
): Promise<ProgrammedExercise> => {
  const params = new URLSearchParams({
    workout_stage_id: workoutStageId.toString(),
    exercise_name: exerciseName,
    position: position.toString(),
  });
  
  // Always include notes parameter, even if empty string
  // The backend expects this parameter to be present
  params.append('notes', notes || '');

  return REQUEST({
    method: 'PATCH',
    url: `/programmed_exercise/${id}?${params.toString()}`,
  });
};

/**
 * Delete a programmed exercise.
 *
 * @param id The programmed exercise ID
 * @returns Promise containing the deletion result
 */
export const deleteProgrammedExercise = (id: number): Promise<void> => {
  return REQUEST({
    method: 'DELETE',
    url: `/programmed_exercise/${id}`,
  });
};

/**
 * Create a new programmed exercise.
 *
 * @param workoutStageId The workout stage ID
 * @param exerciseName The exercise name
 * @param position The position within the stage
 * @param notes Optional notes for the exercise
 * @param totalSets Total number of sets
 * @param targetWeight Target weight for the exercise
 * @param targetReps Target reps for the exercise
 * @param restSeconds Rest period in seconds
 * @param performedWeight Optional performed weight
 * @param performedReps Optional performed reps
 * @param tempo Optional tempo string
 * @param isAmrap Whether this is an AMRAP set
 * @param isEmom Whether this is an EMOM set
 * @returns Promise containing the created programmed exercise
 */
export const createProgrammedExercise = async (
  workoutStageId: number,
  exerciseName: string,
  position: number,
  notes?: string,
  totalSets?: number,
  targetWeight?: number,
  targetReps?: number,
  restSeconds?: number,
  performedWeight?: number,
  performedReps?: number,
  tempo?: string,
  isAmrap?: boolean,
  isEmom?: boolean
): Promise<ProgrammedExercise> => {
  // First, create the programmed exercise with basic info only
  const exerciseParams = new URLSearchParams({
    workout_stage_id: workoutStageId.toString(),
    exercise_name: exerciseName,
    position: position.toString(),
  });
  
  // Always include notes parameter, even if empty string
  exerciseParams.append('notes', notes || '');

  const createdExercise = await REQUEST({
    method: 'POST',
    url: `/programmed_exercise/?${exerciseParams.toString()}`,
  }) as ProgrammedExercise;

  // If set scheme parameters are provided, create set schemes
  if (totalSets && totalSets > 0) {
    const setSchemePromises = [];
    
    for (let setNumber = 1; setNumber <= totalSets; setNumber++) {
      const setSchemeParams = new URLSearchParams({
        programmed_exercise_id: createdExercise.id.toString(),
        set_number: setNumber.toString(),
        is_amrap: (isAmrap || false).toString(),
        is_emom: (isEmom || false).toString(),
        use_tempo: (tempo !== undefined && tempo !== '').toString(),
      });

      // Add tempo parameters if tempo is provided
      if (tempo && tempo !== '') {
        const tempoParts = tempo.split('-');
        if (tempoParts.length === 3) {
          setSchemeParams.append('eccentric_tempo', tempoParts[0] || '');
          setSchemeParams.append('isometric_tempo', tempoParts[1] || '');
          setSchemeParams.append('concentric_tempo', tempoParts[2] || '');
        }
      }
      // Don't append tempo parameters if tempo is not provided - let backend use defaults

      // Add weight and rep parameters with defaults
      setSchemeParams.append('target_weight', (targetWeight || 1).toString());
      if (performedWeight !== undefined) {
        setSchemeParams.append('performed_weight', performedWeight.toString());
      }
      setSchemeParams.append('target_rep_count', (targetReps || 1).toString());
      if (performedReps !== undefined) {
        setSchemeParams.append('performed_rep_count', performedReps.toString());
      }
      setSchemeParams.append('rest_seconds', (restSeconds || 60).toString());

      setSchemePromises.push(
        REQUEST({
          method: 'POST',
          url: `/set_scheme/?${setSchemeParams.toString()}`,
        })
      );
    }

    // Wait for all set schemes to be created
    await Promise.all(setSchemePromises);
  }

  return createdExercise;
};
