import { render, screen } from '@testing-library/react';
import React from 'react';

import { EmptyState } from './EmptyState';

describe('EmptyState', () => {
  it('renders empty state with title and message', () => {
    render(<EmptyState title="No Data" message="There is no data to display" />);

    expect(screen.getByText('No Data')).toBeInTheDocument();
    expect(screen.getByText('There is no data to display')).toBeInTheDocument();
  });

  it('renders action button when provided', () => {
    const action = <button>Add Item</button>;

    render(<EmptyState title="No Data" message="There is no data to display" action={action} />);

    expect(screen.getByText('Add Item')).toBeInTheDocument();
  });

  it('renders as card variant by default', () => {
    render(<EmptyState title="No Data" message="There is no data to display" />);

    // Card variant should be the default
    expect(screen.getByText('No Data')).toBeInTheDocument();
  });

  it('renders as paper variant when specified', () => {
    render(<EmptyState title="No Data" message="There is no data to display" variant="paper" />);

    expect(screen.getByText('No Data')).toBeInTheDocument();
  });
});
