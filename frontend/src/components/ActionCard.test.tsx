import { render, screen, fireEvent } from '@testing-library/react';
import React from 'react';

import { ActionCard } from './ActionCard';

describe('ActionCard', () => {
  it('renders card with title', () => {
    render(<ActionCard title="Test Card" />);
    
    expect(screen.getByText('Test Card')).toBeInTheDocument();
  });

  it('renders subtitle when provided', () => {
    render(<ActionCard title="Test Card" subtitle="Test subtitle" />);
    
    expect(screen.getByText('Test subtitle')).toBeInTheDocument();
  });

  it('renders children content', () => {
    render(
      <ActionCard title="Test Card">
        <div>Card content</div>
      </ActionCard>
    );
    
    expect(screen.getByText('Card content')).toBeInTheDocument();
  });

  it('renders action buttons', () => {
    const actions = (
      <button>Edit</button>
    );
    
    render(<ActionCard title="Test Card" actions={actions} />);
    
    expect(screen.getByText('Edit')).toBeInTheDocument();
  });

  it('calls onClick when card is clicked and clickable', () => {
    const handleClick = jest.fn();
    
    render(
      <ActionCard
        title="Test Card"
        onClick={handleClick}
        clickable
      />
    );
    
    fireEvent.click(screen.getByText('Test Card'));
    expect(handleClick).toHaveBeenCalledTimes(1);
  });

  it('does not call onClick when card is not clickable', () => {
    const handleClick = jest.fn();
    
    render(
      <ActionCard
        title="Test Card"
        onClick={handleClick}
        clickable={false}
      />
    );
    
    fireEvent.click(screen.getByText('Test Card'));
    expect(handleClick).not.toHaveBeenCalled();
  });
});
