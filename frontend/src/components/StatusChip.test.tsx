import { render, screen } from '@testing-library/react';
import React from 'react';

import { StatusChip } from './StatusChip';

describe('StatusChip', () => {
  it('renders chip with label', () => {
    render(<StatusChip label="Active" status="active" />);
    
    expect(screen.getByText('Active')).toBeInTheDocument();
  });

  it('applies success color for active status', () => {
    const { container } = render(<StatusChip label="Active" status="active" />);
    
    const chip = container.querySelector('.MuiChip-root');
    expect(chip).toHaveClass('MuiChip-colorSuccess');
  });

  it('applies success color for success status', () => {
    const { container } = render(<StatusChip label="Success" status="success" />);
    
    const chip = container.querySelector('.MuiChip-root');
    expect(chip).toHaveClass('MuiChip-colorSuccess');
  });

  it('applies default color for inactive status', () => {
    const { container } = render(<StatusChip label="Inactive" status="inactive" />);
    
    const chip = container.querySelector('.MuiChip-root');
    expect(chip).toHaveClass('MuiChip-colorDefault');
  });

  it('applies warning color for warning status', () => {
    const { container } = render(<StatusChip label="Warning" status="warning" />);
    
    const chip = container.querySelector('.MuiChip-root');
    expect(chip).toHaveClass('MuiChip-colorWarning');
  });

  it('applies error color for error status', () => {
    const { container } = render(<StatusChip label="Error" status="error" />);
    
    const chip = container.querySelector('.MuiChip-root');
    expect(chip).toHaveClass('MuiChip-colorError');
  });

  it('applies info color for info status', () => {
    const { container } = render(<StatusChip label="Info" status="info" />);
    
    const chip = container.querySelector('.MuiChip-root');
    expect(chip).toHaveClass('MuiChip-colorInfo');
  });

  it('renders with small size by default', () => {
    const { container } = render(<StatusChip label="Test" status="active" />);
    
    const chip = container.querySelector('.MuiChip-root');
    expect(chip).toHaveClass('MuiChip-sizeSmall');
  });

  it('renders with medium size when specified', () => {
    const { container } = render(<StatusChip label="Test" status="active" size="medium" />);
    
    const chip = container.querySelector('.MuiChip-root');
    expect(chip).toHaveClass('MuiChip-sizeMedium');
  });

  it('renders with filled variant by default', () => {
    const { container } = render(<StatusChip label="Test" status="active" />);
    
    const chip = container.querySelector('.MuiChip-root');
    expect(chip).toHaveClass('MuiChip-filled');
  });

  it('renders with outlined variant when specified', () => {
    const { container } = render(<StatusChip label="Test" status="active" variant="outlined" />);
    
    const chip = container.querySelector('.MuiChip-root');
    expect(chip).toHaveClass('MuiChip-outlined');
  });
});
