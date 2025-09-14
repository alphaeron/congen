import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { SnackbarProvider } from 'notistack';
import React from 'react';

import { ExportButtons } from './ExportButtons';

// Mock the export utilities
jest.mock('../utils/exportUtils', () => ({}));

const mockOnExportPDF = jest.fn();

const defaultProps = {
  onExportPDF: mockOnExportPDF,
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

  it('renders PDF export button', () => {
    renderWithSnackbar(<ExportButtons {...defaultProps} />);
    
    const exportButton = screen.getByRole('button', { name: /export as pdf/i });
    expect(exportButton).toBeInTheDocument();
  });

  it('calls onExportPDF when button is clicked', async () => {
    mockOnExportPDF.mockResolvedValue(undefined);
    
    renderWithSnackbar(<ExportButtons {...defaultProps} />);
    
    const exportButton = screen.getByRole('button', { name: /export as pdf/i });
    fireEvent.click(exportButton);
    
    await waitFor(() => {
      expect(mockOnExportPDF).toHaveBeenCalledTimes(1);
    });
  });

  it('disables button when disabled prop is true', () => {
    renderWithSnackbar(<ExportButtons {...defaultProps} disabled={true} />);
    
    const exportButton = screen.getByRole('button', { name: /export as pdf/i });
    expect(exportButton).toBeDisabled();
  });

  it('shows loading state during PDF export', async () => {
    mockOnExportPDF.mockImplementation(() => new Promise(resolve => setTimeout(resolve, 100)));
    
    renderWithSnackbar(<ExportButtons {...defaultProps} />);
    
    const exportButton = screen.getByRole('button', { name: /export as pdf/i });
    fireEvent.click(exportButton);
    
    // Button should be disabled during export
    await waitFor(() => {
      expect(exportButton).toBeDisabled();
    });
  });

  it('handles PDF export error gracefully', async () => {
    mockOnExportPDF.mockRejectedValue(new Error('Export failed'));
    
    renderWithSnackbar(<ExportButtons {...defaultProps} />);
    
    const exportButton = screen.getByRole('button', { name: /export as pdf/i });
    fireEvent.click(exportButton);
    
    await waitFor(() => {
      expect(screen.getByText('Failed to export PDF')).toBeInTheDocument();
    });
  });

  it('shows success message on successful export', async () => {
    mockOnExportPDF.mockResolvedValue(undefined);
    
    renderWithSnackbar(<ExportButtons {...defaultProps} />);
    
    const exportButton = screen.getByRole('button', { name: /export as pdf/i });
    fireEvent.click(exportButton);
    
    await waitFor(() => {
      expect(screen.getByText('PDF exported successfully')).toBeInTheDocument();
    });
  });
});