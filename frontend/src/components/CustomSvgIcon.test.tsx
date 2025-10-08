import { render, screen } from '@testing-library/react';
import React from 'react';

import { CustomSvgIcon } from './CustomSvgIcon';

// Mock the SvgIcon component
jest.mock('@mui/material', () => ({
  ...jest.requireActual('@mui/material'),
  SvgIcon: ({ children, ...props }: { children: React.ReactNode; [key: string]: unknown }) => (
    <div data-testid="svg-icon" {...props}>
      {children}
    </div>
  ),
}));

describe('CustomSvgIcon', () => {
  const mockSrc = '/test-icon.svg';
  const mockAlt = 'Test Icon';

  it('renders with required props', () => {
    render(<CustomSvgIcon src={mockSrc} />);

    const svgIcon = screen.getByTestId('svg-icon');
    expect(svgIcon).toBeInTheDocument();
  });

  it('renders with alt prop', () => {
    render(<CustomSvgIcon src={mockSrc} alt={mockAlt} />);

    const svgIcon = screen.getByTestId('svg-icon');
    expect(svgIcon).toBeInTheDocument();
  });

  it('renders with custom sx prop', () => {
    const customSx = { color: 'red', fontSize: 24 };
    render(<CustomSvgIcon src={mockSrc} sx={customSx} />);

    const svgIcon = screen.getByTestId('svg-icon');
    expect(svgIcon).toBeInTheDocument();
  });

  it('renders with additional props', () => {
    render(<CustomSvgIcon src={mockSrc} className="custom-class" data-test="test-value" />);

    const svgIcon = screen.getByTestId('svg-icon');
    expect(svgIcon).toBeInTheDocument();
    expect(svgIcon).toHaveClass('custom-class');
    expect(svgIcon).toHaveAttribute('data-test', 'test-value');
  });

  it('renders image element with correct attributes', () => {
    render(<CustomSvgIcon src={mockSrc} />);

    // The image element doesn't have a role in the test environment, so we'll test it differently
    const svgIcon = screen.getByTestId('svg-icon');
    const image = svgIcon.querySelector('image');
    expect(image).toBeInTheDocument();
    expect(image).toHaveAttribute('href', mockSrc);
    expect(image).toHaveAttribute('width', '100%');
    expect(image).toHaveAttribute('height', '100%');
  });
});
