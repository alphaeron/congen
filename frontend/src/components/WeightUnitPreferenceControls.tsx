import { ToggleButton, ToggleButtonGroup } from '@mui/material';
import { useSnackbar } from 'notistack';
import React, { useMemo, useState } from 'react';

import { GameText } from './GameTheme';
import { LoadingSpinner } from './LoadingSpinner';
import { WeightUnit, type UserWeightUnitPreference } from '../api/types';
import { upsertUserWeightUnitPreference } from '../api/userWeightUnitPreference';
import { useAuth } from '../contexts/AuthContext';
import { useData } from '../contexts/DataContext';

/**
 * Props for the WeightUnitPreferenceControls component.
 */
interface WeightUnitPreferenceControlsProps {
  exerciseName: string;
  size?: 'small' | 'medium';
  onPreferenceChange?: (preference: UserWeightUnitPreference) => void;
} // end interface WeightUnitPreferenceControlsProps

/**
 * Inline KG/LBS toggle for an exercise weight unit preference.
 *
 * Reads the current preference from DataContext when present, otherwise defaults
 * to KG to match frontend/backend fallback behavior. Persists changes through the
 * weight unit preference API and refreshes shared DataContext state.
 *
 * @param props The props for the component
 * @return The weight unit preference controls component
 */
export function WeightUnitPreferenceControls(
  props: WeightUnitPreferenceControlsProps
): React.ReactElement<WeightUnitPreferenceControlsProps> {
  const { exerciseName, size = 'small', onPreferenceChange } = props;
  const { user } = useAuth();
  const { enqueueSnackbar } = useSnackbar();
  const { weightUnitPreferences, refreshSpecificData } = useData();
  const [saving, setSaving] = useState(false);

  const preference = useMemo<UserWeightUnitPreference | null>(
    () => weightUnitPreferences.find(p => p.exercise_name === exerciseName) ?? null,
    [weightUnitPreferences, exerciseName]
  );

  const currentUnit = useMemo((): WeightUnit => {
    if (preference?.preferred_unit === WeightUnit.LBS) {
      return WeightUnit.LBS;
    }
    if (preference?.preferred_unit === WeightUnit.KG) {
      return WeightUnit.KG;
    }
    return WeightUnit.KG;
  }, [preference]);

  const handleUnitChange = async (_: React.MouseEvent<HTMLElement>, newUnit: WeightUnit | null) => {
    if (!user?.keycloak_id || newUnit === null || newUnit === currentUnit) {
      return;
    }

    setSaving(true);
    try {
      const updatedPreference = await upsertUserWeightUnitPreference(
        user.keycloak_id,
        exerciseName,
        newUnit
      );
      onPreferenceChange?.(updatedPreference);
      await refreshSpecificData('weightUnitPreferences');
      enqueueSnackbar(`Weight unit set to ${newUnit}`, { variant: 'success' });
    } catch {
      enqueueSnackbar('Failed to update weight unit preference', { variant: 'error' });
    } finally {
      setSaving(false);
    }
  };

  if (saving) {
    return <LoadingSpinner size={20} />;
  }

  return (
    <ToggleButtonGroup
      value={currentUnit}
      exclusive
      onChange={handleUnitChange}
      size={size}
      disabled={saving}
      aria-label={`Weight unit for ${exerciseName}`}
      sx={{
        '& .MuiToggleButton-root': {
          border: '1px solid',
          borderColor: 'divider',
          '&.Mui-selected': {
            backgroundColor: 'primary.main',
            color: 'primary.contrastText',
            '&:hover': {
              backgroundColor: 'primary.dark',
            },
          },
          '&:hover': {
            backgroundColor: 'action.hover',
          },
        },
      }}
    >
      <ToggleButton value={WeightUnit.KG} aria-label={`Set ${exerciseName} to kilograms`}>
        <GameText variant="caption">KG</GameText>
      </ToggleButton>
      <ToggleButton value={WeightUnit.LBS} aria-label={`Set ${exerciseName} to pounds`}>
        <GameText variant="caption">LBS</GameText>
      </ToggleButton>
    </ToggleButtonGroup>
  );
} // end component WeightUnitPreferenceControls
