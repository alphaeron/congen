import { Container, Box, List, ListItem, ListItemText, Divider, Alert } from '@mui/material';
import { motion } from 'framer-motion';
import { useSnackbar } from 'notistack';
import React from 'react';

import { getPrivacyPolicy } from '../api/gdpr';
import type { PrivacyPolicy } from '../api/types';
import { formatDate } from '../common/utils';
import { GameCard, GameText, GAME_CLASSES } from '../components/GameTheme';
import { LoadingSpinner } from '../components/LoadingSpinner';

import type { AxiosError } from 'axios';

/**
 * Privacy Policy page component.
 *
 * This page displays the complete privacy policy and data processing information
 * as required by GDPR Articles 13 and 14. The page is accessible without authentication
 * to ensure transparency and compliance with GDPR requirements.
 *
 * ## GDPR Compliance
 * - Provides information about data controller
 * - Details data processing purposes and legal basis
 * - Explains user rights under GDPR
 * - Shows data retention periods
 * - Accessible without authentication
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
export function PrivacyPolicyPage(): React.ReactElement {
  const { enqueueSnackbar } = useSnackbar();
  const [privacyPolicy, setPrivacyPolicy] = React.useState<PrivacyPolicy | null>(null);
  const [loading, setLoading] = React.useState(true);

  React.useEffect(() => {
    const fetchPrivacyPolicy = async () => {
      try {
        const response = await getPrivacyPolicy();
        setPrivacyPolicy(response);
      } catch (err: unknown) {
        const axiosError = err as AxiosError<{ message?: string }>;
        enqueueSnackbar(axiosError.response?.data?.message || 'Failed to load privacy policy', {
          variant: 'error',
        });
      } finally {
        setLoading(false);
      }
    };

    fetchPrivacyPolicy();
  }, []);

  if (loading) {
    return (
      <Container maxWidth="md" sx={{ py: 4, textAlign: 'center' }}>
        <LoadingSpinner message="Loading Privacy Policy..." />
      </Container>
    );
  }

  if (!privacyPolicy) {
    return (
      <Container maxWidth="md" sx={{ py: 4 }}>
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6, ease: 'easeOut' }}
        >
          <GameCard className="glassmorphism-card">
            <Alert severity="error" sx={{ backgroundColor: 'transparent' }}>
              Privacy policy not available
            </Alert>
          </GameCard>
        </motion.div>
      </Container>
    );
  }

  return (
    <Container maxWidth="md" sx={{ py: 4 }}>
      {/* Header Section */}
      <motion.div
        initial={{ opacity: 0, y: -20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.4, delay: 0.1 }}
      >
        <GameCard className="glassmorphism-card">
          <Box sx={{ p: 3, textAlign: 'center' }}>
            <GameText variant="h1" className={GAME_CLASSES.textBold} sx={{ mb: 2 }}>
              Privacy Policy
            </GameText>
            <GameText variant="body1" className={GAME_CLASSES.textMuted}>
              Last updated: {formatDate(privacyPolicy.last_updated)} (Version{' '}
              {privacyPolicy.version})
            </GameText>
          </Box>
        </GameCard>
      </motion.div>

      {/* Contact Information */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.4, delay: 0.2 }}
        style={{ marginTop: '24px' }}
      >
        <GameCard className="glassmorphism-card">
          <Alert
            severity="info"
            sx={{ backgroundColor: 'transparent', border: '1px solid var(--game-cyan-border)' }}
          >
            <GameText variant="body2" className={GAME_CLASSES.textMedium}>
              <strong>Questions about this privacy policy?</strong>
              <br />
              Contact us at: {privacyPolicy.data_controller.contact}
            </GameText>
          </Alert>
        </GameCard>
      </motion.div>

      {/* Data Controller Section */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.4, delay: 0.3 }}
        style={{ marginTop: '24px' }}
      >
        <GameCard className="glassmorphism-card">
          <Box sx={{ p: 3 }}>
            <GameText variant="h4" className={GAME_CLASSES.textMedium} sx={{ mb: 3 }}>
              Data Controller
            </GameText>
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
              <Box>
                <GameText variant="body1" className={GAME_CLASSES.textBold}>
                  Name:
                </GameText>
                <GameText variant="body1" className={GAME_CLASSES.textMedium}>
                  {privacyPolicy.data_controller.name}
                </GameText>
              </Box>
              <Box>
                <GameText variant="body1" className={GAME_CLASSES.textBold}>
                  Contact:
                </GameText>
                <GameText variant="body1" className={GAME_CLASSES.textMedium}>
                  {privacyPolicy.data_controller.contact}
                </GameText>
              </Box>
              {privacyPolicy.data_controller.dpo && (
                <Box>
                  <GameText variant="body1" className={GAME_CLASSES.textBold}>
                    Data Protection Officer:
                  </GameText>
                  <GameText variant="body1" className={GAME_CLASSES.textMedium}>
                    {privacyPolicy.data_controller.dpo}
                  </GameText>
                </Box>
              )}
            </Box>
          </Box>
        </GameCard>
      </motion.div>

      {/* Data Processing Section */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.4, delay: 0.4 }}
        style={{ marginTop: '24px' }}
      >
        <GameCard className="glassmorphism-card">
          <Box sx={{ p: 3 }}>
            <GameText variant="h4" className={GAME_CLASSES.textMedium} sx={{ mb: 3 }}>
              Data Processing
            </GameText>

            <Box sx={{ mb: 3 }}>
              <GameText variant="h5" className={GAME_CLASSES.textMedium} sx={{ mb: 2 }}>
                Purposes of Processing
              </GameText>
              <List dense>
                {privacyPolicy.data_processing.purposes.map((purpose, index) => (
                  <motion.div
                    key={index}
                    initial={{ opacity: 0, x: -20 }}
                    animate={{ opacity: 1, x: 0 }}
                    transition={{ duration: 0.3, delay: 0.5 + index * 0.1 }}
                  >
                    <ListItem sx={{ py: 0.5 }}>
                      <ListItemText
                        primary={
                          <GameText variant="body2" className={GAME_CLASSES.textMedium}>
                            {purpose}
                          </GameText>
                        }
                      />
                    </ListItem>
                  </motion.div>
                ))}
              </List>
            </Box>

            <Box sx={{ mb: 3 }}>
              <GameText variant="h5" className={GAME_CLASSES.textMedium} sx={{ mb: 2 }}>
                Legal Basis
              </GameText>
              <List dense>
                {privacyPolicy.data_processing.legal_basis.map((basis, index) => (
                  <motion.div
                    key={index}
                    initial={{ opacity: 0, x: -20 }}
                    animate={{ opacity: 1, x: 0 }}
                    transition={{ duration: 0.3, delay: 0.6 + index * 0.1 }}
                  >
                    <ListItem sx={{ py: 0.5 }}>
                      <ListItemText
                        primary={
                          <GameText variant="body2" className={GAME_CLASSES.textMedium}>
                            {basis}
                          </GameText>
                        }
                      />
                    </ListItem>
                  </motion.div>
                ))}
              </List>
            </Box>

            <Box sx={{ mb: 3 }}>
              <GameText variant="h5" className={GAME_CLASSES.textMedium} sx={{ mb: 2 }}>
                Types of Data Collected
              </GameText>
              <List dense>
                {privacyPolicy.data_processing.data_types.map((dataType, index) => (
                  <motion.div
                    key={index}
                    initial={{ opacity: 0, x: -20 }}
                    animate={{ opacity: 1, x: 0 }}
                    transition={{ duration: 0.3, delay: 0.7 + index * 0.1 }}
                  >
                    <ListItem sx={{ py: 0.5 }}>
                      <ListItemText
                        primary={
                          <GameText variant="body2" className={GAME_CLASSES.textMedium}>
                            {dataType}
                          </GameText>
                        }
                      />
                    </ListItem>
                  </motion.div>
                ))}
              </List>
            </Box>

            <Box>
              <GameText variant="h5" className={GAME_CLASSES.textMedium} sx={{ mb: 2 }}>
                Data Retention Periods
              </GameText>
              {Object.entries(privacyPolicy.data_processing.retention_periods).map(
                ([dataType, period], index) => (
                  <motion.div
                    key={dataType}
                    initial={{ opacity: 0, x: -20 }}
                    animate={{ opacity: 1, x: 0 }}
                    transition={{ duration: 0.3, delay: 0.8 + index * 0.1 }}
                  >
                    <Box sx={{ mb: 1 }}>
                      <GameText variant="body2" className={GAME_CLASSES.textBold}>
                        {dataType.replace(/_/g, ' ').replace(/\b\w/g, l => l.toUpperCase())}:
                      </GameText>
                      <GameText variant="body2" className={GAME_CLASSES.textMedium}>
                        {' '}
                        {period}
                      </GameText>
                    </Box>
                  </motion.div>
                )
              )}
            </Box>
          </Box>
        </GameCard>
      </motion.div>

      {/* User Rights Section */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.4, delay: 0.5 }}
        style={{ marginTop: '24px' }}
      >
        <GameCard className="glassmorphism-card">
          <Box sx={{ p: 3 }}>
            <GameText variant="h4" className={GAME_CLASSES.textMedium} sx={{ mb: 3 }}>
              Your Rights
            </GameText>

            <Box sx={{ mb: 3 }}>
              <GameText variant="h5" className={GAME_CLASSES.textMedium} sx={{ mb: 2 }}>
                Right of Access
              </GameText>
              <GameText variant="body2" className={GAME_CLASSES.textMedium} paragraph>
                {privacyPolicy.user_rights.access}
              </GameText>
            </Box>

            <Divider sx={{ my: 2, borderColor: 'var(--game-cyan-border)' }} />

            <Box sx={{ mb: 3 }}>
              <GameText variant="h5" className={GAME_CLASSES.textMedium} sx={{ mb: 2 }}>
                Right to Rectification
              </GameText>
              <GameText variant="body2" className={GAME_CLASSES.textMedium} paragraph>
                {privacyPolicy.user_rights.rectification}
              </GameText>
            </Box>

            <Divider sx={{ my: 2, borderColor: 'var(--game-cyan-border)' }} />

            <Box sx={{ mb: 3 }}>
              <GameText variant="h5" className={GAME_CLASSES.textMedium} sx={{ mb: 2 }}>
                Right to Erasure (Right to be Forgotten)
              </GameText>
              <GameText variant="body2" className={GAME_CLASSES.textMedium}>
                {privacyPolicy.user_rights.erasure}
              </GameText>
            </Box>

            <Divider sx={{ my: 2, borderColor: 'var(--game-cyan-border)' }} />

            <Box sx={{ mb: 3 }}>
              <GameText variant="h5" className={GAME_CLASSES.textMedium} sx={{ mb: 2 }}>
                Right to Data Portability
              </GameText>
              <GameText variant="body2" className={GAME_CLASSES.textMedium}>
                {privacyPolicy.user_rights.portability}
              </GameText>
            </Box>

            <Divider sx={{ my: 2, borderColor: 'var(--game-cyan-border)' }} />

            <Box sx={{ mb: 3 }}>
              <GameText variant="h5" className={GAME_CLASSES.textMedium} sx={{ mb: 2 }}>
                Right to Object
              </GameText>
              <GameText variant="body2" className={GAME_CLASSES.textMedium}>
                {privacyPolicy.user_rights.objection}
              </GameText>
            </Box>

            <Divider sx={{ my: 2, borderColor: 'var(--game-cyan-border)' }} />

            <Box>
              <GameText variant="h5" className={GAME_CLASSES.textMedium} sx={{ mb: 2 }}>
                Right to File a Complaint
              </GameText>
              <GameText variant="body2" className={GAME_CLASSES.textMedium}>
                {privacyPolicy.user_rights.complaint}
              </GameText>
            </Box>
          </Box>
        </GameCard>
      </motion.div>
    </Container>
  );
}
