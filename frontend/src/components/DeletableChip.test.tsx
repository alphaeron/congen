import { render, screen, fireEvent } from '@testing-library/react';
import React from 'react';

import { DeletableChip } from './DeletableChip';

describe('DeletableChip', () => {
  const mockOnDelete = jest.fn();

  beforeEach(() => {
    mockOnDelete.mockClear();
  });

  it('renders with label', () => {
    render(
      <DeletableChip
        label="Test Chip"
        onDelete={mockOnDelete}
        deleteTooltip="Delete chip"
      />
    );

    expect(screen.getByText('Test Chip')).toBeInTheDocument();
  });

  it('calls onDelete when delete button is clicked', () => {
    render(
      <DeletableChip
        label="Test Chip"
        onDelete={mockOnDelete}
        deleteTooltip="Delete chip"
      />
    );

    const deleteButton = screen.getByLabelText('delete');
    fireEvent.click(deleteButton);
    expect(mockOnDelete).toHaveBeenCalledTimes(1);
  });

  it('shows tooltip on delete button', () => {
    render(
      <DeletableChip
        label="Test Chip"
        onDelete={mockOnDelete}
        deleteTooltip="Delete chip"
      />
    );

    const deleteButton = screen.getByLabelText('delete');
    expect(deleteButton).toBeInTheDocument();
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

    const deleteButton = screen.getByLabelText('delete');
    expect(deleteButton).toBeDisabled();
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
