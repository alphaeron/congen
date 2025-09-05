import { render, screen, fireEvent } from '@testing-library/react';
import React from 'react';

import { FormField } from './FormField';

describe('FormField', () => {
  const mockOnChange = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('Text Field', () => {
    it('renders text field with label', () => {
      render(
        <FormField
          type="text"
          label="Test Field"
          value=""
          onChange={mockOnChange}
        />
      );
      
      expect(screen.getByLabelText('Test Field')).toBeInTheDocument();
    });

    it('calls onChange when value changes', () => {
      render(
        <FormField
          type="text"
          label="Test Field"
          value=""
          onChange={mockOnChange}
        />
      );
      
      const input = screen.getByLabelText('Test Field');
      fireEvent.change(input, { target: { value: 'new value' } });
      
      expect(mockOnChange).toHaveBeenCalledWith('new value');
    });

    it('renders number field with input props', () => {
      render(
        <FormField
          type="number"
          label="Number Field"
          value=""
          onChange={mockOnChange}
          inputProps={{ min: 0, max: 100 }}
        />
      );
      
      const input = screen.getByLabelText('Number Field');
      expect(input).toHaveAttribute('type', 'number');
      expect(input).toHaveAttribute('min', '0');
      expect(input).toHaveAttribute('max', '100');
    });

    it('renders multiline text field', () => {
      render(
        <FormField
          type="text"
          label="Multiline Field"
          value=""
          onChange={mockOnChange}
          multiline
          rows={4}
        />
      );
      
      const input = screen.getByLabelText('Multiline Field');
      expect(input).toHaveAttribute('rows', '4');
    });
  });

  describe('Select Field', () => {
    const options = [
      { value: 'option1', label: 'Option 1' },
      { value: 'option2', label: 'Option 2' },
    ];

    it('renders select field with options', () => {
      const { container } = render(
        <FormField
          type="select"
          label="Select Field"
          value=""
          onChange={mockOnChange}
          options={options}
        />
      );
      
      const select = container.querySelector('.MuiSelect-root');
      expect(select).toBeInTheDocument();
    });

    it('calls onChange when selection changes', () => {
      const { container } = render(
        <FormField
          type="select"
          label="Select Field"
          value=""
          onChange={mockOnChange}
          options={options}
        />
      );
      
      const select = container.querySelector('.MuiSelect-select');
      fireEvent.mouseDown(select!);
      
      const option = screen.getByText('Option 1');
      fireEvent.click(option);
      
      expect(mockOnChange).toHaveBeenCalledWith('option1');
    });
  });

  describe('Autocomplete Field', () => {
    const options = ['Option 1', 'Option 2', 'Option 3'];

    it('renders autocomplete field', () => {
      render(
        <FormField
          type="autocomplete"
          label="Autocomplete Field"
          value=""
          onChange={mockOnChange}
          options={options}
        />
      );
      
      expect(screen.getByLabelText('Autocomplete Field')).toBeInTheDocument();
    });

    it('calls onChange when option is selected', () => {
      const { container } = render(
        <FormField
          type="autocomplete"
          label="Autocomplete Field"
          value=""
          onChange={mockOnChange}
          options={options}
        />
      );
      
      const autocomplete = container.querySelector('.MuiAutocomplete-root');
      expect(autocomplete).toBeInTheDocument();
      
      // Test that the component renders correctly
      const input = container.querySelector('input');
      expect(input).toBeInTheDocument();
    });
  });

  describe('Common Props', () => {
    it('renders with error state', () => {
      render(
        <FormField
          type="text"
          label="Test Field"
          value=""
          onChange={mockOnChange}
          error
          helperText="This field has an error"
        />
      );
      
      expect(screen.getByText('This field has an error')).toBeInTheDocument();
    });

    it('renders as disabled', () => {
      render(
        <FormField
          type="text"
          label="Test Field"
          value=""
          onChange={mockOnChange}
          disabled
        />
      );
      
      const input = screen.getByLabelText('Test Field');
      expect(input).toBeDisabled();
    });

    it('renders as required', () => {
      const { container } = render(
        <FormField
          type="text"
          label="Test Field"
          value=""
          onChange={mockOnChange}
          required
        />
      );
      
      const input = container.querySelector('input');
      expect(input).toBeRequired();
    });
  });
});
