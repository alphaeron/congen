import { defineConfig } from "keycloakify";

export default defineConfig({
  themeName: "congen-account-theme",
  themeId: "congen-account-theme",
  themeType: "account",
  // Only build the account theme, not the login theme
  loginTheme: false,
  accountTheme: true,
  // Enable account theme
  accountThemeEnabled: true,
});
