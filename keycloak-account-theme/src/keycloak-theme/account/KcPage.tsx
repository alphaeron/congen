import { lazy } from 'react';
import type { KcContext } from './KcContext';

const KcAccountUi = lazy(() => import('./KcAccountUi'));

export default function KcPage(props: { kcContext: KcContext }) {
  const { kcContext } = props;
  return <KcAccountUi kcContext={kcContext} />;
}
