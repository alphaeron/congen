import { ExpandMore, RestartAlt, VerticalAlignBottom } from '@mui/icons-material';
import {
  Box,
  Button,
  Collapse,
  FormControlLabel,
  IconButton,
  Switch,
  TextField,
  Tooltip,
} from '@mui/material';
import React, { useState } from 'react';

import { GameText } from './GameTheme';
import {
  NumericStepInput,
  compactNumericStepFieldSx,
  defaultNumericStepFieldSx,
} from './NumericStepInput';
import type { ProgrammedExerciseWithSetSchemes, UserWeightUnitPreference } from '../api/types';
import { formatWeightWithUnit } from '../common/utils';

import type { SxProps, Theme } from '@mui/material/styles';
import type { ReactFormExtendedApi } from '@tanstack/react-form';

export interface SetSchemeFormData {
  totalSets: number;
  targetWeight: number;
  performedWeight?: number;
  targetReps: number;
  performedReps?: number;
  performedRepsBySet?: (number | undefined)[];
  performedWeightBySet?: (number | undefined)[];
  customizePerSet: boolean;
  restSeconds: number;
  useTempo: boolean;
  eccentricTempo: string;
  isometricTempo: string;
  concentricTempo: string;
  isAmrap: boolean;
  isEmom: boolean;
}

export type SetSchemeFormApi = ReactFormExtendedApi<
  SetSchemeFormData,
  undefined,
  undefined,
  undefined,
  undefined,
  undefined,
  undefined,
  undefined,
  undefined,
  undefined,
  undefined,
  undefined
>;

export interface SetSchemeFormProps {
  form: SetSchemeFormApi;
  saving?: boolean;
  exerciseName?: string;
  weightUnitPreferences?: UserWeightUnitPreference[];
  showPerformedFields?: boolean;
  showTempoFields?: boolean;
  showSetTypeFields?: boolean;
  bandWeightDisplay?: string;
  compactSteppers?: boolean;
}

export function getEffectivePerformedForSetIndex(
  values: Pick<
    SetSchemeFormData,
    | 'performedReps'
    | 'performedWeight'
    | 'performedRepsBySet'
    | 'performedWeightBySet'
    | 'targetReps'
    | 'targetWeight'
  >,
  setIndex: number
): { reps: number | undefined; weight: number | undefined } {
  return {
    reps: values.performedRepsBySet?.[setIndex] ?? values.performedReps ?? values.targetReps,
    weight:
      values.performedWeightBySet?.[setIndex] ?? values.performedWeight ?? values.targetWeight,
  };
}

export function buildPerformedWeightBySetFilledFromSource(
  values: SetSchemeFormData,
  sourceSetIndex: number
): (number | undefined)[] {
  const setCount = Math.max(1, Math.floor(Number(values.totalSets) || 1));
  const sourceWeight = getEffectivePerformedForSetIndex(values, sourceSetIndex).weight;
  return Array.from({ length: setCount }, () => sourceWeight);
}

export function buildPerformedRepsBySetFilledFromSource(
  values: SetSchemeFormData,
  sourceSetIndex: number
): (number | undefined)[] {
  const setCount = Math.max(1, Math.floor(Number(values.totalSets) || 1));
  const sourceReps = getEffectivePerformedForSetIndex(values, sourceSetIndex).reps;
  return Array.from({ length: setCount }, () => sourceReps);
}

export function buildPerformedBySetFilledFromSource(
  values: SetSchemeFormData,
  sourceSetIndex: number
): {
  performedRepsBySet: (number | undefined)[];
  performedWeightBySet: (number | undefined)[];
} {
  return {
    performedRepsBySet: buildPerformedRepsBySetFilledFromSource(values, sourceSetIndex),
    performedWeightBySet: buildPerformedWeightBySetFilledFromSource(values, sourceSetIndex),
  };
}

