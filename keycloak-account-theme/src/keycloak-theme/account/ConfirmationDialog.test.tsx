import { render, screen, fireEvent } from '@testing-library/react';
import React from 'react';

import { ConfirmationDialog } from './ConfirmationDialog';

describe('ConfirmationDialog', () => {
  const defaultProps = {
    open: true,
    onClose: jest.fn(),
    onConfirm: jest.fn(),
    title: 'Test Dialog',
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders dialog with title', () => {
    render(<ConfirmationDialog {...defaultProps} />);

    expect(screen.getByText('Test Dialog')).toBeInTheDocument();
    expect(screen.getByRole('dialog')).toBeInTheDocument();
  });

  it('renders with message', () => {
    render(<ConfirmationDialog {...defaultProps} message="Are you sure?" />);

    expect(screen.getByText('Are you sure?')).toBeInTheDocument();
  });

  it('renders with default button texts', () => {
    render(<ConfirmationDialog {...defaultProps} />);

    expect(screen.getByText('Cancel')).toBeInTheDocument();
    expect(screen.getByText('Confirm')).toBeInTheDocument();
  });

  it('renders with custom button texts', () => {
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
    render(<ConfirmationDialog {...defaultProps} error="Something went wrong" />);

    expect(screen.getByText('Something went wrong')).toBeInTheDocument();
  });

  it('disables buttons when loading', () => {
    render(<ConfirmationDialog {...defaultProps} loading />);

    expect(screen.getByText('Cancel')).toBeDisabled();
    expect(screen.getByText('Processing...')).toBeDisabled();
  });

  it('disables confirm button when disabled prop is true', () => {
    render(<ConfirmationDialog {...defaultProps} disabled />);

    expect(screen.getByText('Confirm')).toBeDisabled();
    expect(screen.getByText('Cancel')).not.toBeDisabled();
  });

  it('applies correct color to confirm button', () => {
    render(<ConfirmationDialog {...defaultProps} confirmColor="error" />);

    const confirmButton = screen.getByText('Confirm');
    expect(confirmButton).toHaveClass('MuiButton-colorError');
  });

  it('renders children content', () => {
    render(
      <ConfirmationDialog {...defaultProps}>
        <div data-testid="custom-content">Custom content</div>
      </ConfirmationDialog>
    );

    expect(screen.getByTestId('custom-content')).toBeInTheDocument();
    expect(screen.getByText('Custom content')).toBeInTheDocument();
  });

  it('does not render when open is false', () => {
    render(<ConfirmationDialog {...defaultProps} open={false} />);

    expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
  });

  it('renders with all props', () => {
    render(
      <ConfirmationDialog
        {...defaultProps}
        message="Custom message"
        confirmText="Delete"
        cancelText="Keep"
        confirmColor="error"
        loading={false}
        error="Test error"
        disabled={false}
      >
        <div data-testid="child">Child content</div>
      </ConfirmationDialog>
    );

    expect(screen.getByText('Test Dialog')).toBeInTheDocument();
    expect(screen.getByText('Custom message')).toBeInTheDocument();
    expect(screen.getByText('Delete')).toBeInTheDocument();
    expect(screen.getByText('Keep')).toBeInTheDocument();
    expect(screen.getByText('Test error')).toBeInTheDocument();
    expect(screen.getByTestId('child')).toBeInTheDocument();
  });
});
