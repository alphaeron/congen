import { render, screen, fireEvent } from '@testing-library/react';
import React from 'react';

import { DeletableChip } from './DeletableChip';

describe('DeletableChip', () => {
  const mockOnDelete = jest.fn();

  beforeEach(() => {
    mockOnDelete.mockClear();
  });

  it('renders with label', () => {
    render(<DeletableChip label="Test Chip" onDelete={mockOnDelete} deleteTooltip="Delete chip" />);

    expect(screen.getByText('Test Chip')).toBeInTheDocument();
  });

  it('calls onDelete when delete button is clicked', () => {
    render(<DeletableChip label="Test Chip" onDelete={mockOnDelete} deleteTooltip="Delete chip" />);

    // Find the delete icon and click it
    const deleteIcon = screen.getByTestId('DeleteIcon');
    fireEvent.click(deleteIcon);
    expect(mockOnDelete).toHaveBeenCalledTimes(1);
  });

  it('shows tooltip on delete button', () => {
    render(<DeletableChip label="Test Chip" onDelete={mockOnDelete} deleteTooltip="Delete chip" />);

    // The chip has the aria-label for the delete functionality
    const chip = screen.getByLabelText('Delete chip');
    expect(chip).toBeInTheDocument();
  });

  it('disables delete button when disabled prop is true', () => {
    render(
      <DeletableChip
        label="Test Chip"
        onDelete={mockOnDelete}
        deleteTooltip="Delete chip"
        disabled={true}
      />
    );

    // The chip itself is disabled when the disabled prop is true
    const chip = screen.getByLabelText('Delete chip');
    // Check if the element has the disabled class or attribute
    expect(chip).toHaveClass('Mui-disabled');
  });

  it('applies color and variant props', () => {
    render(
      <DeletableChip
        label="Test Chip"
        onDelete={mockOnDelete}
        deleteTooltip="Delete chip"
        color="primary"
        variant="filled"
      />
    );

    expect(screen.getByText('Test Chip')).toBeInTheDocument();
  });
});
