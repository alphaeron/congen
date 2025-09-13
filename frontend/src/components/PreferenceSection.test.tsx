import { render, screen, fireEvent } from '@testing-library/react';
import React from 'react';

import { PreferenceSection } from './PreferenceSection';

describe('PreferenceSection', () => {
  const mockOnAddClick = jest.fn();

  beforeEach(() => {
    mockOnAddClick.mockClear();
  });

  it('renders with title, description, and add button', () => {
    render(
      <PreferenceSection
        title="Test Section"
        description="Test description"
        addButtonText="Add Item"
        onAddClick={mockOnAddClick}
        hasItems={false}
        emptyMessage="No items"
      >
        <div>Test content</div>
      </PreferenceSection>
    );

    expect(screen.getByText('Test Section')).toBeInTheDocument();
    expect(screen.getByText('Test description')).toBeInTheDocument();
    expect(screen.getByText('Add Item')).toBeInTheDocument();
  });

  it('calls onAddClick when add button is clicked', () => {
    render(
      <PreferenceSection
        title="Test Section"
        description="Test description"
        addButtonText="Add Item"
        onAddClick={mockOnAddClick}
        hasItems={false}
        emptyMessage="No items"
      >
        <div>Test content</div>
      </PreferenceSection>
    );

    fireEvent.click(screen.getByText('Add Item'));
    expect(mockOnAddClick).toHaveBeenCalledTimes(1);
  });

  it('shows empty message when hasItems is false', () => {
    render(
      <PreferenceSection
        title="Test Section"
        description="Test description"
        addButtonText="Add Item"
        onAddClick={mockOnAddClick}
        hasItems={false}
        emptyMessage="No items found"
      >
        <div>Test content</div>
      </PreferenceSection>
    );

    expect(screen.getByText('No items found')).toBeInTheDocument();
  });

  it('shows children when hasItems is true', () => {
    render(
      <PreferenceSection
        title="Test Section"
        description="Test description"
        addButtonText="Add Item"
        onAddClick={mockOnAddClick}
        hasItems={true}
        emptyMessage="No items found"
      >
        <div>Test content</div>
      </PreferenceSection>
    );

    expect(screen.getByText('Test content')).toBeInTheDocument();
    expect(screen.queryByText('No items found')).not.toBeInTheDocument();
  });
});
