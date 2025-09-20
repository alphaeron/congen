import {
  FormatBold,
  FormatItalic,
  FormatUnderlined,
  FormatListBulleted,
  Save,
} from '@mui/icons-material';
import { Box, IconButton, Toolbar, Tooltip, Typography, Paper, Divider } from '@mui/material';
import React, { useCallback, useMemo } from 'react';
import { createEditor, Editor, Transforms, Text } from 'slate';
import { withHistory } from 'slate-history';
import { Slate, Editable, withReact } from 'slate-react';

import type { Descendant } from 'slate';
import type { RenderElementProps, RenderLeafProps } from 'slate-react';

// Define custom types for our text formatting
interface CustomText {
  text: string;
  bold?: boolean;
  italic?: boolean;
  underline?: boolean;
}

interface CustomElement {
  type: 'paragraph' | 'bulleted-list' | 'list-item';
  children: CustomText[];
}

// Define our own custom set of helpers.
interface TextMarks {
  bold?: boolean;
  italic?: boolean;
  underline?: boolean;
}

const CustomEditor = {
  isBoldMarkActive(editor: Editor) {
    const marks = Editor.marks(editor) as TextMarks;
    return marks ? marks.bold === true : false;
  },

  isItalicMarkActive(editor: Editor) {
    const marks = Editor.marks(editor) as TextMarks;
    return marks ? marks.italic === true : false;
  },

  isUnderlineMarkActive(editor: Editor) {
    const marks = Editor.marks(editor) as TextMarks;
    return marks ? marks.underline === true : false;
  },

  toggleBoldMark(editor: Editor) {
    const isActive = CustomEditor.isBoldMarkActive(editor);
    if (isActive) {
      Editor.removeMark(editor, 'bold');
    } else {
      Editor.addMark(editor, 'bold', true);
    }
  },

  toggleItalicMark(editor: Editor) {
    const isActive = CustomEditor.isItalicMarkActive(editor);
    if (isActive) {
      Editor.removeMark(editor, 'italic');
    } else {
      Editor.addMark(editor, 'italic', true);
    }
  },

  toggleUnderlineMark(editor: Editor) {
    const isActive = CustomEditor.isUnderlineMarkActive(editor);
    if (isActive) {
      Editor.removeMark(editor, 'underline');
    } else {
      Editor.addMark(editor, 'underline', true);
    }
  },

  toggleBulletedList(editor: Editor) {
    const isActive = CustomEditor.isBlockActive(editor, 'bulleted-list');
    Transforms.unwrapNodes(editor, {
      match: (n: Node) => {
        if (Editor.isEditor(n) || !Editor.isBlock(editor, n) || !('type' in n)) return false;
        return n.type === 'bulleted-list';
      },
      split: true,
    });
    const newProperties: Partial<CustomElement> = {
      type: isActive ? 'paragraph' : 'bulleted-list',
    };
    Transforms.setNodes(editor, newProperties);
    if (!isActive) {
      const block: CustomElement = { type: 'list-item', children: [] };
      Transforms.wrapNodes(editor, block);
    }
  },

  isBlockActive(editor: Editor, format: string) {
    const { selection } = editor;
    if (!selection) return false;

    const [match] = Array.from(
      Editor.nodes(editor, {
        at: Editor.unhangRange(editor, selection),
        match: (n: Node) => {
          if (Editor.isEditor(n) || !Editor.isBlock(editor, n) || !('type' in n)) return false;
          return n.type === format;
        },
      })
    );

    return !!match;
  },
};

// Define a serializing function that takes a value and returns a string.
const serialize = (value: Descendant[]): string => {
  return value
    .map(n => {
      if (Text.isText(n)) {
        const text = n as CustomText;
        let result = text.text;

        // Apply formatting based on marks
        if (text.bold) result = `**${result}**`;
        if (text.italic) result = `*${result}*`;
        if (text.underline) result = `__${result}__`;

        return result;
      }

      // Handle element nodes by recursively processing their children
      if ('children' in n && n.children) {
        const childrenText = serialize(n.children as Descendant[]);
        const element = n as CustomElement;

        if (element.type === 'paragraph') {
          return childrenText;
        } else if (element.type === 'list-item') {
          return `- ${childrenText}`;
        } else if (element.type === 'bulleted-list') {
          return childrenText; // List items will handle the bullet points
        }

        return childrenText;
      }

      return '';
    })
    .join('\n');
};

