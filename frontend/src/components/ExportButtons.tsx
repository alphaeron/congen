import {
  PictureAsPdf as PdfIcon,
  TableChart as XlsxIcon,
  Print as PrintIcon,
} from '@mui/icons-material';
import { Box, IconButton, Tooltip, Menu, MenuItem, ListItemIcon, ListItemText } from '@mui/material';
import { useSnackbar } from 'notistack';
import React, { useState } from 'react';

import { printElement } from '../utils/exportUtils';

interface ExportButtonsProps {
  onExportPDF: () => Promise<void>;
  onExportXLSX: () => Promise<void>;
  disabled?: boolean;
}

/**
 * Export buttons component with PDF, XLSX, and Print options
 */
export const ExportButtons: React.FC<ExportButtonsProps> = ({
  onExportPDF,
  onExportXLSX,
  disabled = false,
}) => {
  const { enqueueSnackbar } = useSnackbar();
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const [isExporting, setIsExporting] = useState(false);

  const handleMenuOpen = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleMenuClose = () => {
    setAnchorEl(null);
  };

  const handleExportPDF = async () => {
    try {
      setIsExporting(true);
      await onExportPDF();
      enqueueSnackbar('PDF exported successfully', { variant: 'success' });
    } catch (error) {
      enqueueSnackbar('Failed to export PDF', { variant: 'error' });
    } finally {
      setIsExporting(false);
      handleMenuClose();
    }
  };

  const handleExportXLSX = async () => {
    try {
      setIsExporting(true);
      await onExportXLSX();
      enqueueSnackbar('Excel file exported successfully', { variant: 'success' });
    } catch (error) {
      enqueueSnackbar('Failed to export Excel file', { variant: 'error' });
    } finally {
      setIsExporting(false);
      handleMenuClose();
    }
  };

  const handlePrint = () => {
    try {
      printElement();
      enqueueSnackbar('Print dialog opened', { variant: 'info' });
    } catch (error) {
      enqueueSnackbar('Failed to open print dialog', { variant: 'error' });
    }
    handleMenuClose();
  };

  return (
    <React.Fragment>
      <Box sx={{ display: 'flex', gap: 1 }}>
        <Tooltip title="Export Options">
          <span>
            <IconButton
              onClick={handleMenuOpen}
              disabled={disabled || isExporting}
              size="small"
              aria-label="Export Options"
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
      </Box>

      <Menu
        anchorEl={anchorEl}
        open={Boolean(anchorEl)}
        onClose={handleMenuClose}
        anchorOrigin={{
          vertical: 'bottom',
          horizontal: 'right',
        }}
        transformOrigin={{
          vertical: 'top',
          horizontal: 'right',
        }}
      >
        <MenuItem onClick={handleExportPDF} disabled={isExporting}>
          <ListItemIcon>
            <PdfIcon fontSize="small" />
          </ListItemIcon>
          <ListItemText primary="Export as PDF" />
        </MenuItem>
        
        <MenuItem onClick={handleExportXLSX}>
          <ListItemIcon>
            <XlsxIcon fontSize="small" />
          </ListItemIcon>
          <ListItemText primary="Export as Excel" />
        </MenuItem>
        
        <MenuItem onClick={handlePrint}>
          <ListItemIcon>
            <PrintIcon fontSize="small" />
          </ListItemIcon>
          <ListItemText primary="Print" />
        </MenuItem>
      </Menu>
    </React.Fragment>
  );
};
