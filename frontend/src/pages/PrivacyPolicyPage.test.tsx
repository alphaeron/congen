import { render, screen, waitFor } from '@testing-library/react';
import MockAdapter from 'axios-mock-adapter';
import * as React from 'react';

import { PrivacyPolicyPage } from './PrivacyPolicyPage';
import { ENDPOINT } from '../api/endpoint';

// Mock the endpoint
const mock = new MockAdapter(ENDPOINT);

const mockPrivacyPolicy = {
  version: '1.0.0',
  last_updated: '2023-08-09T10:15:30Z',
  data_controller: {
    name: 'Congen Fitness Application',
    contact: 'privacy@congen.app',
    dpo: 'dpo@congen.app',
  },
  data_processing: {
    purposes: ['Fitness tracking', 'Workout generation'],
    legal_basis: ['Consent', 'Legitimate interest'],
    data_types: ['Profile information', 'Exercise history'],
    retention_periods: {
      user_profile: '7 years after account deletion',
      exercise_history: '3 years',
    },
  },
  user_rights: {
    access: 'You can request a copy of your data',
    rectification: 'You can correct inaccurate data',
    erasure: 'You can request deletion of your data',
    portability: 'You can export your data',
    objection: 'You can object to data processing',
    complaint: 'You can file a complaint with the data protection authority',
  },
  contact_information: {
    privacy_email: 'privacy@congen.app',
    dpo_email: 'dpo@congen.app',
  },
};

// Custom render function with theme provider
const renderWithTheme = (component: React.ReactElement) => {
  return render(component);
};

describe('PrivacyPolicyPage', () => {
  beforeEach(() => {
    mock.reset();
  });

  afterAll(() => {
    mock.restore();
  });

  it('should render privacy policy content', async () => {
    mock.onGet('/gdpr/privacy_policy').reply(200, mockPrivacyPolicy);

    renderWithTheme(<PrivacyPolicyPage />);

    await waitFor(() => {
      expect(screen.getByText('Privacy Policy')).toBeInTheDocument();
    });

    // Check main sections
    expect(screen.getByText('Data Controller')).toBeInTheDocument();
    expect(screen.getByText('Data Processing')).toBeInTheDocument();
    expect(screen.getByText('Your Rights Under GDPR')).toBeInTheDocument();

    // Check data controller information
    expect(screen.getByText('Congen Fitness Application')).toBeInTheDocument();
    expect(screen.getByText('privacy@congen.app')).toBeInTheDocument();
    expect(screen.getByText('dpo@congen.app')).toBeInTheDocument();

    // Check data processing information
    expect(screen.getByText('Fitness tracking')).toBeInTheDocument();
    expect(screen.getByText('Workout generation')).toBeInTheDocument();
    expect(screen.getByText('Consent')).toBeInTheDocument();
    expect(screen.getByText('Legitimate interest')).toBeInTheDocument();

    // Check user rights
    expect(screen.getByText('Right of Access')).toBeInTheDocument();
    expect(screen.getByText('Right to Rectification')).toBeInTheDocument();
    expect(screen.getByText('Right to Erasure (Right to be Forgotten)')).toBeInTheDocument();
    expect(screen.getByText('Right to Data Portability')).toBeInTheDocument();
    expect(screen.getByText('Right to Object')).toBeInTheDocument();
    expect(screen.getByText('Right to File a Complaint')).toBeInTheDocument();

    // Check version and last updated
    expect(screen.getByText(/Version 1\.0\.0/)).toBeInTheDocument();
  });

  it('should handle loading state', async () => {
    mock.onGet('/gdpr/privacy_policy').reply(() => new Promise(() => {})); // Never resolves

    renderWithTheme(<PrivacyPolicyPage />);

    expect(screen.getByText('Loading Privacy Policy...')).toBeInTheDocument();
    expect(screen.getByRole('progressbar')).toBeInTheDocument();
  });

  it('should handle error state', async () => {
    mock.onGet('/gdpr/privacy_policy').reply(500, { message: 'Server error' });

    renderWithTheme(<PrivacyPolicyPage />);

    await waitFor(() => {
      expect(screen.getByText('Server error')).toBeInTheDocument();
    });
  });

  it('should handle network error', async () => {
    mock.onGet('/gdpr/privacy_policy').networkError();

    renderWithTheme(<PrivacyPolicyPage />);

    await waitFor(() => {
      expect(screen.getByText('Failed to load privacy policy')).toBeInTheDocument();
    });
  });

  it('should format data retention periods correctly', async () => {
    mock.onGet('/gdpr/privacy_policy').reply(200, mockPrivacyPolicy);

    renderWithTheme(<PrivacyPolicyPage />);

    await waitFor(() => {
      expect(screen.getByText('User Profile:')).toBeInTheDocument();
      expect(screen.getByText('7 years after account deletion')).toBeInTheDocument();
      expect(screen.getByText('Exercise History:')).toBeInTheDocument();
      expect(screen.getByText('3 years')).toBeInTheDocument();
    });
  });

  it('should display contact information', async () => {
    mock.onGet('/gdpr/privacy_policy').reply(200, mockPrivacyPolicy);

    renderWithTheme(<PrivacyPolicyPage />);

    await waitFor(() => {
      expect(screen.getByText('Questions about this privacy policy?')).toBeInTheDocument();
      expect(screen.getByText('privacy@congen.app')).toBeInTheDocument(); // Only one instance in the rendered component
    });
  }, 10000); // Increase timeout for this test

  it('should handle missing DPO information', async () => {
    const policyWithoutDPO = {
      ...mockPrivacyPolicy,
      data_controller: {
        name: 'Congen Fitness Application',
        contact: 'privacy@congen.app',
      },
    };

    mock.onGet('/gdpr/privacy_policy').reply(200, policyWithoutDPO);

    renderWithTheme(<PrivacyPolicyPage />);

    await waitFor(() => {
      expect(screen.getByText('Congen Fitness Application')).toBeInTheDocument();
      expect(screen.queryByText('Data Protection Officer:')).not.toBeInTheDocument();
    });
  });
});
