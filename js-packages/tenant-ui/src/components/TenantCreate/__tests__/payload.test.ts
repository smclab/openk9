import { buildCreateTenantRequest } from "../payload";
import { deriveVirtualHost, isStep1Valid } from "../Step1Form";
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

  it("omits tenantName when left empty", () => {
    const req = buildCreateTenantRequest(makeState({ tenantName: "" }));
    expect(req.tenantName).toBeUndefined();
    expect(req.virtualHost).toBe("demo.openk9.io");
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

  it("is valid without tenantName", () => {
    expect(isStep1Valid(makeState({ tenantName: "" }).step1)).toBe(true);
  });

  it("is invalid without virtualHost", () => {
    expect(isStep1Valid(makeState({ virtualHost: "" }).step1)).toBe(false);
  });
});

describe("deriveVirtualHost", () => {
  it("prepends tenantName to the last two labels of the hostname", () => {
    expect(deriveVirtualHost("demo", "tenant-manager.openk9.localhost")).toBe("demo.openk9.localhost");
    expect(deriveVirtualHost("acme", "tenant-manager-x.ok9.it")).toBe("acme.ok9.it");
  });

  it("returns an empty string when tenantName is blank", () => {
    expect(deriveVirtualHost("", "tenant-manager.openk9.localhost")).toBe("");
    expect(deriveVirtualHost("   ", "tenant-manager.openk9.localhost")).toBe("");
  });
});
