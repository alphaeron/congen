import { Box } from '@mui/material';
import React from 'react';

import { RichTextEditor } from './RichTextEditor';

import type { BoxProps } from '@mui/material';

interface RichTextDisplayProps extends Omit<BoxProps, 'children'> {
  content: string;
}

/**
 * Component to display rich text content using a read-only Slate editor.
 * This provides consistent formatting with the RichTextEditor while being secure.
 */
export const RichTextDisplay: React.FC<RichTextDisplayProps> = ({ content, ...boxProps }) => {
  // Convert markdown-like syntax back to Slate.js format for display
  const deserialize = (string: string) => {
    if (!string) return [{ type: 'paragraph', children: [{ text: '' }] }];

    const lines = string.split('\n');
    const result: Array<{
      type: string;
      children: Array<{ text: string; bold?: boolean; italic?: boolean; underline?: boolean }>;
    }> = [];
    let currentList: Array<{
      type: string;
      children: Array<{ text: string; bold?: boolean; italic?: boolean; underline?: boolean }>;
    }> | null = null;
    let listType: 'bulleted-list' | null = null;

    for (const line of lines) {
      // Check for bullet list item
      if (line.match(/^-\s+/)) {
        const content = line.substring(2); // Remove "- "
        const children = parseFormattedText(content);

        if (listType !== 'bulleted-list') {
          // Close previous list if exists
          if (currentList) {
            result.push({ type: listType!, children: currentList });
          }
          // Start new bulleted list
          currentList = [];
          listType = 'bulleted-list';
        }

        currentList.push({
          type: 'list-item',
          children: children.length > 0 ? children : [{ text: '' }],
        });
      }
      // Regular paragraph
      else {
        // Close any existing list
        if (currentList) {
          result.push({ type: listType!, children: currentList });
          currentList = null;
          listType = null;
        }

        const children = parseFormattedText(line);
        result.push({
          type: 'paragraph',
          children: children.length > 0 ? children : [{ text: '' }],
        });
      }
    }

    // Close any remaining list
    if (currentList) {
      result.push({ type: listType!, children: currentList });
    }

    return result.length > 0 ? result : [{ type: 'paragraph', children: [{ text: '' }] }];
  };

  // Helper function to parse formatted text (bold, italic, underline)
  const parseFormattedText = (
    text: string
  ): Array<{ text: string; bold?: boolean; italic?: boolean; underline?: boolean }> => {
    const children: Array<{ text: string; bold?: boolean; italic?: boolean; underline?: boolean }> =
      [];
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

  const slateValue = deserialize(content);

  return (
    <Box {...boxProps}>
      <RichTextEditor
        value={slateValue}
        onChange={() => {}} // No-op since it's read-only
        readOnly
        showToolbar={false}
        minHeight={0}
        maxHeight={undefined}
        sx={{
          '& .MuiPaper-root': {
            boxShadow: 'none',
            border: 'none',
            backgroundColor: 'transparent',
          },
          '& .MuiPaper-root .MuiPaper-root': {
            boxShadow: 'none',
            border: 'none',
            backgroundColor: 'transparent',
          },
        }}
      />
    </Box>
  );
};
