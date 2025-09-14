import { PictureAsPdf as PdfIcon } from '@mui/icons-material';
import { IconButton, Tooltip } from '@mui/material';
import { useSnackbar } from 'notistack';
import React, { useState } from 'react';

interface ExportButtonsProps {
  onExportPDF: () => Promise<void>;
  disabled?: boolean;
}

/**
 * Simple PDF export button component
 */
export const ExportButtons: React.FC<ExportButtonsProps> = ({
  onExportPDF,
  disabled = false,
}) => {
  const { enqueueSnackbar } = useSnackbar();
  const [isExporting, setIsExporting] = useState(false);

  const handleExportPDF = async () => {
    try {
      setIsExporting(true);
      await onExportPDF();
      enqueueSnackbar('PDF opened in new tab', { variant: 'success' });
    } catch (error) {
      enqueueSnackbar('Failed to export PDF', { variant: 'error' });
    } finally {
      setIsExporting(false);
    }
  };

  return (
    <Tooltip title="Open PDF in new tab">
      <span>
        <IconButton
          onClick={handleExportPDF}
          disabled={disabled || isExporting}
          size="small"
          aria-label="Export as PDF"
          sx={{ 
            border: '1px solid',
            borderColor: 'divider',
            '&:hover': {
              backgroundColor: 'action.hover',
            }
          }}
        >
          <PdfIcon />
        </IconButton>
      </span>
    </Tooltip>
  );
};
