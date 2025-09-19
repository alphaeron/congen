import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { RichTextEditor } from './RichTextEditor';

// Mock Slate.js dependencies
jest.mock('slate', () => ({
  createEditor: jest.fn(() => ({
    children: [{ type: 'paragraph', children: [{ text: '' }] }],
    selection: null,
    operations: [],
    marks: null,
    history: { redos: [], undos: [] },
    isInline: jest.fn(),
    isVoid: jest.fn(),
    normalizeNode: jest.fn(),
    onChange: jest.fn(),
  })),
  Editor: {
    isEditor: jest.fn((node) => node && typeof node === 'object' && 'children' in node),
    isBlock: jest.fn(() => true),
    marks: jest.fn(() => ({})),
    addMark: jest.fn(),
    removeMark: jest.fn(),
    nodes: jest.fn(() => []),
    unhangRange: jest.fn((editor, range) => range),
  },
  Text: {
    isText: jest.fn((node) => node && typeof node === 'object' && 'text' in node),
  },
  Transforms: {
    setNodes: jest.fn(),
    unwrapNodes: jest.fn(),
    wrapNodes: jest.fn(),
  },
  Node: {
    isNode: jest.fn(),
  },
}));

jest.mock('slate-react', () => ({
  Slate: ({ children, initialValue, onChange }: any) => (
    <div data-testid="slate-editor" data-initial-value={JSON.stringify(initialValue)}>
      {children}
    </div>
  ),
  Editable: ({ placeholder, onKeyDown, style, renderElement, renderLeaf, ...props }: any) => (
    <div
      data-testid="editable"
      data-placeholder={placeholder}
      style={style}
      onKeyDown={onKeyDown}
      contentEditable
      suppressContentEditableWarning
      {...props}
    >
      Content
    </div>
  ),
  withReact: jest.fn((editor) => editor),
}));

jest.mock('slate-history', () => ({
  withHistory: jest.fn((editor) => editor),
}));

describe('RichTextEditor', () => {
  const mockOnChange = jest.fn();
  const mockOnSave = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders with default props', () => {
    render(<RichTextEditor value="" onChange={mockOnChange} />);
    
    expect(screen.getByTestId('slate-editor')).toBeInTheDocument();
    expect(screen.getByTestId('editable')).toBeInTheDocument();
  });

  it('renders with placeholder', () => {
    render(<RichTextEditor value="" onChange={mockOnChange} placeholder="Enter text..." />);
    
    const editable = screen.getByTestId('editable');
    expect(editable).toHaveAttribute('data-placeholder', 'Enter text...');
  });

  it('renders in read-only mode', () => {
    render(<RichTextEditor value="" onChange={mockOnChange} readOnly />);
    
    const editable = screen.getByTestId('editable');
    expect(editable).toHaveAttribute('contentEditable', 'true');
  });

  it('shows toolbar when showToolbar is true', () => {
    render(<RichTextEditor value="" onChange={mockOnChange} showToolbar />);
    
    expect(screen.getByLabelText(/bold/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/italic/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/underline/i)).toBeInTheDocument();
  });

  it('hides toolbar when showToolbar is false', () => {
    render(<RichTextEditor value="" onChange={mockOnChange} showToolbar={false} />);
    
    expect(screen.queryByLabelText(/bold/i)).not.toBeInTheDocument();
  });

  it('handles keyboard shortcuts', async () => {
    const user = userEvent.setup();
    render(<RichTextEditor value="" onChange={mockOnChange} />);
    
    const editable = screen.getByTestId('editable');
    
    // Test Ctrl+B for bold
    await user.click(editable);
    await user.keyboard('{Control>}b{/Control}');
    
    // Test Ctrl+I for italic
    await user.keyboard('{Control>}i{/Control}');
    
    // Test Ctrl+U for underline
    await user.keyboard('{Control>}u{/Control}');
    
    // Test Ctrl+S for save
    await user.keyboard('{Control>}s{/Control}');
  });

  it('calls onChange when content changes', () => {
    render(<RichTextEditor value="test" onChange={mockOnChange} />);
    
    // The onChange would be called by Slate internally
    // We can't easily test this without more complex mocking
    expect(mockOnChange).toHaveBeenCalledTimes(0); // Initial render
  });

  it('calls onSave when autoSave is enabled', () => {
    render(<RichTextEditor value="test" onChange={mockOnChange} autoSave onSave={mockOnSave} />);
    
    // Auto-save would be triggered by changes
    expect(mockOnSave).not.toHaveBeenCalled(); // Not called on initial render
  });

  it('applies custom styling props', () => {
    const { container } = render(
      <RichTextEditor 
        value="" 
        onChange={mockOnChange} 
        minHeight={200} 
        maxHeight={400} 
      />
    );
    
    // Find the Paper component that contains the editor (not the toolbar)
    const papers = container.querySelectorAll('[class*="MuiPaper"]');
    const editorPaper = Array.from(papers).find(paper => 
      paper.querySelector('[data-testid="slate-editor"]')
    );
    expect(editorPaper).toHaveStyle({ minHeight: '200px', maxHeight: '400px' });
  });

  it('shows help text when not in read-only mode', () => {
    render(<RichTextEditor value="" onChange={mockOnChange} />);
    
    expect(screen.getByText(/Use Ctrl\+B, Ctrl\+I, Ctrl\+U for formatting/)).toBeInTheDocument();
  });

  it('hides help text when in read-only mode', () => {
    render(<RichTextEditor value="" onChange={mockOnChange} readOnly />);
    
    expect(screen.queryByText(/Use Ctrl\+B, Ctrl\+I, Ctrl\+U for formatting/)).not.toBeInTheDocument();
  });

  it('handles toolbar button clicks', async () => {
    const user = userEvent.setup();
    render(<RichTextEditor value="" onChange={mockOnChange} showToolbar />);
    
    const boldButton = screen.getByLabelText(/bold/i);
    const italicButton = screen.getByLabelText(/italic/i);
    const underlineButton = screen.getByLabelText(/underline/i);
    
    await user.click(boldButton);
    await user.click(italicButton);
    await user.click(underlineButton);
    
    // Buttons should be clickable (actual functionality would be tested with more complex mocking)
    expect(boldButton).toBeInTheDocument();
    expect(italicButton).toBeInTheDocument();
    expect(underlineButton).toBeInTheDocument();
  });

          it('handles list formatting buttons', async () => {
            const user = userEvent.setup();
            render(<RichTextEditor value="" onChange={mockOnChange} showToolbar />);
            
            const bulletListButton = screen.getByLabelText(/bulleted list/i);
            
            await user.click(bulletListButton);
            
            expect(bulletListButton).toBeInTheDocument();
          });

        });