// Define a deserializing function that takes a string and returns a value.
const deserialize = (string: string): Descendant[] => {
  if (!string) return [{ type: 'paragraph', children: [{ text: '' }] } as CustomElement];

  const lines = string.split('\n');
  const result: Descendant[] = [];
  let currentList: Descendant[] | null = null;
  let listType: 'bulleted-list' | null = null;

  for (const line of lines) {
    // Check for bullet list item
    if (line.match(/^-\s+/)) {
      const content = line.substring(2); // Remove "- "
      const children = parseFormattedText(content);

      if (listType !== 'bulleted-list') {
        // Close previous list if exists
        if (currentList) {
          result.push({ type: listType!, children: currentList } as CustomElement);
        }
        // Start new bulleted list
        currentList = [];
        listType = 'bulleted-list';
      }

      if (currentList) {
        currentList.push({
          type: 'list-item',
          children: children.length > 0 ? children : [{ text: '' }],
        } as CustomElement);
      }
    }
    // Regular paragraph
    else {
      // Close any existing list
      if (currentList) {
        result.push({ type: listType!, children: currentList } as CustomElement);
        currentList = null;
        listType = null;
      }

      const children = parseFormattedText(line);
      result.push({
        type: 'paragraph',
        children: children.length > 0 ? children : [{ text: '' }],
      } as CustomElement);
    }
  }

  // Close any remaining list
  if (currentList) {
    result.push({ type: listType!, children: currentList } as CustomElement);
  }

  return result.length > 0
    ? result
    : [{ type: 'paragraph', children: [{ text: '' }] } as CustomElement];
};

// Helper function to parse formatted text (bold, italic, underline)
const parseFormattedText = (text: string): CustomText[] => {
  const children: CustomText[] = [];
  let remainingText = text;

  while (remainingText.length > 0) {
    // Check for bold (**text**)
    const boldMatch = remainingText.match(/^\*\*(.*?)\*\*/);
    if (boldMatch) {
      children.push({ text: boldMatch[1], bold: true });
      remainingText = remainingText.substring(boldMatch[0].length);
      continue;
    }

    // Check for italic (*text*)
    const italicMatch = remainingText.match(/^\*(.*?)\*/);
    if (italicMatch) {
      children.push({ text: italicMatch[1], italic: true });
      remainingText = remainingText.substring(italicMatch[0].length);
      continue;
    }

    // Check for underline (__text__)
    const underlineMatch = remainingText.match(/^__(.*?)__/);
    if (underlineMatch) {
      children.push({ text: underlineMatch[1], underline: true });
      remainingText = remainingText.substring(underlineMatch[0].length);
      continue;
    }

    // No formatting found, add as plain text
    const nextBold = remainingText.indexOf('**');
    const nextItalic = remainingText.indexOf('*');
    const nextUnderline = remainingText.indexOf('__');

    const nextFormat = Math.min(
      nextBold === -1 ? Infinity : nextBold,
      nextItalic === -1 ? Infinity : nextItalic,
      nextUnderline === -1 ? Infinity : nextUnderline
    );

    if (nextFormat === Infinity) {
      children.push({ text: remainingText });
      break;
    } else {
      children.push({ text: remainingText.substring(0, nextFormat) });
      remainingText = remainingText.substring(nextFormat);
    }
  }

  return children;
};

interface RichTextEditorProps {
  value: string | Descendant[];
  onChange: (value: string) => void;
  placeholder?: string;
  readOnly?: boolean;
  showToolbar?: boolean;
  autoSave?: boolean;
  onSave?: () => void;
  minHeight?: number;
  maxHeight?: number;
}

interface Element {
  type: string;
  children: Descendant[];
}

const Element = ({ attributes, children, element }: RenderElementProps) => {
  const customElement = element as CustomElement;
  switch (customElement.type) {
    case 'bulleted-list':
      return (
        <ul {...attributes} style={{ margin: 0, paddingLeft: 20 }}>
          {children}
        </ul>
      );
    case 'list-item':
      return <li {...attributes}>{children}</li>;
    default:
      return <p {...attributes}>{children}</p>;
  }
};

const Leaf = ({ attributes, children, leaf }: RenderLeafProps) => {
  const customLeaf = leaf as CustomText;
  if (customLeaf.bold) {
    children = <strong>{children}</strong>;
  }

  if (customLeaf.italic) {
    children = <em>{children}</em>;
  }

  if (customLeaf.underline) {
    children = <u>{children}</u>;
  }

  return <span {...attributes}>{children}</span>;
};