export function resolvePerformedForSetIndex(
  setIndex: number,
  value: Pick<
    SetSchemeFormData,
    | 'customizePerSet'
    | 'performedReps'
    | 'performedWeight'
    | 'performedRepsBySet'
    | 'performedWeightBySet'
  >
): { reps: number | undefined; weight: number | undefined } {
  if (value.customizePerSet) {
    return {
      reps: value.performedRepsBySet?.[setIndex] ?? value.performedReps,
      weight: value.performedWeightBySet?.[setIndex] ?? value.performedWeight,
    };
  }
  return {
    reps: value.performedReps,
    weight: value.performedWeight,
  };
}

export function performedValuesVary(
  repsBySet: (number | undefined)[],
  weightBySet: (number | undefined)[],
  sharedReps: number | undefined,
  sharedWeight: number | undefined
): boolean {
  const effectiveReps = repsBySet.map(r => r ?? sharedReps);
  const effectiveWeights = weightBySet.map(w => w ?? sharedWeight);
  const firstRep = effectiveReps[0];
  const firstWeight = effectiveWeights[0];
  return effectiveReps.some(r => r !== firstRep) || effectiveWeights.some(w => w !== firstWeight);
}

export function buildSetSchemeFormDefaultsFromExercise(
  exercise: ProgrammedExerciseWithSetSchemes,
  unit: 'KG' | 'LBS' | undefined
): SetSchemeFormData {
  const sortedSchemes = [...exercise.set_schemes].sort((a, b) => a.set_number - b.set_number);
  const firstSetScheme = sortedSchemes[0];
  const targetWeight = firstSetScheme?.target_weight
    ? parseFloat(formatWeightWithUnit(firstSetScheme.target_weight, unit, false)) || 0
    : 0;
  const targetReps = firstSetScheme?.target_rep_count || 0;

  const performedRepsBySet = sortedSchemes.map(scheme => {
    if (scheme.performed_rep_count != null) {
      return scheme.performed_rep_count;
    }
    return targetReps > 0 ? targetReps : undefined;
  });

  const performedWeightBySet = sortedSchemes.map(scheme => {
    if (scheme.performed_weight != null) {
      const n = parseFloat(formatWeightWithUnit(scheme.performed_weight, unit, false));
      return Number.isNaN(n) ? undefined : n;
    }
    return targetWeight > 0 ? targetWeight : undefined;
  });

  const sharedReps = performedRepsBySet[0];
  const sharedWeight = performedWeightBySet[0];
  const customizePerSet = performedValuesVary(
    performedRepsBySet,
    performedWeightBySet,
    sharedReps,
    sharedWeight
  );

  return {
    totalSets: Math.max(1, sortedSchemes.length),
    targetWeight,
    targetReps,
    performedReps: sharedReps,
    performedWeight: sharedWeight,
    performedRepsBySet: customizePerSet ? performedRepsBySet : [],
    performedWeightBySet: customizePerSet ? performedWeightBySet : [],
    customizePerSet,
    restSeconds: firstSetScheme?.rest_seconds || 0,
    useTempo: firstSetScheme?.use_tempo || false,
    eccentricTempo: firstSetScheme?.eccentric_tempo || '',
    isometricTempo: firstSetScheme?.isometric_tempo || '',
    concentricTempo: firstSetScheme?.concentric_tempo || '',
    isAmrap: firstSetScheme?.is_amrap || false,
    isEmom: firstSetScheme?.is_emom || false,
  };
}

