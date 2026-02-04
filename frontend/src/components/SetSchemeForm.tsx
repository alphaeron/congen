import { Box, TextField, Divider, FormControlLabel, Switch } from '@mui/material';
import React from 'react';

import { GameText } from './GameTheme';
import type { UserWeightUnitPreference } from '../api/types';

import type { useForm } from '@tanstack/react-form';

export interface SetSchemeFormData {
  totalSets: number;
  targetWeight: number;
  performedWeight?: number;
  targetReps: number;
  performedReps?: number;
  performedRepsBySet?: (number | undefined)[];
  performedWeightBySet?: (number | undefined)[];
  restSeconds: number;
  useTempo: boolean;
  eccentricTempo: string;
  isometricTempo: string;
  concentricTempo: string;
  isAmrap: boolean;
  isEmom: boolean;
}

export interface SetSchemeFormProps {
  form: ReturnType<typeof useForm<SetSchemeFormData>>;
  saving?: boolean;
  exerciseName?: string;
  weightUnitPreferences?: UserWeightUnitPreference[];
  showPerformedFields?: boolean;
  showPerformedRepsPerSet?: boolean;
  showTempoFields?: boolean;
  showSetTypeFields?: boolean;
}

export const SetSchemeForm: React.FC<SetSchemeFormProps> = ({
  form,
  saving = false,
  exerciseName,
  weightUnitPreferences = [],
  showPerformedFields = true,
  showPerformedRepsPerSet = false,
  showTempoFields = true,
  showSetTypeFields = true,
}) => {
  // Get user's weight unit preference for this exercise
  const weightUnitPreference = exerciseName
    ? weightUnitPreferences.find(pref => pref.exercise_name === exerciseName)
    : undefined;

  const preferredUnit = weightUnitPreference?.preferred_unit;
  const weightUnitLabel = preferredUnit === 'LBS' ? 'lbs' : 'kg';

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
      <GameText variant="subtitle2" gutterBottom>
        Set Scheme Details
      </GameText>

      <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
        {/* Total Sets */}
        <Box sx={{ flex: '1 1 200px', minWidth: 200 }}>
          <form.Field
            name="totalSets"
            validators={{
              onChange: ({ value }) => (value < 1 ? 'Must have at least 1 set' : undefined),
            }}
          >
            {field => (
              <TextField
                fullWidth
                size="small"
                label="Total Sets"
                type="text"
                inputProps={{ inputMode: 'decimal' }}
                value={field.state.value === 0 ? '' : field.state.value.toString()}
                onChange={e => {
                  const inputValue = e.target.value;
                  // Allow any valid numeric input including decimals
                  if (inputValue === '' || /^\d*\.?\d*$/.test(inputValue)) {
                    // Store the raw input value to preserve decimal points during typing
                    field.handleChange(inputValue);
                  }
                }}
                onBlur={e => {
                  // Convert to integer on blur for validation
                  const inputValue = e.target.value;
                  if (inputValue !== '') {
                    const numValue = parseFloat(inputValue);
                    if (!isNaN(numValue) && numValue >= 0) {
                      field.handleChange(Math.floor(numValue)); // Ensure integer for sets
                    }
                  }
                }}
                disabled={saving}
                error={!!field.state.meta.errors.length}
                helperText={field.state.meta.errors.join(', ')}
              />
            )}
          </form.Field>
        </Box>

        {/* Rest Period */}
        <Box sx={{ flex: '1 1 200px', minWidth: 200 }}>
          <form.Field
            name="restSeconds"
            validators={{
              onChange: ({ value }) => (value < 0 ? 'Rest period cannot be negative' : undefined),
            }}
          >
            {field => (
              <TextField
                fullWidth
                size="small"
                label="Rest Period (seconds)"
                type="text"
                inputProps={{ inputMode: 'decimal' }}
                value={field.state.value === 0 ? '' : field.state.value.toString()}
                onChange={e => {
                  const inputValue = e.target.value;
                  // Allow any valid numeric input including decimals
                  if (inputValue === '' || /^\d*\.?\d*$/.test(inputValue)) {
                    // Store the raw input value to preserve decimal points during typing
                    field.handleChange(inputValue);
                  }
                }}
                onBlur={e => {
                  // Convert to integer on blur for validation
                  const inputValue = e.target.value;
                  if (inputValue !== '') {
                    const numValue = parseFloat(inputValue);
                    if (!isNaN(numValue) && numValue >= 0) {
                      field.handleChange(Math.floor(numValue)); // Ensure integer for seconds
                    }
                  }
                }}
                disabled={saving}
                error={!!field.state.meta.errors.length}
                helperText={field.state.meta.errors.join(', ')}
              />
            )}
          </form.Field>
        </Box>
      </Box>

      <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
        {/* Target Weight */}
        <Box sx={{ flex: '1 1 200px', minWidth: 200 }}>
          <form.Field
            name="targetWeight"
            validators={{
              onChange: ({ value }) => (value < 0 ? 'Weight cannot be negative' : undefined),
            }}
          >
            {field => (
              <TextField
                fullWidth
                size="small"
                label={`Target Weight (${weightUnitLabel})`}
                type="text"
                inputProps={{ inputMode: 'decimal' }}
                value={
                  field.state.value !== undefined &&
                  field.state.value !== null &&
                  field.state.value !== ''
                    ? field.state.value.toString()
                    : ''
                }
                onChange={e => {
                  const inputValue = e.target.value;
                  // Allow any valid numeric input including decimals
                  if (inputValue === '' || /^\d*\.?\d*$/.test(inputValue)) {
                    // Store the raw input value to preserve decimal points
                    field.handleChange(inputValue);
                  }
                }}
                onBlur={e => {
                  // Convert to number on blur for validation
                  const inputValue = e.target.value;
                  if (inputValue !== '') {
                    const numValue = parseFloat(inputValue);
                    if (!isNaN(numValue) && numValue >= 0) {
                      field.handleChange(numValue);
                    }
                  }
                }}
                disabled={saving}
                error={!!field.state.meta.errors.length}
                helperText={field.state.meta.errors.join(', ')}
              />
            )}
          </form.Field>
        </Box>

        {/* Performed Weight (single field when not per-set) */}
        {showPerformedFields && !showPerformedRepsPerSet && (
          <Box sx={{ flex: '1 1 200px', minWidth: 200 }}>
            <form.Field
              name="performedWeight"
              validators={{
                onChange: ({ value }) =>
                  value !== undefined && value < 0 ? 'Weight cannot be negative' : undefined,
              }}
            >
              {field => (
                <TextField
                  fullWidth
                  size="small"
                  label={`Performed Weight (${weightUnitLabel})`}
                  type="text"
                  inputProps={{ inputMode: 'decimal' }}
                  value={
                    field.state.value !== undefined &&
                    field.state.value !== null &&
                    field.state.value !== ''
                      ? field.state.value.toString()
                      : ''
                  }
                  onChange={e => {
                    const inputValue = e.target.value;
                    if (inputValue === '' || /^\d*\.?\d*$/.test(inputValue)) {
                      field.handleChange(inputValue);
                    }
                  }}
                  onBlur={e => {
                    const inputValue = e.target.value;
                    if (inputValue !== '') {
                      const numValue = parseFloat(inputValue);
                      if (!isNaN(numValue) && numValue >= 0) {
                        field.handleChange(numValue);
                      }
                    } else {
                      field.handleChange(undefined);
                    }
                  }}
                  disabled={saving}
                  placeholder="Actual weight used"
                  error={!!field.state.meta.errors.length}
                  helperText={field.state.meta.errors.join(', ')}
                />
              )}
            </form.Field>
          </Box>
        )}
      </Box>

      <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
        {/* Target Reps */}
        <Box sx={{ flex: '1 1 200px', minWidth: 200 }}>
          <form.Field
            name="targetReps"
            validators={{
              onChange: ({ value }) => (value < 1 ? 'Must have at least 1 rep' : undefined),
            }}
          >
            {field => (
              <TextField
                fullWidth
                size="small"
                label="Target Reps"
                type="text"
                inputProps={{ inputMode: 'decimal' }}
                value={field.state.value === 0 ? '' : field.state.value.toString()}
                onChange={e => {
                  const inputValue = e.target.value;
                  // Allow any valid numeric input including decimals
                  if (inputValue === '' || /^\d*\.?\d*$/.test(inputValue)) {
                    // Store the raw input value to preserve decimal points during typing
                    field.handleChange(inputValue);
                  }
                }}
                onBlur={e => {
                  // Convert to integer on blur for validation
                  const inputValue = e.target.value;
                  if (inputValue !== '') {
                    const numValue = parseFloat(inputValue);
                    if (!isNaN(numValue) && numValue >= 0) {
                      field.handleChange(Math.floor(numValue)); // Ensure integer for reps
                    }
                  }
                }}
                disabled={saving}
                error={!!field.state.meta.errors.length}
                helperText={field.state.meta.errors.join(', ')}
              />
            )}
          </form.Field>
        </Box>

        {/* Performed Reps: single field (e.g. add exercise) or per-set (edit exercise) */}
        {showPerformedFields && !showPerformedRepsPerSet && (
          <Box sx={{ flex: '1 1 200px', minWidth: 200 }}>
            <form.Field
              name="performedReps"
              validators={{
                onChange: ({ value }) =>
                  value !== undefined && value < 0 ? 'Reps cannot be negative' : undefined,
              }}
            >
              {field => (
                <TextField
                  fullWidth
                  size="small"
                  label="Performed Reps"
                  type="text"
                  inputProps={{ inputMode: 'decimal' }}
                  value={field.state.value ? field.state.value.toString() : ''}
                  onChange={e => {
                    const inputValue = e.target.value;
                    if (inputValue === '' || /^\d*\.?\d*$/.test(inputValue)) {
                      field.handleChange(inputValue);
                    }
                  }}
                  onBlur={e => {
                    const inputValue = e.target.value;
                    if (inputValue !== '') {
                      const numValue = parseFloat(inputValue);
                      if (!isNaN(numValue) && numValue >= 0) {
                        field.handleChange(Math.floor(numValue));
                      }
                    } else {
                      field.handleChange(undefined);
                    }
                  }}
                  disabled={saving}
                  placeholder="Actual reps completed"
                  error={!!field.state.meta.errors.length}
                  helperText={field.state.meta.errors.join(', ')}
                />
              )}
            </form.Field>
          </Box>
        )}
      </Box>

      {showPerformedFields && showPerformedRepsPerSet && (
        <React.Fragment>
          <Divider sx={{ my: 1 }} />
          <GameText variant="subtitle2" gutterBottom>
            Set Performance
          </GameText>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.5 }}>
            {Array.from(
              {
                length: Math.max(1, Math.floor(Number(form.state.values.totalSets) || 1)),
              },
              (_, i) => (
                <Box
                  key={i}
                  sx={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: 2,
                    flexWrap: 'wrap',
                  }}
                >
                  <GameText variant="body2" sx={{ minWidth: 44 }}>
                    Set {i + 1}
                  </GameText>
                  <form.Field name="performedRepsBySet">
                    {field => {
                      const arr = field.state.value ?? [];
                      return (
                        <TextField
                          size="small"
                          label="Reps"
                          type="text"
                          inputProps={{
                            inputMode: 'numeric',
                            'aria-label': `Performed reps set ${i + 1}`,
                          }}
                          sx={{ width: 80 }}
                          value={arr[i] !== undefined && arr[i] !== null ? String(arr[i]) : ''}
                          onChange={e => {
                            const inputValue = e.target.value;
                            if (inputValue === '' || /^\d*$/.test(inputValue)) {
                              const next = [...arr];
                              while (next.length < i + 1) next.push(undefined);
                              next[i] =
                                inputValue === '' ? undefined : Math.floor(parseFloat(inputValue));
                              field.handleChange(next);
                            }
                          }}
                          onBlur={e => {
                            const inputValue = e.target.value;
                            const next: (number | undefined)[] = [...arr];
                            while (next.length < i + 1) next.push(undefined);
                            if (inputValue === '') {
                              next[i] = undefined;
                            } else {
                              const n = parseFloat(inputValue);
                              next[i] = !isNaN(n) && n >= 0 ? Math.floor(n) : undefined;
                            }
                            field.handleChange(next);
                          }}
                          disabled={saving}
                          placeholder="-"
                        />
                      );
                    }}
                  </form.Field>
                  <form.Field name="performedWeightBySet">
                    {field => {
                      const arr = field.state.value ?? [];
                      return (
                        <TextField
                          size="small"
                          label={`Weight (${weightUnitLabel})`}
                          type="text"
                          inputProps={{
                            inputMode: 'decimal',
                            'aria-label': `Performed weight set ${i + 1}`,
                          }}
                          sx={{ width: 100 }}
                          value={arr[i] !== undefined && arr[i] !== null ? String(arr[i]) : ''}
                          onChange={e => {
                            const inputValue = e.target.value;
                            if (inputValue === '' || /^\d*\.?\d*$/.test(inputValue)) {
                              const next = [...arr];
                              while (next.length < i + 1) next.push(undefined);
                              next[i] = inputValue === '' ? undefined : parseFloat(inputValue);
                              field.handleChange(next);
                            }
                          }}
                          onBlur={e => {
                            const inputValue = e.target.value;
                            const next: (number | undefined)[] = [...arr];
                            while (next.length < i + 1) next.push(undefined);
                            if (inputValue === '') {
                              next[i] = undefined;
                            } else {
                              const n = parseFloat(inputValue);
                              next[i] = !isNaN(n) && n >= 0 ? n : undefined;
                            }
                            field.handleChange(next);
                          }}
                          disabled={saving}
                          placeholder="-"
                        />
                      );
                    }}
                  </form.Field>
                </Box>
              )
            )}
          </Box>
        </React.Fragment>
      )}

      {/* Tempo Settings */}
      {showTempoFields && (
        <React.Fragment>
          <Divider sx={{ my: 1 }} />
          <GameText variant="subtitle2" gutterBottom>
            Tempo Settings
          </GameText>

          <Box>
            <form.Field name="useTempo">
              {field => (
                <FormControlLabel
                  control={
                    <Switch
                      checked={field.state.value}
                      onChange={e => field.handleChange(e.target.checked)}
                      disabled={saving}
                    />
                  }
                  label="Use Tempo"
                />
              )}
            </form.Field>
          </Box>

          {form.state.values.useTempo && (
            <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
              <Box sx={{ flex: '1 1 150px', minWidth: 150 }}>
                <form.Field name="eccentricTempo">
                  {field => (
                    <TextField
                      fullWidth
                      size="small"
                      label="Eccentric"
                      value={field.state.value}
                      onChange={e => field.handleChange(e.target.value)}
                      disabled={saving}
                      placeholder="e.g., 3"
                    />
                  )}
                </form.Field>
              </Box>
              <Box sx={{ flex: '1 1 150px', minWidth: 150 }}>
                <form.Field name="isometricTempo">
                  {field => (
                    <TextField
                      fullWidth
                      size="small"
                      label="Isometric"
                      value={field.state.value}
                      onChange={e => field.handleChange(e.target.value)}
                      disabled={saving}
                      placeholder="e.g., 1"
                    />
                  )}
                </form.Field>
              </Box>
              <Box sx={{ flex: '1 1 150px', minWidth: 150 }}>
                <form.Field name="concentricTempo">
                  {field => (
                    <TextField
                      fullWidth
                      size="small"
                      label="Concentric"
                      value={field.state.value}
                      onChange={e => field.handleChange(e.target.value)}
                      disabled={saving}
                      placeholder="e.g., X"
                    />
                  )}
                </form.Field>
              </Box>
            </Box>
          )}
        </React.Fragment>
      )}

      {/* Set Type Options */}
      {showSetTypeFields && (
        <React.Fragment>
          <Divider sx={{ my: 1 }} />
          <GameText variant="subtitle2" gutterBottom>
            Set Type Options
          </GameText>

          <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
            <form.Field name="isAmrap">
              {field => (
                <FormControlLabel
                  control={
                    <Switch
                      checked={field.state.value}
                      onChange={e => field.handleChange(e.target.checked)}
                      disabled={saving}
                    />
                  }
                  label="AMRAP"
                />
              )}
            </form.Field>

            <form.Field name="isEmom">
              {field => (
                <FormControlLabel
                  control={
                    <Switch
                      checked={field.state.value}
                      onChange={e => field.handleChange(e.target.checked)}
                      disabled={saving}
                    />
                  }
                  label="EMOM"
                />
              )}
            </form.Field>
          </Box>
        </React.Fragment>
      )}
    </Box>
  );
};
