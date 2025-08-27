import {
  PrivacyTip as PrivacyIcon,
  Download as DownloadIcon,
  DeleteForever as DeleteForeverIcon,
  CheckCircle as CheckCircleIcon,
  Cancel as CancelIcon,
  Policy as PolicyIcon,
} from '@mui/icons-material';
import {
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  Divider,
  FormControl,
  FormControlLabel,
  FormLabel,
  Radio,
  RadioGroup,
  TextField,
  Typography,
  Alert,
  Snackbar,
  CircularProgress,
} from '@mui/material';
import { useSnackbar } from 'notistack';
import React from 'react';
import { Link } from 'react-router';

import {
  recordConsent,
  getConsentStatus,
  exportUserData,
  deleteAllPersonalData,
} from '../api/gdpr';
import type { UserConsent } from '../api/types';

import type { AxiosError } from 'axios';

/**
 * GDPR Compliance Section component for user profile.
 *
 * This component provides users with all required GDPR actions including:
 * - Consent management (give/withdraw consent)
 * - Data export (Right to Data Portability)
 * - Data deletion (Right to be Forgotten)
 * - Privacy policy access
 *
 * ## GDPR Rights Implemented
 * - Article 7: Consent management
 * - Article 15: Right of Access (data export)
 * - Article 17: Right to Erasure (data deletion)
 * - Article 20: Right to Data Portability
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
export function GdprComplianceSection(): React.ReactElement {
  const { enqueueSnackbar } = useSnackbar();
  const [consentStatus, setConsentStatus] = React.useState<UserConsent | null>(null);
  const [loading, setLoading] = React.useState(true);
  const [operationLoading, setOperationLoading] = React.useState<string | null>(null);
  const [successMessage, setSuccessMessage] = React.useState<string | null>(null);

  // Dialog states
  const [consentDialogOpen, setConsentDialogOpen] = React.useState(false);
  const [deleteDialogOpen, setDeleteDialogOpen] = React.useState(false);
  const [newConsentValue, setNewConsentValue] = React.useState<boolean>(true);
  const [deleteConfirmation, setDeleteConfirmation] = React.useState('');

  // Load initial consent status
  React.useEffect(() => {
    loadConsentStatus();
  }, []);

  const loadConsentStatus = async () => {
    try {
      setLoading(true);
      const response = await getConsentStatus();
      setConsentStatus(response.data);
    } catch (err: unknown) {
      const axiosError = err as AxiosError<{ message?: string }>;
      enqueueSnackbar(axiosError.response?.data?.message || 'Failed to load consent status', {
        variant: 'error',
      });
    } finally {
      setLoading(false);
    }
  };

  const handleConsentChange = async () => {
    try {
      setOperationLoading('consent');
      await recordConsent(newConsentValue);
      await loadConsentStatus(); // Refresh status
      setConsentDialogOpen(false);
      setSuccessMessage(
        newConsentValue ? 'Consent given successfully' : 'Consent withdrawn successfully'
      );
    } catch (err: unknown) {
      const axiosError = err as AxiosError<{ message?: string }>;
      enqueueSnackbar(axiosError.response?.data?.message || 'Failed to update consent', {
        variant: 'error',
      });
    } finally {
      setOperationLoading(null);
    }
  };

  const handleDataExport = async () => {
    try {
      setOperationLoading('export');
      const response = await exportUserData();

      // Create and download JSON file
      const dataStr = JSON.stringify(response.data, null, 2);
      const dataBlob = new Blob([dataStr], { type: 'application/json' });
      const url = URL.createObjectURL(dataBlob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `congen-personal-data-${new Date().toISOString().split('T')[0]}.json`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      URL.revokeObjectURL(url);

      setSuccessMessage('Personal data exported successfully');
    } catch (err: unknown) {
      const axiosError = err as AxiosError<{ message?: string }>;
      enqueueSnackbar(axiosError.response?.data?.message || 'Failed to export data', {
        variant: 'error',
      });
    } finally {
      setOperationLoading(null);
    }
  };

  const handleDataDeletion = async () => {
    if (deleteConfirmation !== 'DELETE_ALL_MY_DATA') {
      enqueueSnackbar('Please type "DELETE_ALL_MY_DATA" to confirm deletion', { variant: 'error' });
      return;
    }

    try {
      setOperationLoading('delete');
      await deleteAllPersonalData(deleteConfirmation);
      setDeleteDialogOpen(false);
      setSuccessMessage('All personal data has been deleted. You will be logged out shortly.');
      // In a real app, you'd redirect to logout here
      setTimeout(() => {
        window.location.href = '/login';
      }, 3000);
    } catch (err: unknown) {
      const axiosError = err as AxiosError<{ message?: string }>;
      enqueueSnackbar(axiosError.response?.data?.message || 'Failed to delete data', {
        variant: 'error',
      });
    } finally {
      setOperationLoading(null);
    }
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleString();
  };

  if (loading) {
    return (
      <Card>
        <CardContent>
          <Box display="flex" alignItems="center" gap={2}>
            <CircularProgress size={24} />
            <Typography>Loading GDPR compliance status...</Typography>
          </Box>
        </CardContent>
      </Card>
    );
  }

  return (
    <React.Fragment>
      <Card>
        <CardContent>
          <Box display="flex" alignItems="center" gap={2} mb={2}>
            <PrivacyIcon color="primary" />
            <Typography variant="h6">Privacy & Data Protection</Typography>
          </Box>

          <Typography variant="body2" color="text.secondary" paragraph>
            Manage your data and privacy settings. You have the right to control how your personal
            data is processed.
          </Typography>

          <Divider sx={{ my: 2 }} />

          {/* Consent Status */}
          <Box sx={{ mb: 3 }}>
            <Typography variant="subtitle1" gutterBottom>
              Data Processing Consent
            </Typography>
            <Box display="flex" alignItems="center" gap={2} mb={2}>
              {consentStatus?.data_processing_consent ? (
                <Chip
                  icon={<CheckCircleIcon />}
                  label="Consent Given"
                  color="success"
                  size="small"
                />
              ) : (
                <Chip icon={<CancelIcon />} label="Consent Withdrawn" color="error" size="small" />
              )}
              {consentStatus?.consent_timestamp && (
                <Typography variant="caption" color="text.secondary">
                  Last updated: {formatDate(consentStatus.consent_timestamp)}
                </Typography>
              )}
            </Box>
            <Button
              variant="outlined"
              size="small"
              onClick={() => {
                setNewConsentValue(!consentStatus?.data_processing_consent);
                setConsentDialogOpen(true);
              }}
            >
              {consentStatus?.data_processing_consent ? 'Withdraw Consent' : 'Give Consent'}
            </Button>
          </Box>

          <Divider sx={{ my: 2 }} />

          {/* GDPR Actions */}
          <Typography variant="subtitle1" gutterBottom>
            Your Data Rights
          </Typography>

          <Box display="flex" flexDirection="column" gap={2}>
            {/* Data Export */}
            <Box display="flex" justifyContent="space-between" alignItems="center">
              <Box>
                <Typography variant="body2" fontWeight="medium">
                  Export Your Data
                </Typography>
                <Typography variant="caption" color="text.secondary">
                  Download all your personal data in JSON format
                </Typography>
              </Box>
              <Button
                variant="outlined"
                size="small"
                startIcon={<DownloadIcon />}
                onClick={handleDataExport}
                disabled={operationLoading === 'export'}
              >
                {operationLoading === 'export' ? 'Exporting...' : 'Export Data'}
              </Button>
            </Box>

            {/* Privacy Policy */}
            <Box display="flex" justifyContent="space-between" alignItems="center">
              <Box>
                <Typography variant="body2" fontWeight="medium">
                  Privacy Policy
                </Typography>
                <Typography variant="caption" color="text.secondary">
                  View our privacy policy and data processing information
                </Typography>
              </Box>
              <Button
                variant="outlined"
                size="small"
                startIcon={<PolicyIcon />}
                component={Link}
                to="/privacy_policy"
              >
                View Policy
              </Button>
            </Box>

            {/* Data Deletion */}
            <Box display="flex" justifyContent="space-between" alignItems="center">
              <Box>
                <Typography variant="body2" fontWeight="medium" color="error">
                  Delete All Data
                </Typography>
                <Typography variant="caption" color="text.secondary">
                  Permanently delete all your personal data
                </Typography>
              </Box>
              <Button
                variant="outlined"
                size="small"
                color="error"
                startIcon={<DeleteForeverIcon />}
                onClick={() => setDeleteDialogOpen(true)}
                disabled={operationLoading === 'delete'}
              >
                Delete All Data
              </Button>
            </Box>
          </Box>
        </CardContent>
      </Card>

      {/* Consent Dialog */}
      <Dialog
        open={consentDialogOpen}
        onClose={() => setConsentDialogOpen(false)}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle>{newConsentValue ? 'Give Consent' : 'Withdraw Consent'}</DialogTitle>
        <DialogContent>
          <DialogContentText paragraph>
            {newConsentValue
              ? 'By giving consent, you allow us to process your personal data for the purposes outlined in our privacy policy.'
              : 'By withdrawing consent, you revoke permission for us to process your personal data. Some features may become unavailable.'}
          </DialogContentText>

          <FormControl component="fieldset">
            <FormLabel component="legend">Your choice:</FormLabel>
            <RadioGroup
              value={newConsentValue}
              onChange={e => setNewConsentValue(e.target.value === 'true')}
            >
              <FormControlLabel
                value={true}
                control={<Radio />}
                label="I give consent for data processing"
              />
              <FormControlLabel
                value={false}
                control={<Radio />}
                label="I withdraw consent for data processing"
              />
            </RadioGroup>
          </FormControl>
        </DialogContent>
        <DialogActions>
          <Button
            onClick={() => setConsentDialogOpen(false)}
            disabled={operationLoading === 'consent'}
          >
            Cancel
          </Button>
          <Button
            onClick={handleConsentChange}
            variant="contained"
            disabled={operationLoading === 'consent'}
          >
            {operationLoading === 'consent' ? 'Updating...' : 'Confirm'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <Dialog
        open={deleteDialogOpen}
        onClose={() => setDeleteDialogOpen(false)}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle color="error">Delete All Personal Data</DialogTitle>
        <DialogContent>
          <DialogContentText paragraph>
            <strong>This action cannot be undone!</strong>
          </DialogContentText>
          <DialogContentText paragraph>
            This will permanently delete all your personal data including:
          </DialogContentText>
          <Typography component="ul" variant="body2" color="text.secondary">
            <li>Profile information</li>
            <li>Exercise preferences and history</li>
            <li>Workout programs and progress</li>
            <li>Account settings and preferences</li>
          </Typography>
          <DialogContentText paragraph sx={{ mt: 2 }}>
            To confirm deletion, please type <strong>DELETE_ALL_MY_DATA</strong> below:
          </DialogContentText>
          <TextField
            fullWidth
            variant="outlined"
            placeholder="DELETE_ALL_MY_DATA"
            value={deleteConfirmation}
            onChange={e => setDeleteConfirmation(e.target.value)}
            error={deleteConfirmation !== '' && deleteConfirmation !== 'DELETE_ALL_MY_DATA'}
            helperText={
              deleteConfirmation !== '' && deleteConfirmation !== 'DELETE_ALL_MY_DATA'
                ? 'Please type exactly "DELETE_ALL_MY_DATA"'
                : ''
            }
          />
        </DialogContent>
        <DialogActions>
          <Button
            onClick={() => setDeleteDialogOpen(false)}
            disabled={operationLoading === 'delete'}
          >
            Cancel
          </Button>
          <Button
            onClick={handleDataDeletion}
            color="error"
            variant="contained"
            disabled={operationLoading === 'delete' || deleteConfirmation !== 'DELETE_ALL_MY_DATA'}
          >
            {operationLoading === 'delete' ? 'Deleting...' : 'Delete All Data'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Success/Error Snackbars */}
      <Snackbar
        open={!!successMessage}
        autoHideDuration={6000}
        onClose={() => setSuccessMessage(null)}
      >
        <Alert severity="success" onClose={() => setSuccessMessage(null)}>
          {successMessage}
        </Alert>
      </Snackbar>
    </React.Fragment>
  );
}
