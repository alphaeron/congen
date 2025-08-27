import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import MockAdapter from 'axios-mock-adapter';
import { SnackbarProvider } from 'notistack';
import * as React from 'react';
import { MemoryRouter } from 'react-router';

import { GdprComplianceSection } from './GdprComplianceSection';
import { ENDPOINT } from '../api/endpoint';
import type { UserConsent } from '../api/types';

// Mock the endpoint
const mock = new MockAdapter(ENDPOINT);

const renderWithProviders = (component: React.ReactElement) => {
  return render(
    <SnackbarProvider>
      <MemoryRouter>{component}</MemoryRouter>
    </SnackbarProvider>
  );
};

const mockConsentStatus: UserConsent = {
  keycloak_id: 'test-user-123',
  data_processing_consent: true,
  consent_timestamp: '2023-08-09T10:15:30Z',
  updated_at: '2023-08-09T10:15:30Z',
};

describe('GdprComplianceSection', () => {
  beforeEach(() => {
    mock.reset();
  });

  afterAll(() => {
    mock.restore();
  });

  it('should render GDPR compliance section with consent status', async () => {
    mock.onGet('/gdpr/consent').reply(200, mockConsentStatus);

    renderWithProviders(<GdprComplianceSection />);

    // Wait for content to load
    await waitFor(() => {
      expect(screen.getByText('Privacy & Data Protection')).toBeInTheDocument();
    });

    // Check sections
    expect(screen.getByText('Data Processing Consent')).toBeInTheDocument();
    expect(screen.getByText('Your Data Rights')).toBeInTheDocument();
    expect(screen.getByText('Consent Given')).toBeInTheDocument();
    expect(screen.getByText('Withdraw Consent')).toBeInTheDocument();

    // Check GDPR actions
    expect(screen.getByText('Export Your Data')).toBeInTheDocument();
    expect(screen.getByText('Privacy Policy')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Delete All Data' })).toBeInTheDocument();
  });

  it('should handle consent withdrawal', async () => {
    mock.onGet('/gdpr/consent').reply(200, mockConsentStatus);
    mock.onPost('/gdpr/consent').reply(200, { success: true, message: 'Consent withdrawn' });

    const user = userEvent.setup();
    renderWithProviders(<GdprComplianceSection />);

    await waitFor(() => {
      expect(screen.getByRole('button', { name: 'Withdraw Consent' })).toBeInTheDocument();
    });

    // Click withdraw consent
    await user.click(screen.getByRole('button', { name: 'Withdraw Consent' }));

    // Check dialog opens
    expect(screen.getByRole('heading', { name: 'Withdraw Consent' })).toBeInTheDocument();
    expect(screen.getByText('I withdraw consent for data processing')).toBeInTheDocument();

    // Select withdraw option and confirm
    await user.click(screen.getByLabelText('I withdraw consent for data processing'));
    await user.click(screen.getByText('Confirm'));

    await waitFor(() => {
      expect(mock.history.post).toHaveLength(1);
      expect(mock.history.post[0].params).toEqual({ consent: false });
    });
  });

  it('should handle data deletion with confirmation', async () => {
    mock.onGet('/gdpr/consent').reply(200, mockConsentStatus);
    mock.onDelete('/gdpr/delete_all_data').reply(200, { success: true, message: 'Data deleted' });

    const user = userEvent.setup();
    renderWithProviders(<GdprComplianceSection />);

    await waitFor(() => {
      expect(screen.getByRole('button', { name: 'Delete All Data' })).toBeInTheDocument();
    });

    // Click delete all data
    await user.click(screen.getByRole('button', { name: 'Delete All Data' }));

    // Check dialog opens
    expect(screen.getByText('Delete All Personal Data')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('DELETE_ALL_MY_DATA')).toBeInTheDocument();

    // Type confirmation
    await user.type(screen.getByPlaceholderText('DELETE_ALL_MY_DATA'), 'DELETE_ALL_MY_DATA');

    // Confirm deletion
    await user.click(screen.getByRole('button', { name: 'Delete All Data' }));

    await waitFor(() => {
      expect(mock.history.delete).toHaveLength(1);
      expect(mock.history.delete[0].params).toEqual({ confirmation: 'DELETE_ALL_MY_DATA' });
    });
  });

  it('should handle consent withdrawn status', async () => {
    const withdrawnConsentStatus: UserConsent = {
      keycloak_id: 'test-user-123',
      data_processing_consent: false,
      consent_timestamp: undefined,
      updated_at: '2023-08-09T10:15:30Z',
    };

    mock.onGet('/gdpr/consent').reply(200, withdrawnConsentStatus);

    renderWithProviders(<GdprComplianceSection />);

    await waitFor(() => {
      expect(screen.getByText('Consent Withdrawn')).toBeInTheDocument();
      expect(screen.getByText('Give Consent')).toBeInTheDocument();
    });
  });

  it('should handle loading state', async () => {
    mock.onGet('/gdpr/consent').reply(() => new Promise(() => {})); // Never resolves

    renderWithProviders(<GdprComplianceSection />);

    await waitFor(() => {
      expect(screen.getByText('Loading GDPR compliance status...')).toBeInTheDocument();
    });
    expect(screen.getByRole('progressbar')).toBeInTheDocument();
  });

  it('should handle error state', async () => {
    mock.onGet('/gdpr/consent').reply(500, { message: 'Server error' });

    renderWithProviders(<GdprComplianceSection />);

    await waitFor(() => {
      expect(screen.getByText('Server error')).toBeInTheDocument();
    });
  });

  it('should validate delete confirmation text', async () => {
    mock.onGet('/gdpr/consent').reply(200, mockConsentStatus);

    const user = userEvent.setup();
    renderWithProviders(<GdprComplianceSection />);

    await waitFor(() => {
      expect(screen.getByRole('button', { name: 'Delete All Data' })).toBeInTheDocument();
    });

    // Click delete all data
    await user.click(screen.getByRole('button', { name: 'Delete All Data' }));

    // Type incorrect confirmation
    await user.type(screen.getByPlaceholderText('DELETE_ALL_MY_DATA'), 'WRONG_TEXT');

    // Delete button should be disabled
    expect(screen.getByRole('button', { name: 'Delete All Data' })).toBeDisabled();
    expect(screen.getByText('Please type exactly "DELETE_ALL_MY_DATA"')).toBeInTheDocument();
  });

  it('should have privacy policy link', async () => {
    mock.onGet('/gdpr/consent').reply(200, mockConsentStatus);

    renderWithProviders(<GdprComplianceSection />);

    await waitFor(() => {
      const privacyPolicyLink = screen.getByText('View Policy').closest('a');
      expect(privacyPolicyLink).toHaveAttribute('href', '/privacy_policy');
    });
  });
});
