import { render, screen } from '@testing-library/react';
import React from 'react';

import { Hero } from './Hero';

// Mock useTypewriter hook
jest.mock('./useTypewriter', () => ({
  useTypewriter: jest.fn().mockReturnValue('Conjugate Method Programming, Without the Hastle'),
}));

describe('Hero component', () => {
  it('Renders the catch phrase', () => {
    render(<Hero />);
    expect(
      screen.getByText('Conjugate Method Programming, Without the Hastle')
    ).toBeInTheDocument();
  });
});
