import { useMemo } from 'react';

import type { ProgramWithPreferences, ProgramWithWorkouts } from '../api/types';
import { useData } from '../contexts/DataContext';
import { getActiveProgramFromExport } from '../utils/volumeOverviewUtils';

export interface ActiveProgramContext {
  userData: ReturnType<typeof useData>['userData'];
  activeProgramPreferences: ProgramWithPreferences | null;
  activeProgramData: ProgramWithWorkouts | null;
  workoutsPerWeek: number;
  preferredUnit: 'KG' | 'LBS';
}

/**
 * Resolves active program preferences, workouts-per-week, and display unit from DataContext.
 *
 * @returns Shared program context for workout analytics components
 */
export function useActiveProgramContext(): ActiveProgramContext {
  const { userData, programPreferences = [], weightUnitPreferences = [] } = useData();

  const activeProgramPreferences = useMemo(
    () => programPreferences.find(program => program.program.is_active) ?? null,
    [programPreferences]
  );

  const activeProgramData = useMemo(() => getActiveProgramFromExport(userData), [userData]);

  const workoutsPerWeek = useMemo(
    () => activeProgramPreferences?.program_preferences.program_days_per_week ?? 3,
    [activeProgramPreferences]
  );

  const preferredUnit = useMemo((): 'KG' | 'LBS' => {
    const preference = weightUnitPreferences.find(item => item.preferred_unit);
    return preference?.preferred_unit === 'KG' ? 'KG' : 'LBS';
  }, [weightUnitPreferences]);

  return {
    userData,
    activeProgramPreferences,
    activeProgramData,
    workoutsPerWeek,
    preferredUnit,
  };
}
