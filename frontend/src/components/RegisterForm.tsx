import React, { useState } from 'react';
import {
  Box,
  Button,
  TextField,
  Alert,
  CircularProgress,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Grid,
  SelectChangeEvent,
} from '@mui/material';
import { useAuth } from '../contexts/AuthContext';

interface RegisterFormProps {
  onSuccess?: () => void;
  onSwitchToLogin?: () => void;
}

export const RegisterForm: React.FC<RegisterFormProps> = ({ onSuccess, onSwitchToLogin }) => {
  const { register, isLoading, error, clearError } = useAuth();
  const [formData, setFormData] = useState({
    name: '',
    age: '',
    height: '',
    heightUnit: 'CM',
    weight: '',
    email: '',
    password: '',
    confirmPassword: '',
    unit: 'KG',
  });

  // Height conversion functions
  const cmToFeetInches = (cm: number): { feet: number; inches: number } => {
    const totalInches = cm / 2.54;
    const feet = Math.floor(totalInches / 12);
    const inches = Math.round(totalInches % 12);
    return { feet, inches };
  };

  const feetInchesToCm = (feet: number, inches: number): number => {
    return (feet * 12 + inches) * 2.54;
  };

  const handleHeightUnitChange = (e: SelectChangeEvent) => {
    const newUnit = e.target.value;
    const currentHeight = parseFloat(formData.height) || 0;

    if (formData.heightUnit === 'CM' && newUnit === 'FT_IN') {
      // Convert from cm to feet+inches
      const { feet, inches } = cmToFeetInches(currentHeight);
      setFormData({
        ...formData,
        heightUnit: newUnit,
        height: `${feet}'${inches}"`,
      });
    } else if (formData.heightUnit === 'FT_IN' && newUnit === 'CM') {
      // Convert from feet+inches to cm
      const match = formData.height.match(/(\d+)'(\d+)"/);
      if (match) {
        const feet = parseInt(match[1]);
        const inches = parseInt(match[2]);
        const cm = feetInchesToCm(feet, inches);
        setFormData({
          ...formData,
          heightUnit: newUnit,
          height: cm.toString(),
        });
      }
    } else {
      setFormData({
        ...formData,
        heightUnit: newUnit,
      });
    }
  };

  const handleHeightChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData({
      ...formData,
      height: e.target.value,
    });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    clearError();

    if (formData.password !== formData.confirmPassword) {
      return;
    }

    // Convert height to cm for backend
    let heightInCm = parseFloat(formData.height);
    if (formData.heightUnit === 'FT_IN') {
      const match = formData.height.match(/(\d+)'(\d+)"/);
      if (match) {
        const feet = parseInt(match[1]);
        const inches = parseInt(match[2]);
        heightInCm = feetInchesToCm(feet, inches);
      }
    }

    try {
      await register({
        name: formData.name,
        age: parseInt(formData.age),
        height: heightInCm,
        weight: parseFloat(formData.weight),
        email: formData.email,
        password: formData.password,
        unit: formData.unit,
      });
      onSuccess?.();
    } catch {
      // Error is handled by the auth context
    }
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData({
      ...formData,
      [e.target.name as string]: e.target.value,
    });
  };

  const handleSelectChange = (e: SelectChangeEvent) => {
    setFormData({
      ...formData,
      [e.target.name as string]: e.target.value,
    });
  };

  return (
    <Box component="form" onSubmit={handleSubmit} sx={{ mt: 1 }}>
      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      <TextField
        margin="normal"
        required
        fullWidth
        id="name"
        label="Full Name"
        name="name"
        autoComplete="name"
        autoFocus
        value={formData.name}
        onChange={handleInputChange}
        disabled={isLoading}
      />

      <TextField
        margin="normal"
        required
        fullWidth
        id="age"
        label="Age"
        name="age"
        type="number"
        value={formData.age}
        onChange={handleInputChange}
        disabled={isLoading}
      />

      <Grid container spacing={2}>
        <Grid item xs={8}>
          <TextField
            margin="normal"
            required
            fullWidth
            id="height"
            label={formData.heightUnit === 'CM' ? 'Height (cm)' : 'Height (feet\'inches")'}
            name="height"
            type={formData.heightUnit === 'CM' ? 'number' : 'text'}
            placeholder={formData.heightUnit === 'CM' ? '175' : '5\'10"'}
            value={formData.height}
            onChange={handleHeightChange}
            disabled={isLoading}
          />
        </Grid>
        <Grid item xs={4}>
          <FormControl fullWidth margin="normal">
            <InputLabel id="height-unit-label">Height Unit</InputLabel>
            <Select
              labelId="height-unit-label"
              id="heightUnit"
              name="heightUnit"
              value={formData.heightUnit}
              label="Height Unit"
              onChange={handleHeightUnitChange}
              disabled={isLoading}
            >
              <MenuItem value="CM">CM</MenuItem>
              <MenuItem value="FT_IN">Feet+Inches</MenuItem>
            </Select>
          </FormControl>
        </Grid>
      </Grid>

      <Grid container spacing={2}>
        <Grid item xs={8}>
          <TextField
            margin="normal"
            required
            fullWidth
            id="weight"
            label="Weight"
            name="weight"
            type="number"
            value={formData.weight}
            onChange={handleInputChange}
            disabled={isLoading}
          />
        </Grid>
        <Grid item xs={4}>
          <FormControl fullWidth margin="normal">
            <InputLabel id="unit-label">Unit</InputLabel>
            <Select
              labelId="unit-label"
              id="unit"
              name="unit"
              value={formData.unit}
              label="Unit"
              onChange={handleSelectChange}
              disabled={isLoading}
            >
              <MenuItem value="KG">KG</MenuItem>
              <MenuItem value="LBS">LBS</MenuItem>
            </Select>
          </FormControl>
        </Grid>
      </Grid>

      <TextField
        margin="normal"
        required
        fullWidth
        id="email"
        label="Email Address"
        name="email"
        autoComplete="email"
        type="email"
        value={formData.email}
        onChange={handleInputChange}
        disabled={isLoading}
      />

      <TextField
        margin="normal"
        required
        fullWidth
        name="password"
        label="Password"
        type="password"
        id="password"
        value={formData.password}
        onChange={handleInputChange}
        disabled={isLoading}
      />

      <TextField
        margin="normal"
        required
        fullWidth
        name="confirmPassword"
        label="Confirm Password"
        type="password"
        id="confirmPassword"
        value={formData.confirmPassword}
        onChange={handleInputChange}
        disabled={isLoading}
        error={formData.password !== formData.confirmPassword && formData.confirmPassword !== ''}
        helperText={
          formData.password !== formData.confirmPassword && formData.confirmPassword !== ''
            ? 'Passwords do not match'
            : ''
        }
      />

      <Button
        type="submit"
        fullWidth
        variant="contained"
        sx={{ mt: 3, mb: 2 }}
        disabled={isLoading || formData.password !== formData.confirmPassword}
      >
        {isLoading ? <CircularProgress size={24} /> : 'Create Account'}
      </Button>

      {onSwitchToLogin && (
        <Button fullWidth variant="text" onClick={onSwitchToLogin} disabled={isLoading}>
          Already have an account? Sign in
        </Button>
      )}
    </Box>
  );
};
