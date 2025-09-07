import { Suspense, lazy } from 'react';
import type { ClassKey } from 'keycloakify/account';
import type { KcContext } from './KcContext';
import { useI18n } from './i18n';
import DefaultPage from 'keycloakify/account/DefaultPage';
import Template from 'keycloakify/account/Template';
import CircularProgress from '@mui/material/CircularProgress';

// Lazy load the heavy account overview component
const Account = lazy(() => import('./Account'));

export default function KcPage(props: { kcContext: KcContext }) {
  const { kcContext } = props;

  const { i18n } = useI18n({ kcContext });

  return (
    <Suspense
      fallback={
        <div
          style={{
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center',
            minHeight: '100vh',
          }}
        >
          <CircularProgress size={60} />
        </div>
      }
    >
      {(() => {
        switch (kcContext.pageId) {
          case 'account.ftl':
            return <Account kcContext={kcContext} i18n={i18n} />;
          default:
            return (
              <DefaultPage
                kcContext={kcContext}
                i18n={i18n}
                classes={classes}
                Template={Template}
                doUseDefaultCss={false}
              />
            );
        }
      })()}
    </Suspense>
  );
}

const classes = {} satisfies { [key in ClassKey]?: string };
