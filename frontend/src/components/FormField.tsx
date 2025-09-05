import { TextField, FormControl, InputLabel, Select, MenuItem, Autocomplete } from '@mui/material';
import React, { useMemo } from 'react';

interface BaseFormFieldProps {
  label: string;
  value: any;
  onChange: (value: any) => void;
  error?: boolean;
  helperText?: string;
  disabled?: boolean;
  required?: boolean;
  fullWidth?: boolean;
}

interface TextFormFieldProps extends BaseFormFieldProps {
  type: 'text' | 'number' | 'email' | 'password';
  multiline?: boolean;
  rows?: number;
  inputProps?: Record<string, any>;
}

interface SelectFormFieldProps extends BaseFormFieldProps {
  type: 'select';
  options: Array<{ value: any; label: string }>;
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
 *
 * @param type Type of form field ('text', 'number', 'email', 'password', 'select', 'autocomplete')
 * @param label Field label
 * @param value Current field value
 * @param onChange Function to call when value changes
 * @param error Whether the field has an error
 * @param helperText Helper text to display below the field
 * @param disabled Whether the field is disabled
 * @param required Whether the field is required
 * @param fullWidth Whether the field should take full width
 * @param multiline Whether text field should be multiline (text fields only)
 * @param rows Number of rows for multiline text fields
 * @param inputProps Additional props for the input element
 * @param options Options for select/autocomplete fields
 * @param getOptionLabel Function to get display label for autocomplete options
 * @return Form field component
 */
export const FormField: React.FC<FormFieldProps> = (props) => {
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
  } = props;

  if (type === 'select') {
    const { options } = props as SelectFormFieldProps;
    return (
      <FormControl fullWidth={fullWidth} error={error} disabled={disabled} required={required}>
        <InputLabel>{label}</InputLabel>
        <Select
          value={value}
          label={label}
          onChange={(e) => onChange(e.target.value)}
        >
          {options.map((option) => (
            <MenuItem key={option.value} value={option.value}>
              {option.label}
            </MenuItem>
          ))}
        </Select>
        {helperText && <Typography variant="caption" color={error ? 'error' : 'text.secondary'}>{helperText}</Typography>}
      </FormControl>
    );
  }

  if (type === 'autocomplete') {
    const { options, getOptionLabel } = props as AutocompleteFormFieldProps;
    const memoizedGetOptionLabel = useMemo(() => 
      getOptionLabel || ((option: string) => option), 
      [getOptionLabel]
    );
    return (
      <Autocomplete
        options={options}
        value={value}
        onChange={(_, newValue) => onChange(newValue)}
        getOptionLabel={memoizedGetOptionLabel}
        renderInput={(params) => (
          <TextField
            {...params}
            label={label}
            variant="outlined"
            fullWidth={fullWidth}
            error={error}
            helperText={helperText}
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
    <TextField
      label={label}
      type={type}
      value={value}
      onChange={(e) => onChange(e.target.value)}
      variant="outlined"
      fullWidth={fullWidth}
      error={error}
      helperText={helperText}
      disabled={disabled}
      required={required}
      multiline={multiline}
      rows={rows}
      inputProps={inputProps}
    />
  );
};
