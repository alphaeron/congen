import { Edit } from '@mui/icons-material';
import { Box, IconButton, Tooltip, Popover, Button, Divider, Alert } from '@mui/material';
import { useForm } from '@tanstack/react-form';
import { useSnackbar } from 'notistack';
import React, { useState } from 'react';

import { GameText } from './GameTheme';
import { SetSchemeForm } from './SetSchemeForm';
import { deleteProgrammedExercise } from '../api/programmedExercise';
import { createSetScheme, deleteSetScheme, updateSetScheme } from '../api/setScheme';
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
  const unit = (preferredUnit as 'KG' | 'LBS' | undefined) ?? undefined;

  const convertWeightForStorage = (weight: number): number =>
    convertDisplayWeightToKg(weight, unit);
  const form = useForm({
    defaultValues: {
      totalSets: exercise.set_schemes.length,
      targetWeight: firstSetScheme?.target_weight
        ? parseFloat(formatWeightWithUnit(firstSetScheme.target_weight, unit, false)) || 0
        : 0,
      performedWeight:
        firstSetScheme?.performed_weight != null
          ? (() => {
              const n = parseFloat(
                formatWeightWithUnit(firstSetScheme.performed_weight, unit, false)
              );
              return Number.isNaN(n) ? undefined : n;
            })()
          : undefined,
      targetReps: firstSetScheme?.target_rep_count || 0,
      performedReps: firstSetScheme?.performed_rep_count || undefined,
      performedRepsBySet: exercise.set_schemes
        .sort((a, b) => a.set_number - b.set_number)
        .map(s => s.performed_rep_count),
      performedWeightBySet: exercise.set_schemes
        .sort((a, b) => a.set_number - b.set_number)
        .map(s => {
          if (s.performed_weight == null) return undefined;
          const n = parseFloat(formatWeightWithUnit(s.performed_weight, unit, false));
          return Number.isNaN(n) ? undefined : n;
        }),
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

        const currentCount = exercise.set_schemes.length;
        const newTotal = Math.max(1, Math.floor(Number(value.totalSets) || 1));
        const sortedSchemes = [...exercise.set_schemes].sort((a, b) => a.set_number - b.set_number);
        const programmedExerciseId = exercise.exercise.id;
        const performedBySet = value.performedRepsBySet ?? [];
        const performedWeightBySet = value.performedWeightBySet ?? [];
        const targetWeightKg = convertWeightForStorage(value.targetWeight);

        const schemesToUpdate = sortedSchemes.slice(0, newTotal);
        const updatePromises = schemesToUpdate.map(setScheme => {
          const setIndex = setScheme.set_number - 1;
          const weightDisplay = performedWeightBySet[setIndex];
          const performedWeightKg =
            weightDisplay != null ? convertWeightForStorage(weightDisplay) : undefined;
          return updateSetScheme(
            setScheme.id,
            setScheme.programmed_exercise_id,
            setScheme.set_number,
            value.isAmrap,
            value.isEmom,
            value.useTempo,
            value.eccentricTempo || undefined,
            value.isometricTempo || undefined,
            value.concentricTempo || undefined,
            targetWeightKg,
            performedWeightKg,
            value.targetReps,
            performedBySet[setIndex] ?? value.performedReps,
            value.restSeconds,
            'KG'
          );
        });

        const createPromises: Promise<(typeof exercise.set_schemes)[0]>[] = [];
        if (newTotal > currentCount) {
          for (let setNumber = currentCount + 1; setNumber <= newTotal; setNumber++) {
            const setIndex = setNumber - 1;
            const performedRep = performedBySet[setIndex] ?? value.performedReps;
            const weightDisplay = performedWeightBySet[setIndex];
            const performedWeightKg =
              weightDisplay != null ? convertWeightForStorage(weightDisplay) : undefined;
            createPromises.push(
              createSetScheme(
                programmedExerciseId,
                setNumber,
                value.isAmrap,
                value.isEmom,
                value.useTempo,
                value.eccentricTempo || undefined,
                value.isometricTempo || undefined,
                value.concentricTempo || undefined,
                targetWeightKg,
                performedWeightKg,
                value.targetReps,
                performedRep,
                value.restSeconds,
                'KG'
              )
            );
          }
        }

        const toDelete = newTotal < currentCount ? sortedSchemes.slice(newTotal) : [];
        const deletePromises = toDelete.map(setScheme => deleteSetScheme(setScheme.id));

        const [updated, created] = await Promise.all([
          Promise.all(updatePromises),
          Promise.all(createPromises),
          Promise.all(deletePromises),
        ]).then(([upd, cr]) => [upd, cr] as [typeof upd, typeof cr]);

        const mergedSchemes = [...updated, ...created].sort((a, b) => a.set_number - b.set_number);

        const updatedExercise = {
          ...exercise,
          exercise: { ...exercise.exercise },
          set_schemes: mergedSchemes,
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
                showPerformedRepsPerSet={true}
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
