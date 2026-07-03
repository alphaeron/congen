import { render, screen, fireEvent } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import React from 'react';

import { NumericStepInput } from './NumericStepInput';

describe('NumericStepInput', () => {
  it('renders label and integrated stepper controls', () => {
    render(<NumericStepInput label="Sets" value={3} onChange={jest.fn()} />);

    expect(screen.getByText('Sets')).toBeInTheDocument();
    expect(screen.getByLabelText('Sets')).toHaveValue('3');
    expect(screen.getByLabelText('Decrease Sets')).toBeInTheDocument();
    expect(screen.getByLabelText('Increase Sets')).toBeInTheDocument();
  });

  it('increments and decrements the value', async () => {
    const user = userEvent.setup();
    const onChange = jest.fn();

    render(<NumericStepInput label="Reps" value={5} onChange={onChange} min={1} integer />);

    await user.click(screen.getByLabelText('Increase Reps'));
    expect(onChange).toHaveBeenCalledWith(6);

    await user.click(screen.getByLabelText('Decrease Reps'));
    expect(onChange).toHaveBeenCalledWith(4);
  });

  it('allows direct numeric input', () => {
    const onChange = jest.fn();

    render(<NumericStepInput label="Weight" value={100} onChange={onChange} step={0.5} />);

    const input = screen.getByLabelText('Weight');
    fireEvent.change(input, { target: { value: '135.5' } });

    expect(onChange).toHaveBeenCalledWith(135.5);
  });

  it('supports empty values when allowEmpty is true', () => {
    const onChange = jest.fn();

    render(
      <NumericStepInput label="Performed reps" value={8} onChange={onChange} allowEmpty integer />
    );

    const input = screen.getByLabelText('Performed reps');
    fireEvent.change(input, { target: { value: '' } });

    expect(onChange).toHaveBeenCalledWith(undefined);
  });

  it('renders suffix in the label', () => {
    render(<NumericStepInput label="Rest" suffix="sec" value={60} onChange={jest.fn()} />);

    expect(screen.getByText('Rest (sec)')).toBeInTheDocument();
  });
});
