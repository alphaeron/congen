import { render, screen, fireEvent, act, waitFor } from '@testing-library/react';
import React from 'react';
import { useForm } from '@tanstack/react-form';

import { FormField } from './FormField';

// Mock form for testing
const MockFormProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const form = useForm({
    defaultValues: {
      testField: '',
      email: '',
      password: '',
      selectField: '',
      requiredField: '',
      disabledField: '',
      styledField: '',
      fullWidthField: '',
      notFullWidthField: '',
    },
  });

  return (
    <div data-testid="form-provider">
      {React.Children.map(children, child =>
        React.isValidElement(child) ? React.cloneElement(child, { form }) : child
      )}
    </div>
  );
};

describe('FormField', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders text field with default props', () => {
    render(
      <MockFormProvider>
        <FormField name="testField" label="Test Label" />
      </MockFormProvider>
    );

    expect(screen.getByLabelText('Test Label')).toBeInTheDocument();
    expect(screen.getByDisplayValue('')).toBeInTheDocument();
  });

  it('renders email field', () => {
    render(
      <MockFormProvider>
        <FormField name="email" type="email" label="Email" />
      </MockFormProvider>
    );

    const input = screen.getByLabelText('Email');
    expect(input).toHaveAttribute('type', 'email');
  });

  it('renders password field', () => {
    render(
      <MockFormProvider>
        <FormField name="password" type="password" label="Password" />
      </MockFormProvider>
    );

    const input = screen.getByLabelText('Password');
    expect(input).toHaveAttribute('type', 'password');
  });

  it('renders select field with options', async () => {
    const options = [
      { value: 'option1', label: 'Option 1' },
      { value: 'option2', label: 'Option 2' },
    ];

    render(
      <MockFormProvider>
        <FormField name="selectField" type="select" label="Select Option" options={options} />
      </MockFormProvider>
    );

    await waitFor(() => {
      expect(screen.getByRole('combobox')).toBeInTheDocument();
    });

    // Check that the label is present
    expect(screen.getAllByText('Select Option')).toHaveLength(2); // Label and legend
  });

  it('shows required indicator when required is true', () => {
    render(
      <MockFormProvider>
        <FormField name="requiredField" label="Required Field" required />
      </MockFormProvider>
    );

    const input = screen.getByDisplayValue('');
    expect(input).toBeRequired();
  });

  it('disables field when disabled is true', () => {
    render(
      <MockFormProvider>
        <FormField name="disabledField" label="Disabled Field" disabled />
      </MockFormProvider>
    );

    const input = screen.getByLabelText('Disabled Field');
    expect(input).toBeDisabled();
  });

  it('handles input change', async () => {
    render(
      <MockFormProvider>
        <FormField name="testField" label="Test Label" />
      </MockFormProvider>
    );

    const input = screen.getByLabelText('Test Label');

    await act(async () => {
      fireEvent.change(input, { target: { value: 'test value' } });
    });

    await waitFor(() => {
      expect(input).toHaveValue('test value');
    });
  });

  it('applies custom sx styles', () => {
    const customSx = { marginTop: 2 };

    render(
      <MockFormProvider>
        <FormField name="styledField" label="Styled Field" sx={customSx} />
      </MockFormProvider>
    );

    const input = screen.getByLabelText('Styled Field');
    expect(input).toBeInTheDocument();
  });

  it('renders with fullWidth by default', () => {
    render(
      <MockFormProvider>
        <FormField name="fullWidthField" label="Full Width Field" />
      </MockFormProvider>
    );

    const input = screen.getByLabelText('Full Width Field');
    expect(input).toBeInTheDocument();
  });

  it('renders without fullWidth when fullWidth is false', () => {
    render(
      <MockFormProvider>
        <FormField name="notFullWidthField" label="Not Full Width Field" fullWidth={false} />
      </MockFormProvider>
    );

    const input = screen.getByLabelText('Not Full Width Field');
    expect(input).toBeInTheDocument();
  });
});