export const SetSchemeForm: React.FC<SetSchemeFormProps> = ({
  form,
  saving = false,
  exerciseName,
  weightUnitPreferences = [],
  showPerformedFields = true,
  showTempoFields = true,
  showSetTypeFields = true,
  bandWeightDisplay,
  compactSteppers = false,
}) => {
  const [advancedOpen, setAdvancedOpen] = useState(false);

  const weightUnitPreference = exerciseName
    ? weightUnitPreferences.find(pref => pref.exercise_name === exerciseName)
    : undefined;

  const preferredUnit = weightUnitPreference?.preferred_unit;
  const weightUnitLabel = preferredUnit === 'LBS' ? 'lbs' : 'kg';
  const fieldFlexSx = compactSteppers ? compactNumericStepFieldSx : defaultNumericStepFieldSx;
  const compactFieldRowSx: SxProps<Theme> = {
    display: 'flex',
    flexWrap: 'wrap',
    gap: 2,
    justifyContent: 'flex-start',
    alignItems: 'flex-end',
  };
  const gridFieldRowSx: SxProps<Theme> = {
    display: 'grid',
    gridTemplateColumns: { xs: '1fr', sm: 'repeat(2, minmax(0, 1fr))' },
    gap: 2,
    alignItems: 'start',
  };
  const fieldRowSx = compactSteppers ? compactFieldRowSx : gridFieldRowSx;
  const weightFieldCellSx = (isAmrap: boolean): SxProps<Theme> | undefined =>
    isAmrap && !compactSteppers ? { gridColumn: { sm: '1' } } : undefined;
  const stepperCompact = compactSteppers;

  const syncPerformedToTarget = (values: SetSchemeFormData) => {
    form.setFieldValue('performedReps', values.targetReps);
    form.setFieldValue('performedWeight', values.targetWeight);

    if (values.customizePerSet) {
      const setCount = Math.max(1, Math.floor(Number(values.totalSets) || 1));
      form.setFieldValue(
        'performedRepsBySet',
        Array.from({ length: setCount }, () => values.targetReps)
      );
      form.setFieldValue(
        'performedWeightBySet',
        Array.from({ length: setCount }, () => values.targetWeight)
      );
    } else {
      form.setFieldValue('performedRepsBySet', []);
      form.setFieldValue('performedWeightBySet', []);
    }
  };

  const enableCustomizePerSet = (values: SetSchemeFormData) => {
    const setCount = Math.max(1, Math.floor(Number(values.totalSets) || 1));
    const repsArr: (number | undefined)[] = [];
    const weightArr: (number | undefined)[] = [];
    for (let i = 0; i < setCount; i++) {
      repsArr.push(values.performedReps ?? values.targetReps);
      weightArr.push(values.performedWeight ?? values.targetWeight);
    }
    form.setFieldValue('performedRepsBySet', repsArr);
    form.setFieldValue('performedWeightBySet', weightArr);
    form.setFieldValue('customizePerSet', true);
  };

  const collapseToSharedPerformed = () => {
    form.setFieldValue('performedRepsBySet', []);
    form.setFieldValue('performedWeightBySet', []);
    form.setFieldValue('customizePerSet', false);
  };

  const applyWeightFromSetToAll = (values: SetSchemeFormData, sourceSetIndex: number) => {
    const sourceWeight = getEffectivePerformedForSetIndex(values, sourceSetIndex).weight;
    form.setFieldValue(
      'performedWeightBySet',
      buildPerformedWeightBySetFilledFromSource(values, sourceSetIndex)
    );
    form.setFieldValue('performedWeight', sourceWeight);
  };

  const applyRepsFromSetToAll = (values: SetSchemeFormData, sourceSetIndex: number) => {
    const sourceReps = getEffectivePerformedForSetIndex(values, sourceSetIndex).reps;
    form.setFieldValue(
      'performedRepsBySet',
      buildPerformedRepsBySetFilledFromSource(values, sourceSetIndex)
    );
    form.setFieldValue('performedReps', sourceReps);
  };

  const renderFillDownLabelAction = (
    tooltip: string,
    ariaLabel: string,
    onClick: () => void
  ): React.ReactNode => (
    <Tooltip title={tooltip}>
      <IconButton
        size="small"
        onClick={onClick}
        disabled={saving}
        aria-label={ariaLabel}
        sx={{ minWidth: 32, minHeight: 32, p: 0.5 }}
      >
        <VerticalAlignBottom fontSize="small" />
      </IconButton>
    </Tooltip>
  );

  const togglePerformedPerSet = (values: SetSchemeFormData) => {
    if (values.customizePerSet) {
      collapseToSharedPerformed();
    } else {
      enableCustomizePerSet(values);
    }
  };

  const handleTotalSetsChange = (nextSets: number, values: SetSchemeFormData) => {
    const clamped = Math.max(1, Math.floor(nextSets));
    form.setFieldValue('totalSets', clamped);
    if (values.customizePerSet) {
      const repsArr = [...(values.performedRepsBySet ?? [])];
      const weightArr = [...(values.performedWeightBySet ?? [])];
      while (repsArr.length < clamped) {
        repsArr.push(values.performedReps ?? values.targetReps);
      }
      while (weightArr.length < clamped) {
        weightArr.push(values.performedWeight ?? values.targetWeight);
      }
      form.setFieldValue('performedRepsBySet', repsArr.slice(0, clamped));
      form.setFieldValue('performedWeightBySet', weightArr.slice(0, clamped));
    }
  };

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
      <GameText variant="subtitle2" gutterBottom>
        Set Scheme Details
      </GameText>

      {bandWeightDisplay ? (
        <GameText variant="body2" color="text.secondary">
          Band: {bandWeightDisplay}
        </GameText>
      ) : null}

      <form.Subscribe selector={(state: { values: SetSchemeFormData }) => state.values}>
        {(values: SetSchemeFormData) => (
          <Box sx={fieldRowSx}>
            <form.Field
              name="totalSets"
              validators={{
                onChange: ({ value }: { value: number }) =>
                  value < 1 ? 'Must have at least 1 set' : undefined,
              }}
            >
              {field => (
                <NumericStepInput
                  label="Sets"
                  value={field.state.value}
                  onChange={v => handleTotalSetsChange(v ?? 1, values)}
                  min={1}
                  max={20}
                  integer
                  disabled={saving}
                  error={!!field.state.meta.errors.length}
                  helperText={field.state.meta.errors.join(', ')}
                  flexSx={fieldFlexSx}
                  compact={stepperCompact}
                />
              )}
            </form.Field>

            <form.Field
              name="restSeconds"
              validators={{
                onChange: ({ value }: { value: number }) =>
                  value < 0 ? 'Rest period cannot be negative' : undefined,
              }}
            >
              {field => (
                <NumericStepInput
                  label="Rest"
                  suffix="sec"
                  value={field.state.value}
                  onChange={v => field.handleChange(v ?? 0)}
                  min={0}
                  step={15}
                  integer
                  disabled={saving}
                  error={!!field.state.meta.errors.length}
                  helperText={field.state.meta.errors.join(', ')}
                  flexSx={fieldFlexSx}
                  compact={stepperCompact}
                />
              )}
            </form.Field>
          </Box>
        )}
      </form.Subscribe>

      <form.Subscribe selector={(state: { values: SetSchemeFormData }) => state.values.isAmrap}>
        {isAmrap => (
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
            <GameText variant="subtitle2">Target (all sets)</GameText>
            <Box sx={fieldRowSx}>
              {!isAmrap ? (
                <form.Field
                  name="targetReps"
                  validators={{
                    onChange: ({ value }) => (value < 1 ? 'Must have at least 1 rep' : undefined),
                  }}
                >
                  {field => (
                    <NumericStepInput
                      label="Target reps"
                      value={field.state.value}
                      onChange={v => field.handleChange(v ?? 1)}
                      min={1}
                      integer
                      disabled={saving}
                      error={!!field.state.meta.errors.length}
                      helperText={field.state.meta.errors.join(', ')}
                      flexSx={fieldFlexSx}
                      compact={stepperCompact}
                    />
                  )}
                </form.Field>
              ) : null}

              <Box sx={weightFieldCellSx(isAmrap)}>
                <form.Field
                  name="targetWeight"
                  validators={{
                    onChange: ({ value }) => (value < 0 ? 'Weight cannot be negative' : undefined),
                  }}
                >
                  {field => (
                    <NumericStepInput
                      label="Target weight"
                      suffix={weightUnitLabel}
                      value={field.state.value}
                      onChange={v => field.handleChange(v ?? 0)}
                      min={0}
                      step={0.5}
                      disabled={saving}
                      error={!!field.state.meta.errors.length}
                      helperText={field.state.meta.errors.join(', ')}
                      flexSx={fieldFlexSx}
                      compact={stepperCompact}
                    />
                  )}
                </form.Field>
              </Box>
            </Box>
          </Box>
        )}
      </form.Subscribe>

      {showPerformedFields ? (
        <form.Subscribe selector={(state: { values: SetSchemeFormData }) => state.values}>
          {(values: SetSchemeFormData) => {
            const totalSets = Math.max(1, Math.floor(Number(values.totalSets) || 1));
            const customizePerSet = values.customizePerSet;
            const isAmrap = values.isAmrap;

            return (
              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
                <Box
                  sx={{
                    display: 'flex',
                    flexWrap: 'wrap',
                    alignItems: 'center',
                    gap: 0.5,
                  }}
                >
                  <Tooltip title={customizePerSet ? 'Collapse to all sets' : 'Expand per set'}>
                    <IconButton
                      size="small"
                      onClick={() => togglePerformedPerSet(values)}
                      disabled={saving}
                      aria-expanded={customizePerSet}
                      aria-label={customizePerSet ? 'Collapse to all sets' : 'Expand per set'}
                    >
                      <ExpandMore
                        fontSize="small"
                        sx={{
                          transform: customizePerSet ? 'rotate(180deg)' : 'rotate(0deg)',
                          transition: 'transform 0.2s',
                        }}
                      />
                    </IconButton>
                  </Tooltip>
                  <GameText variant="subtitle2">Performed</GameText>
                  <Tooltip title="Match target">
                    <IconButton
                      size="small"
                      color="primary"
                      onClick={() => syncPerformedToTarget(values)}
                      disabled={saving}
                      aria-label="Match target"
                    >
                      <RestartAlt fontSize="small" />
                    </IconButton>
                  </Tooltip>
                </Box>

                {customizePerSet ? (
                  <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                    {Array.from({ length: totalSets }, (_, setIndex) => (
                      <Box key={setIndex} sx={fieldRowSx}>
                        {!isAmrap ? (
                          <form.Field name="performedRepsBySet">
                            {field => {
                              const arr = field.state.value ?? [];
                              const effective =
                                arr[setIndex] ?? values.performedReps ?? values.targetReps;
                              return (
                                <NumericStepInput
                                  label={`Set ${setIndex + 1} reps`}
                                  value={effective}
                                  onChange={v => {
                                    const next = [...arr];
                                    while (next.length < setIndex + 1) {
                                      next.push(undefined);
                                    }
                                    next[setIndex] = v;
                                    field.handleChange(next);
                                  }}
                                  min={0}
                                  integer
                                  allowEmpty
                                  disabled={saving}
                                  flexSx={fieldFlexSx}
                                  compact={stepperCompact}
                                  labelAction={renderFillDownLabelAction(
                                    'Apply this reps to all sets',
                                    `Apply set ${setIndex + 1} reps to all sets`,
                                    () => applyRepsFromSetToAll(values, setIndex)
                                  )}
                                />
                              );
                            }}
                          </form.Field>
                        ) : null}

                        <Box sx={weightFieldCellSx(isAmrap)}>
                          <form.Field name="performedWeightBySet">
                            {field => {
                              const arr = field.state.value ?? [];
                              const effective =
                                arr[setIndex] ?? values.performedWeight ?? values.targetWeight;
                              return (
                                <NumericStepInput
                                  label={`Set ${setIndex + 1} weight`}
                                  suffix={weightUnitLabel}
                                  value={effective}
                                  onChange={v => {
                                    const next = [...arr];
                                    while (next.length < setIndex + 1) {
                                      next.push(undefined);
                                    }
                                    next[setIndex] = v;
                                    field.handleChange(next);
                                  }}
                                  min={0}
                                  step={0.5}
                                  allowEmpty
                                  disabled={saving}
                                  flexSx={fieldFlexSx}
                                  compact={stepperCompact}
                                  labelAction={renderFillDownLabelAction(
                                    'Apply this weight to all sets',
                                    `Apply set ${setIndex + 1} weight to all sets`,
                                    () => applyWeightFromSetToAll(values, setIndex)
                                  )}
                                />
                              );
                            }}
                          </form.Field>
                        </Box>
                      </Box>
                    ))}
                  </Box>
                ) : (
                  <Box sx={fieldRowSx}>
                    {!isAmrap ? (
                      <form.Field
                        name="performedReps"
                        validators={{
                          onChange: ({ value }: { value: number | undefined }) =>
                            value !== undefined && value < 0
                              ? 'Reps cannot be negative'
                              : undefined,
                        }}
                      >
                        {field => (
                          <NumericStepInput
                            label="Performed reps"
                            value={field.state.value}
                            onChange={v => field.handleChange(v)}
                            min={0}
                            integer
                            allowEmpty
                            disabled={saving}
                            error={!!field.state.meta.errors.length}
                            helperText={field.state.meta.errors.join(', ')}
                            flexSx={fieldFlexSx}
                            compact={stepperCompact}
                          />
                        )}
                      </form.Field>
                    ) : null}

                    <Box sx={weightFieldCellSx(isAmrap)}>
                      <form.Field
                        name="performedWeight"
                        validators={{
                          onChange: ({ value }: { value: number | undefined }) =>
                            value !== undefined && value < 0
                              ? 'Weight cannot be negative'
                              : undefined,
                        }}
                      >
                        {field => (
                          <NumericStepInput
                            label="Performed weight"
                            suffix={weightUnitLabel}
                            value={field.state.value}
                            onChange={v => field.handleChange(v)}
                            min={0}
                            step={0.5}
                            allowEmpty
                            disabled={saving}
                            error={!!field.state.meta.errors.length}
                            helperText={field.state.meta.errors.join(', ')}
                            flexSx={fieldFlexSx}
                            compact={stepperCompact}
                          />
                        )}
                      </form.Field>
                    </Box>
                  </Box>
                )}
              </Box>
            );
          }}
        </form.Subscribe>
      ) : null}

      {(showTempoFields || showSetTypeFields) && (
        <Box>
          <Button
            size="small"
            onClick={() => setAdvancedOpen(prev => !prev)}
            endIcon={
              <ExpandMore
                sx={{
                  transform: advancedOpen ? 'rotate(180deg)' : 'rotate(0deg)',
                  transition: 'transform 0.2s',
                }}
              />
            }
            aria-expanded={advancedOpen}
          >
            Advanced
          </Button>
          <Collapse in={advancedOpen}>
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 2 }}>
              {showTempoFields ? (
                <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
                  <GameText variant="subtitle2">Tempo</GameText>
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

                  <form.Subscribe
                    selector={(state: { values: SetSchemeFormData }) => state.values.useTempo}
                  >
                    {(useTempo: boolean) =>
                      useTempo ? (
                        <Box sx={fieldRowSx}>
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
                      ) : null
                    }
                  </form.Subscribe>
                </Box>
              ) : null}

              {showSetTypeFields ? (
                <Box sx={fieldRowSx}>
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
              ) : null}
            </Box>
          </Collapse>
        </Box>
      )}
    </Box>
  );
};
