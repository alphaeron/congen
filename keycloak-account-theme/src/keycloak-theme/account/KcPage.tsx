import { lazy } from "react";
import type { KcContext } from "./KcContext";

const KcAccountUi = lazy(() => import("./KcAccountUi"));

export default function KcPage(props: { kcContext: KcContext }) {
    const { kcContext } = props;
    
    // Debug logging
    console.log('KcPage - kcContext:', kcContext);

    return <KcAccountUi kcContext={kcContext} />;
}