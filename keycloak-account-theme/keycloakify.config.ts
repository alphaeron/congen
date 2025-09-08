import { defineConfig } from 'keycloakify';

export default defineConfig({
  themeName: 'congen-account-theme',
  themeId: 'congen-account-theme',
  themeType: 'account',
  loginTheme: false,
  accountTheme: true,
  extraThemeProperties: [
    "parent=keycloak.v3",
    "darkMode=true",
    "deprecatedMode=false",
    "favIcon=img/favicon.ico"
  ],
  extraAssets: [
    {
      from: "public/favicon.ico",
      to: "img/favicon.ico"
    }
  ]
});
