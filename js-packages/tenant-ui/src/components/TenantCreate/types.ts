export type SecurityConfigurationKey =
  | "OAUTH2_ADMIN_ONLY"
  | "OAUTH2_SEARCH"
  | "OAUTH2_SEARCH_WITH_API_KEY"
  | "OAUTH2_ADMIN_WITH_API_KEY"
  | "NO_GATEWAY_AUTH";

export type WizardState = {
  step1: {
    tenantName: string;
    virtualHost: string;
    clientId: string;
    clientSecret: string;
    issuerUri: string;
  };
  step2: {
    securityConfiguration: SecurityConfigurationKey | null;
  };
};

export const initialWizardState: WizardState = {
  step1: {
    tenantName: "",
    virtualHost: "",
    clientId: "",
    clientSecret: "",
    issuerUri: "",
  },
  step2: {
    securityConfiguration: null,
  },
};

export const securityConfigLabel: Record<SecurityConfigurationKey, string> = {
  OAUTH2_ADMIN_ONLY: "OAuth2 admin only",
  OAUTH2_SEARCH: "OAuth2 admin and search",
  OAUTH2_SEARCH_WITH_API_KEY: "OAuth2 admin/search + API keys for data",
  OAUTH2_ADMIN_WITH_API_KEY: "OAuth2 admin + API keys everywhere else",
  NO_GATEWAY_AUTH: "No gateway authentication",
};

export const securityConfigDescription: Record<SecurityConfigurationKey, string> = {
  OAUTH2_ADMIN_ONLY: "Admin routes require OAuth2. Public and search are open; ingestion requires API keys.",
  OAUTH2_SEARCH: "Admin and search require OAuth2. Public is open; ingestion requires API keys.",
  OAUTH2_SEARCH_WITH_API_KEY: "Admin and search require OAuth2. Public and ingestion require API keys.",
  OAUTH2_ADMIN_WITH_API_KEY: "Admin requires OAuth2. Public, search, and ingestion require API keys.",
  NO_GATEWAY_AUTH: "No gateway-level authorization on any route. Downstream services handle security.",
};

export const authSchemeLabel: Record<string, string> = {
  OAUTH2: "OAuth2",
  API_KEY: "API Key",
  NO_AUTH: "Open",
};

export const authSchemeColor: Record<string, "primary" | "warning" | "default"> = {
  OAUTH2: "primary",
  API_KEY: "warning",
  NO_AUTH: "default",
};

export const apiGroupLabel: Record<string, string> = {
  ADMINISTRATION: "Administration",
  PUBLIC: "Public",
  SEARCH: "Search",
  INGESTION: "Ingestion",
};