export const RichTextEditor: React.FC<RichTextEditorProps> = ({
  value,
  onChange,
  placeholder = 'Add notes...',
  readOnly = false,
  showToolbar = true,
  autoSave = false,
  onSave,
  minHeight = 100,
  maxHeight = 300,
}) => {
  const editor = useMemo(() => withHistory(withReact(createEditor())), []);

  const initialValue = useMemo(() => {
    if (value) {
      // If value is already a Slate.js value (array), use it directly
      if (Array.isArray(value)) {
        return value;
      }
      // If value is a string, deserialize it
      try {
        return deserialize(value);
      } catch {
        return [{ type: 'paragraph', children: [{ text: value }] }];
      }
    }
    return [{ type: 'paragraph', children: [{ text: '' }] }];
  }, [value]);

  const handleChange = useCallback(
    (newValue: Descendant[]) => {
      if (readOnly) return; // Don't call onChange in read-only mode

      const serialized = serialize(newValue);
      onChange(serialized);

      if (autoSave && onSave) {
        // Debounce auto-save
        setTimeout(() => {
          onSave();
        }, 1000);
      }
    },
    [onChange, autoSave, onSave, readOnly]
  );

  const handleKeyDown = useCallback(
    (event: React.KeyboardEvent) => {
      if (event.ctrlKey || event.metaKey) {
        switch (event.key) {
          case 'b':
            event.preventDefault();
            CustomEditor.toggleBoldMark(editor);
            break;
          case 'i':
            event.preventDefault();
            CustomEditor.toggleItalicMark(editor);
            break;
          case 'u':
            event.preventDefault();
            CustomEditor.toggleUnderlineMark(editor);
            break;
          case 's':
            event.preventDefault();
            if (onSave) {
              onSave();
            }
            break;
        }
      }
    },
    [editor, onSave]
  );

  return (
    <Box>
      {showToolbar && !readOnly && (
        <Paper elevation={1} sx={{ mb: 1 }}>
          <Toolbar variant="dense" sx={{ minHeight: 40 }}>
            <Tooltip title="Bold (Ctrl+B)">
              <IconButton
                size="small"
                onMouseDown={event => {
                  event.preventDefault();
                  CustomEditor.toggleBoldMark(editor);
                }}
                color={CustomEditor.isBoldMarkActive(editor) ? 'primary' : 'default'}
              >
                <FormatBold fontSize="small" />
              </IconButton>
            </Tooltip>

            <Tooltip title="Italic (Ctrl+I)">
              <IconButton
                size="small"
                onMouseDown={event => {
                  event.preventDefault();
                  CustomEditor.toggleItalicMark(editor);
                }}
                color={CustomEditor.isItalicMarkActive(editor) ? 'primary' : 'default'}
              >
                <FormatItalic fontSize="small" />
              </IconButton>
            </Tooltip>

            <Tooltip title="Underline (Ctrl+U)">
              <IconButton
                size="small"
                onMouseDown={event => {
                  event.preventDefault();
                  CustomEditor.toggleUnderlineMark(editor);
                }}
                color={CustomEditor.isUnderlineMarkActive(editor) ? 'primary' : 'default'}
              >
                <FormatUnderlined fontSize="small" />
              </IconButton>
            </Tooltip>

            <Divider orientation="vertical" flexItem sx={{ mx: 1 }} />

            <Tooltip title="Bulleted List">
              <IconButton
                size="small"
                onMouseDown={event => {
                  event.preventDefault();
                  CustomEditor.toggleBulletedList(editor);
                }}
                color={CustomEditor.isBlockActive(editor, 'bulleted-list') ? 'primary' : 'default'}
              >
                <FormatListBulleted fontSize="small" />
              </IconButton>
            </Tooltip>

            {onSave && (
              <React.Fragment>
                <Divider orientation="vertical" flexItem sx={{ mx: 1 }} />
                <Tooltip title="Save (Ctrl+S)">
                  <IconButton size="small" onClick={onSave} color="primary">
                    <Save fontSize="small" />
                  </IconButton>
                </Tooltip>
              </React.Fragment>
            )}
          </Toolbar>
        </Paper>
      )}

      <Paper
        elevation={1}
        sx={{
          minHeight,
          maxHeight,
          overflow: 'auto',
          border: 1,
          borderColor: 'divider',
          '&:focus-within': {
            borderColor: 'primary.main',
          },
        }}
      >
        <Slate editor={editor} initialValue={initialValue} onChange={handleChange}>
          <Editable
            renderElement={Element}
            renderLeaf={Leaf}
            placeholder={placeholder}
            readOnly={readOnly}
            onKeyDown={handleKeyDown}
            style={{
              padding: 12,
              minHeight: minHeight - 24,
              outline: 'none',
            }}
          />
        </Slate>
      </Paper>

      {!readOnly && (
        <Typography variant="caption" color="text.secondary" sx={{ mt: 0.5, display: 'block' }}>
          Use Ctrl+B, Ctrl+I, Ctrl+U for formatting. Ctrl+S to save.
        </Typography>
      )}
    </Box>
  );
};
