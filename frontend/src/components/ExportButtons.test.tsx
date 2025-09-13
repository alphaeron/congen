import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { SnackbarProvider } from 'notistack';
import React from 'react';

import { ExportButtons } from './ExportButtons';

// Mock the export utilities
jest.mock('../utils/exportUtils', () => ({
  printElement: jest.fn(),
}));

const mockOnExportPDF = jest.fn();
const mockOnExportXLSX = jest.fn();

const defaultProps = {
  onExportPDF: mockOnExportPDF,
  onExportXLSX: mockOnExportXLSX,
};

const renderWithSnackbar = (component: React.ReactElement) => {
  return render(
    <SnackbarProvider>
      {component}
    </SnackbarProvider>
  );
};

describe('ExportButtons', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders export button', () => {
    renderWithSnackbar(<ExportButtons {...defaultProps} />);
    
    const exportButton = screen.getByRole('button', { name: /export options/i });
    expect(exportButton).toBeInTheDocument();
  });

  it('opens menu when export button is clicked', () => {
    renderWithSnackbar(<ExportButtons {...defaultProps} />);
    
    const exportButton = screen.getByRole('button', { name: /export options/i });
    fireEvent.click(exportButton);
    
    expect(screen.getByText('Export as PDF')).toBeInTheDocument();
    expect(screen.getByText('Export as Excel')).toBeInTheDocument();
    expect(screen.getByText('Print')).toBeInTheDocument();
  });

  it('calls onExportPDF when PDF option is selected', async () => {
    mockOnExportPDF.mockResolvedValue(undefined);
    
    renderWithSnackbar(<ExportButtons {...defaultProps} />);
    
    const exportButton = screen.getByRole('button', { name: /export options/i });
    fireEvent.click(exportButton);
    
    const pdfOption = screen.getByText('Export as PDF');
    fireEvent.click(pdfOption);
    
    await waitFor(() => {
      expect(mockOnExportPDF).toHaveBeenCalledTimes(1);
    });
  });

  it('calls onExportXLSX when Excel option is selected', async () => {
    mockOnExportXLSX.mockResolvedValue(undefined);
    
    renderWithSnackbar(<ExportButtons {...defaultProps} />);
    
    const exportButton = screen.getByRole('button', { name: /export options/i });
    fireEvent.click(exportButton);
    
    const excelOption = screen.getByText('Export as Excel');
    fireEvent.click(excelOption);
    
    await waitFor(() => {
      expect(mockOnExportXLSX).toHaveBeenCalledTimes(1);
    });
  });

  it('calls printElement when Print option is selected', () => {
    const { printElement } = require('../utils/exportUtils');
    
    renderWithSnackbar(<ExportButtons {...defaultProps} />);
    
    const exportButton = screen.getByRole('button', { name: /export options/i });
    fireEvent.click(exportButton);
    
    const printOption = screen.getByText('Print');
    fireEvent.click(printOption);
    
    expect(printElement).toHaveBeenCalledWith();
  });

  it('disables button when disabled prop is true', () => {
    renderWithSnackbar(<ExportButtons {...defaultProps} disabled={true} />);
    
    const exportButton = screen.getByRole('button', { name: /export options/i });
    expect(exportButton).toBeDisabled();
  });

  it('shows loading state during PDF export', async () => {
    mockOnExportPDF.mockImplementation(() => new Promise(resolve => setTimeout(resolve, 100)));
    
    renderWithSnackbar(<ExportButtons {...defaultProps} />);
    
    const exportButton = screen.getByRole('button', { name: /export options/i });
    fireEvent.click(exportButton);
    
    const pdfOption = screen.getByText('Export as PDF');
    fireEvent.click(pdfOption);
    
    // Button should be disabled during export
    await waitFor(() => {
      expect(exportButton).toBeDisabled();
    });
  });

  it('handles PDF export error gracefully', async () => {
    mockOnExportPDF.mockRejectedValue(new Error('Export failed'));
    
    renderWithSnackbar(<ExportButtons {...defaultProps} />);
    
    const exportButton = screen.getByRole('button', { name: /export options/i });
    fireEvent.click(exportButton);
    
    const pdfOption = screen.getByText('Export as PDF');
    fireEvent.click(pdfOption);
    
    await waitFor(() => {
      expect(screen.getByText('Failed to export PDF')).toBeInTheDocument();
    });
  });

  it('handles XLSX export error gracefully', async () => {
    mockOnExportXLSX.mockRejectedValue(new Error('Export failed'));
    
    renderWithSnackbar(<ExportButtons {...defaultProps} />);
    
    const exportButton = screen.getByRole('button', { name: /export options/i });
    fireEvent.click(exportButton);
    
    const excelOption = screen.getByText('Export as Excel');
    fireEvent.click(excelOption);
    
    await waitFor(() => {
      expect(screen.getByText('Failed to export Excel file')).toBeInTheDocument();
    });
  });

  it('closes menu after option selection', async () => {
    mockOnExportXLSX.mockResolvedValue(undefined);
    
    renderWithSnackbar(<ExportButtons {...defaultProps} />);
    
    const exportButton = screen.getByRole('button', { name: /export options/i });
    fireEvent.click(exportButton);
    
    const excelOption = screen.getByText('Export as Excel');
    fireEvent.click(excelOption);
    
    // Wait for async operation to complete
    await waitFor(() => {
      expect(screen.queryByText('Export as PDF')).not.toBeInTheDocument();
    });
  });

  it('calls printElement for print', () => {
    const { printElement } = require('../utils/exportUtils');
    
    renderWithSnackbar(<ExportButtons {...defaultProps} />);
    
    const exportButton = screen.getByRole('button', { name: /export options/i });
    fireEvent.click(exportButton);
    
    const printOption = screen.getByText('Print');
    fireEvent.click(printOption);
    
    expect(printElement).toHaveBeenCalledWith();
  });
});
