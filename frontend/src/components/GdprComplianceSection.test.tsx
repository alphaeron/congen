import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import MockAdapter from 'axios-mock-adapter';
import * as React from 'react';
import { BrowserRouter } from 'react-router';

import { GdprComplianceSection } from './GdprComplianceSection';
import { ENDPOINT } from '../api/endpoint';
import type { UserConsent } from '../api/types';

// Mock the endpoint
const mock = new MockAdapter(ENDPOINT);

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

    render(
      <BrowserRouter>
        <GdprComplianceSection />
      </BrowserRouter>
    );

    // Check loading state
    expect(screen.getByText('Loading GDPR compliance status...')).toBeInTheDocument();

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
    render(
      <BrowserRouter>
        <GdprComplianceSection />
      </BrowserRouter>
    );

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

  it('should handle data export', async () => {
    mock.onGet('/gdpr/consent').reply(200, mockConsentStatus);
    mock.onGet('/gdpr/export').reply(200, {
      personalData: { profile: { name: 'Test User' } },
      metadata: { exportedAt: '2023-08-09T10:15:30Z', dataTypes: ['profile'], totalRecords: 1 },
    });

    // Mock URL.createObjectURL and document.createElement
    const mockCreateObjectURL = jest.fn(() => 'blob:mock-url');
    const mockRevokeObjectURL = jest.fn();
    global.URL.createObjectURL = mockCreateObjectURL;
    global.URL.revokeObjectURL = mockRevokeObjectURL;

    const mockClick = jest.fn();
    const mockLink = {
      href: '',
      download: '',
      click: mockClick,
      style: { display: '' },
    };
    const originalCreateElement = document.createElement;
    document.createElement = jest.fn(tagName => {
      if (tagName === 'a') {
        return mockLink as unknown as HTMLAnchorElement;
      }
      return originalCreateElement.call(document, tagName);
    });

    const mockAppendChild = jest.fn();
    const mockRemoveChild = jest.fn();
    document.body.appendChild = mockAppendChild;
    document.body.removeChild = mockRemoveChild;

    const user = userEvent.setup();
    render(
      <BrowserRouter>
        <GdprComplianceSection />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('Export Data')).toBeInTheDocument();
    });

    // Click export data
    await user.click(screen.getByText('Export Data'));

    await waitFor(() => {
      expect(mock.history.get).toHaveLength(2); // Initial consent status + export
      expect(mockCreateObjectURL).toHaveBeenCalled();
      expect(mockClick).toHaveBeenCalled();
    });

    // Restore mocks
    document.createElement = originalCreateElement;
  });

  it('should handle data deletion with confirmation', async () => {
    mock.onGet('/gdpr/consent').reply(200, mockConsentStatus);
    mock.onDelete('/gdpr/delete_all_data').reply(200, { success: true, message: 'Data deleted' });

    const user = userEvent.setup();
    render(
      <BrowserRouter>
        <GdprComplianceSection />
      </BrowserRouter>
    );

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

    render(
      <BrowserRouter>
        <GdprComplianceSection />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('Consent Withdrawn')).toBeInTheDocument();
      expect(screen.getByText('Give Consent')).toBeInTheDocument();
    });
  });

  it('should handle loading state', () => {
    mock.onGet('/gdpr/consent').reply(() => new Promise(() => {})); // Never resolves

    render(
      <BrowserRouter>
        <GdprComplianceSection />
      </BrowserRouter>
    );

    expect(screen.getByText('Loading GDPR compliance status...')).toBeInTheDocument();
    expect(screen.getByRole('progressbar')).toBeInTheDocument();
  });

  it('should handle error state', async () => {
    mock.onGet('/gdpr/consent').reply(500, { message: 'Server error' });

    render(
      <BrowserRouter>
        <GdprComplianceSection />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('Server error')).toBeInTheDocument();
    });
  });

  it('should validate delete confirmation text', async () => {
    mock.onGet('/gdpr/consent').reply(200, mockConsentStatus);

    const user = userEvent.setup();
    render(
      <BrowserRouter>
        <GdprComplianceSection />
      </BrowserRouter>
    );

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

    render(
      <BrowserRouter>
        <GdprComplianceSection />
      </BrowserRouter>
    );

    await waitFor(() => {
      const privacyPolicyLink = screen.getByText('View Policy').closest('a');
      expect(privacyPolicyLink).toHaveAttribute('href', '/privacy_policy');
    });
  });

  it('should handle API errors during consent update', async () => {
    mock.onGet('/gdpr/consent').reply(200, mockConsentStatus);
    mock.onPost('/gdpr/consent').reply(500, { message: 'Database connection failed' });

    const user = userEvent.setup();
    render(
      <BrowserRouter>
        <GdprComplianceSection />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByRole('button', { name: 'Withdraw Consent' })).toBeInTheDocument();
    });

    // Click withdraw consent
    await user.click(screen.getByRole('button', { name: 'Withdraw Consent' }));

    // Select withdraw option and confirm
    await user.click(screen.getByLabelText('I withdraw consent for data processing'));
    await user.click(screen.getByText('Confirm'));

    // Check error message appears
    await waitFor(() => {
      expect(screen.getByText('Database connection failed')).toBeInTheDocument();
    });

    // Dialog should remain open for user to retry
    expect(screen.getByRole('heading', { name: 'Withdraw Consent' })).toBeInTheDocument();
  });

  it('should handle API errors during data export', async () => {
    mock.onGet('/gdpr/consent').reply(200, mockConsentStatus);
    mock.onGet('/gdpr/export').reply(500, { message: 'Export service unavailable' });

    const user = userEvent.setup();
    render(
      <BrowserRouter>
        <GdprComplianceSection />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('Export Data')).toBeInTheDocument();
    });

    // Click export data
    await user.click(screen.getByText('Export Data'));

    // Check error message appears
    await waitFor(() => {
      expect(screen.getByText('Export service unavailable')).toBeInTheDocument();
    });
  });

  it('should handle API errors during data deletion', async () => {
    mock.onGet('/gdpr/consent').reply(200, mockConsentStatus);
    mock.onDelete('/gdpr/delete_all_data').reply(500, { message: 'Deletion service unavailable' });

    const user = userEvent.setup();
    render(
      <BrowserRouter>
        <GdprComplianceSection />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByRole('button', { name: 'Delete All Data' })).toBeInTheDocument();
    });

    // Click delete all data
    await user.click(screen.getByRole('button', { name: 'Delete All Data' }));

    // Type confirmation
    await user.type(screen.getByPlaceholderText('DELETE_ALL_MY_DATA'), 'DELETE_ALL_MY_DATA');

    // Confirm deletion
    await user.click(screen.getByRole('button', { name: 'Delete All Data' }));

    // Check error message appears
    await waitFor(() => {
      expect(screen.getByText('Deletion service unavailable')).toBeInTheDocument();
    });

    // Dialog should remain open for user to retry
    expect(screen.getByText('Delete All Personal Data')).toBeInTheDocument();
  });

  it('should show success messages for successful operations', async () => {
    mock.onGet('/gdpr/consent').reply(200, mockConsentStatus);
    mock.onPost('/gdpr/consent').reply(200, { success: true });

    const user = userEvent.setup();
    render(
      <BrowserRouter>
        <GdprComplianceSection />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByRole('button', { name: 'Withdraw Consent' })).toBeInTheDocument();
    });

    // Click withdraw consent
    await user.click(screen.getByRole('button', { name: 'Withdraw Consent' }));

    // Select withdraw option and confirm
    await user.click(screen.getByLabelText('I withdraw consent for data processing'));
    await user.click(screen.getByText('Confirm'));

    // Check success message appears
    await waitFor(() => {
      expect(screen.getByText('Consent withdrawn successfully')).toBeInTheDocument();
    });
  });

  it('should handle network errors gracefully', async () => {
    mock.onGet('/gdpr/consent').reply(() => {
      throw new Error('Network error');
    });

    render(
      <BrowserRouter>
        <GdprComplianceSection />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('Failed to load consent status')).toBeInTheDocument();
    });
  });

  it('should validate consent timestamp display', async () => {
    const consentWithTimestamp: UserConsent = {
      keycloak_id: 'test-user-123',
      data_processing_consent: true,
      consent_timestamp: '2023-08-09T10:15:30Z',
      updated_at: '2023-08-09T10:15:30Z',
    };

    mock.onGet('/gdpr/consent').reply(200, consentWithTimestamp);

    render(
      <BrowserRouter>
        <GdprComplianceSection />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByText(/Last updated:/)).toBeInTheDocument();
    });
  });

  it('should handle consent status without timestamp', async () => {
    const consentWithoutTimestamp: UserConsent = {
      keycloak_id: 'test-user-123',
      data_processing_consent: false,
      consent_timestamp: undefined,
      updated_at: '2023-08-09T10:15:30Z',
    };

    mock.onGet('/gdpr/consent').reply(200, consentWithoutTimestamp);

    render(
      <BrowserRouter>
        <GdprComplianceSection />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('Consent Withdrawn')).toBeInTheDocument();
      expect(screen.getByText('Give Consent')).toBeInTheDocument();
    });

    // Should not show timestamp when consent is withdrawn
    expect(screen.queryByText(/Last updated:/)).not.toBeInTheDocument();
  });

  it('should close success and error snackbars', async () => {
    mock.onGet('/gdpr/consent').reply(200, mockConsentStatus);
    mock.onPost('/gdpr/consent').reply(200, { success: true });

    const user = userEvent.setup();
    render(
      <BrowserRouter>
        <GdprComplianceSection />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByRole('button', { name: 'Withdraw Consent' })).toBeInTheDocument();
    });

    // Trigger success message
    await user.click(screen.getByRole('button', { name: 'Withdraw Consent' }));
    await user.click(screen.getByLabelText('I withdraw consent for data processing'));
    await user.click(screen.getByText('Confirm'));

    await waitFor(() => {
      expect(screen.getByText('Consent withdrawn successfully')).toBeInTheDocument();
    });

    // Close success snackbar
    const closeButton = screen.getByRole('button', { name: /close/i });
    await user.click(closeButton);

    await waitFor(() => {
      expect(screen.queryByText('Consent withdrawn successfully')).not.toBeInTheDocument();
    });
  });
});
