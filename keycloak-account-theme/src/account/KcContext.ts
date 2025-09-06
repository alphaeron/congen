/* eslint-disable @typescript-eslint/ban-types */
import type { ExtendKcContext } from "keycloakify/account";

export type KcContextExtension = {
  themeName: "congen-account-theme";
  properties: Record<string, string> & {};
};

export type KcContextExtensionPerPage = {};

export type KcContext = ExtendKcContext<KcContextExtension, KcContextExtensionPerPage>;
