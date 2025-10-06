import { Autocomplete } from '@mui/material';
import { useField } from '@tanstack/react-form';
import React, { useMemo } from 'react';

import {
  GameText,
  GameTextField,
  GameFormControl,
  GameInputLabel,
  GameSelect,
  GameMenuItem,
} from './GameTheme';

interface BaseFormFieldProps {
  label: string;
  value?: unknown;
  onChange?: (value: unknown) => void;
  error?: boolean;
  helperText?: string;
  disabled?: boolean;
  required?: boolean;
  fullWidth?: boolean;
  sx?: unknown;
  // TanStack Form integration
  name?: string;
  form?: unknown;
}

interface TextFormFieldProps extends BaseFormFieldProps {
  type: 'text' | 'number' | 'email' | 'password';
  multiline?: boolean;
  rows?: number;
  inputProps?: Record<string, unknown>;
}

interface SelectFormFieldProps extends BaseFormFieldProps {
  type: 'select';
  options: Array<{ value: unknown; label: string }>;
}

interface AutocompleteFormFieldProps extends BaseFormFieldProps {
  type: 'autocomplete';
  options: string[];
  getOptionLabel?: (option: string) => string;
}

type FormFieldProps = TextFormFieldProps | SelectFormFieldProps | AutocompleteFormFieldProps;

/**
 * Reusable form field component with consistent styling and validation.
 *
 * Provides a unified interface for different types of form fields including
 * text inputs, selects, and autocomplete fields with consistent styling.
 * Supports both traditional controlled components and TanStack Form integration.
 *
 * @param type Type of form field ('text', 'number', 'email', 'password', 'select', 'autocomplete')
 * @param label Field label
 * @param value Current field value (for controlled mode)
 * @param onChange Function to call when value changes (for controlled mode)
 * @param error Whether the field has an error (for controlled mode)
 * @param helperText Helper text to display below the field (for controlled mode)
 * @param disabled Whether the field is disabled
 * @param required Whether the field is required
 * @param fullWidth Whether the field should take full width
 * @param multiline Whether text field should be multiline (text fields only)
 * @param rows Number of rows for multiline text fields
 * @param inputProps Additional props for the input element
 * @param options Options for select/autocomplete fields
 * @param getOptionLabel Function to get display label for autocomplete options
 * @param name Field name for TanStack Form integration
 * @param form Form instance for TanStack Form integration
 * @return Form field component
 */
export const FormField: React.FC<FormFieldProps> = props => {
  const {
    type,
    label,
    value,
    onChange,
    error = false,
    helperText,
    disabled = false,
    required = false,
    fullWidth = true,
    sx,
    name,
    form,
  } = props;

  // TanStack Form integration
  const tanstackField =
    name && form
      ? useField({
          name,
          form,
          validators: {
            onChange: required
              ? (value: unknown) => {
                  if (!value || (typeof value === 'string' && !value.trim())) {
                    return 'This field is required';
                  }
                  return undefined;
                }
              : undefined,
          },
        })
      : null;

  // Use TanStack Form values if available, otherwise fall back to controlled props
  const fieldValue = tanstackField ? tanstackField.state.value : value;
  const fieldError = tanstackField ? tanstackField.state.meta.errors.length > 0 : error;
  const fieldHelperText = tanstackField ? tanstackField.state.meta.errors[0] : helperText;
  const handleChange = tanstackField ? tanstackField.handleChange : onChange;
  const handleBlur = tanstackField ? tanstackField.handleBlur : undefined;

  if (type === 'select') {
    const { options } = props as SelectFormFieldProps;
    return (
      <GameFormControl
        fullWidth={fullWidth}
        error={fieldError}
        disabled={disabled}
        required={required}
      >
        <GameInputLabel>{label}</GameInputLabel>
        <GameSelect
          value={fieldValue !== undefined ? fieldValue : ''}
          label={label}
          onChange={e => handleChange?.(e.target.value)}
          onBlur={handleBlur}
        >
          {options.map(option => (
            <GameMenuItem key={option.value} value={option.value}>
              {option.label}
            </GameMenuItem>
          ))}
        </GameSelect>
        {fieldHelperText && (
          <GameText variant="caption" textVariant={fieldError ? 'error' : 'secondary'}>
            {fieldHelperText}
          </GameText>
        )}
      </GameFormControl>
    );
  }

  if (type === 'autocomplete') {
    const { options, getOptionLabel } = props as AutocompleteFormFieldProps;
    const memoizedGetOptionLabel = useMemo(
      () => getOptionLabel || ((option: string) => option),
      [getOptionLabel]
    );
    return (
      <Autocomplete
        options={options}
        value={fieldValue || null}
        onChange={(_, newValue) => handleChange?.(newValue)}
        onBlur={handleBlur}
        getOptionLabel={memoizedGetOptionLabel}
        renderInput={params => (
          <GameTextField
            {...params}
            label={label}
            variant="outlined"
            fullWidth={fullWidth}
            error={fieldError}
            helperText={fieldHelperText}
            disabled={disabled}
            required={required}
          />
        )}
        clearOnBlur
        selectOnFocus
        handleHomeEndKeys
      />
    );
  }

  // Text field types
  const { multiline = false, rows, inputProps } = props as TextFormFieldProps;
  return (
    <GameTextField
      label={label}
      type={type}
      value={fieldValue || ''}
      onChange={e => handleChange?.(e.target.value)}
      onBlur={handleBlur}
      variant="outlined"
      fullWidth={fullWidth}
      error={fieldError}
      helperText={fieldHelperText}
      disabled={disabled}
      required={required}
      multiline={multiline}
      rows={rows}
      inputProps={inputProps}
      sx={sx}
    />
  );
};
