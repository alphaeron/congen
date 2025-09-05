import { render, screen, fireEvent } from '@testing-library/react';
import React from 'react';

import { FormDialog } from './FormDialog';

describe('FormDialog', () => {
  const defaultProps = {
    open: true,
    onClose: jest.fn(),
    onSubmit: jest.fn(),
    title: 'Test Form',
    children: <div>Form content</div>,
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders dialog with title and children', () => {
    render(<FormDialog {...defaultProps} />);
    
    expect(screen.getByText('Test Form')).toBeInTheDocument();
    expect(screen.getByText('Form content')).toBeInTheDocument();
  });

  it('renders description when provided', () => {
    render(
      <FormDialog
        {...defaultProps}
        description="This is a test form"
      />
    );
    
    expect(screen.getByText('This is a test form')).toBeInTheDocument();
  });

  it('renders default button texts', () => {
    render(<FormDialog {...defaultProps} />);
    
    expect(screen.getByText('Cancel')).toBeInTheDocument();
    expect(screen.getByText('Submit')).toBeInTheDocument();
  });

  it('renders custom button texts', () => {
    render(
      <FormDialog
        {...defaultProps}
        submitText="Create"
        cancelText="Close"
      />
    );
    
    expect(screen.getByText('Close')).toBeInTheDocument();
    expect(screen.getByText('Create')).toBeInTheDocument();
  });

  it('calls onClose when cancel button is clicked', () => {
    render(<FormDialog {...defaultProps} />);
    
    fireEvent.click(screen.getByText('Cancel'));
    expect(defaultProps.onClose).toHaveBeenCalledTimes(1);
  });

  it('calls onSubmit when submit button is clicked', () => {
    render(<FormDialog {...defaultProps} />);
    
    fireEvent.click(screen.getByText('Submit'));
    expect(defaultProps.onSubmit).toHaveBeenCalledTimes(1);
  });

  it('disables submit button when disabled prop is true', () => {
    render(<FormDialog {...defaultProps} disabled />);
    
    expect(screen.getByText('Submit')).toBeDisabled();
  });

  it('disables buttons when loading', () => {
    render(<FormDialog {...defaultProps} loading />);
    
    expect(screen.getByText('Cancel')).toBeDisabled();
    expect(screen.getByText('Processing...')).toBeDisabled();
  });

  it('applies correct color to submit button', () => {
    render(<FormDialog {...defaultProps} submitColor="error" />);
    
    const submitButton = screen.getByText('Submit');
    expect(submitButton).toHaveClass('MuiButton-colorError');
  });
});
