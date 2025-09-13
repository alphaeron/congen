import { render, screen, fireEvent } from '@testing-library/react';
import React from 'react';

import { NavigationItem } from './NavigationItem';

describe('NavigationItem', () => {
  const mockOnClick = jest.fn();

  beforeEach(() => {
    mockOnClick.mockClear();
  });

  it('renders with label', () => {
    render(
      <NavigationItem
        label="Test Navigation"
        isActive={false}
        onClick={mockOnClick}
      />
    );

    expect(screen.getByText('Test Navigation')).toBeInTheDocument();
  });

  it('calls onClick when clicked', () => {
    render(
      <NavigationItem
        label="Test Navigation"
        isActive={false}
        onClick={mockOnClick}
      />
    );

    fireEvent.click(screen.getByText('Test Navigation'));
    expect(mockOnClick).toHaveBeenCalledTimes(1);
  });

  it('applies active styling when isActive is true', () => {
    render(
      <NavigationItem
        label="Test Navigation"
        isActive={true}
        onClick={mockOnClick}
      />
    );

    const element = screen.getByText('Test Navigation');
    expect(element).toHaveStyle({
      color: 'rgb(25, 118, 210)', // primary.main color
      fontWeight: 'bold',
      textDecoration: 'underline',
    });
  });

  it('applies inactive styling when isActive is false', () => {
    render(
      <NavigationItem
        label="Test Navigation"
        isActive={false}
        onClick={mockOnClick}
      />
    );

    const element = screen.getByText('Test Navigation');
    expect(element).toHaveStyle({
      fontWeight: 'normal',
      textDecoration: 'none',
    });
  });
});
