import { Add, Remove } from '@mui/icons-material';
import { Box, IconButton } from '@mui/material';
import React, { useEffect, useRef, useState } from 'react';

import { GameText } from './GameTheme';

export interface NumericStepInputProps {
  label: string;
  value: number | undefined;
  onChange: (value: number | undefined) => void;
  min?: number;
  max?: number;
  step?: number;
  integer?: boolean;
  disabled?: boolean;
  error?: boolean;
  helperText?: string;
  allowEmpty?: boolean;
  suffix?: string;
  flexSx?: object;
  inputAriaLabel?: string;
  labelAction?: React.ReactNode;
  compact?: boolean;
}

export const defaultNumericStepFieldSx = {
  minWidth: 0,
  width: '100%',
};

export const compactNumericStepFieldSx = {
  flex: '0 1 auto',
  width: 'max-content',
  maxWidth: '100%',
};

/**
 * Numeric input with integrated increment and decrement controls in a single bordered stepper.
 */
export const NumericStepInput: React.FC<NumericStepInputProps> = ({
  label,
  value,
  onChange,
  min = 0,
  max,
  step = 1,
  integer = false,
  disabled = false,
  error = false,
  helperText,
  allowEmpty = false,
  suffix,
  flexSx,
  inputAriaLabel,
  labelAction,
  compact = false,
}) => {
  const fieldAriaLabel = inputAriaLabel ?? label;
  const inputRef = useRef<HTMLInputElement>(null);
  const [draft, setDraft] = useState<string | null>(null);

  const formatExternalValue = (externalValue: number | undefined): string => {
    if (externalValue === undefined || externalValue === null) {
      return '';
    }
    return externalValue.toString();
  };

  const isPartialDecimalInput = (raw: string): boolean => {
    if (integer) {
      return false;
    }
    return raw === '.' || raw.endsWith('.');
  };

  useEffect(() => {
    if (draft === null) {
      return;
    }
    const external = formatExternalValue(value);
    const isFocused = inputRef.current === document.activeElement;
    if (external !== draft && !isFocused) {
      setDraft(null);
    }
  }, [value, draft]);

  const valueToDisplay = (): string => {
    if (draft !== null) {
      return draft;
    }
    if (value === undefined || value === null) {
      return '';
    }
    return value.toString();
  };

  const commitDraft = (raw: string) => {
    if (raw === '') {
      onChange(allowEmpty ? undefined : min);
      return;
    }
    const numValue = integer ? parseInt(raw, 10) : parseFloat(raw);
    if (Number.isNaN(numValue)) {
      return;
    }
    let next = integer ? Math.floor(numValue) : numValue;
    if (max !== undefined) {
      next = Math.min(max, next);
    }
    next = Math.max(min, next);
    onChange(next);
  };

  const applyNumericChange = (raw: string) => {
    if (raw === '') {
      setDraft('');
      if (allowEmpty) {
        onChange(undefined);
      }
      return;
    }
    const pattern = integer ? /^\d*$/ : /^\d*\.?\d*$/;
    if (!pattern.test(raw)) {
      return;
    }
    setDraft(raw);
    if (!isPartialDecimalInput(raw)) {
      commitDraft(raw);
    }
  };

  const normalizeBlurInput = (raw: string): string => {
    const trimmed = raw.trim();
    if (trimmed === '' || trimmed === '.') {
      return '';
    }
    if (!integer && trimmed.endsWith('.')) {
      return trimmed.slice(0, -1);
    }
    return trimmed;
  };

  const decrement = () => {
    if (value === undefined) {
      onChange(min);
      return;
    }
    const next = value - step;
    if (next < min) {
      onChange(allowEmpty ? undefined : min);
      return;
    }
    onChange(integer ? Math.floor(next) : next);
  };

  const increment = () => {
    const base = value ?? min;
    let next = base + step;
    if (max !== undefined) {
      next = Math.min(max, next);
    }
    onChange(integer ? Math.floor(next) : next);
  };

  const handleInputBlur = (raw: string) => {
    setDraft(null);
    const normalized = normalizeBlurInput(raw);
    if (normalized === '' && allowEmpty) {
      onChange(undefined);
      return;
    }
    if (normalized === '') {
      onChange(min);
      return;
    }
    commitDraft(normalized);
  };

  const containerSx = compact
    ? { ...compactNumericStepFieldSx, ...flexSx }
    : { ...defaultNumericStepFieldSx, ...flexSx };

  return (
    <Box sx={containerSx}>
      <Box
        sx={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          gap: 0.5,
          mb: 0.5,
          minHeight: 20,
        }}
      >
        <GameText
          variant="caption"
          color="text.secondary"
          sx={{ whiteSpace: 'nowrap', lineHeight: '20px' }}
        >
          {label}
          {suffix ? ` (${suffix})` : ''}
        </GameText>
        {labelAction ?? null}
      </Box>
      <Box
        sx={{
          display: 'inline-flex',
          alignItems: 'stretch',
          height: 36,
          border: 1,
          borderColor: error ? 'error.main' : 'divider',
          borderRadius: 1,
          overflow: 'hidden',
          bgcolor: disabled ? 'action.disabledBackground' : 'background.paper',
          flexShrink: 0,
        }}
      >
        <IconButton
          size="small"
          onClick={decrement}
          disabled={disabled}
          aria-label={`Decrease ${fieldAriaLabel}`}
          sx={{
            borderRadius: 0,
            borderRight: 1,
            borderColor: 'divider',
            width: 32,
            height: '100%',
            p: 0,
          }}
        >
          <Remove sx={{ fontSize: 18 }} />
        </IconButton>
        <Box
          component="input"
          ref={inputRef}
          value={valueToDisplay()}
          onChange={(e: React.ChangeEvent<HTMLInputElement>) => applyNumericChange(e.target.value)}
          onBlur={(e: React.FocusEvent<HTMLInputElement>) => handleInputBlur(e.target.value)}
          disabled={disabled}
          aria-label={fieldAriaLabel}
          inputMode={integer ? 'numeric' : 'decimal'}
          sx={{
            width: 48,
            minWidth: 40,
            border: 'none',
            outline: 'none',
            textAlign: 'center',
            fontSize: '0.875rem',
            bgcolor: 'transparent',
            color: 'text.primary',
            p: 0,
            MozAppearance: 'textfield',
            '&::-webkit-outer-spin-button, &::-webkit-inner-spin-button': {
              WebkitAppearance: 'none',
              margin: 0,
            },
          }}
        />
        <IconButton
          size="small"
          onClick={increment}
          disabled={disabled}
          aria-label={`Increase ${fieldAriaLabel}`}
          sx={{
            borderRadius: 0,
            borderLeft: 1,
            borderColor: 'divider',
            width: 32,
            height: '100%',
            p: 0,
          }}
        >
          <Add sx={{ fontSize: 18 }} />
        </IconButton>
      </Box>
      {helperText ? (
        <GameText variant="caption" color={error ? 'error' : 'text.secondary'} sx={{ mt: 0.5 }}>
          {helperText}
        </GameText>
      ) : null}
    </Box>
  );
};
