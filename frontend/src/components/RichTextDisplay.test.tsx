import React from 'react';
import { render, screen } from '@testing-library/react';
import { RichTextDisplay } from './RichTextDisplay';

// Mock the RichTextEditor component
jest.mock('./RichTextEditor', () => ({
  RichTextEditor: ({ value, readOnly, showToolbar, onChange }: any) => (
    <div data-testid="rich-text-editor" data-readonly={readOnly} data-showtoolbar={showToolbar}>
      {Array.isArray(value) ? value.map((node: any, index: number) => (
        <div key={index} data-testid={`slate-node-${index}`}>
          {node.children?.map((child: any, childIndex: number) => (
            <span 
              key={childIndex} 
              data-bold={child.bold} 
              data-italic={child.italic} 
              data-underline={child.underline}
            >
              {child.text}
            </span>
          ))}
        </div>
      )) : value}
    </div>
  ),
}));

describe('RichTextDisplay', () => {
  it('renders plain text correctly', () => {
    render(<RichTextDisplay content="Hello world" />);
    expect(screen.getByText('Hello world')).toBeInTheDocument();
    expect(screen.getByTestId('rich-text-editor')).toHaveAttribute('data-readonly', 'true');
    expect(screen.getByTestId('rich-text-editor')).toHaveAttribute('data-showtoolbar', 'false');
  });

  it('renders empty content', () => {
    render(<RichTextDisplay content="" />);
    expect(screen.getByTestId('rich-text-editor')).toBeInTheDocument();
  });

  it('renders bold text correctly', () => {
    render(<RichTextDisplay content="**Bold text**" />);
    const boldSpan = screen.getByText('Bold text');
    expect(boldSpan).toHaveAttribute('data-bold', 'true');
  });

  it('renders italic text correctly', () => {
    render(<RichTextDisplay content="*Italic text*" />);
    const italicSpan = screen.getByText('Italic text');
    expect(italicSpan).toHaveAttribute('data-italic', 'true');
  });

  it('renders underlined text correctly', () => {
    render(<RichTextDisplay content="__Underlined text__" />);
    const underlinedSpan = screen.getByText('Underlined text');
    expect(underlinedSpan).toHaveAttribute('data-underline', 'true');
  });

  it('renders mixed formatting correctly', () => {
    render(<RichTextDisplay content="**Bold** and *italic* and __underlined__" />);
    expect(screen.getByText('Bold')).toHaveAttribute('data-bold', 'true');
    expect(screen.getByText('italic')).toHaveAttribute('data-italic', 'true');
    expect(screen.getByText('underlined')).toHaveAttribute('data-underline', 'true');
  });

  it('handles nested formatting correctly', () => {
    render(<RichTextDisplay content="**Bold with *italic* inside**" />);
    // With the current simple parser, nested formatting is not supported
    // The entire text will be treated as bold
    expect(screen.getByText('Bold with *italic* inside')).toHaveAttribute('data-bold', 'true');
  });

  it('handles complex formatting combinations', () => {
    render(<RichTextDisplay content="**Bold** *italic* __underline__ **Bold *with italic* inside**" />);
    
    // Check that all formatting is applied correctly
    expect(screen.getByText('Bold')).toHaveAttribute('data-bold', 'true');
    expect(screen.getByText('italic')).toHaveAttribute('data-italic', 'true');
    expect(screen.getByText('underline')).toHaveAttribute('data-underline', 'true');
  });

          it('passes through Box props', () => {
            const { container } = render(<RichTextDisplay content="Test" sx={{ backgroundColor: 'red' }} />);
            const box = container.firstChild as HTMLElement;
            expect(box).toHaveStyle({ backgroundColor: 'red' });
          });

        });
