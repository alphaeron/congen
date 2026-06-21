import { Edit } from '@mui/icons-material';
import {
  Box,
  IconButton,
  Tooltip,
  Popover,
  Button,
  Divider,
  Alert,
  useMediaQuery,
  useTheme,
} from '@mui/material';
import { useForm } from '@tanstack/react-form';
import { useSnackbar } from 'notistack';
import React, { useState } from 'react';

import { GameText } from './GameTheme';
import {
  SetSchemeForm,
  buildSetSchemeFormDefaultsFromExercise,
  resolvePerformedForSetIndex,
} from './SetSchemeForm';
import { deleteProgrammedExercise } from '../api/programmedExercise';
import { createSetScheme, deleteSetScheme, updateSetScheme } from '../api/setScheme';
import type { ProgrammedExerciseWithSetSchemes, UserWeightUnitPreference } from '../api/types';
import {
  convertDisplayWeightToKg,
  formatBandWeightWithUnit,
} from '../common/utils';

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
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('sm'));
  const [anchorEl, setAnchorEl] = useState<HTMLElement | null>(null);
  const [saving, setSaving] = useState(false);

  const open = Boolean(anchorEl);

  const handleClick = (event: React.MouseEvent<HTMLElement>) => {
    if (!isMostRecentWeek) {
      enqueueSnackbar('Editing is only available for the current week', { variant: 'warning' });
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

      onExerciseUpdate(null as unknown as ProgrammedExerciseWithSetSchemes);
    } catch {
      enqueueSnackbar('Failed to delete exercise', { variant: 'error' });
    } finally {
      setSaving(false);
    }
  };

  const firstSetScheme = exercise.set_schemes[0];

  const weightUnitPreference = weightUnitPreferences.find(
    pref => pref.exercise_name === exercise.exercise.exercise_name
  );
  const preferredUnit = weightUnitPreference?.preferred_unit;
  const unit = (preferredUnit as 'KG' | 'LBS' | undefined) ?? undefined;

  const convertWeightForStorage = (weight: number): number =>
    convertDisplayWeightToKg(weight, unit);

  const form = useForm({
    defaultValues: buildSetSchemeFormDefaultsFromExercise(exercise, unit),
    onSubmit: async ({ value }) => {
      try {
        setSaving(true);

        const currentCount = exercise.set_schemes.length;
        const newTotal = Math.max(1, Math.floor(Number(value.totalSets) || 1));
        const sortedSchemes = [...exercise.set_schemes].sort((a, b) => a.set_number - b.set_number);
        const programmedExerciseId = exercise.exercise.id;
        const targetWeightKg = convertWeightForStorage(value.targetWeight);

        const schemesToUpdate = sortedSchemes.slice(0, newTotal);
        const updatePromises = schemesToUpdate.map(setScheme => {
          const setIndex = setScheme.set_number - 1;
          const performed = resolvePerformedForSetIndex(setIndex, value);
          const performedWeightKg =
            performed.weight != null ? convertWeightForStorage(performed.weight) : undefined;
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
            performed.reps,
            value.restSeconds,
            'KG'
          );
        });

        const createPromises: Promise<(typeof exercise.set_schemes)[0]>[] = [];
        if (newTotal > currentCount) {
          for (let setNumber = currentCount + 1; setNumber <= newTotal; setNumber++) {
            const setIndex = setNumber - 1;
            const performed = resolvePerformedForSetIndex(setIndex, value);
            const performedWeightKg =
              performed.weight != null ? convertWeightForStorage(performed.weight) : undefined;
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
                performed.reps,
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
        title={isMostRecentWeek ? 'Edit exercise' : 'Editing only available for the current week'}
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
              aria-label="Editing only available for the current week"
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
          sx: {
            width: isMobile ? 'calc(100vw - 16px)' : 600,
            maxWidth: '100vw',
            maxHeight: isMobile ? 'calc(100vh - 16px)' : 700,
            overflow: 'auto',
          },
        }}
      >
        <Box sx={{ p: 2 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}>
            <GameText variant="subtitle1">{exercise.exercise.exercise_name}</GameText>
          </Box>

          <Alert severity="info" sx={{ mb: 2 }}>
            Enter values for all sets, or customize per set for individual performance. Target values
            apply to every set.
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
                bandWeightDisplay={
                  firstSetScheme
                    ? formatBandWeightWithUnit(firstSetScheme.band_weight_lbs) || undefined
                    : undefined
                }
              />
            </Box>

            <Divider sx={{ my: 2 }} />

            <Box
              sx={{
                display: 'flex',
                flexDirection: { xs: 'column', sm: 'row' },
                gap: 1,
                justifyContent: 'space-between',
                alignItems: { xs: 'stretch', sm: 'center' },
              }}
            >
              <Button onClick={handleDelete} disabled={saving} color="error" variant="contained">
                Delete Exercise
              </Button>

              <Box sx={{ display: 'flex', gap: 1, justifyContent: { xs: 'stretch', sm: 'flex-end' } }}>
                <Button onClick={handleClose} disabled={saving} sx={{ flex: { xs: 1, sm: 'none' } }}>
                  Cancel
                </Button>
                <Button
                  type="submit"
                  variant="contained"
                  disabled={saving}
                  sx={{ flex: { xs: 1, sm: 'none' } }}
                >
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
