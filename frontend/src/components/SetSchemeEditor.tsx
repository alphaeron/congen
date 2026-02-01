import { Edit } from '@mui/icons-material';
import { Box, IconButton, Tooltip, Popover, Button, Divider, Alert } from '@mui/material';
import { useForm } from '@tanstack/react-form';
import { useSnackbar } from 'notistack';
import React, { useState } from 'react';

import { GameText } from './GameTheme';
import { SetSchemeForm } from './SetSchemeForm';
import { deleteProgrammedExercise } from '../api/programmedExercise';
import { updateSetScheme } from '../api/setScheme';
import type { ProgrammedExerciseWithSetSchemes, UserWeightUnitPreference } from '../api/types';
import { formatWeightWithUnit, convertDisplayWeightToKg } from '../common/utils';

interface SetSchemeEditorProps {
  exercise: ProgrammedExerciseWithSetSchemes;
  onExerciseUpdate: (updatedExercise: ProgrammedExerciseWithSetSchemes) => void;
  isMostRecentWeek?: boolean;
  weightUnitPreferences?: UserWeightUnitPreference[];
}

export const SetSchemeEditor: React.FC<SetSchemeEditorProps> = ({
  exercise,
  onExerciseUpdate,
  isMostRecentWeek = false,
  weightUnitPreferences = [],
}) => {
  const { enqueueSnackbar } = useSnackbar();
  const [anchorEl, setAnchorEl] = useState<HTMLElement | null>(null);
  const [saving, setSaving] = useState(false);

  const open = Boolean(anchorEl);

  const handleClick = (event: React.MouseEvent<HTMLElement>) => {
    if (!isMostRecentWeek) {
      enqueueSnackbar('Editing is only available for the most recent week', { variant: 'warning' });
      return;
    }
    setAnchorEl(event.currentTarget);
  };

  const handleClose = () => {
    setAnchorEl(null);
  };

  const handleDelete = async () => {
    if (
      !window.confirm(
        `Are you sure you want to delete "${exercise.exercise.exercise_name}"? This action cannot be undone.`
      )
    ) {
      return;
    }

    try {
      setSaving(true);
      await deleteProgrammedExercise(exercise.exercise.id);

      enqueueSnackbar('Exercise deleted successfully', { variant: 'success' });
      handleClose();

      // Call the update callback to refresh the parent component
      onExerciseUpdate(null as unknown as ProgrammedExerciseWithSetSchemes); // This will trigger a refresh
    } catch {
      enqueueSnackbar('Failed to delete exercise', { variant: 'error' });
    } finally {
      setSaving(false);
    }
  };

  // Get the first set scheme for default values (since all sets should have the same reps/weight)
  const firstSetScheme = exercise.set_schemes[0];

  // Get user's weight unit preference for this exercise
  const weightUnitPreference = weightUnitPreferences.find(
    pref => pref.exercise_name === exercise.exercise.exercise_name
  );
  const preferredUnit = weightUnitPreference?.preferred_unit;

  const convertWeightForStorage = (weight: number): number =>
    convertDisplayWeightToKg(weight, preferredUnit);

  const form = useForm({
    defaultValues: {
      totalSets: exercise.set_schemes.length,
      targetWeight: firstSetScheme?.target_weight
        ? parseFloat(
            formatWeightWithUnit(firstSetScheme.target_weight, preferredUnit, false)
          ) || 0
        : 0,
      performedWeight: firstSetScheme?.performed_weight
        ? parseFloat(
            formatWeightWithUnit(firstSetScheme.performed_weight, preferredUnit, false)
          ) || undefined
        : undefined,
      targetReps: firstSetScheme?.target_rep_count || 0,
      performedReps: firstSetScheme?.performed_rep_count || undefined,
      restSeconds: firstSetScheme?.rest_seconds || 0,
      useTempo: firstSetScheme?.use_tempo || false,
      eccentricTempo: firstSetScheme?.eccentric_tempo || '',
      isometricTempo: firstSetScheme?.isometric_tempo || '',
      concentricTempo: firstSetScheme?.concentric_tempo || '',
      isAmrap: firstSetScheme?.is_amrap || false,
      isEmom: firstSetScheme?.is_emom || false,
    },
    onSubmit: async ({ value }) => {
      try {
        setSaving(true);

        // Update all set schemes with the same values
        const updatePromises = exercise.set_schemes.map(setScheme =>
          updateSetScheme(
            setScheme.id,
            setScheme.programmed_exercise_id,
            setScheme.set_number,
            value.isAmrap,
            value.isEmom,
            value.useTempo,
            value.eccentricTempo || undefined,
            value.isometricTempo || undefined,
            value.concentricTempo || undefined,
            convertWeightForStorage(value.targetWeight),
            value.performedWeight ? convertWeightForStorage(value.performedWeight) : undefined,
            value.targetReps,
            value.performedReps,
            value.restSeconds
          )
        );

        await Promise.all(updatePromises);

        // Update the local state
        const updatedExercise = {
          ...exercise,
          exercise: {
            ...exercise.exercise,
          },
          set_schemes: exercise.set_schemes.map(setScheme => ({
            ...setScheme,
            target_weight: value.targetWeight,
            performed_weight: value.performedWeight,
            target_rep_count: value.targetReps,
            performed_rep_count: value.performedReps,
            rest_seconds: value.restSeconds,
            use_tempo: value.useTempo,
            eccentric_tempo: value.eccentricTempo,
            isometric_tempo: value.isometricTempo,
            concentric_tempo: value.concentricTempo,
            is_amrap: value.isAmrap,
            is_emom: value.isEmom,
          })),
        };

        onExerciseUpdate(updatedExercise);
        setAnchorEl(null);
        enqueueSnackbar('Exercise updated successfully', { variant: 'success' });
      } catch {
        enqueueSnackbar('Failed to update exercise', { variant: 'error' });
      } finally {
        setSaving(false);
      }
    },
  });

  return (
    <React.Fragment>
      <Tooltip
        title={isMostRecentWeek ? 'Edit exercise' : 'Editing only available for most recent week'}
      >
        {isMostRecentWeek ? (
          <IconButton size="small" onClick={handleClick} color="primary" aria-label="Edit exercise">
            <Edit fontSize="small" />
          </IconButton>
        ) : (
          <span>
            <IconButton
              size="small"
              onClick={handleClick}
              color="default"
              disabled={true}
              aria-label="Editing only available for most recent week"
            >
              <Edit fontSize="small" />
            </IconButton>
          </span>
        )}
      </Tooltip>

      <Popover
        open={open}
        anchorEl={anchorEl}
        onClose={handleClose}
        anchorOrigin={{
          vertical: 'bottom',
          horizontal: 'left',
        }}
        transformOrigin={{
          vertical: 'top',
          horizontal: 'left',
        }}
        PaperProps={{
          sx: { width: 600, maxHeight: 700 },
        }}
      >
        <Box sx={{ p: 2 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}>
            <GameText variant="subtitle1">{exercise.exercise.exercise_name}</GameText>
          </Box>

          <Alert severity="info" sx={{ mb: 2 }}>
            Edit set scheme details. All sets will use the same target values.
          </Alert>

          <form
            onSubmit={e => {
              e.preventDefault();
              e.stopPropagation();
              form.handleSubmit();
            }}
          >
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
              <SetSchemeForm
                form={form}
                saving={saving}
                exerciseName={exercise.exercise.exercise_name}
                weightUnitPreferences={weightUnitPreferences}
                showPerformedFields={true}
                showTempoFields={true}
                showSetTypeFields={true}
              />
            </Box>

            <Divider sx={{ my: 2 }} />

            <Box
              sx={{
                display: 'flex',
                gap: 1,
                justifyContent: 'space-between',
                alignItems: 'center',
              }}
            >
              <Button onClick={handleDelete} disabled={saving} color="error" variant="contained">
                Delete Exercise
              </Button>

              <Box sx={{ display: 'flex', gap: 1 }}>
                <Button onClick={handleClose} disabled={saving}>
                  Cancel
                </Button>
                <Button type="submit" variant="contained" disabled={saving}>
                  Submit
                </Button>
              </Box>
            </Box>
          </form>
        </Box>
      </Popover>
    </React.Fragment>
  );
};
