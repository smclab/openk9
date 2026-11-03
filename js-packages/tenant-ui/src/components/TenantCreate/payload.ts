import { CreateTenantRequest, SecurityConfiguration } from "../../openapi-generated";
import { WizardState } from "./types";

export function buildCreateTenantRequest(state: WizardState): CreateTenantRequest {
  const { tenantName, virtualHost, clientId, issuerUri } = state.step1;
  const trimmedClientId = clientId.trim();
  const trimmedIssuerUri = issuerUri.trim();

  // OAuth2Settings requires both clientId and issuerUri; omit it entirely
  // (both-or-neither) so an empty form triggers Keycloak realm auto-provisioning.
  const oAuth2Settings =
    trimmedClientId && trimmedIssuerUri
      ? { clientId: trimmedClientId, issuerUri: trimmedIssuerUri }
      : undefined;

  return {
    tenantName: tenantName.trim() || undefined,
    virtualHost: virtualHost.trim(),
    securityConfiguration: state.step2.securityConfiguration as SecurityConfiguration,
    oAuth2Settings,
  };
}
