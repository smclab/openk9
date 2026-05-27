import { buildCreateTenantRequest } from "../payload";
import { isStep1Valid } from "../Step1Form";
import { SecurityConfigurationKey, WizardState } from "../types";

function makeState(
  step1: Partial<WizardState["step1"]>,
  securityConfiguration: SecurityConfigurationKey | null = "OAUTH2_ADMIN_ONLY"
): WizardState {
  return {
    step1: { tenantName: "demo", virtualHost: "demo.openk9.io", clientId: "", clientSecret: "", issuerUri: "", ...step1 },
    step2: { securityConfiguration },
  };
}

describe("buildCreateTenantRequest", () => {
  it("omits oAuth2Settings when clientId and issuerUri are empty", () => {
    const req = buildCreateTenantRequest(makeState({}));
    expect(req.oAuth2Settings).toBeUndefined();
    expect(req.tenantName).toBe("demo");
    expect(req.virtualHost).toBe("demo.openk9.io");
    expect(req.securityConfiguration).toBe("OAUTH2_ADMIN_ONLY");
  });

  it("populates oAuth2Settings when both clientId and issuerUri are present", () => {
    const req = buildCreateTenantRequest(makeState({ clientId: "cid", issuerUri: "https://idp" }));
    expect(req.oAuth2Settings).toEqual({ clientId: "cid", issuerUri: "https://idp", clientSecret: undefined });
  });

  it("includes clientSecret when provided", () => {
    const req = buildCreateTenantRequest(makeState({ clientId: "cid", issuerUri: "https://idp", clientSecret: "sec" }));
    expect(req.oAuth2Settings?.clientSecret).toBe("sec");
  });
});

describe("isStep1Valid", () => {
  it("is valid without OAuth2 fields", () => {
    expect(isStep1Valid(makeState({}).step1)).toBe(true);
  });

  it("is valid with both OAuth2 fields", () => {
    expect(isStep1Valid(makeState({ clientId: "cid", issuerUri: "https://idp" }).step1)).toBe(true);
  });

  it("is invalid with only clientId", () => {
    expect(isStep1Valid(makeState({ clientId: "cid" }).step1)).toBe(false);
  });

  it("is invalid with only issuerUri", () => {
    expect(isStep1Valid(makeState({ issuerUri: "https://idp" }).step1)).toBe(false);
  });

  it("is invalid without tenantName", () => {
    expect(isStep1Valid(makeState({ tenantName: "" }).step1)).toBe(false);
  });
});
