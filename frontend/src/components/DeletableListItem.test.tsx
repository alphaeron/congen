import { render, screen, fireEvent } from '@testing-library/react';
import React from 'react';

import { DeletableListItem } from './DeletableListItem';

describe('DeletableListItem', () => {
  const mockOnDelete = jest.fn();

  beforeEach(() => {
    mockOnDelete.mockClear();
  });

  it('renders with primary text', () => {
    render(
      <DeletableListItem
        primary="Test Item"
        onDelete={mockOnDelete}
        deleteTooltip="Delete item"
      />
    );

    expect(screen.getByText('Test Item')).toBeInTheDocument();
  });

  it('renders with secondary text when provided', () => {
    render(
      <DeletableListItem
        primary="Test Item"
        secondary="Secondary text"
        onDelete={mockOnDelete}
        deleteTooltip="Delete item"
      />
    );

    expect(screen.getByText('Test Item')).toBeInTheDocument();
    expect(screen.getByText('Secondary text')).toBeInTheDocument();
  });

  it('calls onDelete when delete button is clicked', () => {
    render(
      <DeletableListItem
        primary="Test Item"
        onDelete={mockOnDelete}
        deleteTooltip="Delete item"
      />
    );

    const deleteButton = screen.getByLabelText('delete');
    fireEvent.click(deleteButton);
    expect(mockOnDelete).toHaveBeenCalledTimes(1);
  });

  it('shows tooltip on delete button', () => {
    render(
      <DeletableListItem
        primary="Test Item"
        onDelete={mockOnDelete}
        deleteTooltip="Delete item"
      />
    );

    const deleteButton = screen.getByLabelText('delete');
    expect(deleteButton).toBeInTheDocument();
  });

  it('disables delete button when disabled prop is true', () => {
    render(
      <DeletableListItem
        primary="Test Item"
        onDelete={mockOnDelete}
        deleteTooltip="Delete item"
        disabled={true}
      />
    );

    const deleteButton = screen.getByLabelText('delete');
    expect(deleteButton).toBeDisabled();
  });
});
