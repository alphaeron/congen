import { render, screen } from '@testing-library/react';
import * as React from 'react';

import { FEATURE_ITEMS, Features } from './Features';

describe('Features component', () => {
  beforeEach(() => {
    render(<Features />);
  });

  it.each(FEATURE_ITEMS)("Renders the feature titled '$title'", ({ title, description }) => {
    expect(screen.getByText(title)).toBeInTheDocument();
    expect(screen.getByText(description)).toBeInTheDocument();
  });
});
