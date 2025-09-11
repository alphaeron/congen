import React from 'react';
import {
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Typography,
} from '@mui/material';
import { useField } from '@tanstack/react-form';

interface FormFieldProps {
  name: string;
  form: any;
  type?: 'text' | 'email' | 'password' | 'select';
  label: string;
  required?: boolean;
  disabled?: boolean;
  fullWidth?: boolean;
  options?: Array<{ value: string; label: string }>;
  sx?: any;
}

/**
 * FormField component that integrates with TanStack Form for validation.
 * Provides consistent form field styling and validation across the Keycloak theme.
 */
export const FormField: React.FC<FormFieldProps> = ({
  name,
  form,
  type = 'text',
  label,
  required = false,
  disabled = false,
  fullWidth = true,
  options = [],
  sx,
}) => {
  const field = useField({
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
  });

  const fieldValue = field.state.value;
  const fieldError = field.state.meta.errors.length > 0;
  const fieldHelperText = field.state.meta.errors[0];

  if (type === 'select') {
    return (
      <FormControl fullWidth={fullWidth} error={fieldError} disabled={disabled} required={required} sx={sx}>
        <InputLabel>{label}</InputLabel>
        <Select
          value={fieldValue || ''}
          label={label}
          onChange={e => field.handleChange(e.target.value)}
          onBlur={field.handleBlur}
        >
          {options.map(option => (
            <MenuItem key={option.value} value={option.value}>
              {option.label}
            </MenuItem>
          ))}
        </Select>
        {fieldHelperText && (
          <Typography variant="caption" color="error" sx={{ mt: 0.5, ml: 1.75 }}>
            {fieldHelperText}
          </Typography>
        )}
      </FormControl>
    );
  }

  return (
    <TextField
      name={name}
      type={type}
      label={label}
      value={fieldValue || ''}
      onChange={e => field.handleChange(e.target.value)}
      onBlur={field.handleBlur}
      error={fieldError}
      helperText={fieldHelperText}
      required={required}
      disabled={disabled}
      fullWidth={fullWidth}
      sx={sx}
    />
  );
};
