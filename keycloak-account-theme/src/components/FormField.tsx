import React from 'react';
import { useField } from '@tanstack/react-form';
import {
  GameTextField,
  GameFormControl,
  GameInputLabel,
  GameSelect,
  GameMenuItem,
  GameText,
} from './GameTheme';

interface FormFieldProps {
  name: string;
  form: unknown;
  type?: 'text' | 'email' | 'password' | 'select';
  label: string;
  required?: boolean;
  disabled?: boolean;
  fullWidth?: boolean;
  options?: Array<{ value: string; label: string }>;
  sx?: Record<string, unknown>;
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
      <GameFormControl
        fullWidth={fullWidth}
        error={fieldError}
        disabled={disabled}
        required={required}
        sx={sx}
      >
        <GameInputLabel>{label}</GameInputLabel>
        <GameSelect
          value={fieldValue || ''}
          label={label}
          onChange={e => field.handleChange(e.target.value)}
          onBlur={field.handleBlur}
        >
          {options.map(option => (
            <GameMenuItem key={option.value} value={option.value}>
              {option.label}
            </GameMenuItem>
          ))}
        </GameSelect>
        {fieldHelperText && (
          <GameText variant="caption" textVariant="error" sx={{ mt: 0.5, ml: 1.75 }}>
            {fieldHelperText}
          </GameText>
        )}
      </GameFormControl>
    );
  }

  return (
    <GameTextField
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
