import { render, screen, fireEvent } from '@testing-library/react';
import React from 'react';

import { ConfirmationDialog } from './ConfirmationDialog';

describe('ConfirmationDialog', () => {
  const defaultProps = {
    open: true,
    onClose: jest.fn(),
    onConfirm: jest.fn(),
    title: 'Test Dialog',
    message: 'Are you sure?',
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders dialog with title and message', () => {
    render(<ConfirmationDialog {...defaultProps} />);
    
    expect(screen.getByText('Test Dialog')).toBeInTheDocument();
    expect(screen.getByText('Are you sure?')).toBeInTheDocument();
  });

  it('renders default button texts', () => {
    render(<ConfirmationDialog {...defaultProps} />);
    
    expect(screen.getByText('Cancel')).toBeInTheDocument();
    expect(screen.getByText('Confirm')).toBeInTheDocument();
  });

  it('renders custom button texts', () => {
    render(
      <ConfirmationDialog
        {...defaultProps}
        confirmText="Delete"
        cancelText="Keep"
      />
    );
    
    expect(screen.getByText('Keep')).toBeInTheDocument();
    expect(screen.getByText('Delete')).toBeInTheDocument();
  });

  it('calls onClose when cancel button is clicked', () => {
    render(<ConfirmationDialog {...defaultProps} />);
    
    fireEvent.click(screen.getByText('Cancel'));
    expect(defaultProps.onClose).toHaveBeenCalledTimes(1);
  });

  it('calls onConfirm when confirm button is clicked', () => {
    render(<ConfirmationDialog {...defaultProps} />);
    
    fireEvent.click(screen.getByText('Confirm'));
    expect(defaultProps.onConfirm).toHaveBeenCalledTimes(1);
  });

  it('displays error message when provided', () => {
    render(
      <ConfirmationDialog
        {...defaultProps}
        error="Something went wrong"
      />
    );
    
    expect(screen.getByText('Something went wrong')).toBeInTheDocument();
  });

  it('disables buttons when loading', () => {
    render(<ConfirmationDialog {...defaultProps} loading />);
    
    expect(screen.getByText('Cancel')).toBeDisabled();
    expect(screen.getByText('Processing...')).toBeDisabled();
  });

  it('applies correct color to confirm button', () => {
    render(<ConfirmationDialog {...defaultProps} confirmColor="error" />);
    
    const confirmButton = screen.getByText('Confirm');
    expect(confirmButton).toHaveClass('MuiButton-colorError');
  });
});
