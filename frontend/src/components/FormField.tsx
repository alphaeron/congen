import { Autocomplete, Checkbox, Chip } from '@mui/material';
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
  multiple?: boolean;
  placeholder?: string;
}

type FormFieldProps = TextFormFieldProps | SelectFormFieldProps | AutocompleteFormFieldProps;

/**
 * Returns a validation error message when a required field value is empty.
 *
 * @param value The field value to validate
 * @return An error message when invalid, otherwise undefined
 */
function getRequiredFieldError(value: unknown): string | undefined {
  if (Array.isArray(value)) {
    if (value.length === 0) {
      return 'This field is required';
    }
    return undefined;
  }
  if (!value || (typeof value === 'string' && !value.trim())) {
    return 'This field is required';
  }
  return undefined;
}

/**
 * Reusable form field component with consistent styling and validation.
 *
 * Provides a unified interface for different types of form fields including
 * text inputs, selects, and autocomplete fields with consistent styling.
 * Supports both traditional controlled components and TanStack Form integration.
 * Autocomplete fields support single or multi-select with searchable options.
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
 * @param multiple Whether autocomplete allows selecting multiple options
 * @param placeholder Placeholder text for autocomplete search input
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

  const tanstackField =
    name && form
      ? useField({
          name,
          form,
          validators: {
            onChange: required
              ? (props: { value: unknown } | unknown) => {
                  const fieldVal =
                    props && typeof props === 'object' && 'value' in props
                      ? (props as { value: unknown }).value
                      : props;
                  return getRequiredFieldError(fieldVal);
                }
              : undefined,
          },
        })
      : null;

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
            <GameMenuItem key={String(option.value)} value={option.value as string | number}>
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
    const { options, getOptionLabel, multiple = false, placeholder } =
      props as AutocompleteFormFieldProps;
    const memoizedGetOptionLabel = useMemo(
      () => getOptionLabel || ((option: string) => option),
      [getOptionLabel]
    );
    const autocompleteValue = multiple
      ? Array.isArray(fieldValue)
        ? fieldValue
        : []
      : fieldValue || null;

    return (
      <Autocomplete
        multiple={multiple}
        options={options}
        value={autocompleteValue}
        onChange={(_, newValue) => handleChange?.(newValue)}
        onBlur={handleBlur}
        getOptionLabel={memoizedGetOptionLabel}
        disableCloseOnSelect={multiple}
        renderTags={
          multiple
            ? (selected, getTagProps) =>
                selected.map((option, index) => {
                  const { key, ...tagProps } = getTagProps({ index });
                  return (
                    <Chip
                      key={key}
                      variant="outlined"
                      label={memoizedGetOptionLabel(option)}
                      size="small"
                      color="primary"
                      {...tagProps}
                    />
                  );
                })
            : undefined
        }
        renderOption={(optionProps, option, { selected }) => (
          <li {...optionProps} key={option}>
            {multiple && <Checkbox style={{ marginRight: 8 }} checked={selected} />}
            {memoizedGetOptionLabel(option)}
          </li>
        )}
        renderInput={params => (
          <GameTextField
            {...params}
            label={label}
            variant="outlined"
            fullWidth={fullWidth}
            error={fieldError}
            helperText={fieldHelperText}
            disabled={disabled}
            required={multiple ? false : required}
            placeholder={placeholder}
            inputProps={{
              ...params.inputProps,
              ...(multiple ? { required: false } : {}),
            }}
            InputLabelProps={{
              ...params.InputLabelProps,
              required,
            }}
          />
        )}
        clearOnBlur={!multiple}
        selectOnFocus
        handleHomeEndKeys
        disabled={disabled}
      />
    );
  }

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
