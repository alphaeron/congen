import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import InfoIcon from '@mui/icons-material/Info';
import {
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  Collapse,
  IconButton,
  Link,
  Stack,
  Typography,
} from '@mui/material';
import { alpha } from '@mui/material/styles';
import * as React from 'react';

import { useCookie } from '../contexts/CookieContext';

interface CookieConsentBannerProps {
  onClose?: () => void;
}

export const CookieConsentBanner: React.FC<CookieConsentBannerProps> = ({ onClose }) => {
  const { acceptAll } = useCookie();
  const [expanded, setExpanded] = React.useState(false);

  const handleAccept = () => {
    acceptAll();
    onClose?.();
  };

  const toggleExpanded = () => {
    setExpanded(!expanded);
  };

  return (
    <Card
      sx={{
        position: 'fixed',
        bottom: 16,
        right: 16,
        zIndex: 1300,
        width: 400,
        boxShadow: theme => `0 8px 32px ${alpha(theme.palette.common.black, 0.12)}`,
        borderRadius: 3,
        border: theme => `1px solid ${alpha(theme.palette.divider, 0.5)}`,
        bgcolor: 'background.paper',
        backdropFilter: 'blur(20px)',
      }}
    >
      <CardContent sx={{ p: 3 }}>
        <Stack spacing={2}>
          {/* Header */}
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <InfoIcon
              sx={{
                color: 'primary.main',
                fontSize: 24,
                flexShrink: 0,
              }}
            />
            <Typography variant="h6" component="h2">
              Cookie Notice
            </Typography>
          </Box>
          <Typography variant="caption" color="text.secondary" sx={{ mt: -1, mb: 2 }}>
            We use essential cookies to ensure our website functions properly, including
            authentication and security features.
          </Typography>

          {/* Cookie Information */}
          <Box>
            <Box
              sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 1 }}
            >
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <Chip label="Essential" size="small" color="success" variant="outlined" />
                <Typography variant="subtitle2">Authentication Cookies</Typography>
              </Box>
              <IconButton
                size="small"
                onClick={toggleExpanded}
                sx={{
                  transform: expanded ? 'rotate(180deg)' : 'rotate(0deg)',
                  transition: 'transform 0.2s',
                }}
              >
                <ExpandMoreIcon />
              </IconButton>
            </Box>
            <Collapse in={expanded}>
              <Typography variant="caption" color="text.secondary" sx={{ mb: 2 }}>
                These cookies are essential for the website to function properly. They enable secure
                authentication and maintain your login session.
              </Typography>
            </Collapse>
          </Box>

          {/* Action */}
          <Box sx={{ display: 'flex', justifyContent: 'flex-end' }}>
            <Button
              variant="contained"
              onClick={handleAccept}
              sx={{ borderRadius: 2, textTransform: 'none' }}
            >
              Accept
            </Button>
          </Box>

          {/* Footer */}
          <Typography variant="caption" color="text.secondary" sx={{ textAlign: 'center' }}>
            By using our website, you agree to our{' '}
            <Link href="/privacy_policy" color="primary" underline="hover">
              Privacy Policy
            </Link>
            .
          </Typography>
        </Stack>
      </CardContent>
    </Card>
  );
};
