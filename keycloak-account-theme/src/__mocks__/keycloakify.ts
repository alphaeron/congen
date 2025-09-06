import React from 'react';

// Mock for keycloakify module
export const i18nBuilder = {
  withThemeName: () => ({
    build: () => ({
      useI18n: () => ({
        msg: (key: string) => key,
        msgStr: (key: string) => key,
      }),
      ofTypeI18n: (obj: unknown) => obj,
    }),
  }),
};

// Mock for keycloakify components
const DefaultPage = ({ children }: { children: React.ReactNode }) =>
  React.createElement('div', null, children);
const Template = ({ children }: { children: React.ReactNode }) =>
  React.createElement('div', null, children);

export default DefaultPage;
export { Template };
