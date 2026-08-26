import { renderHook } from '@testing-library/react';

import { useActiveProgramContext } from './useActiveProgramContext';

const mockUseData = jest.fn();

jest.mock('../contexts/DataContext', () => ({
  useData: () => mockUseData(),
}));

describe('useActiveProgramContext', () => {
  beforeEach(() => {
    mockUseData.mockReturnValue({
      userData: {
        training_programs: [
          {
            program: { id: 1, is_active: true },
            workouts: [],
          },
        ],
      },
      programPreferences: [
        {
          program: { id: 1, is_active: true },
          program_preferences: { program_days_per_week: 4 },
        },
      ],
      weightUnitPreferences: [{ preferred_unit: 'KG' }],
    });
  });

  it('returns active program context values', () => {
    const { result } = renderHook(() => useActiveProgramContext());

    expect(result.current.workoutsPerWeek).toBe(4);
    expect(result.current.preferredUnit).toBe('KG');
    expect(result.current.activeProgramPreferences?.program.id).toBe(1);
    expect(result.current.activeProgramData?.program.is_active).toBe(true);
  });

  it('falls back to defaults when preferences are missing', () => {
    mockUseData.mockReturnValue({
      userData: null,
      programPreferences: [],
      weightUnitPreferences: [],
    });

    const { result } = renderHook(() => useActiveProgramContext());

    expect(result.current.workoutsPerWeek).toBe(3);
    expect(result.current.preferredUnit).toBe('LBS');
    expect(result.current.activeProgramPreferences).toBeNull();
    expect(result.current.activeProgramData).toBeNull();
  });
});
